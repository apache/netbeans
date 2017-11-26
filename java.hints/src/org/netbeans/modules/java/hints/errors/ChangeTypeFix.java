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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;


/**
 * This hint changes the type of a variable to the type of
 * initializer expression. In effect it works opposite of 
 * Add Cast hint.
 *
 * @author Sandip Chitale
 */
final class ChangeTypeFix implements Fix {
    
    private JavaSource js;
    private String treeName;
    private String type;
    private int position;
    
    public ChangeTypeFix(JavaSource js, String treeName, String type, int position) {
        this.js = js;
        this.treeName = escape(treeName);
        this.type = escape(type);
        this.position = position;
    }
    
    public ChangeInfo implement() throws IOException {
        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy working) throws IOException {
                working.toPhase(Phase.RESOLVED);
                TypeMirror[] tm = new TypeMirror[1];
                TypeMirror[] expressionType = new TypeMirror[1];
                Tree[] leaf = new Tree[1];

                ChangeType.computeType(working, position, tm, expressionType, leaf);

                //anonymous class?
                expressionType[0] = Utilities.convertIfAnonymous(expressionType[0]);

                if (leaf[0] instanceof VariableTree) {
                    VariableTree oldVariableTree = ((VariableTree)leaf[0]);
                    TreeMaker make = working.getTreeMaker();

                    VariableTree newVariableTree = make.Variable(
                            oldVariableTree.getModifiers(), 
                            oldVariableTree.getName(),
                            make.Type(expressionType[0]),
                            oldVariableTree.getInitializer()); 

                    working.rewrite(leaf[0], newVariableTree);
                }
            }
        }).commit();
        
        return null;
    }
    
    public String getText() {
        return NbBundle.getMessage(ChangeTypeFix.class, "MSG_ChangeVariablesType", treeName, type); // NOI18N
    }

    static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (Exception ex) {}
        }
        return s;
    }

}
