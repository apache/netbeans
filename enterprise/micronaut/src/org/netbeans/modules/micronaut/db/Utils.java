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
package org.netbeans.modules.micronaut.db;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.openide.util.WeakListeners;

/**
 *
 * @author Dusan Balek
 */
public final class Utils {

    private static final String CONTROLLER_ANNOTATION_NAME = "io.micronaut.http.annotation.Controller"; //NOI18N
    private static final String GET_ANNOTATION_NAME = "io.micronaut.http.annotation.Get"; //NOI18N
    private static final String DELETE_ANNOTATION_NAME = "io.micronaut.http.annotation.Delete"; //NOI18N
    private static final String PUT_ANNOTATION_NAME = "io.micronaut.http.annotation.Put"; //NOI18N
    private static final String POST_ANNOTATION_NAME = "io.micronaut.http.annotation.Post"; //NOI18N
    private static final String BODY_ANNOTATION_NAME = "io.micronaut.http.annotation.Body"; //NOI18N
    private static final String VALID_ANNOTATION_NAME = "jakarta.validation.Valid"; //NOI18N
    private static final String CRUD_REPOSITORY_TYPE_NAME = "io.micronaut.data.repository.CrudRepository"; //NOI18N
    private static final String PAGEABLE_REPOSITORY_TYPE_NAME = "io.micronaut.data.repository.PageableRepository"; //NOI18N
    private static final String PAGEABLE_TYPE_NAME = "io.micronaut.data.model.Pageable"; //NOI18N
    private static final String HTTP_RESPONSE_TYPE_NAME = "io.micronaut.http.HttpResponse"; //NOI18N
    private static final String COMPLETION_CASE_SENSITIVE = "completion-case-sensitive"; //NOI18N
    private static final boolean COMPLETION_CASE_SENSITIVE_DEFAULT = true;
    private static final String JAVA_COMPLETION_SUBWORDS = "javaCompletionSubwords"; //NOI18N
    private static final boolean JAVA_COMPLETION_SUBWORDS_DEFAULT = false;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || COMPLETION_CASE_SENSITIVE.equals(settingName)) {
                caseSensitive = preferences.getBoolean(COMPLETION_CASE_SENSITIVE, COMPLETION_CASE_SENSITIVE_DEFAULT);
            }
            if (settingName == null || JAVA_COMPLETION_SUBWORDS.equals(settingName)) {
                javaCompletionSubwords = preferences.getBoolean(JAVA_COMPLETION_SUBWORDS, JAVA_COMPLETION_SUBWORDS_DEFAULT);
            }
        }
    };
    private static final AtomicBoolean inited = new AtomicBoolean(false);

    private static Preferences preferences;
    private static boolean caseSensitive = COMPLETION_CASE_SENSITIVE_DEFAULT;
    private static boolean javaCompletionSubwords = JAVA_COMPLETION_SUBWORDS_DEFAULT;
    private static String cachedPrefix = null;
    private static Pattern cachedCamelCasePattern = null;
    private static Pattern cachedSubwordsPattern = null;

    public static List<VariableElement> collectMissingDataEndpoints(CompilationInfo info, TypeElement te, String prefix, DataEndpointConsumer consumer) {
        AnnotationMirror controllerAnn = getAnnotation(te.getAnnotationMirrors(), CONTROLLER_ANNOTATION_NAME);
        if (controllerAnn == null) {
            return Collections.emptyList();
        }
        List<VariableElement> repositories = getRepositoriesFor(info, te);
        if (!repositories.isEmpty()) {
            String controllerId = null;
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : controllerAnn.getElementValues().entrySet()) {
                if ("value".contentEquals(entry.getKey().getSimpleName()) || "uri".contentEquals(entry.getKey().getSimpleName())) { // NOI18N
                    controllerId = (String) entry.getValue().getValue();
                }
            }
            List<ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
            for (VariableElement repository : repositories) {
                TypeMirror repositoryType = repository.asType();
                if (repositoryType.getKind() == TypeKind.DECLARED) {
                    TypeMirror pageableRepositoryType = info.getTypes().erasure(info.getElements().getTypeElement(PAGEABLE_REPOSITORY_TYPE_NAME).asType());
                    boolean isPageableRepository = info.getTypes().isSubtype(info.getTypes().erasure(repositoryType), pageableRepositoryType);
                    TypeElement repositoryTypeElement = (TypeElement) ((DeclaredType) repositoryType).asElement();
                    String id = null;
                    if (repositories.size() > 1) {
                        id = '/' + repositoryTypeElement.getSimpleName().toString().toLowerCase();
                        if (id.endsWith("repository")) { //NOI18N
                            id = id.substring(0, id.length() - 10);
                        }
                        if (controllerId != null && !controllerId.equals(id)) {
                            continue;
                        }
                    }
                    List<ExecutableElement> repositoryMethods = ElementFilter.methodsIn(info.getElements().getAllMembers(repositoryTypeElement)).stream().filter(method -> {
                        String methodName = method.getSimpleName().toString();
                        if ("findAll".equals(methodName)) { //NOI18N
                            if (isPageableRepository) {
                                TypeMirror paramType = method.getParameters().size() == 1 ? method.getParameters().get(0).asType() : null;
                                return paramType != null && paramType.getKind() == TypeKind.DECLARED && PAGEABLE_TYPE_NAME.contentEquals(((TypeElement) ((DeclaredType) paramType).asElement()).getQualifiedName());
                            }
                            return method.getParameters().isEmpty();
                        }
                        if (methodName.endsWith("All")) { //NOI18N
                            return false;
                        }
                        if (methodName.startsWith("find")) { //NOI18N
                            return true;
                        }
                        if (methodName.startsWith("delete")) { //NOI18N
                            return true;
                        }
                        if (methodName.startsWith("save")) { //NOI18N
                            return true;
                        }
                        if (methodName.startsWith("update")) { //NOI18N
                            return true;
                        }
                        return false;
                    }).collect(Collectors.toList());
                    for (ExecutableElement repositoryMethod : repositoryMethods) {
                        if (getEndpointMethodFor(info, methods, (DeclaredType) repository.asType(), repositoryMethod, id) == null) {
                            consumer.accept(repository, repositoryMethod, controllerId, id);
                        }
                    }
                }
            }
        }
        return repositories;
    }

    public static AnnotationMirror getAnnotation(List<? extends AnnotationMirror> annotations, String annotationName) {
        return getAnnotation(annotations, annotationName, new HashSet<>());
    }

    public static List<VariableElement> getRepositoriesFor(CompilationInfo info, TypeElement te) {
        List<VariableElement> repositories = new ArrayList<>();
        TypeMirror tm = info.getTypes().erasure(info.getElements().getTypeElement(CRUD_REPOSITORY_TYPE_NAME).asType());
        for (VariableElement ve : ElementFilter.fieldsIn(te.getEnclosedElements())) {
            if (ve.asType().getKind() == TypeKind.DECLARED && info.getTypes().isSubtype(info.getTypes().erasure(ve.asType()), tm)) {
                repositories.add(ve);
            }
        }
        return repositories;
    }

    public static MethodTree createControllerFindAllDataEndpointMethod(WorkingCopy copy, TypeElement repositoryTypeElement, String repositoryFieldName, String controllerId, String idProefix) {
        TypeMirror repositoryType = repositoryTypeElement.asType();
        if (repositoryType.getKind() == TypeKind.DECLARED) {
            TypeMirror pageableRepositoryType = copy.getTypes().erasure(copy.getElements().getTypeElement(PAGEABLE_REPOSITORY_TYPE_NAME).asType());
            boolean isPageableRepository = copy.getTypes().isSubtype(copy.getTypes().erasure(repositoryType), pageableRepositoryType);
            ExecutableElement delegateMethod = ElementFilter.methodsIn(copy.getElements().getAllMembers(repositoryTypeElement)).stream().filter(method -> {
                if (!"findAll".contentEquals(method.getSimpleName())) { //NOI18N
                    return false;
                }
                if (isPageableRepository) {
                    TypeMirror paramType = method.getParameters().size() == 1 ? method.getParameters().get(0).asType() : null;
                    return paramType != null && paramType.getKind() == TypeKind.DECLARED && PAGEABLE_TYPE_NAME.contentEquals(((TypeElement) ((DeclaredType) paramType).asElement()).getQualifiedName());
                }
                return method.getParameters().isEmpty();
            }).findFirst().orElse(null);
            if (delegateMethod != null) {
                return createControllerDataEndpointMethod(copy, (DeclaredType) repositoryType, repositoryFieldName, delegateMethod, controllerId, idProefix);
            }
        }
        return null;
    }

    public static MethodTree createControllerDataEndpointMethod(WorkingCopy copy, DeclaredType repositoryType, String repositoryFieldName, ExecutableElement delegateMethod, String controllerId, String idPrefix) {
        TreeMaker tm = copy.getTreeMaker();
        GenerationUtils gu = GenerationUtils.newInstance(copy);
        ExecutableType delegateMethodType = (ExecutableType) copy.getTypes().asMemberOf(repositoryType, delegateMethod);
        String delegateMethodName = delegateMethod.getSimpleName().toString();
        List<AnnotationTree> annotations = new ArrayList<>();
        String annotationTypeName = getControllerDataEndpointAnnotationTypeName(delegateMethodName);
        String value = getControllerDataEndpointAnnotationValue(delegateMethod, delegateMethodType, idPrefix);
        annotations.add(value != null ? gu.createAnnotation(annotationTypeName, List.of(tm.Literal(value))) : gu.createAnnotation(annotationTypeName));
        if (DELETE_ANNOTATION_NAME.equals(annotationTypeName)) {
            annotations.add(gu.createAnnotation("io.micronaut.http.annotation.Status", List.of(tm.MemberSelect(tm.QualIdent("io.micronaut.http.HttpStatus"), "NO_CONTENT")))); //NOI18N
        };
        ModifiersTree mods = tm.Modifiers(Set.of(Modifier.PUBLIC), annotations);
        String methodName = getControllerDataEndpointMethodName(delegateMethodName, idPrefix);
        TypeMirror returnType = getControllerDataEndpointReturnType(copy, delegateMethodName, delegateMethodType);
        List<TypeParameterTree> typeParams = new ArrayList<>();
        for (TypeVariable tv : delegateMethodType.getTypeVariables()) {
            typeParams.add(tm.TypeParameter(tv.asElement().getSimpleName(), List.of((ExpressionTree) tm.Type(tv.getUpperBound()))));
        }
        List<? extends VariableTree> params = getControllerDataEndpointParams(copy, delegateMethod, delegateMethodType);
        String body = getControllerDataEndpointBody(copy, repositoryFieldName, delegateMethod, delegateMethodType, controllerId, idPrefix);
        return tm.Method(mods, methodName, tm.Type(returnType), typeParams, params, List.of(), body, null);
    }

    public static String getControllerDataEndpointMethodName(String delegateMethodName, String postfix) {
        String name;
        switch (delegateMethodName) {
            case "findAll": //NOI18N
                name = "list"; //NOI18N
                break;
            case "findById": //NOI18N
                name = "get"; //NOI18N
                break;
            case "deleteById": //NOI18N
                name = "delete"; //NOI18N
                break;
            default:
                name = delegateMethodName;
        }
        if (postfix != null) {
            if (postfix.startsWith("/")) { //NOI18N
                postfix = postfix.substring(1);
            }
            if (!postfix.isEmpty()) {
                return name + Character.toUpperCase(postfix.charAt(0)) + postfix.substring(1);
            }
        }
        return name;
    }

    public static TypeMirror getControllerDataEndpointReturnType(CompilationInfo info, String delegateMethodName, ExecutableType type) {
        TypeMirror returnType = type.getReturnType();
        if (delegateMethodName.startsWith("update")) { //NOI18N
            returnType = info.getTypes().getDeclaredType(info.getElements().getTypeElement(HTTP_RESPONSE_TYPE_NAME));
        } else if (delegateMethodName.startsWith("save")) { //NOI18N
            returnType = info.getTypes().getDeclaredType(info.getElements().getTypeElement(HTTP_RESPONSE_TYPE_NAME), returnType);
        } else if ("findAll".equals(delegateMethodName) && !type.getParameterTypes().isEmpty() && returnType.getKind() == TypeKind.DECLARED) { //NOI18N
            TypeElement te = (TypeElement) ((DeclaredType) returnType).asElement();
            Optional<ExecutableElement> getContentMethod = ElementFilter.methodsIn(info.getElements().getAllMembers(te)).stream().filter(m -> "getContent".contentEquals(m.getSimpleName()) && m.getParameters().isEmpty()).findAny(); //NOI18N
            if (getContentMethod.isPresent()) {
                returnType = ((ExecutableType) info.getTypes().asMemberOf((DeclaredType) returnType, getContentMethod.get())).getReturnType();
            }
        }
        return returnType;
    }

    public static String getControllerDataEndpointAnnotationTypeName(String delegateMethodName) {
        if (delegateMethodName.startsWith("find")) { //NOI18N
            return GET_ANNOTATION_NAME;
        }
        if (delegateMethodName.startsWith("delete")) { //NOI18N
            return DELETE_ANNOTATION_NAME;
        }
        if (delegateMethodName.startsWith("save")) { //NOI18N
            return POST_ANNOTATION_NAME;
        }
        if (delegateMethodName.startsWith("update")) { //NOI18N
            return PUT_ANNOTATION_NAME;
        }
        return null;
    }

    public static String getControllerDataEndpointAnnotationValue(ExecutableElement delegateMethod, ExecutableType delegateMethodType, String idPrefix) {
        String delegateMethodName = delegateMethod.getSimpleName().toString();
        if (delegateMethodName.endsWith("ById") && !delegateMethod.getParameters().isEmpty()) { //NOI18N
            String id = delegateMethod.getParameters().get(0).getSimpleName().toString();
            return idPrefix != null ? idPrefix + "/{" + id + "}" : "/{" + id + "}"; //NOI18N
        }
        if (delegateMethodName.startsWith("update")) { //NOI18N
            VariableElement idElement = getIdElement(delegateMethod.getParameters());
            if (idElement != null) {
                String id = idElement.getSimpleName().toString();
                return idPrefix != null ? idPrefix + "/{" + id + "}" : "/{" + id + "}"; //NOI18N
            }
        }
        return idPrefix;
    }

    public static  List<TypeElement> getRelevantAnnotations(VariableElement variableElement) {
        List<TypeElement> annotations = new ArrayList<>();
        for (AnnotationMirror am : variableElement.getAnnotationMirrors()) {
            TypeElement te = (TypeElement) am.getAnnotationType().asElement();
            String fqn = te.getQualifiedName().toString();
            if (fqn.equals("io.micronaut.data.annotation.Id") || fqn.startsWith("jakarta.validation.constraints.")) { //NOI18N
                annotations.add(te);
            }
        }
        return annotations;
    }

    public static boolean isJPASupported(SourceGroup sg) {
        return resolveClassName(sg, "io.micronaut.data.jpa.repository.JpaRepository"); //NOI18N
    }

    public static boolean isDBSupported(SourceGroup sg) {
        return resolveClassName(sg, "io.micronaut.data.annotation.Id"); //NOI18N
    }

    public static boolean startsWith(String theString, String prefix) {
        return isCamelCasePrefix(prefix) ? isCaseSensitive()
                ? startsWithCamelCase(theString, prefix)
                : startsWithCamelCase(theString, prefix) || startsWithPlain(theString, prefix)
                : startsWithPlain(theString, prefix);
    }

    public static CharSequence getTypeName(CompilationInfo info, TypeMirror type, boolean fqn, boolean varArg) {
        Set<TypeUtilities.TypeNameOptions> options = EnumSet.noneOf(TypeUtilities.TypeNameOptions.class);
        if (fqn) {
            options.add(TypeUtilities.TypeNameOptions.PRINT_FQN);
        }
        if (varArg) {
            options.add(TypeUtilities.TypeNameOptions.PRINT_AS_VARARG);
        }
        return info.getTypeUtilities().getTypeName(type, options.toArray(new TypeUtilities.TypeNameOptions[0]));
    }

    private static ExecutableElement getEndpointMethodFor(CompilationInfo info, List<ExecutableElement> methods, DeclaredType repositoryType, ExecutableElement delegateMethod, String id) {
        String delegateMethodName = delegateMethod.getSimpleName().toString();
        String annotationName = getControllerDataEndpointAnnotationTypeName(delegateMethodName);
        if (annotationName != null) {
            String methodName = getControllerDataEndpointMethodName(delegateMethodName, id);
            ExecutableType delegateMethodType = (ExecutableType) info.getTypes().asMemberOf(repositoryType, delegateMethod);
            String value = getControllerDataEndpointAnnotationValue(delegateMethod, delegateMethodType, id);
            for (ExecutableElement method : methods) {
                if (methodName.contentEquals(method.getSimpleName())) {
                    AnnotationMirror annotation = getAnnotation(method.getAnnotationMirrors(), annotationName, new HashSet<>());
                    if (annotation != null) {
                        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getElementValues();
                        Object val = null;
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                            if ("value".contentEquals(entry.getKey().getSimpleName())) { //NOI18N
                                val = entry.getValue().getValue();
                            }
                        }
                        if (Objects.equals(value, val)) {
                            return method;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static VariableElement getIdElement(List<? extends VariableElement> elements) {
        for (VariableElement element : elements) {
            for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
                TypeElement annotationElement = (TypeElement) annotation.getAnnotationType().asElement();
                if ("io.micronaut.data.annotation.Id".contentEquals(annotationElement.getQualifiedName()) || "javax.persistence.Id".contentEquals(annotationElement.getQualifiedName())) { //NOI18N
                    return element;
                }
            }
        }
        return null;
    }

    private static List<? extends VariableTree> getControllerDataEndpointParams(WorkingCopy copy, ExecutableElement delegateMethod, ExecutableType type) {
        TreeMaker tm = copy.getTreeMaker();
        GenerationUtils gu = GenerationUtils.newInstance(copy);
        List<VariableTree> params = new ArrayList<>();
        String delegateMethodName = delegateMethod.getSimpleName().toString();
        VariableElement idElem = getIdElement(delegateMethod.getParameters());
        if (idElem == null && delegateMethodName.endsWith("ById") && !delegateMethod.getParameters().isEmpty()) { //NOI18N
            idElem = delegateMethod.getParameters().get(0);
        }
        Iterator<? extends VariableElement> it = delegateMethod.getParameters().iterator();
        Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
        while (it.hasNext() && tIt.hasNext()) {
            VariableElement param = it.next();
            TypeMirror paramType = tIt.next();
            List<AnnotationTree> annotations = new ArrayList<>();
            if ("findAll".equals(delegateMethodName)) { //NOI18N
                annotations.add(gu.createAnnotation(VALID_ANNOTATION_NAME));
            } else if (idElem == null) {
                annotations.add(gu.createAnnotation(BODY_ANNOTATION_NAME));
                annotations.add(gu.createAnnotation(VALID_ANNOTATION_NAME));
            } else if (idElem != param) {
                annotations.add(gu.createAnnotation(BODY_ANNOTATION_NAME, List.of(tm.Literal(param.getSimpleName().toString()))));
                for (AnnotationMirror am : param.getAnnotationMirrors()) {
                    annotations.add(gu.createAnnotation(((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().toString()));
                }
            }
            params.add(tm.Variable(tm.Modifiers(0, annotations), param.getSimpleName(), tm.Type(paramType), null));
        }
        return params;
    }

    private static String getControllerDataEndpointBody(WorkingCopy copy, String repositoryFieldName, ExecutableElement delegateMethod, ExecutableType delegateMethodType, String controllerId, String idPrefix) {
        String delegateMethodName = delegateMethod.getSimpleName().toString();
        StringBuilder delegateMethodCall = new StringBuilder();
        delegateMethodCall.append(repositoryFieldName).append('.').append(delegateMethodName).append('(');
        for (Iterator<? extends VariableElement> it = delegateMethod.getParameters().iterator(); it.hasNext();) {
            VariableElement param = it.next();
            delegateMethodCall.append(param.getSimpleName());
            if (it.hasNext()) {
                delegateMethodCall.append(',');
            }
        }
        delegateMethodCall.append(')');
        if (delegateMethodName.equals("findAll") && !delegateMethod.getParameters().isEmpty()) { //NOI18N
            return "{return " + delegateMethodCall.toString() + ".getContent();}"; //NOI18N
        }
        if (delegateMethodName.startsWith("find")) { //NOI18N
            return "{return " + delegateMethodCall.toString() + ";}"; //NOI18N
        }
        if (delegateMethodName.startsWith("delete")) { //NOI18N
            return "{" + delegateMethodCall.toString() + ";}"; //NOI18N
        }
        if (delegateMethodName.startsWith("save")) { //NOI18N
            copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), Set.of(copy.getElements().getTypeElement("java.net.URI")))); //NOI18N
            String idUri = getIdUri(delegateMethod, delegateMethodType, controllerId, idPrefix);
            CharSequence typeName = getTypeName(copy, delegateMethodType.getReturnType(), false, false);
            StringBuilder sb = new StringBuilder(typeName);
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            return "{" + typeName + " " + sb.toString() + " = " + delegateMethodCall.toString() + "return HttpResponse.created(" + sb.toString() + ").headers(headers -> headers.location(" + idUri + "));}"; //NOI18N
        }
        if (delegateMethodName.startsWith("update")) { //NOI18N
            copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), Set.of(copy.getElements().getTypeElement("java.net.URI"), copy.getElements().getTypeElement("io.micronaut.http.HttpHeaders")))); //NOI18N
            String idUri = getIdUri(delegateMethod, delegateMethodType, controllerId, idPrefix);
            return "{" + delegateMethodCall.toString() + ";return HttpResponse.noContent().header(HttpHeaders.LOCATION, " + idUri+ ".getPath());}"; //NOI18N
        }
        return "{}"; //NOI18N
    }

    private static AnnotationMirror getAnnotation(List<? extends AnnotationMirror> annotations, String annotationName, HashSet<TypeElement> checked) {
        for (AnnotationMirror annotation : annotations) {
            TypeElement annotationElement = (TypeElement) annotation.getAnnotationType().asElement();
            if (annotationName.contentEquals(annotationElement.getQualifiedName())) {
                return annotation;
            }
            if (checked.add(annotationElement)) {
                AnnotationMirror nestedAnnotation = getAnnotation(annotationElement.getAnnotationMirrors(), annotationName, checked);
                if (nestedAnnotation != null) {
                    return nestedAnnotation;
                }
            }
        }
        return null;
    }

    private static String getIdUri(ExecutableElement delegateMethod, ExecutableType delegateMethodType, String controllerId, String idPrefix) {
        StringBuilder idGet = new StringBuilder();
        VariableElement idElem = getIdElement(delegateMethod.getParameters());
        if (idElem != null) {
            idGet.append(idElem.getSimpleName());
        } else {
            Iterator<? extends VariableElement> it = delegateMethod.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = delegateMethodType.getParameterTypes().iterator();
            if (it.hasNext() && tIt.hasNext()) {
                DeclaredType entityType = null;
                TypeMirror tm = tIt.next();
                if (tm.getKind() == TypeKind.TYPEVAR) {
                    TypeMirror upperBound = ((TypeVariable) tm).getUpperBound();
                    if (upperBound.getKind() == TypeKind.DECLARED) {
                        entityType = (DeclaredType) upperBound;
                    }
                } else if (tm.getKind() == TypeKind.DECLARED) {
                    entityType = (DeclaredType) tm;
                }
                if (entityType != null) {
                    VariableElement idField = getIdElement(ElementFilter.fieldsIn(entityType.asElement().getEnclosedElements()));
                    if (idField != null) {
                        StringBuilder getter = new StringBuilder(idField.getSimpleName());
                        getter.setCharAt(0, Character.toUpperCase(getter.charAt(0)));
                        getter.insert(0, "get").append("()"); //NOI18N
                        idGet.append(it.next().getSimpleName()).append('.').append(getter.toString());
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder("URI.create(\""); //NOI18N
        if (controllerId != null) {
            sb.append(controllerId);
        }
        if (idPrefix != null) {
            sb.append(idPrefix);
        }
        sb.append("/\""); //NOI18N
        if (idGet.length() > 0) {
            sb.append(" + ").append(idGet); //NOI18N
        }
        return sb.append(')').toString();
    }

    private static boolean isCamelCasePrefix(String prefix) {
        if (prefix == null || prefix.length() < 2 || prefix.charAt(0) == '"') {
            return false;
        }
        for (int i = 1; i < prefix.length(); i++) {
            if (Character.isUpperCase(prefix.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCaseSensitive() {
        lazyInit();
        return caseSensitive;
    }

    private static boolean isSubwordSensitive() {
        lazyInit();
        return javaCompletionSubwords;
    }

    private static boolean startsWithPlain(String theString, String prefix) {
        if (theString == null || theString.length() == 0) {
            return false;
        }
        if (prefix == null || prefix.length() == 0) {
            return true;
        }
        if (isSubwordSensitive()) {
            if (!prefix.equals(cachedPrefix)) {
                cachedCamelCasePattern = null;
                cachedSubwordsPattern = null;
            }
            if (cachedSubwordsPattern == null) {
                cachedPrefix = prefix;
                String patternString = createSubwordsPattern(prefix);
                cachedSubwordsPattern = patternString != null ? Pattern.compile(patternString) : null;
            }
            if (cachedSubwordsPattern != null && cachedSubwordsPattern.matcher(theString).matches()) {
                return true;
            }
        }
        return isCaseSensitive() ? theString.startsWith(prefix) : theString.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }

    private static String createSubwordsPattern(String prefix) {
        StringBuilder sb = new StringBuilder(3 + 8 * prefix.length());
        sb.append(".*?"); // NOI18N
        for (int i = 0; i < prefix.length(); i++) {
            char charAt = prefix.charAt(i);
            if (!Character.isJavaIdentifierPart(charAt)) {
                return null;
            }
            if (Character.isLowerCase(charAt)) {
                sb.append("["); // NOI18N
                sb.append(charAt);
                sb.append(Character.toUpperCase(charAt));
                sb.append("]"); // NOI18N
            } else {
                //keep uppercase characters as beacons
                // for example: java.lang.System.sIn -> setIn
                sb.append(charAt);
            }
            sb.append(".*?"); // NOI18N
        }
        return sb.toString();
    }

    private static boolean startsWithCamelCase(String theString, String prefix) {
        if (theString == null || theString.length() == 0 || prefix == null || prefix.length() == 0) {
            return false;
        }
        if (!prefix.equals(cachedPrefix)) {
            cachedCamelCasePattern = null;
            cachedSubwordsPattern = null;
        }
        if (cachedCamelCasePattern == null) {
            StringBuilder sb = new StringBuilder();
            int lastIndex = 0;
            int index;
            do {
                index = findNextUpper(prefix, lastIndex + 1);
                String token = prefix.substring(lastIndex, index == -1 ? prefix.length() : index);
                sb.append(token);
                sb.append(index != -1 ? "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                lastIndex = index;
            } while (index != -1);
            cachedPrefix = prefix;
            cachedCamelCasePattern = Pattern.compile(sb.toString());
        }
        return cachedCamelCasePattern.matcher(theString).matches();
    }

    private static int findNextUpper(String text, int offset) {
        for (int i = offset; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static void lazyInit() {
        if (inited.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }

    private static boolean resolveClassName(SourceGroup sg, String fqn) {
        if (sg == null) {
            return false;
        }
        ClassPath compile = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.COMPILE);
        if (compile == null) {
            return false;
        }
        return compile.findResource(fqn.replace('.', '/') + ".class") != null; //NOI18N
    }

    @FunctionalInterface
    public static interface DataEndpointConsumer {
        public void accept(VariableElement repository, ExecutableElement delegateMethod, String controllerId, String id);
    }
}
