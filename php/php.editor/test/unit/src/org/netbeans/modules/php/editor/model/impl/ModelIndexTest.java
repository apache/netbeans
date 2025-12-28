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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous
 */
public class ModelIndexTest extends ModelTestBase {
    public ModelIndexTest(String testName) {
        super(testName);

    }
    private static final String FOLDER = "GsfPlugins";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        FileObject f = FileUtil.getConfigFile(FOLDER + "/text/html");

        if (f != null) {
            f.delete();
        }

        Logger.global.setFilter(new Filter() {

            public boolean isLoggable(LogRecord record) {
                Throwable t = record.getThrown();

                if (t == null) {
                    return true;
                }

                for (StackTraceElement e : t.getStackTrace()) {
                    if ("org.netbeans.modules.php.editor.index.GsfUtilities".equals(e.getClassName()) && "getBaseDocument".equals(e.getMethodName()) && t instanceof ClassNotFoundException) {
                        return false;
                    }
                }
                return false;
            }
        });
    }

    public void testModelFile1() throws Exception {
        TestModelTask task = new TestModelTask();
        task.addDeclaredMethodNamesForClass("clsModelTest1", "methodClsModelTest1");
        task.addDeclaredMethodNamesForClass("clsModelTest2", "methodClsModelTest2");
        task.addDeclaredMethodNamesForIface("ifaceModelTest1", "methodIfaceModelTest1");
        task.addDeclaredMethodNamesForIface("ifaceModelTest2", "methodIfaceModelTest2");

        task.addInheritedMethodNamesForClass("clsModelTest1", "methodIfaceModelTest1");
        task.addInheritedMethodNamesForClass("clsModelTest2", "methodClsModelTest1", "methodIfaceModelTest1", "methodIfaceModelTest2");
        task.addInheritedMethodNamesForIface("ifaceModelTest1");
        task.addInheritedMethodNamesForIface("ifaceModelTest2", "methodIfaceModelTest1");

        performModelTest(task, "testfiles/model/modelfile1.php");
    }

    void performModelTest(AbstractTestModelTask task, String testFilePath) throws Exception {
        FileObject testFile = getTestFile(testFilePath);
        Source testSource = getTestSource(testFile);
        Map<String, ClassPath> classPaths = createClassPathsForTest();
        if (classPaths == null || classPaths.isEmpty()) {
            ParserManager.parse(Collections.singleton(testSource), task);
        } else {
            Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
            if (!future.isDone()) {
                future.get();
            }
        }
    }

    static <T extends ModelElement> T getFirst(Collection<T> allElements,
            final String... elementName) {
        return ModelUtils.getFirst(allElements, elementName);
    }

    private static <T extends ModelElement> void assertAnyElement(Collection<? extends ModelElement> allElements, String... elemNames) {
        assertNotNull(allElements);
        ModelElement anyElement = null;
        for (int i = 0; i < elemNames.length; i++) {
            ModelElement elem = getFirst(allElements, elemNames[i]);
            if (elem != null) {
                anyElement = elem;
                break;
            }
        }
        assertNotNull(anyElement);
    }

    private static <T extends ModelElement> void assertAllElements(Collection<? extends ModelElement> allElements, String... elemNames) {
        assertNotNull(allElements);
        for (int i = 0; i < elemNames.length; i++) {
            ModelElement elem = getFirst(allElements, elemNames[i]);
            assertNotNull(elemNames[i], elem);
        }
    }

    private static <T extends ModelElement> void assertAllMethods(TypeScope typeScope, String... elemNames) {
        assertAllElements(typeScope.getMethods(), elemNames);
    }

    private static <T extends ModelElement> void assertExactMethods(TypeScope typeScope, String... elemNames) {
        assertExactElements(typeScope.getMethods(), elemNames);
    }

    private static <T extends ModelElement> void assertExactDeclaredMethods(TypeScope typeScope, String... elemNames) {
        assertExactElements(typeScope.getDeclaredMethods(), elemNames);
    }

    private static <T extends ModelElement> void assertExactInheritedMethods(TypeScope typeScope, String... elemNames) {
        assertExactElements(typeScope.getInheritedMethods(), elemNames);
    }

    private static <T extends ModelElement> void assertExactElements(Collection<? extends ModelElement> allElements, String... elemNames) {
        assertNotNull(allElements);
        List<ModelElement> testedElems = new ArrayList<ModelElement>();
        for (int i = 0; i < elemNames.length; i++) {
            ModelElement elem = getFirst(allElements, elemNames[i]);
            assertNotNull(elemNames[i], elem);
            testedElems.add(elem);
        }
        List<ModelElement> notTestedElems = new ArrayList<ModelElement>(allElements);
        notTestedElems.removeAll(testedElems);
        assertTrue(notTestedElems.toString(), notTestedElems.isEmpty());
        assertEquals(allElements.size(), elemNames.length);
    }

    private static <T> void assertNotEmpty(final Collection<? extends T> collection) {
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
    }

    protected static String computeFileName(int index) {
        return "test" + (index == (-1) ? "" : (char) ('a' + index)) + ".php";
    }

    abstract class AbstractTestModelTask extends UserTask {

        public void cancel() {
        }

        public void run (ResultIterator resultIterator) throws Exception {
            PHPParseResult parameter = (PHPParseResult) resultIterator.getParserResult();
            if (parameter != null) {
                Model model = ModelFactory.getModel(parameter);
                assertNotNull(model);
                FileScope fileScope = model.getFileScope();
                assertNotNull(fileScope);
                IndexScope indexScope = fileScope.getIndexScope();
                assertNotNull(indexScope);
                testIndexScope(model, fileScope, indexScope);
                testFileScope(model, fileScope, indexScope);
                for (ClassScope classScope : ModelUtils.getDeclaredClasses(fileScope)) {
                    testClassScope(model, fileScope, classScope, indexScope);
                }
                for (InterfaceScope ifaceScope : ModelUtils.getDeclaredInterfaces(fileScope)) {
                    testIfaceScope(model, fileScope, ifaceScope, indexScope);
                }
            }
        }
        abstract void testIndexScope(Model model, FileScope fileScope, IndexScope indexScope);

        abstract void testFileScope(Model model, FileScope fileScope, IndexScope indexScope);

        abstract void testClassScope(Model model, FileScope fileScope, ClassScope classScope, IndexScope indexScope);

        abstract void testIfaceScope(Model model, FileScope fileScope, InterfaceScope classScope, IndexScope indexScope);
    }

    enum MemberType {

        DECLARED, INHERITED, ALL
    };

    private class TestModelTask extends AbstractTestModelTask {

        private Set<String> clsNames = new LinkedHashSet<String>();
        private Set<String> ifaceNames = new LinkedHashSet<String>();
        private Map<String, List<String>> declaredClsMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> declaredIfaceMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> inheritedClsMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> inheritedIfaceMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> allClsMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> allIfaceMethods = new LinkedHashMap<String, List<String>>();

        void addDeclaredMethodNamesForClass(String typeName, String... methodNames) {
            addMethodNames(typeName, true, MemberType.DECLARED, methodNames);
        }

        void addDeclaredMethodNamesForIface(String typeName, String... methodNames) {
            addMethodNames(typeName, false, MemberType.DECLARED, methodNames);
        }

        void addInheritedMethodNamesForClass(String typeName, String... methodNames) {
            addMethodNames(typeName, true, MemberType.INHERITED, methodNames);
        }

        void addInheritedMethodNamesForIface(String typeName, String... methodNames) {
            addMethodNames(typeName, false, MemberType.INHERITED, methodNames);
        }

        private void addMethodNames(String typeName, boolean isClass, MemberType memberType, String... methodNames) {
            Set<String> typeNames = isClass ? clsNames : ifaceNames;
            Map<String, List<String>> allMethods = isClass ? allClsMethods : allIfaceMethods;
            Map<String, List<String>> typeMethods = null;
            switch (memberType) {
                case ALL:
                    typeMethods = isClass ? allClsMethods : allIfaceMethods;
                    break;
                case DECLARED:
                    typeMethods = isClass ? declaredClsMethods : declaredIfaceMethods;
                    break;
                case INHERITED:
                    typeMethods = isClass ? inheritedClsMethods : inheritedIfaceMethods;
                    break;
                default:
                    fail();
            }
            List<String> methNameList = typeMethods.get(typeName);
            if (methNameList == null) {
                methNameList = new ArrayList<String>();
                typeMethods.put(typeName, methNameList);
            }

            typeNames.add(typeName);
            methNameList.addAll(Arrays.asList(methodNames));

            List<String> allMethNameList = allMethods.get(typeName);
            if (allMethNameList == null) {
                allMethNameList = new ArrayList<String>();
                allMethods.put(typeName, allMethNameList);
            }

            allMethNameList.addAll(Arrays.asList(methodNames));

        }

        @Override
        void testIndexScope(Model model, FileScope fScope, IndexScope iScope) {
            Collection<? extends ClassScope> declaredClasses = ModelUtils.getDeclaredClasses(fScope);
            for (ClassScope classScope : declaredClasses) {
                assertNotNull(classScope.getName(), getFirst(iScope.findClasses(classScope.getFullyQualifiedName())));
            }
        }

        @Override
        void testFileScope(Model model, FileScope fScope, IndexScope iScope) {
            assertExactElements(ModelUtils.getDeclaredClasses(fScope), getClsNames());
            assertExactElements(ModelUtils.getDeclaredInterfaces(fScope), getIfaceNames());
        }

        @Override
        void testClassScope(Model model, FileScope fileScope, ClassScope clsScope, IndexScope indexScope) {
            assertDeclaredMethodNames(clsScope, declaredClsMethods);
            assertInheritedMethodNames(clsScope, inheritedClsMethods);
            assertMethodNames(clsScope, allClsMethods);
        }

        @Override
        void testIfaceScope(Model model, FileScope fileScope, InterfaceScope ifaceScope, IndexScope indexScope) {
            assertDeclaredMethodNames(ifaceScope, declaredIfaceMethods);
            assertInheritedMethodNames(ifaceScope, inheritedIfaceMethods);
            assertMethodNames(ifaceScope, allIfaceMethods);
        }

        private void assertDeclaredMethodNames(TypeScope typeScope, Map<String, List<String>> methods) {
            String[] typeNames = methods.keySet().toArray(new String[methods.size()]);
            assertAnyElement(Collections.singletonList(typeScope), typeNames);
            String name = typeScope.getName();
            for (int i = 0; i < typeNames.length; i++) {
                String iName = typeNames[i];
                if (name.equals(iName)) {
                    List<String> names = methods.get(iName);

                    assertExactDeclaredMethods(typeScope, names.toArray(new String[0]));
                }
            }
        }

        private void assertInheritedMethodNames(TypeScope typeScope, Map<String, List<String>> methods) {
            String[] typeNames = methods.keySet().toArray(new String[methods.size()]);
            assertAnyElement(Collections.singletonList(typeScope), typeNames);
            String name = typeScope.getName();
            for (int i = 0; i < typeNames.length; i++) {
                String iName = typeNames[i];
                if (name.equals(iName)) {
                    List<String> names = methods.get(iName);
                    assertExactInheritedMethods(typeScope, names.toArray(new String[0]));
                }
            }
        }

        private void assertMethodNames(TypeScope typeScope, Map<String, List<String>> methods) {
            String[] typeNames = methods.keySet().toArray(new String[methods.size()]);
            assertAnyElement(Collections.singletonList(typeScope), typeNames);
            String name = typeScope.getName();
            for (int i = 0; i < typeNames.length; i++) {
                String iName = typeNames[i];
                if (name.equals(iName)) {
                    List<String> names = methods.get(iName);
                    assertExactMethods(typeScope, names.toArray(new String[0]));
                }
            }
        }

        private String[] getClsNames() {
            return clsNames.toArray(new String[0]);
        }

        private String[] getIfaceNames() {
            return ifaceNames.toArray(new String[0]);
        }
    }
}
