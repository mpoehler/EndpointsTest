package endpointstest;

import com.google.appengine.api.NamespaceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by marco on 18.08.15.
 *
 * This filter sets the Namespace
 */
public class NamespaceFilter implements Filter {

    public static Log log = LogFactory.getLog(NamespaceFilter.class);

    private String namespace;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        namespace = filterConfig.getInitParameter("namespace");
        log.info("init namespace filter with " + namespace);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("call namespace filter");
        if (NamespaceManager.get() == null) {
            NamespaceManager.set(namespace);
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {
    }
}
