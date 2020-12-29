/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.project2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.python.api.PythonException;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ProjectFactory.class)
public class PythonProjectFactory implements ProjectFactory {
    

    @Override
    public boolean isProject(FileObject projectDirectory) {
        return PythonProject2.isProject(projectDirectory);
    }

    //Specifies when the project will be opened, i.e., if the project exists:
    @Override
    public Project loadProject(FileObject dir, ProjectState state) throws IOException {
        try {
            return isProject(dir) ? new PythonProject2(dir, state) : null;
        } catch (PythonException ex) {
            LOG.log(Level.WARNING, "Unable to load project.", ex);
            return null;
        }
    }
    private static final Logger LOG = Logger.getLogger(PythonProjectFactory.class.getName());

    @Override
    public void saveProject(final Project project) throws IOException, ClassCastException {
        // leave unimplemented for the moment
    }

}
