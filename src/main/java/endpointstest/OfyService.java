package endpointstest;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;

public class OfyService {

    private Set<Class> classes;

    public static Log log = LogFactory.getLog(OfyService.class);

    public void init () {
        for (Class clazz : classes) {
            ObjectifyService.register(clazz);
            log.info("init objectify for class " + clazz.getName());
        }
    }

	public Objectify ofy() {
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
}
