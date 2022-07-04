package pl.grabojan.certsentry.tsldownload.validation;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;

public interface XmlSchemaValidator {

	boolean validate(Source document, TSLValidatorContext ctx);
	Schema getSchema();

}