package pl.grabojan.certsentry.tsldownload;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.data.model.TrustedList;
import pl.grabojan.certsentry.data.model.TrustedListType;
import pl.grabojan.certsentry.data.model.TrustedListUpdate;
import pl.grabojan.certsentry.data.model.UpdateStatus;
import pl.grabojan.certsentry.data.service.TrustedListDataService;
import pl.grabojan.certsentry.tsldownload.data.TSLDataWrapper;
import pl.grabojan.certsentry.tsldownload.data.TSLXmlToObjectConverter;
import pl.grabojan.certsentry.tsldownload.data.TrustedListDataMapper;
import pl.grabojan.certsentry.tsldownload.util.FileSystemLocalStore;
import pl.grabojan.certsentry.tsldownload.util.KeyStoreServicesHelper;
import pl.grabojan.certsentry.tsldownload.validation.TSLValidator;
import pl.grabojan.certsentry.tsldownload.validation.TSLValidatorContext;
import pl.grabojan.certsentry.tsldownload.validation.TSLValidatorInfo;
import pl.grabojan.certsentry.util.CertificateServiceHelper;
import pl.grabojan.certsentry.util.HttpResource;

@Slf4j
@RequiredArgsConstructor
public class TSLDownloadManager {
		
	private final CertsentryTSLProperties tslProperties;
	private final HttpResource httpResource;
	private final TSLValidator tslValidator;
	private final KeyStoreServicesHelper tslAuthKeyStoreServicesHelper;
	private final TSLXmlToObjectConverter tslXmlToObjectConverter;
	private final FileSystemLocalStore fileSystemLocalStore;
	private final TrustedListDataService trustedListDataService;
	private final TrustedListDataMapper trustedListDataMapper;
	private final CertificateServiceHelper certificateServiceHelper;
	
	
	@Transactional
	public void runDownload() {
					
		TSLDataWrapper lotlData = runDownloadLOTL();
		
		if(lotlData == null || !lotlData.isLOTL()) {
			log.error("Unable to get LOTL, STOPPING UPDATE!");
			return;
		}
		
		lotlData.getPointersToOtherTSLLocations().forEach(tslLocation -> {
			runDownloadTSL(tslLocation,
					lotlData.getPointerSchemeTerritory(tslLocation),
					lotlData.getPointerServiceDigitalIdentities(tslLocation));
		});
	}
	
	private TSLDataWrapper runDownloadLOTL() {

		String loltLocation = tslProperties.getLocation();
		TrustedList currLotl = trustedListDataService.getListOfTheList();
		
		log.info("Processing LOTL (EU)");
		log.info("Current hash: [{}]",(currLotl == null) ? null : currLotl.getListHash());
		
		try {
		
			Optional<byte[]> lotlData = downloadTSL(
					loltLocation,
					(currLotl == null) ? null : currLotl.getListHash(), 
					false,
					createDefaultValidationContext(
							TSLConstants.TSLTYPE_EULOTL,
							tslAuthKeyStoreServicesHelper.getTrustedCerts()
					)
				);
		
			TSLDataWrapper lotlLocalData = null;
			
			if(lotlData.isPresent()) {
				
				// save xml, store info in db
				String storedFile = fileSystemLocalStore.storeFile(loltLocation, lotlData.get());
				log.info("Stored LOTL file: {}", storedFile);
			
				lotlLocalData = convertTSLXmlToDataWrapper(lotlData.get());
				saveInDbTSLData(currLotl, lotlLocalData, tslHash(lotlData.get()), storedFile);
			
				
			} else {
				if(currLotl != null) {
					log.info("Retrieving stored LOTL file: {}", currLotl.getLocalUri());
					byte[] lotlLocalCopy = fileSystemLocalStore.getFile(currLotl.getLocalUri());
					lotlLocalData = convertTSLXmlToDataWrapper(lotlLocalCopy);
					updateDbTSLLastCheck(currLotl);
				} 
			}
			
			return lotlLocalData;
		
		} catch(DownloadException e) {
			log.error("LOTL Download failure", e);
			// save error info
			saveInDbTSLDownloadFailureInfo("EU",currLotl, e.getErrorCode(), e.getErrorMessage());
			return null;
		}
	}


