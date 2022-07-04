package pl.grabojan.certsentry.tsldownload.data;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.data.model.CertIdentity;
import pl.grabojan.certsentry.data.model.ExtensionType;
import pl.grabojan.certsentry.data.model.Provider;
import pl.grabojan.certsentry.data.model.Service;
import pl.grabojan.certsentry.data.model.ServiceExtension;
import pl.grabojan.certsentry.data.model.ServiceStatus;
import pl.grabojan.certsentry.data.model.ServiceType;
import pl.grabojan.certsentry.data.model.SupplyPoint;
import pl.grabojan.certsentry.data.model.SupplyPointType;
import pl.grabojan.certsentry.data.model.TrustedList;
import pl.grabojan.certsentry.data.model.TrustedListType;
import pl.grabojan.certsentry.schema.tsl.AdditionalServiceInformation;
import pl.grabojan.certsentry.schema.tsl.AttributedNonEmptyURIType;
import pl.grabojan.certsentry.schema.tsl.DigitalIdentityType;
import pl.grabojan.certsentry.schema.tsl.Extension;
import pl.grabojan.certsentry.schema.tsl.MultiLangNormStringType;
import pl.grabojan.certsentry.schema.tsl.NonEmptyMultiLangURIType;
import pl.grabojan.certsentry.schema.tsl.SchemeInformation;
import pl.grabojan.certsentry.schema.tsl.ServiceInformation;
import pl.grabojan.certsentry.schema.tsl.TSPInformation;
import pl.grabojan.certsentry.schema.tsl.TSPService;
import pl.grabojan.certsentry.schema.tsl.TrustServiceProvider;
import pl.grabojan.certsentry.schema.tsl.TrustServiceStatusList;
import pl.grabojan.certsentry.tsldownload.TSLConstants;
import pl.grabojan.certsentry.util.CertificateServiceHelper;

@Slf4j
@RequiredArgsConstructor
public class TrustedListDataMapperImpl implements TrustedListDataMapper, TSLConstants {
	
	private final String[] langs = new String[] { "pl", "en" }; 
	private final boolean strictMode = false;
	private final CertificateServiceHelper certServHelper;
	
	@Override
	public TrustedList createOrUpdateData(TrustedList dbTL, TrustServiceStatusList xmlTL) {
		if(dbTL == null) {
			return createTL(xmlTL);
		} 
		
		return updateTL(dbTL, xmlTL);
	}

	private <T> void setValueIfDiffers(Supplier<T> val1, Supplier<T> val2, Consumer<T> target) {
		
		T value1 = val1.get();
		T value2 = val2.get();
		
		if(value1 == null || !value1.equals(value2)) {
			target.accept(value2);
		}
	}
		
	// computes the difference without modifying the sets
	private static <T> Set<T> difference(final Set<T> setOne, final Set<T> setTwo) {
	     Set<T> result = new HashSet<T>(setOne);
	     result.removeIf(setTwo::contains);
	     return result;
	}
		
	private TrustedList createTL(TrustServiceStatusList tsl) {
		
		TrustedList tl = new TrustedList();
		
		updateTrustedListProperties(tl, tsl);
		
		log.info("Created new TrustedList, territory: {}, sequence: {}", tl.getTerritory(), tl.getSequenceNumber());
		log.debug("Created TrustedList: {}", tl);
		
		if(tsl.getTrustServiceProviderList() != null) {
			List<Provider> provs = tsl.getTrustServiceProviderList().getTrustServiceProviders().
					stream().
					map(tsp -> createTLProvider(tl, tsp)).
					//filter(p -> p.getServices().size() > 0).
					collect(Collectors.toList());	
			tl.getProviders().addAll(provs);
		}		
				
		return tl;
	}
		
	private Provider createTLProvider(TrustedList tl, TrustServiceProvider tsp) {
		Provider p = new Provider();
		
		updateProviderProperties(p, tsp);
		
		log.info("Created new Provider, name: {}", p.getName());
		log.debug("Created Provider: {}", p);
		
		p.setTrustedList(tl);
				
		List<Service> srvs = tsp.getTSPServices().getTSPServices().
				stream().
				filter(this::isActiveCertService).
				map(s -> createProviderService(p,s)).
				collect(Collectors.toList());
		p.getServices().addAll(srvs);
				
		return p;	
	}
	
