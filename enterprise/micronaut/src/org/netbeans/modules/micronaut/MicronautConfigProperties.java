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
package org.netbeans.modules.micronaut;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TypeUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;

/**
 *
 * @author Dusan Balek
 */
public final class MicronautConfigProperties {

    private static final String CONFIG_METADATA_JSON = "META-INF/spring-configuration-metadata.json";
    private static final ElementHandle<TypeElement> PROPERTY_HANDLE = ElementHandle.createTypeElementHandle(ElementKind.ANNOTATION_TYPE, "io.micronaut.context.annotation.Property");

    private MicronautConfigProperties() {
    }

    public static boolean hasConfigMetadata(Project project) {
        ClassPath cp = getExecuteClasspath(project);
        return cp != null && !cp.findAllResources(CONFIG_METADATA_JSON).isEmpty();
    }

    public static Map<String, ConfigurationMetadataProperty> getProperties(Project project) {
        Map<String, ConfigurationMetadataProperty> props = new LinkedHashMap<>();
        ClassPath cp = getExecuteClasspath(project);
        if (cp != null) {
            for (FileObject fo : cp.findAllResources(CONFIG_METADATA_JSON)) {
                try {
                    ConfigurationMetadataRepository repository = ConfigurationMetadataRepositoryJsonBuilder.create().withJsonResource(fo.getInputStream()).build();
                    props.putAll(repository.getAllProperties());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            String customRepository = getCustomRepository(project);
            if (customRepository != null) {
                try {
                    InputStream stream = new ByteArrayInputStream(customRepository.getBytes(StandardCharsets.UTF_8));
                    ConfigurationMetadataRepository repository = ConfigurationMetadataRepositoryJsonBuilder.create().withJsonResource(stream).build();
                    props.putAll(repository.getAllProperties());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return props;
    }

    public static Map<String, ConfigurationMetadataGroup> getGroups(Project project) {
        Map<String, ConfigurationMetadataGroup> groups = new LinkedHashMap<>();
        ClassPath cp = getExecuteClasspath(project);
        if (cp != null) {
            for (FileObject fo : cp.findAllResources(CONFIG_METADATA_JSON)) {
                try {
                    ConfigurationMetadataRepository repository = ConfigurationMetadataRepositoryJsonBuilder.create().withJsonResource(fo.getInputStream()).build();
                    groups.putAll(repository.getAllGroups());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            String customRepository = getCustomRepository(project);
            if (customRepository != null) {
                try {
                    InputStream stream = new ByteArrayInputStream(customRepository.getBytes(StandardCharsets.UTF_8));
                    ConfigurationMetadataRepository repository = ConfigurationMetadataRepositoryJsonBuilder.create().withJsonResource(stream).build();
                    groups.putAll(repository.getAllGroups());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return groups;
    }

    private static String getCustomRepository(Project project) {
        AtomicReference<String> ret = new AtomicReference<>();
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < srcGroups.length; i++) {
            SourceGroup srcGroup = srcGroups[i];
            ClassIndex ci = ClasspathInfo.create(srcGroup.getRootFolder()).getClassIndex();
            Set<FileObject> resources = ci.getResources(PROPERTY_HANDLE,
                    EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE));
            try {
                ParserManager.parse(resources.stream().map(resource -> Source.create(resource)).collect(Collectors.toList()), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController cc = CompilationController.get(resultIterator.getParserResult());
                        cc.toPhase(JavaSource.Phase.RESOLVED);
                        Trees trees = cc.getTrees();
                        TypeElement te = PROPERTY_HANDLE.resolve(cc);
                        new TreePathScanner<Void, Void>() {
                            @Override
                            public Void visitAnnotation(AnnotationTree node, Void p) {
                                if (te == trees.getElement(new TreePath(getCurrentPath(), node.getAnnotationType()))) {
                                    for (ExpressionTree argument : node.getArguments()) {
                                        if (argument.getKind() == Tree.Kind.ASSIGNMENT) {
                                            ExpressionTree variable = ((AssignmentTree) argument).getVariable();
                                            ExpressionTree expression = ((AssignmentTree) argument).getExpression();
                                            if (expression.getKind() == Tree.Kind.STRING_LITERAL && variable.getKind() == Tree.Kind.IDENTIFIER && "name".contentEquals(((IdentifierTree) variable).getName())) {
                                                try {
                                                    String value = (String) ((LiteralTree) expression).getValue();
                                                    JSONObject group = new JSONObject();
                                                    JSONObject property = new JSONObject();
                                                    group.put("name", value);
                                                    property.put("name", value);
                                                    final TreePath grandParentPath = getCurrentPath().getParentPath().getParentPath();
                                                    if (grandParentPath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                                                        Element el = trees.getElement(grandParentPath);
                                                        if (el != null && el.getKind().isField()) {
                                                            String typeName = cc.getTypeUtilities().getTypeName(el.asType(), TypeUtilities.TypeNameOptions.PRINT_FQN).toString();
                                                            String sourceTypeName = cc.getElementUtilities().getElementName(el.getEnclosingElement(), true).toString();
                                                            property.put("type", typeName);
                                                            property.put("sourceType", sourceTypeName);
                                                            group.put("type", sourceTypeName);
                                                        }
                                                    }
                                                    JSONArray groups = new JSONArray();
                                                    groups.put(group);
                                                    JSONArray properties = new JSONArray();
                                                    properties.put(property);
                                                    JSONObject data = new JSONObject();
                                                    data.put("groups", groups);
                                                    data.put("properties", properties);
                                                    ret.set(data.toString());
                                                } catch (JSONException ex) {
                                                    Exceptions.printStackTrace(ex);
                                                }
                                            }
                                        }
                                    }
                                }
                                return super.visitAnnotation(node, p);
                            }
                        }.scan(new TreePath(cc.getCompilationUnit()), null);
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return ret.get();
    }

    private static ClassPath getExecuteClasspath(Project project) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups.length > 0) {
            return ClassPath.getClassPath(srcGroups[0].getRootFolder(), ClassPath.EXECUTE);
        }
        return null;
    }
}
