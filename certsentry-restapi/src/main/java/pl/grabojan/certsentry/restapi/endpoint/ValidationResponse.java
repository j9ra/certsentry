package pl.grabojan.certsentry.restapi.endpoint;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ValidationResponse {

	private String status;
	private String reasonMessage;
	private List<String> certPath = new ArrayList<>();
	private String ref;
}
