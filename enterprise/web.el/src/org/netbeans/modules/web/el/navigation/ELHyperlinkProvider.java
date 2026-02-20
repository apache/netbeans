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
package org.netbeans.modules.web.el.navigation;

import com.sun.el.parser.AstDotSuffix;
import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstMethodArguments;
import com.sun.el.parser.AstString;
import com.sun.el.parser.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELLanguage;
import org.netbeans.modules.web.el.ELParserResult;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.ResourceBundles;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Go to declaration for Expression Language.
 *
 * @author Erno Mononen
 */
public final class ELHyperlinkProvider implements HyperlinkProviderExt {

    @Override
    public boolean isHyperlinkPoint(final Document doc, final int offset, HyperlinkType type) {
        final AtomicBoolean ret = new AtomicBoolean(false);
        doc.render(new Runnable() {

            @Override
            public void run() {
                ret.set(getELIdentifierSpan(doc, offset) != null);
            }
        });

        return ret.get();
    }

    @Override
    public int[] getHyperlinkSpan(final Document doc, final int offset, HyperlinkType type) {
        final AtomicReference<int[]> ret = new AtomicReference<>();
        doc.render(new Runnable() {

            @Override
            public void run() {
                ret.set(getELIdentifierSpan(doc, offset));
            }
        });
        return ret.get();
    }

