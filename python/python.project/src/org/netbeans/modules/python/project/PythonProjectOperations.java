/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.openide.filesystems.FileObject;

public class PythonProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation,
        MoveOperationImplementation {

    private final PythonProject project;

    public PythonProjectOperations(final PythonProject project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public void notifyDeleted() throws IOException {
        project.getHelper().notifyDeleted();
    }

    @Override
    public void notifyDeleting() throws IOException {
    }


    @Override
    public void notifyCopied(Project originalProject, File file, String newName) throws IOException {
        if (originalProject == null) {
            // do nothing for the original project.
            return;
        }
        project.setName(newName);
    }

    @Override
    public void notifyCopying() throws IOException {
    }

    @Override
    public void notifyMoved(Project originalProject, File file, String newName) throws IOException {
        if (originalProject == null) {
            project.getHelper().notifyDeleted();
            return;
        }
        project.setName(newName);
    }

    @Override
    public void notifyMoving() throws IOException {
    }

    @Override
    public List<FileObject> getDataFiles() {
        final FileObject[] srcRoots = project.getSourceRoots().getRoots();
        final FileObject[] testRoots = project.getTestRoots().getRoots();
        final List<FileObject> result = new ArrayList<>(srcRoots.length + testRoots.length);
        result.addAll(Arrays.asList(srcRoots));
        result.addAll(Arrays.asList(testRoots));
        return result;
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        List<FileObject> files = new ArrayList<>(1);        
        FileObject nbProject = project.getHelper().getProjectDirectory().getFileObject("nbproject"); // NOI18N
        if (nbProject != null) {
            files.add(nbProject);
        }
        return files;
    }

}
