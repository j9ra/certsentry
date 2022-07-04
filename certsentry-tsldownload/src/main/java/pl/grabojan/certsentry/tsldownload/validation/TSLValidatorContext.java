package pl.grabojan.certsentry.tsldownload.validation;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class TSLValidatorContext implements Cloneable {
	
	private int tslVersion;
	private String tslType;
	private Date validationDate;
	private boolean schemaCheck;
	private boolean signatureCheck;
	private List<X509Certificate> signatureAllowedCertificates = new ArrayList<>();

	private String errorMessage;
	private int errorCode;
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		TSLValidatorContext copy = (TSLValidatorContext)super.clone();
		copy.signatureAllowedCertificates = new ArrayList<>(this.signatureAllowedCertificates);
		return copy;
	}
	
	
}
