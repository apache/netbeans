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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static javax.lang.model.type.TypeKind.BYTE;
import static javax.lang.model.type.TypeKind.CHAR;
import static javax.lang.model.type.TypeKind.DOUBLE;
import static javax.lang.model.type.TypeKind.FLOAT;
import static javax.lang.model.type.TypeKind.INT;
import static javax.lang.model.type.TypeKind.LONG;
import static javax.lang.model.type.TypeKind.SHORT;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lsp.CodeAction;
import org.netbeans.api.lsp.Command;
import org.netbeans.api.lsp.Range;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.micronaut.symbol.MicronautSymbolFinder;
import static org.netbeans.modules.micronaut.symbol.MicronautSymbolFinder.getEndpointMethod;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.lsp.CodeActionProvider;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProviders;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProviders({
    @ServiceProvider(service = CodeActionProvider.class),
    @ServiceProvider(service = CommandProvider.class)
})
public class MicronautEndpointTestGenerator implements CodeActionProvider, CommandProvider {

    private static final String SOURCE = "source";
    private static final Set<String> SUPPORTED_CODE_ACTION_KINDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(SOURCE)));
    private static final String GENERATE_MICRONAUT_ENDPOINT_TEST = "nbls.micronaut.generate.endpoint.test";
    private static final String CONTROLLER_ANNOTATION_NAME = "io.micronaut.http.annotation.Controller";
    private static final String MICRONAUT_TEST_ANNOTATION_NAME = "io.micronaut.test.extensions.junit5.annotation.MicronautTest";
    private static final String MICRONAUT_HTTP_CLIENT_ANNOTATION_NAME = "io.micronaut.http.client.annotation.Client";
    private static final String MICRONAUT_SERDEABLE_ANNOTATION_NAME = "io.micronaut.serde.annotation.Serdeable";
    private static final String MICRONAUT_BODY_ANNOTATION_NAME = "io.micronaut.http.annotation.Body";
    private static final String JAKARTA_INJECT_NAME = "jakarta.inject.Inject";
    private static final String JUPITER_ASSERTIONS_NAME = "org.junit.jupiter.api.Assertions";
    private static final String JUPITER_TEST_ANNOTATION_NAME = "org.junit.jupiter.api.Test";
    private static final String MICRONAUT_HTTP_CLIENT_NAME = "io.micronaut.http.client.HttpClient";
    private static final String MICRONAUT_HTTP_REQUEST_NAME = "io.micronaut.http.HttpRequest";
    private static final String MICRONAUT_HTTP_RESPONSE_NAME = "io.micronaut.http.HttpResponse";
    private static final String MICRONAUT_HTTP_STATUS_NAME = "io.micronaut.http.HttpStatus";
    private static final String MICRONAUT_ARGUMENT_NAME = "io.micronaut.core.type.Argument";
    private static final String LIST_TYPE_NAME = "java.util.List";
    private static final String MAP_TYPE_NAME = "java.util.Map";
    private static final String OPTIONAL_TYPE_NAME = "java.util.Optional";
    private static final String ORIGIN = "origin";
    private static final String FILE_NAME = "fileName";
    private static final String LOCATIONS = "locations";
    private static final String PUT = "PUT";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private static final String EMPTY =  "";

    private final Gson gson = new GsonBuilder().registerTypeAdapter(NotifyDescriptor.QuickPick.Item.class, (JsonDeserializer<NotifyDescriptor.QuickPick.Item>) (JsonElement json, Type type, JsonDeserializationContext jdc) -> {
        String label = json.getAsJsonObject().get("label").getAsString();
        String description = json.getAsJsonObject().get("description").getAsString();
        return new NotifyDescriptor.QuickPick.Item(label, description);
    }).create();

    @Override
    public Set<String> getSupportedCodeActionKinds() {
        return SUPPORTED_CODE_ACTION_KINDS;
    }

    @Override
    @NbBundle.Messages({
        "DN_GenerateEndpointTest=Generate Micronaut Endpoint Tests...",
        "DN_SelectTargetLocation=Select target location"
    })
    public List<CodeAction> getCodeActions(Document doc, Range range, Lookup context) {
        try {
            List<String> only = context.lookup(List.class);
            if (only == null || !only.contains(SOURCE)) {
                return Collections.emptyList();
            }
            ResultIterator resultIterator = context.lookup(ResultIterator.class);
            CompilationController cc = resultIterator != null && resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
            if (cc == null) {
                return Collections.emptyList();
            }
            ClassPath cp = cc.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
            FileObject fileObject = cc.getFileObject();
            if (!fileObject.isValid()) {
                return Collections.emptyList();
            }
            FileObject root = cp.findOwnerRoot(fileObject);
            if (root == null) {
                return Collections.emptyList();
            }
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            int offset = range.getStartOffset();
            TreePath path = cc.getTreeUtilities().pathFor(offset);
            path = cc.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
            if (path == null) {
                return Collections.emptyList();
            }
            TypeElement te = (TypeElement) cc.getTrees().getElement(path);
            if (te == null || !te.getKind().isClass()) {
                return Collections.emptyList();
            }
            AnnotationMirror controllerAnn = Utils.getAnnotation(te.getAnnotationMirrors(), CONTROLLER_ANNOTATION_NAME);
            if (controllerAnn == null) {
                return Collections.emptyList();
            }
            URL[] locations = getMicronautTestLocations(root);
            if (locations == null || locations.length == 0) {
                return Collections.emptyList();
            }
            String name = cp.getResourceName(fileObject, '/', false) + "MicronautTest.java";
            long cnt = Arrays.stream(locations).filter(location -> {
                FileObject fo = URLMapper.findFileObject(location);
                return fo != null && fo.getFileObject(name) != null;
            }).count();
            if (cnt == 0) {
                Map<String, Object> data = new HashMap<>();
                data.put(ORIGIN, te.getQualifiedName().toString());
                data.put(FILE_NAME, name);
                data.put(LOCATIONS, Arrays.stream(locations).map(location -> new NotifyDescriptor.QuickPick.Item(location.toString(), EMPTY)).collect(Collectors.toList()));
                return Collections.singletonList(new CodeAction(Bundle.DN_GenerateEndpointTest(), SOURCE, new Command(Bundle.DN_GenerateEndpointTest(), "nbls.generate.code", Arrays.asList(GENERATE_MICRONAUT_ENDPOINT_TEST, data)), null));
            }
        } catch (IOException | ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(GENERATE_MICRONAUT_ENDPOINT_TEST);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        if (arguments.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        JsonObject data = (JsonObject) arguments.get(0);
        CompletableFuture<Object> future = new CompletableFuture<>();
        RequestProcessor.getDefault().post(() -> {
            String origin = data.getAsJsonPrimitive(ORIGIN).getAsString();
            String name = data.getAsJsonPrimitive(FILE_NAME).getAsString();
            List<NotifyDescriptor.QuickPick.Item> items = Arrays.asList(gson.fromJson(data.get(LOCATIONS), NotifyDescriptor.QuickPick.Item[].class));
            if (items.size() == 1) {
                future.complete(generate(items.get(0).getLabel(), name, origin));
            } else {
                NotifyDescriptor.QuickPick pick = new NotifyDescriptor.QuickPick(Bundle.DN_GenerateEndpointTest(), Bundle.DN_SelectTargetLocation(), items, false);
                if (DialogDescriptor.OK_OPTION != DialogDisplayer.getDefault().notify(pick)) {
                    future.complete(null);
                } else {
                    future.complete(null);
                    List<NotifyDescriptor.QuickPick.Item> selected = pick.getItems().stream().filter(item -> item.isSelected()).collect(Collectors.toList());
                    if (selected.isEmpty()) {
                        future.complete(null);
                    } else {
                        future.complete(generate(selected.get(0).getLabel(), name, origin));
                    }
                }
            }
        });
        return future;
    }

    private URL[] getMicronautTestLocations(FileObject root) {
        Project p = FileOwnerQuery.getOwner(root);
        if (p != null) {
            Project parent = FileOwnerQuery.getOwner(p.getProjectDirectory().getParent());
            if (parent != null) {
                Set<Project> containedProjects = ProjectUtils.getContainedProjects(parent, false);
                if (containedProjects.contains(p)) {
                    List<URL> urls = new ArrayList<>();
                    for (Project cp : containedProjects) {
                        if (cp != p && ProjectUtils.getDependencyProjects(cp, false).contains(p)) {
                            urls.addAll(Arrays.asList(UnitTestForSourceQuery.findUnitTests(cp.getProjectDirectory())));
                        }
                    }
                    if (!urls.isEmpty()) {
                        return urls.toArray(new URL[0]);
                    }
                }
            }
        }
        return UnitTestForSourceQuery.findUnitTests(root);
    }

    @NbBundle.Messages({
        "MSG_MicronautEndpointTestClass=Micronaut endpoint test class {0}\n"
    })
    private static Object generate(String location, String fqn, String origin) {
        try {
            java.net.URI locationURI = java.net.URI.create(location);
            int idx = fqn.lastIndexOf('/');
            String folderName = idx < 0 ? EMPTY : fqn.substring(0, idx);
            locationURI = locationURI.resolve(folderName);
            File file = BaseUtilities.toFile(locationURI);
            if (file != null && !file.exists()) {
                file.mkdirs();
            }
            FileObject folder = URLMapper.findFileObject(locationURI.toURL());
            if (folder == null) {
                return null;
            }
            String fileName = idx < 0 ? fqn : fqn.substring(idx + 1);
            FileObject fo = GenerationUtils.createClass(folder, fileName.substring(0, fileName.length() - 5), Bundle.MSG_MicronautEndpointTestClass(fileName));
            if (fo != null) {
                JavaSource js = JavaSource.forFileObject(fo);
                if (js != null) {
                    return Utils.modify2Edit(js, copy -> {
                        copy.toPhase(JavaSource.Phase.RESOLVED);
                        Tree origTree = copy.getCompilationUnit().getTypeDecls().get(0);
                        if (origTree.getKind() == Tree.Kind.CLASS) {
                            GenerationUtils gu = GenerationUtils.newInstance(copy);
                            ClassTree cls = gu.addAnnotation((ClassTree) origTree, gu.createAnnotation(MICRONAUT_TEST_ANNOTATION_NAME));
                            List<Tree> members = new ArrayList<>();
                            members.add(createClientField(copy));
                            TypeElement te = copy.getElements().getTypeElement(origin);
                            if (te != null) {
                                AnnotationMirror controllerAnn = Utils.getAnnotation(te.getAnnotationMirrors(), CONTROLLER_ANNOTATION_NAME);
                                String path = "";
                                if (controllerAnn != null) {
                                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : controllerAnn.getElementValues().entrySet()) {
                                        if ("value".contentEquals(entry.getKey().getSimpleName())) {
                                            path = (String) entry.getValue().getValue();
                                        }
                                    }
                                }
                                Set<Element> toImport = new HashSet<>();
                                for (ExecutableElement ee : ElementFilter.methodsIn(te.getEnclosedElements())) {
                                    MicronautSymbolFinder.MthIterator it = new MicronautSymbolFinder.MthIterator(ee, copy.getElements(), copy.getTypes());
                                    while(it.hasNext()) {
                                        ExecutableElement mth = it.next();
                                        for (AnnotationMirror ann : mth.getAnnotationMirrors()) {
                                            String method = getEndpointMethod((TypeElement) ann.getAnnotationType().asElement());
                                            if (method != null) {
                                                List<String> ids = new ArrayList<>();
                                                Map<? extends ExecutableElement, ? extends AnnotationValue> values = ann.getElementValues();
                                                if (values.isEmpty()) {
                                                    ids.add("/");
                                                } else {
                                                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                                                        if ("value".contentEquals(entry.getKey().getSimpleName()) || "uri".contentEquals(entry.getKey().getSimpleName())) {
                                                            ids.add((String) entry.getValue().getValue());
                                                        } else if ("uris".contentEquals(entry.getKey().getSimpleName())) {
                                                            for (AnnotationValue av : (List<AnnotationValue>) entry.getValue().getValue()) {
                                                                ids.add((String) av.getValue());
                                                            }
                                                        }
                                                    }
                                                }
                                                if (!ids.isEmpty()) {
                                                    members.add(createTestMethodFor(copy, ee, method, path, ids, toImport));
                                                }
                                            }
                                        }
                                    }
                                }
                                copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), toImport));
                            }
                            copy.rewrite(origTree, GeneratorUtilities.get(copy).insertClassMembers(cls, members));
                        }
                    });
                }
            }
        } catch (IOException | IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static VariableTree createClientField(WorkingCopy copy) {
        GenerationUtils gu = GenerationUtils.newInstance(copy);
        TreeMaker tm = copy.getTreeMaker();
        List<? extends AnnotationTree> anns = List.of(gu.createAnnotation(JAKARTA_INJECT_NAME),
                gu.createAnnotation(MICRONAUT_HTTP_CLIENT_ANNOTATION_NAME, List.of(tm.Literal("/")))
        );
        return tm.Variable(tm.Modifiers(Set.of(Modifier.PRIVATE), anns), "client", tm.Type(MICRONAUT_HTTP_CLIENT_NAME), null);
    }

    private static MethodTree createTestMethodFor(WorkingCopy copy, ExecutableElement ee, String method, String path, List<String> ids, Set<Element> toImport) {
        TreeMaker tm = copy.getTreeMaker();
        toImport.add(copy.getElements().getTypeElement(MICRONAUT_HTTP_REQUEST_NAME));
        Set<String> assertionMethods = new HashSet<>();
        String[] info = getInfo(copy, ee, toImport);
        Map<String, VariableElement> bodyParams = getBodyParams(ee);
        List<? extends AnnotationTree> anns = List.of(GenerationUtils.newInstance(copy).createAnnotation(JUPITER_TEST_ANNOTATION_NAME));
        Map<String, String> usedPathNames = new LinkedHashMap<>();
        Set<String> usedBodyNames = new HashSet<>();
        StringBuilder body = new StringBuilder("{");
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            Map<String, TypeMirror> pathParams = new LinkedHashMap<>();
            String processedPath = processPathParams(ee, path + id, pathParams);
            for (Map.Entry<String, TypeMirror> entry : pathParams.entrySet()) {
                String name = entry.getKey();
                TypeMirror value = entry.getValue();
                if (usedPathNames.put(name, name) == null) {
                    if (value != null) {
                        if (value.getKind() == TypeKind.DECLARED) {
                            toImport.add(((DeclaredType) value).asElement());
                        }
                        body.append(typeName(copy, entry.getValue())).append(' ');
                    } else {
                        body.append("Object ");
                    }
                }
                body.append(name).append(" = ").append(defaultValue(value)).append(";\n");
            }
            StringBuilder sb = new StringBuilder();
            if (bodyParams != null) {
                for (Map.Entry<String, VariableElement> entry : bodyParams.entrySet()) {
                    String name = entry.getKey();
                    VariableElement value = entry.getValue();
                    String simpleName = value.getSimpleName().toString();
                    if (EMPTY.equals(name)) {
                        sb.append(',').append(simpleName);
                    } else {
                        if (sb.isEmpty()) {
                            sb.append(",Map.of(");
                        } else {
                            sb.append(',');
                        }
                        sb.append('"').append(name).append("\",").append(simpleName);
                    }
                    if (usedBodyNames.add(simpleName)) {
                        body.append(typeName(copy, value.asType())).append(' ');
                    }
                    body.append(simpleName).append(" = ").append(defaultValue(value.asType())).append(";\n");
                }
                if (sb.isEmpty()) {
                    sb.append(",null");
                } else {
                    toImport.add(copy.getElements().getTypeElement(MAP_TYPE_NAME));
                }
            }
            if (i == 0) {
                body.append(info[1]).append(' ');
            }
            body.append(info[2]).append("=client.toBlocking().").append(info[0]).append('(')
                    .append("HttpRequest.").append(method).append('(').append(processedPath).append(sb).append(')').append(info[3]).append(");");
            if (info[1].startsWith("List<") || info[1].startsWith("Optional<")) {
                body.append("assertFalse(").append(info[2]).append(".isEmpty());\n");
                assertionMethods.add("assertFalse");
            } else if (info[1].startsWith("HttpResponse<")) {
                body.append("assertEquals(HttpStatus.");
                switch (method) {
                    case POST:
                        body.append("CREATED");
                        break;
                    case PUT:
                    case DELETE:
                        body.append("NO_CONTENT");
                        break;
                    default:
                        body.append("OK");
                }
                body.append(',').append(info[2]).append(".getStatus());\n");
                assertionMethods.add("assertEquals");
                toImport.add(copy.getElements().getTypeElement(MICRONAUT_HTTP_STATUS_NAME));
            } else {
                body.append("assertNotNull(").append(info[2]).append(");\n");
                assertionMethods.add("assertNotNull");
            }
        }
        body.append("\n// TODO review the generated test code and remove the default call to fail.\n");
        body.append("fail(\"The test case is a prototype.\");");
        assertionMethods.add("fail");
        TypeElement ae = copy.getElements().getTypeElement(JUPITER_ASSERTIONS_NAME);
        for (ExecutableElement mth : ElementFilter.methodsIn(ae.getEnclosedElements())) {
            if (assertionMethods.remove(mth.getSimpleName().toString())) {
                toImport.add(mth);
            }
        }
        body.append('}');
        String joined = usedPathNames.keySet().stream().map(name -> varName(name, true)).collect(Collectors.joining("And"));
        String methodName = joined.isEmpty() ? ee.getSimpleName().toString() : ee.getSimpleName().toString() + "By" + joined;
        return tm.Method(tm.Modifiers(Set.of(Modifier.PUBLIC), anns), methodName, tm.Type(copy.getTypes().getNoType(TypeKind.VOID)), List.of(), List.of(), List.of(), body.toString(), null);
    }

    private static Map<String, VariableElement> getBodyParams(ExecutableElement ee) {
        Map<String, VariableElement> bodyParams = new LinkedHashMap<>();
        for (VariableElement ve : ee.getParameters()) {
            AnnotationMirror am = Utils.getAnnotation(ve.getAnnotationMirrors(), MICRONAUT_BODY_ANNOTATION_NAME);
            if (am != null) {
                String value = EMPTY;
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                    if ("value".contentEquals(entry.getKey().getSimpleName())) {
                        value = (String) entry.getValue().getValue();
                    }
                }
                if (value.isEmpty()) {
                    return Collections.singletonMap(EMPTY, ve);
                } else {
                    bodyParams.put(value, ve);
                }
            }
        }
        return bodyParams.isEmpty() ? null : bodyParams;
    }

    private static String processPathParams(ExecutableElement ee, String path, Map<String, TypeMirror> pathParams) {
        Matcher matcher = Pattern.compile("\\{(.*)}").matcher(path);
        StringBuilder sb = new StringBuilder("\"");
        int idx = 0;
        Stream<? extends VariableElement> parameters = ee.getParameters().stream();
        while (matcher.find(idx)) {
            String name = matcher.group(1);
            if (!pathParams.containsKey(name)) {
                Optional<? extends VariableElement> param = parameters.filter(p -> name.contentEquals(p.getSimpleName())).findFirst();
                pathParams.put(name, param.isPresent() ? param.get().asType() : null);
            }
            sb.append(path.substring(idx, matcher.start())).append("\"+").append(name);
            idx = matcher.end();
            if (idx < path.length()) {
                sb.append("+\"");
            }
        }
        if (idx < path.length()) {
            sb.append(path.substring(idx)).append('"');
        }
        return sb.toString();
    }

    private static String[] getInfo(CompilationInfo info, ExecutableElement ee, Set<Element> toImport) {
        TypeMirror type = ee.getReturnType();
        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declType = (DeclaredType) type;
            TypeElement te = (TypeElement) declType.asElement();
            switch (te.getQualifiedName().toString()) {
                case LIST_TYPE_NAME:
                    TypeMirror typeArg = declType.getTypeArguments().get(0);
                    if (typeArg.getKind() == TypeKind.DECLARED) {
                        TypeElement tae = (TypeElement) ((DeclaredType) typeArg).asElement();
                        if (Utils.getAnnotation(tae.getAnnotationMirrors(), MICRONAUT_SERDEABLE_ANNOTATION_NAME) != null) {
                            toImport.add(info.getElements().getTypeElement(LIST_TYPE_NAME));
                            toImport.add(info.getElements().getTypeElement(MICRONAUT_ARGUMENT_NAME));
                            toImport.add(tae);
                            String typeName = typeName(info, type);
                            String typeArgName = typeName(info, typeArg);
                            return new String[] {"retrieve", typeName, "list", ",Argument.listOf(" + typeArgName + ".class)"};
                        }
                    }
                    break;
                case OPTIONAL_TYPE_NAME:
                    typeArg = declType.getTypeArguments().get(0);
                    if (typeArg.getKind() == TypeKind.DECLARED) {
                        TypeElement tae = (TypeElement) ((DeclaredType) typeArg).asElement();
                        if (Utils.getAnnotation(tae.getAnnotationMirrors(), MICRONAUT_SERDEABLE_ANNOTATION_NAME) != null) {
                            toImport.add(info.getElements().getTypeElement(OPTIONAL_TYPE_NAME));
                            toImport.add(info.getElements().getTypeElement(MICRONAUT_ARGUMENT_NAME));
                            toImport.add(tae);
                            String typeName = typeName(info, type);
                            String typeArgName = typeName(info, typeArg);
                            return new String[] {"retrieve", typeName, varName(typeArgName, false), ",Argument.optionalOf(" + typeArgName + ".class)"};
                        }
                    }
                    break;
                default:
                    if (Utils.getAnnotation(te.getAnnotationMirrors(), MICRONAUT_SERDEABLE_ANNOTATION_NAME) != null) {
                        toImport.add(te);
                        String typeName = typeName(info, type);
                        return new String[] {"retrieve", typeName, varName(typeName, false), "," + typeName + ".class"};
                    }
            }
        }
        toImport.add(info.getElements().getTypeElement(MICRONAUT_HTTP_RESPONSE_NAME));
        return new String[] {"exchange", "HttpResponse<?>", "response", EMPTY};
    }

    private static String typeName(CompilationInfo info, TypeMirror tm) {
        if (tm.getKind() == TypeKind.TYPEVAR) {
            tm = ((TypeVariable) tm).getUpperBound();
        }
        return Utils.getTypeName(info, tm, false, false).toString();
    }

    private static String varName(String varTypeName, boolean upperCase) {
        StringBuilder sb = new StringBuilder(varTypeName);
        char firstChar = sb.charAt(0);
        sb.setCharAt(0, upperCase ? Character.toUpperCase(firstChar) : Character.toLowerCase(firstChar));
        return sb.toString();
    }

    private static String defaultValue(TypeMirror tm) {
        if (tm != null) {
            switch(tm.getKind()) {
                case BOOLEAN:
                    return "false";
                case BYTE:
                case CHAR:
                case DOUBLE:
                case FLOAT:
                case INT:
                case LONG:
                case SHORT:
                    return "0";
            }
        }
        return "null";
    }
}
