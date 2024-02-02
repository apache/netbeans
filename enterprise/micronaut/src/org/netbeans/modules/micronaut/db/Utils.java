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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
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
    private static final String CRUD_REPOSITORY_TYPE_NAME = "io.micronaut.data.repository.CrudRepository"; //NOI18N
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
        AnnotationMirror controllerAnn = Utils.getAnnotation(te.getAnnotationMirrors(), CONTROLLER_ANNOTATION_NAME);
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
                    List<ExecutableElement> repositoryMethods = ElementFilter.methodsIn(info.getElements().getAllMembers(repositoryTypeElement));
                    String listMethodName = getEndpointMethodName("findAll", id); //NOI18N
                    if (Utils.startsWith(listMethodName, prefix) && Utils.getAnnotatedMethod(methods, listMethodName, GET_ANNOTATION_NAME, id) == null) {
                        ExecutableElement delegateMethod = null;
                        for (ExecutableElement method : repositoryMethods.stream().filter(el -> "findAll".contentEquals(el.getSimpleName())).collect(Collectors.toList())) { //NOI18N
                            List<? extends VariableElement> params = method.getParameters();
                            if (delegateMethod == null && params.isEmpty()) {
                                delegateMethod = method;
                            } else if (params.size() == 1) {
                                TypeMirror paramType = params.get(0).asType();
                                if (paramType.getKind() == TypeKind.DECLARED && "io.micronaut.data.model.Pageable".contentEquals(((TypeElement) ((DeclaredType) paramType).asElement()).getQualifiedName())) { //NOI18N
                                    delegateMethod = method;
                                }
                            }
                        }
                        if (delegateMethod != null) {
                            consumer.accept(repository, delegateMethod, id);
                        }
                    }
                    String getMethodName = getEndpointMethodName("findById", id); //NOI18N
                    if (Utils.startsWith(getMethodName, prefix) && Utils.getAnnotatedMethod(methods, getMethodName, GET_ANNOTATION_NAME, id != null ? id + "/{id}" : "/{id}") == null) { //NOI18N
                        Optional<ExecutableElement> method = repositoryMethods.stream().filter(el -> "findById".contentEquals(el.getSimpleName()) && el.getParameters().size() == 1).findAny(); //NOI18N
                        if (method.isPresent()) {
                            consumer.accept(repository, method.get(), id);
                        }
                    }
                    String deleteMethodName = getEndpointMethodName("deleteById", id); //NOI18N
                    if (Utils.startsWith(deleteMethodName, prefix) && Utils.getAnnotatedMethod(methods, deleteMethodName, DELETE_ANNOTATION_NAME, id != null ? id + "/{id}" : "/{id}") == null) { //NOI18N
                        Optional<ExecutableElement> method = repositoryMethods.stream().filter(el -> "deleteById".contentEquals(el.getSimpleName()) && el.getParameters().size() == 1).findAny(); //NOI18N
                        if (method.isPresent()) {
                            consumer.accept(repository, method.get(), id);
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

    public static ExecutableElement getAnnotatedMethod(List<ExecutableElement> methods, String methodName, String annotationName, String value) {
        for (ExecutableElement method : methods) {
            if (startsWith(method.getSimpleName().toString(), methodName)) {
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
        return null;
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

    public static MethodTree createControllerDataEndpointMethod(WorkingCopy copy, TypeElement repositoryTypeElement, String repositoryFieldName, String delegateMethodName, String idProefix) {
        TypeMirror repositoryType = repositoryTypeElement.asType();
        if (repositoryType.getKind() == TypeKind.DECLARED) {
            List<ExecutableElement> repositoryMethods = ElementFilter.methodsIn(copy.getElements().getAllMembers(repositoryTypeElement));
            ExecutableElement delegateMethod = null;
            if ("findAll".equals(delegateMethodName)) { //NOI18N
                for (ExecutableElement method : repositoryMethods.stream().filter(el -> delegateMethodName.contentEquals(el.getSimpleName())).collect(Collectors.toList())) {
                    List<? extends VariableElement> params = method.getParameters();
                    if (delegateMethod == null && params.isEmpty()) {
                        delegateMethod = method;
                    } else if (params.size() == 1) {
                        TypeMirror paramType = params.get(0).asType();
                        if (paramType.getKind() == TypeKind.DECLARED && "io.micronaut.data.model.Pageable".contentEquals(((TypeElement) ((DeclaredType) paramType).asElement()).getQualifiedName())) { //NOI18N
                            delegateMethod = method;
                        }
                    }
                }
            } else {
                delegateMethod = repositoryMethods.stream().filter(method -> delegateMethodName.contentEquals(method.getSimpleName()) && method.getParameters().size() == 1).findAny().orElse(null);
            }
            if (delegateMethod != null) {
                return createControllerDataEndpointMethod(copy, (DeclaredType) repositoryType, repositoryFieldName, delegateMethod, idProefix);
            }
        }
        return null;
    }

    public static MethodTree createControllerDataEndpointMethod(WorkingCopy copy, DeclaredType repositoryType, String repositoryFieldName, ExecutableElement delegateMethod, String idPrefix) {
        switch (delegateMethod.getSimpleName().toString()) {
            case "findAll": //NOI18N
                return createControllerListMethod(copy, repositoryType, repositoryFieldName, delegateMethod, idPrefix);
            case "findById": //NOI18N
                return createControllerGetMethod(copy, repositoryType, repositoryFieldName, delegateMethod, idPrefix);
            case "deleteById": //NOI18N
                return createControllerDeleteMethod(copy, repositoryType, repositoryFieldName, delegateMethod, idPrefix);
        }
        return null;
    }

    public static String getEndpointMethodName(String delegateMethodName, String postfix) {
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

    private static MethodTree createControllerGetMethod(WorkingCopy copy, DeclaredType repositoryType, String repositoryFieldName, ExecutableElement delegateMethod, String idPrefix) {
        TreeMaker tm = copy.getTreeMaker();
        GenerationUtils gu = GenerationUtils.newInstance(copy);
        ModifiersTree mods = tm.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.singletonList(gu.createAnnotation("io.micronaut.http.annotation.Get", Collections.singletonList(tm.Literal(idPrefix != null ? idPrefix + "/{id}" : "/{id}"))))); //NOI18N
        ExecutableType type = (ExecutableType) copy.getTypes().asMemberOf(repositoryType, delegateMethod);
        VariableTree param = tm.Variable(tm.Modifiers(Collections.emptySet()), "id", tm.Type(type.getParameterTypes().get(0)), null); //NOI18N
        return tm.Method(mods, getEndpointMethodName(delegateMethod.getSimpleName().toString(), idPrefix), tm.Type(type.getReturnType()), Collections.<TypeParameterTree>emptyList(), Collections.singletonList(param), Collections.<ExpressionTree>emptyList(), "{return " + repositoryFieldName + "." + delegateMethod.getSimpleName() + "(id);}", null); //NOI18N
    }

    private static MethodTree createControllerDeleteMethod(WorkingCopy copy, DeclaredType repositoryType, String repositoryFieldName, ExecutableElement delegateMethod, String idPrefix) {
        TreeMaker tm = copy.getTreeMaker();
        GenerationUtils gu = GenerationUtils.newInstance(copy);
        ModifiersTree mods = tm.Modifiers(Collections.singleton(Modifier.PUBLIC), Arrays.asList(new AnnotationTree[] {
            gu.createAnnotation("io.micronaut.http.annotation.Delete", Collections.singletonList(tm.Literal(idPrefix != null ? idPrefix + "/{id}" : "/{id}"))), //NOI18N
            gu.createAnnotation("io.micronaut.http.annotation.Status", Collections.singletonList(tm.MemberSelect(tm.QualIdent("io.micronaut.http.HttpStatus"), "NO_CONTENT"))) //NOI18N
        }));
        ExecutableType type = (ExecutableType) copy.getTypes().asMemberOf(repositoryType, delegateMethod);
        VariableTree param = tm.Variable(tm.Modifiers(Collections.emptySet()), "id", tm.Type(type.getParameterTypes().get(0)), null); //NOI18N
        return tm.Method(mods, getEndpointMethodName(delegateMethod.getSimpleName().toString(), idPrefix), tm.Type(type.getReturnType()), Collections.<TypeParameterTree>emptyList(), Collections.singletonList(param), Collections.<ExpressionTree>emptyList(), "{" + repositoryFieldName + "." + delegateMethod.getSimpleName() + "(id);}", null); //NOI18N
    }

    private static MethodTree createControllerListMethod(WorkingCopy copy, DeclaredType repositoryType, String repositoryFieldName, ExecutableElement delegateMethod, String idPrefix) {
        TreeMaker tm = copy.getTreeMaker();
        GenerationUtils gu = GenerationUtils.newInstance(copy);
        ModifiersTree mods = tm.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.singletonList(gu.createAnnotation("io.micronaut.http.annotation.Get", idPrefix != null ? Collections.singletonList(tm.Literal(idPrefix)) : Collections.emptyList()))); //NOI18N
        if (delegateMethod.getParameters().isEmpty()) {
            TypeMirror returnType = ((ExecutableType) copy.getTypes().asMemberOf(repositoryType, delegateMethod)).getReturnType();
            return tm.Method(mods, getEndpointMethodName(delegateMethod.getSimpleName().toString(), idPrefix), tm.Type(returnType), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{return " + repositoryFieldName + "." + delegateMethod.getSimpleName() + "();}", null); //NOI18N
        } else {
            ExecutableType type = (ExecutableType) copy.getTypes().asMemberOf(repositoryType, delegateMethod);
            VariableTree param = tm.Variable(tm.Modifiers(0, Collections.singletonList(gu.createAnnotation("jakarta.validation.Valid"))), "pageable", tm.Type(type.getParameterTypes().get(0)), null); //NOI18N
            TypeMirror returnType = type.getReturnType();
            if (returnType.getKind() == TypeKind.DECLARED) {
                TypeElement te = (TypeElement) ((DeclaredType) returnType).asElement();
                Optional<ExecutableElement> getContentMethod = ElementFilter.methodsIn(copy.getElements().getAllMembers(te)).stream().filter(m -> "getContent".contentEquals(m.getSimpleName()) && m.getParameters().isEmpty()).findAny();
                if (getContentMethod.isPresent()) {
                    returnType = ((ExecutableType) copy.getTypes().asMemberOf((DeclaredType) returnType, getContentMethod.get())).getReturnType();
                    return tm.Method(mods, getEndpointMethodName(delegateMethod.getSimpleName().toString(), idPrefix), tm.Type(returnType), Collections.<TypeParameterTree>emptyList(), Collections.singletonList(param), Collections.<ExpressionTree>emptyList(), "{return " + repositoryFieldName + "." + delegateMethod.getSimpleName() + "(pageable).getContent();}", null); //NOI18N
                }
            }
        }
        return null;
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
        public void accept(VariableElement repository, ExecutableElement delegateMethod, String id);
    }
}
