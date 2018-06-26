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
package org.netbeans.modules.web.beans.model;

import java.io.IOException;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class ScopeTest extends CommonTestCase {

    public ScopeTest( String testName ) {
        super(testName);
    }
    
    public void testInheritedScope() throws IOException{ 
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.SessionScoped; "+
                "@SessionScoped "+
                "public class SuperClass  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/ChildClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.ApplicationScoped; "+
                "@ApplicationScoped "+
                "public class ChildClass  extends SuperClass{ " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SubClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class SubClass extends ChildClass{ " +
                "}" );
        
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.SuperClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.SessionScoped");
                
                mirror = model.resolveType( "foo.ChildClass" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                
                mirror = model.resolveType( "foo.SubClass" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                
                return null;
            }
            
        });
    }
    
    public void testDependentScope() throws IOException{ 
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "public class Clazz  { " +
                "}" );
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.Clazz" );
                Element clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.Dependent");
                return null;
            }
        });
    }
    
    public void testSterotypedScope() throws IOException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.SessionScoped; "+
                "@Stereotype "+
                "@SessionScoped "+
                "@Target({TYPE}) "+ 
                "@Retention(RUNTIME) "+
                "public @interface Stereotype1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class1.java",
                "package foo; " +
                "@Stereotype1 "+
                "public class Class1  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class2.java",
                "package foo; " +
                "import javax.enterprise.context.ApplicationScoped; "+
                "@Stereotype1 "+
                "@ApplicationScoped "+
                "public class Class2  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class3.java",
                "package foo; " +
                "import javax.enterprise.context.ApplicationScoped; "+
                "@Stereotype1 "+
                "public class Class3 extends Class2 { " +
                "}" );
        
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.Class1" );
                Element clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.SessionScoped");
                
                mirror = model.resolveType( "foo.Class2" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                
                mirror = model.resolveType( "foo.Class3" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                return null;
            }
        });
    }
    
    public void testCustomScope() throws IOException{ 
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomScope.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.*; "+
                "@NormalScope "+
                "@Target({TYPE,METHOD,FIELD}) "+ 
                "@Retention(RUNTIME) "+
                "@Inherited "+
                "public @interface CustomScope {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "@CustomScope "+
                "public class Clazz  { " +
                "}" );
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.Clazz" );
                Element clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "foo.CustomScope");
                return null;
            }
        });
    }
    
    public void testDefaultScope() throws IOException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.SessionScoped; "+
                "@Stereotype "+
                "@SessionScoped "+
                "@Target({TYPE}) "+ 
                "@Retention(RUNTIME) "+
                "public @interface Stereotype1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.SessionScoped; "+
                "@Stereotype "+
                "@SessionScoped "+
                "@Target({TYPE}) "+ 
                "@Retention(RUNTIME) "+
                "public @interface Stereotype2 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype3.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.enterprise.inject.*; "+
                "import javax.enterprise.context.ApplicationScoped; "+
                "@Stereotype "+
                "@ApplicationScoped "+
                "@Target({TYPE}) "+ 
                "@Retention(RUNTIME) "+
                "public @interface Stereotype3 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class1.java",
                "package foo; " +
                "@Stereotype1 "+
                "@Stereotype2 "+
                "public class Class1  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class2.java",
                "package foo; " +
                "@Stereotype1 "+
                "@Stereotype3 "+
                "public class Class2  { " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Class3.java",
                "package foo; " +
                "import javax.enterprise.context.ApplicationScoped; "+
                "@Stereotype1 "+
                "@Stereotype3 "+
                "@ApplicationScoped "+
                "public class Class3  { " +
                "}" );
        
        
        final TestWebBeansModelImpl modelImpl = createModelImpl(true );
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.Class1" );
                Element clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.SessionScoped");
                
                mirror = model.resolveType( "foo.Class3" );
                clazz = ((DeclaredType)mirror).asElement();
                checkScope(model, clazz, "javax.enterprise.context.ApplicationScoped");
                
                mirror = model.resolveType( "foo.Class2" );
                clazz = ((DeclaredType)mirror).asElement();
                boolean exception = false;
                
                try {
                    model.getScope(clazz);
                }
                catch(CdiException e ){
                    exception = true;
                }
                assertTrue( "Class2 has no explicit Scope but has different " +
                		"Stereotypes with different default Scopes. So it has no" +
                		" default scope and this is a problem", exception );
                return null;
            }
        });
    }

    private void checkScope(WebBeansModel model , Element element, 
            String fqn ){
        try {
            String scope = model.getScope(element);
            assertEquals("Not expected scope type", fqn, scope);
        }
        catch ( CdiException e ){
            throw new RuntimeException( e );
        }
    }
}
