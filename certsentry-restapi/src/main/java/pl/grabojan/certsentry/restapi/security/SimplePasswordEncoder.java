package pl.grabojan.certsentry.restapi.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;


@SuppressWarnings("deprecation")
public class SimplePasswordEncoder {

	public static void main(String[] args) {
		
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put("bcrypt", new BCryptPasswordEncoder());
		encoders.put("scrypt", new SCryptPasswordEncoder());
		encoders.put("sha256", new StandardPasswordEncoder());
		encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());

		

		
		  java.io.Console console = System.console();
	        String type = console.readLine("Type (bcrypt,scrypt,sha256,pbkdf2): ");
	        String password = new String(console.readPassword("Password: "));
		
	        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(type, encoders);
			String pass = passwordEncoder.encode(password);
			System.out.println(pass);
	        
	}
	
	
	
}
