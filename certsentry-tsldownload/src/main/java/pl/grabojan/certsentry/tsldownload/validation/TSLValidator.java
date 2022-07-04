package pl.grabojan.certsentry.tsldownload.validation;

import java.io.InputStream;

public interface TSLValidator {

	TSLValidatorInfo validate(InputStream inputStream, TSLValidatorContext context);

}