	private Service createProviderService(Provider p, TSPService srv) {
		
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		
		Service s = new Service();
		
		updateServiceProperties(s, srv);
		
		log.info("Created new Service, name: {}, startDate: {}", s.getName(), dateFormat1.format(s.getStartDate()));
		log.debug("Created Service: {}", s);
		 
		s.setProvider(p);

		ServiceInformation servInfo = srv.getServiceInformation();

		if(servInfo.getServiceDigitalIdentity() != null) {
			List<CertIdentity> certIds = servInfo.getServiceDigitalIdentity().getDigitalIds().
					stream().
					filter(di->di.getX509Certificate() != null).
					map(di->createServiceCertIdentity(s, di)).
					collect(Collectors.toList()); 
			s.getCertIdentities().addAll(certIds);
		}

		if(servInfo.getServiceSupplyPoints() != null) {
			List<SupplyPoint> supplyPnts = servInfo.getServiceSupplyPoints().getServiceSupplyPoints().
					stream().
					map(sp->createServiceSupplyPoint(s,sp)).
					collect(Collectors.toList());
			s.getSupplyPoints().addAll(supplyPnts);
		}

		if(servInfo.getServiceInformationExtensions() != null) {
			List<ServiceExtension> extns = servInfo.getServiceInformationExtensions().getExtensions().
					stream().
					filter(this::isAdditionalServiceInformationExtension).
					map(e->createServiceExtension(s,e)).
					collect(Collectors.toList());
			s.getExtensions().addAll(extns);
		}

		 return s;
	}
	
	private CertIdentity createServiceCertIdentity(Service s, DigitalIdentityType dit) {
		
		CertIdentity ci = new CertIdentity();
		
		updateCertIdentityProperties(ci, dit);
		
		log.info("Created new CertIdentity, issuer: {} / serial: {}", ci.getIssuer(), ci.getSerialNumber());
		log.debug("Created CertIdentity: {}", ci);

		ci.setService(s);
				
		return ci;
	}
	
	private SupplyPoint createServiceSupplyPoint(Service s, AttributedNonEmptyURIType supplyPoint) {
		
		SupplyPoint sp = new SupplyPoint();
		
		updateSupplyPointProperties(sp, supplyPoint);
		
		log.info("Created new SupplyPoint: {}", sp.getPointUri());
		log.debug("Created SupplyPoint: {}", sp);
		
		sp.setService(s);
		
		return sp;
	}
	
	
	private ServiceExtension createServiceExtension(Service s, Extension extension) {
		
		ServiceExtension ext = new ServiceExtension();

		updateServiceExtensionProperties(ext, extension);
		
		log.info("Created new ServiceExtension: {}", ext.getValue());
		log.debug("Created ServiceExtension: {}", ext);
	
		ext.setService(s);
		
		return ext;		
	}
		
	private void updateTrustedListProperties(TrustedList tl, TrustServiceStatusList tsl) {
		
		SchemeInformation si = tsl.getSchemeInformation();
				
		// mandatory
		setValueIfDiffers(()->tl.getType(),()->toTypeFromURI(si.getTSLType()), tl::setType);
		setValueIfDiffers(()->tl.getSequenceNumber(), ()->si.getTSLSequenceNumber().longValueExact(), tl::setSequenceNumber);
		setValueIfDiffers(()->tl.getOperatorName(), ()->getLocalizedName(si.getSchemeOperatorName().getNames()), tl::setOperatorName);
		// information URI for lotl
		if(tsl.getSchemeInformation().getTSLType().equals(TSLConstants.TSLTYPE_EULOTL)) {
			setValueIfDiffers(()->tl.getInformationUri(), ()->si.getSchemeInformationURI().getURIS().get(0).getValue(), tl::setInformationUri);
		} else {
			setValueIfDiffers(()->tl.getInformationUri(), ()->getLocalizedUri(si.getSchemeInformationURI().getURIS()), tl::setInformationUri);
		}
		setValueIfDiffers(()->tl.getTerritory(), ()->si.getSchemeTerritory(), tl::setTerritory);
		setValueIfDiffers(()->tl.getListIssue(), ()->si.getListIssueDateTime().toGregorianCalendar().getTime(), tl::setListIssue);
		
		// UK abbadoned list
		if(si.getNextUpdate().getDateTime() != null) {
			setValueIfDiffers(()->tl.getNextUpdate(), ()->si.getNextUpdate().getDateTime().toGregorianCalendar().getTime(), tl::setNextUpdate);
		}
		
		// optional
		if(si.getDistributionPoints() != null) {
			setValueIfDiffers(()->tl.getDistributionPoint(), ()->si.getDistributionPoints().getURIS().get(0), tl::setDistributionPoint);
		} else {
			tl.setDistributionPoint(null);
		}
	}
	
