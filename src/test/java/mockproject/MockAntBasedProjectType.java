package mockproject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.spi.intern.ProjectIDEServices;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.Lookup;

/**
 *
 * @author Александр
 */
public class MockAntBasedProjectType implements AntBasedProjectType {
    private final String iconResource;
    private final String type;
    private final String className;
    private final String methodName;
    private final String[] configNames;
    private final String[] configNamespaces;

    public MockAntBasedProjectType() {
        iconResource = "org/black/kotlin/kotlin.png";
        type = "org.black.kotlin.project.KotlinProject";
        className = type;
        methodName = null;
        configNames = new String[]{
            "data",
            "project-private"
        };
        configNamespaces = new String[]{
            "http://www.netbeans.org/ns/kotlin-project/1",
            "http://www.netbeans.org/ns/kotlin-project-private/1"
        };
    }

    @Override
    public String getType() {
        return type;
    }

    public Icon getIcon() {
        return ProjectIDEServices.loadImageIcon(iconResource, true);
    }

    @Override
    public Project createProject(AntProjectHelper helper) throws IOException {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = MockAntBasedProjectType.class.getClassLoader();
        }
        try {
            Class<?> clazz = l.loadClass(className);
            Constructor c = clazz.getConstructor(AntProjectHelper.class);
            return (Project) c.newInstance(helper);

        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof IOException) {
                throw (IOException) ex.getTargetException();
            }
            if (ex.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ex.getTargetException();
            }
            throw new IllegalArgumentException(ex);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? configNames[0] : configNames[1];
    }

    @Override
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? configNamespaces[0] : configNamespaces[1];
    }
}
