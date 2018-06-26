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
