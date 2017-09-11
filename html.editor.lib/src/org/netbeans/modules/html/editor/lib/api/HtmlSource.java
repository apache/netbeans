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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.lib.api;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public final class HtmlSource {

    private CharSequence sourceCode;
    private Snapshot snapshot;
    private FileObject sourceFileObject;

    public HtmlSource(CharSequence sourceCode) {
        this.sourceCode = sourceCode;
    }

    public HtmlSource(FileObject sourceFileObject) {
        this.sourceFileObject = sourceFileObject;
    }

    public HtmlSource(Snapshot snapshot) {
        this.snapshot = snapshot;
    }
    
    public HtmlSource(CharSequence sourceCode, Snapshot snapshot, FileObject sourceFileObject) {
        this.sourceCode = sourceCode;
        this.snapshot = snapshot;
        this.sourceFileObject = sourceFileObject;
    }

    public synchronized CharSequence getSourceCode() {
        if(sourceCode == null) {
            if(snapshot != null) {
                loadContentFromSnapshot();
            } else if(sourceFileObject != null) {
                loadContentFromFileObject();
            }
        }
        return sourceCode;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public synchronized FileObject getSourceFileObject() {
        if (sourceFileObject == null && snapshot != null) {
            //try to obtain the FileObject from the snapshot if not explicitly provided
            sourceFileObject = snapshot.getSource().getFileObject();
        }
        return sourceFileObject;
    }

    private void loadContentFromFileObject() {
        try {
            DataObject dobj = DataObject.find(sourceFileObject);
            if (dobj != null) {
                EditorCookie cake = dobj.getCookie(EditorCookie.class);
                final Document doc = cake.openDocument();
                doc.render(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sourceCode = doc.getText(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void loadContentFromSnapshot() {
        sourceCode = snapshot.getText(); //immutable CharSequence
    }
}
