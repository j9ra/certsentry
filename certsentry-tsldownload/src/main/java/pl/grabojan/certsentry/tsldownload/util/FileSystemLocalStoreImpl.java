package pl.grabojan.certsentry.tsldownload.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FileSystemLocalStoreImpl implements FileSystemLocalStore {

	private final String localStorePathName;
	private final boolean archiveCopy;
	private Path parent;
			
	public void init() {
		parent = Paths.get(localStorePathName);
		if(Files.notExists(parent)) {
			try {
				Files.createDirectories(parent);
				log.info("Created local store dir: {}", localStorePathName);
			} catch (IOException e) {
				throw new RuntimeException("unable to create store directory " + localStorePathName, e);
			}
		}
	}
	
	@Override
	public String storeFile(String relativeName, byte[] blob) {
		
		if(parent == null) {
			throw new IllegalStateException("store not initialized - parent is null");
		}
		
		if(relativeName == null || relativeName.length() == 0) {
			throw new IllegalArgumentException("RelativeName can not be null");
		}
						
		String baseFileName = safeFileName(relativeName);
		String tempFileName = baseFileName + ".new";
		String archFileName = baseFileName + ".old";
				
		OutputStream os = null;
		try {

			// safe update file on existing resource
			if(Files.exists(relativePath(baseFileName))) {
				if(archiveCopy) {
					Files.copy(relativePath(baseFileName), relativePath(archFileName), REPLACE_EXISTING);
				}
				os = Files.newOutputStream(relativePath(tempFileName), CREATE_NEW);
				os.write(blob);
				
				Files.move(relativePath(tempFileName),relativePath(baseFileName), REPLACE_EXISTING);
				
			} else { // no resource - just save data to file
				os = Files.newOutputStream(relativePath(baseFileName), CREATE_NEW);
				os.write(blob);
			}
			
			return baseFileName;

		} catch(IOException ioe) {
			log.error("Unable to store file", ioe);
			// conpensate deleting new file if exists
			Path np = relativePath(tempFileName);
			if(Files.exists(np)) {
				log.error("Compensating - deleting temporary resource {}", np);
				try {
					Files.delete(np);
				} catch (IOException e) {}
			}
			throw new RuntimeException("Store failed", ioe);
		} finally {
			if(os != null) {
				try {
					os.close();
				} catch (IOException e) {}
			}
		}
	
		
	}

	public byte[] getFile(String relativeName) {
	
		Path targetFilePath = relativePath(relativeName);
		
		if(Files.exists(targetFilePath)) {
			try {
				return Files.readAllBytes(targetFilePath);
			} catch (IOException e) {
				throw new RuntimeException("IO Error, accessing file: " +
						targetFilePath, e);
			}
		} else {
			throw new IllegalStateException("No such file error " + 
					targetFilePath);
		}
	}
	
	private String safeFileName(String name) {
		try {
			return URLEncoder.encode(name,"ASCII");
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to encode file name {}", name);
			throw new RuntimeException("Encode failed", e);
		}
	}
	
	private Path relativePath(String name) {
		return Paths.get(parent.toString(),name);
	}
	
	
	
	
}
