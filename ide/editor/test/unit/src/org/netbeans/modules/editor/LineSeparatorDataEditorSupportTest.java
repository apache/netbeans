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
