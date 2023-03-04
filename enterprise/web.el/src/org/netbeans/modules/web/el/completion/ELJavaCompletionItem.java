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
package org.netbeans.modules.web.el.completion;

import com.sun.el.parser.Node;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Erno Mononen
 */
final class ELJavaCompletionItem extends DefaultCompletionProposal {

    private static final String ICON_PATH = "org/netbeans/modules/web/el/completion/resources/jsf_bean_16.png";//NOI18N

    private final String elementName;
    private final ELElement elElement;
    private final ElementHandleAdapter adapter;
    
    private final String javaElementSimpleName;
    private final String javaElementTypeName;
    private final String javaElementParametersAsString;
    private final TypeMirror javaElementReturnType;
    private final List<String> javaElementParametersList;
    private final boolean isMethod;
    private final boolean isMethodWithParams;
    private final boolean isBracketCall;


    public ELJavaCompletionItem(CompilationContext info, Element javaElement, ELElement elElement) {
        this(info, javaElement, null, elElement);
    }

    public ELJavaCompletionItem(CompilationContext info, Element javaElement, ELElement elElement, boolean isBracketCall) {
        this(info, javaElement, null, elElement, isBracketCall);
    }
    
    public ELJavaCompletionItem(CompilationContext info, Element javaElement, String elementName, ELElement elElement) {
        this(info, javaElement, elementName, elElement, false);
    }

    public ELJavaCompletionItem(CompilationContext info, Element javaElement, String elementName, ELElement elElement, boolean isBracketProperty) {
        assert javaElement != null;
        this.elElement = elElement;
        this.elementName = elementName;
        this.isBracketCall = isBracketProperty;
        
        isMethod =  javaElement.getKind() == javax.lang.model.element.ElementKind.METHOD;
        
        javaElementSimpleName = javaElement.getSimpleName().toString();
            
        javaElementTypeName = ELTypeUtilities.getTypeNameFor(info, javaElement);
        javaElementParametersAsString = isMethod 
                ? ELTypeUtilities.getParametersAsString(info, (ExecutableElement) javaElement)
                : null;
        javaElementParametersList = isMethod
                ? ELTypeUtilities.getParameterNames(info, (ExecutableElement) javaElement)
                : Collections.<String>emptyList();
        javaElementReturnType = isMethod
                ? ((ExecutableElement) javaElement).getReturnType()
                : null;
        
        isMethodWithParams = isMethod && !javaElementParametersList.isEmpty();

        
        adapter = new ElementHandleAdapter(info, javaElement);
        
        setAnchorOffset(elElement.getOriginalOffset().getStart());
    }

    @Override
    public org.netbeans.modules.csl.api.ElementHandle getElement() {
        return adapter;
    }

    @Override
    public String getName() {
        return elementName != null ? elementName : RefactoringUtil.getPropertyName(javaElementSimpleName, javaElementReturnType, false);
    }
        
    @Override
    public ElementKind getKind() {
        if(isPropertyMethod()) {
            return ElementKind.PROPERTY;
        } else {
            return isMethod() ? ElementKind.METHOD : ElementKind.CLASS;
        }
    }
    
    @Override
    public Set<Modifier> getModifiers() {
        return Collections.singleton(Modifier.PUBLIC);
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        ElementKind kind = getKind();
        formatter.name(kind, true);
        formatter.appendText(getName());
        
        //do not add the method parameters for the is/get/set methods - properties
        if(isMethod()) {
            if(!isPropertyMethod()) {
                if(isMethodWithParamaters()) {
                    //method with params
                    formatter.appendText(javaElementParametersAsString);
                } else {
                    //w/o params, add empty () pair
                    formatter.appendText("()");
                }
            }
        }
        formatter.name(kind, false);

        return formatter.getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return javaElementTypeName;
    }

    @Override
    public ImageIcon getIcon() {
        if (getKind() == ElementKind.CLASS) {
            return ImageUtilities.loadImageIcon(ICON_PATH, false);
        }
        return super.getIcon();
    }

    @Override
    public List<String> getInsertParams() {
        if (!isMethod()) {
            return null;
        }
        if(isPropertyMethod()) { //no params for properties
            return null;
        }

        return !isPropertyMethod() && isMethodWithParamaters()
                ? javaElementParametersList :
                Collections.<String>singletonList(""); //add and empty paramater for non argument method calls
    }

    @Override
    public String getCustomInsertTemplate() {
        if (isBracketCall) {
            return getInsertPrefix();
        }

        return super.getCustomInsertTemplate(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean beforeDefaultAction() {
        Node node = elElement.findNodeAt(anchorOffset);
        if (node != null) {
            OffsetRange originalOffset = elElement.getOriginalOffset(node);
            int startOfsset = originalOffset.getStart();
            int endOfsset = originalOffset.getEnd();
            if (startOfsset != -1 && endOfsset != -1 && endOfsset != anchorOffset + 1) {
                Document document = elElement.getSnapshot().getSource().getDocument(false);
                if (document == null) {
                    return false;
                }
                try {
                    document.remove(startOfsset, endOfsset - startOfsset);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return super.beforeDefaultAction();
    }

    @Override
    public String[] getParamListDelimiters() {
        return new String[]{"(", ")"};
    }

    private boolean isMethod() {
        return isMethod;
    }

    private boolean isMethodWithParamaters() {
        return isMethodWithParams;
    }

    private boolean isPropertyMethod() {
        return isMethod() && !isMethodWithParamaters() && 
                RefactoringUtil.isPropertyAccessor(javaElementSimpleName, javaElementReturnType);
    }

    
    final class ElementHandleAdapter extends ELElementHandle {

        
        private final String in;
        private final ElementHandle elementHandle;
        
        public ElementHandleAdapter(CompilationContext info, Element javaElement) {
            this.in = getIn(javaElement);
            this.elementHandle = ElementHandle.create(javaElement);
        }
        
        @Override
        public String getName() {
            return ELJavaCompletionItem.this.getName();
        }

        @Override
        public ElementKind getKind() {
            return ELJavaCompletionItem.this.getKind();
        }

        private String getIn(Element javaElement) {
            if (isMethod()) {
                return javaElement.getEnclosingElement().getSimpleName().toString();
            }
            return javaElement.getSimpleName().toString();
        }
        
        @Override
        public FileObject getFileObject() {
            return elElement.getSnapshot().getSource().getFileObject();
        }

        @Override
        public String getMimeType() {
            return "text/java"; //NOI18N
        }
            
        @Override
        public String getIn() {
            return in;
        }


        @Override
        public Set<Modifier> getModifiers() {
            return Collections.singleton(Modifier.PUBLIC);
        }

        @Override
        public boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle handle) {
            return getName().equals(handle.getName());
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return elElement.getOriginalOffset();
        }

        @Override
        Documentation document(ParserResult info, final Callable<Boolean> cancel) {
            final Documentation[] result = new Documentation[1];
            try {
                FileObject file = info.getSnapshot().getSource().getFileObject();
                ClasspathInfo cp = ClasspathInfo.create(file);
                JavaSource source = JavaSource.create(cp);
                if (source == null) {
                    return null;
                }
                source.runUserActionTask(new Task<CompilationController>() {

                    @Override
                    public void run(CompilationController info) throws Exception {
                        Element element = elementHandle.resolve(info);
                        ElementJavadoc javadoc = ElementJavadoc.create(info, element, cancel);
                        result[0] = Documentation.create(javadoc.getText());
                    }

                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return result[0];
        }
    }
}
