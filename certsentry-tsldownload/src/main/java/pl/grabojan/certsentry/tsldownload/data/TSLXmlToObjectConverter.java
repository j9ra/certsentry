package pl.grabojan.certsentry.tsldownload.data;


import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import pl.grabojan.certsentry.schema.tsl.TrustServiceStatusList;

public class TSLXmlToObjectConverter {
	
	private final Jaxb2Marshaller marshaller;
	
	public TSLXmlToObjectConverter(Jaxb2Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	
	public TSLDataWrapper convertToTSLDataWrapper(StreamSource inputStreamSource) {
		
		TrustServiceStatusList tsl = (TrustServiceStatusList) marshaller.unmarshal(inputStreamSource);
		return new TSLDataWrapper(tsl);
	}
}
