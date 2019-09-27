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
package org.netbeans.modules.php.editor.model.impl;

import java.util.Collection;
import org.netbeans.modules.php.editor.model.*;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.model.FileScope;

/**
 * @author Radek Matous
 */
public class ModelTest extends ModelTestBase {

    public ModelTest(String testName) {
        super(testName);
    }

    public void testVarsForBasicFileScope() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/basicFileScope.php"));
        FileScope topScope = model.getFileScope();
        FunctionScope fncScope = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(topScope),"myfnc"));
        assertNotNull(fncScope);
        VariableName varA = ModelUtils.getFirst(ModelUtils.filter(fncScope.getDeclaredVariables(),"$a"));
        assertNotNull(varA);
        varA = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredVariables(topScope),"$a"));
        assertNotNull(varA);
        VariableName varC = ModelUtils.getFirst(ModelUtils.filter(fncScope.getDeclaredVariables(),"$c"));
        assertNotNull(varC);
        VariableName varD = ModelUtils.getFirst(ModelUtils.filter(fncScope.getDeclaredVariables(),"$d"));
        assertNotNull(varD);
        TypeScope varCType = ModelUtils.getFirst(varC.getTypes(varD.getOffset()));
        assertNotNull(varCType);
        VariableName varParam = ModelUtils.getFirst(ModelUtils.filter(fncScope.getDeclaredVariables(),"$param"));
        assertNotNull(varParam);
        TypeScope vParamType = ModelUtils.getFirst(varParam.getTypes(varD.getOffset()));
        assertNotNull(vParamType);
        assertEquals("MyClass", vParamType.getName());
        VariableName exc = ModelUtils.getFirst(ModelUtils.filter(fncScope.getDeclaredVariables(),"$exc"));
        assertNotNull(exc);
        TypeScope excType = ModelUtils.getFirst(exc.getTypes(exc.getOffset()));
        assertNotNull(excType);
        assertEquals("MyException", excType.getName());
    }

    public void testGlobalVars2() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/globalvars2.php"));
        FileScope topScope = model.getFileScope();
        varContainerTestForGlobal2(ModelUtils.getFirst(topScope.getDeclaredNamespaces()));
    }

    public void testGlobalVars3() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/globalvars3.php"));
        FileScope topScope = model.getFileScope();
        FunctionScope fncScope = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(topScope),"fnc"));
        assertNotNull(fncScope);
        VariableName varA = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredVariables(topScope),"$varA"));
        assertNotNull(varA);
        VariableScope variableScope = model.getVariableScope(fncScope.getBlockRange().getStart());
        assertNotNull(variableScope);
        varA = ModelUtils.getFirst(ModelUtils.filter(variableScope.getDeclaredVariables(),"$varA"));
        assertNotNull(variableScope);
        TypeScope varAType = ModelUtils.getFirst(varA.getTypes(fncScope.getOffset()));
        assertNotNull(varAType);
        assertEquals("YourClass", varAType.getName());
        VariableName varB = ModelUtils.getFirst(ModelUtils.filter(fncScope.getDeclaredVariables(),"$varB"));
        assertNotNull(varB);
        TypeScope varBType = ModelUtils.getFirst(varB.getTypes(varB.getOffset()));
        assertNotNull(varBType);
        assertEquals("YourClass", varBType.getName());

    }

    public void testFunctionVars2() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/globalvars2.php"));
        FileScope topScope = model.getFileScope();
        FunctionScope fncScope = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(topScope),"myfnc"));
        assertNotNull(fncScope);
        varContainerTestForGlobal2(fncScope);
    }

    public void testScopes() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/scope.php"));
        FileScope topScope = model.getFileScope();

        Collection<? extends TypeScope> types = ModelUtils.getDeclaredTypes(topScope);
        Collection<? extends ClassScope> classes = ModelUtils.getDeclaredClasses(topScope);
        Collection<? extends InterfaceScope> interfaces = ModelUtils.getDeclaredInterfaces(topScope);
        assertEquals(types.size(), classes.size() + interfaces.size());
        Collection<? extends FunctionScope> functions = ModelUtils.getDeclaredFunctions(topScope);
        Collection<? extends VariableName> allVariables = ModelUtils.getDeclaredVariables(topScope);

        Collection<? extends ModelElement> elements = topScope.getElements();
        assertEquals(1, elements.size());
        ModelElement e = ModelUtils.getFirst(elements);
        assertTrue(e instanceof NamespaceScope);
        elements = ModelUtils.getFirst(topScope.getDeclaredNamespaces()).getElements();
        assertEquals(elements.size(), types.size() + functions.size() + allVariables.size());

        for (ModelElement elm : elements) {
            assertTrue(elm instanceof ModelElement);
            switch (elm.getPhpElementKind()) {
                case CLASS:
                    assertTrue(elm.getName().startsWith("cls"));
                    assertTrue(elm instanceof Scope);
                    assertTrue(elm.getInScope() instanceof NamespaceScope);
                    assertTrue(elm.getInScope().getInScope() instanceof FileScope);
                    assertTrue(elm.getInScope().getInScope() == topScope);
                    break;
                case IFACE:
                    assertTrue(elm.getName().startsWith("iface"));
                    assertTrue(elm instanceof Scope);
                    assertTrue(elm.getInScope() instanceof NamespaceScope);
                    assertTrue(elm.getInScope().getInScope() instanceof FileScope);
                    assertTrue(elm.getInScope().getInScope() == topScope);
                    break;
                case FUNCTION:
                    assertTrue(elm.getName().contains("fnc"));
                    assertTrue(elm instanceof Scope);
                    assertTrue(elm.getInScope() instanceof NamespaceScope);
                    assertTrue(elm.getInScope().getInScope() instanceof FileScope);
                    assertTrue(elm.getInScope().getInScope() == topScope);
                    break;
                case VARIABLE:
                    //TODO: add som ebasic tests here
                    break;
                default:
                    fail();
                    break;
            }
        }
    }

    public void testFunctionScopes() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/scope.php"));
        FileScope topScope = model.getFileScope();
        assertEquals(1, ModelUtils.filter(ModelUtils.getDeclaredFunctions(topScope),"fnca").size());
        assertEquals(2,  ModelUtils.filter(ModelUtils.getDeclaredFunctions(topScope),"fnca", "fncb").size());

        FunctionScope fnca = ModelUtils.getFirst( ModelUtils.filter(ModelUtils.getDeclaredFunctions(topScope),"fnca"));
        assertNotNull(fnca);
        assertTrue(fnca.getInScope() instanceof NamespaceScope);
        assertTrue(fnca.getInScope().getInScope() instanceof FileScope);
        assertSame(topScope, fnca.getInScope().getInScope());

        assertEquals("fnca", fnca.getName());
        assertEquals("", ModelUtils.getCamelCaseName(fnca));
        assertNotNull(fnca.getFileObject());
        assertSame(topScope.getFileObject(), fnca.getFileObject());

        assertEquals(2, fnca.getParameters().size());
        for (String params : fnca.getParameterNames()) {
            assertTrue(params.contains("$param"));
        }
        TypeScope returnType = ModelUtils.getFirst(fnca.getReturnTypes());
        assertSame(returnType, ModelUtils.getFirst( ModelUtils.filter(ModelUtils.getDeclaredClasses(topScope),"cls1")));
    }

    public void testBasicFileScope() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/basicFileScope.php"));
        FileScope program = model.getFileScope();
        assertNotNull(program);
        assertEquals(1, program.getElements().size());
        //classes
        assertEquals(ModelUtils.getDeclaredClasses(program).size(),
                ModelUtils.filter(ModelUtils.getDeclaredClasses(program), program.getFileObject()).size());
        assertEquals(2,  ModelUtils.filter(ModelUtils.getDeclaredClasses(program),"MyClass", "MySuperClass").size());
        assertEquals(2, ModelUtils.filter(ModelUtils.getDeclaredClasses(program),"myclass", "mysuperclass").size());
        assertEquals(1, ModelUtils.filter(ModelUtils.getDeclaredClasses(program),"MyClass").size());
        assertEquals(1, ModelUtils.filter(ModelUtils.getDeclaredClasses(program),"MySuperClass").size());
        assertEquals(3, ModelUtils.filter(ModelUtils.getDeclaredClasses(program), QuerySupport.Kind.PREFIX, "My").size());
        assertEquals(3, ModelUtils.filter(ModelUtils.getDeclaredClasses(program),QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, "my").size());
        assertEquals(1, ModelUtils.filter(ModelUtils.getDeclaredClasses(program),QuerySupport.Kind.CAMEL_CASE, "MC").size());
        assertEquals(1, ModelUtils.filter(ModelUtils.getDeclaredClasses(program),QuerySupport.Kind.CAMEL_CASE, "MSC").size());
        assertEquals(2, ModelUtils.filter(ModelUtils.getDeclaredClasses(program),QuerySupport.Kind.REGEXP, "M[^z].*C.*ss").size());
        assertEquals(2, ModelUtils.filter(ModelUtils.getDeclaredClasses(program),QuerySupport.Kind.CASE_INSENSITIVE_REGEXP, "m[y].*c.*ss").size());

        ClassScope myClass = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredClasses(program),"MyClass"));
        assertNotNull(myClass);
        ClassScope mySuperClass = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredClasses(program),"MySuperClass"));
        assertNotNull(mySuperClass);
        assertSame(ModelUtils.getFirst(myClass.getSuperClasses()), mySuperClass);

        assertEquals("MC", ModelUtils.getCamelCaseName(myClass));
        final MethodScope statmeth = ModelUtils.getFirst(myClass.getDeclaredMethods(), "statmeth");
        assertNotNull(statmeth);
        assertTrue(statmeth.getPhpModifiers().isStatic());
        assertTrue(statmeth.getPhpModifiers().isPublic());
        assertFalse(statmeth.getPhpModifiers().isPrivate());

        MethodScope method = ModelUtils.getFirst(myClass.getDeclaredMethods(), "meth");
        assertNotNull(method);
        //TODO: fix it
        //assertEquals(2, method.getReturnTypes().size());
        TypeScope type = ModelUtils.getFirst(method.getReturnTypes());
        assertNotNull(type);
        assertEquals(method.getName(), "meth");
        assertSame(myClass, ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredClasses(program),QuerySupport.Kind.REGEXP, "MyC.*")));

        assertSame(method.getInScope(), myClass);
        //fields
        FieldElement fieldElement = ModelUtils.getFirst(myClass.getDeclaredFields(), "$myFld");
        assertNotNull(fieldElement);
        assertTrue(fieldElement.getPhpModifiers().isPublic());

        //ifaces
        assertNotNull(ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredInterfaces(program),"MyIFace")));

        assertEquals(1, ModelUtils.filter(ModelUtils.getDeclaredInterfaces(program),"MyIFace").size());
        assertEquals(1, ModelUtils.filter(ModelUtils.getDeclaredInterfaces(program),"MySuperIFace").size());
        assertEquals(2, ModelUtils.filter(ModelUtils.getDeclaredInterfaces(program),QuerySupport.Kind.PREFIX, "My").size());
        assertEquals(2, ModelUtils.filter(ModelUtils.getDeclaredInterfaces(program),QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, "my").size());
        assertEquals(1, ModelUtils.filter(ModelUtils.getDeclaredInterfaces(program),QuerySupport.Kind.CAMEL_CASE, "MIF").size());
        assertEquals(2, ModelUtils.filter(ModelUtils.getDeclaredInterfaces(program),QuerySupport.Kind.REGEXP, "M.*I.*").size());
        assertEquals(2, ModelUtils.filter(ModelUtils.getDeclaredInterfaces(program),QuerySupport.Kind.CASE_INSENSITIVE_REGEXP, "m.*f.*").size());

        //functions
        assertNotNull(ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(program),"myfnc")));
        assertNotNull(ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(program),"myfnc2")));
        assertNotNull(ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(program),"myfnc3")));
        assertNotNull(ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(program),"myfnc4")));
    }

    public void testReturnTypes01() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/returnTypes01.php"));
        FileScope program = model.getFileScope();
        assertNotNull(program);
        assertEquals(1, program.getElements().size());
        FunctionScope functionScope = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(program), "foo"));
        assertNotNull(functionScope);
        Collection<? extends String> returnTypeNames = functionScope.getReturnTypeNames();
        assertEquals(1, returnTypeNames.size());
        assertEquals("\\DateTime", ModelUtils.getFirst(returnTypeNames));
    }

    public void testReturnTypes02() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/returnTypes02.php"));
        FileScope program = model.getFileScope();
        assertNotNull(program);
        assertEquals(1, program.getElements().size());
        FunctionScope functionScope = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(program), "foo"));
        assertNotNull(functionScope);
        Collection<? extends String> returnTypeNames = functionScope.getReturnTypeNames();
        assertEquals(1, returnTypeNames.size());
        assertEquals("array", ModelUtils.getFirst(returnTypeNames));
    }

    public void testReturnTypes03() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/returnTypes03.php"));
        FileScope program = model.getFileScope();
        assertNotNull(program);
        assertEquals(1, program.getElements().size());
        // iface
        InterfaceScope iface = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredInterfaces(program), "A"));
        assertNotNull(iface);
        MethodScope ifaceMethod = ModelUtils.getFirst(iface.getDeclaredMethods(), "make");
        assertNotNull(ifaceMethod);
        Collection<? extends String> ifaceMethodReturnTypes = ifaceMethod.getReturnTypeNames();
        assertEquals(1, ifaceMethodReturnTypes.size());
        assertEquals("\\A", ModelUtils.getFirst(ifaceMethodReturnTypes));
        // class
        ClassScope clazz = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredClasses(program), "B"));
        assertNotNull(clazz);
        MethodScope clazzMethod = ModelUtils.getFirst(clazz.getDeclaredMethods(), "make");
        assertNotNull(clazzMethod);
        Collection<? extends String> clazzMethodReturnTypes = clazzMethod.getReturnTypeNames();
        assertEquals(1, clazzMethodReturnTypes.size());
        assertEquals("\\A", ModelUtils.getFirst(clazzMethodReturnTypes));
        // trait
        TraitScope trait = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredTraits(program), "C"));
        assertNotNull(trait);
        MethodScope traitMethod = ModelUtils.getFirst(trait.getDeclaredMethods(), "make");
        assertNotNull(traitMethod);
        Collection<? extends String> traitMethodReturnTypes = traitMethod.getReturnTypeNames();
        assertEquals(1, traitMethodReturnTypes.size());
        assertEquals("\\C", ModelUtils.getFirst(traitMethodReturnTypes));
    }

    public void testReturnTypes04() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/returnTypes04.php"));
        FileScope program = model.getFileScope();
        assertNotNull(program);
        assertEquals(1, program.getElements().size());
        FunctionScope foo = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(program), "foo"));
        assertNotNull(foo);
        Collection<? extends String> fooReturnTypeNames = foo.getReturnTypeNames();
        assertEquals(1, fooReturnTypeNames.size());
        assertEquals("\\Iterator", ModelUtils.getFirst(fooReturnTypeNames));
        FunctionScope bar = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredFunctions(program), "bar"));
        assertNotNull(bar);
        Collection<? extends String> barReturnTypeNames = bar.getReturnTypeNames();
        assertEquals(1, barReturnTypeNames.size());
        assertEquals("DateTime", ModelUtils.getFirst(barReturnTypeNames));
    }

    // XXX lambda functions need to be improved
    public void xtestReturnTypes05() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/returnTypes05.php"));
        FileScope program = model.getFileScope();
        assertNotNull(program);
        assertEquals(1, program.getElements().size());
        Collection<? extends FunctionScope> declaredFunctions = ModelUtils.getDeclaredFunctions(program);
    }

    public void testIssue202460 () throws Exception {
        Model model = getModel(getTestSource("testfiles/model/issue202460.php"));
        FileScope topScope = model.getFileScope();
        Collection<? extends NamespaceScope> declaredNamespaces = topScope.getDeclaredNamespaces();
        Collection<? extends ConstantElement> declaredConstants = declaredNamespaces.iterator().next().getDeclaredConstants();

        assertEquals(2, declaredConstants.size());
        for (ConstantElement constantElement : declaredConstants) {
            if(constantElement.getName().equals("NEGATIVE")) {
                assertEquals("-1", constantElement.getValue());
            } else if(constantElement.getName().equals("POSITIVE")) {
                assertEquals("1", constantElement.getValue());
            } else {
                assertTrue(false);
            }
        }
    }

    public void testIssue268825() throws Exception {
        Model model = getModel(getTestSource("testfiles/model/issue268825.php"), false);
        FileScope topScope = model.getFileScope();
        ClassScope classScope = ModelUtils.getFirst(ModelUtils.filter(ModelUtils.getDeclaredClasses(topScope), "Anon"));
        assertNotNull(classScope);
        MethodScope methodScope = ModelUtils.getFirst(ModelUtils.filter(classScope.getDeclaredMethods(), "test"));
        assertNotNull(methodScope);
        int offset = "{\n        AutoPopup::test('test', function(AutoPopup $test) {".length();
        VariableScope variableScope = model.getVariableScope(methodScope.getBlockRange().getStart() + offset);
        assertNotNull(variableScope);
        String name = variableScope.getName();
        assertTrue(name.startsWith("LambdaFunctionDeclaration"));
    }

    private void varContainerTestForGlobal2(VariableScope topScope) {
        VariableName my = ModelUtils.getFirst(ModelUtils.filter(topScope.getDeclaredVariables(),"$my"));
        assertNotNull(my);
        VariableName your = ModelUtils.getFirst(ModelUtils.filter(topScope.getDeclaredVariables(),"$your"));
        assertNotNull(your);
        VariableName our = ModelUtils.getFirst(ModelUtils.filter(topScope.getDeclaredVariables(),"$our"));
        assertNotNull(our);
        VariableName ourComplex = ModelUtils.getFirst(ModelUtils.filter(topScope.getDeclaredVariables(),"$ourComplex"));
        assertNotNull(ourComplex);
        VariableName otherComplex = ModelUtils.getFirst(ModelUtils.filter(topScope.getDeclaredVariables(),"$otherComplex"));
        assertNotNull(otherComplex);
        VariableName last = ModelUtils.getFirst(ModelUtils.filter(topScope.getDeclaredVariables(),"$last"));
        assertNotNull(last);
        VariableName foreign = ModelUtils.getFirst(ModelUtils.filter(topScope.getDeclaredVariables(),"$foreign"));
        assertNotNull(foreign);
        VariableName foreign2 = ModelUtils.getFirst(ModelUtils.filter(topScope.getDeclaredVariables(),"$foreign2"));
        assertNotNull(foreign2);
        TypeScope myType = ModelUtils.getFirst(my.getTypes(our.getOffset()));
        assertNotNull(myType);
        assertEquals("MyCls", myType.getName());
        TypeScope yourType = ModelUtils.getFirst(your.getTypes(last.getOffset()));
        assertNotNull(yourType);
        assertEquals("MyCls", yourType.getName());
        TypeScope myType2 = ModelUtils.getFirst(my.getTypes(ourComplex.getOffset()));
        assertNotNull(myType2);
        assertEquals("MyCls2", myType2.getName());
        TypeScope ourComplexType = ModelUtils.getFirst(my.getTypes(otherComplex.getOffset()));
        assertNotNull(ourComplexType);
        assertEquals("MyCls2", ourComplexType.getName());
        TypeScope otherComplexType = ModelUtils.getFirst(otherComplex.getTypes(last.getOffset()));
        assertNotNull(otherComplexType);
        assertEquals("MyCls", otherComplexType.getName());
        TypeScope foreign2Type = ModelUtils.getFirst(foreign2.getTypes(last.getOffset()));
        assertNull(foreign2Type);
    }

}
