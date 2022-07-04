package pl.grabojan.certsentry.tsldownload.validation;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;

import org.springframework.xml.xpath.XPathExpression;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.tsldownload.TSLConstants;
import pl.grabojan.certsentry.tsldownload.util.PooledDocumentBuilder;

@Slf4j
@RequiredArgsConstructor
public class XmlTSLFormatValidatorImpl implements XmlTSLFormatValidator, TSLConstants {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
	
	private final XPathExpression versionExpr;
	private final XPathExpression typeExpr;
	private final XPathExpression nextUpdateExpr;
	private final PooledDocumentBuilder pooledDocumentBuilderProxy;
	
	@Override
	public boolean validate(InputStream inputStream, TSLValidatorContext ctx) {
		
		try {
			DocumentBuilder documentBuilder = pooledDocumentBuilderProxy.createDocumentBuilder();
			Document doc = documentBuilder.parse(inputStream);
			
			Element rootElement = doc.getDocumentElement();
			
			String attr1Val = rootElement.getAttribute("TSLTag");
			log.debug("Tag: {}", attr1Val);
			if(!TSLTAG.equals(attr1Val)) {
				log.error("Xml document dont have TSLTag attribute!");
				ctx.setErrorMessage("Xml document dont have TSLTag attribute!");
				ctx.setErrorCode(-1);
				return false;
			}

			double version = versionExpr.evaluateAsNumber(rootElement);
			String type = typeExpr.evaluateAsString(rootElement);
			String nextUpdateText = nextUpdateExpr.evaluateAsString(rootElement);
			Date nextUpdate = (nextUpdateText.length() > 0) ? dateFormat.parse(nextUpdateText) : null;

			log.debug("Validator context {}", ctx);
			log.debug("TSL xml document - version: {}, type: {}, nextUpdate: {}",
					version, type, nextUpdate);

			if (version == (double)ctx.getTslVersion() && 
					ctx.getTslType().equals(type)
					&& ( nextUpdate == null || nextUpdate.after(ctx.getValidationDate()))) { 
				return true;
			} else {
				log.info("version: {}, type: {}, nextUpdate: {}", version, type, nextUpdate);
				ctx.setErrorMessage("failed to verify values");
				ctx.setErrorCode(-2);
				return false;
			}

		} catch (SAXException | IOException e) {
			log.error("Unable to convert string to Document", e);
			ctx.setErrorMessage(e.getMessage());
			ctx.setErrorCode(-3);
			return false;
		} catch (ParseException e) {
			log.error("Unable to parse Document", e);
			ctx.setErrorMessage(e.getMessage());
			ctx.setErrorCode(-4);
			return false;
		}

		
	}
	
	
}
