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
package org.netbeans.modules.web.beans.testutilities;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

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

    public CdiTestUtilities(FileObject fileObject, boolean jakartaVariant) {
        this.mySourceRoot = fileObject;
        this.jakartaVariant = jakartaVariant;
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
                "import " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject.*; "+
                "import " + (jakartaVariant ? "jakarta" : "javax") + ".inject.*; "+
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
                "import " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject.*; "+
                "import " + (jakartaVariant ? "jakarta" : "javax") + ".inject.*; "+
                "import java.lang.annotation.*; "+
                "import " + (jakartaVariant ? "jakarta" : "javax") + ".interceptor.*; "+
                "@InterceptorBinding " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, TYPE}) "+
                "public @interface "+name+"  {} ");
    }
    
    public void initEnterprise()  throws IOException{
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/Singleton.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".ejb; " +
                "import static java.lang.annotation.ElementType; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.TYPE}) "+          
                "public @interface Singleton  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/Stateless.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".ejb; " +
                "import static java.lang.annotation.ElementType; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.TYPE}) "+          
                "public @interface Stateless  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/Stateful.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".ejb; " +
                "import static java.lang.annotation.ElementType; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.TYPE}) "+          
                "public @interface Stateful  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/PostActivate.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".ejb; " +
                "import static java.lang.annotation.ElementType; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.METHOD}) "+          
                "public @interface PostActivate  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/ejb/PrePassivate.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".ejb; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".inject; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.ANNOTATION_TYPE}) "+          
                "public @interface Qualifier  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/inject/Named.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".inject; " +
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Qualifier "+
                "public @interface Named  { " +
                " String value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/inject/Inject.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".inject; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import " + (jakartaVariant ? "jakarta" : "javax") + ".inject.Qualifier; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+          
                "public @interface Any  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/New.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import " + (jakartaVariant ? "jakarta" : "javax") + ".inject.Qualifier; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({FIELD, PARAMETER}) "+          
                "public @interface New  { " +
                " Class<?> value() ; "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Default.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import " + (jakartaVariant ? "jakarta" : "javax") + ".inject.Qualifier; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+          
                "public @interface Default  {} ");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Produces.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD }) "+          
                "public @interface Produces  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/util/Nonbinding.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.util; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD }) "+          
                "public @interface Nonbinding  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/event/Observes.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.event; " +
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({PARAMETER}) "+          
                "public @interface Observes  {}");
        
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Disposes.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({PARAMETER}) "+          
                "public @interface Disposes  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Specializes.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE,METHOD }) "+          
                "public @interface Specializes  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/Alternative.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ANNOTATION_TYPE}) "+          
                "public @interface Stereotype  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/NormalScope.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.context; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".inject; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ANNOTATION_TYPE}) "+          
                "public @interface Scope  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/ApplicationScoped.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.context; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.context; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.context; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import " + (jakartaVariant ? "jakarta" : "javax") + ".inject.Scope; "+
                "@Retention(RUNTIME) "+
                "@Scope "+
                "@Inherited "+
                "@Target({METHOD, FIELD, TYPE}) "+          
                "public @interface Dependent  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/RequestScoped.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.context; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.context; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.event; " +
                "public interface Event<T>  {" +
                " void fire( T event ); "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/decorator/Delegate.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".decorator; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".decorator; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject; " +
                "public interface Instance<T>  extends java.lang.Iterable<T> {" +
                " void fire( T event ); "+
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/spi/Extension.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject.spi; " +
                "public interface Extension  {}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/interceptor/InterceptorBinding.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".interceptor; " +
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ANNOTATION_TYPE}) "+      
                "public @interface InterceptorBinding  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/interceptor/Interceptor.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".interceptor; " +
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE}) "+      
                "public @interface Interceptor  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/interceptor/Interceptors.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".interceptor; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".ejb; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".ejb; " +
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
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".ejb; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD}) "+      
                "public @interface PostActivate  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/inject/spi/InjectionPoint.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.inject.spi; " +
                "public interface InjectionPoint  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(mySourceRoot, "javax/enterprise/context/spi/Context.java",
                "package " + (jakartaVariant ? "jakarta" : "javax") + ".enterprise.context.spi; " +
                "public interface Context  {" +
                "}");
    }

    public Path setupJakartaMarker() throws IOException {
        Path tempDir = Files.createTempDirectory("jakartaMarker");
        Path packageDir;
        if(jakartaVariant) {
            packageDir = tempDir.resolve("jakarta/inject");
        } else {
            packageDir = tempDir.resolve("javax/inject");
        }
        Files.createDirectories(packageDir);
        TestUtilities.copyStringToFile(packageDir.resolve("Qualifier.class").toFile(), "");
        return tempDir;
    }

    public void deleteTree(Path targetPath) throws IOException {
        Files.walkFileTree(targetPath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path t, BasicFileAttributes bfa) throws IOException {
                Files.delete(t);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
                Files.delete(t);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private final FileObject mySourceRoot;
    private final boolean jakartaVariant;
}
