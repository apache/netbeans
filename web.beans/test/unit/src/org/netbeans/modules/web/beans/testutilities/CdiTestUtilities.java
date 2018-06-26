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
package org.netbeans.modules.web.beans.testutilities;

import java.io.IOException;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelFactory;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class CdiTestUtilities {
    
    public CdiTestUtilities( FileObject fileObject ){
        mySourceRoot = fileObject;
    }
    
    public void clearRoot() throws IOException {
        FileObject[] children = mySourceRoot.getChildren();
        for (FileObject fileObject : children) {
            fileObject.delete();
        }
    }
    
    public MetadataModel<WebBeansModel> createBeansModel() throws IOException, InterruptedException {
        ModelUnit modelUnit = ModelUnit.create(
                ClassPath.getClassPath(mySourceRoot, ClassPath.BOOT),
                ClassPath.getClassPath(mySourceRoot, ClassPath.COMPILE),
                ClassPath.getClassPath(mySourceRoot, ClassPath.SOURCE), null);
        return WebBeansModelFactory.createMetaModel(modelUnit);
    }
    
    public void createQualifier(String name ) throws IOException{
        TestUtilities.copyStringToFileObject(mySourceRoot, "foo/"+name+".java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface "+name+"  {} ");
    }
    
    public void createInterceptorBinding(String name ) throws IOException{
        TestUtilities.copyStringToFileObject(mySourceRoot, "foo/"+name+".java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "public @interface "+name+"  {} ");
    }
    
    public void initEnterprise()  throws IOException{
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/Singleton.java",
                "package javax.ejb; " +
                "import static java.lang.annotation.ElementType; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.TYPE}) "+          
                "public @interface Singleton  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/Stateless.java",
                "package javax.ejb; " +
                "import static java.lang.annotation.ElementType; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.TYPE}) "+          
                "public @interface Stateless  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/Stateful.java",
                "package javax.ejb; " +
                "import static java.lang.annotation.ElementType; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.TYPE}) "+          
                "public @interface Stateful  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/PostActivate.java",
                "package javax.ejb; " +
                "import static java.lang.annotation.ElementType; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.METHOD}) "+          
                "public @interface PostActivate  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/PrePassivate.java",
                "package javax.ejb; " +
                "import static java.lang.annotation.ElementType; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.METHOD}) "+          
                "public @interface PrePassivate  {}");
    }
    
    public  void initAnnotations() throws IOException{
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/inject/Qualifier.java",
                "package javax.inject; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.ANNOTATION_TYPE}) "+          
                "public @interface Qualifier  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/inject/Named.java",
                "package javax.inject; " +
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Qualifier "+
                "public @interface Named  { " +
                " String value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/inject/Inject.java",
                "package javax.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.CONSTRUCTOR; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, CONSTRUCTOR}) "+
                "public @interface Inject  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Any.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.inject.Qualifier; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+          
                "public @interface Any  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/New.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.inject.Qualifier; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({FIELD, PARAMETER}) "+          
                "public @interface New  { " +
                " Class<?> value() ; "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Default.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.Qualifier; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+          
                "public @interface Default  {} ");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Produces.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD }) "+          
                "public @interface Produces  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/util/Nonbinding.java",
                "package javax.enterprise.util; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD }) "+          
                "public @interface Nonbinding  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/event/Observes.java",
                "package javax.enterprise.event; " +
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({PARAMETER}) "+          
                "public @interface Observes  {}");
        
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Disposes.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({PARAMETER}) "+          
                "public @interface Disposes  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Specializes.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE,METHOD }) "+          
                "public @interface Specializes  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Alternative.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, TYPE}) "+          
                "public @interface Alternative  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Stereotype.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ANNOTATION_TYPE}) "+          
                "public @interface Stereotype  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/NormalScope.java",
                "package javax.enterprise.context; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ANNOTATION_TYPE}) "+          
                "public @interface NormalScope  {" +
                " boolean passivating() default faslse ; "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/inject/Scope.java",
                "package javax.inject; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ANNOTATION_TYPE}) "+          
                "public @interface Scope  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/ApplicationScoped.java",
                "package javax.enterprise.context; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@NormalScope "+
                "@Inherited "+
                "@Target({METHOD, FIELD, TYPE}) "+          
                "public @interface ApplicationScoped  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/ConversationScoped.java",
                "package javax.enterprise.context; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@NormalScope(passivating=true) "+
                "@Inherited "+
                "@Target({METHOD, FIELD, TYPE}) "+          
                "public @interface ConversationScoped  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/Dependent.java",
                "package javax.enterprise.context; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.inject.Scope; "+
                "@Retention(RUNTIME) "+
                "@Scope "+
                "@Inherited "+
                "@Target({METHOD, FIELD, TYPE}) "+          
                "public @interface Dependent  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/RequestScoped.java",
                "package javax.enterprise.context; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@NormalScope "+
                "@Inherited "+
                "@Target({METHOD, FIELD, TYPE}) "+          
                "public @interface RequestScoped  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/SessionScoped.java",
                "package javax.enterprise.context; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@NormalScope(passivating=true) "+
                "@Inherited "+
                "@Target({METHOD, FIELD, TYPE}) "+          
                "public @interface SessionScoped  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Typed.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, TYPE}) "+          
                "public @interface Typed  {" +
                " Class<?>[] value() ; "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/event/Event.java",
                "package javax.enterprise.event; " +
                "public interface Event<T>  {" +
                " void fire( T event ); "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/decorator/Delegate.java",
                "package javax.decorator; " +
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({FIELD, PARAMETER}) "+          
                "public @interface Delegate  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/decorator/Decorator.java",
                "package javax.decorator; " +
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE}) "+      
                "@javax.enterprise.inject.Stereotype "+
                "public @interface Decorator  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Instance.java",
                "package javax.enterprise.inject; " +
                "public interface Instance<T>  extends java.lang.Iterable<T> {" +
                " void fire( T event ); "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/spi/Extension.java",
                "package javax.enterprise.inject.spi; " +
                "public interface Extension  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/interceptor/InterceptorBinding.java",
                "package javax.interceptor; " +
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ANNOTATION_TYPE}) "+      
                "public @interface InterceptorBinding  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/interceptor/Interceptor.java",
                "package javax.interceptor; " +
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE}) "+      
                "public @interface Interceptor  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/interceptor/Interceptors.java",
                "package javax.interceptor; " +
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE,METHOD}) "+      
                "public @interface Interceptors  {" +
                " Class<?>[] value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/Singleton.java",
                "package javax.ejb; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE}) "+      
                "public @interface Singleton  {" +
                " String name() default \"\"; "+
                " String description() default \"\"; "+
                " String mappedName() default \"\"; "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/Stateful.java",
                "package javax.ejb; " +
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE}) "+      
                "public @interface Stateful  {" +
                " String name() default \"\"; "+
                " String description() default \"\"; "+
                " String mappedName() default \"\"; "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/PostActivate.java",
                "package javax.ejb; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD}) "+      
                "public @interface PostActivate  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/spi/InjectionPoint.java",
                "package javax.enterprise.inject.spi; " +
                "public interface InjectionPoint  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/spi/Context.java",
                "package javax.enterprise.context.spi; " +
                "public interface Context  {" +
                "}");
    }
    
    private FileObject mySourceRoot;
}
