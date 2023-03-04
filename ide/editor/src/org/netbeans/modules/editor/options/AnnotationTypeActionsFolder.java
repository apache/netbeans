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
