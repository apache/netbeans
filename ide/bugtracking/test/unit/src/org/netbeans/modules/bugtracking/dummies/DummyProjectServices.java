/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.bugtracking.dummies;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.modules.team.ide.spi.IDEProject;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 *
 * @author tomas
 */
public class DummyProjectServices implements ProjectServices {

    @Override
    public <T> T runAfterProjectOpenFinished(Callable<T> operation) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileObject[] getOpenProjectsDirectories() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileObject getMainProjectDirectory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileObject getFileOwnerDirectory(FileObject fileObject) {
        return fileObject;
    }

    @Override
    public FileObject[] getCurrentSelection() {
        Node[] nodes = WindowManager.getDefault().getRegistry().getCurrentNodes();
        if(nodes == null) {
            return null;
        }
        List<FileObject> ret = new ArrayList<FileObject>(nodes.length);
        for (Node node : nodes) {
            if(node instanceof DummyNode) {
                FileObject fo = ((DummyNode) node).getAssociatedFileObject();
                if(fo != null) {
                    ret.add(fo);
                }
            }
        }
        return ret.toArray(new FileObject[0]);
    }
    
    @Override
    public boolean openProject(URL url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void openOtherProject(File workingDir) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDEProject getIDEProject(URL url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDEProject[] getOpenProjects() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addProjectOpenListener(IDEProject.OpenListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeProjectOpenListener(IDEProject.OpenListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File[] chooseProjects(File workingDir) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reopenProjectsFromNewLocation(File[] oldLocations, File[] newLocations) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createNewProject(File workingDir) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
