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
package org.netbeans.modules.micronaut.hyperlink;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
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
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        int[] span = new int[2];
        MicronautConfigUtilities.Usage usage = resolve(doc, offset, span);
        return usage != null ? span : null;
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        MicronautConfigUtilities.Usage usage = resolve(doc, offset, null);
        if (usage == null || !UiUtils.open(usage.getFileObject(), usage.getStartOffset())) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        MicronautConfigUtilities.Usage usage = resolve(doc, offset, null);
        return usage != null ? usage.getText() : null;
    }

    private static MicronautConfigUtilities.Usage resolve(Document doc, int offset, int[] span) {
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
                        if (!configFiles.isEmpty()) {
                            JavaSource source = JavaSource.forDocument(doc);
                            if (source != null) {
                                try {
                                    String propertyName = getPropertyName(doc, offset, span);
                                    ConfigurationMetadataProperty property = MicronautConfigProperties.getProperties(project).get(propertyName);
                                    if (property != null) {
                                        List<MicronautConfigUtilities.Usage> usages = new ArrayList<>();
                                        for (FileObject configFile : configFiles) {
                                            MicronautConfigUtilities.collectUsages(configFile, propertyName, usage -> {
                                                usages.add(usage);
                                            });
                                        }
                                        if (!usages.isEmpty()) {
                                            return usages.get(0);
                                        }
                                    }
                                } catch (Exception ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String getPropertyName(Document doc, int offset, int[] span) throws IOException {
        String[] ret = new String[1];
        JavaSource source = JavaSource.forDocument(doc);
        if (source != null) {
            source.runUserActionTask(controller -> {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TreeUtilities tu = controller.getTreeUtilities();
                SourcePositions sp = controller.getTrees().getSourcePositions();
                TreePath path = tu.pathFor(offset);
                LiteralTree literal = null;
                AnnotationTree annotation = null;
                if (path.getLeaf().getKind() == Tree.Kind.STRING_LITERAL) {
                    literal = (LiteralTree) path.getLeaf();
                    TreePath annPath = tu.getPathElementOfKind(Tree.Kind.ANNOTATION, path);
                    annotation = annPath != null ? (AnnotationTree) annPath.getLeaf() : null;
                } else if (path.getLeaf().getKind() == Tree.Kind.ANNOTATION) {
                    annotation = (AnnotationTree) path.getLeaf();
                    for (ExpressionTree arg : annotation.getArguments()) {
                        if (arg.getKind() == Tree.Kind.ASSIGNMENT) {
                            ExpressionTree expression = ((AssignmentTree) arg).getExpression();
                            if (expression.getKind() == Tree.Kind.STRING_LITERAL && sp.getStartPosition(path.getCompilationUnit(), expression) < offset && sp.getEndPosition(path.getCompilationUnit(), expression) >= offset) {
                                literal = (LiteralTree) expression;
                            }
                        }
                    }
                }
                if (literal != null && annotation != null) {
                    Element el = controller.getTrees().getElement(new TreePath(path, annotation.getAnnotationType()));
                    if (el != null && el.asType().getKind() == TypeKind.DECLARED) {
                        Name name = ((TypeElement)((DeclaredType)el.asType()).asElement()).getQualifiedName();
                        if ("io.micronaut.context.annotation.Property".contentEquals(name)) {
                            ret[0] = (String) literal.getValue();
                            if (span != null) {
                                span[0] = (int) sp.getStartPosition(path.getCompilationUnit(), literal) + 1;
                                span[1] = (int) sp.getEndPosition(path.getCompilationUnit(), literal) - 1;
                            }
                        } else if ("io.micronaut.context.annotation.Value".contentEquals(name)) {
                            String value = (String) literal.getValue();
                            Matcher matcher = INJECT.matcher(value);
                            if (matcher.find()) {
                                ret[0] = matcher.group(1);
                                if (span != null) {
                                    span[0] = (int) sp.getStartPosition(path.getCompilationUnit(), literal) + matcher.start(1) + 1;
                                    span[1] = (int) sp.getStartPosition(path.getCompilationUnit(), literal) + matcher.end(1) + 1;
                                }
                            }
                        }
                    }
                }
            }, true);
        }
        return ret[0];
    }

    @MimeRegistration(mimeType = "text/x-java", service = HyperlinkLocationProvider.class)
    public static class LocationProvider implements HyperlinkLocationProvider {

        @Override
        public CompletableFuture<HyperlinkLocation> getHyperlinkLocation(Document doc, int offset) {
            MicronautConfigUtilities.Usage usage = resolve(doc, offset, null);
            return CompletableFuture.completedFuture(usage != null
                    ? HyperlinkLocationProvider.createHyperlinkLocation(usage.getFileObject(), usage.getStartOffset(), usage.getEndOffset())
                    : null);
        }
    }
}
