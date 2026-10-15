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
package org.netbeans.modules.javadoc.hints;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Pokorsky
 */
final class GenerateJavadocFix extends JavaFix {

    private final String name;
    private final SourceVersion spec;

    public GenerateJavadocFix(String name, TreePathHandle handle, SourceVersion spec) {
        super(handle);
        this.name = name;
        this.spec = spec;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(GenerateJavadocFix.class, "MISSING_JAVADOC_HINT", name); // NOI18N
    }

    @Override
    protected void performRewrite(TransformationContext ctx) throws Exception {
        WorkingCopy javac = ctx.getWorkingCopy();
        TreeMaker make = javac.getTreeMaker();
        TreePath path = ctx.getPath();
        Element elm = javac.getTrees().getElement(path);
        
        if(elm != null) {
            final JavadocGenerator gen = new JavadocGenerator(spec);
            DocCommentTree newDocCommentTree = gen.generateComment(elm, javac, make);
            DocCommentTree docCommentTree = ((DocTrees) javac.getTrees()).getDocCommentTree(path);
            javac.rewrite(path.getLeaf(), docCommentTree, newDocCommentTree);
        }
    }
}
