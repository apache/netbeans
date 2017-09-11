/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
