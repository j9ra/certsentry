package pl.grabojan.certsentry.tsldownload.util;

public interface FileSystemLocalStore {

	String storeFile(String relativeName, byte[] blob);
	byte[] getFile(String relativeName);

}