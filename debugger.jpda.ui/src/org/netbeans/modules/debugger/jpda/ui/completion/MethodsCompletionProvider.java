/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.ui.completion;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchScopeType;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Entlicher
 */
@MimeRegistration(mimeType = JavaMethodNbDebugEditorKit.MIME_TYPE, service = CompletionProvider.class)
public class MethodsCompletionProvider implements CompletionProvider {
    
    //private final Set<? extends SearchScopeType> scope = Collections.singleton(new ClassSearchScopeType());

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            
            @Override
            protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
                if (caretOffset < 0) caretOffset = 0;
                String text;
                try {
                    text = doc.getText(0, caretOffset);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                    text = "";
                }
                String className = (String) doc.getProperty("class-name");
                if (className == null) {
                    resultSet.finish();
                    return;
                }
                String packageName;
                String simpleClassName;
                int dot = className.lastIndexOf('.');
                if (dot > 0) {
                    packageName = className.substring(0, dot + 1); // We need the dot at the end
                    simpleClassName = className.substring(dot + 1);
                } else {
                    packageName = "";
                    simpleClassName = className;
                }
                Set<? extends SearchScopeType> scope = Collections.singleton(new ClassSearchScopeType(packageName));
                int n = text.length();
                ClasspathInfo cpi = ClassCompletionProvider.getClassPathInfo();
                ClassIndex classIndex = cpi.getClassIndex();
                Set<ElementHandle<TypeElement>> declaredTypes = classIndex.getDeclaredTypes(simpleClassName, ClassIndex.NameKind.PREFIX, scope);
                ElementHandle<TypeElement> theType = null;
                for (ElementHandle<TypeElement> type : declaredTypes) {
                    if (className.equals(type.getQualifiedName())) {
                        theType = type;
                        break;
                    }
                }
                if (theType != null) {
                    final ElementHandle<TypeElement> type = theType;
                    final int caret = caretOffset;
                    try {
                        JavaSource.create(cpi, new FileObject[]{}).runUserActionTask(new Task<CompilationController>() {
                            @Override
                            public void run(CompilationController cc) throws Exception {
                                TypeElement te = type.resolve(cc);
                                List<? extends Element> enclosedElements = te.getEnclosedElements();
                                for (Element elm : enclosedElements) {
                                    ElementKind kind = elm.getKind();
                                    if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
                                        String name = elm.getSimpleName().toString();
                                        if ("<init>".equals(name)) {    // NOI18N
                                            name = te.getSimpleName().toString();
                                        }
                                        ElementCompletionItem eci = new ElementCompletionItem(name, kind, elm.getModifiers(), caret);
                                        eci.setExecutableElement((ExecutableElement) elm);
                                        resultSet.addItem(eci);
                                    }
                                }
                            }
                        }, true);
                    } catch (IOException ex) {
                        //Exceptions.printStackTrace(ex);
                        Logger.getLogger(MethodsCompletionProvider.class.getName()).log(Level.CONFIG, className, ex);
                    }
                }
                
                resultSet.finish();
            }
        }, component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return COMPLETION_QUERY_TYPE;
    }
    
    
    
}
