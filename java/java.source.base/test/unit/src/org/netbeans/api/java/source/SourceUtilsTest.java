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

package org.netbeans.api.java.source;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.modules.java.source.ClassIndexTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.parsing.ClassParser;
import org.netbeans.modules.java.source.parsing.ParameterNameProviderImpl;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.RunWhenScanFinishedSupport;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class SourceUtilsTest extends ClassIndexTestCase {

    private JavaSource js;
    private CompilationInfo info;

    static {
        System.setProperty("org.openide.util.Lookup", SourceUtilsTestUtil.class.getName());
    }

    public SourceUtilsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(
                new String[0],
                new Object[] {
                    SFBQImpl.getDefault(),
                    CPProvider.getDefault()
                });
    }

    public void testGetEnclosingTypeElement() throws Exception {
        //only a scatch of the test, add testcases as needed:
        prepareTest();

        TypeElement test = info.getElements().getTypeElement("sourceutils.TestGetEnclosingTypeElement");

        assertEquals("TestGetEnclosingTypeElement.java", SourceUtils.findSourceFileName(test));

        assertNotNull(test);

        ExecutableElement testMethod = ElementFilter.methodsIn(test.getEnclosedElements()).get(0);
//        TypeElement classInMethod = ElementFilter.typesIn(testMethod.getEnclosedElements()).get(0);
//        ExecutableElement classInMethodMethod = ElementFilter.methodsIn(classInMethod.getEnclosedElements()).get(0);
//        VariableElement classInMethodField = ElementFilter.fieldsIn(classInMethod.getEnclosedElements()).get(0);
//        TypeElement classInMethodNestedClass = ElementFilter.typesIn(classInMethod.getEnclosedElements()).get(0);
        VariableElement testField = ElementFilter.fieldsIn(test.getEnclosedElements()).get(0);
        TypeElement nestedClass = ElementFilter.typesIn(test.getEnclosedElements()).get(0);
        ExecutableElement nestedClassMethod = ElementFilter.methodsIn(nestedClass.getEnclosedElements()).get(0);
        VariableElement nestedClassField = ElementFilter.fieldsIn(nestedClass.getEnclosedElements()).get(0);
        TypeElement nestedClassNestedClass = ElementFilter.typesIn(nestedClass.getEnclosedElements()).get(0);

        assertEquals("TestGetEnclosingTypeElement", test.getSimpleName().toString());
        assertEquals("testMethod", testMethod.getSimpleName().toString());
//        assertEquals("classInMethod", classInMethod.getSimpleName().toString());
//        assertEquals("classInMethodMethod", classInMethodMethod.getSimpleName().toString());
//        assertEquals("classInMethodField", classInMethodField.getSimpleName().toString());
//        assertEquals("classInMethodNestedClass", classInMethodNestedClass.getSimpleName().toString());
        assertEquals("testField", testField.getSimpleName().toString());
        assertEquals("NestedClass", nestedClass.getSimpleName().toString());
        assertEquals("nestedClassMethod", nestedClassMethod.getSimpleName().toString());
        assertEquals("nestedClassField", nestedClassField.getSimpleName().toString());
        assertEquals("NestedClassNestedClass", nestedClassNestedClass.getSimpleName().toString());

        assertEquals(null, SourceUtils.getEnclosingTypeElement(test));
        assertEquals(test, SourceUtils.getEnclosingTypeElement(testMethod));
        assertEquals(test, SourceUtils.getEnclosingTypeElement(testField));
        assertEquals(test, SourceUtils.getEnclosingTypeElement(nestedClass));
        assertEquals(nestedClass, SourceUtils.getEnclosingTypeElement(nestedClassMethod));
        assertEquals(nestedClass, SourceUtils.getEnclosingTypeElement(nestedClassField));
        assertEquals(nestedClass, SourceUtils.getEnclosingTypeElement(nestedClassNestedClass));

        try {
            SourceUtils.getEnclosingTypeElement(test.getEnclosingElement());
            fail();
        } catch (IllegalArgumentException e) {
            //good.
        }
    }

    public void testIsDeprecated1() throws Exception {
        prepareTest();

        TypeElement test = info.getElements().getTypeElement("sourceutils.TestIsDeprecated1");

        assertNotNull(test);

        ExecutableElement methodDeprecated = findElementBySimpleName("methodDeprecated", ElementFilter.methodsIn(test.getEnclosedElements()));
        ExecutableElement methodNotDeprecated = findElementBySimpleName("methodNotDeprecated", ElementFilter.methodsIn(test.getEnclosedElements()));
        VariableElement fieldDeprecated = findElementBySimpleName("fieldDeprecated", ElementFilter.fieldsIn(test.getEnclosedElements()));
        VariableElement fieldNotDeprecated = findElementBySimpleName("fieldNotDeprecated", ElementFilter.fieldsIn(test.getEnclosedElements()));
        TypeElement classDeprecated = findElementBySimpleName("classDeprecated", ElementFilter.typesIn(test.getEnclosedElements()));
        TypeElement classNotDeprecated = findElementBySimpleName("classNotDeprecated", ElementFilter.typesIn(test.getEnclosedElements()));

        assertFalse(info.getElements().isDeprecated(methodNotDeprecated));
        assertFalse(info.getElements().isDeprecated(fieldNotDeprecated));
        assertFalse(info.getElements().isDeprecated(classNotDeprecated));

        assertTrue(info.getElements().isDeprecated(methodDeprecated));
        assertTrue(info.getElements().isDeprecated(fieldDeprecated));
        assertTrue(info.getElements().isDeprecated(classDeprecated));
    }

    public void testIsDeprecated2() throws Exception {
        prepareTest();

        TypeElement test = info.getElements().getTypeElement("sourceutils.TestIsDeprecated2");

        assertNotNull(test);

        ExecutableElement methodDeprecated = findElementBySimpleName("methodDeprecated", ElementFilter.methodsIn(test.getEnclosedElements()));
        ExecutableElement methodNotDeprecated = findElementBySimpleName("methodNotDeprecated", ElementFilter.methodsIn(test.getEnclosedElements()));
        VariableElement fieldDeprecated = findElementBySimpleName("fieldDeprecated", ElementFilter.fieldsIn(test.getEnclosedElements()));
        VariableElement fieldNotDeprecated = findElementBySimpleName("fieldNotDeprecated", ElementFilter.fieldsIn(test.getEnclosedElements()));
        TypeElement classDeprecated = findElementBySimpleName("classDeprecated", ElementFilter.typesIn(test.getEnclosedElements()));
        TypeElement classNotDeprecated = findElementBySimpleName("classNotDeprecated", ElementFilter.typesIn(test.getEnclosedElements()));

        assertFalse(info.getElements().isDeprecated(methodNotDeprecated));
        assertFalse(info.getElements().isDeprecated(fieldNotDeprecated));
        assertFalse(info.getElements().isDeprecated(classNotDeprecated));

        assertTrue(info.getElements().isDeprecated(methodDeprecated));
        assertTrue(info.getElements().isDeprecated(fieldDeprecated));
        assertTrue(info.getElements().isDeprecated(classDeprecated));
    }

    public void testGetOutermostEnclosingTypeElement () throws Exception {
	prepareTest();
	TypeElement test = info.getElements().getTypeElement("sourceutils.TestGetOutermostEnclosingTypeElement");
        assertNotNull(test);
	assertEquals("TestGetOutermostEnclosingTypeElement", test.getSimpleName().toString());

	ExecutableElement testMethod = ElementFilter.methodsIn(test.getEnclosedElements()).get(0);
//        TypeElement classInMethod = ElementFilter.typesIn(testMethod.getEnclosedElements()).get(0);
//        ExecutableElement classInMethodMethod = ElementFilter.methodsIn(classInMethod.getEnclosedElements()).get(0);
//        VariableElement classInMethodField = ElementFilter.fieldsIn(classInMethod.getEnclosedElements()).get(0);
//        TypeElement classInMethodNestedClass = ElementFilter.typesIn(classInMethod.getEnclosedElements()).get(0);
        VariableElement testField = ElementFilter.fieldsIn(test.getEnclosedElements()).get(0);
        TypeElement nestedClass = ElementFilter.typesIn(test.getEnclosedElements()).get(0);
        ExecutableElement nestedClassMethod = ElementFilter.methodsIn(nestedClass.getEnclosedElements()).get(0);
        VariableElement nestedClassField = ElementFilter.fieldsIn(nestedClass.getEnclosedElements()).get(0);
        TypeElement nestedClassNestedClass = ElementFilter.typesIn(nestedClass.getEnclosedElements()).get(0);


        assertEquals("testMethod", testMethod.getSimpleName().toString());
//        assertEquals("classInMethod", classInMethod.getSimpleName().toString());
//        assertEquals("classInMethodMethod", classInMethodMethod.getSimpleName().toString());
//        assertEquals("classInMethodField", classInMethodField.getSimpleName().toString());
//        assertEquals("classInMethodNestedClass", classInMethodNestedClass.getSimpleName().toString());
        assertEquals("testField", testField.getSimpleName().toString());
        assertEquals("NestedClass", nestedClass.getSimpleName().toString());
        assertEquals("nestedClassMethod", nestedClassMethod.getSimpleName().toString());
        assertEquals("nestedClassField", nestedClassField.getSimpleName().toString());
        assertEquals("NestedClassNestedClass", nestedClassNestedClass.getSimpleName().toString());

        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(test));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(testMethod));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(testField));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(nestedClass));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(nestedClassMethod));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(nestedClassField));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(nestedClassNestedClass));

        try {
            SourceUtils.getOutermostEnclosingTypeElement(test.getEnclosingElement());
            fail();
        } catch (IllegalArgumentException e) {
            //good.
        }
    }


    public void testGetDependentRoots () throws Exception {
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject url0 = FileUtil.createFolder(wd,"url0");  //NOI18N
        final FileObject bin0 = FileUtil.createFolder(wd, "bin0"); //NOI18N
        final FileObject url1 = FileUtil.createFolder(wd,"url1");  //NOI18N
        final FileObject bin1 = FileUtil.createFolder(wd, "bin0"); //NOI18N
        final FileObject url2 = FileUtil.createFolder(wd,"url2");  //NOI18N
        final FileObject bin2 = FileUtil.createFolder(wd, "bin2"); //NOI18N
        final FileObject url3 = FileUtil.createFolder(wd,"url3");  //NOI18N
        final FileObject bin3 = FileUtil.createFolder(wd, "bin3"); //NOI18N
        final FileObject url4 = FileUtil.createFolder(wd,"url4");  //NOI18N
        final FileObject bin4 = FileUtil.createFolder(wd, "bin4"); //NOI18N
        final FileObject url5 = FileUtil.createFolder(wd,"url5");  //NOI18N
        final FileObject bin5 = FileUtil.createFolder(wd, "bin5"); //NOI18N

        SFBQImpl.getDefault().register(bin0, url0);
        SFBQImpl.getDefault().register(bin1, url1);
        SFBQImpl.getDefault().register(bin2, url2);
        SFBQImpl.getDefault().register(bin3, url3);
        SFBQImpl.getDefault().register(bin4, url4);
        SFBQImpl.getDefault().register(bin5, url5);



        final ClassPath cp1 = ClassPathSupport.createClassPath(url1);
        final ClassPath compile1 = ClassPathSupport.createClassPath(bin0);
        final ClassPath cp2 = ClassPathSupport.createClassPath(url2);
        final ClassPath compile2 = ClassPath.EMPTY;
        final ClassPath cp3 = ClassPathSupport.createClassPath(url3);
        final ClassPath compile3 = ClassPathSupport.createClassPath(bin1, bin2);
        final ClassPath cp4 = ClassPathSupport.createClassPath(url4);
        final ClassPath compile4 = ClassPathSupport.createClassPath(bin2);
        final ClassPath cp5 = ClassPathSupport.createClassPath(url5);
        final ClassPath compile5 = ClassPathSupport.createClassPath(bin3, bin4);
        final ClassPath[] cps = new ClassPath[] {cp1,cp2,cp3,cp4,cp5};

        CPProvider.getDefault().register(url1, ClassPath.COMPILE, compile1);
        CPProvider.getDefault().register(url1, ClassPath.SOURCE, cp1);
        CPProvider.getDefault().register(url2, ClassPath.COMPILE, compile2);
        CPProvider.getDefault().register(url2, ClassPath.SOURCE, cp2);
        CPProvider.getDefault().register(url3, ClassPath.COMPILE, compile3);
        CPProvider.getDefault().register(url3, ClassPath.SOURCE, cp3);
        CPProvider.getDefault().register(url4, ClassPath.COMPILE, compile4);
        CPProvider.getDefault().register(url4, ClassPath.SOURCE, cp4);
        CPProvider.getDefault().register(url5, ClassPath.COMPILE, compile5);
        CPProvider.getDefault().register(url5, ClassPath.SOURCE, cp5);

        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cps);
        final Future<Void> f = RunWhenScanFinishedSupport.runWhenScanFinished(() -> null, Collections.emptySet());
        f.get();    //Wait for scan to finish
        IndexingManager.getDefault().refreshIndexAndWait(url1.toURL(), null, true);
        IndexingManager.getDefault().refreshIndexAndWait(url2.toURL(), null, true);
        IndexingManager.getDefault().refreshIndexAndWait(url3.toURL(), null, true);
        IndexingManager.getDefault().refreshIndexAndWait(url4.toURL(), null, true);
        IndexingManager.getDefault().refreshIndexAndWait(url5.toURL(), null, true);


        Set<URL> result = SourceUtils.getDependentRoots(url5.toURL(), true);
        assertEquals (1, result.size());
        assertEquals (url5.toURL(),result.iterator().next());

        result = SourceUtils.getDependentRoots(url4.toURL(), true);
        assertEquals (new URL[] {url4.toURL(), url5.toURL()}, result);

        result = SourceUtils.getDependentRoots(url3.toURL(), true);
        assertEquals (new URL[] {url3.toURL(), url5.toURL()}, result);

        result = SourceUtils.getDependentRoots(url2.toURL(), true);
        assertEquals (new URL[] {url2.toURL(), url3.toURL(), url4.toURL(), url5.toURL()}, result);

        result = SourceUtils.getDependentRoots(url1.toURL(), true);
        assertEquals (new URL[] {url1.toURL(), url3.toURL(), url5.toURL()}, result);
    }


    public void testGetFile () throws Exception {
        File workDir = getWorkDir();
        FileObject workFo = FileUtil.toFileObject(workDir);
        assertNotNull (workFo);
        FileObject src = workFo.createFolder("src");
        FileObject userDir = workFo.createFolder("ud");
        CacheFolder.setCacheFolder(userDir);

        ensureRootValid(src.getURL());

        FileObject srcInDefPkg = src.createData("Foo","java");
        assertNotNull(srcInDefPkg);
        FileObject sourceFile = src.createFolder("org").createFolder("me").createData("Test", "java");
        src.getFileObject("org/me").createFolder("Test"); // https://github.com/apache/netbeans/issues/5738
        assertNotNull(sourceFile);
        ClasspathInfo cpInfo = ClasspathInfo.create(ClassPathSupport.createClassPath(new FileObject[0]), ClassPathSupport.createClassPath(new FileObject[0]),
            ClassPathSupport.createClassPath(new FileObject[]{src}));
        FileObject cls = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(cpInfo,PathKind.OUTPUT).getRoots()[0];
        FileObject classInDefPkg = cls.createData("Foo","class");
        assertNotNull(classInDefPkg);
        FileObject classPkg = cls.createFolder("org").createFolder("me");
        assertNotNull(classPkg);
        FileObject classFile = classPkg.createData("Test", "class");
        assertNotNull(classFile);
        FileObject classFileInnder = classPkg.createData("Test$Inner", "class");
        assertNotNull(classFileInnder);
        SFBQImpl.getDefault().register(cls, src);
        ElementHandle<? extends Element> handle = ElementHandle.createTypeElementHandle(ElementKind.CLASS, "org.me.Test");
        assertNotNull (handle);
        FileObject result = SourceUtils.getFile(handle, cpInfo);
        assertEquals(sourceFile, result);
        handle = ElementHandle.createTypeElementHandle(ElementKind.CLASS, "org.me.Test$Inner");
        result = SourceUtils.getFile(handle,cpInfo);
        assertEquals(sourceFile, result);
        handle = ElementHandle.createPackageElementHandle("org.me");
        result = SourceUtils.getFile(handle,cpInfo);
        assertEquals(sourceFile.getParent(), result);
        handle = ElementHandle.createTypeElementHandle(ElementKind.CLASS, "Foo");
        result = SourceUtils.getFile(handle,cpInfo);
        assertEquals(srcInDefPkg, result);
    }

    public void testGetMainClasses() throws Exception {
        final File wd = getWorkDir();
        final FileObject src = FileUtil.createFolder(new File (wd,"src"));
        final FileObject userDir = FileUtil.createFolder(new File (wd,"ud"));
        CacheFolder.setCacheFolder(userDir);
        final FileObject emptyClass = createFile(src, "C1.java","class C1 {}");
        assertEquals(0, SourceUtils.getMainClasses(emptyClass).size());
        final FileObject classWithMethod = createFile(src, "C2.java","class C2 { public static void test (String... args){} }");
        assertEquals(0, SourceUtils.getMainClasses(classWithMethod).size());
        final FileObject classWithMethod2 = createFile(src, "C3.java","class C3 { static void main (String... args){} }");
        assertEquals(0, SourceUtils.getMainClasses(classWithMethod2).size());
        final FileObject classWithMethod3 = createFile(src, "C4.java","class C4 { public void main (String... args){} }");
        assertEquals(0, SourceUtils.getMainClasses(classWithMethod3).size());
        final FileObject classWithMethod4 = createFile(src, "C5.java","class C5 { public static void main (StringBuilder... args){} }");
        assertEquals(0, SourceUtils.getMainClasses(classWithMethod4).size());
        final FileObject simpleMain = createFile(src, "M1.java","class M1 { public static void main (String... args){} }");
        assertMain(new String[] {"M1"}, SourceUtils.getMainClasses(simpleMain));
        final FileObject simpleMain2 = createFile(src, "M2.java","public class M2 { public static void main (String... args){} }");
        assertMain(new String[] {"M2"}, SourceUtils.getMainClasses(simpleMain2));
        final FileObject innerMain = createFile(src, "M3.java","class M3 { public static class Inner { public static void main (String... args){} } }");
        assertMain(new String[] {"M3.Inner"}, SourceUtils.getMainClasses(innerMain));
        final FileObject innerMain2 = createFile(src, "M4.java","class M4 { protected static class Inner { public static void main (String... args){} } }");
        assertMain(new String[] {"M4.Inner"}, SourceUtils.getMainClasses(innerMain2));
        final FileObject innerMain3 = createFile(src, "M5.java","class M5 { static class Inner { public static void main (String... args){} } }");
        assertMain(new String[] {"M5.Inner"}, SourceUtils.getMainClasses(innerMain3));
        final FileObject innerMain4 = createFile(src, "M6.java","class M6 { private static class Inner { public static void main (String... args){} } }");
        assertMain(new String[] {"M6.Inner"}, SourceUtils.getMainClasses(innerMain4));
        final FileObject innerMain5 = createFile(src, "M7.java","class M7 { class Inner { public static void main (String... args){} } }");
        assertEquals(0, SourceUtils.getMainClasses(innerMain5).size());
        final FileObject innerMain6 = createFile(src, "M8.java","class M8 { class Inner { static class InnerInner {public static void main (String... args){} } } }");
        assertEquals(0, SourceUtils.getMainClasses(innerMain6).size());
        final FileObject innerMain7 = createFile(src, "M9.java","class M9 { static class Inner { static class InnerInner {public static void main (String... args){} } } }");
        assertMain(new String[] {"M9.Inner.InnerInner"}, SourceUtils.getMainClasses(innerMain7));
        final FileObject twoTop = createFile(src, "T1.java","class T1 { public static void main (String... args){}} class T1X {public static void main (String... args){}}");
        assertMain(new String[] {"T1","T1X"}, SourceUtils.getMainClasses(twoTop));
        final FileObject twoTop2 = createFile(src, "T2.java","class T2 { public static void main (String... args){} static class Inner {public static void main (String... args){}}} class T2X {public static void main (String... args){} static class Inner {public static void main (String... args){}}}");
        assertMain(new String[] {"T2","T2X", "T2.Inner", "T2X.Inner"}, SourceUtils.getMainClasses(twoTop2));
        final FileObject inhMain = createFile(src, "D1.java","class D1 { public static void main (String... args){}} class D1X extends D1 {}");
        assertMain(new String[] {"D1","D1X"}, SourceUtils.getMainClasses(inhMain));
    }

    public void testIsClassFile() throws Exception {
        final File wd = getWorkDir();
        final FileObject src = FileUtil.createFolder(new File (wd,"src"));
        final FileObject emptyJava = createFile(src, "C1.java","class C1 {}");
        assertFalse(SourceUtils.isClassFile(emptyJava));
        final FileObject emptyClass = createFile(src, "C1.class","");
        assertTrue(SourceUtils.isClassFile(emptyClass));
    }

    public void testGenerateReadableParameterName() throws Exception {
        System.out.println("testGenerateReadableParameterName");
        Match m = new Match("java.lang.Object", "o");
        m.match("java.lang.Runnable", "r")
        .match("java.awt.event.ActionListener,java.awt.event.ActionListener","al,al1")
        .match("java.io.InputStream", "in")
        .match("java.io.OutputStream","out")
        .match("java.io.ByteArrayOutputStream","stream")
        .match("missingthing.Foodbar", "fdbr")
        .match("somepackage.FillUpNoKnownMessageEverywhere", "funkme")
        .match("java.lang.Class","type")
        .match("java.lang.Class<T>", "type")
        .match("org.openide.util.Lookup","lkp")
        .match("sun.awt.KeyboardFocusManagerPeerImpl", "kfmpi")
        .match("com.foo.BigInterface", "bi")
        .match("java.util.concurrent.Callable<Runnable>", "clbl")
        .match("int[]", "ints")
        .match("short", "s")
        .match("java.lang.Integer...", "intgrs")
        .match("java.awt.Component[]", "cmpnts")
        .match("int,java.lang.Runnable", "i,r")
        .match("java.lang.Runnable[]", "rs")
        .match("com.foo.Classwithanannoyinglylongname", "c")
        .match("com.foo.Classwithanannoyinglylongname,foo.bar.Classwithanotherannoyinglylongname", "c,c1")
        .match("com.foo.Classwithanannoyinglylongname,foo.bar.ClasswithLongnameButshortAcronym", "c,clba")
        .match("com.foo.ClassWithAnAnnoyinglyLongNameThatGoesOnForever", "c");
        m.assertMatch();
    }

    /**
     * Checks that a toplevel class that contains $ in its name is found in the
     * dollar file. Checks that inner classes of such a old-style class are also found correctly.
     * @throws Exception
     */
    public void testDollarSourceName() throws Exception {
        prepareTest();
        TypeElement test = info.getElements().getTypeElement("sourceutils.TestDollarSourceName$dollar");
        assertNotNull(test);
        FileObject outerFile = SourceUtils.getFile(ElementHandle.create(test), info.getClasspathInfo());
        assertEquals("TestDollarSourceName.java", outerFile.getNameExt());
        assertEquals("TestDollarSourceName.java", SourceUtils.findSourceFileName(test));

        TypeElement inner = info.getElements().getTypeElement("sourceutils.TestDollarSourceName$dollar.InnerClass");
        FileObject innerFile = SourceUtils.getFile(ElementHandle.create(inner), info.getClasspathInfo());
        assertEquals("TestDollarSourceName.java", innerFile.getNameExt());
        assertEquals("TestDollarSourceName.java", SourceUtils.findSourceFileName(inner));
    }

    public void testGetBound() throws Exception {
        //only a scatch of the test, add testcases as needed:
        prepareTest();

        TypeElement test = info.getElements().getTypeElement("sourceutils.TestGetBound");

        assertNotNull(test);

        TypeParameterElement typeParam = test.getTypeParameters().get(0);
        TypeMirror outerBound = ((TypeVariable) typeParam.asType()).getUpperBound();

        TypeMirror bound = SourceUtils.getBound((WildcardType) ((DeclaredType) outerBound).getTypeArguments().get(0));
        assertEquals("java.lang.CharSequence", String.valueOf(bound));
    }

    public void testNameForAClassFile() throws Exception {
        // compiled as Hello.java
        // package my.hello; public class Hello {}
        byte[] classFile = new byte[] {
            -54, -2, -70, -66, 0, 0, 0, 50, 0, 13, 10, 0, 3, 0, 10, 7, 0, 11,
            7, 0, 12, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41,
            86, 1, 0, 4, 67, 111, 100, 101, 1, 0, 15, 76, 105, 110, 101, 78,
            117, 109, 98, 101, 114, 84, 97, 98, 108, 101, 1, 0, 10, 83, 111,
            117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 10, 72, 101, 108, 108,
            111, 46, 106, 97, 118, 97, 12, 0, 4, 0, 5, 1, 0, 14, 109, 121, 47,
            104, 101, 108, 108, 111, 47, 72, 101, 108, 108, 111, 1, 0, 16, 106,
            97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116,
            0, 33, 0, 2, 0, 3, 0, 0, 0, 0, 0, 1, 0, 1, 0, 4, 0, 5, 0, 1, 0, 6,
            0, 0, 0, 29, 0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 1, -79, 0, 0, 0,
            1, 0, 7, 0, 0, 0, 6, 0, 1, 0, 0, 0, 1, 0, 1, 0, 8, 0, 0, 0, 2, 0, 9
        };

        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        FileObject target = FileUtil.createFolder(workFO, "target");
        JavaSourceTest.SourceLevelQueryImpl.sourceLevel = "1.8";
        FileObject clazz = FileUtil.createData(target, "my/hello/Hello.class");
        try (OutputStream os = clazz.getOutputStream()) {
            os.write(classFile);
        }
        FileUtil.setMIMEType("class", ClassParser.MIME_TYPE);
        assertEquals(ClassParser.MIME_TYPE, clazz.getMIMEType());
        js = JavaSource.forFileObject(clazz);
        assertNotNull("JavaSource found", js);
        info = SourceUtilsTestUtil.getCompilationInfo(js, JavaSource.Phase.RESOLVED);
        assertNotNull("info found", info);
        int count  = 0;
        for (TypeElement test : info.getTopLevelElements()) {
            assertNotNull("type element found", test);
            assertEquals("Hello.java", SourceUtils.findSourceFileName(test));
            count++;
        }
        assertEquals("One element found", 1, count);
    }

    public void testNewMainMethod() throws Exception {
        class TestCase {
            public final String code;
            public final String mainMethod;

            public TestCase(String code, String mainMethod) {
                this.code = code;
                this.mainMethod = mainMethod;
            }
        }
        TestCase[] testCases = new TestCase[] {
            new TestCase("public class Test {\n" +
                         "    static void main(String... args) {}\n" +
                         "    static void main() {}\n" +
                         "}\n",
                         "Test:main:([Ljava/lang/String;)V"),
            new TestCase("public class Test {\n" +
                         "    static void main(String... args) {}\n" +
                         "    void main() {}\n" +
                         "}\n",
                         "Test:main:([Ljava/lang/String;)V"),
            new TestCase("public class Test {\n" +
                         "    static void main() {}\n" +
                         "    void main(String... args) {}\n" +
                         "}\n",
                         "Test:main:()V"),
            new TestCase("public class Test {\n" +
                         "    void main(String... args) {}\n" +
                         "    void main() {}\n" +
                         "}\n",
                         "Test:main:([Ljava/lang/String;)V"),
            new TestCase("public class Test {\n" +
                         "    public static void plain(String... args) {}\n" +
                         "    void main() {}\n" +
                         "}\n",
                         "Test:main:()V"),
            new TestCase("public class Test {\n" +
                         "    void main() {}\n" +
                         "    public static void plain(String... args) {}\n" +
                         "}\n",
                         "Test:main:()V"),
        };
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        FileObject src = FileUtil.createFolder(workFO, "src");
        FileObject build = FileUtil.createFolder(workFO, "build");
        FileObject cache = FileUtil.createFolder(workFO, "cache");
        FileObject testFile = FileUtil.createData(src, "Test.java");
        SourceUtilsTestUtil.setSourceLevel(testFile, Integer.toString(SourceVersion.latest().ordinal()));
        SourceUtilsTestUtil.setCompilerOptions(src, Arrays.asList("--enable-preview"));
        SourceUtilsTestUtil.prepareTest(src, build, cache);

        for (TestCase tc : testCases) {
            TestUtilities.copyStringToFile(testFile, tc.code);
            js = JavaSource.forFileObject(testFile);
            assertNotNull("JavaSource found", js);
            info = SourceUtilsTestUtil.getCompilationInfo(js, JavaSource.Phase.RESOLVED);
            assertNotNull("info found", info);
            ExecutableElement mainMethod = null;
            for (TypeElement test : info.getTopLevelElements()) {
                if (test.getSimpleName().contentEquals("Test")) {
                    for (ExecutableElement el : ElementFilter.methodsIn(test.getEnclosedElements())) {
                        if (SourceUtils.isMainMethod(el)) {
                            assertNull(mainMethod);
                            mainMethod = el;
                        }
                    }
                }
            }
            String mainMethodSignature = 
                    Arrays.stream(SourceUtils.getJVMSignature(ElementHandle.create(mainMethod)))
                          .collect(Collectors.joining(":"));
            assertEquals(tc.mainMethod, mainMethodSignature);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Helper methods & Mock services">

    private void prepareTest() throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        FileObject sourceRoot = workFO.getFileObject("src");
        if (sourceRoot == null) {
            sourceRoot = workFO.createFolder("src");
        }
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = sourceRoot.getFileObject("sourceutils");
        if (packageRoot == null) {
            packageRoot = sourceRoot.createFolder("sourceutils");
        }

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        String capitalizedName = "T" + getName().substring(1);

        TestUtil.copyFiles(FileUtil.toFile(sourceRoot), "sourceutils/" + capitalizedName + ".java");

        packageRoot.refresh();

        FileObject testSource = packageRoot.getFileObject(capitalizedName + ".java");

        assertNotNull(testSource);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);

        js = JavaSource.forFileObject(testSource);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, JavaSource.Phase.RESOLVED);

        assertNotNull(info);
    }

    private void assertEquals (URL[] expected, Set<URL> result) {
        assertEquals (expected.length,result.size());
        for (URL eurl : expected) {
            assertTrue (result.remove(eurl));
        }
        assertTrue(result.isEmpty());
    }

    private <E extends Element> E findElementBySimpleName(String simpleName, List<E> elements) {
        for (E e : elements) {
            if (simpleName.contentEquals(e.getSimpleName()))
                return e;
        }

        fail("Not found element with simple name: " + simpleName);

        throw new Error("Should never be here!");
    }

    private void assertMain(final String[] expected, final Iterable<? extends ElementHandle<TypeElement>> result) {
        final Set<String> es = new HashSet<String>(Arrays.asList(expected));
        for (ElementHandle<TypeElement> r : result) {
            assertTrue(es.remove(r.getQualifiedName()));
        }
        assertTrue(es.isEmpty());
    }


    private static FileObject createFile (final FileObject folder, final String name, final String content) throws IOException {
        final FileObject fo = FileUtil.createData(folder, name);
        final FileLock lock = fo.lock();
        try {
            final OutputStream out = fo.getOutputStream(lock);
            try {
                FileUtil.copy(new ByteArrayInputStream(content.getBytes()), out);
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
    }

    private static class CPProvider implements ClassPathProvider {

        private ConcurrentMap<FileObject,Map<String,ClassPath>> cps =
                new ConcurrentHashMap<FileObject, Map<String, ClassPath>>();

        private CPProvider() {}

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            final Map<String,ClassPath> cps4file = cps.get(file);
            if (cps4file == null) {
                return null;
            }
            return cps4file.get(type);
        }

        void register(
            FileObject root,
            String type,
            ClassPath cp) {
            Map<String,ClassPath> res = cps.get(root);
            if (res == null) {
                final Map<String, ClassPath> newCps = new HashMap<String, ClassPath>();
                res = cps.putIfAbsent(root, newCps);
                if (res == null) {
                    res = newCps;
                }
            }
            res.put(type, cp);
        }

        static CPProvider getDefault() {
            return H.INSTANCE;
        }

        private static class H {
            private static final CPProvider INSTANCE = new CPProvider();
        }

    }

    private static class SFBQImpl implements SourceForBinaryQueryImplementation {

        private static SFBQImpl instance;

        private final Map<URL, FileObject> map = new HashMap<URL, FileObject> ();

        private SFBQImpl () {

        }

        public void register (FileObject bin, FileObject src) throws IOException {
            map.put(bin.toURL(), src);
        }

        public Result findSourceRoots(URL binaryRoot) {
            final FileObject src = map.get (binaryRoot);
            if (src != null) {
                return new Result() {

                    public FileObject[] getRoots() {
                        return new FileObject[] {src};
                    }

                    public void addChangeListener(ChangeListener l) {
                    }

                    public void removeChangeListener(ChangeListener l) {
                    }
                };
            }
            return null;
        }

        public static synchronized SFBQImpl getDefault () {
            if (instance == null) {
                instance = new SFBQImpl ();
            }
            return instance;
        }
    }

    private static final class Match {
        private String[] fqns;
        private String[] names;
        private final Set<String> used = new HashSet<String>();
        Match(String fqns, String names) {
            this.fqns = fqns.split(",");
            this.names = names.split(",");
            assertEquals ("Test is broken: " + fqns + " vs " + names + " do " +
                    "not have same number of elements", this.fqns.length, this.names.length);
        }

        public void assertMatch() {
            assertTrue (this.fqns.length > 0);
            assertTrue (this.names.length > 0);
            for (int i = 0; i < fqns.length; i++) {
                String fqn = fqns[i];
                String expected = names[i];
                String got = ParameterNameProviderImpl.generateReadableParameterName(fqn, used);
                String msg = "For " + Arrays.asList(fqns) + " expected " + Arrays.asList(names);
                assertEquals (msg, expected, got);
            }
            if (next != null) {
                next.assertMatch();
            }
        }

        private Match next;
        public Match match(String fqns, String names) {
            next = new Match (fqns, names);
            return next;
        }
    }
    //</editor-fold>
}
