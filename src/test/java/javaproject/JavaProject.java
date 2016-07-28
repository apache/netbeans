package javaproject;

import java.io.IOException;
import mockproject.MockActiveDocumentProvider;
import mockproject.MockKotlinParserFactory;
import mockproject.MockOpenProjectsTrampoline;
import mockproject.TestEnvironmentFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
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
            createMockLookup();
            project = createJavaProject();
            OpenProjects.getDefault().open(new Project[]{project}, false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Project createJavaProject() throws IOException {
        ProjectFactory projectFactory = new JavaProjectFactory();
        
        return projectFactory.loadProject(JavaProjectUnzipper.INSTANCE.getProjectFolder(), new JavaProjectState());
    }
    
    private void createMockLookup() {
        MockServices.setServices(AntBasedProjectFactorySingleton.class);
        MockServices.setServices(org.netbeans.modules.project.ant.StandardAntArtifactQueryImpl.class);
        MockServices.setServices(TestEnvironmentFactory.class);
        MockServices.setServices(MockKotlinParserFactory.class);
        MockServices.setServices(MockActiveDocumentProvider.class);
        MockServices.setServices(MockOpenProjectsTrampoline.class);
    }
    
    public Project getJavaProject() {
        return project;
    }
}
