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
package org.netbeans.modules.testng.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author lukas
 */
@MimeRegistration(mimeType = "text/x-testng+xml", service = HyperlinkProvider.class, position = 1000)
public class TestNGSuiteHyperlingProvider implements HyperlinkProvider {

    private int startOffset;
    private int endOffset;
    private String file;
    private String method;
    private LinkType targetType;
    private static final Map<String, LinkType> linkMap = new ConcurrentHashMap<String, LinkType>();

    private enum LinkType {

        RESOURCE,
        JAVA_SOURCE;
    }

    static {
        linkMap.put("suite-file#path", LinkType.RESOURCE);
        linkMap.put("class#name", LinkType.JAVA_SOURCE);
        linkMap.put("listener#class-name", LinkType.JAVA_SOURCE);
        linkMap.put("selector-class#name", LinkType.JAVA_SOURCE);
        linkMap.put("include#name", LinkType.JAVA_SOURCE);
        linkMap.put("exclude#name", LinkType.JAVA_SOURCE);
//        linkMap.put("class/methods/include#name", LinkType.JAVA_SOURCE);
//        linkMap.put("class/methods/exclude#name", LinkType.JAVA_SOURCE);
    }

    public TestNGSuiteHyperlingProvider() {
    }

    public boolean isHyperlinkPoint(Document doc, int offset) {
        JTextComponent target = EditorRegistry.lastFocusedComponent();
        final StyledDocument styledDoc = (StyledDocument) target.getDocument();
        if (styledDoc == null) {
            return false;
        }

        // Work only with the open editor
        //and the editor has to be the active component:
        if ((target == null) || (target.getDocument() != doc)) {
            return false;
        }

        TokenHierarchy hi = TokenHierarchy.get(doc);
        TokenSequence<XMLTokenId> ts = hi.tokenSequence(XMLTokenId.language());
        ts.move(offset);
        ts.moveNext();
        Token<XMLTokenId> tok = ts.token();
        if (tok != null) {
            int tokOffset = ts.offset();
            Token<XMLTokenId> t = getAttribute(ts);
            if (t == null) {
                return false;
            }
            String xpath = "#" + t.text().toString();
            t = getParentElement(ts);
            xpath = getElementName(t) + xpath;

            targetType = linkMap.get(xpath);
            if (targetType == null) {
                return false;
            }

            if (xpath.startsWith("include") || xpath.startsWith("exclude")) {
                t = getParentElement(ts);
                if ("methods".equals(getElementName(t))) {
                    method = tok.text().toString();
                    getParentElement(ts); // <class ...> element
                    file = getAttributeValue(ts, "name");
                } else {
                    return false;
                }
            } else {
                method = null;
            }
            startOffset = tokOffset + 1;
            endOffset = startOffset + tok.text().length() - 2;
            if (method == null) {
                file = tok.text().subSequence(1, tok.text().length() - 1).toString();
            } else {
                method = method.substring(1, method.length() - 1);
            }
            return true;
        }
        return false;
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        JTextComponent target = EditorRegistry.lastFocusedComponent();
        final StyledDocument styledDoc = (StyledDocument) target.getDocument();
        if (styledDoc == null) {
            return null;
        }

        // Return the position, which was set in the isHyperlink method:
        return new int[]{startOffset, endOffset};
    }

    public void performClickAction(final Document doc, final int offset) {
        final AtomicBoolean cancel = new AtomicBoolean();
        ProgressUtils.runOffEventDispatchThread(new Runnable() {

            public void run() {
                performGoTo(doc, offset, file, method, targetType, cancel);
            }
        }, NbBundle.getMessage(TestNGSuiteHyperlingProvider.class, "LBL_GoToDeclaration"), cancel, false);
    }

