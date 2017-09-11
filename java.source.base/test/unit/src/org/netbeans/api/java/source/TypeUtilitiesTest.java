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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
        TestUtilities.copyStringToFile(src, "package test; public class Test {}");
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), ClassPathSupport.createClassPath(new URL[0]), ClassPathSupport.createClassPath(new URL[0])), src);
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController info) throws IOException  {
                info.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement context = info.getTopLevelElements().get(0);
                assertEquals("java.util.List<java.lang.String>[]", info.getTypeUtilities().getTypeName(info.getTreeUtilities().parseType("java.util.List<java.lang.String>[]", context), TypeUtilities.TypeNameOptions.PRINT_FQN));
                assertEquals("List<String>[]", info.getTypeUtilities().getTypeName(info.getTreeUtilities().parseType("java.util.List<java.lang.String>[]", context)));
                assertEquals("java.util.List<java.lang.String>...", info.getTypeUtilities().getTypeName(info.getTreeUtilities().parseType("java.util.List<java.lang.String>[]", context), TypeUtilities.TypeNameOptions.PRINT_FQN, TypeUtilities.TypeNameOptions.PRINT_AS_VARARG));
                assertEquals("List<String>...", info.getTypeUtilities().getTypeName(info.getTreeUtilities().parseType("java.util.List<java.lang.String>[]", context), TypeUtilities.TypeNameOptions.PRINT_AS_VARARG));
            }
        }, true);
        
    }
    
}
