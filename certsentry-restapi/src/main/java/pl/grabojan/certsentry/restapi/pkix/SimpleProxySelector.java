package pl.grabojan.certsentry.restapi.pkix;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class SimpleProxySelector extends ProxySelector {

	private ProxySelector defaultSelector;
	private Proxy proxy;
	
	public SimpleProxySelector(String proxyUrl) {
		defaultSelector = ProxySelector.getDefault();
		proxy = createProxy(proxyUrl);
	}

	@Override
	public List<Proxy> select(URI uri) {
		
		String protocol = uri.getScheme();
        if ("http".equalsIgnoreCase(protocol) ||
                "https".equalsIgnoreCase(protocol)) {
                return Collections.singletonList(proxy);
        }
		
        if (defaultSelector != null) {
            return defaultSelector.select(uri);
        } else {  
            return Collections.singletonList(Proxy.NO_PROXY);
        }
	}

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		// noop
	}
	
	private Proxy createProxy(String url) {
		
		String text = url;
		String scheme = "http";
		final int schemeIdx = text.indexOf("://");
		if (schemeIdx > 0) {
			scheme = text.substring(0, schemeIdx);
			text = text.substring(schemeIdx + 3);
		}
		int port = 8080;
		final int portIdx = text.lastIndexOf(":");
		if (portIdx > 0) {
			try {
				port = Integer.parseInt(text.substring(portIdx + 1));
			} catch (final NumberFormatException ex) {
				throw new IllegalArgumentException("Invalid HTTP proxy: " + text);
			}
			text = text.substring(0, portIdx);
		}
	
		return new Proxy(Proxy.Type.valueOf(scheme.toUpperCase()), new InetSocketAddress(text, port));
	}

}
