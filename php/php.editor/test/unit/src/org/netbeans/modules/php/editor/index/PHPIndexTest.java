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
package org.netbeans.modules.php.editor.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.junit.Test;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.Exact;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.AliasedElement;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.csl.PHPNavTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous
 */
public class PHPIndexTest extends PHPNavTestBase {

    private ElementQuery.Index index;

    public PHPIndexTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Source source = getTestSource();
        Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                final ParserResult parserResult = (ParserResult) resultIterator.getParserResult();
                if (parserResult != null) {
                    QuerySupport querySupport = QuerySupportFactory.get(parserResult);
                    index = ElementQueryFactory.createIndexQuery(querySupport);
                }
            }
        });
        if (!future.isDone()) {
            future.get();
        }
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses() throws Exception {
        checkIndexer(getTestPath());
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses_all() throws Exception {
        Collection<String> classNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getClasses(NameKind.empty()));
        assertEquals(classNames.size(), allTypes.size());
        for (TypeElement indexedClass : allTypes) {
            assertTrue(classNames.contains(indexedClass.getName()));
            assertEquals(indexedClass, indexedClass);
            assertEquals(PhpElementKind.CLASS, indexedClass.getPhpElementKind());
        }
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses_exact() throws Exception {
        Collection<String> classNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getClasses(NameKind.empty()));
        assertEquals(classNames.size(), allTypes.size());
        for (String clsName : classNames) {
            Collection<ClassElement> classes = index.getClasses(NameKind.exact(clsName));
            assertTrue(classes.size() > 0);
            for (ClassElement indexedClass : classes) {
                assertEquals(clsName, indexedClass.getName());
                assertTrue(classNames.contains(indexedClass.getName()));
                assertTrue(allTypes.contains(indexedClass));
            }
        }
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses_prefix() throws Exception {
        Collection<String> classNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getClasses(NameKind.empty()));
        assertEquals(classNames.size(), allTypes.size());
        for (String clsName : classNames) {
            Collection<ClassElement> classes = index.getClasses(NameKind.prefix(clsName.substring(0, 1)));
            assertTrue(classes.size() > 0);
            for (ClassElement indexedClass : classes) {
                assertEquals(clsName, indexedClass.getName());
                assertTrue(classNames.contains(indexedClass.getName()));
                assertTrue(allTypes.contains(indexedClass));
            }
        }
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses_preferred() throws Exception {
        Collection<TypeElement> ccClasses = new ArrayList<TypeElement>(index.getClasses(NameKind.exact("CCC")));
        assertEquals(2, ccClasses.size());
        TypeElement[] classesArray = ccClasses.toArray(new TypeElement[0]);
        final TypeElement firstCC = classesArray[0];
        final TypeElement secondCC = classesArray[1];
        assertNotNull(firstCC);
        assertNotNull(secondCC);
        assertNotSame(secondCC, firstCC);
        assertNotNull(firstCC.getFileObject());
        assertNotNull(secondCC.getFileObject());
        assertNotSame(secondCC.getFileObject(), firstCC.getFileObject());
        TypeElement testingCC = "testGetClasses_1.php".equals(firstCC.getFileObject().getNameExt()) ? firstCC : secondCC;
        final Collection<ClassElement> preferredClasses =
                ElementFilter.forFiles(testingCC.getFileObject()).prefer(index.getClasses(NameKind.exact("CCC")));
        assertEquals(1, preferredClasses.size());
        final ClassElement preffered = getFirst(preferredClasses);
        assertEquals(testingCC, preffered);
        assertEquals(testingCC.getFileObject(), preffered.getFileObject());

        final Collection<ClassElement> aaClasses =
                ElementFilter.forFiles(preffered.getFileObject()).prefer(index.getClasses(NameKind.exact("AAA")));
        assertEquals(1, aaClasses.size());
        assertNotSame(getFirst(aaClasses).getFileObject(), preffered.getFileObject());
    }

    private static <T extends PhpElement> T getFirst(Collection<T> classes) {
        final Iterator<T> iterator = classes.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    private static <T extends PhpElement> T getSecond(Collection<T> classes) {
        final Iterator<T> iterator = classes.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            return iterator.hasNext() ? iterator.next() : null;
        }
        return null;
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces() throws Exception {
        checkIndexer(getTestPath());
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces_all() throws Exception {
        Collection<String> typeNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getInterfaces(NameKind.empty()));
        assertEquals(typeNames.size(), allTypes.size());
        for (TypeElement indexedIface : allTypes) {
            assertTrue(typeNames.contains(indexedIface.getName()));
            assertEquals(indexedIface, indexedIface);
            assertEquals(PhpElementKind.IFACE, indexedIface.getPhpElementKind());
        }
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces_exact() throws Exception {
        Collection<String> typeNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getInterfaces(NameKind.empty()));
        assertEquals(typeNames.size(), allTypes.size());
        for (String clsName : typeNames) {
            Collection<InterfaceElement> ifaces = index.getInterfaces(NameKind.exact(clsName));
            assertTrue(ifaces.size() > 0);
            for (InterfaceElement indexedIface : ifaces) {
                assertEquals(clsName, indexedIface.getName());
                assertTrue(typeNames.contains(indexedIface.getName()));
                assertTrue(allTypes.contains(indexedIface));
            }
        }
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces_prefix() throws Exception {
        Collection<String> classNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getInterfaces(NameKind.empty()));
        assertEquals(classNames.size(), allTypes.size());
        for (String typeName : classNames) {
            Collection<InterfaceElement> classes = index.getInterfaces(NameKind.prefix(typeName.substring(0, 1)));
            assertTrue(classes.size() > 0);
            for (InterfaceElement indexedInterfaces : classes) {
                assertEquals(typeName, indexedInterfaces.getName());
                assertTrue(classNames.contains(indexedInterfaces.getName()));
                assertTrue(allTypes.contains(indexedInterfaces));
            }
        }
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces_preferred() throws Exception {
        Collection<TypeElement> ccInterfaces = new ArrayList<TypeElement>(index.getInterfaces(NameKind.exact("CCC")));
        assertEquals(2, ccInterfaces.size());
        TypeElement[] interfacesArray = ccInterfaces.toArray(new TypeElement[0]);
        TypeElement firstCC = interfacesArray[0];
        TypeElement secondCC = interfacesArray[1];
        assertNotNull(firstCC);
        assertNotNull(secondCC);
        assertNotSame(secondCC, firstCC);
        assertNotNull(firstCC.getFileObject());
        assertNotNull(secondCC.getFileObject());
        assertNotSame(secondCC.getFileObject(), firstCC.getFileObject());

        if (firstCC.getFileObject().getName().endsWith("_1")) {
            final TypeElement tmpCC = firstCC;
            firstCC = secondCC;
            secondCC = tmpCC;
        }

        final Collection<InterfaceElement> preferredInterfaces =
                ElementFilter.forFiles(firstCC.getFileObject()).prefer(index.getInterfaces(NameKind.exact("CCC")));
        assertEquals(1, preferredInterfaces.size());
        final InterfaceElement preffered = getFirst(preferredInterfaces);
        assertEquals(firstCC, preffered);
        assertEquals(firstCC.getFileObject(), preffered.getFileObject());

        final Collection<InterfaceElement> aaInterfaces =
                ElementFilter.forFiles(preffered.getFileObject()).prefer(index.getInterfaces(NameKind.exact("AAA")));
        assertEquals(1, aaInterfaces.size());
        assertSame(getFirst(aaInterfaces).getFileObject(), preffered.getFileObject());
    }

    // NETBEANS-5599 PHP 8.1 Support
    public void testGetEnums() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testGetEnums_all() throws Exception {
        Collection<String> enumNames = Arrays.asList(
                "Simple1", "Simple1", "Simple2", "BackedCaseInt1", "BackedCaseInt2", "BackedCaseString1", "BackedCaseString2",
                "Impl", "Attributes", "WithTrait"
        );
        Collection<TypeElement> allTypes = new ArrayList<>(index.getEnums(NameKind.empty()));
        assertEquals(enumNames.size(), allTypes.size());
        for (TypeElement indexedEnum : allTypes) {
            assertTrue(enumNames.contains(indexedEnum.getName()));
            assertEquals(indexedEnum, indexedEnum);
            assertEquals(PhpElementKind.ENUM, indexedEnum.getPhpElementKind());
        }

        for (TypeElement indexedEnum : allTypes) {
            Collection<EnumCaseElement> declaredEnumCases = new ArrayList<>(index.getDeclaredEnumCases(indexedEnum));
            switch (indexedEnum.getName()) {
                case "Simple1":
                    assertTrue(declaredEnumCases.isEmpty());
                    Set<EnumCaseElement> enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.empty());
                    assertTrue(enumCases.isEmpty());
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.exact("A"));
                    assertTrue(enumCases.isEmpty());
                    break;

                case "Simple2":
                    assertTrue(declaredEnumCases.size() == 5);
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.empty());
                    assertTrue(enumCases.size() == 5);
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.exact("A"));
                    assertTrue(enumCases.size() == 1);
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.prefix("A"));
                    assertTrue(enumCases.size() == 2);
                    break;
                case "BackedCaseInt1":
                    assertTrue(declaredEnumCases.isEmpty());
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.empty());
                    assertTrue(enumCases.isEmpty());
                    break;
                case "BackedCaseInt2":
                    assertTrue(declaredEnumCases.size() == 6);
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.empty());
                    assertTrue(enumCases.size() == 6);
                    break;
                case "BackedCaseString1":
                    assertTrue(declaredEnumCases.isEmpty());
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.empty());
                    assertTrue(enumCases.isEmpty());
                    break;
                case "BackedCaseString2":
                    assertTrue(declaredEnumCases.size() == 7);
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.empty());
                    assertTrue(enumCases.size() == 7);
                    break;
                case "Impl":
                    assertTrue(declaredEnumCases.size() == 3);
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.empty());
                    assertTrue(enumCases.size() == 3);
                    break;
                case "Attributes":
                    assertTrue(declaredEnumCases.size() == 2);
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.empty());
                    assertTrue(enumCases.size() == 2);
                    break;
                case "WithTrait":
                    assertTrue(declaredEnumCases.size() == 2);
                    enumCases = index.getEnumCases(NameKind.exact(indexedEnum.getName()), NameKind.empty());
                    assertTrue(enumCases.size() == 2);
                    break;
                default:
                    assert false : indexedEnum.getName();
            }
        }
        Set<EnumCaseElement> enumCases = index.getEnumCases(NameKind.empty());
        assertTrue(enumCases.size() == 5 + 6 + 7 + 3 + 2 + 2);
    }

    public void testGetEnums_exact() throws Exception {
        Collection<String> enumNames = Arrays.asList(
                "Simple1", "Simple1", "Simple2", "BackedCaseInt1", "BackedCaseInt2", "BackedCaseString1", "BackedCaseString2",
                "Impl", "Attributes", "WithTrait"
        );
        Collection<TypeElement> allTypes = new ArrayList<>(index.getEnums(NameKind.empty()));
        assertEquals(enumNames.size(), allTypes.size());
        for (String enumName : enumNames) {
            Collection<EnumElement> enums = index.getEnums(NameKind.exact(enumName));
            assertTrue(!enums.isEmpty());
            for (EnumElement indexedEnum : enums) {
                assertEquals(enumName, indexedEnum.getName());
                assertTrue(enumNames.contains(indexedEnum.getName()));
                assertTrue(allTypes.contains(indexedEnum));
            }
        }
    }

    public void testGetEnums_prefix() throws Exception {
        Collection<String> enumNames = Arrays.asList(
                "Simple1", "Simple1", "Simple2", "BackedCaseInt1", "BackedCaseInt2", "BackedCaseString1", "BackedCaseString2",
                "Impl", "Attributes", "WithTrait"
        );
        Collection<TypeElement> allTypes = new ArrayList<>(index.getEnums(NameKind.empty()));
        assertEquals(enumNames.size(), allTypes.size());
        for (String enumName : enumNames) {
            Collection<EnumElement> enums = index.getEnums(NameKind.prefix(enumName.substring(0, 1)));
            assertTrue(!enums.isEmpty());
            for (EnumElement indexedEnum : enums) {
                assertTrue(enumNames.contains(indexedEnum.getName()));
                assertTrue(allTypes.contains(indexedEnum));
            }
        }
    }

    public void testGetEnums_preferred() throws Exception {
        Collection<TypeElement> enums1 = new ArrayList<>(index.getEnums(NameKind.exact("Simple1")));
        assertEquals(2, enums1.size());
        TypeElement[] enumsArray = enums1.toArray(new TypeElement[0]);
        final TypeElement first = enumsArray[0];
        final TypeElement second = enumsArray[1];
        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(second, first);
        assertNotNull(first.getFileObject());
        assertNotNull(second.getFileObject());
        assertNotSame(second.getFileObject(), first.getFileObject());
        TypeElement testingCC = "testGetEnums_1.php".equals(first.getFileObject().getNameExt()) ? first : second;
        final Collection<EnumElement> preferredClasses
                = ElementFilter.forFiles(testingCC.getFileObject()).prefer(index.getEnums(NameKind.exact("Simple1")));
        assertEquals(1, preferredClasses.size());
        final EnumElement preffered = getFirst(preferredClasses);
        assertEquals(testingCC, preffered);
        assertEquals(testingCC.getFileObject(), preffered.getFileObject());

        final Collection<EnumElement> emuns2
                = ElementFilter.forFiles(preffered.getFileObject()).prefer(index.getEnums(NameKind.exact("BackedCaseString1")));
        assertEquals(1, emuns2.size());
        assertNotSame(getFirst(emuns2).getFileObject(), preffered.getFileObject());
    }

    /**
     * Test of getFunctions method, of class PHPIndex.
     */
    @Test
    public void testGetFunctions() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testGetFunctions_parameters() throws Exception {
        final Exact fncName = NameKind.exact("af");
        Set<FunctionElement> functions = index.getFunctions(fncName);
        FunctionElement fncA = getFirst(functions);
        assertNotNull(fncA);
        assertEquals(fncName.getQueryName(), fncA.getName());

        List<ParameterElement> parameters = fncA.getParameters();
        assertEquals(parameters.size(), 4);

        final ParameterElement firstParam = parameters.get(1);
        assertTrue(firstParam.isMandatory());
        assertTrue(firstParam.hasDeclaredType());
        assertEquals(1, firstParam.getTypes().size());
        TypeResolver firstType = firstParam.getTypes().iterator().next();
        assertTrue(firstType.isResolved());
        assertTrue(firstType.canBeResolved());
        assertEquals("ParameterIface", firstType.getRawTypeName());

        final ParameterElement secondParam = parameters.get(0);
        assertTrue(secondParam.isMandatory());
        assertTrue(secondParam.hasDeclaredType());
        assertEquals(1, secondParam.getTypes().size());
        TypeResolver secondType = secondParam.getTypes().iterator().next();
        assertTrue(secondType.isResolved());
        assertTrue(secondType.canBeResolved());
        assertEquals("ParameterClass", secondType.getRawTypeName());

        final ParameterElement thirdParam = parameters.get(2);
        assertFalse(thirdParam.isMandatory());
        assertEquals("\"test\"", thirdParam.getDefaultValue());
        assertFalse(thirdParam.hasDeclaredType());
        assertEquals(0, thirdParam.getTypes().size());

        final ParameterElement fourthParam = parameters.get(3);
        assertFalse(fourthParam.isMandatory());
        assertEquals("MY_CONST", fourthParam.getDefaultValue());
        assertFalse(fourthParam.hasDeclaredType());
        assertEquals(0, thirdParam.getTypes().size());
    }

    /**
     * Test of getFunctions method, of class PHPIndex.
     */
    @Test
    public void testGetFunctions_all() throws Exception {
        Collection<String> fncNames = Arrays.asList(new String[]{"af", "bf", "cf", "cf", "df"});
        Collection<FunctionElement> allFunctions = index.getFunctions(NameKind.empty());
        assertEquals(fncNames.size(), allFunctions.size());
        for (FunctionElement indexedFunction : allFunctions) {
            assertTrue(fncNames.contains(indexedFunction.getName()));
            assertEquals(indexedFunction, indexedFunction);
            assertEquals(PhpElementKind.FUNCTION, indexedFunction.getPhpElementKind());
        }
    }

    /**
     * Test of getFunctions method, of class PHPIndex.
     */
    @Test
    public void testGetFunctions_exact() throws Exception {
        Collection<String> fncNames = Arrays.asList(new String[]{"af", "bf", "cf", "cf", "df"});
        Collection<FunctionElement> allFunctions = index.getFunctions(NameKind.empty());
        assertEquals(fncNames.size(), allFunctions.size());
        for (String fnName : fncNames) {
            Collection<FunctionElement> functions = index.getFunctions(NameKind.exact(fnName));
            assertTrue(functions.size() > 0);
            for (FunctionElement indexedFnc : functions) {
                assertEquals(fnName, indexedFnc.getName());
                assertTrue(fncNames.contains(indexedFnc.getName()));
                assertTrue(allFunctions.contains(indexedFnc));
            }
        }
    }

    /**
     * Test of getFunctionsmethod, of class PHPIndex.
     */
    @Test
    public void testGetFunctions_prefix() throws Exception {
        Collection<String> fncNames = Arrays.asList(new String[]{"af", "bf", "cf", "cf", "df"});
        Collection<FunctionElement> allFunctions = index.getFunctions(NameKind.empty());
        assertEquals(fncNames.size(), allFunctions.size());
        for (String fncName : fncNames) {
            Collection<FunctionElement> functions = index.getFunctions(NameKind.prefix(fncName.substring(0, 1)));
            assertTrue(functions.size() > 0);
            for (FunctionElement indexedFunction : functions) {
                assertEquals(fncName, indexedFunction.getName());
                assertTrue(fncNames.contains(indexedFunction.getName()));
                assertTrue(allFunctions.contains(indexedFunction));
            }
        }
    }

    @Test
    public void testGetMethods() throws Exception {
        checkIndexer(getTestPath());
    }

    @Test
    public void testGetMethods_NameKind_empty() {
        Collection<String> methodNames = Arrays.asList(
                new String[]{"testMethodDeclaration",
                    "testMethodDeclarationIface1",
                    "testMethodDeclarationIface2",
                    "testMethodDeclarationNext1",
                    "testMethodDeclarationNext2"});
        Collection<MethodElement> allMethods = index.getMethods(NameKind.empty());
        assertTrue(allMethods.size() >= methodNames.size());
        for (String methName : methodNames) {
            Set<MethodElement> methods = index.getMethods(NameKind.exact(methName));
            assertTrue(methods.size() > 0);
            for (MethodElement indexedMethod : methods) {
                assertEquals(methName, indexedMethod.getName());
                assertTrue(methodNames.contains(indexedMethod.getName()));
                assertTrue(allMethods.contains(indexedMethod));
            }
        }
    }

    @Test
    public void testGetMethods_TypeElement() {
        Set<ClassElement> classes = index.getClasses(NameKind.exact("testMethodDeclaration"));
        assertEquals(1, classes.size());
        ClassElement clz = getFirst(classes);
        Set<MethodElement> methods = index.getDeclaredMethods(clz);
        assertEquals(1, methods.size());
        methods = index.getInheritedMethods(clz);
        assertEquals(0, methods.size());

        classes = index.getClasses(NameKind.exact("testMethodDeclaration_1"));
        assertEquals(1, classes.size());
        clz = getFirst(classes);
        Set<TypeElement> inheritedTypes = index.getInheritedTypes(clz);
        assertEquals(2, inheritedTypes.size());

        methods = index.getDeclaredMethods(clz);
        assertEquals(3, methods.size());
        methods = index.getInheritedMethods(clz);
        assertEquals(3, methods.size());
        MethodElement firstMethod = getFirst(methods);
        TypeElement firstType = firstMethod.getType();
        final boolean isFirstTypeClass = firstType.getPhpElementKind().equals(PhpElementKind.CLASS);
        if (isFirstTypeClass) {
            assertEquals(firstType.getName(), clz.getSuperClassName().getName());
            assertEquals(getSecond(methods).getType().getName(), clz.getSuperInterfaces().iterator().next().getName());
            assertEquals(firstMethod.getName(), "testMethodDeclaration");
        } else {
            assertEquals(firstType.getName(), clz.getSuperInterfaces().iterator().next().getName());
            assertEquals(getSecond(methods).getType().getName(), clz.getSuperClassName().getName());
            assertTrue(firstMethod.getName().startsWith("testMethodDeclarationIface"));
        }
        Collection<String> methodNames = Arrays.asList(
                new String[]{"testMethodDeclaration",
                    "testMethodDeclarationIface1",
                    "testMethodDeclarationIface2",
                    "testMethodDeclarationNext1",
                    "testMethodDeclarationNext2"});
        Collection<MethodElement> allMethods = index.getAccessibleMethods(clz, clz);
        assertTrue(allMethods.size() >= methodNames.size());
        for (String methName : methodNames) {
            Set<MethodElement> meths = index.getMethods(NameKind.exact(methName));
            assertTrue(meths.size() > 0);
            for (MethodElement indexedMethod : meths) {
                assertEquals(methName, indexedMethod.getName());
                assertTrue(methodNames.contains(indexedMethod.getName()));
                if (indexedMethod.getType().equals(clz)) {
                    assertTrue(allMethods.contains(indexedMethod));
                }
            }
        }
    }

    @Test
    public void testGetNamespaces() throws Exception {
        checkIndexer(getTestPath());
    }

    /*@Test
    public void testGetNamespaces_NameKind() throws Exception {
        Collection<String> nsNames = Arrays.asList(new String[]{
            "\\my\\name",
            "\\your\\name",
            "\\their\\name",
            "\\our\\name",
            "\\my\\surname",
            "\\your\\surname",
            "\\their\\surname",
            "\\our\\surname"
        });
        Set<NamespaceElement> allNamespaces = index.getNamespaces(NameKind.empty());
        assertEquals(nsNames.size(), allNamespaces.size());
        for (NamespaceElement ns : allNamespaces) {
            Set<NamespaceElement> namespaces = index.getNamespaces(NameKind.exact(ns.getFullyQualifiedName()));
            assertEquals(1, namespaces.size());
            NamespaceElement firstNs = getFirst(namespaces);
            assertEquals(firstNs.getFullyQualifiedName(), ns.getFullyQualifiedName());
            assertTrue(nsNames.contains(firstNs.getFullyQualifiedName().toString()));
        }

        assertEquals(1, index.getNamespaces(NameKind.prefix(QualifiedName.create("\\my\\na"))).size());
        assertEquals(2, index.getNamespaces(NameKind.prefix(QualifiedName.create("\\my\\"))).size());
        assertEquals(1, index.getNamespaces(NameKind.prefix(QualifiedName.createFullyQualified("n", "my"))).size());
        assertEquals(2, index.getNamespaces(NameKind.prefix(QualifiedName.createFullyQualified("", "my"))).size());
        assertEquals(4, index.getNamespaces(NameKind.prefix(QualifiedName.create("nam"))).size());
        assertEquals(4, index.getNamespaces(NameKind.prefix(QualifiedName.create("sur"))).size());

    }*/

    /**
     * Test of getConstants method, of class PHPIndex.
     */
    @Test
    public void testGetConstants() {
    }


    /**
     * Test of getTopLevelVariables method, of class PHPIndex.
     */
    @Test
    public void testGetTopLevelVariables() {
    }

    /**
     * Test of getAllTopLevel method, of class PHPIndex.
     */
    @Test
    public void testGetAllTopLevel_ElementQuery() {
    }

    /**
     * Test of getAllTopLevel method, of class PHPIndex.
     */
    @Test
    public void testGetAllTopLevel_ElementQuery_EnumSet() {
    }

    /**
     * Test of getClassAncestors method, of class PHPIndex.
     */
    @Test
    public void testGetClassAncestors() {
    }

    /**
     * Test of getFiles method, of class PHPIndex.
     */
    @Test
    public void testGetFiles() {
    }

    /**
     * Test of getDirectIncludes method, of class PHPIndex.
     */
    @Test
    public void testGetDirectIncludes() {
    }

    @Test
    public void testGetClassesWithNsInterfaces() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testIssue240824() throws Exception {
        checkIndexer(getTestPath());
    }

    // PHP7.1
    public void testNullableTypesForFunctions() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testNullableTypesForMethods() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testClassConstantVisibility() throws Exception {
        checkIndexer(getTestPath());
    }

    // PHP 7.4
    public void testPHP74TypedPropertiesClass() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testPHP74TypedPropertiesTrait() throws Exception {
        checkIndexer(getTestPath());
    }

    // #241740
    public void testMixin() throws Exception {
        checkIndexer(getTestPath());
    }

    // NETBEANS-4443 PHP 8.0
    public void testPHP80UnionTypesFunctions() throws Exception {
        // function, lambda function, arrow function
        checkIndexer(getTestPath());
    }

    public void testPHP80UnionTypesTypes() throws Exception {
        // class, abstract class, interface, trait
        checkIndexer(getTestPath());
    }

    public void testPHP80MixedReturnType() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testPHP80ConstructorPropertyPromotion() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testPHP80AttributeClasses() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testGetAttributeClasses_all() throws Exception {
        Collection<String> classNames = Arrays.asList(new String[]{
            "AttrGlobal1",
            "AttrGlobal2",
            "AttrA1",
            "AttrA2",
            "AttrB1",
            "AttrB2",
            "AttrB3",
        });
        Collection<TypeElement> allTypes = new ArrayList<>(index.getAttributeClasses(NameKind.empty(), Collections.emptySet(), AliasedElement.Trait.ALIAS));
        assertEquals(classNames.size(), allTypes.size());
        for (TypeElement indexedClass : allTypes) {
            assertTrue(classNames.contains(indexedClass.getName()));
            assertEquals(PhpElementKind.CLASS, indexedClass.getPhpElementKind());
        }
    }

    public void testGetAttributeClasses_prefix() throws Exception {
        HashMap<String, List<String>> map = new HashMap<>();
        map.put("Global", Arrays.asList("AttrGlobal1", "AttrGlobal2"));
        map.put("A", Arrays.asList("AttrA1", "AttrA2"));
        map.put("B", Arrays.asList("AttrB1", "AttrB2", "AttrB3"));
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            Collection<TypeElement> types = new ArrayList<>(index.getAttributeClasses(NameKind.prefix("Attr" + key), Collections.emptySet(), AliasedElement.Trait.ALIAS));
            assertEquals(values.size(), types.size());
            for (TypeElement type : types) {
                assertTrue(values.contains(type.getName()));
                assertEquals(PhpElementKind.CLASS, type.getPhpElementKind());
            }
        }
        Collection<TypeElement> types = new ArrayList<>(index.getAttributeClasses(NameKind.prefix("NotAttr"), Collections.emptySet(), AliasedElement.Trait.ALIAS));
        assertTrue(types.isEmpty());
    }

    public void testPHP81PureIntersectionTypes() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testPHP82ReadonlyClasses() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testPHP82ConstantsInTraits() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testPHP82DNFReturnTypes() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testPHP82DNFParameterTypes() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testPhpDocParameterTypes() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testPHP83TypedClassConstants() throws Exception {
        checkIndexer(getTestPath());
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        final File folder = new File(getDataDir(), getTestFolderPath());
        return new FileObject[]{FileUtil.toFileObject(folder)};
    }

    protected Source getTestSource() {
        final File file = new File(getDataDir(), getTestPath());
        FileObject fileObject = FileUtil.toFileObject(file);
        return Source.create(fileObject);
    }

    private String getTestFolderPath() {
        return "testfiles/index/" + getTestName();//NOI18N
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestName() + ".php";//NOI18N
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }
}
