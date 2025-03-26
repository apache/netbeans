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

package org.netbeans.modules.web.core.palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.api.JspContextInfo;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Libor Kotouc
 */
public final class JspPaletteUtilities {

    public static final String CARET = "&CARET&";// NOI18N
    private static final String JSTL_PREFIX = "c";  //NOI18N
    private static final String JSTL_URI = "http://java.sun.com/jsp/jstl/core";  //NOI18N
    private static final String SQL_PREFIX = "sql";  //NOI18N
    private static final String SQL_URI = "http://java.sun.com/jsp/jstl/sql";  //NOI18N

    public static void insert(String s, JTextComponent target) throws BadLocationException {
        insert(s, target, true);
    }

    public static void insert(String s, JTextComponent target, boolean reformat) throws BadLocationException {
        Document _doc = target.getDocument();
        if (!(_doc instanceof BaseDocument)) {
            return;
        }

        //check whether we are not in a scriptlet
//        JspSyntaxSupport sup = (JspSyntaxSupport)(doc.getSyntaxSupport().get(JspSyntaxSupport.class));
//        int start = target.getCaret().getDot();
//        TokenItem token = sup.getTokenChain(start, start + 1);
//        if (token != null && token.getTokenContextPath().contains(JavaTokenContext.contextPath)) // we are in a scriptlet
//            return false;
        if (s == null) {
            s = "";  //NOI18N
        }
        BaseDocument doc = (BaseDocument) _doc;
        Indent indent = Indent.get(doc);
        indent.lock();
        try {
            doc.atomicLock();
            try {
                int cursor_offset = s.indexOf(CARET);
                if (cursor_offset != -1) {
                    s = s.replace(CARET, "");  //NOI18N
                }
                int start = insert(s, target, _doc);
                if (cursor_offset != -1) {
                    target.setCaretPosition(start + cursor_offset);
                }
                if (reformat && start >= 0 && _doc instanceof BaseDocument) {
                    // format the inserted text
                    int end = start + s.length();
                    indent.reindent(start, end);
                }
            } finally {
                doc.atomicUnlock();
            }
        } finally {
            indent.unlock();
        }
    }

    private static FileObject getFileObject(JTextComponent target) {
        BaseDocument doc = (BaseDocument) target.getDocument();
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile() : null;
        return fobj;
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
            Exceptions.printStackTrace(ble);
        }

