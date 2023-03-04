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
package org.netbeans.modules.php.project.connections.sync.diff;

import java.io.File;
import java.io.IOException;
import javax.swing.text.Document;
import org.netbeans.modules.php.project.connections.TmpLocalFile;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.Lookups;

/**
 * Editable {@link TmpLocalFileStreamSource}.
 */
public class EditableTmpLocalFileStreamSource extends TmpLocalFileStreamSource {

    private final EditorCookie editorCookie;
    private final Document document;


    public EditableTmpLocalFileStreamSource(String name, TmpLocalFile tmpFile, String mimeType, String charsetName, boolean remote) throws IOException {
        super(name, tmpFile, mimeType, charsetName, remote);
        editorCookie = getEditorCookie(tmpFile);
        document = getDocument();
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public Lookup getLookup() {
        if (document == null) {
            return super.getLookup();
        }
        return Lookups.fixed(document);
    }

    @NbBundle.Messages("EditableTmpLocalFileStreamSource.open.confirm=File is too big. Do you really want to open it?")
    private Document getDocument() throws IOException {
        if (editorCookie == null) {
            return null;
        }
        try {
            return editorCookie.openDocument();
        } catch (UserQuestionException uqe) {
            NotifyDescriptor.Confirmation desc = new NotifyDescriptor.Confirmation(uqe.getLocalizedMessage(),
                    Bundle.EditableTmpLocalFileStreamSource_open_confirm(), NotifyDescriptor.Confirmation.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION)) {
                uqe.confirmed();
                return editorCookie.openDocument();
            }
        }
        return null;
    }

    public boolean save() throws IOException {
        if (editorCookie == null) {
            return false;
        }
        if (editorCookie.isModified()) {
            editorCookie.saveDocument();
            return true;
        }
        return false;
    }

    private EditorCookie getEditorCookie(TmpLocalFile tmpFile) throws IOException {
        FileObject fileObject = FileUtil.toFileObject(new File(tmpFile.getAbsolutePath()));
        assert fileObject != null : "Fileobject for tmp local file not found: " + tmpFile;
        DataObject dataObject = DataObject.find(fileObject);
        return dataObject.getLookup().lookup(EditorCookie.class);
    }

}
