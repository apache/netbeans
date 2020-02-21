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

package org.netbeans.modules.cnd.editor.api;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.editor.indent.CppIndentTask;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 */
public final class FormattingSupport {

    private FormattingSupport() {
    }

    public static String getLineIndentation(Document doc, int caretOffset) {
        int indent = new CppIndentTask(doc).getLineIndentation(caretOffset);
        StringBuilder sb = new StringBuilder(indent);
        for (int i = 0; i < indent; i++) {
            sb.append(' '); // NOI18N
        }
        return sb.toString();
    }

    public static CharSequence getFormattedText(Document doc, int caretOffset, CharSequence textToFormat) {
        if (doc == null) {
            System.err.println("original document is not specified for getFormattedText");
            return textToFormat;
        }
        String mimeType = DocumentUtilities.getMimeType(doc);
        if (!MIMENames.isHeaderOrCppOrC(mimeType)) {
            System.err.println("Unsupported MIME type of document " + doc);
            return textToFormat;
        }
        String ext = MIMEExtensions.get(mimeType).getDefaultExtension();
        try {
            FileSystem fs = FileUtil.createMemoryFileSystem();
            FileObject root = fs.getRoot();
            String fileName = FileUtil.findFreeFileName(root, "cnd-format", ext);// NOI18N
            FileObject data = FileUtil.createData(root, fileName + "." + ext);// NOI18N
            Writer writer = new OutputStreamWriter(data.getOutputStream(), "UTF8");// NOI18N
            try {
                writer.append(textToFormat);
                writer.flush();
            } finally {
                writer.close();
            }
            DataObject dob = DataObject.find(data);
            EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                final StyledDocument fmtDoc = ec.openDocument();
                final Reformat fmt = Reformat.get(fmtDoc);
                fmt.lock();
                try {
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                fmt.reformat(0, fmtDoc.getLength());
                            } catch (BadLocationException ex) {
                            }
                        }
                    };
                    if (fmtDoc instanceof BaseDocument) {
                        ((BaseDocument)fmtDoc).runAtomic(runnable);
                    } else {
                        runnable.run();
                    }
                } finally {
                    fmt.unlock();
                }
                SaveCookie save = dob.getLookup().lookup(SaveCookie.class);
                if (save != null) {
                    save.save();
                }
                final String text = fmtDoc.getText(0, fmtDoc.getLength());
                StringBuilder declText = new StringBuilder();
                final int len = text.length();
                int start = 0;
                int end = len - 1;
                // skip all whitespaces in the beginning and end of formatted text
                for (; start < len && Character.isWhitespace(text.charAt(start)); start++) {
                }
                for (; end > start && Character.isWhitespace(text.charAt(end)); end--) {
                }
                String indent = getLineIndentation(doc, caretOffset);
                // start with indented line
                declText.append(indent);// NOI18N
                for (int i = start; i <= end; i++) {
                    final char charAt = text.charAt(i);
                    if (charAt == '\n') { // NOI18N
                        // indented new line if not the last
                        if (i <= end) {
                            declText.append(charAt);
                            declText.append(indent);
                        }
                    } else {
                        declText.append(charAt);
                    }
                }
                return declText.toString();
            }
            data.delete();
        } catch (BadLocationException ex) {
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return textToFormat;
    }

    /**
     * Indent text fragment inserted into document in offset.
     * Indented lines from caretOffset+1 to end of inserted text.
     * 
     * @param doc
     * @param caretOffset
     * @param textToFormat should be started with new line
     * @return return indented text.
     */
    public static CharSequence getIndentedText(Document doc, final int caretOffset, CharSequence textToFormat) {
        if (doc == null) {
            System.err.println("original document is not specified for getFormattedText");
            return textToFormat;
        }
        String mimeType = DocumentUtilities.getMimeType(doc);
        if (!MIMENames.isHeaderOrCppOrC(mimeType)) {
            System.err.println("Unsupported MIME type of document " + doc);
            return textToFormat;
        }
        String ext = MIMEExtensions.get(mimeType).getDefaultExtension();
        try {
            FileSystem fs = FileUtil.createMemoryFileSystem();
            FileObject root = fs.getRoot();
            String fileName = FileUtil.findFreeFileName(root, "cnd-format", ext);// NOI18N
            FileObject data = FileUtil.createData(root, fileName + "." + ext);// NOI18N
            Writer writer = new OutputStreamWriter(data.getOutputStream(), "UTF8");// NOI18N
            try {
                writer.append(doc.getText(0, caretOffset));
                writer.append(textToFormat);
                writer.flush();
            } finally {
                writer.close();
            }
            DataObject dob = DataObject.find(data);
            EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                final StyledDocument fmtDoc = ec.openDocument();
                final Indent fmt = Indent.get(fmtDoc);
                fmt.lock();
                try {
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                fmt.reindent(caretOffset+1, fmtDoc.getLength());
                            } catch (BadLocationException ex) {
                            }
                        }
                    };
                    if (fmtDoc instanceof BaseDocument) {
                        ((BaseDocument)fmtDoc).runAtomic(runnable);
                    } else {
                        runnable.run();
                    }
                } finally {
                    fmt.unlock();
                }
                SaveCookie save = dob.getLookup().lookup(SaveCookie.class);
                if (save != null) {
                    save.save();
                }
                return fmtDoc.getText(caretOffset, fmtDoc.getLength()-caretOffset);
            }
            data.delete();
        } catch (BadLocationException ex) {
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return textToFormat;
    }

}
