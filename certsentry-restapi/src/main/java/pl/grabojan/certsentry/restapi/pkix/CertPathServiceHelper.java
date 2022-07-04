package pl.grabojan.certsentry.restapi.pkix;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.PKIXRevocationChecker.Option;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.util.StopWatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.data.model.CertIdentity;
import pl.grabojan.certsentry.data.model.Profile;
import pl.grabojan.certsentry.data.model.SupplyPoint;
import pl.grabojan.certsentry.data.model.SupplyPointType;
import pl.grabojan.certsentry.data.service.CertIdentityDataService;
import pl.grabojan.certsentry.util.CertificateServiceHelper;

@Slf4j
@RequiredArgsConstructor
public class CertPathServiceHelper {
	
	private boolean oneLevel = true;
	private boolean revokePreferOCSP = true;
	private boolean revokePreferCDP = false;
	private boolean revokeOcspUseNonce = false;
	
	private final CertificateServiceHelper certificateServiceHelper;
	private final CertIdentityDataService certIdentityDataService;
	private final PkixURIResolver pkiUriResolver;
		
	public void setOneLevel(boolean oneLevel) {
		this.oneLevel = oneLevel;
		log.debug("OneLevel param value: {}", oneLevel);
	}

	public void setRevokePreferOCSP(boolean revokePreferOCSP) {
		this.revokePreferOCSP = revokePreferOCSP;
		log.debug("RevokePreferOCSP param value: {}", revokePreferOCSP);
	}

	public void setRevokePreferCDP(boolean revokePreferCDP) {
		this.revokePreferCDP = revokePreferCDP;
		log.debug("RevokePreferCDP param value: {}", revokePreferCDP);
	}
	
	public void setRevokeOcspUseNonce(boolean revokeOcspUseNonce) {
		this.revokeOcspUseNonce = revokeOcspUseNonce;
		log.debug("RevokeOcspUseNonceP param value: {}", revokeOcspUseNonce);
	}
	
	public List<X509Certificate> buildPathAndValidate(X509Certificate userCert, Profile profile) 
			throws CertPathNotFoundException, CertPathNotValidException, CertPathPartialValidException  {
		
		// pre-check
		abortIfCertIsExpired(userCert);
		
		StopWatch sw = new StopWatch();
		
		try {
			sw.start("resolveIssuersCerts");
			List<X509Certificate> cAs = resolveIssuersCerts(userCert, profile);
			sw.stop();
					
			sw.start("buildPath");
			MyCertPath cp = buildPath(userCert, cAs);
			sw.stop();
			
			sw.start("checkRevocationInfo");
			RevocationData revData = checkRevocationInfo(toCertList(cp));
			sw.stop();
			
			sw.start("validatePath");
			validatePath(cp.getAnchor(), cp.getCertPath(), revData);
			sw.stop();
			
			return toCertList(cp);
		} finally {
			if(sw.isRunning()) {
				sw.stop();
			}
			log.info("Perf Stats {}", sw.prettyPrint());			
		}
		
		
	}
	
	protected void abortIfCertIsExpired(X509Certificate cert) throws CertPathNotValidException {
		try {
			cert.checkValidity();
			log.debug("Target certificate within valid date range");
		} catch (CertificateNotYetValidException|CertificateExpiredException e) {
			String msg = "Target certificate is expired";
			log.error(msg);
			throw new CertPathNotValidException(msg);
		} 
	}
	
