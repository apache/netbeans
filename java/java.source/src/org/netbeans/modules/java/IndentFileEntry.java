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

package org.netbeans.modules.java;

import java.io.*;
import java.nio.charset.Charset;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;
import org.netbeans.api.queries.FileEncodingQuery;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.text.IndentEngine;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  Svata
 * @version 1.0
 */
public abstract class IndentFileEntry extends FileEntry.Format {
    private static final String NEWLINE = "\n"; // NOI18N
    private static final String EA_PREFORMATTED = "org-netbeans-modules-java-preformattedSource"; // NOI18N

    private ThreadLocal indentEngine;
    
    /** Creates new JavaFileEntry */
    IndentFileEntry(MultiDataObject dobj, FileObject file) {
        super(dobj, file);
    }

    private EditorKit createEditorKit(String mimeType) {
        EditorKit kit;
        
        kit = JEditorPane.createEditorKitForContentType(mimeType);
        if (kit == null) {
            kit = new javax.swing.text.DefaultEditorKit();
        }
        return kit;
    }
    
    /* package private */ final void setIndentEngine(IndentEngine engine) {
        synchronized (this) {
            if (indentEngine == null)
                indentEngine = new ThreadLocal();
        }
        indentEngine.set(engine);
    }
    
    /* package private */ final void initializeIndentEngine() {
        StyledDocument doc = createDocument(createEditorKit(getFile().getMIMEType()));
        IndentEngine engine = IndentEngine.find(doc); // NOI18N
        setIndentEngine(engine);
    }

    private StyledDocument createDocument(EditorKit kit) {
        Document doc = kit.createDefaultDocument();
        if (doc instanceof StyledDocument) {
            return (StyledDocument)doc;
        } else {
            return new org.openide.text.FilterDocument(doc);
        }
    }
    
    /** Creates a new Java source from the template. Unlike the standard FileEntry.Format,
        this indents the resulting text using an indentation engine.
    */
    @Override
    public FileObject createFromTemplate (FileObject f, String name) throws IOException {
        String ext = getFile ().getExt ();

        if (name == null) {
            name = FileUtil.findFreeFileName(f, getFile ().getName (), ext);
        }
        FileObject fo = f.createData (name, ext);
        java.text.Format frm = createFormat (f, name, ext);
        InputStream is=getFile ().getInputStream ();
        Charset encoding = FileEncodingQuery.getEncoding(getFile());
        Reader reader = new InputStreamReader(is,encoding);
        BufferedReader r = new BufferedReader (reader);
        StyledDocument doc = createDocument(createEditorKit(fo.getMIMEType()));
        IndentEngine eng = (IndentEngine)indentEngine.get();
        if (eng == null) eng = IndentEngine.find(doc);
        Object attr = getFile().getAttribute(EA_PREFORMATTED);
        boolean preformatted = false;
        
        if (attr instanceof Boolean) {
            preformatted = ((Boolean)attr).booleanValue();
        }

        try {
            FileLock lock = fo.lock ();
            try {
                encoding = FileEncodingQuery.getEncoding(fo);
                OutputStream os=fo.getOutputStream(lock);
                OutputStreamWriter w = new OutputStreamWriter(os, encoding);
                try {
                    String line = null;
                    String current;
                    int offset = 0;

                    while ((current = r.readLine ()) != null) {
                        if (line != null) {
                            // newline between lines
                            doc.insertString(offset, NEWLINE, null);
                            offset++;
                        }
                        line = frm.format (current);

                        // partial indentation used only for pre-formatted sources
                        // see #19178 etc.
                        if (!preformatted || !line.equals(current)) {
                            line = fixupGuardedBlocks(safeIndent(eng, line, doc, offset));
                        }
                        doc.insertString(offset, line, null);
                            offset += line.length();
                    }
                    doc.insertString(doc.getLength(), NEWLINE, null);
                    w.write(doc.getText(0, doc.getLength()));
                } catch (javax.swing.text.BadLocationException e) {
                } finally {
                    w.close ();
                }
            } finally {
                lock.releaseLock ();
            }
        } finally {
            r.close ();
        }
        // copy attributes
        FileUtil.copyAttributes (getFile (), fo, (n, v) -> {
            return DataObject.PROP_TEMPLATE.equals(n) ? null : FileUtil.defaultAttributesTransformer().apply(n, v);
        });
        return fo;
    }
    
    /** The prefix of all magic strings */
    private static final String MAGIC_PREFIX = "//GEN-"; // NOI18N

    static String fixupGuardedBlocks(String indentedLine) {
        int offset = indentedLine.indexOf(MAGIC_PREFIX);
        if (offset == -1)
            return indentedLine;
        // move the guarded block at the end of the first line in the string
        int firstLineEnd = indentedLine.indexOf('\n'); // NOI18N
        if (firstLineEnd == -1 || firstLineEnd > offset)
            // already on the first line.
            return indentedLine;
        int guardedLineEnd = indentedLine.indexOf('\n', offset); // NOI18N
        StringBuffer sb = new StringBuffer(indentedLine.length());
        sb.append(indentedLine.substring(0, firstLineEnd));
        if (guardedLineEnd != -1) {
            sb.append(indentedLine.substring(offset, guardedLineEnd));
        } else {
            sb.append(indentedLine.substring(offset));
        }
        sb.append(indentedLine.substring(firstLineEnd, offset));
        if (guardedLineEnd != -1)
            sb.append(indentedLine.substring(guardedLineEnd));
        return sb.toString();
    }

    public static String safeIndent(IndentEngine engine, String text, StyledDocument doc, int offset) {
        if (engine == null)
            return text;
        try {
            StringWriter writer = new StringWriter();
            Writer indentator = engine.createWriter(doc, offset, writer);
            indentator.write(text);
            indentator.close();
            return writer.toString();
        } catch (Exception ex) {	    
            Exceptions.printStackTrace(Exceptions.attachMessage(ex,NbBundle.getMessage(IndentFileEntry.class, "EXMSG_IndentationEngineError")));
            return text;
        }
    }
}
