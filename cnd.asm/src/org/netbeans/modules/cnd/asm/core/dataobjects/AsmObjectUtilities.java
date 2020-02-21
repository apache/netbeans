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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
