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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class DecoratorTest extends CommonTestCase {

    public DecoratorTest( String testName ) {
        super(testName);
    }
    
    public void testSimple() throws IOException, InterruptedException {
        createQualifier("Binding");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest extends One {"+
                " @Inject @Delegate @Binding One myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest1 extends One {"+
                " @Inject SimpleTest1( @Delegate One param ) {} "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@Binding "+
                "public class Two extends One {}");
        
        inform("start simple  decorators tests");
        
        TestWebBeansModelImpl modelImpl = createModelImpl();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkDecorator(model, "foo.One" , "foo.SimpleTest1" );
                
                checkDecorator(model, "foo.Two" , "foo.SimpleTest");
                
                return null;
            }

        });
    }
    
    public void testMultipleQualifiers() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.enterprise.util.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {" +
                "    String value(); "+
                "    @Nonbinding String comment() default \"\"; "+
                "}");
        createQualifier("Binding2");
        createQualifier("Binding3");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest extends One {"+
                " @Inject @Delegate @Binding1(value=\"a\", comment=\"a\") @Binding2 One myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest1 extends One {"+
                " @Inject @Delegate @Binding2 One myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@Binding1(value=\"a\", comment=\"b\") "+
                "public class One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding2 "+
                "@Specializes "+
                "public class Two extends One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding2 @Binding3 "+
                "public class Three extends One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Four.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1(value=\"b\", comment=\"a\") @Binding3 "+
                "public class Four extends One {}");
        
        inform("start multiple decorators tests");
        
        TestWebBeansModelImpl modelImpl = createModelImpl();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkDecorator(model, "foo.One" );
                
                checkDecorator(model, "foo.Two" , "foo.SimpleTest", "foo.SimpleTest1");
                
                checkDecorator(model, "foo.Three" ,  "foo.SimpleTest1");
                
                checkDecorator(model, "foo.Four" );
                
                return null;
            }

        });
    }
    
    /*
     * @Default, @New , @Any
     */
    public void testCornerCasesQualifiers() throws IOException, InterruptedException {
        createQualifier("Binding1");
        createQualifier("Binding2");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest extends One {"+
                " @Inject @Delegate @Binding1 @Default One myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest1 extends One {"+
                " @Inject @Delegate One myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest2 extends One {"+
                " @Inject @Delegate @Any Three myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SimpleTest3.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                "public class SimpleTest3 extends One {"+
                " @Inject @Delegate @New Four myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@Binding1 "+
                "public class One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Specializes "+
                "public class Two extends One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class Three extends One {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Four.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "@Default @Named "+
                "public class Four extends One {}");
        
        inform("start multiple decorators tests");
        
        TestWebBeansModelImpl modelImpl = createModelImpl();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            @Override
            public Void run( WebBeansModel model ) throws Exception {
                checkDecorator(model, "foo.One" );
                
                checkDecorator(model, "foo.Two" , "foo.SimpleTest", "foo.SimpleTest1");
                
                checkDecorator(model, "foo.Three" ,  "foo.SimpleTest1", "foo.SimpleTest2");
                
                checkDecorator(model, "foo.Four" ,  "foo.SimpleTest1", "foo.SimpleTest3");
                
                return null;
            }

        });
    }
    
    private void checkDecorator( WebBeansModel model, String className ,
            String... decorators ) 
    {
        TypeMirror mirror = model.resolveType( className);
        Element clazz = ((DeclaredType)mirror).asElement();
        TypeElement type = (TypeElement)clazz;
        
        Collection<TypeElement> decoratorElements = model.getDecorators( type );
        assertEquals( "Class "+className+" should have exactly "+
                decorators.length+" decorator(s)", decorators.length ,  
                decoratorElements.size());
        Set<String> set =new HashSet<String>();
        set.addAll( Arrays.asList( decorators ));
        
        Set<String> fqns = new HashSet<String>();
        for (TypeElement typeElement : decoratorElements) {
            fqns.add( typeElement.getQualifiedName().toString());
        }
        assertTrue( fqns.containsAll( set ));
    }

}