	private void runDownloadTSL(String tslLocation, String territory, List<byte[]> trustedCerts) {
		
		TrustedList currTSL = trustedListDataService.getListForTerritory(territory);
		
		List<X509Certificate> certs = trustedCerts.
				stream().
				map(b->certificateServiceHelper.parseCertificate(b)).
				collect(Collectors.toList());
		
		log.info("Processing territory {}", territory);
		log.info("Current hash: [{}]",(currTSL == null) ? null : currTSL.getListHash());
		
		try {
		
			Optional<byte[]> tslData = downloadTSL(
					tslLocation,
					(currTSL == null) ? null : currTSL.getListHash(), 
					true,
					createDefaultValidationContext(
							TSLConstants.TSLTYPE_EUGENERIC,
							certs)
				);
		
			if(tslData.isPresent()) {
				//  save xml, store info in db
				String storedFile = fileSystemLocalStore.storeFile(tslLocation, tslData.get());
				log.info("Stored TSL file: {}", storedFile);
					
				saveInDbTSLData(currTSL, convertTSLXmlToDataWrapper(tslData.get()), tslHash(tslData.get()), storedFile);
				
			} else {
				updateDbTSLLastCheck(currTSL);
			}
		
		} catch(DownloadException e) {
			log.error("TSL Download failure", e);
			// save error info
			saveInDbTSLDownloadFailureInfo(territory,currTSL, e.getErrorCode(), e.getErrorMessage());
		}
	}
	
	private Optional<byte[]> downloadTSL(String location, String currTSLHash, boolean sha2FileSupport, TSLValidatorContext valContext) {
		
		log.info("TSL download location: {}", location);
		
		// get sha2 if supported
		String sha2file = "0";
		if(sha2FileSupport) {
			Optional<byte[]> responseSha2 = httpResource.get(sha2Location(location));
			if(responseSha2.isPresent()) {
				byte[] content = responseSha2.get();
				sha2file = new String(Arrays.copyOfRange(content, 0, 64));
			} else {
				sha2file = "";
			}
	
			log.info("SHA2 file from server [{}]", sha2file);
		}
		
		// check updated
		if(!sha2file.equalsIgnoreCase(currTSLHash)) {
					
			log.info("Possible update: [{}]<>[{}]",currTSLHash, sha2file);
			
			Optional<byte[]> response = httpResource.get(location);
			if(!response.isPresent()) {
				log.error("No response or resource not found - STOPING!");
				
				// first run and no response
				throw new DownloadException("Unable to download TSL file!","No response",500);
			}
			
			byte[] respBuff = response.get();
			
			// recheck update if no sha2 supported
			sha2file = tslHash(respBuff);
			if(!sha2file.equalsIgnoreCase(currTSLHash)) {
				log.info("Found update: [{}]<>[{}]",currTSLHash, sha2file);
				
				TSLValidatorInfo valInfo = tslValidator.validate(new ByteArrayInputStream(respBuff), valContext);
				log.info("Validation status {}", valInfo.getStatus().toString());
				if(valInfo.getStatus() == TSLValidatorInfo.Status.FAILED) {
					TSLValidatorInfo.Stage stage = valInfo.getFailedStage();
					String errorMsg = valInfo.getErrorMessage();
					int errorCode = valInfo.getErrorCode();
					
					log.error("List validation failed at stage {}, errorMsg: {}, errorCode: {}",
							stage, errorMsg, errorCode);
					
					// first run and invalid response
					throw new DownloadException("Unable to download TSL file!", errorMsg, errorCode);
			
				} 
				return Optional.of(respBuff);
			}
		} 
		// no update
		log.info("No update");	
		return Optional.empty();
	}

	
	private TSLDataWrapper convertTSLXmlToDataWrapper(byte[] buff) {
		return tslXmlToObjectConverter.convertToTSLDataWrapper(toStreamSource(buff));
	}
	
