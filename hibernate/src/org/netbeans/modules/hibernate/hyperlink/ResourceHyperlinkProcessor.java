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
package org.netbeans.modules.hibernate.hyperlink;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM), Dongmei Cao
 */
public class ResourceHyperlinkProcessor extends HyperlinkProcessor {

    public ResourceHyperlinkProcessor() {
    }

    public void process(HyperlinkEnv env) {
        FileObject fo = NbEditorUtilities.getFileObject(env.getDocument());
        if (fo == null) {
            return;
        }
        FileObject parent = fo.getParent();

        if (!openFile(parent.getFileObject(env.getValueString()))) {
            String message = NbBundle.getMessage(ResourceHyperlinkProcessor.class, "LBL_ResourceNotFound", env.getValueString());
            StatusDisplayer.getDefault().setStatusText(message);
        }
    }

    private boolean openFile(FileObject file) {
        if (file == null) {
            return false;
        }
        DataObject dObj;
        try {
            dObj = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            return false;
        }
        EditorCookie editorCookie = dObj.getCookie(EditorCookie.class);
        if (editorCookie == null) {
            return false;
        }
        editorCookie.open();
        return true;
    }
}
