/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
