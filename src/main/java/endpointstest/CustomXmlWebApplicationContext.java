package endpointstest;

import com.google.appengine.api.utils.SystemProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class CustomXmlWebApplicationContext extends XmlWebApplicationContext {

    public static Log log = LogFactory.getLog(CustomXmlWebApplicationContext.class);
    
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
		super.initBeanDefinitionReader(beanDefinitionReader);
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            log.info("production mode - disable validation");
            beanDefinitionReader.setValidating(false);
			beanDefinitionReader.setNamespaceAware(true);
		} else {
            log.info("development mode - enable validation");
        }
	}
}