	private void updateProviderProperties(Provider p, TrustServiceProvider tsp) {
		
		TSPInformation tspInfo = tsp.getTSPInformation();
		
		// mandatory
		setValueIfDiffers(()->p.getName(), ()->getLocalizedName(tspInfo.getTSPName().getNames()), p::setName);
		setValueIfDiffers(()->p.getInformationUri(), ()->getLocalizedUri(tspInfo.getTSPInformationURI().getURIS()), p::setInformationUri);
		
		// optional
		if(tspInfo.getTSPTradeName() != null) {
			setValueIfDiffers(()->p.getTradeName(), ()->getLocalizedName(tspInfo.getTSPTradeName().getNames()), p::setTradeName);
		} else {
			p.setTradeName(null);
		}
	}
	
	private void updateServiceProperties(Service s, TSPService tspServ) {
		
		ServiceInformation servInfo = tspServ.getServiceInformation();
		
		// mandatory
		setValueIfDiffers(()->s.getType(), ()->toServiceTypeFromURI(servInfo.getServiceTypeIdentifier()), s::setType);
		setValueIfDiffers(()->s.getName(), ()->getLocalizedName(servInfo.getServiceName().getNames()), s::setName);
		setValueIfDiffers(()->s.getStatus(), ()->toServiceStatusFromURI(servInfo.getServiceStatus()), s::setStatus);
		setValueIfDiffers(()->s.getStartDate(), ()->servInfo.getStatusStartingTime().toGregorianCalendar().getTime(), s::setStartDate);
		
		// optional
		if(servInfo.getTSPServiceDefinitionURI() != null) {
			setValueIfDiffers(()->s.getDefinitionUri(), ()->getLocalizedUri(servInfo.getTSPServiceDefinitionURI().getURIS()), s::setDefinitionUri);
		} else {
			s.setDefinitionUri(null);
		}
	}
	
	private void updateCertIdentityProperties(CertIdentity ci, DigitalIdentityType dit) {
		
		X509Certificate cert = certServHelper.parseCertificate(dit.getX509Certificate());
		
		log.debug("Cert: {}", cert);
		
		setValueIfDiffers(()->ci.getSerialNumber(), ()->cert.getSerialNumber().toString(), ci::setSerialNumber);
		setValueIfDiffers(()->ci.getNotBefore(), ()->cert.getNotBefore(), ci::setNotBefore);
		setValueIfDiffers(()->ci.getNotAfter(), ()->cert.getNotAfter(), ci::setNotAfter);
		setValueIfDiffers(()->ci.getIssuer(), ()->certServHelper.resolveX500Principal(cert.getIssuerX500Principal()), ci::setIssuer);
		setValueIfDiffers(()->ci.getSubject(), ()->certServHelper.resolveX500Principal(cert.getSubjectX500Principal()), ci::setSubject);
		setValueIfDiffers(()->ci.getPublicKeyHash(), ()->certServHelper.getPublicKeyHash(cert.getPublicKey()), ci::setPublicKeyHash);
		setValueIfDiffers(()->ci.getSignatureAlgo(), ()->cert.getSigAlgName(), ci::setSignatureAlgo);
		setValueIfDiffers(()->ci.getValue(), ()->dit.getX509Certificate(), ci::setValue);
		
	}
	
	private void updateSupplyPointProperties(SupplyPoint sp, AttributedNonEmptyURIType sput) {
		
		setValueIfDiffers(()->sp.getType(), ()->toSupplyPointTypeFromURI(sput.getType(), sput.getValue()), sp::setType);	
		setValueIfDiffers(()->sp.getPointUri(), ()->sput.getValue(), sp::setPointUri);
	}
	
