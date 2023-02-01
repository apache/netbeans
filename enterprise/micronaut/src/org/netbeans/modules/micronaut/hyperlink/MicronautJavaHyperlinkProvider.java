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
package org.netbeans.modules.micronaut.hyperlink;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lsp.HyperlinkLocation;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.micronaut.MicronautConfigProperties;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.lsp.HyperlinkLocationProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 * CURRENTLY NOT ACTIVE - @MimeRegistration DISABLED to work around
 * <a href="https://github.com/apache/netbeans/issues/3913">GITHUB-3913</a>
 *
 * @author Dusan Balek
 */
//@MimeRegistration(mimeType = "text/x-java", service = HyperlinkProviderExt.class, position = 1250)
public class MicronautJavaHyperlinkProvider implements HyperlinkProviderExt {

    private static final Pattern INJECT = Pattern.compile("\\$\\{(\\S+)(:\\S*)?}");

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return resolve(doc, offset) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        List<PropertyDesc> propertyNames = (List<PropertyDesc>) doc.getProperty(ProjectDesc.class);

        for (PropertyDesc desc : propertyNames) {
            if (desc.start.getOffset() <= offset && offset <= desc.end.getOffset()) {
                return new int[] {
                    desc.start.getOffset(),
                    desc.end.getOffset()
                };
            }
        }