        return start;
    }

    public static PageInfo.BeanData[] getAllBeans(JTextComponent target) {
        FileObject fobj = getFileObject(target);
        if (fobj != null) {
            JspParserAPI.ParseResult result = JspContextInfo.getContextInfo(fobj).getCachedParseResult(fobj, false, true);
            if (result != null && result.getPageInfo() != null) {
                return result.getPageInfo().getBeans();
            }
        }
        return null;
    }

    public static boolean isJakartaVariant(JTextComponent target) {
        FileObject fobj = getFileObject(target);
        if(fobj != null) {
            ClassPath cp = ClassPath.getClassPath(fobj, ClassPath.COMPILE);
            return cp != null && cp.findResource("jakarta/servlet/http/HttpServletRequest.class") != null;
        }
        return false;
    }

    public static boolean idExists(String id, PageInfo.BeanData[] beanData) {
        boolean res = false;
        if (id != null && beanData != null) {
            for (int i = 0; i < beanData.length; i++) {
                PageInfo.BeanData beanData1 = beanData[i];
                if (beanData1.getId().equals(id)) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    public static boolean typeExists(JTextComponent target, final String fqcn) {
        final boolean[] result = {false};
        if (fqcn != null) {
            runUserActionTask(target, (CompilationController parameter) -> {
                result[0] = parameter.getElements().getTypeElement(fqcn) != null;
            });
        }
        return result[0];
    }

    private static void runUserActionTask(JTextComponent target, Task<CompilationController> aTask) {
        FileObject fobj = getFileObject(target);
        if (fobj == null) {
            return;
        }
        try {
            JavaSource src = JavaSource.create(ClasspathInfo.create(fobj));
            if (src != null)
                src.runUserActionTask(aTask, false);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public static List<String> getTypeProperties(JTextComponent target, final String fqcn, final String[] prefix) {
        final List<String> result = new ArrayList<>();
        if (prefix != null) {
            runUserActionTask(target, new Task<CompilationController>() {

                @Override
                public void run(CompilationController parameter) throws Exception {
                    TypeElement te = parameter.getElements().getTypeElement(fqcn);
                    if (te != null) {
                        List<ExecutableElement> list = ElementFilter.methodsIn(te.getEnclosedElements());
                        for (Iterator<ExecutableElement> it = list.iterator(); it.hasNext();) {
                            ExecutableElement executableElement = it.next();
                            String methodName = executableElement.getSimpleName().toString();
                            if (executableElement.getModifiers().contains(Modifier.PUBLIC) && match(methodName, prefix)) {
                                String propName = extractPropName(methodName, prefix);
                                if (propName != null) {
                                    result.add(propName);
                                }
                            }
                        }
                    }
                }

                private String extractPropName(String methodName, String[] prefix) {
                    for (int i = 0; i < prefix.length; i++) {
                        String string = prefix[i];
                        if (methodName.startsWith(string)) {
                            // only first character to lower case
                            String res = methodName.substring(string.length());
                            if (res.length() > 0) {
                                return Character.toLowerCase(res.charAt(0)) +
                                        res.substring(1);
                            }
                        }
                    }
                    return null;
                }

                private boolean match(String methodName, String[] prefix) {
                    boolean res = false;
                    for (int i = 0; i < prefix.length; i++) {
                        String string = prefix[i];
                        if (methodName.startsWith(string)) {
                            res = true;
                            break;
                        }
                    }
                    return res;
                }
            });
        }
        return result;
    }

    /**************************************************************************/
    public static String getTagLibPrefix(JTextComponent target, String tagLibUri) {
        FileObject fobj = getFileObject(target);
        if (fobj != null) {
            JspParserAPI.ParseResult result = JspContextInfo.getContextInfo(fobj).getCachedParseResult(fobj, false, true);
            if (result != null && result.getPageInfo() != null) {
                 for (TagLibraryInfo tli : result.getPageInfo().getTaglibs()) {
                     if (tagLibUri.equals(tli.getURI()))
                         return tli.getPrefixString();
                 }
            }
        }
        return null;
    }

    /**************************************************************************/
    public static String findJstlPrefix(JTextComponent target) {
        String res = getTagLibPrefix(target, JSTL_URI);
        if (res == null)
            insertTagLibRef(target, JSTL_PREFIX, JSTL_URI);
        return (res != null) ? res : JSTL_PREFIX;
    }

    /**************************************************************************/
    public static String findSqlPrefix(JTextComponent target) {
        String res = getTagLibPrefix(target, SQL_URI);
        if (res == null)
            insertTagLibRef(target, SQL_PREFIX, SQL_URI);
        return (res != null) ? res : SQL_PREFIX;
    }

    /**************************************************************************/
    private static void insertTagLibRef(JTextComponent target, String prefix, String uri) {
        Document doc = target.getDocument();
        if (doc instanceof BaseDocument) {
            BaseDocument baseDoc = (BaseDocument)doc;
            baseDoc.runAtomic(() -> {
                try {
                    int pos = 0;  // FIXME: compute better where to insert tag lib definition?
                    String definition = "<%@taglib prefix=\"" + prefix + "\" uri=\"" + uri + "\"%>\n";  //NOI18N

                    //test for .jspx. FIXME: find better way to detect xml syntax?.
                    FileObject fobj = getFileObject(target);
                    if (fobj != null && "jspx".equals(fobj.getExt())) {
                        int baseDocLength = baseDoc.getLength();
                        String text = baseDoc.getText(0, baseDocLength);
                        String jspRootBegin = "<jsp:root "; //NOI18N
                        int jspRootIndex = text.indexOf(jspRootBegin);
                        if (jspRootIndex != -1) {
                            pos = jspRootIndex + jspRootBegin.length();
                            definition = "xmlns:" + prefix + "=\"" + uri + "\" ";  //NOI18N
                        }
                    }

                    doc.insertString(pos, definition, null);
                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }
            });
        }
    }

}