	private void updateServiceExtensionProperties(ServiceExtension se, Extension e) {
		
		List<Object> contentList = e.getContent();
		contentList.forEach(content -> { 
		
			if(content instanceof AdditionalServiceInformation) {

				AdditionalServiceInformation asi = (AdditionalServiceInformation)content;
				NonEmptyMultiLangURIType uri = asi.getURI();
				if(uri == null) {
					throw new IllegalArgumentException("AdditionalServiceInformation doesnt contains URI!");
				}
						
				se.setName(asi.getClass().getSimpleName());
				
				setValueIfDiffers(()->se.getType(), ()->toExtensionTypeFromURI(uri.getValue()), se::setType);
				setValueIfDiffers(()->se.getValue(), ()->uri.getValue(), se::setValue);	
				
			}
			
		});

	}
	
	private TrustedList updateTL(TrustedList currTL, TrustServiceStatusList tsl) {
		
		updateTrustedListProperties(currTL, tsl);
		
		log.info("Updating TrustList territory: {}, sequence: {}", currTL.getTerritory(), currTL.getSequenceNumber());
		
		if(tsl.getTrustServiceProviderList() != null) {
			updateProviders(currTL, tsl.getTrustServiceProviderList().getTrustServiceProviders());
		}
				
		return currTL;
	}
	
	private void updateProviders(TrustedList currTL, List<TrustServiceProvider> tslTsps) {
		
		Map<String,Provider> currProvs = currTL.getProviders().
				stream().
				collect(Collectors.toMap(Provider::getName, Function.identity()));
		
		Map<String, TrustServiceProvider> tslProvs = tslTsps.
				stream().
				collect(Collectors.toMap(p->getLocalizedName(p.getTSPInformation().getTSPName().getNames()), Function.identity()));
		
		Set<String> tslKeys = tslProvs.keySet();
		
		tslKeys.forEach(k -> {
			if(currProvs.containsKey(k)) {
				// update
				updateProviderProperties(currProvs.get(k), tslProvs.get(k));
				log.info("Updating Provider: {}", currProvs.get(k).getName());
				updateServices(currProvs.get(k),tslProvs.get(k).getTSPServices().getTSPServices());
			} else {
				// add
				Provider p = createTLProvider(currTL,tslProvs.get(k));
				log.info("Adding new Provider: {}", p.getName());
				currTL.getProviders().add(p);
			}
		});
		
		// remove db orphans (not on tsl)
		Set<String> dbKeys = currProvs.keySet();
		difference(dbKeys,tslKeys).forEach(k -> {
				Provider p = currProvs.get(k);
				currTL.getProviders().remove(p);
				log.info("Removed orphan Provider: {}", p.getName());
		});
		
	}
		
