package pl.grabojan.certsentry.tsldownload.validation;

import java.io.InputStream;

public interface XmlTSLFormatValidator {

	boolean validate(InputStream inputStream, TSLValidatorContext ctx);

}