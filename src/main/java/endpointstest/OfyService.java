package endpointstest;

import com.google.appengine.api.NamespaceManager;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;

public class OfyService {

    private Set<Class> classes;
    
    private String namespace;

    public static Log log = LogFactory.getLog(OfyService.class);

    public void init () {
        NamespaceManager.set(namespace);
        for (Class clazz : classes) {
            ObjectifyService.register(clazz);
            log.info("init objectify for class " + clazz.getName());
        }
    }

	public Objectify ofy() {
        NamespaceManager.set(namespace);
        return ObjectifyService.ofy();
	}

	public ObjectifyFactory factory() {
		return ObjectifyService.factory();
	}

    public Set<Class> getClasses() {
        return classes;
    }

    public void setClasses(Set<Class> classes) {
        this.classes = classes;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
