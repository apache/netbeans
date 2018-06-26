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

package org.netbeans.modules.maven.jaxws.actions;

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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
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
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizer;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodCustomizerFactory;
//import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
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
                NbBundle.getMessage(AddWsOperationHelper.class,"TXT_DefaultOperationName"), //NOI18N
                "java.lang.String", //NOI18N
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
    }
    
    public String getTitle() {
        return name;
    }
    
    protected MethodCustomizer createDialog(FileObject fileObject, MethodModel methodModel) throws IOException {
        
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
    public MethodModel getMethodModel(FileObject fileObject, String className) throws IOException{
        addMethod(fileObject, className);
        return method;
    }
    
    protected void okButtonPressed(MethodModel method, FileObject implClassFo, String className) throws IOException {
        addOperation(method, implClassFo);
    }
    
//    protected FileObject getDDFile(FileObject fileObject) {
//        return EjbJar.getEjbJar(fileObject).getDeploymentDescriptor();
//    }
    
    /*
     * Adds a method definition to the the implementation class
     */
    private void addOperation(final MethodModel methodModel, final FileObject implClassFo) {
        final JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(AddWsOperationHelper.class, "MSG_AddingNewOperation", methodModel.getName()));
        handle.start(100);
        final String[] seiClass = new String[1];
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                MethodTree method = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                if (method!=null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                    if (typeElement!=null) {

                        boolean increaseProgress = true;
                        
                        if (createAnnotations) {
                            if (seiClass[0] == null) {
                                seiClass[0] = getEndpointInterface(typeElement, workingCopy);
                            } else {
                                seiClass[0] = null;
                                increaseProgress = false;
                            }                           
                        }
                        
                        if (increaseProgress) handle.progress(20);
                        
                        ClassTree javaClass = workingCopy.getTrees().getTree(typeElement);
                        TypeElement webMethodAn = workingCopy.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                        TypeElement webParamAn = workingCopy.getElements().getTypeElement("javax.jws.WebParam"); //NOI18N
                                               
                       // Public modifier
                        ModifiersTree modifiersTree = make.Modifiers(
                                Collections.<Modifier>singleton(Modifier.PUBLIC),
                                Collections.<AnnotationTree>emptyList()
                                );                        
                        
                        // add @WebMethod annotation
                        if(createAnnotations && seiClass[0] == null) {

                            String methodName = method.getName().toString();
                            // find value for @WebMethod:oparationName
                            String operationName = findNewOperationName(typeElement, workingCopy, methodName);
                                                     
                            AssignmentTree opName = make.Assignment(make.Identifier("operationName"), make.Literal(operationName)); //NOI18N

                            AnnotationTree webMethodAnnotation = make.Annotation(
                                    make.QualIdent(webMethodAn),
                                    Collections.<ExpressionTree>singletonList(opName)
                                    );
                            modifiersTree = make.addModifiersAnnotation(modifiersTree, webMethodAnnotation);

                            // add @Oneway annotation
                            
                            boolean isOneWay = false;
                            if (Kind.PRIMITIVE_TYPE == method.getReturnType().getKind()) {
                                PrimitiveTypeTree primitiveType = (PrimitiveTypeTree)method.getReturnType();
                                if (TypeKind.VOID == primitiveType.getPrimitiveTypeKind()) {
                                    if (method.getThrows().size() == 0) {
                                        isOneWay = true;
                                        TypeElement oneWayAn = workingCopy.getElements().getTypeElement("javax.jws.Oneway"); //NOI18N
                                        AnnotationTree oneWayAnnotation = make.Annotation(
                                                make.QualIdent(oneWayAn),
                                                Collections.<ExpressionTree>emptyList()
                                                );

                                            modifiersTree = make.addModifiersAnnotation(modifiersTree, oneWayAnnotation);
                                    }
                                }
                            }
                            if (!methodName.equals(operationName)) {
                                // generate Request/Response wrapper annotations to avoid class conflicts
                                // this enables to generate operations with identical method names
                                String packagePrefix = getPackagePrefix(typeElement.getQualifiedName().toString());

                                TypeElement reqWrapperAn = workingCopy.getElements().getTypeElement("javax.xml.ws.RequestWrapper"); //NOI18N
                                AssignmentTree className = make.Assignment(make.Identifier("className"), make.Literal(packagePrefix+operationName)); //NOI18N
                                AnnotationTree reqWrapperAnnotation = make.Annotation(
                                        make.QualIdent(reqWrapperAn),
                                        Collections.<ExpressionTree>singletonList(className)
                                        );
                                modifiersTree = make.addModifiersAnnotation(modifiersTree, reqWrapperAnnotation);
                                if (!isOneWay) {
                                    TypeElement resWrapperAn = workingCopy.getElements().getTypeElement("javax.xml.ws.ResponseWrapper"); //NOI18N
                                    className = make.Assignment(make.Identifier("className"), make.Literal(packagePrefix+operationName+"Response")); //NOI18N
                                    AnnotationTree resWrapperAnnotation = make.Annotation(
                                            make.QualIdent(resWrapperAn),
                                            Collections.<ExpressionTree>singletonList(className)
                                            );
                                    modifiersTree = make.addModifiersAnnotation(modifiersTree, resWrapperAnnotation);
                                }
                            }
                        }
                        
                        if (increaseProgress) handle.progress(40);
                        
                        // add @WebParam annotations
                        List<? extends VariableTree> parameters = method.getParameters();
                        List<VariableTree> newParameters = new ArrayList<VariableTree>();
                        
                        if(createAnnotations && seiClass[0] == null) {
                            for (VariableTree param:parameters) {
                                AnnotationTree paramAnnotation = make.Annotation(
                                        make.QualIdent(webParamAn),
                                        Collections.<ExpressionTree>singletonList(
                                        make.Assignment(make.Identifier("name"), make.Literal(param.getName().toString()))) //NOI18N
                                        );
                                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                                newParameters.add(genUtils.addAnnotation(param, paramAnnotation));
                            }
                        } else {
                            newParameters.addAll(parameters);
                        }
                                               
                        if (increaseProgress) handle.progress(70);
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
                                NbBundle.getMessage(AddWsOperationHelper.class, "TXT_WSOperation"));
                        make.addComment(annotatedMethod, comment, true);
                        
                        if (increaseProgress) handle.progress(90);
                        ClassTree modifiedClass = make.addClassMember(javaClass,annotatedMethod);
                        workingCopy.rewrite(javaClass, modifiedClass);
                    }
                }
            }
            @Override
            public void cancel() {
            }
        };
        final Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                doAddOperation(implClassFo, targetSource, handle, seiClass,
                        modificationTask);                
            }
        };
        final String title = NbBundle.getMessage(AddWsOperationHelper.class, 
                "LBL_AddOperation");        // NOI18N
        if (SwingUtilities.isEventDispatchThread()) {
            ScanDialog.runWhenScanFinished( runnable, title );
        } else {
            SwingUtilities.invokeLater( new Runnable() {
                
                @Override
                public void run() {
                    ScanDialog.runWhenScanFinished( runnable, title );                    
                }
            });       
        }

    }

    private void doAddOperation( final FileObject implClassFo,
            final JavaSource targetSource, final ProgressHandle handle,
            final String[] seiClass,
            final CancellableTask<WorkingCopy> modificationTask )
    {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    targetSource.runModificationTask(modificationTask).commit();
                    // add method to SEI class
                    if (seiClass[0] != null) {
                        ClassPath sourceCP = ClassPath.getClassPath(implClassFo, ClassPath.SOURCE);
                        FileObject seiFo = sourceCP.findResource(seiClass[0].replace('.', '/')+".java"); //NOI18N
                        if (seiFo != null) {
                            JavaSource seiSource = JavaSource.forFileObject(seiFo);
                            seiSource.runModificationTask(modificationTask).commit();
                            saveFile(seiFo);
                        }
                    }
                    saveFile(implClassFo);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                } finally {
                    handle.finish();
                }                
            }
        });
    }

    private String getEndpointInterface(TypeElement classEl, CompilationController controller) {
        TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
        if (wsElement != null) {
            List<? extends AnnotationMirror> annotations = classEl.getAnnotationMirrors();
            for (AnnotationMirror anMirror : annotations) {
                if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: expressions.entrySet()) {
                        if (entry.getKey().getSimpleName().contentEquals("endpointInterface")) { //NOI18N
                            String value = (String) expressions.get(entry.getKey()).getValue();
                            if (value != null) {
                                TypeElement seiEl = controller.getElements().getTypeElement(value);
                                if (seiEl != null) {
                                    return seiEl.getQualifiedName().toString();
                                }
                            }
                        }
                    }
                } // end if
            }
        }
        return null;
    }
    
    private String findNewOperationName(TypeElement classEl, CompilationController controller, String suggestedMethodName) 
        throws IOException {
        
        TypeElement methodElement = controller.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
        Set<String> operationNames = new HashSet<String>();
        if (methodElement != null) {
            List<ExecutableElement> methods = getMethods(controller,classEl);
            for (ExecutableElement m:methods) {
                String opName = null;
                List<? extends AnnotationMirror> annotations = m.getAnnotationMirrors();
                for (AnnotationMirror anMirror : annotations) {
                    if (controller.getTypes().isSameType(methodElement.asType(), anMirror.getAnnotationType())) {
                        Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: expressions.entrySet()) {
                            if (entry.getKey().getSimpleName().contentEquals("operationName")) { //NOI18N
                                opName = (String) expressions.get(entry.getKey()).getValue();
                                break;
                            }
                        }
                    } // end if
                    if (opName != null) break;
                } //enfd for
                if (opName == null) opName = m.getSimpleName().toString();
                operationNames.add(opName);
            }
        }
        return findNewOperationName(operationNames, suggestedMethodName);
    }
    
    
    private String findNewOperationName(Set<String> operationNames, String suggestedMethodName) {       
        int i=0;
        String newName = suggestedMethodName; //NOI18N
        while(operationNames.contains(newName)) {
            newName = suggestedMethodName+"_"+String.valueOf(++i); //NOI18N
        }
        return newName;
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
            if (TypeKind.VOID == type) body = ""; //NOI18N
            else if (TypeKind.BOOLEAN == type) body = "return false;"; // NOI18N
            else if (TypeKind.INT == type) body = "return 0;"; // NOI18N
            else if (TypeKind.LONG == type) body = "return 0;"; // NOI18N
            else if (TypeKind.FLOAT == type) body = "return 0.0;"; // NOI18N
            else if (TypeKind.DOUBLE == type) body = "return 0.0;"; // NOI18N
            else if (TypeKind.BYTE == type) body = "return 0;"; // NOI18N
            else if (TypeKind.SHORT == type) body = "return 0;"; // NOI18N
            else if (TypeKind.CHAR == type) body = "return ' ';"; // NOI18N
            else body = "return null"; //NOI18N
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
        final JavaSource javaSource = JavaSource.forFileObject(implClass);
        final ResultHolder<MethodModel> result = new ResultHolder<MethodModel>();
        if (javaSource!=null) {
            final CancellableTask<CompilationController> task = 
                new CancellableTask<CompilationController>() 
                {
                
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = SourceUtils.
                        getPublicTopLevelElement(controller);
                    Collection<MethodModel> wsOperations = new ArrayList<MethodModel>();
                    if (typeElement!=null) {
                        // find methods
                        List<ExecutableElement> allMethods = getMethods(controller, 
                                typeElement);
                        boolean foundWebMethodAnnotation=false;
                        for(ExecutableElement method:allMethods) {
                            // check if return type is a valid type
                            if (method.getReturnType().getKind() == TypeKind.ERROR) break;
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
            final Runnable runnable = new Runnable() {
                
                @Override
                public void run() {
                    try {
                        javaSource.runUserActionTask(task, true);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            };
            final String title = NbBundle.getMessage(AddWsOperationHelper.class,
                    "LBL_FindMethods") ;                // NOI18N
            if ( SwingUtilities.isEventDispatchThread() ){
                ScanDialog.runWhenScanFinished(runnable,title  );
            }
            else {
                try {
                    SwingUtilities.invokeAndWait( new Runnable() {
                    
                        @Override
                        public void run() {
                            ScanDialog.runWhenScanFinished(runnable,title  );                        
                        }
                    });
                }
                catch (InvocationTargetException e ){
                    ErrorManager.getDefault().notify(e);
                }
                catch( InterruptedException e ){
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return result.getResult();
    }
    
    private List<ExecutableElement> getMethods(CompilationController controller, TypeElement classElement) throws IOException {
        List<? extends Element> members = classElement.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(members);
        List<ExecutableElement> publicMethods = new ArrayList<ExecutableElement>();
        for (ExecutableElement m:methods) {
            //Set<Modifier> modifiers = method.getModifiers();
            //if (modifiers.contains(Modifier.PUBLIC)) {
            publicMethods.add(m);
            //}
        }
        return publicMethods;
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