	private void updateServices(Provider currProv, List<TSPService> tslTspServs) {
		
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		
		Map<String, Service> currServs = currProv.getServices().stream().collect(toSpecMap(o-> {
				// special map, with CertIdentity dependent object
				String serviceName = o.getName();
				String serviceStartDateFormated = dateFormat1.format(o.getStartDate());
				String subCertIdentityHash = "";
				if (o.getCertIdentities().size() > 0)  { 
					subCertIdentityHash = o.getCertIdentities().stream().findAny().get().getPublicKeyHash();
				}
				return serviceName + " " + serviceStartDateFormated + " " + subCertIdentityHash;
			}, Function.identity(), k->k+'_'+UUID.randomUUID().toString()));
		
		Map<String, TSPService> tslServs = tslTspServs.stream().filter(this::isActiveCertService).collect(toSpecMap(o-> { 
			// special map, with DigitalIdentityType dependent object
			String serviceName = getLocalizedName(o.getServiceInformation().getServiceName().getNames());
			String serviceStartDateFormated = dateFormat1.format(o.getServiceInformation().
					getStatusStartingTime().toGregorianCalendar().getTime());
			String subCertIdentityHash = "";
			List<DigitalIdentityType> dits = o.getServiceInformation().getServiceDigitalIdentity().getDigitalIds().
					stream().filter(c -> c.getX509Certificate() != null).collect(Collectors.toList());
			if(dits.size() > 0) {
				byte[] certBin = dits.get(0).getX509Certificate();
				X509Certificate cert = certServHelper.parseCertificate(certBin);
				subCertIdentityHash = certServHelper.getPublicKeyHash(cert.getPublicKey());
			}
			return serviceName + " " + serviceStartDateFormated + " " + subCertIdentityHash;
					
			}, Function.identity(), k->k+'_'+UUID.randomUUID().toString()));
		
		
		Set<String> tslKeys = tslServs.keySet();
		
		tslKeys.forEach(k -> {
			if(currServs.containsKey(k)) {
				// update
				updateServiceProperties(currServs.get(k), tslServs.get(k));
				log.info("Updating Service, name: {}, startDate: {}", currServs.get(k).getName(), dateFormat1.format(currServs.get(k).getStartDate()));
				// go to service deps
				Service s = currServs.get(k);
				TSPService tsps = tslServs.get(k);
				if(tsps.getServiceInformation().getServiceDigitalIdentity() != null) {
					updateCertIdentities(s,tsps.getServiceInformation().getServiceDigitalIdentity().getDigitalIds());
				}
				if(tsps.getServiceInformation().getServiceSupplyPoints() != null) {
					updateSupplyPoints(s, tsps.getServiceInformation().getServiceSupplyPoints().getServiceSupplyPoints());
				}
				if(tsps.getServiceInformation().getServiceInformationExtensions() != null) {
					updateServiceExtensions(s, tsps.getServiceInformation().getServiceInformationExtensions().getExtensions());
				}
			} else {
				// add
				Service s = createProviderService(currProv, tslServs.get(k));
				log.info("Adding new Service, name: {}, startDate: {}", s.getName(), dateFormat1.format(s.getStartDate()));
				// go to service deps
				currProv.getServices().add(s);
				
			}
		});
		
		// remove db orphans (not on tsl)
		Set<String> dbKeys = currServs.keySet();
		difference(dbKeys,tslKeys).forEach(k -> {
			Service s = currServs.get(k);
			currProv.getServices().remove(s);
			log.info("Removed orphan Service, name: {}, startDate: {}", s.getName(), dateFormat1.format(s.getStartDate()));
		});
		
	}
	
	private void updateCertIdentities(Service currServ, List<DigitalIdentityType> tslDits) {
		
		Map<String, CertIdentity> currCertIdents = currServ.getCertIdentities().stream().collect(Collectors.toMap(o-> { 
			return o.getIssuer() + "/" + o.getSerialNumber(); 
			}, Function.identity(), (existing, replacement) -> existing));
		
		Map<String, DigitalIdentityType> tslDitCerts = tslDits.stream().filter(di->di.getX509Certificate() != null).collect(Collectors.toMap(o-> {
			X509Certificate c = certServHelper.parseCertificate(o.getX509Certificate());
			String issuer = certServHelper.resolveX500Principal(c.getIssuerX500Principal());
			String serialNumber = c.getSerialNumber().toString();
			return issuer + "/" + serialNumber; 
			}, Function.identity(), (existing, replacement) -> existing));
		
		Set<String> tslKeys = tslDitCerts.keySet();
		tslKeys.forEach(k -> {
			if(currCertIdents.containsKey(k)) {
				// update
				updateCertIdentityProperties(currCertIdents.get(k), tslDitCerts.get(k));
				log.info("Updating CertIdentity, issuer: {} / serial: {}", currCertIdents.get(k).getIssuer(), currCertIdents.get(k).getSerialNumber());
			} else {
				// add
				CertIdentity ci = createServiceCertIdentity(currServ, tslDitCerts.get(k));
				log.info("Adding new CertIdentity, issuer: {} / serial: {}", ci.getIssuer(), ci.getSerialNumber());
				currServ.getCertIdentities().add(ci);
			}
		});
		
		// remove db orphans (not on tsl)
		Set<String> dbKeys = currCertIdents.keySet();
		difference(dbKeys,tslKeys).forEach(k -> {
			CertIdentity ci = currCertIdents.get(k);
			currServ.getCertIdentities().remove(ci);	
			log.info("Removed orphan CertIdentity, issuer: {} / serial: {}", ci.getIssuer(), ci.getSerialNumber());
		});
	}
	
