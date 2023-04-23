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

package org.netbeans.modules.websvc.core;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizerFactory;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * Helper for adding WS Operation to Web Service.
 * @author Milan Kuchtiak
 */
public class AddWsOperationHelper {
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    
    private final String name;
    private final boolean createAnnotations;
    private MethodModel method;
    
    public AddWsOperationHelper(String name, boolean flag) {
        this.name = name;
        this.createAnnotations = flag;
    }
    
    public AddWsOperationHelper(String name) {
        this(name,true);
    }
    
    protected MethodModel getPrototypeMethod() {
        return MethodModel.create(
                NbBundle.getMessage(AddWsOperationHelper.class,
                        "TXT_DefaultOperationName"), //NOI18N
                String.class.getCanonicalName(), 
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
    }
    
    public String getTitle() {
        return name;
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    protected MethodCustomizer createDialog(FileObject fileObject, 
            MethodModel methodModel) throws IOException 
    {
        return MethodCustomizerFactory.operationMethod(
                getTitle(),
                methodModel,
                ClasspathInfo.create(
                    ClassPath.getClassPath(fileObject, ClassPath.BOOT), // JDK classes
                    ClassPath.getClassPath(fileObject, ClassPath.COMPILE), // classpath from dependent projects and libraries
                    ClassPath.getClassPath(fileObject, ClassPath.SOURCE)), // source classpath
                getExistingMethods(fileObject));
    }
    
    public void addMethod(FileObject fileObject, String className) throws IOException {
        if (className == null) {
            return;
        }
        method = getPrototypeMethod();
        MethodCustomizer methodCustomizer = createDialog(fileObject, method);
        if (methodCustomizer.customizeMethod()) {
            try {
                
                method = methodCustomizer.getMethodModel();
                okButtonPressed(method, fileObject, className);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        else{  //user pressed cancel button
            method = null;
        }
    }
    
    /**
     *  Variant of addMethod(FileObject, String)which returns the final MethodModel.
     */ 
    public MethodModel getMethodModel(FileObject fileObject, String className) 
        throws IOException
    {
        addMethod(fileObject, className);
        return method;
    }
    
    protected void okButtonPressed(MethodModel method, FileObject implClassFo, 
            String className) throws IOException 
    {
        addOperation(method, implClassFo);
    }
    
    protected FileObject getDDFile(FileObject fileObject) {
        return EjbJar.getEjbJar(fileObject).getDeploymentDescriptor();
    }
    
    /*
     * Adds a method definition to the the implementation class
     */
    private void addOperation(final MethodModel methodModel, 
            final FileObject implClassFo) 
    {
        final JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(AddWsOperationHelper.class, 
                        "MSG_AddingNewOperation", methodModel.getName()));      // NOI18N
        handle.start(100);
        final AddOperationTask modificationTask = 
            new AddOperationTask(handle, methodModel );
        
        if (SwingUtilities.isEventDispatchThread()) {
            executeInRequestProcessor(implClassFo, targetSource, handle,
                    modificationTask);
        } else {
            executeModificationTask(implClassFo, targetSource, handle,
                    modificationTask);            
        }

    }

    private void executeInRequestProcessor( final FileObject implClassFo,
            final JavaSource targetSource, final ProgressHandle handle,
            final AddOperationTask modificationTask )
    {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                executeModificationTask(implClassFo, targetSource, handle,
                        modificationTask);                 
            }
        });
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void executeModificationTask( final FileObject implClassFo,
            final JavaSource targetSource, final ProgressHandle handle,
            final AddOperationTask modificationTask )
    {
        try {
            ModificationResult result = targetSource.runModificationTask(modificationTask);
            if ( modificationTask.isIncomplete() && 
                    org.netbeans.api.java.source.SourceUtils.isScanInProgress() )
            {
                final Runnable runnable = new Runnable(){

                    @Override
                    public void run() {
                        executeInRequestProcessor(implClassFo, targetSource, 
                                handle, modificationTask);
                    }
                    
                };
                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run() {
                        ScanDialog.runWhenScanFinished(runnable, getTitle());
                    }
                });
            }
            else {
                result.commit();
                // add method to SEI class
                String seiClass = modificationTask.getSeiClass();
                if (seiClass != null) {
                    ClassPath sourceCP = ClassPath.getClassPath(implClassFo, 
                            ClassPath.SOURCE);
                    FileObject seiFo = sourceCP.findResource(
                            seiClass.replace('.', '/')+".java"); //NOI18N
                    if (seiFo != null) {
                        JavaSource seiSource = JavaSource.forFileObject(seiFo);
                        seiSource.runModificationTask(modificationTask).commit();
                        saveFile(seiFo);
                    }
                }
                saveFile(implClassFo);
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            handle.finish();
        }
    }
    