	protected List<X509Certificate> resolveIssuersCerts(X509Certificate targetCert, Profile profile) {
		
		log.debug("Resolving issuers for cert: {}", targetCert);
		
		List<X509Certificate> cAs = new ArrayList<>();
		
		String issuerName = certificateServiceHelper.resolveX500Principal(targetCert.getIssuerX500Principal());
		Set<String> names = new HashSet<>(Collections.singleton(issuerName));
		
		do {
			Set<String> tempNames = new HashSet<>();
		
			names.forEach(name -> {
				log.debug("Getting issuers with name: {}", name);
				List<CertIdentity> certIssuers = certIdentityDataService.getIssuers(name);
				certIssuers.forEach(ci -> { 
					
					if(matchProfile(ci,profile)) {
						log.debug("Found matching CertIssuer: [{}]", ci);
						cAs.add(toX509Certificate(ci));
						tempNames.add(ci.getIssuer());
					}
				});				
			});
			// replace names -> another iteration for issuer issuers etc... until root found
			names = tempNames;
			
		} while(!haveTailRootCA(cAs) && !names.isEmpty() && oneLevel == false);	
		
		if(cAs.isEmpty()) {
			String msg = "unable to find any issuer by name " + issuerName;
			log.error(msg);
			throw new CertPathNotFoundException(msg);
		}
		
		// recheck path have Root only if full path to root required
		while(!oneLevel && !haveTailRootCA(cAs)) {
			log.info("Not root cert found, getting from cAIssuers");
			List<X509Certificate> certTemp = new ArrayList<>(cAs);
			Collections.reverse(certTemp);
			
			Set<X500Principal> subject = new HashSet<>();
			certTemp.forEach(c -> {
				if(subject.isEmpty() || subject.contains(c.getSubjectX500Principal())) { // only same subject 
					subject.add(c.getSubjectX500Principal());
					String issuerUri = certificateServiceHelper.getCaIssuersURI(c);
					log.info("Downloading certificate file from: {}", issuerUri);
					X509Certificate issuerCert = pkiUriResolver.getCertificate(issuerUri);
					cAs.add(issuerCert);
				}
			});
		}
		
		log.debug("Revolved certificates: {}", cAs);
		
		return cAs;
	}
	
