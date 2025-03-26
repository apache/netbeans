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

package org.netbeans.modules.web.jsf.palette;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.web.jsf.palette.items.PrefixResolver;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Libor Kotouc
 */
public final class JSFPaletteUtilities {

    private static final String SCRIPT_ENGINE_ATTR = "javax.script.ScriptEngine"; //NOI18N
    private static final String ENCODING_PROPERTY_NAME = "encoding"; //NOI18N

    private static ScriptEngineManager manager;

//    private static final String JSF_CORE_PREFIX = "f";  //NOI18N
//    private static final String JSF_CORE_URI = "http://java.sun.com/jsf/core";  //NOI18N
//    private static final String JSF_HTML_PREFIX = "h";  //NOI18N
//    private static final String JSF_HTML_URI = "http://java.sun.com/jsf/html";  //NOI18N
//
//    public static String findJsfCorePrefix(JTextComponent target) {
//        String res = getTagLibPrefix(target, JSF_CORE_URI);
//        if (res == null)
//            insertTagLibRef(target, JSF_CORE_PREFIX, JSF_CORE_URI);
//        return (res != null) ? res : JSF_CORE_PREFIX;
//    }
//
//    public static String findJsfHtmlPrefix(JTextComponent target) {
//        String res = getTagLibPrefix(target, JSF_HTML_URI);
//        if (res == null)
//            insertTagLibRef(target, JSF_HTML_PREFIX, JSF_HTML_URI);
//        return (res != null) ? res : JSF_HTML_PREFIX;
//    }
//
//    public static String getTagLibPrefix(JTextComponent target, String tagLibUri) {
//        FileObject fobj = getFileObject(target);
//        if (fobj != null) {
//            JspParserAPI.ParseResult result = JspContextInfo.getContextInfo(fobj).getCachedParseResult(fobj, false, true);
//            if (result != null && result.getPageInfo() != null) {
//                 for (TagLibraryInfo tli : result.getPageInfo().getTaglibs()) {
//                     if (tagLibUri.equals(tli.getURI()))
//                         return tli.getPrefixString();
//                 }
//            }
//        }
//        return null;
//    }
//
//    private static void insertTagLibRef(final JTextComponent target, final String prefix, final String uri) {
//        Document doc = target.getDocument();
//        if (doc != null && doc instanceof BaseDocument) {
//            final BaseDocument baseDoc = (BaseDocument) doc;
//            Runnable edit = new Runnable() {
//                public void run() {
//                    try {
//                        int pos = 0;  // FIXME: compute better where to insert tag lib definition?
//                        String definition = "<%@taglib prefix=\"" + prefix + "\" uri=\"" + uri + "\"%>\n";  //NOI18N
//
//                        //test for .jspx. FIXME: find better way to detect xml syntax?.
//                        FileObject fobj = getFileObject(target);
//                        if (fobj != null && "jspx".equals(fobj.getExt())) {
//                            int baseDocLength = baseDoc.getLength();
//                            String text = baseDoc.getText(0, baseDocLength);
//                            String jspRootBegin = "<jsp:root "; //NOI18N
//                            int jspRootIndex = text.indexOf(jspRootBegin);
//                            if (jspRootIndex != -1) {
//                                pos = jspRootIndex + jspRootBegin.length();
//                                definition = "xmlns:" + prefix + "=\"" + uri + "\" ";  //NOI18N
//                            }
//                        }
//
//                        baseDoc.insertString(pos, definition, null);
//                    } catch (BadLocationException e) {
//                        Exceptions.printStackTrace(e);
//                    }
//                }
//            };
//            baseDoc.runAtomic(edit);
//        }
//    }
//
    public static FileObject getFileObject(JTextComponent target) {
        BaseDocument doc = (BaseDocument) target.getDocument();
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile() : null;
        return fobj;
    }

    public static void insert(String s, final JTextComponent target) throws BadLocationException {
        Document doc = target.getDocument();
        if (doc instanceof BaseDocument) {
            final String str = (s == null) ? "" : s;

            final BaseDocument baseDoc = (BaseDocument) doc;
            final Reformat formatter = Reformat.get(baseDoc);
            Runnable edit = new Runnable() {
                public void run() {
                    try {
                        int start = insert(str, target, baseDoc);

                        // format the inserted text
                        if (start >= 0) {
                            int end = start + str.length();
                            formatter.reformat(start, end);

                        }
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            };

            formatter.lock();
            try {
                baseDoc.runAtomic(edit);
            }finally {
                formatter.unlock();
            }
        }
    }

    private static int insert(String s, JTextComponent target, Document doc) throws BadLocationException {
        int start = -1;
        try {
            //at first, find selected text range
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);

            //replace selected text by the inserted one
            start = caret.getDot();
            doc.insertString(start, s, null);
        } catch (BadLocationException ble) {
        }

        return start;
    }

    public static void expandJSFTemplate(FileObject template, Map<String, Object> values, FileObject target) throws IOException {
        Charset targetEncoding = FileEncodingQuery.getEncoding(target);
        Writer w = new OutputStreamWriter(target.getOutputStream(), targetEncoding);
        try {
            expandJSFTemplate(template, values, targetEncoding, w);
        } finally {
            w.close();
        }
        DataObject dob = DataObject.find(target);
        if (dob != null) {
            JSFPaletteUtilities.reformat(dob);
        }
    }

    public static void expandJSFTemplate(FileObject template, Map<String, Object> values, Charset targetEncoding, Writer w) throws IOException {
        Charset sourceEnc = FileEncodingQuery.getEncoding(template);
        ScriptEngine eng = getScriptEngine(template);
        Bindings bind = eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        bind.putAll(values);
        bind.put(ENCODING_PROPERTY_NAME, targetEncoding.name());

        Reader is = null;
        try {
            eng.getContext().setWriter(w);
            is = new InputStreamReader(template.getInputStream(), sourceEnc);
            eng.eval(is);
        } catch (ScriptException ex) {
            throw new IOException(ex);
        } finally {
            if (is != null) is.close();
        }
    }

    /**
     * Used core method for getting {@code ScriptEngine} from {@code
     * org.netbeans.modules.templates.ScriptingCreateFromTemplateHandler}.
     */
    protected static ScriptEngine getScriptEngine(FileObject fo) {
        Object obj = fo.getAttribute(SCRIPT_ENGINE_ATTR);
        // create.ftl, edit.ftl etc. templates doens't have stored any script engine
        if (obj == null) {
            obj = "freemarker"; //NOI18N
        }

        if (obj instanceof ScriptEngine) {
            return (ScriptEngine) obj;
        }
        if (obj instanceof String) {
            synchronized (JSFPaletteUtilities.class) {
                if (manager == null) {
                    ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                    try {
                        loader.loadClass(PrefixResolver.class.getName());
                    } catch (ClassNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    manager = Scripting.createManager();
                }
            }
            return manager.getEngineByName((String) obj);
        }
        return null;
    }

    public static void reformat(DataObject dob) {
        try {
            EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            if (ec == null) {
                return;
            }

            final StyledDocument doc = ec.openDocument();
            final Reformat reformat = Reformat.get(doc);

            reformat.lock();
            try {
                NbDocument.runAtomicAsUser(doc, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reformat.reformat(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                reformat.unlock();
                ec.saveDocument();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
