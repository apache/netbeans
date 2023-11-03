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
package org.netbeans.modules.websvc.rest.codegen.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.wizard.AbstractPanel;
import org.openide.util.NbBundle;

/**
 * @author ads
 *
 */
public class SourceModeler extends ResourceModel {
    private static final Logger LOG = Logger.getLogger( SourceModeler.class.getCanonicalName());

    public SourceModeler(Project p) {
        project = p;
    }

    public State validate() {
        return State.VALID;
    }

    public void build() throws IOException {
        State state = validate();
        if(state != State.VALID) {
            throw new IOException(
                    NbBundle.getMessage(AbstractPanel.class,
                    "MSG_ProjectsWithoutREST")+", "+state.value());
        }
        List<JavaSource> sources = JavaSourceHelper.getJavaSources(project);
        for (JavaSource src : sources) {
            if (JavaSourceHelper.isEntity(src)) {
                continue;
            }
            String className = JavaSourceHelper.getClassNameQuietly(src);
            if (className == null) {
                continue;
            }
            if (RestUtils.isStaticResource(src)) {
                Resource r = createResource(src);
                if ( r!= null ){
                    addResource(r);
                }
            }
        }
    }

    private Resource createResource(JavaSource rSrc) throws IOException {
        String name = null;
        String path = null;
        String template = RestUtils.findUri(rSrc);
        resourceFqns = new HashSet<String>();
        if (template != null) {
            path = template;
            name = path;
            if (name.startsWith("/")) {     // NOI18N
                name = name.substring(1);
            }
            if (name.endsWith("/")) {       // NOI18N
                name = name.substring(0, name.length() - 1);
            }
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            Resource r = new Resource(ClientStubModel.normalizeName(name), path);
            buildResource(r, rSrc);
            return r;
        } 
        return null;
    }

