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
package org.netbeans.modules.spring.webmvc.editor;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import javax.lang.model.element.Name;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.util.Exceptions;

/**
 *
 * @author alexeybutenko
 */
public class AddBeanPanel {
    private Document document;
    private String className;
    private String id;

    AddBeanPanel(Document document) {
        this.document = document;
    }

    Document getDocument() {
        return document;
    }
    
    void setId(String id) {
        this.id = id;
    }


    public String getClassName() {
        if (className == null) {
            className = findClassName();
        }
        return className;
    }

    void setClassName(String className) {
        this.className = className;
    }

    public String getId() {
        if (id == null) {
            String name = getClassName();
            if (name == null) {
                return null;
            }
            name = name.substring(name.lastIndexOf(".") + 1); //NOI18N
            id = name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        return id;
    }

    private String findClassName() {
        final String[] clazzName = new String[1];
        JavaSource js = JavaSource.forDocument(document);
        if (js == null) {
            return null;
        }
        try {
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    Document doc = cc.getDocument();
                    if (doc != null) {
                        ExpressionTree packageTree = cc.getCompilationUnit().getPackageName();
                        if (packageTree != null) {
                            TreePath path = cc.getTrees().getPath(cc.getCompilationUnit(), packageTree);
                            Name qualifiedName = cc.getElements().getPackageOf(
                                    cc.getTrees().getElement(path)).getQualifiedName();
                            clazzName[0] = qualifiedName.toString() + "."; //NOI18N
                        }
                        String cls = new ClassScanner().scan(cc.getCompilationUnit(), null);
                        if (clazzName[0] == null) {
                            clazzName[0] = cls;
                        } else {
                            clazzName[0] += cls;
                        }
                    }


                }
            }, true);
            return clazzName[0];
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private class ClassScanner extends TreePathScanner<String, Void> {

        @Override
        public String visitClass(ClassTree tree, Void p) {
            return tree.getSimpleName().toString();
        }

    }
}
