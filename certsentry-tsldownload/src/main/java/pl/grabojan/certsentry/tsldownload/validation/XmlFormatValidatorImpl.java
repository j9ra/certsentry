package pl.grabojan.certsentry.tsldownload.validation;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlFormatValidatorImpl implements XmlFormatValidator {

	private XMLInputFactory factory; 
		
	public XmlFormatValidatorImpl() {
		 factory = XMLInputFactory.newInstance();
	}
	
	@Override
	public boolean validate(Source document, TSLValidatorContext context) {
		
		try {
			XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(document);
			
			while(xmlStreamReader.hasNext()) {
				xmlStreamReader.next();
		    }
			return true;
			 
		} catch (XMLStreamException e) {
			log.error("XML parsing failed", e);
			context.setErrorMessage(e.getMessage());
			context.setErrorCode(-1);
			return false;
		}

	}

}
