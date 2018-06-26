/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
