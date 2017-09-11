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

package org.netbeans.api.java.source;

import com.sun.tools.javac.model.JavacElements;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import junit.framework.*;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
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
        ClassPathProviderImpl.getDefault().setClassPaths(createBootPath(),ClassPathSupport.createClassPath(new URL[0]),ClassPathSupport.createClassPath(new FileObject[]{this.src}));
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
                Element csl = elements.getTypeElementByBinaryName("java.util.Collections$SingletonList");
                assertNotNull (csl);
                innerClassHandle[0] = ElementHandle.create(csl);
                assertNotNull (innerClassHandle[0]);
                Element cadd = getCollectionAdd (elements.getTypeElement(java.util.Collection.class.getName()));
                assertNotNull (cadd);
                collectionAddHandle[0] = ElementHandle.create(cadd);
                assertNotNull (collectionAddHandle[0]);
                TypeElement annonClass = elements.getTypeElementByBinaryName("java.lang.String$1"); //NOI18N
                assertNotNull (annonClass);
                annonClassHandle[0] = ElementHandle.create(annonClass);
                assertNotNull (annonClassHandle[0]);
                TypeElement listClass = elements.getTypeElementByBinaryName("java.util.List"); //NOI18N
                assertNotNull(listClass);
                List<? extends TypeParameterElement> tpes = listClass.getTypeParameters();
                assertEquals(tpes.size(), 1);
                genParList[0] = ElementHandle.create(tpes.get(0));
                TypeElement collsClass = elements.getTypeElementByBinaryName("java.util.Collections"); //NOI18N
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
                Element csl = elements.getTypeElementByBinaryName("java.util.Collections$SingletonList");
                assertNotNull (csl);
                assertEquals(resolved,csl);
                resolved = collectionAddHandle[0].resolve(parameter);
                assertNotNull(resolved);
                Element cadd = getCollectionAdd(elements.getTypeElement(java.util.Collection.class.getName()));
                assertNotNull(cadd);
                assertEquals (resolved, cadd);
                resolved = annonClassHandle[0].resolve(parameter);
                assertNotNull (resolved);
                TypeElement annonClass = elements.getTypeElementByBinaryName("java.lang.String$1"); //NOI18N
                assertNotNull (annonClass);
                assertEquals(resolved,annonClass);
                
                TypeElement listClass = elements.getTypeElementByBinaryName("java.util.List"); //NOI18N
                assertNotNull(listClass);
                TypeParameterElement tpe = listClass.getTypeParameters().get(0);
                resolved = genParList[0].resolve(parameter);
                assertEquals(tpe, resolved);
                
                tpe = null;
                TypeElement collsClass = elements.getTypeElementByBinaryName("java.util.Collections"); //NOI18N
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
                TypeElement annonClass = elements.getTypeElementByBinaryName("java.lang.String$1"); //NOI18N
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
                Element csl = elements.getTypeElementByBinaryName("java.util.Collections$SingletonList");
                assertNotNull (csl);
                assertTrue(innerClassHandle[0].signatureEquals(csl));
                Element cadd = getCollectionAdd(elements.getTypeElement(java.util.Collection.class.getName()));
                assertNotNull(cadd);
                assertTrue (collectionAddHandle[0].signatureEquals(cadd));
                TypeElement annonClass = elements.getTypeElementByBinaryName("java.lang.String$1"); //NOI18N
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
                TypeElement annonClass = elements.getTypeElementByBinaryName("java.lang.String$1"); //NOI18N
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
                
                element = elements.getTypeElementByBinaryName("java.util.Collections$SingletonList");
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
                TypeElement te = elements.getTypeElementByBinaryName("java.lang.String$1");
                List<? extends Element> content = elements.getAllMembers(te);                
            }
        }, true);
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
    
    private static ClassPath createBootPath () throws IOException {
        String bootPath = System.getProperty ("sun.boot.class.path");   //NOI18N
        String[] paths = bootPath.split(File.pathSeparator);
        List<URL>roots = new ArrayList<URL> (paths.length);
        for (String path : paths) {
            File f = new File (path);            
            if (!f.exists()) {
                continue;
            }
            URL url = Utilities.toURI(f).toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            roots.add (url);
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }        
    
}
