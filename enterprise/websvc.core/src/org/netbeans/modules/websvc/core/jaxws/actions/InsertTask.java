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

package org.netbeans.modules.websvc.core.jaxws.actions;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.openide.filesystems.FileObject;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

/** This is task for inserting field annotated with @WebServiceRef annotation
 * (only available since Java EE 5 version - in objects manageable by container(servlets, EJBs, Web Services)
 */
class InsertTask implements CancellableTask<WorkingCopy> {
    
    private final String serviceJavaName;
    private final String serviceFName;
    private final String wsdlUrl;
    private final boolean containsWsRefInjection;
    private final PolicyManager manager;

    public InsertTask(String serviceJavaName, String serviceFName, String wsdlUrl,
            PolicyManager manager , boolean containsWsRefInjection) 
    {
        this.serviceJavaName = serviceJavaName;
        this.serviceFName = serviceFName;
        this.wsdlUrl = wsdlUrl;
        this.containsWsRefInjection = containsWsRefInjection;
        this.manager = manager;
    }

    public void run(WorkingCopy workingCopy) throws IOException {
        workingCopy.toPhase(Phase.RESOLVED);
        TreeMaker make = workingCopy.getTreeMaker();
        ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
        
        
        TypeElement classElement = (TypeElement)workingCopy.getTrees().
            getElement(TreePath.getPath( workingCopy.getCompilationUnit(), 
                javaClass));
        
        if (javaClass != null) {
            ClassTree modifiedClass = generateWsServiceRef(workingCopy, make, javaClass);
            if ( manager.isSupported() ){
                modifiedClass = modifyJavaClass(workingCopy, make,
                    modifiedClass, classElement);
            }
            workingCopy.rewrite(javaClass, modifiedClass);
        }
    }

    public void cancel() {
    }
    
    private ClassTree generateWsServiceRef(WorkingCopy workingCopy,
            TreeMaker make, ClassTree javaClass)
    {
        if ( containsWsRefInjection ) {
            return javaClass;
        }
        //TypeElement wsRefElement = workingCopy.getElements().getTypeElement("javax.xml.ws.WebServiceRef"); //NOI18N
        AnnotationTree wsRefAnnotation = make.Annotation(
                make.QualIdent("javax.xml.ws.WebServiceRef"),
                Collections.<ExpressionTree>singletonList(make.Assignment(make.
                        Identifier("wsdlLocation"), make.Literal(wsdlUrl)))); //NOI18N
        // create field modifier: private(static) with @WebServiceRef annotation
        FileObject targetFo = workingCopy.getFileObject();
        Set<Modifier> modifiers = EnumSet.of(Modifier.PRIVATE);
        if (Car.getCar(targetFo) != null) {
            modifiers.add(Modifier.STATIC);
        }
        ModifiersTree methodModifiers = make.Modifiers(
                modifiers,
                Collections.<AnnotationTree>singletonList(wsRefAnnotation));
        TypeElement typeElement = workingCopy.getElements().getTypeElement(serviceJavaName);
        VariableTree serviceRefInjection = make.Variable(
            methodModifiers,
            serviceFName,
            (typeElement != null ? make.Type(typeElement.asType()) : 
                make.Identifier(serviceJavaName)),
            null);
        
        ClassTree modifiedClass = make.insertClassMember(javaClass, 0, 
                serviceRefInjection);
        return modifiedClass;
    }
    
    private ClassTree modifyJavaClass( WorkingCopy workingCopy,
            TreeMaker make, ClassTree javaClass, TypeElement classElement )
    {
        Collection<String> existingImports = getImports(workingCopy);
        CompilationUnitTree original = workingCopy.getCompilationUnit();
        CompilationUnitTree modified = original;
        for (String imp : manager.getImports()) {
            if (!existingImports.contains(imp)) {
                modified = make.addCompUnitImport(
                        modified, make.Import(make.Identifier(imp), false));
            }
        }
        workingCopy.rewrite(original, modified);
        return insertSecurityFetaureField(workingCopy, make, javaClass, 
                classElement);
    }
    
    private ClassTree insertSecurityFetaureField(WorkingCopy workingCopy,
            TreeMaker make, ClassTree javaClass, TypeElement classElement )
    {
        for (VariableElement var : 
            ElementFilter.fieldsIn( classElement.getEnclosedElements())) 
        {
            TypeMirror varType = var.asType();
            if (varType.getKind() != TypeKind.ARRAY) {
                continue;
            }
            if ( var.getSimpleName().contentEquals(PolicyManager.SECURITY_FEATURE)) {
                /*
                 * there is no way to find existing comments. So if field is
                 * already in the class. Just return
                 */
                return javaClass;
            }
        }

        Set<Modifier> modifiers = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        
        ModifiersTree modifiersTree = make.Modifiers(
                modifiers);
        
        Tree typeTree = manager.createSecurityFeatureType( workingCopy , make );
        ExpressionTree initializer = manager.createSecurityFeatureInitializer( 
                workingCopy, make );
        VariableTree securityFeature = make.Variable(
                modifiersTree, PolicyManager.SECURITY_FEATURE,      
                typeTree,
                initializer);
        if ( manager.isSupported() ){
            manager.modifySecurityFeatureAttribute( securityFeature , workingCopy , 
                make );
        }
        return make.insertClassMember(javaClass, 0, securityFeature);
    }
    
    public static Collection<String> getImports(CompilationController controller) {
        Set<String> imports = new HashSet<String>();
        CompilationUnitTree cu = controller.getCompilationUnit();
        
        if (cu != null) {
            List<? extends ImportTree> importTrees = cu.getImports();
            
            for (ImportTree importTree : importTrees) {
                imports.add(importTree.getQualifiedIdentifier().toString());
            }
        }
        
        return imports;
    }
}