    private boolean checkMethodAnnotation( TypeElement element, 
            Element annotationElement ) 
    {
        List<ExecutableElement> methods = ElementFilter.methodsIn( 
                element.getEnclosedElements() );
        if ( methods.size() == 0 ){
            return true;
        }
        for (ExecutableElement method : methods) {
            // ignore constructors
            if ( method.getKind() == ElementKind.METHOD ){
                List<? extends AnnotationMirror> annotationMirrors = 
                    method.getAnnotationMirrors();
                for (AnnotationMirror annotationMirror : annotationMirrors) {
                    DeclaredType annotationType = 
                        annotationMirror.getAnnotationType();
                    Element annotation = annotationType.asElement();
                    if ( annotationElement.equals( annotation )){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private String getPackagePrefix (String className) {
        int lastDot = className.indexOf("."); //NOI18N
        if (lastDot > 0) return className.substring(0,lastDot+1);
        else return "";
    }
    
    private void saveFile(FileObject file) throws IOException {
        DataObject dataObject = DataObject.find(file);
        if (dataObject!=null) {
            SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
            if (cookie!=null) cookie.save();
        }        
    }
    
    private String getMethodBody(Tree returnType) {
        String body = null;
        if (Kind.PRIMITIVE_TYPE == returnType.getKind()) {
            TypeKind type = ((PrimitiveTypeTree)returnType).getPrimitiveTypeKind();
            if (TypeKind.VOID == type) {
                body = ""; //NOI18N
            }
            else if (TypeKind.BOOLEAN == type) {
                body = "return false;"; // NOI18N
            }
            else if (TypeKind.INT == type) {
                body = "return 0;"; // NOI18N
            }
            else if (TypeKind.LONG == type) {
                body = "return 0;"; // NOI18N
            }
            else if (TypeKind.FLOAT == type) {
                body = "return 0.0;"; // NOI18N
            }
            else if (TypeKind.DOUBLE == type) {
                body = "return 0.0;"; // NOI18N
            }
            else if (TypeKind.BYTE == type) {
                body = "return 0;"; // NOI18N
            }
            else if (TypeKind.SHORT == type) {
                body = "return 0;"; // NOI18N
            }
            else if (TypeKind.CHAR == type) {
                body = "return ' ';"; // NOI18N
            }
            else {
                body = "return null"; //NOI18N
            }
        } else
            body = "return null"; //NOI18N
        return "{\n\t\t"+NbBundle.getMessage(AddWsOperationHelper.class, "TXT_TodoComment")+"\n"+body+"\n}";
    }
    /*
    protected static MethodsNode getMethodsNode() {
        Node[] nodes = Utilities.actionsGlobalContext().lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]);
        if (nodes.length != 1) {
            return null;
        }
        return nodes[0].getLookup().lookup(MethodsNode.class);
    }
     */
    
    private Collection<MethodModel> getExistingMethods(FileObject implClass) {
        JavaSource javaSource = JavaSource.forFileObject(implClass);
        final ResultHolder<MethodModel> result = new ResultHolder<MethodModel>();
        if (javaSource!=null) {
            CancellableTask<CompilationController> task = 
                new CancellableTask<CompilationController>() 
                {
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                    Collection<MethodModel> wsOperations = new ArrayList<MethodModel>();
                    if (typeElement!=null) {
                        // find methods
                        List<ExecutableElement> allMethods = getMethods(controller, 
                                typeElement);
                        boolean foundWebMethodAnnotation=false;
                        for(ExecutableElement method:allMethods) {
                            // check if return type is a valid type
                            if (method.getReturnType().getKind() == TypeKind.ERROR) {
                                break;
                            }
                            // check if param types are valid types
                            
                            boolean validParamTypes = true;
                            List<? extends VariableElement> params = method.getParameters();
                            for (VariableElement param:params) {
                                if (param.asType().getKind() == TypeKind.ERROR) {
                                    validParamTypes = false;
                                    break;
                                }
                            }
                            if (validParamTypes) {
                                MethodModel methodModel = MethodModelSupport.
                                createMethodModel(controller, method);
                                wsOperations.add(methodModel);
                            }
                        } // for
                    }
                    result.setResult(wsOperations);
                }
                @Override
                public void cancel() {}
            };
            try {
                javaSource.runUserActionTask(task, true);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return result.getResult();
    }
    
    private List<ExecutableElement> getMethods(CompilationController controller, 
            TypeElement classElement) throws IOException 
    {
        List<? extends Element> members = classElement.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(members);
        List<ExecutableElement> publicMethods = new ArrayList<ExecutableElement>();
        for (ExecutableElement method:methods) {
            //Set<Modifier> modifiers = method.getModifiers();
            //if (modifiers.contains(Modifier.PUBLIC)) {
            publicMethods.add(method);
            //}
        }
        return publicMethods;
    }
    
    private class AddOperationTask implements CancellableTask<WorkingCopy> {
        
        private AddOperationTask( ProgressHandle handle , MethodModel methodModel) {
            this.handle = handle;
            this.methodModel = methodModel;
        }

        public String getSeiClass() {
            return seiClass;
        }
        
        public boolean isIncomplete(){
            return isIncomplete;
        }

        @Override
        public void run(WorkingCopy workingCopy) throws IOException{
            workingCopy.toPhase(Phase.RESOLVED);
            MethodTree method = MethodModelSupport.createMethodTree(workingCopy, 
                    methodModel);
            if (method!=null) {
                TreeMaker make = workingCopy.getTreeMaker();
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(
                        workingCopy);
                if (typeElement!=null) {

                    boolean increaseProgress = true;
                    
                    if (createAnnotations) {
                        if (seiClass == null) {
                            seiClass = getEndpointInterface(typeElement, workingCopy);
                        } else {
                            seiClass = null;
                            increaseProgress = false;
                        }                           
                    }
                    boolean needAnnotations = createAnnotations;
                    
                    if (increaseProgress) {
                        handle.progress(20);
                    }
                    
                    ClassTree javaClass = workingCopy.getTrees().
                        getTree(typeElement);
                    TypeElement webMethodAn = workingCopy.getElements().
                        getTypeElement("javax.jws.WebMethod");      //NOI18N
                    TypeElement webParamAn = workingCopy.getElements().
                        getTypeElement("javax.jws.WebParam");       //NOI18N
                    
                    if ( webMethodAn == null || webParamAn == null ){
                        isIncomplete = true;
                        return;
                    }
                                
                    if( needAnnotations ){
                        needAnnotations = checkMethodAnnotation( typeElement , 
                                webMethodAn);
                    }
                    
                   // Public modifier
                    ModifiersTree modifiersTree = make.Modifiers(
                            Collections.<Modifier>singleton(Modifier.PUBLIC),
                            Collections.<AnnotationTree>emptyList()
                            );                        
                    
                    // add @WebMethod annotation
                    if(needAnnotations && seiClass == null) {

                        String methodName = method.getName().toString();
                        // find value for @WebMethod:oparationName
                        String operationName = findNewOperationName(
                                typeElement, workingCopy, methodName);
                                                 
                        AssignmentTree opName = make.Assignment(
                                make.Identifier("operationName"), 
                                make.Literal(operationName)); //NOI18N

                        AnnotationTree webMethodAnnotation = make.Annotation(
                                make.QualIdent(webMethodAn),
                                Collections.<ExpressionTree>singletonList(opName)
                                );
                        modifiersTree = make.addModifiersAnnotation(modifiersTree, 
                                webMethodAnnotation);

                        // add @Oneway annotation
                        
                        boolean isOneWay = false;
                        if (Kind.PRIMITIVE_TYPE == method.getReturnType().getKind()) {
                            PrimitiveTypeTree primitiveType = 
                                (PrimitiveTypeTree)method.getReturnType();
                            if (TypeKind.VOID == 
                                primitiveType.getPrimitiveTypeKind()) 
                            {
                                if (method.getThrows().size() == 0) {
                                    isOneWay = true;
                                    TypeElement oneWayAn = workingCopy.
                                        getElements().
                                        getTypeElement("javax.jws.Oneway"); //NOI18N
                                    if ( oneWayAn == null ){
                                        isIncomplete = true;
                                        return;
                                    }
                                    AnnotationTree oneWayAnnotation = make.Annotation(
                                            make.QualIdent(oneWayAn),
                                            Collections.<ExpressionTree>emptyList()
                                            );

                                    modifiersTree = make.addModifiersAnnotation(
                                            modifiersTree, oneWayAnnotation);
                                }
                            }
                        }
                        if (!methodName.equals(operationName)) {
                            // generate Request/Response wrapper annotations to avoid class conflicts
                            // this enables to generate operations with identical method names
                            String packagePrefix = getPackagePrefix(
                                    typeElement.getQualifiedName().toString());

                            TypeElement reqWrapperAn = workingCopy.getElements().
                                getTypeElement("javax.xml.ws.RequestWrapper"); //NOI18N
                            if ( reqWrapperAn == null ){
                                isIncomplete = true;
                                return;
                            }
                            AssignmentTree className = make.Assignment(
                                    make.Identifier("className"), 
                                    make.Literal(packagePrefix+operationName)); //NOI18N
                            AnnotationTree reqWrapperAnnotation = make.Annotation(
                                    make.QualIdent(reqWrapperAn),
                                    Collections.<ExpressionTree>singletonList(className)
                                    );
                            modifiersTree = make.addModifiersAnnotation(modifiersTree, 
                                    reqWrapperAnnotation);
                            if (!isOneWay) {
                                TypeElement resWrapperAn = workingCopy.getElements().
                                    getTypeElement("javax.xml.ws.ResponseWrapper"); //NOI18N
                                if ( resWrapperAn == null ){
                                    isIncomplete = true;
                                    return;
                                }
                                className = make.Assignment(make.Identifier("className"), 
                                        make.Literal(packagePrefix+operationName+"Response")); //NOI18N
                                AnnotationTree resWrapperAnnotation = make.Annotation(
                                        make.QualIdent(resWrapperAn),
                                        Collections.<ExpressionTree>singletonList(className)
                                        );
                                modifiersTree = make.addModifiersAnnotation(modifiersTree, 
                                        resWrapperAnnotation);
                            }
                        }
                    }
                    
                    if (increaseProgress) {
                        handle.progress(40);
                    }
                    
                    // add @WebParam annotations
                    List<? extends VariableTree> parameters = method.getParameters();
                    List<VariableTree> newParameters = new ArrayList<VariableTree>();
                    
                    if(needAnnotations && seiClass == null) {
                        for (VariableTree param:parameters) {
                            AnnotationTree paramAnnotation = make.Annotation(
                                    make.QualIdent(webParamAn),
                                    Collections.<ExpressionTree>singletonList(
                                    make.Assignment(make.Identifier("name"),
                                            make.Literal(param.getName().toString()))) //NOI18N
                                    );
                            GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                            newParameters.add(genUtils.addAnnotation(param, paramAnnotation));
                        }
                    } else {
                        newParameters.addAll(parameters);
                    }
                                           
                    if (increaseProgress) {
                        handle.progress(70);
                    }
                    // create new (annotated) method
                    MethodTree  annotatedMethod = typeElement.getKind() == ElementKind.CLASS ?
                        make.Method(
                            modifiersTree,
                            method.getName(),
                            method.getReturnType(),
                            method.getTypeParameters(),
                            newParameters,
                            method.getThrows(),
                            getMethodBody(method.getReturnType()), //NOI18N
                            (ExpressionTree)method.getDefaultValue()) :
                        make.Method(
                            modifiersTree,
                            method.getName(),
                            method.getReturnType(),
                            method.getTypeParameters(),
                            newParameters,
                            method.getThrows(),
                            (BlockTree)null,
                            (ExpressionTree)method.getDefaultValue());
                    Comment comment = Comment.create(Style.JAVADOC, -2, -2, -2,
                            NbBundle.getMessage(AddWsOperationHelper.class, 
                                    "TXT_WSOperation"));    // NOI18N
                    make.addComment(annotatedMethod, comment, true);
                    
                    if (increaseProgress) {
                        handle.progress(90);
                    }
                    ClassTree modifiedClass = make.addClassMember(javaClass,
                            annotatedMethod);
                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }
        }
        
        @Override
        public void cancel() {
            
        }
        
        private String getEndpointInterface(TypeElement classEl, 
                CompilationController controller)  
        {
            TypeElement wsElement = controller.getElements().
                getTypeElement("javax.jws.WebService");                 //NOI18N
            if ( wsElement == null ){
                isIncomplete = true;
                return null;
            }
            List<? extends AnnotationMirror> annotations = classEl.getAnnotationMirrors();
            for (AnnotationMirror anMirror : annotations) {
                if (controller.getTypes().isSameType(wsElement.asType(),
                        anMirror.getAnnotationType()))
                {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = 
                        anMirror.getElementValues();
                    for (Map.Entry<? extends ExecutableElement, 
                            ? extends AnnotationValue> entry : expressions.entrySet())
                    {
                        if (entry.getKey().getSimpleName()
                                .contentEquals("endpointInterface")) // NOI18N
                        {
                            String value = (String) expressions.get(entry.getKey())
                                    .getValue();
                            if (value != null) {
                                TypeElement seiEl = controller.getElements()
                                        .getTypeElement(value);
                                if (seiEl != null) {
                                    return seiEl.getQualifiedName().toString();
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
        
        private String findNewOperationName(TypeElement classEl, 
                CompilationController controller, String suggestedMethodName) 
                throws IOException 
        {
            
            TypeElement methodElement = controller.getElements().
                getTypeElement("javax.jws.WebMethod"); //NOI18N
            if ( methodElement == null ){
                isIncomplete = true;
            }
            Set<String> operationNames = new HashSet<String>();
            List<ExecutableElement> methods = getMethods(controller, classEl);
            for (ExecutableElement m : methods) {
                String opName = null;
                List<? extends AnnotationMirror> annotations = m
                        .getAnnotationMirrors();
                for (AnnotationMirror anMirror : annotations) {
                    if ( methodElement!= null && 
                            controller.getTypes().isSameType(methodElement.asType(),
                                    anMirror.getAnnotationType()))
                    {
                        Map<? extends ExecutableElement, 
                                ? extends AnnotationValue> expressions = 
                                    anMirror.getElementValues();
                        for (Map.Entry<? extends ExecutableElement, 
                                ? extends AnnotationValue> entry : expressions.entrySet())
                        {
                            if (entry.getKey().getSimpleName()
                                    .contentEquals("operationName")) // NOI18N
                            {
                                opName = (String) expressions.get(entry.getKey()).getValue();
                                break;
                            }
                        }
                    } // end if
                    if (opName != null)
                        break;
                } // enfd for
                if (opName == null) {
                    opName = m.getSimpleName().toString();
                }
                operationNames.add(opName);
            }
            return findNewOperationName(operationNames, suggestedMethodName);
        }
        
        private String findNewOperationName(Set<String> operationNames, 
                String suggestedMethodName) 
        {       
            int i=0;
            String newName = suggestedMethodName; //NOI18N
            while(operationNames.contains(newName)) {
                newName = suggestedMethodName+"_"+String.valueOf(++i); //NOI18N
            }
            return newName;
        }
     
        private ProgressHandle handle;
        private MethodModel methodModel;
        private String seiClass;
        private boolean isIncomplete;
    }
    
    /** Holder class for result
     */
    private static class ResultHolder<E> {
        private Collection<E> result;
        
        public Collection<E> getResult() {
            return result;
        }
        
        public void setResult(Collection<E> result) {
            this.result=result;
        }
    }
}
