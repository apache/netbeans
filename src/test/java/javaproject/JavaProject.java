package javaproject;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class JavaProject extends NbTestCase {
    
    public static JavaProject INSTANCE = new JavaProject();
    private Project project = null;
    
    private JavaProject(){
        super("Java project");
        try {
            project = createJavaProject();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Project createJavaProject() throws IOException {
        ProjectFactory projectFactory = new JavaProjectFactory();
        
        return projectFactory.loadProject(JavaProjectUnzipper.INSTANCE.getProjectFolder(), new JavaProjectState());
    }
    
    public Project getJavaProject() {
        return project;
    }
}