	private void updateSupplyPoints(Service currServ, List<AttributedNonEmptyURIType> tslSupps) {
		
		Map<String,SupplyPoint> currSupplyPoints = currServ.getSupplyPoints().
				stream().
				collect(Collectors.toMap(SupplyPoint::getPointUri, Function.identity()));
		
		Map<String,AttributedNonEmptyURIType> tslSupplyPoints = tslSupps.
				stream().
				collect(Collectors.toMap(AttributedNonEmptyURIType::getValue, Function.identity()));
		
		Set<String> tslKeys = tslSupplyPoints.keySet();
		tslKeys.forEach(k -> {
			if(currSupplyPoints.containsKey(k)) {
				// update
				updateSupplyPointProperties(currSupplyPoints.get(k), tslSupplyPoints.get(k));
				log.info("Updating SupplyPoint: {}", currSupplyPoints.get(k).getPointUri());
			} else {
				// add
				SupplyPoint sp = createServiceSupplyPoint(currServ, tslSupplyPoints.get(k));
				log.info("Adding new SupplyPoint: {}", sp.getPointUri());
				currServ.getSupplyPoints().add(sp);
			}
		});
		
		// remove db orphans (not on tsl)
		Set<String> dbKeys = currSupplyPoints.keySet();
		difference(dbKeys,tslKeys).forEach(k -> {
			SupplyPoint sp = currSupplyPoints.get(k);
			currServ.getSupplyPoints().remove(sp);	
			log.info("Removed orphan SupplyPoint: {}", sp.getPointUri());
		});
		
	}
	
	private void updateServiceExtensions(Service currServ, List<Extension> tslExts) {
		
		Map<String,ServiceExtension> currServiceExtns = currServ.getExtensions().stream().collect(Collectors.toMap(ServiceExtension::getValue, Function.identity()));
		
		Map<String,Extension> tslServiceExtns = tslExts.stream().filter(this::isAdditionalServiceInformationExtension).collect(Collectors.toMap(o->{
			
			AdditionalServiceInformation asi = (AdditionalServiceInformation)o.getContent().
					stream().
					filter(c -> AdditionalServiceInformation.class.isAssignableFrom(c.getClass())).
					findFirst().
					get();

			return asi.getURI().getValue(); 
			}, Function.identity()));
		
		Set<String> tslKeys = tslServiceExtns.keySet();
		tslKeys.forEach(k -> {
			if(currServiceExtns.containsKey(k)) {
				// update
				updateServiceExtensionProperties(currServiceExtns.get(k), tslServiceExtns.get(k));
				log.info("Updating ServiceExtension: {}", currServiceExtns.get(k).getValue());
			} else {
				// add
				ServiceExtension se = createServiceExtension(currServ,  tslServiceExtns.get(k));
				log.info("Adding new ServiceExtension: {}", se.getValue());
				currServ.getExtensions().add(se);
			}
		});
		
		// remove db orphans (not on tsl)
		Set<String> dbKeys = currServiceExtns.keySet();
		difference(dbKeys,tslKeys).forEach(k -> {
			ServiceExtension se = currServiceExtns.get(k);
			currServ.getExtensions().remove(se);
			log.info("Removed orphan ServiceExtension: {}", se.getValue());
		});
	}
 
	private TrustedListType toTypeFromURI(String uri) {
		if(uri.equals(TSLConstants.TSLTYPE_EULOTL)) {
			return TrustedListType.EU_LOTL;
		} else if(uri.equals(TSLConstants.TSLTYPE_EUGENERIC)) {
			return TrustedListType.EU_GENERIC;
		} else {
			if(strictMode) {
				throw new IllegalArgumentException("Unsupported trusted list type: " + uri);
			} else {
				return TrustedListType.OTHER;
			}
		}
	}
	
	private ServiceType toServiceTypeFromURI(String uri) {
		if(uri.equals(TSLConstants.SVCTYPE_QC_CA)) {
			return ServiceType.QC_CA;
		} else if(uri.equals(TSLConstants.SVCTYPE_QC_NATIONALROOTCA)) {
			return ServiceType.QC_NATIONALROOTCA;
		} else {
			if(strictMode) {
				throw new IllegalArgumentException("Unsupported service type: " + uri);
			} else {
				return ServiceType.OTHER;
			}
		}
	}
	
	private ServiceStatus toServiceStatusFromURI(String uri) {
		if(uri.equals(TSLConstants.SVCSTATUS_GRANTED)) {
			return ServiceStatus.GRANTED;
		} else if(uri.equals(TSLConstants.SVCSTATUS_RECOGNISED_AT_NATIONALLEVEL)) {
			return ServiceStatus.RECOGNISED_AT_NATIONAL_LEVEL;
		} else {
			if(strictMode) {
				throw new IllegalArgumentException("Unsupported service status: " + uri);
			} else {
				return ServiceStatus.UNKNOWN;
			}
		}
	}
	
