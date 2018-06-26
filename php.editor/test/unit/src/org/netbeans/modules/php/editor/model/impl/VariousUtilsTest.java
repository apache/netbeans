/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.model.impl;

import java.util.Collection;
import java.util.HashSet;
import org.junit.Test;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;

/**
 *
 * @author Petr Pisl
 */
public class VariousUtilsTest extends ModelTestBase{

    public VariousUtilsTest(String testName) {
        super(testName);
    }



    /**
     * Test of getPossibleNamespaces method, of class VariousUtils.
     */
    @Test
    public void testGetPossibleNamespaces01() throws Exception {
        QualifiedName name = QualifiedName.createFullyQualified("Kolesa", "baf\\haf");
        NamespaceScope contextNamespace = null;
        Collection<QualifiedName> expResult = new HashSet<QualifiedName>();
        expResult.add(QualifiedName.create("\\baf\\haf\\Kolesa"));
        Collection<QualifiedName> result = VariousUtils.getPossibleFQN(name, 0, contextNamespace);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPossibleNamespaces02() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/namespaces01.php"));
        FileScope topScope = model.getFileScope();

        Collection<? extends ClassScope> classes = ModelUtils.getDeclaredClasses(topScope);
        ClassScope classScope = ModelUtils.getFirst(classes);
        NamespaceScope contextNamespace = (NamespaceScope)classScope.getInScope();
        Collection<QualifiedName> expResult = new HashSet<QualifiedName>();
        expResult.add(QualifiedName.create("\\Libs\\Bar\\IBuz"));
        QualifiedName name = ModelUtils.getFirst(classScope.getSuperInterfaces());
        Collection<QualifiedName> result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPossibleNamespaces03() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/namespaces02.php"));
        FileScope topScope = model.getFileScope();

        Collection<? extends ClassScope> classes = ModelUtils.getDeclaredClasses(topScope);
        ClassScope classScope = ModelUtils.getFirst(classes);
        NamespaceScope contextNamespace = (NamespaceScope)classScope.getInScope();
        Collection<QualifiedName> expResult = new HashSet<QualifiedName>();
        expResult.add(QualifiedName.create("\\Libs\\Bar\\Foo\\IBuz"));
        QualifiedName name = ModelUtils.getFirst(classScope.getSuperInterfaces());
        Collection<QualifiedName> result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPossibleNamespaces04() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/namespaces03.php"));
        FileScope topScope = model.getFileScope();

        Collection<? extends ClassScope> classes = ModelUtils.getDeclaredClasses(topScope);
        ClassScope classScope = ModelUtils.getFirst(classes);
        NamespaceScope contextNamespace = (NamespaceScope)classScope.getInScope();
        Collection<QualifiedName> expResult = new HashSet<QualifiedName>();
        expResult.add(QualifiedName.create("\\Libs\\Bar\\IBuz"));
        QualifiedName name = ModelUtils.getFirst(classScope.getSuperInterfaces());
        Collection<QualifiedName> result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPossibleNamespaces05() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/namespaces04.php"));
        FileScope topScope = model.getFileScope();

        Collection<? extends ClassScope> classes = ModelUtils.getDeclaredClasses(topScope);
        ClassScope classScope = ModelUtils.getFirst(classes);
        NamespaceScope contextNamespace = (NamespaceScope)classScope.getInScope();
        Collection<QualifiedName> expResult = new HashSet<QualifiedName>();
        expResult.add(QualifiedName.create("\\Libs\\Bar\\IBuz"));
        QualifiedName name = ModelUtils.getFirst(classScope.getSuperInterfaces());
        Collection<QualifiedName> result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPossibleNamespaces06() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/namespaces05.php"));
        FileScope topScope = model.getFileScope();

        Collection<? extends ClassScope> classes = ModelUtils.getDeclaredClasses(topScope);
        ClassScope classScope = ModelUtils.getFirst(classes);
        NamespaceScope contextNamespace = (NamespaceScope)classScope.getInScope();
        Collection<QualifiedName> expResult = new HashSet<QualifiedName>();
        expResult.add(QualifiedName.create("\\Libs\\Bar\\IBuz"));
        QualifiedName name = ModelUtils.getFirst(classScope.getSuperInterfaces());
        Collection<QualifiedName> result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPossibleNamespaces07() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/namespaces06.php"));
        FileScope topScope = model.getFileScope();

        Collection<? extends ClassScope> classes = ModelUtils.getDeclaredClasses(topScope);

        ClassScope classScope = ModelUtils.getFirst(ModelUtils.filter(classes, "Foo"));
        NamespaceScope contextNamespace = (NamespaceScope)classScope.getInScope();
        Collection<QualifiedName> expResult = new HashSet<QualifiedName>();
        expResult.add(QualifiedName.create("\\Libs\\Bar\\Buz"));
        QualifiedName name = classScope.getSuperClassName();
        Collection<QualifiedName> result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);