    private void buildResource(final Resource resource, JavaSource src) throws IOException {
        resource.setSource(src);
        try {
            src.runUserActionTask(new AbstractTask<CompilationController>() {

                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    
                    TypeElement classElement = JavaSourceHelper.
                        getTopLevelClassElement(controller);
                    
                    Collection<Method> methods = doBuildResource(resource, 
                            classElement, controller, getBoxedPrimitives(controller));
                    for (Method method : methods) {
                        resource.addMethod( method );
                    }
                }

            }, true);
        } catch (IOException ex) {
            LOG.log( Level.WARNING , null , ex);
        }
    }
    
    private Collection<TypeMirror> getBoxedPrimitives( 
            CompilationController controller)
    {
        TypeKind[] values = TypeKind.values();
        Collection<TypeMirror> result = new ArrayList<TypeMirror>( values.length);
        for (TypeKind typeKind : values) {
            if ( typeKind.isPrimitive() ){
                PrimitiveType primitiveType = 
                    controller.getTypes().getPrimitiveType(typeKind);
                TypeElement boxedClass = controller.getTypes().
                    boxedClass(primitiveType);
                result.add( boxedClass.asType() );
            }
        }
        return result;
    }

    private Collection<Method> doBuildResource( final Resource resource,
            TypeElement clazz, CompilationController controller , 
            Collection<TypeMirror> boxedPrimitives )
    {
        String fqn = clazz.getQualifiedName().toString();
        if ( resourceFqns.contains( fqn)){
            return Collections.emptyList();
        }
        else {
            resourceFqns.add( fqn);
        }
        Collection<Method> result = new LinkedList<Method>();

        List<ExecutableElement> methods = ElementFilter.methodsIn(controller
                .getElements().getAllMembers(clazz));

        for (ExecutableElement method : methods) {
            List<? extends AnnotationMirror> annotationMirrors = controller
                    .getElements().getAllAnnotationMirrors(method);
            Map<String, AnnotationMirror> restAnnotations = new HashMap<String, 
                AnnotationMirror>();
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                DeclaredType annotationType = annotationMirror
                        .getAnnotationType();
                Element annotationElement = annotationType.asElement();
                if (annotationElement instanceof TypeElement) {
                    TypeElement annotation = (TypeElement) annotationElement;
                    String fqnAnnotation = annotation.getQualifiedName()
                            .toString();
                    if (isRestAnnotation(fqnAnnotation)) {
                        restAnnotations.put(fqnAnnotation, annotationMirror);
                    }
                }
            }
            Collection<Method> collection = createRestMethods(resource,
                    restAnnotations, clazz, method, controller, boxedPrimitives);
            result.addAll(collection);
        }
        return result;
    }

    private Collection<Method> createRestMethods( Resource resource,
        Map<String, AnnotationMirror> restAnnotations , TypeElement clazz, 
        ExecutableElement method , CompilationController controller , 
        Collection<TypeMirror> boxedPrimitives)
    {
        AnnotationMirror pathAnnotation = restAnnotations.get(RestConstants.PATH_JAKARTA);
        if (pathAnnotation == null) {
            pathAnnotation = restAnnotations.get(RestConstants.PATH);
        }
        AnnotationMirror produceAnnotion = restAnnotations.get(RestConstants.PRODUCE_MIME_JAKARTA);
        if (produceAnnotion == null) {
            produceAnnotion = restAnnotations.get(RestConstants.PRODUCE_MIME);
        }
        AnnotationMirror consumeAnnotion = restAnnotations.get(RestConstants.CONSUME_MIME_JAKARTA);
        if (produceAnnotion == null) {
            consumeAnnotion = restAnnotations.get(RestConstants.CONSUME_MIME);
        }
        AnnotationMirror getAnnotion = restAnnotations.get(RestConstants.GET_JAKARTA);
        if (produceAnnotion == null) {
            getAnnotion = restAnnotations.get(RestConstants.GET);
        }
        AnnotationMirror postAnnotion = restAnnotations.get(RestConstants.POST_JAKARTA);
        if (produceAnnotion == null) {
            postAnnotion = restAnnotations.get(RestConstants.POST);
        }
        AnnotationMirror putAnnotion = restAnnotations.get(RestConstants.PUT_JAKARTA);
        if (produceAnnotion == null) {
            putAnnotion = restAnnotations.get(RestConstants.PUT);
        }
        AnnotationMirror deleteAnnotion = restAnnotations.get(RestConstants.DELETE_JAKARTA);
        if (produceAnnotion == null) {
            deleteAnnotion = restAnnotations.get(RestConstants.DELETE);
        }
        
        AnnotationMirror httpAnnotation = chooseNotNull( getAnnotion, postAnnotion, 
                putAnnotion, deleteAnnotion);
        
        TypeMirror methodMirror = controller.getTypes().asMemberOf(
                (DeclaredType)clazz.asType(), method);
        TypeMirror returnTypeMirror = ((ExecutableType)methodMirror).
            getReturnType();
        if ( pathAnnotation != null ){
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = 
                    pathAnnotation.getElementValues();
            String path = null;
            Iterator<? extends AnnotationValue> iterator = values.values().iterator();
            if ( iterator.hasNext() ){
                Object value = iterator.next().getValue();
                if ( value != null ){
                    path = value.toString();
                }
            }
                
            if ( path == null ){
                return Collections.emptyList();
            }
                
            if ( httpAnnotation!=null )
            {
                Method restMethod = createMethod( method.getSimpleName().toString(),
                        clazz , controller  );
                StringBuilder resourcePath = new StringBuilder();
                if ( resource.getName() == null ){
                    resourcePath.append( resource.getPath());
                    if ( !resource.getPath().endsWith("/")){
                        resourcePath.append('/');
                    }
                }
                resourcePath.append(path);

                configureMethod( restMethod , produceAnnotion, consumeAnnotion , 
                        httpAnnotation, resourcePath.toString(), method , 
                        (ExecutableType)methodMirror , 
                        controller , boxedPrimitives );
                return Collections.singletonList(restMethod);
            }
            else {
                // returnTypeMirror represent mirror of resource locator
                Element returnElement = controller.getTypes().asElement( returnTypeMirror );
                if ( returnElement instanceof TypeElement ){
                    String resourePath = resource.getPath();
                    if ( resource.getName() != null ){
                        resourePath = "";
                    }
                    StringBuilder newPath = new StringBuilder( resourePath );
                    if ( !resource.getPath().endsWith("/") ){   // NOI18N
                        newPath.append('/');
                    }
                    newPath.append( path );
                    Resource resourceLocator = new Resource(null, newPath.toString() ); 
                    return doBuildResource(resourceLocator, (TypeElement)returnElement, 
                            controller, boxedPrimitives );
                }
                return Collections.emptyList();
            }
        }
        else if ( httpAnnotation!=null ){
            Method restMethod = createMethod( method.getSimpleName().toString(),
                    clazz , controller);
            String resourcePath = resource.getPath();
            if ( resource.getName() != null ){
                resourcePath = null;
            }
            configureMethod( restMethod , produceAnnotion, consumeAnnotion , 
                    httpAnnotation, resourcePath , method,(ExecutableType)methodMirror , 
                    controller, boxedPrimitives );
            return Collections.singletonList(restMethod);
        }
        else {
            return Collections.emptyList();
        }
    }
    
    private void configureMethod( Method restMethod,
            AnnotationMirror produceAnnotion, AnnotationMirror consumeAnnotion,
            AnnotationMirror httpAnnotation, String path , 
            ExecutableElement methodElement, ExecutableType method,
            CompilationController controller , Collection<TypeMirror> boxedPrimitives)
    {
        String fqn = ((TypeElement)httpAnnotation.getAnnotationType().asElement()).
            getQualifiedName().toString();
        if ( RestConstants.GET.equals( fqn ) || RestConstants.GET_JAKARTA.equals( fqn ) ){
            List<String> mimes = getAnnotationMimes(produceAnnotion );
            if ( mimes != null ) { 
                restMethod.setResponseMimes( mimes );
            }
        }
        else {
            List<String> mimes = getAnnotationMimes(produceAnnotion );
            if ( mimes != null ) { 
                restMethod.setResponseMimes( mimes );
            }
            mimes = getAnnotationMimes( consumeAnnotion);
            if ( mimes != null ) { 
                restMethod.setRequestMimes( mimes );
            }
        }
        restMethod.setPath(path);
        
        TypeMirror returnTypeMirror = method.getReturnType();
        RestEntity returnEntity = getRestEntity(controller, boxedPrimitives, 
                returnTypeMirror);
        restMethod.setReturnType(returnEntity);
        RestEntity paramEntity = getParamEntity(controller, boxedPrimitives, 
                methodElement, method);
        restMethod.setParamType( paramEntity );
        for( HttpMethodType type : HttpMethodType.values() ){
            String annotationTypeJakarta = type.getAnnotationType(true);
            String annotationTypeJavax = type.getAnnotationType(false);
            if (annotationTypeJakarta.equals(fqn) || annotationTypeJavax.equals(fqn)) {
                restMethod.setType(type);
                break;
            }
        }
        
    }

    private RestEntity getParamEntity( CompilationController controller,
            Collection<TypeMirror> boxedPrimitives, 
            ExecutableElement methodElement, ExecutableType method )

    {
        List<? extends VariableElement> parameters = methodElement.getParameters();
        int index=-1;
        int i=0;
        for (VariableElement variableElement : parameters) {
            List<? extends AnnotationMirror> annotationMirrors = 
                variableElement.getAnnotationMirrors();
            boolean isUriParam = false;
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                DeclaredType annotationType = annotationMirror.getAnnotationType();
                Element annotationElement = annotationType.asElement();
                if ( annotationElement instanceof TypeElement ){
                    String fqn = ((TypeElement)annotationElement).
                        getQualifiedName().toString();
                    // skip arguments which are URI parameters ( query param or path param )
                    if (fqn.equals(RestConstants.QUERY_PARAM_JAKARTA)
                            || fqn.equals(RestConstants.QUERY_PARAM)
                            || fqn.equals(RestConstants.PATH_PARAM_JAKARTA)
                            || fqn.equals(RestConstants.PATH_PARAM)
                            )
                    {
                        isUriParam = true;
                        break;
                    }
                }
            }
            if ( !isUriParam ){
                index = i;
                break;
            }
            i++;
        }
        
        if ( index==-1 ){
            return new RestEntity(true);
        }
        List<? extends TypeMirror> parameterTypes = method.getParameterTypes();
        TypeMirror typeMirror = parameterTypes.get( index );
        return getRestEntity(controller, boxedPrimitives, typeMirror);
    }

    private RestEntity getRestEntity( CompilationController controller,
            Collection<TypeMirror> boxedPrimitives, TypeMirror typeMirror )
    {
        if ( typeMirror.getKind() == TypeKind.VOID ){
            return new RestEntity( true );
        }
        String entityFqn = null;
        boolean isCollection = false;
        boolean isPrimitive = false;
        if ( controller.getTypes().isSubtype( controller.getTypes().erasure(typeMirror), 
                controller.getTypes().erasure(controller.getElements().getTypeElement(
                        Collection.class.getCanonicalName()).asType())) )
        {
            isCollection = true;
            List<? extends TypeMirror> typeArguments = 
                ((DeclaredType)typeMirror).getTypeArguments();
            
            if ( typeArguments.size() != 0 ){
                // There should be only one parameter
                TypeMirror typeArg = typeArguments.get( 0 );
                entityFqn = getQualifiedName(controller, typeArg);
            }
        }
        else if ( typeMirror.getKind() == TypeKind.ARRAY ){
            isCollection = true;
            TypeMirror componentType = ((ArrayType)typeMirror).
                getComponentType();
            entityFqn = getQualifiedName(controller, componentType);
        }
        if ( typeMirror.getKind().isPrimitive() || 
                controller.getTypes().isSameType(typeMirror, 
                        controller.getElements().
                        getTypeElement(String.class.getCanonicalName()).asType()))
        {
            isPrimitive = true;
        }
        else {
            for ( TypeMirror boxed: boxedPrimitives ){
                if ( controller.getTypes().isSameType(typeMirror, boxed)){
                    isPrimitive = true;
                    break;
                }
            }
        }
        if ( !isCollection && !isPrimitive ){
            entityFqn = getQualifiedName(controller, typeMirror );
        }
        if ( entityFqn== null ){
            entityFqn = Object.class.getCanonicalName();
        }
        
        if ( isPrimitive){
            return new RestEntity( false );
        }
        return new RestEntity( entityFqn, isCollection );
    }

    private String getQualifiedName( CompilationController controller,
            TypeMirror typeMirror )
    {
        String entityFqn = null;
        if ( typeMirror instanceof DeclaredType ){
            Element parameterElement = controller.getTypes().asElement( 
                    typeMirror );
            if ( parameterElement instanceof TypeElement ){
                entityFqn = ((TypeElement)parameterElement).
                getQualifiedName().toString();
            }
        }
        return entityFqn;
    }

    private <T> T chooseNotNull( T... choice ){
        for( T t : choice ){
            if ( t!= null ){
                return t;
            }
        }
        return null;    
    }
    
    private boolean isRestAnnotation(String annotation) {
        return annotation.equals(RestConstants.PATH_JAKARTA)
                || annotation.equals(RestConstants.PATH)
                || annotation.equals(RestConstants.PRODUCE_MIME_JAKARTA)
                || annotation.equals(RestConstants.PRODUCE_MIME)
                || annotation.equals(RestConstants.CONSUME_MIME_JAKARTA)
                || annotation.equals(RestConstants.CONSUME_MIME)
                || annotation.equals(RestConstants.GET_JAKARTA)
                || annotation.equals(RestConstants.GET)
                || annotation.equals(RestConstants.POST_JAKARTA)
                || annotation.equals(RestConstants.POST)
                || annotation.equals(RestConstants.PUT_JAKARTA)
                || annotation.equals(RestConstants.PUT)
                || annotation.equals(RestConstants.DELETE_JAKARTA)
                || annotation.equals(RestConstants.DELETE)
                ;
    }
    
    private List<String> getAnnotationMimes( AnnotationMirror mirror ) {
        if ( mirror == null ){
            return null;
        }
        String mimes = RestUtils.getValueFromAnnotation(mirror);
        String[] mimeTypes = mimes.split(",");
        List<String> result = new ArrayList<String>( mimeTypes.length );
        for (String mime : mimeTypes) {
            mime = mime.trim();
            if (mime.startsWith("\"")) {
                mime = mime.substring(1);
            }
            if (mime.endsWith("\"")) {
                mime = mime.substring(0, mime.length() - 1);
            }
            result.add( mime );
        }
        return result;
    }

    private Method createMethod(String mName , TypeElement clazz , 
            CompilationController controller ) 
    {
        Method method = new Method(mName);
        
        List<? extends AnnotationMirror> annotationMirrors = 
            controller.getElements().getAllAnnotationMirrors( clazz );
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            Element annotationElement = annotationMirror.getAnnotationType().
                asElement();
            if ( annotationElement instanceof TypeElement ){
                TypeElement annotation = (TypeElement)annotationElement;
                String name = annotation.getQualifiedName().toString();
                if ( RestConstants.PRODUCE_MIME_JAKARTA.equals( name ) || RestConstants.PRODUCE_MIME.equals( name ) ){
                    List<String> mimes = getAnnotationMimes( annotationMirror );
                    method.setResponseMimes( mimes );
                }
                else if ( RestConstants.CONSUME_MIME_JAKARTA.equals( name ) || RestConstants.CONSUME_MIME.equals( name ) ){
                    List<String> mimes = getAnnotationMimes( annotationMirror );
                    method.setRequestMimes( mimes );
                }
            }
        }
        
        return method;
    }
    
    private Project project;
    private Set<String> resourceFqns;

}