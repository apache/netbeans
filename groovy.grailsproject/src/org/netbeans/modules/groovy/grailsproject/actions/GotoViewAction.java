/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

import java.io.File;
import static org.netbeans.modules.groovy.grailsproject.actions.Bundle.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

@Messages("CTL_GotoViewAction=Go to Grails Vi&ew")
@ActionID(id = "org.netbeans.modules.groovy.grailsproject.actions.GotoViewAction", category = "Groovy")
@ActionRegistration(lazy = false, displayName = "#CTL_GotoViewAction")
@ActionReferences(value = {
    @ActionReference(path = "Menu/GoTo", position = 575),
    @ActionReference(path = "Editors/text/x-groovy/Popup/goto", position = 100),
    @ActionReference(path = "Editors/text/x-gsp/Popup/goto", position = 100)
})
public final class GotoViewAction extends GotoBaseAction {

    public GotoViewAction() {
        super(CTL_GotoViewAction()); // NOI18N
    }

    @Override
    protected FileObject getTargetFO(String fileName, FileObject sourceFO) {
        File targetFile = new File(getTargetFilePath(fileName, sourceFO));
        FileObject targetFO = FileUtil.toFileObject(FileUtil.normalizeFile(targetFile));

        // do not navigate to itself
        if (sourceFO.equals(targetFO)) {
            return null;
        }

        return targetFO;
    }

    @Override
    protected String getTargetFilePath(String filename, FileObject sourceFO) {
        // this needs to be done if we are moving from controller
        if (filename.endsWith("Controller")) {
            filename = filename.replaceAll("Controller$", "");
        }
        if (filename.length() > 1) {
            filename = Character.toLowerCase(filename.charAt(0)) + filename.substring(1);
        } else {
            filename = filename.toLowerCase();
        }
        return getExtendedBaseDir(sourceFO, "views") + filename + File.separator + "show.gsp"; //NOI18N
    }
}
