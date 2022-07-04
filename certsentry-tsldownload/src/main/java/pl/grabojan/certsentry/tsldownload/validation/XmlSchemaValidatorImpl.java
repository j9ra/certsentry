package pl.grabojan.certsentry.tsldownload.validation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.core.io.Resource;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class XmlSchemaValidatorImpl implements XmlSchemaValidator {

	private List<Source> sources = new ArrayList<>();
	private Schema schema;
	
	public void addSchema(InputStream schema) {
		Source schemaFile = new StreamSource(schema);
		log.debug("Adding schema {}", schemaFile.getSystemId());
		sources.add(schemaFile);
	}
	
	public void compileSchema(Map<String, Resource> resourcesMapping) {
		
		if(sources.isEmpty()) {
			log.error("Unable to compile schema - no schema files added");
			throw new IllegalStateException("no schema files added!");
		}
		
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			factory.setResourceResolver(new ResourceMappingResourceResolver(resourcesMapping));
			schema = factory.newSchema(sources.toArray(new Source[sources.size()]));
			
		} catch (SAXException e) {
			log.error("Schema factory exception", e);
			throw new RuntimeException("Unable to build schema", e);
		}
	}
	
	public Schema getSchema() {
		return schema;
	}
	
	@Override
	public boolean validate(Source document, TSLValidatorContext ctx) {
			
		try {
			Validator validator = schema.newValidator();
			validator.validate(document);
			return true;
		} catch (SAXException|IOException e) {
			log.error("Validator exception", e);
			ctx.setErrorMessage(e.getMessage());
			ctx.setErrorCode(-2);
			return false;
		} 
	}
	
	/*
	 * Local resources resolver
	 */
	private static class ResourceMappingResourceResolver implements LSResourceResolver {

		private Map<String, Resource> resources;
		
		public ResourceMappingResourceResolver(Map<String, Resource> resourcesMapping) {
			this.resources = resourcesMapping;
		}
		
		@Override
		public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
				String baseURI) {
			
			log.debug("Resolving publicId: {}, namespaceURI: {}, baseURI: {}",
					publicId, namespaceURI, baseURI);
			
			if(!systemId.startsWith("http")) {
				int pathEndIdx = baseURI.lastIndexOf('/');
				systemId = baseURI.substring(0, pathEndIdx + 1) + systemId;
			}
			
			if(resources.containsKey(systemId)) {
				Resource resource = resources.get(systemId);
				InputStream is = null;
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				try {	
					is = resource.getInputStream();
					int nRead;
				    byte[] data = new byte[1024];
				    while ((nRead = is.read(data, 0, data.length)) != -1) {
				        buffer.write(data, 0, nRead);
				    }
				    buffer.flush();
				} catch (IOException e) {
					throw new RuntimeException("Failed to read file " + systemId, e);
				} finally {
					if(is != null) {
						try {
							is.close();
						} catch (IOException e) { 
							// noop
						}
					}
				}
				return new Input(publicId, systemId, buffer.toByteArray());
			}
			log.debug("resolve failed, passing null for parent resolving");
			return null;
		}
		
	}
	
	private static class Input implements LSInput {
		
		private String publicId;

		private String systemId;
		
		private byte[] inputBuff;
		
		public Input(String publicId, String sysId, byte[] inputBuff) {
		    this.publicId = publicId;
		    this.systemId = sysId;
		    this.inputBuff = inputBuff;
		}

		@Override
		public Reader getCharacterStream() {
			return null;
		}

		@Override
		public void setCharacterStream(Reader characterStream) {
		}

		@Override
		public InputStream getByteStream() {
			return null;
		}

		@Override
		public void setByteStream(InputStream byteStream) {
		}

		@Override
		public String getStringData() {
			
			byte b1 = inputBuff[0];
			byte b2 = inputBuff[1];
			
			String encoding = null;
			if(b1 == 0x3c) {
				encoding = "UTF-8";
			} else if(b1 == 0x0 && b2 == 0x3c) {
				encoding = "UTF-16";
			} else if(b1 == 0xff && b2 == 0xfe) {
				encoding = "UTF-16LE";
			} else if(b1 == 0xfe && b2 == 0xff) {
				encoding = "UTF-16BE";
			} else {
				throw new IllegalStateException("Unable to determine content encoding, got [" 
							+ String.format("%X %X", b1, b2 ) );
			}
			
			try {
				return new String(inputBuff,encoding);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("String encoding " +  encoding + " exception", e);
			}
		}

		@Override
		public void setStringData(String stringData) {
		}

		@Override
		public String getSystemId() {
			return this.systemId;
		}

		@Override
		public void setSystemId(String systemId) {
			this.systemId = systemId;
			
		}

		@Override
		public String getPublicId() {
			return this.publicId;
		}

		@Override
		public void setPublicId(String publicId) {
			this.publicId = publicId;
			
		}

		@Override
		public String getBaseURI() {
			return null;
		}

		@Override
		public void setBaseURI(String baseURI) {			
		}

		@Override
		public String getEncoding() {
			return null;
		}

		@Override
		public void setEncoding(String encoding) {
		}

		@Override
		public boolean getCertifiedText() {
			return false;
		}

		@Override
		public void setCertifiedText(boolean certifiedText) {
		}
		
	
	}
}
