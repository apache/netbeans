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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import junit.textui.TestRunner;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Tests more transaction on file with fields. Tests fields generating and
 * also update.
 *
 * @author  Pavel Flaska
 */
public class FieldTest4 extends GeneratorTestBase {
    
    /** Creates a new instance of FieldTest4 */
    public FieldTest4(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(FieldTest4.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "FieldTest4.java");
    }

    /**
     * Tests inital value for fields in field group.
     */
    public void testAddField() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    VariableTree vtecko = make.Variable(
                            make.Modifiers(Collections.singleton(Modifier.PROTECTED)),
                            "newField",
                            make.Identifier("String"),
                            null
                    );
                    List<Tree> memberDecl = new ArrayList<Tree>(node.getMembers());
                    memberDecl.add(vtecko);
                    ClassTree ct = make.Class(node.getModifiers(),
                            node.getSimpleName(),
                            node.getTypeParameters(),
                            node.getExtendsClause(),
                            (List<ExpressionTree>) node.getImplementsClause(),
                            memberDecl);
                    model.setElement(ct, model.getElement(node));
                    model.setType(ct, model.getType(node));
                    copyCommentTo(node, ct);
                    copy.rewrite(node, ct);
                    return null;
                }
            }
        );
        assertFiles("testAddField_FieldTest4.pass");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/FieldTest4/";
    }
}
