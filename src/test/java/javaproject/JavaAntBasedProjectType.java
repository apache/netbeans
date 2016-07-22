package javaproject;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 *
 * @author Alexander.Baratynski
 */
public class JavaAntBasedProjectType implements AntBasedProjectType{
    
    private static final String TYPE = "org.netbeans.modules.java.j2seproject"; 
    private static final String PROJECT_CONFIGURATION_NAME = "data"; 
    private static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/project/1"; 
    private static final String PRIVATE_CONFIGURATION_NAME = "data"; 
    private static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/project-private/1"; 

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Project createProject(AntProjectHelper helper) throws IOException {
        return new J2SEProject(helper);
    }

    @Override
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }

    @Override
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAMESPACE : PRIVATE_CONFIGURATION_NAMESPACE;
    }
    
}
