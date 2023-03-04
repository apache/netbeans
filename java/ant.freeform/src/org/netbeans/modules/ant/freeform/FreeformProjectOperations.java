/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 *
 * @author Jan Lahoda
 */
public class FreeformProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOrRenameOperationImplementation {
    
    private FreeformProject project;
    
    public FreeformProjectOperations(FreeformProject project) {
        this.project = project;
    }
    
    private static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null) {
            result.add(file);
        }
    }
    
    public List<FileObject> getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<FileObject>();
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        Element genldata = project.getPrimaryConfigurationData();
        Element foldersEl = XMLUtil.findElement(genldata, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        List<Element> folders = foldersEl != null ? XMLUtil.findSubElements(foldersEl) : Collections.<Element>emptyList();
        List<FileObject> result = new ArrayList<FileObject>();

        for (Element el : folders) {
            if ("source-folder".equals(el.getLocalName()) && FreeformProjectType.NS_GENERAL.equals(el.getNamespaceURI())) { // NOI18N
                addFile(el, result);
            }
        }
        
        addFile(project.getProjectDirectory(), "build.xml", result); // NOI18N
        
        return result;
    }
    
    private void addFile(Element folder, List<FileObject> result) {
        Element location = XMLUtil.findElement(folder, "location", FreeformProjectType.NS_GENERAL); // NOI18N
        
        if (location == null) {
            return ;
        }
        
        PropertyEvaluator evaluator = project.evaluator();
        String val = evaluator.evaluate(XMLUtil.findText(location));
        
        if (val == null) {
            return ;
        }
        
        File f = project.helper().resolveFile(val);
            
        if (f == null) {
            return ;
        }
        
        FileObject fo = FileUtil.toFileObject(f);
        
        if (fo != null && FileUtil.isParentOf(project.getProjectDirectory(), fo)) {
            result.add(fo);
        }
    }
    
    public void notifyDeleting() throws IOException {
        //TODO: invoke clean action if bound.
    }
    
    public void notifyDeleted() throws IOException {
        project.helper().notifyDeleted();
    }

    public void notifyCopying() throws IOException {
    }

    public void notifyCopied(Project original, File originalPath, String nueName) throws IOException {
        if (original != null) {
            project.setName(nueName);
        }
    }

    public void notifyMoving() throws IOException {
    }

    public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
        if (original != null) {
            project.setName(nueName);
        } else {
            project.helper().notifyDeleted();
        }
    }

    public @Override void notifyRenaming() throws IOException {
    }

    public @Override void notifyRenamed(String nueName) throws IOException {
        project.setName(nueName);
    }
    
}
