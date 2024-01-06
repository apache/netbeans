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
package org.netbeans.modules.websvc.rest.editor;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;


/**
 * @author ads
 *
 */
abstract class AsyncConverter {

    boolean isApplicable(FileObject fileObject){
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return false;
        }
        WebModule webModule = WebModule.getWebModule(project
                .getProjectDirectory());
        if (webModule == null) {
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        if (profile.isAtMost(Profile.JAVA_EE_6_FULL)) {
            return false;
        }
        return true;
    }

    boolean isApplicable(Element element){
        if ( element == null || element.getKind() != ElementKind.METHOD){
            return false;
        }

        Element enclosingElement = element.getEnclosingElement();
        if (!(enclosingElement instanceof TypeElement)){
            return false;
        }
        return true;
    }

    protected abstract Logger getLogger();

    protected boolean isAsync(Element method){
        if ( method instanceof ExecutableElement ){
            ExecutableElement exec = (ExecutableElement)method;
            List<? extends VariableElement> parameters = exec.getParameters();
            for (VariableElement param : parameters) {
                boolean hasAsyncType = false;
                TypeMirror type = param.asType();
                if ( type instanceof DeclaredType ){
                    Element paramElement = ((DeclaredType)type).asElement();
                    if ( paramElement instanceof TypeElement ){
                        hasAsyncType = ((TypeElement)paramElement).getQualifiedName().
                                contentEquals("javax.ws.rs.container.AsyncResponse");   // NOI18N
                    }
                }
                if( hasAsyncType && hasAnnotation(param,
                        "javax.ws.rs.container.Suspended")){ // NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean checkRestMethod( final String fqn , Element method,
            FileObject source) {
        final String methodName = method.getSimpleName().toString();
        /*
         *  TODO : method name doesn't uniquely identify the method.
         *  So implementation should be improved
         */
        Project project = FileOwnerQuery.getOwner(source);
        RestServicesModel model = RestUtils.getRestServicesMetadataModel(project);
        if ( model == null){
            return false;
        }
        try {
            return model.runReadAction(
                    new MetadataModelAction<RestServicesMetadata, Boolean>()
            {

                @Override
                public Boolean run( RestServicesMetadata metadata )
                        throws Exception
                {
                    RestServices services = metadata.getRoot();
                    RestServiceDescription[] descriptions =
                            services.getRestServiceDescription();
                    for (RestServiceDescription description : descriptions) {
                        if ( fqn.equals(description.getClassName())){
                            List<RestMethodDescription> methods =
                                    description.getMethods();
                            for (RestMethodDescription method : methods){
                                if ( methodName.equals(method.getName())){
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            });
        }
        catch(MetadataModelException e ){
            getLogger().log(Level.INFO, null,  e);
        }
        catch(IOException e ){
            getLogger().log(Level.INFO, null,  e);
        }
        return false;
    }

    protected boolean hasAnnotation(Element element, String... annotationFqns){
        List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
        for (AnnotationMirror annotation : annotations) {
            Element annotationElement = annotation.getAnnotationType().asElement();
            if ( annotationElement instanceof TypeElement){
                String fqn = ((TypeElement)annotationElement).getQualifiedName().
                        toString();
                for(String annotationFqn : annotationFqns){
                    if (fqn.equals(annotationFqn)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void convertMethod( final ElementHandle<Element> handle,
            FileObject fileObject ) throws IOException
    {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null) {
            return;
        }
        ModificationResult task = javaSource
                .runModificationTask(new Task<WorkingCopy>() {

                    @Override
                    public void run( WorkingCopy copy ) throws Exception {
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);

                        Element restMethod = handle.resolve(copy);
                        if (restMethod == null) {
                            return;
                        }
                        Element enclosingElement = restMethod
                                .getEnclosingElement();
                        if (!(enclosingElement instanceof TypeElement)) {
                            return;
                        }
                        ClassTree classTree = (ClassTree) copy.getTrees()
                                .getTree(enclosingElement);

                        MethodTree method = (MethodTree) copy.getTrees()
                                .getTree(restMethod);
                        String name = restMethod.getSimpleName().toString();
                        String asyncName = findFreeName(name, enclosingElement,
                                restMethod);
                        String movedName = findFreeName(
                                convertMethodName(name), enclosingElement,
                                restMethod);

                        TreeMaker maker = copy.getTreeMaker();
                        boolean isEjb = isEjb(enclosingElement);
                        ClassTree newTree = classTree;
                        if (!isEjb) {
                            newTree = addExecutionService(maker, copy,
                                    enclosingElement, classTree);
                        }
                        newTree = createAsyncMethod(maker, asyncName,
                                serviceField, method, movedName, copy, newTree,
                                isEjb);
                        newTree = moveRestMethod(maker, movedName, method,
                                copy, newTree);
                        copy.rewrite(classTree, newTree);
                    }

                    private ClassTree addExecutionService( TreeMaker maker,
                            WorkingCopy copy, Element clazz, ClassTree classTree )
                    {
                        List<VariableElement> fields = ElementFilter
                                .fieldsIn(clazz.getEnclosedElements());
                        Set<String> fieldNames = new HashSet<String>();
                        for (VariableElement field : fields) {
                            fieldNames.add(field.getSimpleName().toString());
                            TypeMirror fieldType = field.asType();
                            Element fieldTypeElement = copy.getTypes()
                                    .asElement(fieldType);
                            if (fieldTypeElement instanceof TypeElement) {
                                TypeElement type = (TypeElement) fieldTypeElement;
                                if (ExecutorService.class.getName()
                                        .contentEquals(type.getQualifiedName()))
                                {
                                    serviceField = field.getSimpleName()
                                            .toString();
                                }
                            }
                        }
                        if (serviceField == null) {
                            String name = "executorService"; // NOI18N
                            serviceField = name;
                            int i = 0;
                            while (fieldNames.contains(serviceField)) {
                                serviceField = name + i;
                                i++;
                            }
                        }
                        else {
                            return classTree;
                        }
                        MethodInvocationTree init = maker.MethodInvocation(
                                Collections.<ExpressionTree> emptyList(),
                                maker.QualIdent(Executors.class.getName()
                                        + ".newCachedThreadPool"),
                                Collections.<ExpressionTree> emptyList());
                        VariableTree service = maker.Variable(maker
                                .Modifiers(EnumSet.of(Modifier.PRIVATE)),
                                serviceField, maker
                                        .QualIdent(ExecutorService.class
                                                .getName()), init);
                        return maker.addClassMember(classTree, service);
                    }

                    private String serviceField;

                });
        task.commit();
    }

    private String findFreeName( String name,Element enclosingElement,
            Element havingName)
    {
        for(ExecutableElement method:
            ElementFilter.methodsIn(enclosingElement.getEnclosedElements()))
        {
            if (method.equals(havingName)){
                continue;
            }
            if ( method.getSimpleName().contentEquals(name)){
                return findFreeName(name+1,enclosingElement, havingName);
            }
        }
        return name;
    }

    private String convertMethodName(String name){
        if ( name.length()<=1){
            return "do"+name;       // NOI18N
        }
        else {
            return "do"+Character.toUpperCase(name.charAt(0))+name.substring(1);
        }
    }

    private boolean isEjb(Element element){
        return hasAnnotation(element, "javax.ejb.Stateless",
                "javax.ejb.Singleton");// NOI18N
    }

    private ClassTree createAsyncMethod( TreeMaker maker,
            String asyncName, String service, MethodTree method, String movedName ,
            WorkingCopy copy, ClassTree classTree, boolean isEjb)
    {
        ModifiersTree modifiers = method.getModifiers();
        if ( isEjb ){
            AnnotationTree async = maker.Annotation(maker.QualIdent(
                    "javax.ejb.Asynchronous"),          // NOI18N
                    Collections.<ExpressionTree>emptyList());
            modifiers  = maker.addModifiersAnnotation(modifiers, async);
        }
        List<? extends VariableTree> parameters = method.getParameters();
        String asyncReponseParam = getAsynParam("asyncResponse",parameters);//NOI18N

        ModifiersTree paramModifier = maker.Modifiers(EnumSet.of(Modifier.FINAL));
        AnnotationTree annotation = maker.Annotation(
                maker.QualIdent("javax.ws.rs.container.Suspended"),        // NOI18N
                Collections.<ExpressionTree>emptyList());
        paramModifier = maker.Modifiers(paramModifier,
                Collections.singletonList(annotation));
        VariableTree asyncParam = maker.Variable(paramModifier, asyncReponseParam,
                maker.QualIdent("javax.ws.rs.container.AsyncResponse"), null);//NOI18N
        List<VariableTree> params = new ArrayList<VariableTree>(parameters.size()+1);
        params.add(asyncParam);

        Tree returnType = method.getReturnType();
        boolean noReturn =returnType.toString().equals("void");     // NOI18N

        StringBuilder body = new StringBuilder("{");                // NOI18N
        if ( !isEjb ){
            body.append(service);
            body.append(".submit(new Runnable() { public void run() {");//NOI18N
        }
        if ( !noReturn ){
            body.append(asyncReponseParam);
            body.append(".resume(");                            // NOI18N
        }
        body.append(movedName);
        body.append('(');
        for (VariableTree param : parameters) {
            ModifiersTree modifier = maker.addModifiersModifier(param.getModifiers(),
                    Modifier.FINAL);
            VariableTree newParam = maker.Variable(modifier, param.getName(),
                    param.getType(), param.getInitializer());
            params.add(newParam);
            TreePath pathParam = copy.getTrees().getPath(
                    copy.getCompilationUnit(), param);
            body.append(copy.getTrees().getElement(pathParam).getSimpleName());
            body.append(',');
        }
        if ( !parameters.isEmpty()){
            body.deleteCharAt(body.length()-1);
        }
        if ( noReturn){
            body.append(");");
            body.append(asyncReponseParam);
            body.append(".resume(javax.ws.rs.core.Response.ok().build());");//NOI18N
        }
        else {
            body.append("));");
        }
        if ( !isEjb ){
            body.append("}});");
        }
        body.append('}');

        MethodTree newMethod = maker.Method(modifiers, asyncName,
                maker.Type("void"),             // NOI18N
                Collections.<TypeParameterTree> emptyList(),
                params,
                Collections.<ExpressionTree> emptyList(),
                body.toString(),null);
        return maker.addClassMember(classTree, newMethod);
    }

    private String getAsynParam(String paramName, List<? extends VariableTree> parameters ) {
        for (VariableTree variableTree : parameters) {
            if ( paramName.equals(variableTree.getName())){
                return getAsynParam(paramName+1 ,parameters);
            }
        }
        return paramName;
    }

    private ClassTree moveRestMethod( TreeMaker maker, String movedName,
            MethodTree method, WorkingCopy copy, ClassTree classTree)
    {
        List<? extends VariableTree> parameters = method.getParameters();
        Tree returnType = method.getReturnType();
        BlockTree body = method.getBody();

        ModifiersTree modifiers = maker.Modifiers(EnumSet.of(Modifier.PRIVATE));
        MethodTree newMethod = maker.Method(modifiers, movedName,
                returnType,
                Collections.<TypeParameterTree> emptyList(),
                parameters,
                Collections.<ExpressionTree> emptyList(),body,null);

        ClassTree newClass = maker.addClassMember(classTree, newMethod);
        newClass = maker.removeClassMember(newClass, method);
        return newClass;
    }
}