    @Override
    public void performClickAction(final Document doc, final int offset, HyperlinkType type) {
        final AtomicBoolean cancel = new AtomicBoolean();
        BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                performGoTo(doc, offset, cancel);
            }
        }, NbBundle.getMessage(ELHyperlinkProvider.class, "LBL_GoToDeclaration"), cancel, false);
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        Pair<Node, ELElement> nodeAndElement = resolveNodeAndElement(doc, offset, new AtomicBoolean());
        if (nodeAndElement != null) {
            if (nodeAndElement.first() instanceof AstString) {
                // could be a resource bundle key
                return getTooltipTextForBundleKey(nodeAndElement);
            } else {
                return getTooltipTextForElement(nodeAndElement);
            }
        }
        return null;
    }

    private String getTooltipTextForElement(final Pair<Node, ELElement> pair) {
        final String[] result = new String[1];
        final FileObject file = pair.second().getSnapshot().getSource().getFileObject();
        ClasspathInfo cp = ELTypeUtilities.getElimplExtendedCPI(file);
        try {
            JavaSource.create(cp, file).runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    final Element resolvedElement = ELTypeUtilities.resolveElement(CompilationContext.create(file, cc), pair.second(), pair.first());
                    if (resolvedElement == null) {
                        return;
                    }
                    DisplayNameElementVisitor dnev = new DisplayNameElementVisitor(cc);
                    dnev.visit(resolvedElement, Boolean.TRUE);
                    result[0] = "<html><body>" + dnev.result.toString(); //NOI18N
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result[0];
    }
    
    private String getTooltipTextForBundleKey(Pair<Node, ELElement> pair) {
        FileObject context = pair.second().getSnapshot().getSource().getFileObject();
        ResourceBundles resourceBundles = ResourceBundles.get(context);
        if (!resourceBundles.canHaveBundles()) {
            return null;
        }
        for (Pair<AstIdentifier, Node> each : resourceBundles.collectKeys(pair.second().getNode())) {
            if (each.second().equals(pair.first())) {
                StringBuilder result = new StringBuilder();
                String key = each.second().getImage();
                String value = resourceBundles.getValue(each.first().getImage(), each.second().getImage());
                result.append("<html><body>")
                        .append(key)
                        .append("=<font color='#ce7b00'>") // NOI18N
                        .append(value)
                        .append("</font>"); // NOI18N

                return result.toString();
            }
        }
        return null;
    }

    protected static Pair<Node, ELElement> resolveNodeAndElement(final Document doc, final int offset, final AtomicBoolean cancel) {
        Source source = Source.create(doc);
        return resolveNodeAndElement(source, offset, cancel);
    }
    /**
     * Resolves the Node at the given offset.
     * @return the node and the ELElement containing it or null.
     */
    protected static Pair<Node, ELElement> resolveNodeAndElement(final Source source, final int offset, final AtomicBoolean cancel) {
        final List<Pair<Node,ELElement>> result = new ArrayList<>(1);
        try {
            ParserManager.parse(Collections.singletonList(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator elRi = WebUtils.getResultIterator(resultIterator, ELLanguage.MIME_TYPE);
                    if (cancel.get()) {
                        return;
                    }
                    ELParserResult parserResult = (ELParserResult) elRi.getParserResult();
                    ELElement elElement = parserResult.getElementAt(offset);
                    if (elElement == null || !elElement.isValid()) {
                        return;
                    }
                    Node node = elElement.findNodeAt(offset);
                    if (node == null) {
                        return;
                    }
                    
                    if(node instanceof AstMethodArguments) {
                        node = node.jjtGetParent(); 
                        assert node instanceof AstDotSuffix;
                    }
                    
                    result.add(Pair.<Node, ELElement>of(node, elElement));
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result.isEmpty() ? null : result.get(0);
    }

    private void performGoTo(final Document doc, final int offset, final AtomicBoolean cancel) {
        final Pair<Node, ELElement> nodeElem = resolveNodeAndElement(doc, offset, cancel);
        if (nodeElem == null) {
            return;
        }
        final FileObject file = DataLoadersBridge.getDefault().getFileObject(doc);
        ClasspathInfo cp = ELTypeUtilities.getElimplExtendedCPI(file);
        final AtomicReference<ElementHandle<Element>> handleRef = new AtomicReference<>();
        try {
            JavaSource.create(cp).runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    Element javaElement = ELTypeUtilities.resolveElement(CompilationContext.create(file, cc), nodeElem.second(), nodeElem.first());
                    if(javaElement != null) {
                        handleRef.set(ElementHandle.create(javaElement));
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        ElementHandle<Element> handle = handleRef.get();
        if(handle != null) {
            ElementOpen.open(cp, handle);
        }
        
    }

    protected static int[] getELIdentifierSpan(Document doc, int offset) {
        TokenSequence<?> elTokenSequence = LexerUtils.getTokenSequence(doc, offset, ELTokenId.language(), false);
        if (elTokenSequence == null) {
            return null;
        }

        elTokenSequence.move(offset);
        if (!elTokenSequence.moveNext()) {
            return null; //no token
        }

        if (elTokenSequence.token().id() == ELTokenId.IDENTIFIER
                || elTokenSequence.token().id() == ELTokenId.STRING_LITERAL) { // string for bundle keys

            return new int[]{elTokenSequence.offset(), elTokenSequence.offset() + elTokenSequence.token().length()};
        }

        return null;
    }

    /**
     * This is a copy of {@code org.netbeans.modules.editor.java.GoToSupport.DisplayNameElementVisitor}.
     * See #189669.
     */
    private static final class DisplayNameElementVisitor extends AbstractElementVisitor6<Void, Boolean> {

        private final CompilationInfo info;

        public DisplayNameElementVisitor(CompilationInfo info) {
            this.info = info;
        }

        private StringBuffer result        = new StringBuffer();

        private void boldStartCheck(boolean highlightName) {
            if (highlightName) {
                result.append("<b>");
            }
        }

        private void boldStopCheck(boolean highlightName) {
            if (highlightName) {
                result.append("</b>");
            }
        }

        @Override
        public Void visitPackage(PackageElement e, Boolean highlightName) {
            boldStartCheck(highlightName);

            result.append(e.getQualifiedName());

            boldStopCheck(highlightName);

            return null;
        }

        @Override
        public Void visitType(TypeElement e, Boolean highlightName) {
            return printType(e, null, highlightName);
        }

        Void printType(TypeElement e, DeclaredType dt, Boolean highlightName) {
            modifier(e.getModifiers());
            switch (e.getKind()) {
                case CLASS:
                    result.append("class ");
                    break;
                case INTERFACE:
                    result.append("interface ");
                    break;
                case ENUM:
                    result.append("enum ");
                    break;
                case ANNOTATION_TYPE:
                    result.append("@interface ");
                    break;
                default:
                    break;
            }
            Element enclosing = e.getEnclosingElement();

            if (enclosing == info.getElementUtilities().enclosingTypeElement(e)) {
                result.append(((TypeElement) enclosing).getQualifiedName());
                result.append('.');
                boldStartCheck(highlightName);
                result.append(e.getSimpleName());
                boldStopCheck(highlightName);
            } else {
                result.append(e.getQualifiedName());
            }

            if (dt != null)
                dumpRealTypeArguments(dt.getTypeArguments());

            return null;
        }

        @Override
        public Void visitVariable(VariableElement e, Boolean highlightName) {
            modifier(e.getModifiers());

            result.append(getTypeName(info, e.asType(), true));

            result.append(' ');

            boldStartCheck(highlightName);

            result.append(e.getSimpleName());

            boldStopCheck(highlightName);

            if (highlightName) {
                if (e.getConstantValue() != null) {
                    result.append(" = ");
                    result.append(e.getConstantValue().toString());
                }

                Element enclosing = e.getEnclosingElement();

                if (e.getKind() != ElementKind.PARAMETER && e.getKind() != ElementKind.LOCAL_VARIABLE && e.getKind() != ElementKind.EXCEPTION_PARAMETER) {
                    result.append(" in ");

                    //short typename:
                    result.append(getTypeName(info, enclosing.asType(), true));
                }
            }

            return null;
        }

        @Override
        public Void visitExecutable(ExecutableElement e, Boolean highlightName) {
            return printExecutable(e, null, highlightName);
        }

        Void printExecutable(ExecutableElement e, DeclaredType dt, Boolean highlightName) {
            switch (e.getKind()) {
                case CONSTRUCTOR:
                    modifier(e.getModifiers());
                    dumpTypeArguments(e.getTypeParameters());
                    result.append(' ');
                    boldStartCheck(highlightName);
                    result.append(e.getEnclosingElement().getSimpleName());
                    boldStopCheck(highlightName);
                    if (dt != null) {
                        dumpRealTypeArguments(dt.getTypeArguments());
                        dumpArguments(e.getParameters(), ((ExecutableType) info.getTypes().asMemberOf(dt, e)).getParameterTypes());
                    } else {
                        dumpArguments(e.getParameters(), null);
                    }
                    dumpThrows(e.getThrownTypes());
                    break;
                case METHOD:
                    modifier(e.getModifiers());
                    dumpTypeArguments(e.getTypeParameters());
                    result.append(getTypeName(info, e.getReturnType(), true));
                    result.append(' ');
                    boldStartCheck(highlightName);
                    result.append(e.getSimpleName());
                    boldStopCheck(highlightName);
                    dumpArguments(e.getParameters(), null);
                    dumpThrows(e.getThrownTypes());
                    break;
                case INSTANCE_INIT:
                case STATIC_INIT:
                    //these two cannot be referenced anyway...
            }
            return null;
        }

        @Override
        public Void visitTypeParameter(TypeParameterElement e, Boolean highlightName) {
            return null;
        }

        private void modifier(Set<Modifier> modifiers) {
            boolean addSpace = false;

            for (Modifier m : modifiers) {
                if (addSpace) {
                    result.append(' ');
                }
                addSpace = true;
                result.append(m.toString());
            }

            if (addSpace) {
                result.append(' ');
            }
        }

        private void dumpTypeArguments(List<? extends TypeParameterElement> list) {
            if (list.isEmpty())
                return ;

            boolean addSpace = false;

            result.append("&lt;");

            for (TypeParameterElement e : list) {
                if (addSpace) {
                    result.append(", ");
                }

                result.append(getTypeName(info, e.asType(), true));

                addSpace = true;
            }

            result.append("&gt;");
        }

        private void dumpRealTypeArguments(List<? extends TypeMirror> list) {
            if (list.isEmpty())
                return ;

            boolean addSpace = false;

            result.append("&lt;");

            for (TypeMirror t : list) {
                if (addSpace) {
                    result.append(", ");
                }

                result.append(getTypeName(info, t, true));

                addSpace = true;
            }

            result.append("&gt;");
        }

        private void dumpArguments(List<? extends VariableElement> list, List<? extends TypeMirror> types) {
            boolean addSpace = false;

            result.append('(');

            Iterator<? extends VariableElement> listIt = list.iterator();
            Iterator<? extends TypeMirror> typesIt = types != null ? types.iterator() : null;

            while (listIt.hasNext()) {
                if (addSpace) {
                    result.append(", ");
                }

                VariableElement ve = listIt.next();
                TypeMirror      type = typesIt != null ? typesIt.next() : ve.asType();

                result.append(getTypeName(info, type, true));
                result.append(" ");
                result.append(ve.getSimpleName());

                addSpace = true;
            }

            result.append(')');
        }

        private void dumpThrows(List<? extends TypeMirror> list) {
            if (list.isEmpty())
                return ;

            boolean addSpace = false;

            result.append(" throws ");

            for (TypeMirror t : list) {
                if (addSpace) {
                    result.append(", ");
                }

                result.append(getTypeName(info, t, true));

                addSpace = true;
            }
        }
    }

    private static String getTypeName(CompilationInfo info, TypeMirror t, boolean fqn) {
        return translate(info.getTypeUtilities().getTypeName(t).toString());
    }

    private static String[] c = new String[] {"&", "<", ">", "\n", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "<br>", "&quot;"}; // NOI18N

    private static String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }

        return input;
    }

}
