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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.util.MapFormat;

/**
 *
 * @author Andrei Badea
 */
public class AnnotationScannerTest extends PersistenceTestCase {

    public AnnotationScannerTest(String testName) {
        super(testName);
    }

    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
    }

    public PrintStream getLog() {
        return System.err;
    }

    public void testScanTypes() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Customer.java",
                "@javax.persistence.Entity()" +
                "public class Customer { }");
        TestUtilities.copyStringToFileObject(srcFO, "Foo.java",
                "public class Foo { }");
        TestUtilities.copyStringToFileObject(srcFO, "Item.java",
                "@javax.persistence.Entity()" +
                "public class Item { }");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final Set<String> types = new HashSet<String>();
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws InterruptedException {
                helper.getAnnotationScanner().findAnnotations("javax.persistence.Entity", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() {
                    public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                        types.add(type.getQualifiedName().toString());
                    }
                });
                return null;
            }
        });
        assertEquals(2, types.size());
        assertTrue(types.contains("Customer"));
        assertTrue(types.contains("Item"));
    }

    public void testPerformance() throws Exception {
        // Logger.getLogger(AnnotationScanner.class.getName()).setLevel(Level.FINEST);
        final int ENTITY_COUNT = 500;
        String template = TestUtilities.copyStreamToString(getClass().getResourceAsStream("Table.javax"));
        Map<String, String> args = new HashMap<String, String>();
        MapFormat format = new MapFormat(args);
        format.setLeftBrace("__");
        format.setRightBrace("__");
        for (int i = 0; i < ENTITY_COUNT; i++) {
            String name = "Table" + i;
            args.put("NUM", Integer.toString(i));
            args.put("NAME", name);
            String contents = format.format(template);
            TestUtilities.copyStringToFileObject(srcFO, name + ".java", contents);
        }
        long startTime = System.nanoTime();
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        long compilationDoneTime = System.nanoTime();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final int[] entityCount = { 0 };
        final long initialScanDoneTime[] = { 0L };
        final List<ElementHandle<TypeElement>> typeHandles  = new ArrayList<ElementHandle<TypeElement>>();
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws InterruptedException {
                helper.getAnnotationScanner().findAnnotations("javax.persistence.Entity", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() {
                    public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                        typeHandles.add(ElementHandle.create(type));
                        entityCount[0]++;
                    }
                });
                initialScanDoneTime[0] = System.nanoTime();
                return null;
            }
        });
        assertEquals(ENTITY_COUNT, entityCount[0]);
        System.out.println("Compilation time (ms): " + (compilationDoneTime - startTime) / 1e6);
        System.out.println("Initial annotation scan time (ms): " + (initialScanDoneTime[0] - compilationDoneTime) / 1e6);
    }
    
    private ClasspathInfo createClasspathInfoForScanningAnnotations() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/MyClass.java",
                "package foo;" +
                "" +
                "import javax.annotation.Resource;" +
                "import javax.sql.DataSource;" +
                "" +
                "@Resource(name=\"myClass\")" +
                "public class MyClass {" +
                "   @Resource" +
                "   private DataSource myDS;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/YourClass.java",
                "package foo;" +
                "" +
                "import javax.annotation.Resource;" +
                "import javax.sql.DataSource;" +
                "" +
                "public class YourClass {" +
                "   @Resource" +
                "   private void setYourDataSource(DataSource ds) {" +
                "   }" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        return ClasspathInfo.create(srcFO);
    }
    
    public void testScanAnnotationsOnClassesMethodsFields() throws Exception {
        final AnnotationModelHelper helper = AnnotationModelHelper.create(createClasspathInfoForScanningAnnotations());
        final Set<String> elements = new HashSet<String>();
        
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws InterruptedException{
                helper.getAnnotationScanner().findAnnotations(
                        "javax.annotation.Resource",
                        EnumSet.of(ElementKind.CLASS, ElementKind.METHOD, ElementKind.FIELD),
                        new AnnotationHandler() {
                            public void handleAnnotation(TypeElement typeElement, Element element, AnnotationMirror annotationMirror) {
                                elements.add(element.getSimpleName().toString());
                            }
                        });
                return null;
            }
        });
        assertEquals(3, elements.size());
        assertTrue(elements.contains("MyClass"));
        assertTrue(elements.contains("myDS"));
        assertTrue(elements.contains("setYourDataSource"));
    }
    
    public void testScanAnnotationsOnMethodsFields() throws Exception {
        final AnnotationModelHelper helper = AnnotationModelHelper.create(createClasspathInfoForScanningAnnotations());
        final Set<String> elements = new HashSet<String>();
        
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws InterruptedException {
                helper.getAnnotationScanner().findAnnotations(
                        "javax.annotation.Resource",
                        EnumSet.of(ElementKind.METHOD, ElementKind.FIELD),
                        new AnnotationHandler() {
                            public void handleAnnotation(TypeElement typeElement, Element element, AnnotationMirror annotationMirror) {
                                elements.add(element.getSimpleName().toString());
                            }
                        });
                return null;
            }
        });
        assertEquals(2, elements.size());
        assertTrue(elements.contains("myDS"));
        assertTrue(elements.contains("setYourDataSource"));
    }
    
    public void testScanAnnotationsOnClasses() throws Exception {
        final AnnotationModelHelper helper = AnnotationModelHelper.create(createClasspathInfoForScanningAnnotations());
        final Set<String> elements = new HashSet<String>();
        
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws InterruptedException {
                helper.getAnnotationScanner().findAnnotations(
                        "javax.annotation.Resource",
                        EnumSet.of(ElementKind.CLASS),
                        new AnnotationHandler() {
                            public void handleAnnotation(TypeElement typeElement, Element element, AnnotationMirror annotationMirror) {
                                assertSame(typeElement, element);
                                elements.add(element.getSimpleName().toString());
                            }
                        });
                return null;
            }
        });
        assertEquals(1, elements.size());
        assertTrue(elements.contains("MyClass"));
    }
    
    public void testScanAnnotationsOnMethods() throws Exception {
        final AnnotationModelHelper helper = AnnotationModelHelper.create(createClasspathInfoForScanningAnnotations());
        final Set<String> elements = new HashSet<String>();
        
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws InterruptedException {
                helper.getAnnotationScanner().findAnnotations(
                        "javax.annotation.Resource",
                        EnumSet.of(ElementKind.METHOD),
                        new AnnotationHandler() {
                            public void handleAnnotation(TypeElement typeElement, Element element, AnnotationMirror annotationMirror) {
                                assertEquals("foo.YourClass", typeElement.getQualifiedName().toString());
                                elements.add(element.getSimpleName().toString());
                            }
                        });
                return null;
            }
        });
        assertEquals(1, elements.size());
        assertTrue(elements.contains("setYourDataSource"));
    }
    
    public void testScanAnnotationsOnFields() throws Exception {
        final AnnotationModelHelper helper = AnnotationModelHelper.create(createClasspathInfoForScanningAnnotations());
        final Set<String> elements = new HashSet<String>();
        
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws InterruptedException {
                helper.getAnnotationScanner().findAnnotations(
                        "javax.annotation.Resource",
                        EnumSet.of(ElementKind.FIELD),
                        new AnnotationHandler() {
                            public void handleAnnotation(TypeElement typeElement, Element element, AnnotationMirror annotationMirror) {
                                assertEquals("foo.MyClass", typeElement.getQualifiedName().toString());
                                elements.add(element.getSimpleName().toString());
                            }
                        });
                return null;
            }
        });
        assertEquals(1, elements.size());
        assertTrue(elements.contains("myDS"));
    }
}
