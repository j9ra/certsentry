package pl.grabojan.certsentry.tsldownload.validation;

import javax.xml.transform.Source;

public interface XmlFormatValidator {

	boolean validate(Source document, TSLValidatorContext context);
}
