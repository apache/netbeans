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
package org.netbeans.modules.micronaut.expression;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.micronaut.MicronautConfigProperties;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataSource;

/**
 *
 * @author Dusan Balek
 */
public class MicronautExpressionLanguageUtilities {

    private static final Pattern LINK_PATTERN = Pattern.compile("<a href='(\\*\\d+)'>(.*?)<\\/a>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public static <T> T resolve(Document doc, int offset, BiFunction<CompilationInfo, Element, T> withElement, BiFunction<ConfigurationMetadataProperty, ConfigurationMetadataSource, T> withProperty) {
        AtomicReference<T> ret = new AtomicReference<>();
        try {
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController cc = CompilationController.get(resultIterator.getParserResult(offset));
                    if (cc != null) {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TreePath treePath = cc.getTreeUtilities().pathFor(offset);
                        if (treePath.getLeaf().getKind() == Tree.Kind.STRING_LITERAL) {
                            int off = offset - (int) cc.getTrees().getSourcePositions().getStartPosition(treePath.getCompilationUnit(), treePath.getLeaf()) - 1;
                            Matcher matcher = MicronautExpressionLanguageParser.MEXP_PATTERN.matcher((String) ((LiteralTree) treePath.getLeaf()).getValue());
                            while (matcher.find() && matcher.groupCount() == 1) {
                                if (off >= matcher.start(1) && off <= matcher.end(1)) {
                                    MicronautExpressionLanguageParser parser = new MicronautExpressionLanguageParser(matcher.group(1));
                                    ExpressionTree tree = parser.parse();
                                    ExpressionTree.Path path = ExpressionTree.Path.get(tree, off - matcher.start(1));
                                    if (path != null) {
                                        Element el = path.getLeaf().getElement(EvaluationContext.get(cc, treePath));
                                        if (el != null) {
                                            ret.set(withElement.apply(cc, el));
                                        } else if (path.getLeaf().getKind() == ExpressionTree.Kind.STRING_LITERAL && path.getParentPath() != null
                                                && path.getParentPath().getLeaf().getKind() == ExpressionTree.Kind.ENVIRONMENT_ACCESS) {
                                            Project project = FileOwnerQuery.getOwner(cc.getFileObject());
                                            if (project != null) {
                                                String propertyName = (String) ((ExpressionTree.Literal) path.getLeaf()).getValue();
                                                List<ConfigurationMetadataSource> sources = new ArrayList<>();
                                                ConfigurationMetadataProperty property = MicronautConfigUtilities.getProperty(MicronautConfigProperties.getGroups(project), propertyName, sources);
                                                if (property != null) {
                                                    Optional<ConfigurationMetadataSource> source = sources.stream().filter(s -> s.getProperties().get(property.getId()) == property).findFirst();
                                                    ret.set(withProperty.apply(property, source.orElse(null)));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        } catch (ParseException pe) {
            Exceptions.printStackTrace(pe);
        }
        return ret.get();
    }

    @NbBundle.Messages({
        "LBL_More=..."
    })
    public static String getJavadocText(CompilationInfo info, Element e, boolean shorten, int timeoutInSeconds) {
        String text = null;
        try {
            AtomicBoolean cancel = new AtomicBoolean();
            ElementJavadoc javadoc = ElementJavadoc.create(info, e, () -> cancel.get());
            javadoc.getTextAsync();
            text = javadoc.getTextAsync() != null ? javadoc.getTextAsync().get(timeoutInSeconds, TimeUnit.SECONDS) : null;
            if (text != null) {
                text = resolveLinks(text, javadoc);
                if (shorten) {
                    int idx = 0;
                    for (int i = 0; i < 3 && idx >= 0; i++) {
                        idx = text.indexOf("<p>", idx + 1); //NOI18N
                    }
                    if (idx >= 0) {
                        text = text.substring(0, idx + 3);
                        text += Bundle.LBL_More();
                    }
                }
                int idx = text.indexOf("<p id=\"not-found\">"); //NOI18N
                if (idx >= 0) {
                    text = text.substring(0, idx);
                }
            }
            cancel.set(true);
        } catch (Exception ex) {}
        return text;
    }

    private static String resolveLinks(String content, ElementJavadoc doc) {
        Matcher matcher = LINK_PATTERN.matcher(content);
        String updatedContent = matcher.replaceAll(result -> {
            if (result.groupCount() == 2) {
                try {
                    ElementJavadoc link = doc.resolveLink(result.group(1));
                    URL url = link != null ? link.getURL() : null;
                    if (url != null) {
                        return "<a href='" + url.toString() + "'>" + result.group(2) + "</a>";
                    }
                } catch (Exception ex) {}
                return result.group(2);
            }
            return result.group();
        });
        return updatedContent;
    }

}
