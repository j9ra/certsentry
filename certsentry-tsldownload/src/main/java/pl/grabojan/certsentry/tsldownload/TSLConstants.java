package pl.grabojan.certsentry.tsldownload;

public interface TSLConstants {

	String TSLTAG = "http://uri.etsi.org/19612/TSLTag";
	String TSLTYPE_EULOTL = "http://uri.etsi.org/TrstSvc/TrustedList/TSLType/EUlistofthelists";
	String TSLTYPE_EUGENERIC = "http://uri.etsi.org/TrstSvc/TrustedList/TSLType/EUgeneric";
	
	String SVCTYPE_QC_CA = "http://uri.etsi.org/TrstSvc/Svctype/CA/QC";
	String SVCTYPE_QC_NATIONALROOTCA = "http://uri.etsi.org/TrstSvc/Svctype/NationalRootCA-QC";
	
	String SVCSTATUS_GRANTED = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/granted";
	String SVCSTATUS_RECOGNISED_AT_NATIONALLEVEL = "http://uri.etsi.org/TrstSvc/TrustedList/Svcstatus/recognisedatnationallevel";
	
	String CERTSTATUS_QC_CRL = "http://uri.etsi.org/TrstSvc/Svctype/Certstatus/CRL/QC";
	String CERTSTATUS_QC_OCSP = "http://uri.etsi.org/TrstSvc/Svctype/Certstatus/OCSP/QC";
	
	String SVCINFOEXT_FOR_ESIGNATURES = "http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/ForeSignatures";
	String SVCINFOEXT_FOR_ESEALS = "http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/ForeSeals";
	String SVCINFOEXT_FOR_WEBSITEAUTHENTICATION = "http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/ForWebSiteAuthentication";
	String SVCINFOEXT_ROOTCA_QC = "http://uri.etsi.org/TrstSvc/TrustedList/SvcInfoExt/RootCA-QC";
	
	
}
