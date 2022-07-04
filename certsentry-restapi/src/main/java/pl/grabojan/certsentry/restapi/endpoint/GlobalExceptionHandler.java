package pl.grabojan.certsentry.restapi.endpoint;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.util.CertificateServiceFailureException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	// on invalid structured parameter
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.toList());
        
        Map<String, Object> body = createErrorBody(status.value(), errors);
        
        log.error("Validation error for: {}, status: {}, error: {}", ex.getBindingResult().getTarget(), status, errors );

        return new ResponseEntity<>(body, headers, status);
		
		
	}
	
	// on invalid simple parameter
	@ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolationException(ConstraintViolationException ex, HandlerMethod mth) {
				
        List<String> errors = ex.getConstraintViolations().
        		stream().
        		map(e -> e.getMessage()).
        		collect(Collectors.toList());
        
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST.value(), errors);
        
        log.error("Validation error for metod: {}, error: {}", mth.getMethod(), errors );
        
		return new ResponseEntity<Object>(body, HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(CertificateServiceFailureException.class)
	public ResponseEntity<Object> certificateParseException(CertificateServiceFailureException e) {
		
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST.value(), 
        		Collections.singletonList("Invalid certificate " + e.getMessage()));
        
        log.error("Invalid certificate parameter ", e);
		
        return new ResponseEntity<Object>(body, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(NoSuchProfileException.class)
	public ResponseEntity<Object> noProfileException(NoSuchProfileException e) {
		
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST.value(), 
        		Collections.singletonList("Invalid certificate " + e.getMessage()));
        
        log.error("Invalid profile parameter ", e);
		
        return new ResponseEntity<Object>(body, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Object> runtimeException(RuntimeException e) {

		 Map<String, Object> body = createErrorBody(HttpStatus.SERVICE_UNAVAILABLE.value(),
				 Collections.singletonList("General Server Error"));
		
		log.error("RuntimeException ", e);
		
        return new ResponseEntity<Object>(body, HttpStatus.SERVICE_UNAVAILABLE);
	}
	
	
	private Map<String, Object> createErrorBody(int statusCode, List<String> errors) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", new Date());
        body.put("status", statusCode);
        body.put("errors", errors);
        
        return body;
	}

}