	private void saveInDbTSLData(TrustedList currDbTSL, TSLDataWrapper tslData, String hash, String localFile) {
		
		TrustedList tl = trustedListDataMapper.createOrUpdateData(currDbTSL, tslData.getTrustServiceStatusList());
		tl.setIsValid(true);
		tl.setLastCheck(Calendar.getInstance().getTime());
		tl.setListHash(hash);
		tl.setLocalUri(localFile);
		TrustedList tlCreated = trustedListDataService.save(tl);
		
		// save monitoring info of update
		TrustedListUpdate tlu = new TrustedListUpdate();
		tlu.setErrorCode(0);
		tlu.setInfo("Update succeeded, hash=" + hash);
		tlu.setStatus(UpdateStatus.DONE);
		tlu.setArchLocalUri(localFile);
		tlu.setTimestamp(Calendar.getInstance().getTime());
		tlu.setTrustedList(tlCreated);
		trustedListDataService.save(tlu);
	}
	
	private void updateDbTSLLastCheck(TrustedList currDbTSL) {
		trustedListDataService.markLastCheck(currDbTSL.getTerritory());
	}
	
	private void saveInDbTSLDownloadFailureInfo(String territory, TrustedList currLotl, int errorCode, String errorMessage) {
		if(currLotl == null) {
			// create provisional object
			currLotl = new TrustedList();
			currLotl.setIsValid(Boolean.FALSE);
			currLotl.setType(TrustedListType.OTHER);
			currLotl.setTerritory(territory);
			currLotl.setSequenceNumber(0L);
			currLotl.setOperatorName("");
			currLotl.setListIssue(new Date(0L));
			currLotl.setNextUpdate(new Date(0L));
			currLotl.setLastCheck(Calendar.getInstance().getTime());
		}
		
		TrustedList tlCreated = trustedListDataService.save(currLotl);
		
		// save monitoring info of update
		TrustedListUpdate tlu = new TrustedListUpdate();
		tlu.setErrorCode(errorCode);
		tlu.setInfo("Update failure: " + errorMessage);
		tlu.setStatus(UpdateStatus.ERROR);
		tlu.setTimestamp(Calendar.getInstance().getTime());
		tlu.setTrustedList(tlCreated);
		trustedListDataService.save(tlu);
		
	}

	private TSLValidatorContext createDefaultValidationContext(String listType,
			List<X509Certificate> signatureAllowedCertificates) {
		TSLValidatorContext valContext = new TSLValidatorContext();
		valContext.setTslVersion(5);
		valContext.setTslType(listType); 
		valContext.setValidationDate(Calendar.getInstance().getTime());
		valContext.setSchemaCheck(true);
		valContext.setSignatureCheck(true);
		valContext.setSignatureAllowedCertificates(signatureAllowedCertificates);
		return valContext;
	}

	
	private StreamSource toStreamSource(byte[] buff) {
		return new StreamSource(new ByteArrayInputStream(buff));
	}
	
	private String tslHash(byte[] buff) {
		return new DigestUtils(MessageDigestAlgorithms.SHA_256).digestAsHex(buff);
	}
	
	private String sha2Location(String location) {
	    if (location.endsWith("xml")) {
	    	String prefix = location.substring(0, location.length() - 3);
	    	return prefix + "sha2";
	    } else if (location.endsWith("XML")) {
	    	String prefix = location.substring(0, location.length() - 3);
	    	return prefix + "SHA2";
		} else if (location.endsWith("xtsl")) {
	    	String prefix = location.substring(0, location.length() - 4);
	    	return prefix + "sha2";
	    } else {
	    	return location;
	    }
	}
	
	private static class DownloadException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		private String errorMessage;

		private int errorCode;
		
		
		public DownloadException(String message, String errorMessage, int errorCode) {
			super(message);
			this.errorMessage = errorMessage;
			this.errorCode = errorCode;
		}
		
		public String getErrorMessage() {
			return errorMessage;
		}
		
		public int getErrorCode() {
			return errorCode;
		}
		
	}
	

}