        return null;
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        MicronautConfigUtilities.Usage usage = resolve(doc, offset);
        if (usage == null || !UiUtils.open(usage.getFileObject(), usage.getStartOffset())) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        MicronautConfigUtilities.Usage usage = resolve(doc, offset);
        return usage != null ? usage.getText() : null;
    }

    private static MicronautConfigUtilities.Usage resolve(Document doc, int offset) {
        ProjectDesc configFiles = getConfigFiles(doc);
        if (configFiles != null) {
            JavaSource source = JavaSource.forDocument(doc);
            if (source != null) {
                try {
                    Function<CompilationInfo, TreePath> info2StartPath = info -> {
                        TreeUtilities tu = info.getTreeUtilities();
                        TreePath path = tu.pathFor(offset);
                        if (path.getLeaf().getKind() == Tree.Kind.STRING_LITERAL) {
                            return tu.getPathElementOfKind(Tree.Kind.ANNOTATION, path);
                        } else if (path.getLeaf().getKind() == Tree.Kind.ANNOTATION) {
                            return path;
                        } else {
                            return null;
                        }
                    };
                    List<PropertyDesc> propertyNames = getPropertyNames(doc, info2StartPath);
                    for (PropertyDesc desc : propertyNames) {
                        if (desc.start.getOffset() <= offset && offset <= desc.end.getOffset()) {
                            ConfigurationMetadataProperty property = MicronautConfigProperties.getProperties(configFiles.project).get(desc.name);
                            if (property != null) {
                                List<MicronautConfigUtilities.Usage> usages = new ArrayList<>();
                                for (FileObject configFile : configFiles.configFiles) {
                                    MicronautConfigUtilities.collectUsages(configFile, desc.name, usage -> {
                                        usages.add(usage);
                                    });
                                }
                                if (!usages.isEmpty()) {
                                    return usages.get(0);
                                }
                            }
                            return null;
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    private static ProjectDesc getConfigFiles(Document doc) {
        FileObject fo = EditorDocumentUtils.getFileObject(doc);
        if (fo != null) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                if (MicronautConfigProperties.hasConfigMetadata(project)) {
                    SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
                    for (SourceGroup sourceGroup : sourceGroups) {
                        FileObject rootFolder = sourceGroup.getRootFolder();
                        List<FileObject> configFiles = new ArrayList<>();
                        for (FileObject chldFo : rootFolder.getChildren()) {
                            if (MicronautConfigUtilities.isMicronautConfigFile(chldFo)) {
                                configFiles.add(chldFo);
                            }
                        }
                        return new ProjectDesc(project, configFiles);
                    }
                }
            }
        }
        return null;
    }

    private static List<PropertyDesc> getPropertyNames(Document doc, Function<CompilationInfo, TreePath> info2StartPath) throws IOException {
        AtomicReference<List<PropertyDesc>> result = new AtomicReference<>(Collections.emptyList());
        JavaSource source = JavaSource.forDocument(doc);
        if (source != null) {
            source.runUserActionTask(controller -> {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                result.set(getPropertyNames(doc, controller, info2StartPath, new AtomicBoolean()));
            }, true);
        }
        return result.get();
    }

    private static List<PropertyDesc> getPropertyNames(Document doc, CompilationInfo info, Function<CompilationInfo, TreePath> info2StartPath, AtomicBoolean cancel) throws IOException {
        List<PropertyDesc> result = new ArrayList<>();
        SourcePositions sp = info.getTrees().getSourcePositions();
        new CancellableTreePathScanner<Void, Void>(cancel) {
            @Override
            public Void visitAnnotation(AnnotationTree tree, Void v) {
                Element el = info.getTrees().getElement(new TreePath(getCurrentPath(), tree.getAnnotationType()));
                if (el != null && el.asType().getKind() == TypeKind.DECLARED) {
                    Name name = ((TypeElement)((DeclaredType)el.asType()).asElement()).getQualifiedName();
                    LiteralTree literal;
                    if ("io.micronaut.context.annotation.Property".contentEquals(name) &&
                        (literal = getAnnotationValue(tree)) != null) {
                        try {
                            result.add(new PropertyDesc((String) literal.getValue(),
                                       doc.createPosition((int) sp.getStartPosition(info.getCompilationUnit(), literal) + 1),
                                       doc.createPosition((int) sp.getEndPosition(info.getCompilationUnit(), literal) - 1)));
                        } catch (BadLocationException ex) {
                            cancel.set(true);
                        }
                    } else if ("io.micronaut.context.annotation.Value".contentEquals(name) &&
                        (literal = getAnnotationValue(tree)) != null) {
                        String value = (String) literal.getValue();
                        Matcher matcher = INJECT.matcher(value);
                        if (matcher.find()) {
                            try {
                                result.add(new PropertyDesc(value.substring(2, value.length() - 1),
                                           doc.createPosition((int) sp.getStartPosition(info.getCompilationUnit(), literal) + matcher.start(1) + 1),
                                           doc.createPosition((int) sp.getStartPosition(info.getCompilationUnit(), literal) + matcher.end(1) + 1)));
                            } catch (BadLocationException ex) {
                                cancel.set(true);
                            }
                        }
                    }
                }
                return null;
            }
        }.scan(info2StartPath.apply(info), null);
        return result;
    }

    private static LiteralTree getAnnotationValue(AnnotationTree annotation) {
        for (ExpressionTree arg : annotation.getArguments()) {
            if (arg.getKind() == Tree.Kind.ASSIGNMENT) {
                AssignmentTree at = (AssignmentTree) arg;
                ExpressionTree expression = at.getExpression();
                if (expression.getKind() == Tree.Kind.STRING_LITERAL &&
                    ((IdentifierTree) at.getVariable()).getName().contentEquals("value")) {
                    return (LiteralTree) expression;
                }
            }
        }

        return null;
    }

    private static final class ProjectDesc {
        public final Project project;
        public final List<FileObject> configFiles;

        public ProjectDesc(Project project, List<FileObject> configFiles) {
            this.project = project;
            this.configFiles = configFiles;
        }

    }

    private static final class PropertyDesc {
        public final String name;
        public final Position start;
        public final Position end;

        public PropertyDesc(String name, Position start, Position end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

    }

    @MimeRegistration(mimeType = "text/x-java", service = HyperlinkLocationProvider.class)
    public static class LocationProvider implements HyperlinkLocationProvider {

        @Override
        public CompletableFuture<HyperlinkLocation> getHyperlinkLocation(Document doc, int offset) {
            MicronautConfigUtilities.Usage usage = resolve(doc, offset);
            return CompletableFuture.completedFuture(usage != null
                    ? HyperlinkLocationProvider.createHyperlinkLocation(usage.getFileObject(), usage.getStartOffset(), usage.getEndOffset())
                    : null);
        }
    }

    public static final class SpanTask extends JavaParserResultTask {

        private final AtomicBoolean cancel = new AtomicBoolean();

        public SpanTask() {
            super(Phase.ELEMENTS_RESOLVED, TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        @Override
        public int getPriority() {
            return 100;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
            cancel.set(true);
        }

        @Override
        public void run(Result result, SchedulerEvent event) {
            cancel.set(false);

            CompilationInfo info = CompilationInfo.get(result);

            if (info == null) {
                return ;
            }

            Document doc = result.getSnapshot().getSource().getDocument(false);

            if (doc == null) {
                return ;
            }

            ProjectDesc configFiles = getConfigFiles(doc);

            if (configFiles == null) {
                return ;
            }

            try {
                List<PropertyDesc> propertyNames = getPropertyNames(doc, info, i -> new TreePath(i.getCompilationUnit()), cancel);
                if (cancel.get()) {
                    return ;
                }
                doc.putProperty(ProjectDesc.class, propertyNames);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @MimeRegistration(mimeType = "text/x-java", service = TaskFactory.class)
        public static class FactoryImpl extends TaskFactory {
            @Override
            public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
                return Collections.singleton(new SpanTask());
            }
        }
    }

}