    private Token<XMLTokenId> getAttribute(TokenSequence<XMLTokenId> ts) {
        Token<XMLTokenId> tok = ts.token();
        if (tok.id() == XMLTokenId.VALUE) {
            while (ts.movePrevious()) {
                tok = ts.token();
                switch (tok.id()) {
                    case ARGUMENT:
                        return tok;
                    case OPERATOR:
                    case EOL:
                    case ERROR:
                    case WS:
                        continue;
                    default:
                        return null;
                }
            }
        }
        return null;
    }

    private Token<XMLTokenId> getParentElement(TokenSequence<XMLTokenId> ts) {
        int depth = 0;
        while (ts.movePrevious()) {
            Token<XMLTokenId> prev = ts.token();
            switch (prev.id()) {
                case TAG:
                    if (prev.text().length() == 1) {
                        continue;
                    }
                    if (prev.text().toString().contains("/")) {
                        depth++;
                        continue;
                    }
                    depth--;
                    if (depth == -1) {
                        return prev;
                    }
                    continue;
                default:
                    continue;
            }
        }
        return null;
    }

    private String getElementName(Token<XMLTokenId> tok) {
        return tok.text().toString().substring(1);
    }

    private String getAttributeValue(TokenSequence<XMLTokenId> ts, String name) {
        boolean readValue = false;
        while (ts.moveNext()) {
            Token<XMLTokenId> next = ts.token();

            switch (next.id()) {
                case ARGUMENT:
                    if (name.equals(next.text().toString())) {
                        readValue = true;
                    }
                    continue;
                case VALUE:
                    if (readValue) {
                        CharSequence val = next.text().subSequence(1, next.text().length() - 1);
                        return val.toString();
                    }
                    continue;
                case OPERATOR:
                case EOL:
                case ERROR:
                case WS:
                    continue;
                default:
                    return null;
            }
        }
        return null;
    }

    private void performGoTo(Document doc, int offset, final String file, final String method, final LinkType type, AtomicBoolean cancel) {
        switch (type) {
            case RESOURCE:
                FileObject fo = NbEditorUtilities.getFileObject(doc);
                File target = new File(file);
                FileObject targetFO;
                if (target.isAbsolute()) {
                    targetFO = FileUtil.toFileObject(FileUtil.normalizeFile(target));
                } else {
                    targetFO = fo.getParent().getFileObject(file);
                }
                if (targetFO.isData() && targetFO.isValid() && !targetFO.isVirtual()) {
                    openInEditor(targetFO);
                } else {
                    String key = "goto_source_source_not_found"; //NOI18N
                    String msg = NbBundle.getMessage(TestNGSuiteHyperlingProvider.class, key);
                    StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object[]{file}));
                }
                break;
            case JAVA_SOURCE:
                final ClasspathInfo cp = ClasspathInfo.create(doc);
                JavaSource js = JavaSource.create(cp, Collections.<FileObject>emptyList());
                try {
                    js.runUserActionTask(new Task<CompilationController>() {

                        @Override
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element element = cc.getElements().getTypeElement(file.trim());
                            if (element != null) {
                                if (method != null) {
                                    List<? extends Element> enclosedElements = cc.getElements().getAllMembers((TypeElement) element);
                                    for (Element e : enclosedElements) {
                                        if (e.getKind() == ElementKind.METHOD) {
                                            if (e.getSimpleName().toString().equals(method)) {
                                                element = e;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!ElementOpen.open(cp, element)) {
                                    String key = "goto_source_source_not_found"; //NOI18N
                                    String msg = NbBundle.getMessage(TestNGSuiteHyperlingProvider.class, key);
                                    StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object[]{file}));
                                }
                            }
                        }
                    }, false);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                break;
        }
    }

    private void openInEditor(FileObject fo) {
        DataObject dobj;
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException e) {
            Exceptions.printStackTrace(e);
            return;
        }
        if (dobj != null) {
            Node.Cookie cookie = dobj.getLookup().lookup(OpenCookie.class);
            if (cookie != null) {
                ((OpenCookie) cookie).open();
            }
        }
    }
}
