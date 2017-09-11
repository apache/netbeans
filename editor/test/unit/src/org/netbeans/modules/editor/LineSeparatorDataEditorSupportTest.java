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
package org.netbeans.modules.editor;

import java.io.File;
import java.io.InputStream;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Alexander Simon
 */
public class LineSeparatorDataEditorSupportTest extends NbTestCase {
    private MimePath textMimePath;

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    public LineSeparatorDataEditorSupportTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(new Class[]{MockMimeLookup.class});
        textMimePath = MimePath.parse("text/plain");
        MockMimeLookup.setInstances(textMimePath, new NbEditorKit(), NbPreferences.forModule(getClass()));
    }
    
    public void testLineSeparator() throws Exception {
        File file = File.createTempFile("lineSeparator", ".txt", getWorkDir());
        file.deleteOnExit();
        FileObject fileObject = FileUtil.toFileObject(file);
        fileObject.setAttribute(FileObject.DEFAULT_LINE_SEPARATOR_ATTR, "\r");
        DataObject dataObject = DataObject.find(fileObject);
        EditorCookie editor = dataObject.getLookup().lookup(org.openide.cookies.EditorCookie.class);
        final StyledDocument doc = editor.openDocument();
        SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                try {
                    doc.insertString(doc.getLength(), ".\n", null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
        editor.saveDocument();
        InputStream inputStream = fileObject.getInputStream();
        assertEquals('.',inputStream.read());
        assertEquals('\r',inputStream.read());
        inputStream.close();
    }
}
