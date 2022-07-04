package pl.grabojan.certsentry.tsldownload.validation;

import java.io.InputStream;

public interface XmlTSLSignatureValidator {

	boolean validate(InputStream inputStream, TSLValidatorContext ctx);

}