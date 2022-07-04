package pl.grabojan.certsentry.tsldownload.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.grabojan.certsentry.schema.tsl.AnyType;
import pl.grabojan.certsentry.schema.tsl.OtherTSLPointer;
import pl.grabojan.certsentry.schema.tsl.TrustServiceStatusList;
import pl.grabojan.certsentry.tsldownload.TSLConstants;

@RequiredArgsConstructor
public class TSLDataWrapper implements TSLConstants {
	
	private final @Getter TrustServiceStatusList trustServiceStatusList;
	
	public boolean isLOTL() {
		return TSLTYPE_EULOTL.equals(trustServiceStatusList.getSchemeInformation().getTSLType());
	}
	
	public List<String> getPointersToOtherTSLLocations() {
		List<String> retList = new ArrayList<>();
		List<OtherTSLPointer> pointers = trustServiceStatusList.getSchemeInformation().getPointersToOtherTSL().getOtherTSLPointers();
		pointers.forEach(p -> { 
			if(isPointerInXmlMimeType(p) && isPointerInListTypeGeneric(p)) {	
				retList.add(p.getTSLLocation());
			};
		});
		return retList;
	}
	
	public List<byte[]> getPointerServiceDigitalIdentities(String pointerLocation) {

		List<byte[]> retList = new ArrayList<>();
		List<OtherTSLPointer> pointers = trustServiceStatusList.getSchemeInformation().getPointersToOtherTSL().getOtherTSLPointers();
		pointers.forEach(p -> { 
			if(p.getTSLLocation().equals(pointerLocation)) {
				p.getServiceDigitalIdentities().getServiceDigitalIdentities().forEach(sdi -> {
					sdi.getDigitalIds().forEach(i -> {
						byte[] certBin = i.getX509Certificate();
						if(certBin != null) {
								retList.add(certBin);
						}
					});
				});
			}
		});
		return retList;
	}
	
	public String getPointerSchemeTerritory(String pointerLocation) {
		
		JAXBElement<String> schemeTerritory = tslFactory.createSchemeTerritory("any");
		String value = null;	
		List<OtherTSLPointer> pointers = trustServiceStatusList.getSchemeInformation().getPointersToOtherTSL().getOtherTSLPointers();
		for (OtherTSLPointer p : pointers) {
			if(p.getTSLLocation().equals(pointerLocation)) {
				List<Object> textInfAndOthrInf = p.getAdditionalInformation().getTextualInformationsAndOtherInformations();
				value = findElementInAnyType(textInfAndOthrInf, schemeTerritory).orElseThrow(IllegalStateException::new);
			}			
		}
		return value;
		
	}
	
	
	@SuppressWarnings("unchecked")
	private Optional<String> findElementInAnyType(List<Object> haystack, JAXBElement<String> needle) {
		for (Object o : haystack) {
			if(o instanceof AnyType) { // AnyType only / skip unknown elements
				AnyType any = (AnyType)o;
				List<Object> contentList = any.getContent();
				for (Object c : contentList) {
					if(needle.getClass().isAssignableFrom(c.getClass())) { // match element
						JAXBElement<String> e = (JAXBElement<String>)c;
						if(needle.getName().equals(e.getName())) {			
								return Optional.of(e.getValue());
						}	
					}
				}
			}
		}
		return Optional.empty();
	}
	
	
	@SuppressWarnings("unchecked")
	private boolean existsElementInAnyType(List<Object> haystack, JAXBElement<String> needle, boolean matchValue) {
		for (Object o : haystack) {
			if(o instanceof AnyType) { // AnyType only / skip unknown elements
				AnyType any = (AnyType)o;
				List<Object> contentList = any.getContent();
				for (Object c : contentList) {
					if(needle.getClass().isAssignableFrom(c.getClass())) { // match element
						JAXBElement<String> e = (JAXBElement<String>)c;
						if(needle.getName().equals(e.getName())) {			
							if(matchValue) {
								 if(needle.getValue().equals(e.getValue())) {
									 return true;
								 }
							} else {
								return true;
							}
						}	
					}
				}
			}
		}
		return false;
	}
	
	private boolean isPointerInXmlMimeType(OtherTSLPointer pointer) {
		
		JAXBElement<String> tslMimeType = tslAddTypFactory.createMimeType("application/vnd.etsi.tsl+xml");
		
		List<Object> textInfAndOthrInf = pointer.getAdditionalInformation().getTextualInformationsAndOtherInformations();
		return existsElementInAnyType(textInfAndOthrInf, tslMimeType, true);
	}
	
	private boolean isPointerInListTypeGeneric(OtherTSLPointer pointer) {
			
		JAXBElement<String> tslType = tslFactory.createTSLType("http://uri.etsi.org/TrstSvc/TrustedList/TSLType/EUgeneric");
		
		List<Object> textInfAndOthrInf = pointer.getAdditionalInformation().getTextualInformationsAndOtherInformations();
		return existsElementInAnyType(textInfAndOthrInf, tslType, true);
	}
	
	private pl.grabojan.certsentry.schema.tsladdtyp.ObjectFactory tslAddTypFactory =
			new pl.grabojan.certsentry.schema.tsladdtyp.ObjectFactory();
	
	private pl.grabojan.certsentry.schema.tsl.ObjectFactory tslFactory =
			new pl.grabojan.certsentry.schema.tsl.ObjectFactory();
}


