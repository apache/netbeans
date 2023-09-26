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
package org.netbeans.modules.micronaut.refactor;

import java.io.IOException;
import java.util.ArrayList;
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
import javax.swing.text.Position;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.micronaut.MicronautConfigProperties;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.modules.micronaut.completion.MicronautConfigCompletionProvider;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataSource;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class MicronautRefactoringFactory implements RefactoringPluginFactory {

    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup lkp = refactoring.getRefactoringSource();
        TreePathHandle handle = lkp.lookup(TreePathHandle.class);
        if (refactoring instanceof WhereUsedQuery && ((WhereUsedQuery) refactoring).getBooleanValue(WhereUsedQuery.FIND_REFERENCES)) {
            if (handle != null) {
                return new MicronautWhereUsedRefactoringPlugin((WhereUsedQuery) refactoring);
            }
        }
        return null;
    }

    private static class MicronautWhereUsedRefactoringPlugin implements RefactoringPlugin {

        private final WhereUsedQuery refactoring;

        public MicronautWhereUsedRefactoringPlugin(WhereUsedQuery refactoring) {
            this.refactoring = refactoring;
        }

        @Override
        public Problem preCheck() {
            return null;
        }

        @Override
        public Problem checkParameters() {
            return null;
        }

        @Override
        public Problem fastCheckParameters() {
            return null;
        }

        @Override
        public void cancelRequest() {
        }

        @Override
        public Problem prepare(RefactoringElementsBag refactoringElements) {
            Scope scope = refactoring.getContext().lookup(Scope.class);
            if (scope != null) {
                Set<Project> projects = new HashSet<>();
                for (FileObject sourceRoot : scope.getSourceRoots()) {
                    Project p = FileOwnerQuery.getOwner(sourceRoot);
                    if (p != null) {
                        projects.add(p);
                    }
                }
                try {
                    Info info = null;
                    for (Project project : projects) {
                        if (MicronautConfigProperties.hasConfigMetadata(project)) {
                            SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
                            for (SourceGroup sourceGroup : sourceGroups) {
                                FileObject rootFolder = sourceGroup.getRootFolder();
                                List<FileObject> configFiles = new ArrayList<>();
                                for (FileObject fo : rootFolder.getChildren()) {
                                    if (MicronautConfigUtilities.isMicronautConfigFile(fo)) {
                                        configFiles.add(fo);
                                    }
                                }
                                if (!configFiles.isEmpty()) {
                                    if (info == null) {
                                        info = getInfo();
                                    }
                                    if (info.className != null && info.propertyName != null) {
                                        for (ConfigurationMetadataGroup group : MicronautConfigProperties.getGroups(project).values()) {
                                            ConfigurationMetadataSource source = group.getSources().get(info.className);
                                            if (source != null) {
                                                for (ConfigurationMetadataProperty property : source.getProperties().values()) {
                                                    String name = property.getName().replace("-", "");
                                                    if (name.equalsIgnoreCase(info.propertyName)) {
                                                        for (FileObject configFile : configFiles) {
                                                            MicronautConfigUtilities.collectUsages(configFile, property.getId(), usage -> {
                                                                refactoringElements.add(refactoring, new WhereUsedRefactoringElement(usage.getFileObject(), usage.getStartOffset(), usage.getEndOffset(), usage.getText()));
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException ioe) {
                }
            }
            return null;
        }

        private Info getInfo() throws IOException {
            final TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
            final Info info = new Info();
            JavaSource source = JavaSource.forFileObject(handle.getFileObject());
            if (source != null) {
                source.runUserActionTask(controller -> {
                    controller.toPhase(JavaSource.Phase.RESOLVED);
                    Element el = handle.resolveElement(controller);
                    if (el != null) {
                        if (el.getKind() == ElementKind.METHOD) {
                            if (el.getModifiers().contains(Modifier.PUBLIC) && el.getSimpleName().toString().startsWith("set")) {
                                info.propertyName = el.getSimpleName().toString().substring(3);
                                info.className = ((TypeElement) el.getEnclosingElement()).getQualifiedName().toString();
                            }
                        } else if (el.getKind().isField()) {
                            TypeElement typeElement = controller.getElements().getTypeElement("io.micronaut.context.annotation.Property");
                            for (AnnotationMirror annotationMirror : el.getAnnotationMirrors()) {
                                if (typeElement == annotationMirror.getAnnotationType().asElement()) {
                                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                                        if ("name".contentEquals(entry.getKey().getSimpleName())) {
                                            info.propertyName = (String) entry.getValue().getValue();
                                            info.className = ((TypeElement) el.getEnclosingElement()).getQualifiedName().toString();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, true);
            }
            return info;
        }

        private static class Info {
            private String className;
            private String propertyName;
        }
    }

    public static final class WhereUsedRefactoringElement extends SimpleRefactoringElementImplementation {

        private final FileObject fileObject;
        private final int startOffset;
        private final int endOffset;
        private final String text;

        private WhereUsedRefactoringElement(FileObject fileObject, int startOffset, int endOffset, String text) {
            this.fileObject = fileObject;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getDisplayText() {
            StringBuilder sb = new StringBuilder();
            int idx = text.indexOf(':');
            if (idx < 0) {
                sb.append(text);
            } else {
                sb.append(MicronautConfigCompletionProvider.PROPERTY_NAME_COLOR).append("<b>");
                sb.append(text.substring(0, idx));
                sb.append("</b></font>");
                sb.append(text.substring(idx));
            }
            return sb.toString();
        }

        @Override
        public void performChange() {
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return fileObject;
        }

        @Override
        public PositionBounds getPosition() {
            try {
                DataObject dobj = DataObject.find(getParentFile());
                if (dobj != null) {
                    EditorCookie.Observable obs = dobj.getCookie(EditorCookie.Observable.class);
                    if (obs instanceof CloneableEditorSupport) {
                        CloneableEditorSupport supp = (CloneableEditorSupport)obs;
                        return new PositionBounds(supp.createPositionRef(startOffset, Position.Bias.Forward), supp.createPositionRef(Math.max(startOffset, endOffset), Position.Bias.Forward));
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
}
