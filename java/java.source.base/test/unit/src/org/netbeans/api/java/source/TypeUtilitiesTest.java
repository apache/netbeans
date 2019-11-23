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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class TypeUtilitiesTest extends NbTestCase {
    
    public TypeUtilitiesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsCastable() throws Exception {
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), ClassPathSupport.createClassPath(new URL[0]), ClassPathSupport.createClassPath(new URL[0])));
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController info)  {
                TypeElement jlStringElement = info.getElements().getTypeElement("java.lang.String");
                TypeMirror jlString = info.getTypes().getDeclaredType(jlStringElement);
                TypeElement jlIntegerElement = info.getElements().getTypeElement("java.lang.Integer");
                TypeMirror jlInteger = info.getTypes().getDeclaredType(jlIntegerElement);
                TypeElement juListElement = info.getElements().getTypeElement("java.util.List");
                TypeMirror juListString = info.getTypes().getDeclaredType(juListElement, jlString);
                TypeMirror juListInteger = info.getTypes().getDeclaredType(juListElement, jlInteger);
                TypeElement jlObjectElement = info.getElements().getTypeElement("java.lang.Object");
                TypeMirror jlObject = info.getTypes().getDeclaredType(jlObjectElement);
                TypeMirror primitiveChar = info.getTypes().getPrimitiveType(TypeKind.CHAR);
                
                TypeUtilities u = info.getTypeUtilities();
                
                assertTrue(u.isCastable(jlObject, jlString));
                assertTrue(u.isCastable(jlObject, jlInteger));
                assertTrue(u.isCastable(jlObject, juListString));
                
                assertFalse(u.isCastable(jlString, jlInteger));
                assertFalse(u.isCastable(jlInteger, jlString));
                assertFalse(u.isCastable(juListString, juListInteger));
                assertFalse(u.isCastable(juListInteger, juListString));
                
                //verify that the order of arguments is understood correctly:
                //(requires 1.5):
                //XXX: after d3ead6731a91, the types are castable in both directions
//                assertFalse(u.isCastable(jlObject, primitiveChar));
//                assertTrue(u.isCastable(primitiveChar, jlObject));
            }
        }, true);
        
    }

    public void testSubstitute() throws Exception {
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), ClassPathSupport.createClassPath(new URL[0]), ClassPathSupport.createClassPath(new URL[0])));
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController info)  {
                TypeElement jlStringElement = info.getElements().getTypeElement("java.lang.String");
                TypeMirror jlString = info.getTypes().getDeclaredType(jlStringElement);
                TypeElement juListElement = info.getElements().getTypeElement("java.util.List");
                TypeMirror juListString = info.getTypes().getDeclaredType(juListElement, jlString);
                
                DeclaredType juListType = (DeclaredType) juListElement.asType();
                TypeMirror substituted = info.getTypeUtilities().substitute(juListType, juListType.getTypeArguments(), Collections.singletonList(jlString));
                
                assertTrue(info.getTypes().isSameType(juListString, substituted));
                
                boolean wasThrown = false;
                
                try {
                    info.getTypeUtilities().substitute(juListType, juListType.getTypeArguments(), Collections.<TypeMirror>emptyList());
                } catch (IllegalArgumentException ex) {
                    wasThrown = true;
                }
                
                assertTrue(wasThrown);
            }
        }, true);
        
    }
    
    public void testTypeName() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject src  = root.createData("Test.java");
        TestUtilities.copyStringToFile(src, "package test; public class Test { { get().run(); } private <Z extends Exception&Runnable> Z get() { return null; } }");
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), ClassPathSupport.createClassPath(new URL[0]), ClassPathSupport.createClassPath(new URL[0])), src);
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController info) throws IOException  {
                info.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement context = info.getTopLevelElements().get(0);
                assertEquals("java.util.List<java.lang.String>[]", info.getTypeUtilities().getTypeName(info.getTreeUtilities().parseType("java.util.List<java.lang.String>[]", context), TypeUtilities.TypeNameOptions.PRINT_FQN));
                assertEquals("List<String>[]", info.getTypeUtilities().getTypeName(info.getTreeUtilities().parseType("java.util.List<java.lang.String>[]", context)));
                assertEquals("java.util.List<java.lang.String>...", info.getTypeUtilities().getTypeName(info.getTreeUtilities().parseType("java.util.List<java.lang.String>[]", context), TypeUtilities.TypeNameOptions.PRINT_FQN, TypeUtilities.TypeNameOptions.PRINT_AS_VARARG));
                assertEquals("List<String>...", info.getTypeUtilities().getTypeName(info.getTreeUtilities().parseType("java.util.List<java.lang.String>[]", context), TypeUtilities.TypeNameOptions.PRINT_AS_VARARG));
                ClassTree clazz = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
                BlockTree init = (BlockTree) clazz.getMembers().get(1);
                ExpressionStatementTree var = (ExpressionStatementTree) init.getStatements().get(0);
                ExpressionTree getInvocation = ((MemberSelectTree) ((MethodInvocationTree) var.getExpression()).getMethodSelect()).getExpression();
                TypeMirror intersectionType = info.getTrees().getTypeMirror(info.getTrees().getPath(info.getCompilationUnit(), getInvocation));
                assertEquals("Exception & Runnable", info.getTypeUtilities().getTypeName(intersectionType));
            }
        }, true);
        
    }
    
}
