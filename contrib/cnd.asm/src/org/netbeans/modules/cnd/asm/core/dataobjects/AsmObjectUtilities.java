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
package org.netbeans.modules.cnd.asm.core.dataobjects;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.queries.FileEncodingQuery;

import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.windows.TopComponent;

import org.netbeans.modules.cnd.asm.model.AsmModel;
import org.netbeans.modules.cnd.asm.model.AsmModelAccessor;

public class AsmObjectUtilities {

    private static final Logger LOGGER =
            Logger.getLogger(AsmObjectUtilities.class.getName());

    public static Document getDocument(DataObject dob) {

        EditorCookie editorCookie = dob.getCookie(EditorCookie.class);
        if (editorCookie == null) {
            LOGGER.log(Level.INFO, "Can't determine document"); // NOI18N
            return null;
        }

        return editorCookie.getDocument();
    }

    public static Document getDocument(JTextComponent pane) {
        return pane.getDocument();
    }

    public static AsmModel getModel(DataObject dob) {
        Document doc = getDocument(dob);

        if (doc == null) {
            LOGGER.log(Level.INFO, "Can't determine model for " + dob); // NOI18N
            return null;
        }

        return (AsmModel) doc.getProperty(AsmModel.class);
    }

    public static AsmModelAccessor getAccessor(DataObject dob) {
        Document doc = getDocument(dob);
        if (doc == null) {
            return null;
        }

        return getAccessor(doc);
    }

    public static AsmModelAccessor getAccessor(Document doc) {
        return (AsmModelAccessor) doc.getProperty(AsmModelAccessor.class);
    }

    public static AsmModelAccessor getAccessor(JTextComponent pane) {
        Document doc = getDocument(pane);
        return getAccessor(doc);
    }

    public static String getText(final Document doc) {
        final String[] text = new String[1];

        doc.render(new Runnable() {

            public void run() {
                try {
                    text[0] = doc.getLength() == 0 ? "" : doc.getText(0, doc.getLength() - 1);
                } catch (BadLocationException ex) {
                    text[0] = "";
                    LOGGER.log(Level.INFO, "Impossible error with getText()"); // NOI18N
                }
            }
        });

        return text[0];
    }

    public static String getText(FileObject fo) {
        if (fo == null) {
            return "";
        }
        InputStream is = null;

        try {
            is = new BufferedInputStream(fo.getInputStream());
            Reader reader;

            reader = new InputStreamReader(is, getEncoding(fo));

            return new String(readContents(reader));
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Can't load FileObject text " + // NOI18N
                    ex.getMessage());
            return "";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Can't load FileObject text " + // NOI18N
                        ex.getMessage());
                return "";
            }
        }
    }

    private static Charset getEncoding(FileObject fo) {
        Charset cs = null;
        if (fo != null && fo.isValid()) {
            cs = FileEncodingQuery.getEncoding(fo);
        }
        if (cs == null) {
            cs = FileEncodingQuery.getDefaultEncoding();
        }
        return cs;
    }

    public static char[] readContents(Reader r) throws IOException {

        final int READ_BY = 1024;

        List<char[]> list = new LinkedList<char[]>();

        int count = 0;
        int wasRead = 0;

        do {
            char[] buf = new char[READ_BY];
            int offset = 0;

            wasRead = r.read(buf, offset, buf.length);
            if (wasRead == -1) {
                break;
            }

            offset += wasRead;

            if (offset > 0) {
                list.add(buf);
            }

            count += offset;

        } while (wasRead >= 0);
        r.close();

        char[] res = new char[count];
        Iterator<char[]> it = list.iterator();
        int offset = 0;

        while (it.hasNext()) {
            char[] buf = it.next();
            int size = (it.hasNext()) ? buf.length : count - offset;
            System.arraycopy(buf, 0, res, offset, size);
            offset += size;
        }

        return res;
    }

    public static void goToSource(DataObject ob, int offset) {

        if (!openFileInEditor(ob)) {
            return;
        }

        EditorCookie ed = ob.getCookie(org.openide.cookies.EditorCookie.class);

        if (ed != null) {
            try {
                ed.openDocument();
            } catch (IOException ex) {
                return;
            }

            JEditorPane pane = ed.getOpenedPanes()[0];
            Document doc = pane.getDocument();
            if (doc != null && offset >= 0 && offset < doc.getLength()) {
                pane.setCaretPosition(offset);
            }

            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class,
                    pane);
            if (tc != null) {
                tc.requestActive();
            }
        }
    }

    public static boolean openFileInEditor(DataObject ob) {

        EditCookie ck = ob.getLookup().lookup(EditCookie.class);
        if (ck != null) {
            ck.edit();
            return true;
        }
        OpenCookie oc = ob.getLookup().lookup(OpenCookie.class);
        if (oc != null) {
            oc.open();
            return true;
        }
        return false;
    }
}