	protected MyCertPath buildPath(X509Certificate userCert, List<X509Certificate> caCerts) throws CertPathNotFoundException {
		
		log.debug("Building CertPath");
		
		CertPathBuilder cpb = createCertPathBuilder();
		
		Set<TrustAnchor> rootCAs = new HashSet<>();
		
		List<X509Certificate> pathCerts = new ArrayList<>();
				
	    X509CertSelector targetConstraints = new X509CertSelector();
	    targetConstraints.setCertificate(userCert);
	    pathCerts.add(userCert);
	    
	    if(oneLevel) {
	    	makeTrustCerts(caCerts, rootCAs);
	    } else {
	    	splitCerts(caCerts, rootCAs, pathCerts);
	    }
	    
	    log.debug("Root CAs set: {}", rootCAs);
	    log.debug("SubCAs set: {}", pathCerts);
	    	    
	    PKIXBuilderParameters params = null;
		try {
			params = new PKIXBuilderParameters(rootCAs, targetConstraints);
		} catch (InvalidAlgorithmParameterException e) {
			log.error("PKIXBuilderParameters ctor() failed", e);
			throw new CertPathFailureException("Unable to create PKIXBuilderParameters instance",e);
		}
	    params.addCertStore(createCertStore(pathCerts));
		params.setRevocationEnabled(false); // only building path
	  	    
	    try {
	        PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) cpb.build(params);
	        log.info("Found CertPath");
	        return new MyCertPath(result.getTrustAnchor(), result.getCertPath());
	        
	    } catch (CertPathBuilderException e) {
	    	log.error("Building CertPath failed", e);
	    	throw new CertPathNotFoundException("CertPath not found", e);
	    } catch (InvalidAlgorithmParameterException e) {
	    	log.error("Invalid algorithm found by CertPath builder", e);
	    	throw new CertPathFailureException("Build failed Invalid Algorithm", e);
	    }
	}
	
	protected RevocationData checkRevocationInfo(List<X509Certificate> certPath) {	
		
		log.debug("Checking revocation infos");
		log.debug("CertPath to check: {}", certPath);
		log.debug("OneLevel?={}, RevokePreferOCSP?={}, RevokePreferCDP?={}, RevokeOcspUseNonce?={}", 
				oneLevel, revokePreferOCSP, revokePreferCDP, revokeOcspUseNonce);
		
		RevocationData revData = new RevocationData();

		// CertPath is constructed in revers order - from usercert by subca to rootca
		for(int idx=0; idx < certPath.size()-1; ++idx) {
			X509Certificate xc = certPath.get(idx);
			log.debug("Checking cert with subject: [{}]", xc.getSubjectX500Principal());
						
			if((revokePreferOCSP && certificateServiceHelper.getOcspResponderURI(xc) != null) ||
					(revokePreferCDP && certificateServiceHelper.getCRLDPServerURI(xc) != null)) {
			
				log.debug("Cert have OCSP/CDP extensions");
				continue;
			}
			
			// no OCSP or CDP found in cert - try use supplyPoint from TSL
			X509Certificate xcIssuer = certPath.get(idx+1);			
			List<SupplyPoint> suppPoints = certIdentityDataService.getCertIdentitySupplyPoints(
					xcIssuer.getSerialNumber().toString(),
					certificateServiceHelper.resolveX500Principal(xcIssuer.getIssuerX500Principal()));
						
			if(suppPoints.isEmpty()) {
				log.error("No revocation information found in cert and no SupplyPoint available");
				throw new CertPathPartialValidException("No revocation data available");
			}
			
			log.debug("Found SupplyPoint(s): [{}]", suppPoints);
						
			// simple map - only one record by type
			Map<SupplyPointType,SupplyPoint> supps = suppPoints.
					stream().
					collect(Collectors.toMap(s -> s.getType(),
											Function.identity(),
											(existing, replacement) -> existing));
						
			if(revokePreferOCSP && supps.containsKey(SupplyPointType.OCSP)) {
				String ocspUri = supps.get(SupplyPointType.OCSP).getPointUri();
				try {
					byte[] ocspNonce = (revokeOcspUseNonce) ? createOcspNonce(xc) : null;
					byte[] ocspReq = generateOCSPRequest(xcIssuer,xc,ocspNonce);
					byte[] ocspResp = pkiUriResolver.resolveWithPost(ocspUri, ocspReq);
					revData.getOcspResponses().put(xc, Arrays.asList(ocspResp, ocspNonce));
					log.debug("Added RevocationData from SupplyPoint [{}]",
							supps.get(SupplyPointType.OCSP));
				} catch(UnresolvedResourceException e) {
					log.error("Unresolved resource for OCSP " + ocspUri, e);
					throw new CertPathPartialValidException("Unresolved resource", e);
				}		
			} else if(supps.containsKey(SupplyPointType.CRL)) {
				String crlUri = supps.get(SupplyPointType.CRL).getPointUri();
				try {
					X509CRL crl = pkiUriResolver.getCrl(crlUri);
					revData.getRevocationLists().add(crl);
					log.debug("Added RevocationData from SupplyPoint [{}]",
							supps.get(SupplyPointType.CRL));
				} catch(UnresolvedResourceException e) {
					log.error("Unresolved resource for CDP " + crlUri, e);
					throw new CertPathPartialValidException("Unresolved resource", e);
				}
			} else {
				log.error("No prefered SupplyPoint available");
				throw new CertPathPartialValidException("No revocation data available");
			}
			
		}
		
		return revData;
	}
	
	protected PublicKey validatePath(TrustAnchor trustAnchor, CertPath cp, RevocationData addRevData) throws CertPathNotValidException, CertPathPartialValidException {
		
		log.debug("Validating path");
		log.debug("TrustAnchor: {}", trustAnchor);
		log.debug("CertPath: {}", cp);
		log.debug("RevocationData: {}", addRevData);
		
		CertPathValidator cpv = createCertPathValidator();
		
		PKIXParameters params = null;
		try {
			params = new PKIXParameters(Collections.singleton(trustAnchor));
		} catch (InvalidAlgorithmParameterException e) {
			throw new CertPathFailureException("Unable to create PKIXBuilderParameters instance",e);
		}
		
		params.setRevocationEnabled(false); // handled by RevocationChecker
		PKIXRevocationChecker rc = (PKIXRevocationChecker)cpv.getRevocationChecker();
		
		Set<Option> options = new HashSet<>();
		options.add(Option.SOFT_FAIL); // Ignore network failures
		
		if(revokePreferOCSP != true && revokePreferCDP == true) {
			options.add(Option.PREFER_CRLS); // Prefers CDP instead of OCSP
			log.debug("CRLs CDP is preferred");
		}
		
		if(oneLevel) {
			options.add(Option.ONLY_END_ENTITY); // Check only userCert, skip CAs
			log.debug("OneLevel issuer CA check is enabled");
		}
		rc.setOptions(options); 
				
		// additional params
		if(addRevData.getRevocationLists().size() > 0) {
			log.debug("Adding revocationData CRLs");
			params.addCertStore(createCertStore(addRevData.getRevocationLists()));
		}
		
		if(!addRevData.getOcspResponses().isEmpty()) {
			log.debug("Adding revocationData OCSP responses");
			// convert params to PKIXRevocationChecker format
			Map<X509Certificate,byte[]> responses = new HashMap<>(); 
			List<java.security.cert.Extension> extns = new ArrayList<>(); 
						
			addRevData.getOcspResponses().forEach( 
				(cert, blobs) -> { 
					responses.put(cert, blobs.get(RevocationData.OCSP_RESP));
					if(blobs.get(RevocationData.OCSP_NONCE) != null) {
						extns.add(new NonceExtension(blobs.get(RevocationData.OCSP_NONCE)));
					}
				});
			
			rc.setOcspResponses(responses);
			rc.setOcspExtensions(extns);
		}
				
		params.addCertPathChecker(rc);
		
		try {
			
			PKIXCertPathValidatorResult pcpvr = (PKIXCertPathValidatorResult)cpv.validate(cp, params);
			log.debug("CertPath is valid");
			List<CertPathValidatorException> exps = rc.getSoftFailExceptions();
			if(!exps.isEmpty()) {
				log.error("Soft fail exceptions: {}", exps);
				throw new CertPathPartialValidException("Unable to check revocation", exps.get(0));
			}
						
			// public key of validated user certificate 
			// can be safely use to validate signature
			return pcpvr.getPublicKey();
			
		} catch (CertPathValidatorException e) {
			throw new CertPathNotValidException("CertPath not valid", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CertPathFailureException("Validation failed Invalid Algorithm",e);
		}
	}
	
	private boolean matchProfile(CertIdentity ci, Profile p) {
		
		// no profile, no filtering so match any (true)
		if(p == null) {
			return true;
		}
		
		log.info("CertIdentity filtering by Profile {}", p);
		
		boolean matchTerritory = false;
		if(p.getTerritory() != null && p.getTerritory().length() > 0) {
			List<String> allowedTerritories = Arrays.asList(p.getTerritory().split(","));
			
			if(allowedTerritories.contains(ci.
					getService().
					getProvider().
					getTrustedList().
					getTerritory())) {
				matchTerritory = true;
			}
		} else {
			matchTerritory = true;
		}
		log.debug("Territory isMatched?={}", matchTerritory);
		
		boolean matchProvider = false;
		if(p.getProvider() != null && p.getProvider().length() > 0) {
			List<String> allowedProviders = Arrays.asList(p.getProvider().split(","));
			
			if(allowedProviders.contains(ci.
					getService().
					getProvider().
					getName())) {
				matchProvider = true;
			}	
		} else {
			matchProvider = true;
		}
		log.debug("Provider isMatched?={}", matchProvider);
		
		
		boolean matchServiceInfo = false;
		if(p.getServiceInfo() != null && p.getServiceInfo().length() > 0) {
			List<String> allowedServiceInfos = Arrays.asList(p.getServiceInfo().split(","));
			
			matchServiceInfo = ci.getService().getExtensions().stream().
					map(se -> se.getType().name()).
					anyMatch(s -> allowedServiceInfos.contains(s));		
		} else {
			matchServiceInfo = true;
		}
		log.debug("Service isMatched?={}", matchServiceInfo);
		
		return matchTerritory && matchProvider && matchServiceInfo;
	}
	
	private void splitCerts(List<X509Certificate> caCerts, Set<TrustAnchor> rootCAs, List<X509Certificate> subCAs) {
		
		if(caCerts == null || rootCAs == null || subCAs == null) {
			throw new IllegalStateException("Argument cant be null!");
		}
		
		caCerts.forEach(cert -> {
			if(cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal())) {
				rootCAs.add(new TrustAnchor(cert, null));
			} else {
				subCAs.add(cert);
			}
		});
	}

	private void makeTrustCerts(List<X509Certificate> caCerts, Set<TrustAnchor> rootCAs) {
		
		if(caCerts == null || rootCAs == null) {
			throw new IllegalStateException("Argument cant be null!");
		}
		caCerts.forEach(cert -> rootCAs.add(new TrustAnchor(cert, null)));
	}
	
	private X509Certificate toX509Certificate(CertIdentity ci) {
		return certificateServiceHelper.parseCertificate(ci.getValue());
	}
	
	private List<X509Certificate> toCertList(MyCertPath cp) {
		List<X509Certificate> certsRet = new ArrayList<>();
		cp.getCertPath().getCertificates().forEach(
				c -> certsRet.add((X509Certificate)c));
		certsRet.add(cp.getAnchor().getTrustedCert());
		return certsRet;
	}
	
	
	
	private byte[] generateOCSPRequest(X509Certificate issuer, X509Certificate cert, byte[] nonce) {
			
		
		X509CertificateHolder issuerCert = null;
		try {
			issuerCert = new X509CertificateHolder(issuer.getEncoded());
		} catch (CertificateEncodingException | IOException e) {
			throw new RuntimeException("Unable to create X509CertificateHolder instance", e);
		}
		
		CertificateID certId = null;
		try {
			DigestCalculatorProvider digCalcProv = new JcaDigestCalculatorProviderBuilder().build();
			certId = new CertificateID(digCalcProv.get(CertificateID.HASH_SHA1), issuerCert, cert.getSerialNumber());
		} catch (OperatorCreationException|OCSPException e) {
			throw new RuntimeException("Unable to create CertificateID", e);
		} 
		
		OCSPReqBuilder orb = new OCSPReqBuilder();
		orb.addRequest(certId);
		
		if(nonce != null) {
			orb.setRequestExtensions(new Extensions(
				 new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce,
				 false, new DEROctetString(nonce))));
		}
		
		try {
			return orb.build().getEncoded();
		} catch (IOException | OCSPException e) {
			throw new RuntimeException("Unable to encode request", e);
		}
		
	}
	
	private byte[] createOcspNonce(X509Certificate cert) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write(cert.getIssuerX500Principal().getEncoded());
			bos.write(cert.getSerialNumber().toByteArray());
			bos.write(("" + System.identityHashCode(cert)).getBytes()); // simple instance random
		} catch(IOException e) {
			throw new RuntimeException("buffer fill error", e);
		}
		
		DigestUtils digest = new DigestUtils(MessageDigestAlgorithms.SHA_256);
		byte[] out = digest.digest(bos.toByteArray());

		return Arrays.copyOfRange(out, 0, 20);
	}
	
	
	
	private boolean haveTailRootCA(List<X509Certificate> cAs) {
		if(cAs.size() == 0) {
			return false;
		}
		
		X509Certificate tailCert = cAs.get(cAs.size() - 1);
		return tailCert.getSubjectX500Principal().equals(tailCert.getIssuerX500Principal());
	}
	
	private CertPathBuilder createCertPathBuilder() {
		try {
			return CertPathBuilder.getInstance("PKIX");
		} catch (NoSuchAlgorithmException e) {
			log.error("CertPathBuilder.getInstance() failed", e);
			throw new CertPathFailureException("Unable to create CertPathBuilder instance", e);
		}
	}
	
	private CertPathValidator createCertPathValidator() {
		try {
			return CertPathValidator.getInstance("PKIX");
		} catch (NoSuchAlgorithmException e) {
			log.error("CertPathValidator.getInstance() failed", e);
			throw new CertPathFailureException("Unable to create CertPathValidator instance", e);
		}
	}
	
	private CertStore createCertStore(Collection<?> collection) {
		 CollectionCertStoreParameters ccsp = new CollectionCertStoreParameters(collection);
		 try {
			return CertStore.getInstance("Collection", ccsp);
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
			log.error("CertStore.getInstance() failed", e);
			throw new CertPathFailureException("Unable to create CertStore instance", e);
		}
	}
	

	@Data
	@AllArgsConstructor
	private static class MyCertPath {
		private TrustAnchor anchor;
		private CertPath certPath;
	}
	
	@Data
	private static class RevocationData {
		public static final int OCSP_RESP = 0;
		public static final int OCSP_NONCE = 1;
		private List<X509CRL> revocationLists = new ArrayList<>();
		private Map<X509Certificate,List<byte[]>> ocspResponses = new HashMap<>();
	}
	
	private static class NonceExtension implements java.security.cert.Extension {
		
		private byte[] nonce;
		
		public NonceExtension(byte[] nonce) {
			this.nonce = nonce;
		}
		
		@Override
		public String getId() {
			return OCSPObjectIdentifiers.id_pkix_ocsp_nonce.getId();
		}

		@Override
		public boolean isCritical() {
			return false;
		}

		@Override
		public byte[] getValue() {
			return nonce;
		}

		@Override
		public void encode(OutputStream out) throws IOException {
			out.write(nonce);
		}
	}
}
