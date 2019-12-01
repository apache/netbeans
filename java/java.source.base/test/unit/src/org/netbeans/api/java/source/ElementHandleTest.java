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

import com.sun.tools.javac.model.JavacElements;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import junit.framework.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.ElementUtils;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tomas Zezula
 */
public class ElementHandleTest extends NbTestCase {
    
    private FileObject src;
    private FileObject data;
    
    
    static {
        ElementHandleTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", ElementHandleTest.Lkp.class.getName());
        Assert.assertEquals(ElementHandleTest.Lkp.class, Lookup.getDefault().getClass());
    }   
    
    public static class Lkp extends ProxyLookup {
        
        private static Lkp DEFAULT;
        
        public Lkp () {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = Lkp.class.getClassLoader();
            this.setLookups(
                 new Lookup [] {
                    Lookups.metaInfServices(l),
                    Lookups.singleton(l),
                    Lookups.singleton(ClassPathProviderImpl.getDefault()),
                    Lookups.singleton(SourceLevelQueryImpl.getDefault()),
            });
        }
        
    }
    
    
    public ElementHandleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
        FileObject wd = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull(wd);
        this.src = wd.createFolder("src");
        this.data = src.createData("Test","java");
        FileLock lock = data.lock();
        try {
            PrintWriter out = new PrintWriter ( new OutputStreamWriter (data.getOutputStream(lock)));
            try {
                out.println ("public class Test {}");
            } finally {
                out.close ();
            }
        } finally {
            lock.releaseLock();
        }
        ClassPathProviderImpl.getDefault().setClassPaths(TestUtil.getBootClassPath(),
                                                         ClassPathSupport.createClassPath(new URL[0]),
                                                         ClassPathSupport.createClassPath(new FileObject[]{this.src}));
    }

    protected void tearDown() throws Exception {
    }


    public void testElementHandle() throws Exception {
        final JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathProviderImpl.getDefault().findClassPath(data,ClassPath.BOOT), ClassPathProviderImpl.getDefault().findClassPath(data, ClassPath.COMPILE), null));
        assertNotNull(js);
        final ElementHandle[] utilElementHandle = new ElementHandle[1];
        final ElementHandle[] stringElementHandle = new ElementHandle[1];
        final ElementHandle[] stringLengthElementHandle = new ElementHandle[1];
        final ElementHandle[] stringConstructorElementHandle = new ElementHandle[1];
        final ElementHandle[] stringCountElementHandle = new ElementHandle[1];
        final ElementHandle[] retentionPolicyHandle = new ElementHandle[1];
        final ElementHandle[] retentionPolicyClassHandle = new ElementHandle[1];
        final ElementHandle[] innerClassHandle = new ElementHandle[1];
        final ElementHandle[] collectionAddHandle = new ElementHandle[1];
        final ElementHandle[] annonClassHandle = new ElementHandle[1];
        final ElementHandle[] genParList = new ElementHandle[1];
        final ElementHandle[] genParColsMin = new ElementHandle[1];
        
        js.runUserActionTask(new Task<CompilationController>() {            
            public void run(CompilationController parameter) {
                JavacElements elements = (JavacElements) parameter.getElements();
                Element utilElement = elements.getPackageElement("java.util");
                assertNotNull (utilElement);
                utilElementHandle[0] = ElementHandle.create(utilElement);
                assertNotNull (utilElementHandle[0]);
                Element stringElement = elements.getTypeElement (String.class.getName());
                assertNotNull (stringElement);
                stringElementHandle[0] = ElementHandle.create(stringElement);
                assertNotNull (stringElementHandle[0]);
                Element[] stringMembers = getStringElements(stringElement);
                assertEquals(3,stringMembers.length);
                assertNotNull (stringMembers[0]);
                assertNotNull (stringMembers[1]);
                assertNotNull (stringMembers[2]);
                stringLengthElementHandle[0] = ElementHandle.create(stringMembers[0]);
                assertNotNull (stringLengthElementHandle[0]);
                stringConstructorElementHandle[0] = ElementHandle.create(stringMembers[1]);
                assertNotNull (stringConstructorElementHandle[0]);
                stringCountElementHandle[0] = ElementHandle.create(stringMembers[2]);
                assertNotNull (stringCountElementHandle[0]);
                Element retentionPolicy = elements.getTypeElement(java.lang.annotation.RetentionPolicy.class.getName());
                assertNotNull (retentionPolicy);
                retentionPolicyHandle[0] = ElementHandle.create(retentionPolicy);
                assertNotNull (retentionPolicyHandle[0]);
                List<? extends Element> members = ((TypeElement)retentionPolicy).getEnclosedElements();                    
                for (Element member : members) {
                    if (member.getKind() == ElementKind.ENUM_CONSTANT && "CLASS".contentEquals(((VariableElement)member).getSimpleName())) {
                        retentionPolicyClassHandle[0] = ElementHandle.create(member);
                        break;
                    }
                }
                assertNotNull (retentionPolicyClassHandle[0]);
                Element csl = ElementUtils.getTypeElementByBinaryName(parameter, "java.util.Collections$SingletonList");
                assertNotNull (csl);
                innerClassHandle[0] = ElementHandle.create(csl);
                assertNotNull (innerClassHandle[0]);
                Element cadd = getCollectionAdd (elements.getTypeElement(java.util.Collection.class.getName()));
                assertNotNull (cadd);
                collectionAddHandle[0] = ElementHandle.create(cadd);
                assertNotNull (collectionAddHandle[0]);
                TypeElement annonClass = ElementUtils.getTypeElementByBinaryName(parameter, "java.lang.String$1"); //NOI18N
                assertNotNull (annonClass);
                annonClassHandle[0] = ElementHandle.create(annonClass);
                assertNotNull (annonClassHandle[0]);
                TypeElement listClass = ElementUtils.getTypeElementByBinaryName(parameter, "java.util.List"); //NOI18N
                assertNotNull(listClass);
                List<? extends TypeParameterElement> tpes = listClass.getTypeParameters();
                assertEquals(tpes.size(), 1);
                genParList[0] = ElementHandle.create(tpes.get(0));
                TypeElement collsClass = ElementUtils.getTypeElementByBinaryName(parameter, "java.util.Collections"); //NOI18N
                assertNotNull(collsClass);
                for (Element member : collsClass.getEnclosedElements()) {
                    if (member.getKind() == ElementKind.METHOD && member.getSimpleName().contentEquals("min")) {
                        ExecutableElement ee = (ExecutableElement) member;
                        if (ee.getParameters().size() == 1) {
                            genParColsMin[0] = ElementHandle.create(ee.getTypeParameters().get(0));
                        }
                    }
                }
                assertNotNull(genParColsMin[0]);
            }
            
        },true);
        
        js.runUserActionTask(new Task<CompilationController>() {            
            public void run(CompilationController parameter) {
                JavacElements elements = (JavacElements) parameter.getElements();
                Element resolved = utilElementHandle[0].resolve(parameter);
                assertNotNull (resolved);
                Element utilElement = elements.getPackageElement("java.util");
                assertNotNull (utilElement);
                assertEquals(resolved,utilElement);
                resolved = stringElementHandle[0].resolve(parameter);
                assertNotNull (resolved);
                Element stringElement = elements.getTypeElement(String.class.getName());
                assertNotNull (stringElement);
                assertEquals(resolved,stringElement);                    
                Element[] stringMembers = getStringElements(stringElement);
                assertEquals(3,stringMembers.length);
                assertNotNull (stringMembers[0]);
                assertNotNull (stringMembers[1]);                    
                assertNotNull (stringMembers[2]);
                resolved = stringLengthElementHandle[0].resolve(parameter);
                assertNotNull (resolved);        
                assertEquals(resolved,stringMembers[0]);
                resolved = stringConstructorElementHandle[0].resolve(parameter);
                assertNotNull (resolved);
                assertEquals(resolved,stringMembers[1]);
                resolved = stringCountElementHandle[0].resolve(parameter);
                assertNotNull (resolved);
                assertEquals(resolved,stringMembers[2]);
                resolved = retentionPolicyHandle[0].resolve(parameter);
                assertNotNull (resolved);
                Element retentionPolicy = elements.getTypeElement(java.lang.annotation.RetentionPolicy.class.getName());
                assertNotNull (retentionPolicy);
                assertEquals(resolved, retentionPolicy);
                List<? extends Element> members = ((TypeElement)retentionPolicy).getEnclosedElements();
                Element retentionClassElement = null;
                for (Element member : members) {
                    if (member.getKind() == ElementKind.ENUM_CONSTANT && "CLASS".contentEquals(((VariableElement)member).getSimpleName())) {
                        retentionClassElement = member;
                        break;
                    }
                }
                assertNotNull (retentionClassElement);
                resolved = retentionPolicyClassHandle[0].resolve(parameter);
                assertNotNull (resolved);
                assertEquals(resolved,retentionClassElement);
                resolved = innerClassHandle[0].resolve(parameter);
                assertNotNull (resolved);
                Element csl = ElementUtils.getTypeElementByBinaryName(parameter, "java.util.Collections$SingletonList");
                assertNotNull (csl);
                assertEquals(resolved,csl);
                resolved = collectionAddHandle[0].resolve(parameter);
                assertNotNull(resolved);
                Element cadd = getCollectionAdd(elements.getTypeElement(java.util.Collection.class.getName()));
                assertNotNull(cadd);
                assertEquals (resolved, cadd);
                resolved = annonClassHandle[0].resolve(parameter);
                assertNotNull (resolved);
                TypeElement annonClass = ElementUtils.getTypeElementByBinaryName(parameter, "java.lang.String$1"); //NOI18N
                assertNotNull (annonClass);
                assertEquals(resolved,annonClass);
                
                TypeElement listClass = ElementUtils.getTypeElementByBinaryName(parameter, "java.util.List"); //NOI18N
                assertNotNull(listClass);
                TypeParameterElement tpe = listClass.getTypeParameters().get(0);
                resolved = genParList[0].resolve(parameter);
                assertEquals(tpe, resolved);
                
                tpe = null;
                TypeElement collsClass = ElementUtils.getTypeElementByBinaryName(parameter, "java.util.Collections"); //NOI18N
                assertNotNull(collsClass);
                for (Element member : collsClass.getEnclosedElements()) {
                    if (member.getKind() == ElementKind.METHOD && member.getSimpleName().contentEquals("min")) {
                        ExecutableElement ee = (ExecutableElement) member;
                        if (ee.getParameters().size() == 1) {
                            tpe = ee.getTypeParameters().get(0);
                        }
                    }
                }
                assertNotNull(tpe);
                resolved = genParColsMin[0].resolve(parameter);
                assertEquals(tpe, resolved);
            }
            
        },true);
        
    }
    
    public void testSignatureEquals() throws Exception {
        final JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathProviderImpl.getDefault().findClassPath(data,ClassPath.BOOT), ClassPathProviderImpl.getDefault().findClassPath(data, ClassPath.COMPILE), null));
        assertNotNull(js);
        final ElementHandle[] utilElementHandle = new ElementHandle[1];
        final ElementHandle[] stringElementHandle = new ElementHandle[1];
        final ElementHandle[] stringLengthElementHandle = new ElementHandle[1];
        final ElementHandle[] stringConstructorElementHandle = new ElementHandle[1];
        final ElementHandle[] stringCountElementHandle = new ElementHandle[1];
        final ElementHandle[] retentionPolicyHandle = new ElementHandle[1];
        final ElementHandle[] retentionPolicyClassHandle = new ElementHandle[1];
        final ElementHandle[] innerClassHandle = new ElementHandle[1];
        final ElementHandle[] collectionAddHandle = new ElementHandle[1];
        final ElementHandle[] annonClassHandle = new ElementHandle[1];
        
        js.runUserActionTask(new Task<CompilationController>() {            
            public void run(CompilationController parameter) {
                JavacElements elements = (JavacElements) parameter.getElements();
                Element utilElement = elements.getPackageElement("java.util");
                assertNotNull (utilElement);
                utilElementHandle[0] = ElementHandle.create(utilElement);
                assertNotNull (utilElementHandle[0]);
                Element stringElement = elements.getTypeElement (String.class.getName());
                assertNotNull (stringElement);
                stringElementHandle[0] = ElementHandle.create(stringElement);
                assertNotNull (stringElementHandle[0]);
                Element[] stringMembers = getStringElements(stringElement);
                assertEquals(3,stringMembers.length);
                assertNotNull (stringMembers[0]);
                assertNotNull (stringMembers[1]);
                assertNotNull (stringMembers[2]);
                stringLengthElementHandle[0] = ElementHandle.create(stringMembers[0]);
                assertNotNull (stringLengthElementHandle[0]);
                stringConstructorElementHandle[0] = ElementHandle.create(stringMembers[1]);
                assertNotNull (stringConstructorElementHandle[0]);
                stringCountElementHandle[0] = ElementHandle.create(stringMembers[2]);
                assertNotNull (stringCountElementHandle[0]);
                Element retentionPolicy = elements.getTypeElement(java.lang.annotation.RetentionPolicy.class.getName());
                assertNotNull (retentionPolicy);
                retentionPolicyHandle[0] = ElementHandle.create(retentionPolicy);
                assertNotNull (retentionPolicyHandle[0]);
                List<? extends Element> members = ((TypeElement)retentionPolicy).getEnclosedElements();                    
                for (Element member : members) {
                    if (member.getKind() == ElementKind.ENUM_CONSTANT && "CLASS".contentEquals(((VariableElement)member).getSimpleName())) {
                        retentionPolicyClassHandle[0] = ElementHandle.create(member);
                        break;
                    }
                }
                assertNotNull (retentionPolicyClassHandle[0]);
                Element csl = elements.getTypeElement("java.util.Collections.SingletonList");
                assertNotNull (csl);
                innerClassHandle[0] = ElementHandle.create(csl);
                assertNotNull (innerClassHandle[0]);
                Element cadd = getCollectionAdd (elements.getTypeElement(java.util.Collection.class.getName()));
                assertNotNull (cadd);
                collectionAddHandle[0] = ElementHandle.create(cadd);
                assertNotNull (collectionAddHandle[0]);
                TypeElement annonClass = ElementUtils.getTypeElementByBinaryName(parameter, "java.lang.String$1"); //NOI18N
                assertNotNull (annonClass);
                annonClassHandle[0] = ElementHandle.create(annonClass);
                assertNotNull (annonClassHandle[0]);
            }
            
        },true);
        
        js.runUserActionTask(new Task<CompilationController>() {            
            public void run(CompilationController parameter) {
                JavacElements elements = (JavacElements)parameter.getElements();
                Element utilElement = elements.getPackageElement("java.util");
                assertNotNull (utilElement);
                assertTrue (utilElementHandle[0].signatureEquals(utilElement));
                Element stringElement = elements.getTypeElement(String.class.getName());
                assertNotNull (stringElement);
                assertTrue(stringElementHandle[0].signatureEquals(stringElement));                    
                Element[] stringMembers = getStringElements(stringElement);
                assertEquals(3,stringMembers.length);
                assertNotNull (stringMembers[0]);
                assertNotNull (stringMembers[1]);                    
                assertNotNull (stringMembers[2]);
                assertTrue(stringLengthElementHandle[0].signatureEquals(stringMembers[0]));
                assertTrue(stringConstructorElementHandle[0].signatureEquals(stringMembers[1]));
                assertTrue(stringCountElementHandle[0].signatureEquals(stringMembers[2]));
                Element retentionPolicy = elements.getTypeElement(java.lang.annotation.RetentionPolicy.class.getName());
                assertNotNull (retentionPolicy);
                assertTrue(retentionPolicyHandle[0].signatureEquals(retentionPolicy));
                List<? extends Element> members = ((TypeElement)retentionPolicy).getEnclosedElements();
                Element retentionClassElement = null;
                for (Element member : members) {
                    if (member.getKind() == ElementKind.ENUM_CONSTANT && "CLASS".contentEquals(((VariableElement)member).getSimpleName())) {
                        retentionClassElement = member;
                        break;
                    }
                }
                assertNotNull (retentionClassElement);
                assertTrue(retentionPolicyClassHandle[0].signatureEquals(retentionClassElement));
                Element csl = ElementUtils.getTypeElementByBinaryName(parameter, "java.util.Collections$SingletonList");
                assertNotNull (csl);
                assertTrue(innerClassHandle[0].signatureEquals(csl));
                Element cadd = getCollectionAdd(elements.getTypeElement(java.util.Collection.class.getName()));
                assertNotNull(cadd);
                assertTrue (collectionAddHandle[0].signatureEquals(cadd));
                TypeElement annonClass = ElementUtils.getTypeElementByBinaryName(parameter, "java.lang.String$1"); //NOI18N
                assertNotNull (annonClass);
                assertTrue(annonClassHandle[0].signatureEquals(annonClass));
            }
            
        },true);
        
    }
    
    
    public void testCreatePackageElementHandle() {
        final ElementHandle<PackageElement> eh = ElementHandle.createPackageElementHandle("org.me");    //NOI18N
        assertEquals(ElementKind.PACKAGE, eh.getKind());
    }
    
    
    public void testCreateTypeElementHandle() {
        final Set<ElementKind> allowed = new HashSet<ElementKind>(Arrays.asList(new ElementKind[]{
            ElementKind.CLASS,
            ElementKind.INTERFACE,
            ElementKind.ENUM,
            ElementKind.ANNOTATION_TYPE
        }));
        for (ElementKind aek : allowed) {
            ElementHandle<TypeElement> eh = ElementHandle.createTypeElementHandle(aek, "org.me.Foo");    //NOI18N
            assertEquals(aek, eh.getKind());
        }
        for (ElementKind aek : ElementKind.values()) {
            if (!allowed.contains(aek)) {
                try {
                    ElementHandle<TypeElement> eh = ElementHandle.createTypeElementHandle(aek, "org.me.Foo");    //NOI18N
                    assertTrue(false);
                }catch (IllegalArgumentException e) {
                }
            }
        }
    }
    
    public void testEquals() throws Exception {
        final JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathProviderImpl.getDefault().findClassPath(data,ClassPath.BOOT), ClassPathProviderImpl.getDefault().findClassPath(data, ClassPath.COMPILE), null));
        assertNotNull(js);
        final ElementHandle[] utilElementHandle = new ElementHandle[1];
        final ElementHandle[] stringElementHandle = new ElementHandle[1];
        final ElementHandle[] stringLengthElementHandle = new ElementHandle[1];
        final ElementHandle[] stringConstructorElementHandle = new ElementHandle[1];
        final ElementHandle[] stringCountElementHandle = new ElementHandle[1];
        final ElementHandle[] retentionPolicyHandle = new ElementHandle[1];
        final ElementHandle[] retentionPolicyClassHandle = new ElementHandle[1];
        final ElementHandle[] innerClassHandle = new ElementHandle[1];
        final ElementHandle[] collectionAddHandle = new ElementHandle[1];
        final ElementHandle[] annonClassHandle = new ElementHandle[1];
        
        js.runUserActionTask(new Task<CompilationController>() {            
            public void run(CompilationController parameter) {
                JavacElements elements = (JavacElements) parameter.getElements();
                Element utilElement = elements.getPackageElement("java.util");
                assertNotNull (utilElement);
                utilElementHandle[0] = ElementHandle.create(utilElement);
                assertNotNull (utilElementHandle[0]);
                Element stringElement = elements.getTypeElement (String.class.getName());
                assertNotNull (stringElement);
                stringElementHandle[0] = ElementHandle.create(stringElement);
                assertNotNull (stringElementHandle[0]);
                Element[] stringMembers = getStringElements(stringElement);
                assertEquals(3,stringMembers.length);
                assertNotNull (stringMembers[0]);
                assertNotNull (stringMembers[1]);
                assertNotNull (stringMembers[2]);
                stringLengthElementHandle[0] = ElementHandle.create(stringMembers[0]);
                assertNotNull (stringLengthElementHandle[0]);
                stringConstructorElementHandle[0] = ElementHandle.create(stringMembers[1]);
                assertNotNull (stringConstructorElementHandle[0]);
                stringCountElementHandle[0] = ElementHandle.create(stringMembers[2]);
                assertNotNull (stringCountElementHandle[0]);
                Element retentionPolicy = elements.getTypeElement(java.lang.annotation.RetentionPolicy.class.getName());
                assertNotNull (retentionPolicy);
                retentionPolicyHandle[0] = ElementHandle.create(retentionPolicy);
                assertNotNull (retentionPolicyHandle[0]);
                List<? extends Element> members = ((TypeElement)retentionPolicy).getEnclosedElements();                    
                for (Element member : members) {
                    if (member.getKind() == ElementKind.ENUM_CONSTANT && "CLASS".contentEquals(((VariableElement)member).getSimpleName())) {
                        retentionPolicyClassHandle[0] = ElementHandle.create(member);
                        break;
                    }
                }
                assertNotNull (retentionPolicyClassHandle[0]);
                Element csl = elements.getTypeElement("java.util.Collections.SingletonList");
                assertNotNull (csl);
                innerClassHandle[0] = ElementHandle.create(csl);
                assertNotNull (innerClassHandle[0]);
                Element cadd = getCollectionAdd (elements.getTypeElement(java.util.Collection.class.getName()));
                assertNotNull (cadd);
                collectionAddHandle[0] = ElementHandle.create(cadd);
                assertNotNull (collectionAddHandle[0]);
                TypeElement annonClass = ElementUtils.getTypeElementByBinaryName(parameter, "java.lang.String$1"); //NOI18N
                assertNotNull (annonClass);
                annonClassHandle[0] = ElementHandle.create(annonClass);
                assertNotNull (annonClassHandle[0]);
            }
            
        },true);                
        
        
        assertTrue (utilElementHandle[0].equals(utilElementHandle[0]));
        assertTrue (stringElementHandle[0].equals(stringElementHandle[0]));
        assertTrue (stringLengthElementHandle[0].equals(stringLengthElementHandle[0]));
        assertTrue (stringConstructorElementHandle[0].equals(stringConstructorElementHandle[0]));
        assertTrue (stringCountElementHandle[0].equals(stringCountElementHandle[0]));
        assertTrue (retentionPolicyHandle[0].equals(retentionPolicyHandle[0]));
        assertTrue (retentionPolicyClassHandle[0].equals(retentionPolicyClassHandle[0]));
        assertTrue (innerClassHandle[0].equals(innerClassHandle[0]));
        assertTrue (collectionAddHandle[0].equals(collectionAddHandle[0]));
        assertTrue (annonClassHandle[0].equals(annonClassHandle[0]));
        
        assertFalse (utilElementHandle[0].equals(stringElementHandle[0]));
        assertFalse (utilElementHandle[0].equals(stringLengthElementHandle[0]));
        assertFalse (utilElementHandle[0].equals(stringConstructorElementHandle[0]));
        assertFalse (utilElementHandle[0].equals(stringCountElementHandle[0]));
        assertFalse (utilElementHandle[0].equals(retentionPolicyHandle[0]));
        assertFalse(utilElementHandle[0].equals(retentionPolicyClassHandle[0]));
        assertFalse (utilElementHandle[0].equals(innerClassHandle[0]));
        assertFalse (utilElementHandle[0].equals(collectionAddHandle[0]));
        assertFalse (utilElementHandle[0].equals(annonClassHandle[0]));        
        
        assertTrue (utilElementHandle[0].hashCode() == utilElementHandle[0].hashCode());
        assertTrue (stringElementHandle[0].hashCode() == stringElementHandle[0].hashCode());
        assertTrue (stringLengthElementHandle[0].hashCode() == stringLengthElementHandle[0].hashCode());
        assertTrue (stringConstructorElementHandle[0].hashCode() == stringConstructorElementHandle[0].hashCode());
        assertTrue (stringCountElementHandle[0].hashCode() == stringCountElementHandle[0].hashCode());
        assertTrue (retentionPolicyHandle[0].hashCode() == retentionPolicyHandle[0].hashCode());
        assertTrue (retentionPolicyClassHandle[0].hashCode() == retentionPolicyClassHandle[0].hashCode());
        assertTrue (innerClassHandle[0].hashCode() == innerClassHandle[0].hashCode());
        assertTrue (collectionAddHandle[0].hashCode() == collectionAddHandle[0].hashCode());
        assertTrue (annonClassHandle[0].hashCode() == annonClassHandle[0].hashCode());
        
        assertFalse (stringElementHandle[0].hashCode() == utilElementHandle[0].hashCode());
        assertFalse (stringLengthElementHandle[0].hashCode() == stringCountElementHandle[0].hashCode());    //Changed by ElementHandle 1.15
        assertFalse (stringLengthElementHandle[0].hashCode() == retentionPolicyHandle[0].hashCode());
        assertFalse (stringLengthElementHandle[0].hashCode() == retentionPolicyClassHandle[0].hashCode());
        assertFalse (stringLengthElementHandle[0].hashCode() == innerClassHandle[0].hashCode());
        assertFalse (stringLengthElementHandle[0].hashCode() == collectionAddHandle[0].hashCode());
        assertFalse (stringLengthElementHandle[0].hashCode() == annonClassHandle[0].hashCode());
        
    }
    
    
    public void testNames () throws Exception {
        final JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathProviderImpl.getDefault().findClassPath(data,ClassPath.BOOT), ClassPathProviderImpl.getDefault().findClassPath(data, ClassPath.COMPILE), null));
        assertNotNull(js);
        js.runUserActionTask(new Task<CompilationController>() {            
            public void run(CompilationController parameter) {
                final JavacElements elements = (JavacElements) parameter.getElements();
                Element element = elements.getTypeElement (String.class.getName());
                assertNotNull (element);
                ElementHandle handle = ElementHandle.create(element);
                assertEquals("java.lang.String", handle.getBinaryName());
                assertEquals("java.lang.String", handle.getQualifiedName());
                
                element = elements.getTypeElement(java.lang.annotation.RetentionPolicy.class.getName());
                assertNotNull (element);
                handle = ElementHandle.create(element);
                assertEquals("java.lang.annotation.RetentionPolicy", handle.getBinaryName());
                assertEquals("java.lang.annotation.RetentionPolicy", handle.getQualifiedName());
                
                element = elements.getTypeElement(java.io.Externalizable.class.getName());
                assertNotNull(element);
                handle = ElementHandle.create(element);
                assertEquals("java.io.Externalizable", handle.getBinaryName());
                assertEquals("java.io.Externalizable", handle.getQualifiedName());
                
                element = elements.getTypeElement(java.lang.annotation.RetentionPolicy.class.getName());
                assertNotNull(element);
                handle = ElementHandle.create(element);
                assertEquals("java.lang.annotation.RetentionPolicy", handle.getBinaryName());
                assertEquals("java.lang.annotation.RetentionPolicy", handle.getQualifiedName());
                
                element = ElementUtils.getTypeElementByBinaryName(parameter, "java.util.Collections$SingletonList");
                assertNotNull (element);                
                handle = ElementHandle.create(element);
                assertEquals("java.util.Collections$SingletonList", handle.getBinaryName());
                assertEquals("java.util.Collections.SingletonList", handle.getQualifiedName());
                
                element = elements.getPackageElement("java.lang");               
                assertNotNull (element);                
                handle = ElementHandle.create(element);
                try {
                    handle.getBinaryName();
                    assertFalse(true);
                } catch (IllegalStateException is) {                    
                }
                try {
                    handle.getQualifiedName();
                    assertFalse (true);
                } catch (IllegalStateException is) {
                    
                }
            }
            
        },true);
    }
    
    public void testTypesRankNPE () throws Exception {
        final JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathProviderImpl.getDefault().findClassPath(data,ClassPath.BOOT), ClassPathProviderImpl.getDefault().findClassPath(data, ClassPath.COMPILE), null));
        assertNotNull(js);
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                JavacElements elements = (JavacElements) parameter.getElements();
                TypeElement te = ElementUtils.getTypeElementByBinaryName(parameter, "java.lang.String$1");
                List<? extends Element> content = elements.getAllMembers(te);                
            }
        }, true);
    }
    
    public void testHandleClassBasedCompilations() throws Exception {
        ClassPath systemClasses = BootClassPathUtil.getModuleBootPath();
        FileObject jlObject = systemClasses.findResource("java/lang/Object.class");
        assertNotNull(jlObject);
        ClasspathInfo cpInfo = new ClasspathInfo.Builder(BootClassPathUtil.getBootClassPath())
                                                .setModuleBootPath(systemClasses)
                                                .build();
        JavaSource js = JavaSource.create(cpInfo, jlObject);
        assertNotNull(js);
        SourceLevelQueryImpl.getDefault().setSourceLevel(jlObject, "11");
        js.runUserActionTask(cc -> {
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            ElementHandle.create(cc.getTopLevelElements().get(0)).resolve(cc);
            Elements elements = cc.getElements();
            TypeElement te = elements.getTypeElement("java.lang.String");
            ElementHandle.create(te).resolve(cc);
        }, true);
        SourceLevelQueryImpl.getDefault().setSourceLevel(jlObject, null);
    }

    private Element[] getStringElements (Element stringElement) {
        List<? extends Element> members = ((TypeElement)stringElement).getEnclosedElements();
        Element[] result = new Element[3];
        for (Element member : members) {
            if (member.getKind() == ElementKind.METHOD && "length".contentEquals(member.getSimpleName())) {
                result[0] = member;
            }            
            if (member.getKind() == ElementKind.CONSTRUCTOR) {
                List<? extends VariableElement> params = ((ExecutableElement)member).getParameters();
                if (params.size() == 1) {
                    TypeMirror type = params.get(0).asType();
                    if (type.getKind() == TypeKind.DECLARED) {
                        TypeElement element = (TypeElement)((DeclaredType)type).asElement();
                        if (java.lang.StringBuilder.class.getName().contentEquals(element.getQualifiedName())) {
                            result[1] = element;
                        }
                    }
                }
            }
            if (member.getKind() == ElementKind.FIELD) {
                VariableElement ve = (VariableElement) member;
                if ("value".contentEquals(ve.getSimpleName())) {
                    result[2] = member;
                }
            }
        }
        return result;
    }            
    
    
    
    private Element getCollectionAdd (Element collectionElement) {
        List<? extends Element> members = ((TypeElement)collectionElement).getEnclosedElements();
        for (Element member : members) {
            if (member.getKind() == ElementKind.METHOD && "add".contentEquals(member.getSimpleName())) {
                return member;
            }
        }
        return null;
    }
    
    
    private static class ClassPathProviderImpl implements ClassPathProvider {
        
        private static ClassPathProviderImpl instance;
        
        private ClassPath compile;
        private ClassPath boot;
        private ClassPath src;
        
        private ClassPathProviderImpl () {
            
        }
        
        public synchronized ClassPath findClassPath(FileObject file, String type) {
            if (ClassPath.COMPILE.equals(type)) {
                return compile;
            }
            else if (ClassPath.BOOT.equals(type)) {
                return boot;
            }
            else if (ClassPath.SOURCE.equals(type)) {
                return src;
            }
            else {
                return null;
            }
        }
        
        public synchronized void setClassPaths (ClassPath boot, ClassPath compile, ClassPath src) {
            this.boot = boot;
            this.compile = compile;
            this.src = src;
        }
        
        public static synchronized ClassPathProviderImpl getDefault () {
            if (instance == null) {
                instance = new ClassPathProviderImpl ();
            }
            return instance;
        }
    }
    
    private static class SourceLevelQueryImpl implements SourceLevelQueryImplementation {

        private static SourceLevelQueryImpl instance;

        private final Map<FileObject, String> sourceLevels = new HashMap<>();

        private SourceLevelQueryImpl() {}

        @Override
        public synchronized String getSourceLevel(FileObject javaFile) {
            return sourceLevels.get(javaFile);
        }

        public synchronized void setSourceLevel(FileObject file, String sourceLevel) {
            if (sourceLevel != null) {
                sourceLevels.put(file, sourceLevel);
            } else {
                sourceLevels.remove(file);
            }
        }

        public static synchronized SourceLevelQueryImpl getDefault () {
            if (instance == null) {
                instance = new SourceLevelQueryImpl();
            }
            return instance;
        }
    }

    static {
        System.setProperty("CachingArchiveProvider.disableCtSym", "true");
    }
}
