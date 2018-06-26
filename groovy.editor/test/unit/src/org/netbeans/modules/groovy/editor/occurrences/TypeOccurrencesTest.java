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

package org.netbeans.modules.groovy.editor.occurrences;

import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Martin Janicek
 */
public class TypeOccurrencesTest extends GroovyTestBase {

    public TypeOccurrencesTest(String testName) {
        super(testName);
    }

    public void testImportStatement() throws Exception {
        testCaretLine("import java.lang.Str^ing");
    }

    public void testExtends() throws Exception {
        testCaretLine("class TypeOccurrencesTester extends Str^ing {");
    }

    public void testField() throws Exception {
        testCaretLine("    protected St^ring fieldString");
    }

    public void testFieldArray() throws Exception {
        testCaretLine("    private St^ring[] fieldArrayString");
    }

    public void testFieldList() throws Exception {
        testCaretLine("    private List<Stri^ng> fieldList");
    }

    public void testProperty() throws Exception {
        testCaretLine("    St^ring propertyString");
    }

    public void testPropertyArray() throws Exception {
        testCaretLine("    Str^ing[] propertyArrayString");
    }

    public void testPropertyList() throws Exception {
        testCaretLine("    List<St^ring> propertyList = new ArrayList<String>();");
    }

    public void testPropertyList_init() throws Exception {
        testCaretLine("    List<String> propertyList = new ArrayList<St^ring>();");
    }

    public void testConstructor() throws Exception {
        testCaretLine("    TypeOccurrencesTester(St^ring constructorParam) {");
    }

    public void testConstructorArray() throws Exception {
        testCaretLine("    TypeOccurrencesTester(St^ring[] constructorParam) {");
    }

    public void testConstructorList() throws Exception {
        testCaretLine("    TypeOccurrencesTester(List<St^ring> constructorParam) {");
    }

    public void testReturnType() throws Exception {
        testCaretLine("    public St^ring returnType() {}");
    }

    public void testReturnTypeArray() throws Exception {
        testCaretLine("    public St^ring[] arrayReturnType() {}");
    }

    public void testReturnTypeList() throws Exception {
        testCaretLine("    public List<St^ring> listReturnType() {}");
    }

    public void testParameterType() throws Exception {
        testCaretLine("    public void parameterType(St^ring parameterType, Number test) {}");
    }

    public void testParameterTypeArray() throws Exception {
        testCaretLine("    public void arrayParameterType(St^ring[] parameterType, Number test) {}");
    }

    public void testParameterTypeList() throws Exception {
        testCaretLine("    public void listParameterType(List<St^ring> parameterType, Number test) {}");
    }

    public void testDeclaration() throws Exception {
        testCaretLine("        Str^ing string");
    }

    public void testDeclarationWithInitialization() throws Exception {
        testCaretLine("        Str^ing stringInit = new String()");
    }

    public void testDeclarationArray() throws Exception {
        testCaretLine("        Str^ing[] stringArray");
    }

    public void testDeclarationArrayWithInitialization() throws Exception {
        testCaretLine("        Str^ing[] stringArrayInit = new String[1]");
    }

    public void testDeclarationArrayWithInitialization_init() throws Exception {
        testCaretLine("        String[] stringArrayInit = new St^ring[1]");
    }

    public void testDeclarationList() throws Exception {
        testCaretLine("        List<Str^ing> stringList");
    }

    public void testDeclarationListWithInitialization() throws Exception {
        testCaretLine("        List<St^ring> stringListInit = new ArrayList<String>()");
    }

    public void testDeclarationListWithInitialization_init() throws Exception {
        testCaretLine("        List<String> stringListInit = new ArrayList<Str^ing>()");
    }

    public void testStaticAccess() throws Exception {
        testCaretLine("        Str^ing.CASE_INSENSITIVE_ORDER");
    }

    public void testInstanceOf() throws Exception {
        testCaretLine("        if (val instanceof Str^ing) {");
    }

    public void testForLoopCycle() throws Exception {
        testCaretLine("        for (St^ring sss : somearray) {");
    }

    public void testDeclarationWithAssignement() throws Exception {
        testCaretLine("            St^ring innerString = sss.concat(\"\");");
    }



    private void testCaretLine(String caretLine) throws Exception {
        checkOccurrences("testfiles/TypeOccurrencesTester.groovy", caretLine, false);
    }
}
