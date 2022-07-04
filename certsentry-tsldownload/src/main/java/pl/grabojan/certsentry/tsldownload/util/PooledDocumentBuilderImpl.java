package pl.grabojan.certsentry.tsldownload.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import lombok.extern.slf4j.Slf4j;

/** 
 * Pooled DocumentBuilderFactory
 * <b>Not thread safe! Use only with Spring AOP CommonsPool2TargetSource</b>
 * @author janusz
 *
 */
@Slf4j
public class PooledDocumentBuilderImpl implements PooledDocumentBuilder {

	private DocumentBuilderFactory factory;
	
	public PooledDocumentBuilderImpl(Schema schema) {
		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setSchema(schema);
		
		try {
			// preserve original element/attributes values after schema validation
			factory.setFeature("http://apache.org/xml/features/validation/schema/normalized-value", false);
		} catch (ParserConfigurationException e) {
			log.error("DocumentBuilderFactory setting features failed",e);
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public DocumentBuilder createDocumentBuilder() {
		log.debug("createDocumentBuilder() call on instance: [{}]", this); 
		try {
			return factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("failed to create new DocumentBuilder", e);
		} finally {
			log.debug("createDocumentBuilder() return on instance: [{}]", this);
		}
	}
	
}
