package pl.grabojan.certsentry.tsldownload.validation;


import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TSLValidatorImpl implements TSLValidator {
	
	private final XmlFormatValidator xmlFormatValidator;
	private final XmlSchemaValidator xmlSchemaValidator;
	private final XmlTSLFormatValidator xmlTSLFormatValidator;
	private final XmlTSLSignatureValidator xmlTSLSignatureValidator;
	

	@Override
	public TSLValidatorInfo validate(InputStream inputStream, TSLValidatorContext context) {
		
		if(!inputStream.markSupported()) {
			throw new IllegalArgumentException("InputStream must support mark/reset");
		}
				
		TSLValidatorContext ctx = null;
		try {
			ctx = (TSLValidatorContext)context.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("failed to make copy of context!");
		}	
		
		resetInputStream(inputStream);
		if(!xmlFormatValidator.validate(new StreamSource(inputStream), ctx)) {
			return new TSLValidatorInfo(TSLValidatorInfo.Status.FAILED, 
					TSLValidatorInfo.Stage.FORMAT, ctx.getErrorMessage(), ctx.getErrorCode());
		}
		
		if(ctx.isSchemaCheck()) {
			resetInputStream(inputStream);
			if(!xmlSchemaValidator.validate(new StreamSource(inputStream), ctx)) {
				return new TSLValidatorInfo(TSLValidatorInfo.Status.FAILED, 
						TSLValidatorInfo.Stage.SCHEMA, ctx.getErrorMessage(), ctx.getErrorCode());
			}
		}
		
		resetInputStream(inputStream);
		if(!xmlTSLFormatValidator.validate(inputStream, ctx)) {
			return new TSLValidatorInfo(TSLValidatorInfo.Status.FAILED, 
					TSLValidatorInfo.Stage.LIST_SUPPORT, ctx.getErrorMessage(), ctx.getErrorCode());
		}
		
		if(ctx.isSignatureCheck()) {
			resetInputStream(inputStream);
			if(!xmlTSLSignatureValidator.validate(inputStream, ctx)) {
				return new TSLValidatorInfo(TSLValidatorInfo.Status.FAILED, 
						TSLValidatorInfo.Stage.SIGNATURE, ctx.getErrorMessage(), ctx.getErrorCode());
			}
		}
	
		return new TSLValidatorInfo(TSLValidatorInfo.Status.OK, TSLValidatorInfo.Stage.NONE, "", 0);
	}

	private void resetInputStream(InputStream is) {
		try {
			is.reset();
		} catch (IOException e) {
			throw new RuntimeException("Failed to reset input stream!", e);
		}
	}
}
