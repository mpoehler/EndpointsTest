package endpointstest;

import com.google.appengine.api.users.UserServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by marco on 11/17/15.
 */
public class LogoutServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(LogoutServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("call logout servlet");
        StringBuffer requestURL = req.getRequestURL();
        String root = requestURL.substring(0,requestURL.indexOf("/", "https://".length()));
        String logoutURL = UserServiceFactory.getUserService().createLogoutURL(root);
        log.info("logoutUrl " + logoutURL);
        resp.sendRedirect(logoutURL);
    }

}
