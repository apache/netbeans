/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

public class PythonProjectType implements AntBasedProjectType {

    public static final String TYPE = PythonProjectType.class.getPackage().getName();
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://nbpython.dev.java.net/ns/php-project/1"; // NOI18N
    private static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N

    private static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://nbpython.dev.java.net/ns/php-project-private/1"; // NOI18N
    private static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    
    //Probably it should become a part of python api.
    public static final String SOURCES_TYPE_PYTHON = "python"; // NOI18N

    @Override
    public Project createProject(AntProjectHelper helper) throws IOException {
        assert helper != null;
        return new PythonProject(helper);
    }

    @Override
    public String getPrimaryConfigurationDataElementName( boolean shared ) {
        /*
         * Copied from MakeProjectType.
         */
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }

    @Override
    public String getPrimaryConfigurationDataElementNamespace( boolean shared ) {
        /*
         * Copied from MakeProjectType.
         */
        return shared ? PROJECT_CONFIGURATION_NAMESPACE : PRIVATE_CONFIGURATION_NAMESPACE;
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
