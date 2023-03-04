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
package org.netbeans.modules.debugger.jpda.ui.completion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchScopeType;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Entlicher
 */
@MimeRegistration(mimeType = JavaClassNbDebugEditorKit.MIME_TYPE, service = CompletionProvider.class)
public class ClassCompletionProvider implements CompletionProvider {
    
    //private final Set<? extends SearchScopeType> scope = Collections.singleton(new ClassSearchScopeType());

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                if (caretOffset < 0) caretOffset = 0;
                String text;
                try {
                    text = doc.getText(0, caretOffset);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                    text = "";
                }
                Set<? extends SearchScopeType> scope = Collections.singleton(new ClassSearchScopeType(text));
                int n = text.length();
                ClasspathInfo cpi = getClassPathInfo();
                ClassIndex classIndex = cpi.getClassIndex();
                Set<String> packageNames = classIndex.getPackageNames(text, false, scope);
                Set<String> resultPackages = new HashSet<String>();
                int lastTextDot = text.lastIndexOf('.');
                for (String pn : packageNames) {
                    int dot = pn.indexOf('.', n);
                    if (dot > 0) pn = pn.substring(0, dot);
                    if (lastTextDot > 0) pn = pn.substring(lastTextDot + 1);
                    if (!resultPackages.contains(pn)) {
                        resultSet.addItem(new ElementCompletionItem(pn, ElementKind.PACKAGE, caretOffset));
                        resultPackages.add(pn);
                    }
                }
                
                String classFilter;
                if (lastTextDot > 0) {
                    classFilter = text.substring(lastTextDot + 1);
                } else {
                    classFilter = text;
                }
                String classFilterLC = classFilter.toLowerCase();
                Set<ElementHandle<TypeElement>> declaredTypes = classIndex.getDeclaredTypes(classFilter, ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX, scope);
                Set<String> resultClasses = new HashSet<String>();
                for (ElementHandle<TypeElement> type : declaredTypes) {
                    String className = type.getQualifiedName();
                    int packageDotIndex = -1;
                    if (lastTextDot > 0) {
                        className = className.substring(lastTextDot + 1);
                        if (!className.toLowerCase().startsWith(classFilterLC)) {
                            continue;
                        }
                    } else {
                        packageDotIndex = type.getBinaryName().lastIndexOf('.');
                        if (packageDotIndex > 0) {
                            className = className.substring(packageDotIndex + 1);
                        }
                    }
                    int dot = className.indexOf('.');
                    if (dot > 0) className = className.substring(0, dot);
                    if (!resultClasses.contains(className)) {
                        ElementCompletionItem eci = new ElementCompletionItem(className, type.getKind(), caretOffset);
                        if (packageDotIndex > 0 && lastTextDot < 0) {
                            eci.setInsertPrefix(type.getQualifiedName().substring(0, packageDotIndex + 1));
                        }
                        resultSet.addItem(eci);
                        resultClasses.add(className);
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
    
    static ClasspathInfo getClassPathInfo() {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        // TODO
        //if (engine != null)
        // Grab the class path from the engine
        
        Set<ClassPath> bootPaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT);
        Set<ClassPath> classPaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        ClassPath cp = ClassPathSupport.createProxyClassPath(classPaths.toArray(new ClassPath[0]));
        return ClasspathInfo.create(
                ClassPathSupport.createProxyClassPath(bootPaths.toArray(new ClassPath[0])),
                cp, cp);
    }
    
}
