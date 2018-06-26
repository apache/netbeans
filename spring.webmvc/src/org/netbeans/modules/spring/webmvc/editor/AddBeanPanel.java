/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
