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

package org.netbeans.modules.editor.options;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.loaders.FolderInstance;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.netbeans.editor.AnnotationType;
import java.util.LinkedList;
import javax.swing.Action;
import org.openide.filesystems.FileUtil;

/** Processing of folders with annotation types actions.
 *
 * @author  David Konecny
 * @since 08/2001
 */
public class AnnotationTypeActionsFolder extends FolderInstance{
    
    /** root folder for annotation type actions subfolders */
    private static final String FOLDER = "Editors/AnnotationTypes/"; // NOI18N
    
    private AnnotationType type;
    
    /** Creates new AnnotationTypesFolder */
    private AnnotationTypeActionsFolder(AnnotationType type, DataFolder fld) {
        super(fld);
        this.type = type;
        recreate();
        instanceFinished();
    }

    /** Factory method for AnnotationTypeActionsFolder instance. */
    public static boolean readActions(AnnotationType type, String subFolder) {

        FileObject f = FileUtil.getConfigFile(FOLDER + subFolder);
        if (f == null) {
            return false;
        }
        
        try {
            DataObject d = DataObject.find(f);
            DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
            if (df != null) {
                AnnotationTypeActionsFolder folder;
                folder = new AnnotationTypeActionsFolder(type, df);
                return true;
            }
        } catch (org.openide.loaders.DataObjectNotFoundException ex) {
            Logger.getLogger("global").log(Level.INFO,null, ex);
            return false;
        }
        return false;
    }

    /** Called for each XML file found in FOLDER directory */
    protected Object createInstance(InstanceCookie[] cookies) throws java.io.IOException, ClassNotFoundException {
        LinkedList annotationActions = new LinkedList();

        for (int i = 0; i < cookies.length; i++) {
            if (isAction(cookies[i])) {
                Action action = (Action) cookies[i].instanceCreate();
                annotationActions.add(action);
            }
        }
        
        // set all these types to AnnotationType static member
        type.setActions((Action[])annotationActions.toArray(new Action[0]));

        return null;
    }

    private static boolean isAction(InstanceCookie ic) {
        if (ic instanceof InstanceCookie.Of) {
            return ((InstanceCookie.Of) ic).instanceOf(Action.class);
        } else {
            return Action.class.isAssignableFrom(ic.getClass());
        }
    }
}