        classScope = ModelUtils.getFirst(ModelUtils.filter(classes, "Foo2"));
        contextNamespace = (NamespaceScope)classScope.getInScope();
        expResult.clear();
        expResult.add(QualifiedName.create("\\Libs\\Kolesa\\Buz"));
        name = classScope.getSuperClassName();
        result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);

        classScope = ModelUtils.getFirst(ModelUtils.filter(classes, "Foo3"));
        contextNamespace = (NamespaceScope)classScope.getInScope();
        expResult.clear();
        expResult.add(QualifiedName.create("\\Libs\\Tetov\\Buz"));
        name = classScope.getSuperClassName();
        result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);

        classScope = ModelUtils.getFirst(ModelUtils.filter(classes, "Foo4"));
        contextNamespace = (NamespaceScope)classScope.getInScope();
        expResult.clear();
        expResult.add(QualifiedName.create("\\Libs\\Komarov\\Buz"));
        name = classScope.getSuperClassName();
        result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);

        classScope = ModelUtils.getFirst(ModelUtils.filter(classes, "Foo5"));
        contextNamespace = (NamespaceScope)classScope.getInScope();
        expResult.clear();
        expResult.add(QualifiedName.create("\\Libs\\Kolesa\\Buz"));
        name = classScope.getSuperClassName();
        result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);

        classScope = ModelUtils.getFirst(ModelUtils.filter(classes, "Foo6"));
        contextNamespace = (NamespaceScope)classScope.getInScope();
        expResult.clear();
        expResult.add(QualifiedName.create("\\Libs\\Komarov\\Buz"));
        name = classScope.getSuperClassName();
        result = VariousUtils.getPossibleFQN(name, classScope.getOffset(), contextNamespace);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPossibleNamespaces08() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/namespaces07.php"));
        FileScope topScope = model.getFileScope();

        Collection<? extends FunctionScope> functions = ModelUtils.getDeclaredFunctions(topScope);
        assertEquals(2, functions.size());
    }

    public void testIssue206727() throws Exception {
        QualifiedName testName = QualifiedName.create("Nette\\Configurator");
        Model model = getModel(getTestSource("testfiles/model/issue206727.php"));
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), 1);
        QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(testName, 1, namespaceScope);
        assertEquals(2, fullyQualifiedName.getSegments().size());
        assertEquals("Nette", fullyQualifiedName.getNamespaceName());
        assertEquals("Configurator", fullyQualifiedName.getName());
        assertEquals(QualifiedNameKind.FULLYQUALIFIED, fullyQualifiedName.getKind());
    }

    public void testIssue210558() throws Exception {
        String array = "array";
        QualifiedName testName = QualifiedName.create(array);
        Model model = getModel(getTestSource("testfiles/model/issue210558.php"));
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(model.getFileScope(), 1);
        QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(testName, 1, namespaceScope);
        assertEquals(QualifiedNameKind.UNQUALIFIED, fullyQualifiedName.getKind());
        assertEquals(array, fullyQualifiedName.getName());
        assertEquals("", fullyQualifiedName.getNamespaceName());
        assertEquals(1, fullyQualifiedName.getSegments().size());
    }

    public void testIssue197024() throws Exception {
        assertEquals("' " + VariousUtils.POST_OPERATION_TYPE_DELIMITER_SUBS + " '", VariousUtils.encodeVariableName("' : '"));
        assertEquals("' " + VariousUtils.POST_OPERATION_TYPE_DELIMITER_SUBS + " " + VariousUtils.POST_OPERATION_TYPE_DELIMITER_SUBS + " '", VariousUtils.encodeVariableName("' : : '"));
        assertEquals("\" " + VariousUtils.POST_OPERATION_TYPE_DELIMITER_SUBS + " \"", VariousUtils.encodeVariableName("\" : \""));
        assertEquals("\" " + VariousUtils.POST_OPERATION_TYPE_DELIMITER_SUBS + " " + VariousUtils.POST_OPERATION_TYPE_DELIMITER_SUBS + " \"", VariousUtils.encodeVariableName("\" : : \""));
    }

    public void testIssue209530() throws Exception {
        assertEquals("", VariousUtils.qualifyTypeNames("|", 0, null));
        assertEquals("", VariousUtils.qualifyTypeNames("| |", 0, null));
        assertEquals("", VariousUtils.qualifyTypeNames("   |     |  ||  ", 0, null));
    }

    public void testIsSemiType() throws Exception {
        assertTrue(VariousUtils.isSemiType("@cls:\\NS\\Foo@fld:context"));
    }

    public void testIsNotSemiType() throws Exception {
        assertFalse(VariousUtils.isSemiType("\\NS\\Foo"));
    }

    public void testNullIsNotSemiType() throws Exception {
        assertFalse(VariousUtils.isSemiType(null));
    }

    public void testStaticClassName() throws Exception {
        assertTrue(VariousUtils.isStaticClassName("static"));
        assertTrue(VariousUtils.isStaticClassName("self"));
        assertFalse(VariousUtils.isStaticClassName("parent"));
    }

    public void testSpecialClassName() throws Exception {
        assertTrue(VariousUtils.isSpecialClassName("static"));
        assertTrue(VariousUtils.isSpecialClassName("self"));
        assertTrue(VariousUtils.isSpecialClassName("parent"));
    }

    public void testIssue235393() throws Exception {
        assertFalse(VariousUtils.isStaticClassName(null));
    }

}