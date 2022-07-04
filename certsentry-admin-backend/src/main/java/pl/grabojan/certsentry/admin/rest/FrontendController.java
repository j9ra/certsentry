package pl.grabojan.certsentry.admin.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
public class FrontendController {
	
	// Forwards all routes to FrontEnd except: '/', '/index.html', '/api', '/api/**'
    // Required because of 'mode: history' usage in frontend routing VUE.JS
	@RequestMapping(value = "{_:^(?!index\\.html|api).*$}")
    public String redirectApi() {
        return "forward:/";
    }
}
