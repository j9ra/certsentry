package pl.grabojan.certsentry.tsldownload.util;

import javax.xml.parsers.DocumentBuilder;

public interface PooledDocumentBuilder {

	DocumentBuilder createDocumentBuilder();

}