	private SupplyPointType toSupplyPointTypeFromURI(String uriType, String uriValue) {
		if(CERTSTATUS_QC_CRL.equals(uriType)) {
			return SupplyPointType.CRL;
		} else if(CERTSTATUS_QC_OCSP.equals(uriType)) {
			return SupplyPointType.OCSP;
		} else {
			if(uriValue.endsWith(".crl")) {
				return SupplyPointType.CRL;
			} else if(uriValue.contains("ocsp")) {
				return SupplyPointType.OCSP;
			} else {
				return SupplyPointType.INFO;
			}
		}
	}
	
	private ExtensionType toExtensionTypeFromURI(String uri) {
		
		if(SVCINFOEXT_FOR_ESIGNATURES.equals(uri)) {
			return ExtensionType.FOR_ESIGNATURES;
		} else if(SVCINFOEXT_FOR_ESEALS.equals(uri)) {
			return ExtensionType.FOR_ESEALS;
		} else if(SVCINFOEXT_FOR_WEBSITEAUTHENTICATION.equals(uri)) {
			return ExtensionType.FOR_WEBSITEAUTHENTICATION;
		}  else if(SVCINFOEXT_ROOTCA_QC.equals(uri)) {
			return ExtensionType.ROOTCA_QC;
		} else {
			if(strictMode) {
				throw new IllegalArgumentException("Unsupported extension type: " + uri);
			} else {
				return ExtensionType.UNKNOWN;
			}
		}
		
	}
		
	private boolean isActiveCertService(TSPService srv) {
	
		ServiceInformation servInfo = srv.getServiceInformation();
		 if((SVCTYPE_QC_CA.equals(servInfo.getServiceTypeIdentifier()) && 
				 SVCSTATUS_GRANTED.equals(servInfo.getServiceStatus())) ||
			(SVCTYPE_QC_NATIONALROOTCA.equals(servInfo.getServiceTypeIdentifier()) &&
					SVCSTATUS_RECOGNISED_AT_NATIONALLEVEL.equals(servInfo.getServiceStatus()))) {
			 return true;
		 }
		
		 return false;
	}
	
	private boolean isAdditionalServiceInformationExtension(Extension e) {
		
		List<Object> content = e.getContent();
		return content.stream().filter(c -> AdditionalServiceInformation.class.isAssignableFrom(c.getClass())).count() > 0;
	}
		
	private String getLocalizedName(List<MultiLangNormStringType> mlang) {
		 
		String val = null;
		
		foundval:
		for (String l : langs) {	
			for (MultiLangNormStringType m : mlang) {
				if(m.getLang().equals(l)) {
					val = m.getValue();
					break foundval;
				}
			}
		}
		
		if(val != null) {
			return val;
		} else {
			return mlang.get(0).getValue();
		}
		
	}
	
	private String getLocalizedUri(List<NonEmptyMultiLangURIType> mUri) {
		
		String val = null;
		
		foundval:
		for (String l : langs) {
			for (NonEmptyMultiLangURIType u : mUri) {
				if(u.getLang().equals(l)) {
					val = u.getValue();
					break foundval;
				}
			}
		}
		
		if(val != null) {
			return val;
		} else {
			return mUri.get(0).getValue();
		}
		
	}
	
	private static <T, K, V> Collector<T, ?, Map<K,V>> toSpecMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper,
			Function<? super K, ? extends K> replaceKeyMapper) {
		return Collector.of(HashMap::new,
        (m, t) -> {
            K k = keyMapper.apply(t);
            V v = Objects.requireNonNull(valueMapper.apply(t));
            if(m.putIfAbsent(k, v) != null) {
            	K kr = replaceKeyMapper.apply(k);
            	m.put(kr, v);
            }
        },
        (m1, m2) -> {
            m2.forEach((k,v) -> {
                if(m1.putIfAbsent(k, v)!=null) {
                	K kr = replaceKeyMapper.apply(k);
                	m1.put(kr, v);
                }
            });
            return m1;
        });
	}
	
}
