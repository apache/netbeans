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

package org.netbeans.modules.websvc.core;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsNode;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author mkuchtiak
 */
public class MethodGenerator {
    FileObject implClassFo;
    WsdlModel wsdlModel;
    /** Creates a new instance of MethodGenerator */
    public MethodGenerator(WsdlModel wsdlModel, FileObject implClassFo) {
        this.implClassFo=implClassFo;
        this.wsdlModel=wsdlModel;
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void generateMethod(final String operationName) throws IOException {
        
        // Use Progress API to display generator messages.
        //ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(MethodGenerator.class, "TXT_AddingMethod")); //NOI18N
        //handle.start(100);
        
        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass!=null) {
              
                    // get proper wsdlOperation;
                    WsdlOperation wsdlOperation = getWsdlOperation(operationName);
                    
                    if (wsdlOperation != null) {
                        TreeMaker make = workingCopy.getTreeMaker();

                        // return type
                        String returnType = wsdlOperation.getReturnTypeName();

                        // create parameters
                        List<WsdlParameter> parameters = wsdlOperation.getParameters();
                        List<VariableTree> params = new ArrayList<VariableTree>();
                        for (WsdlParameter parameter:parameters) {
                            // create parameter:
                            params.add(make.Variable(
                                    make.Modifiers(
                                    Collections.<Modifier>emptySet(),
                                    Collections.<AnnotationTree>emptyList()
                                    ),
                                    parameter.getName(), // name
                                    make.Identifier(parameter.getTypeName()), // parameter type
                                    null // initializer - does not make sense in parameters.
                                    ));
                        }

                        // create exceptions
                        Iterator<String> exceptions = wsdlOperation.getExceptions();
                        List<ExpressionTree> exc = new ArrayList<ExpressionTree>();
                        while (exceptions.hasNext()) {
                            String exception = exceptions.next();
                            exc.add(make.Identifier(exception));
                        }

                        // create method
                        ModifiersTree methodModifiers = make.Modifiers(
                                Collections.<Modifier>singleton(Modifier.PUBLIC),
                                Collections.<AnnotationTree>emptyList()
                                );
                        MethodTree method = make.Method(
                                methodModifiers, // public
                                wsdlOperation.getJavaName(), // operation name
                                make.Identifier(returnType), // return type
                                Collections.<TypeParameterTree>emptyList(), // type parameters - none
                                params,
                                exc, // throws
                                "{ //TODO implement this method\nthrow new UnsupportedOperationException(\"Not implemented yet.\") }", // body text
                                null // default value - not applicable here, used by annotations
                                );

                        ClassTree modifiedClass =  make.addClassMember(javaClass, method);

                        workingCopy.rewrite(javaClass, modifiedClass);
                    } else {
                        Logger.getLogger(MethodGenerator.class.getName()).log(Level.INFO, 
                                "Failed to bind WSDL operation to java method: "+operationName); //NOI18N             
                    }
                }
            }
            
            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();
        DataObject dobj = DataObject.find(implClassFo);
        if (dobj!=null) {
            SaveCookie cookie = dobj.getCookie(SaveCookie.class);
            if (cookie!=null) cookie.save();
        }
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static void deleteMethod(final FileObject implClass, final String operationName) throws IOException{
        JavaSource targetSource = JavaSource.forFileObject(implClass);
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                //workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass!=null) {
                    ExecutableElement method = new MethodVisitor(workingCopy).
                        getMethod( operationName);
                    TreeMaker make  = workingCopy.getTreeMaker();
                    if(method != null){
                        MethodTree methodTree = workingCopy.getTrees().getTree(
                                method);
                        ClassTree modifiedJavaClass = make.removeClassMember(
                                javaClass, methodTree);
                        workingCopy.rewrite(javaClass, modifiedJavaClass);
                        boolean removeImplementsClause = false;
                        //find out if there are no more exposed operations, if so remove the implements clause
                        if(! new MethodVisitor(workingCopy).hasPublicMethod()){
                            removeImplementsClause = true;
                        }
                    
                        if(removeImplementsClause){
                            //TODO: need to remove implements clause on the SEI
                            //for now all implements are being remove
                            List<? extends Tree> implementeds = javaClass.
                                getImplementsClause();
                            for(Tree implemented : implementeds) {
                                modifiedJavaClass = make.
                                    removeClassImplementsClause(modifiedJavaClass, 
                                            implemented);
                            }
                            workingCopy.rewrite(javaClass, modifiedJavaClass);
                        }
                    }
                }
            }
            //}
            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();
        DataObject dobj = DataObject.find(implClass);
        if (dobj!=null) {
            SaveCookie cookie = dobj.getCookie(SaveCookie.class);
            if (cookie!=null) cookie.save();
        }
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static void removeMethod(final FileObject implClass, 
            final String operationName) throws IOException 
    {
        JavaSource targetSource = JavaSource.forFileObject(implClass);
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement classEl = SourceUtils.getPublicTopLevelElement(
                        workingCopy);
                if (classEl!=null) {
                    ExecutableElement method = new MethodVisitor(workingCopy).
                        getMethod( operationName);
                    if(method != null){
                        ClassTree javaClass = workingCopy.getTrees().
                            getTree(classEl);
                        //first find out if @WebService annotation is present in the class
                        boolean foundWebServiceAnnotation = JaxWsUtils.hasAnnotation(
                                classEl, "javax.jws.WebService");   //NOI18N
                        //do the class methods have at least one WebMethod annotation
                        boolean classHasWebMethods = new MethodVisitor(workingCopy).
                            hasWebMethod();
                        TreeMaker make = workingCopy.getTreeMaker();
                        MethodTree methodTree = workingCopy.getTrees().getTree(method);
                        if(methodTree != null){
                            if(foundWebServiceAnnotation){
                                if(!classHasWebMethods){
                                    //if class has no WebMethod annotations, add WebMethod annotation to all
                                    //methods except the removed operation
                                    List<ExecutableElement> publicMethods = 
                                        new MethodVisitor(workingCopy).
                                            getPublicMethods();
                                    for(ExecutableElement m : publicMethods){
                                        if(m != method){
                                            List<ExpressionTree> emptyList = 
                                                Collections.emptyList();
                                            
                                            AnnotationTree webMethodAnnotation = 
                                                make.Annotation(make.QualIdent(
                                                        "javax.jws.WebMethod"),
                                                            emptyList);
                                            MethodTree mTree = workingCopy.
                                                getTrees().getTree(m);
                                            ModifiersTree modTree = 
                                                    mTree.getModifiers();
                                            ModifiersTree newModifiersTree = 
                                                make.addModifiersAnnotation(
                                                        modTree, webMethodAnnotation);
                                            workingCopy.rewrite(modTree, 
                                                    newModifiersTree);
                                        }
                                    }
                                }else{ //there are WebMethod annotations in the class, remove WebMethod annotation from the method
                                    ModifiersTree modifiersTree = methodTree.
                                        getModifiers();
                                    ModifiersTree newModTree = make.
                                        Modifiers(modifiersTree.getFlags());
                                    workingCopy.rewrite(modifiersTree, newModTree);
                                }
                            } else{ //no @WebService annotation, there must have been a @WebMethod annotation, remove it
                                AnnotationMirror webMethodAnMirr =  
                                    getWebMethodAnnotation(workingCopy, method);
                                if(webMethodAnMirr != null){
                                    ModifiersTree modifiersTree = methodTree.
                                        getModifiers();
                                    AnnotationTree annotTree = 
                                        (AnnotationTree)workingCopy.getTrees().
                                            getTree(classEl,webMethodAnMirr);
                                    ModifiersTree newModTree = make.
                                        removeModifiersAnnotation(modifiersTree, 
                                                annotTree);
                                    workingCopy.rewrite(modifiersTree, newModTree);
                                }
                            }
                            
                            boolean removeImplementsClause = false;
                            //find out if there are no more exposed operations, if so remove the implements clause
                            if(foundWebServiceAnnotation){
                                //if there is a WebService annotation find out if there are no more public methods
                                if(! new MethodVisitor(workingCopy).hasPublicMethod()){
                                    removeImplementsClause = true;
                                }
                            } else{
                                if(! new MethodVisitor(workingCopy).hasWebMethod()){
                                    removeImplementsClause = true;
                                }
                            }
                            if(removeImplementsClause){
                                //TODO: need to remove implements clause on the SEI
                                //for now all implements are being removed
                                List<? extends Tree> implementeds = javaClass.
                                    getImplementsClause();
                                ClassTree modifiedJavaClass = javaClass;
                                for(Tree implemented : implementeds) {
                                    modifiedJavaClass = make.
                                        removeClassImplementsClause(
                                                modifiedJavaClass, implemented);
                                }
                                workingCopy.rewrite(javaClass, modifiedJavaClass);
                            }
                        }
                    }
                }
            }
            
            @Override
            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();
        DataObject dobj = DataObject.find(implClass);
        if (dobj!=null) {
            SaveCookie cookie = dobj.getCookie(SaveCookie.class);
            if (cookie!=null) cookie.save();
        }
    }
    
    
    private static AnnotationMirror getWebMethodAnnotation(WorkingCopy workingCopy, 
            ExecutableElement method)
    {
        return JaxWsUtils.getAnnotation(method, "javax.jws.WebMethod"); //NOI18N
    }
    
    private WsdlOperation getWsdlOperation(String operationName) {
        // TODO: exclude non DOCUMENT/LITERAL ports
        List<WsdlService> services = wsdlModel.getServices();
        for (WsdlService service:services) {
            List<WsdlPort> ports = service.getPorts();
            for (WsdlPort port:ports) {
                List<WsdlOperation> operations = port.getOperations();
                for (WsdlOperation operation:operations) {
                    if (operationName.equals(operation.getName())) {
                        return (WsdlOperation) operation;
                    }
                }
            }
        }
        return null;
    }
    
}
