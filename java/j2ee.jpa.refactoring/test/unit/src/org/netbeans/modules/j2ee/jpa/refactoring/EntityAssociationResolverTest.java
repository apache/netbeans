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

package org.netbeans.modules.j2ee.jpa.refactoring;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation.EntityMappingsMetadataModelFactory;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Erno Mononen
 */
public class EntityAssociationResolverTest extends SourceTestSupport {
    
    private static final String PKG  = "entities.";
    private static final String CUSTOMER = PKG + "Customer";
    private static final String ORDER = PKG +  "Order";
    private static final String DEPARTMENT = PKG + "Department";
    private static final String EMPLOYEE = PKG + "Employee";
    private static final String USER = PKG + "User";
    private static final String GROUP = PKG + "Group";
    
    public EntityAssociationResolverTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    private String getPath(){
        return getDataDir().getAbsoluteFile().toString();
    }
    
    private FileObject getJavaFile(String name){
        return FileUtil.toFileObject(new File(getPath() +"/" + name.replace('.', '/') + ".java"));
    }
    
    protected FileObject[] getClassPathRoots(){
        return new FileObject[]{FileUtil.toFileObject(new File(getPath()))};
    }
    
    private TreePathHandle getTreePathHandle(final String fieldName, String className) throws IOException{
        return RefactoringUtil.getTreePathHandle(fieldName, className, getJavaFile(className));
    }
    
    private MetadataModel<EntityMappingsMetadata> createModel() throws IOException, InterruptedException{
        FileObject src = FileUtil.toFileObject(new File(getPath()));
        IndexingManager.getDefault().refreshIndexAndWait(src.getURL(), null);
        return  EntityMappingsMetadataModelFactory.createMetadataModel(
                ClassPath.getClassPath(src, ClassPath.BOOT),
                ClassPath.getClassPath(src, JavaClassPathConstants.MODULE_BOOT_PATH),
                ClassPath.getClassPath(src, ClassPath.COMPILE),
                ClassPath.getClassPath(src, JavaClassPathConstants.MODULE_COMPILE_PATH),
                ClassPath.getClassPath(src, JavaClassPathConstants.MODULE_CLASS_PATH),
                ClassPath.getClassPath(src, ClassPath.SOURCE),
                ClassPath.getClassPath(src, JavaClassPathConstants.MODULE_SOURCE_PATH));
    }

    /**
     * TODO, resolve fail
     * currently it fails in EntityAssociationResolver
     * on TypeElement te = info.getElements().getTypeElement(targetClass);
     * because te==null after this line, can't debug inside of com.sun.tools.javac, need additional invesigation
     * @throws Exception
     */
//    public void testGetTarget() throws Exception {
//        EntityAssociationResolver resolver = new EntityAssociationResolver(getTreePathHandle("customer", ORDER), createModel());
//        List<EntityAssociationResolver.Reference> orderRefs = resolver.getReferringProperties();
//        assertEquals(2, orderRefs.size());
//
//        EntityAssociationResolver.Reference fieldRef = orderRefs.get(0);
//        assertEquals(CUSTOMER, fieldRef.getClassName());
//        assertEquals("orders", fieldRef.getPropertyName());
//        assertEquals("customer", fieldRef.getSourceProperty());
//
//        EntityAssociationResolver.Reference propertyRef = orderRefs.get(1);
//        assertEquals(CUSTOMER, propertyRef.getClassName());
//        assertEquals("getOrders", propertyRef.getPropertyName());
//        assertEquals("customer", propertyRef.getSourceProperty());
//
//
//    }
    
   /**
     * TODO, resolve fail
     * @throws Exception
     */
//    public void testResolveReferences() throws Exception {
//        EntityAssociationResolver resolver = new EntityAssociationResolver(getTreePathHandle("customer", ORDER), createModel());
//        List<EntityAnnotationReference> result = resolver.resolveReferences();
//        assertEquals(1, result.size());
//        EntityAnnotationReference reference = result.get(0);
//        assertEquals(EntityAssociationResolver.ONE_TO_MANY, reference.getAnnotation());
//        assertEquals("entities.Customer", reference.getEntity());
//        assertEquals(EntityAssociationResolver.MAPPED_BY, reference.getAttribute());
//        assertEquals("customer", reference.getAttributeValue());
//
//    }
    
    public void testGetTreePathHandle() throws Exception{
        final TreePathHandle handle  = RefactoringUtil.getTreePathHandle("orders", CUSTOMER, getJavaFile(CUSTOMER));
        JavaSource source = JavaSource.forFileObject(handle.getFileObject());
        source.runUserActionTask(new CancellableTask<CompilationController>(){
            
            public void cancel() {
            }
            
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                Element element = handle.resolveElement(parameter);
                assertEquals("orders", element.getSimpleName().toString());
                for (AnnotationMirror annotation : element.getAnnotationMirrors()){
                    assertEquals(EntityAssociationResolver.ONE_TO_MANY, annotation.getAnnotationType().toString());
                }
            }
        }, true);
        
    }
    
}
