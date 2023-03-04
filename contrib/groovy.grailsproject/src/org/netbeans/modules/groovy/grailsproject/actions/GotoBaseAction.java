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

package org.netbeans.modules.groovy.grailsproject.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Base class for all Goto actions. It should encapsule all the general logic
 * and make the concrete implementation as simple as possible.
 * 
 * @author Martin Janicek
 */
public abstract class GotoBaseAction extends BaseAction {

    protected static final String GSP_MIME_TYPE = "text/x-gsp"; // NOI18N


    protected abstract FileObject getTargetFO(String fileName, FileObject sourceFO);
    protected abstract String getTargetFilePath(String filename, FileObject sourceFO);


    
    public GotoBaseAction(String name) {
        super(name);
    }

    @Override
    public boolean isEnabled() {
        if (isValid(Utilities.getFocusedComponent()) == false) {
            return false;
        }

        FileObject fileObject = findTargetFO();
        if (fileObject != null && fileObject.canRead()) {
            return true;
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent caller, JTextComponent sourceComponent) {
        FileObject targetFO = findTargetFO();

        if (targetFO != null && targetFO.isValid()) {
            GsfUtilities.open(targetFO, 0, "");
        }
    }

    private FileObject findTargetFO() {
        DataObject dataObject = getDataObjectFromComponent(Utilities.getFocusedComponent());
        FileObject sourceFO = dataObject.getPrimaryFile();
        return getTargetFO(dataObject.getName(), sourceFO);
    }

    protected final boolean isGspFO(FileObject fo)  {
        if (GSP_MIME_TYPE.equals(fo.getMIMEType())) {
            return true;
        }
        return false;
    }

    protected String findPackagePath(FileObject fo) {
        FileObject pkgFO = fo.getParent();
        if (pkgFO == null) {
            return "";
        }
        String pkgName = pkgFO.getName();
        if (!"controllers".equals(pkgName) &&    //NOI18N
            !"domain".equals(pkgName) &&         //NOI18N
            !"views".equals(pkgName)) {          //NOI18N

            String parentPath = findPackagePath(pkgFO);
            if ("".equals(parentPath)) {
                return pkgName; // We don't want to add separator at the beginning
            } else {
                return parentPath + File.separator + pkgName;
            }
        } else {
            return ""; //NOI18N
        }
    }

    protected String getExtendedBaseDir(FileObject fo, String extension) {
        return getBaseDir(fo) + extension + File.separator;
    }

    /**
     * Finds out if the source component is valid. Those are validation criteria:
     *   1. Are we are dealing with a Grails Project?
     *   2. Are we called up from a groovy document?
     *   3. Is the target where it should be?
     *
     * @param sourceComponent
     * @return true, if the source component is valid, false otherwise
     */
    private boolean isValid(JTextComponent sourceComponent) {
        DataObject dob = getDataObjectFromComponent(sourceComponent);
        if (dob == null) {
            return false;
        }

        FileObject fo = dob.getPrimaryFile();
        if (fo == null) {
            return false;
        }

        GrailsProject project = getOwningProject(fo);
        if (project == null) {
            return false;
        }

        String mimetype = fo.getMIMEType();
        if (!(mimetype.equals(GroovyTokenId.GROOVY_MIME_TYPE) || mimetype.equals(GSP_MIME_TYPE))) {
            return false;
        }
        return true;
    }

    private DataObject getDataObjectFromComponent(JTextComponent sourceComponent) {
        if (sourceComponent == null) {
            return null;
        }

        Document doc = sourceComponent.getDocument();
        if (doc == null) {
            return null;
        }
        return NbEditorUtilities.getDataObject(doc);
    }

    private GrailsProject getOwningProject(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);

        if (project instanceof GrailsProject) {
            return (GrailsProject) project;
        }
        return null;
    }

    private String getBaseDir(FileObject fo) {
        return FileUtil.getFileDisplayName(getOwningProject(fo).getProjectDirectory()) + File.separator + "grails-app" + File.separator; //NOI18N
    }
}
