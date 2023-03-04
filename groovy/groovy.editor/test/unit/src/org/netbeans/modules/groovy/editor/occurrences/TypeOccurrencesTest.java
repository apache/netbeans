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
