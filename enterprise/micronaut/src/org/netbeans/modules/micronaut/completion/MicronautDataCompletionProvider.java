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
package org.netbeans.modules.micronaut.completion;

import java.awt.Color;
import java.io.CharConversionException;
import java.net.URL;
import java.util.Iterator;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionProvider.class, position = 250)
public final class MicronautDataCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        switch (queryType) {
            case COMPLETION_ALL_QUERY_TYPE:
            case COMPLETION_QUERY_TYPE:
                return new AsyncCompletionTask(new MicronautDataCompletionQuery(), component);
        }
        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static class MicronautDataCompletionQuery extends AsyncCompletionQuery {

        private static final String MICRONAUT_ICON = "org/netbeans/modules/micronaut/resources/micronaut.png";
        private static final String PACKAGE_ICON = "org/netbeans/modules/java/editor/resources/package.gif";
        private static final String CLASS_ICON = "org/netbeans/modules/editor/resources/completion/class_16.png";
        private static final String INTERFACE_ICON = "org/netbeans/modules/editor/resources/completion/interface.png";
        private static final String ENUM_ICON = "org/netbeans/modules/editor/resources/completion/enum.png";
        private static final String ANNOTATION_TYPE_ICON = "org/netbeans/modules/editor/resources/completion/annotation_type.png";
        private static final String RECORD_ICON = "org/netbeans/modules/editor/resources/completion/record.png";
        private static final String METHOD_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_16.png"; //NOI18N
        private static final String METHOD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_static_16.png";
        private static final String ATTRIBUTE_VALUE = "org/netbeans/modules/java/editor/resources/attribute_value_16.png"; // NOI18N
        private static final String PROPERTY = "org/netbeans/modules/beans/resources/propertyRO.gif";
        private static final String KEYWORD_COLOR = getHTMLColor(64, 64, 217);
        private static final String PACKAGE_COLOR = getHTMLColor(64, 150, 64);
        private static final String CLASS_COLOR = getHTMLColor(150, 64, 64);
        private static final String INTERFACE_COLOR = getHTMLColor(128, 128, 128);
        private static final String PARAMETER_NAME_COLOR = getHTMLColor(224, 160, 65);
        private static final String ATTRIBUTE_VALUE_COLOR = getHTMLColor(128, 128, 128);
        private static final String PROPERTY_COLOR = getHTMLColor(64, 198, 88);
        private static final String COLOR_END = "</font>";

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            MicronautDataCompletionTask task = new MicronautDataCompletionTask();
            resultSet.addAllItems(task.query(doc, caretOffset, new MicronautDataCompletionTask.ItemFactory<CompletionItem>() {
                @Override
                public CompletionItem createFinderMethodItem(String name, String returnType, int offset) {
                    CompletionUtilities.CompletionItemBuilder builder = CompletionUtilities.newCompletionItemBuilder(name)
                            .iconResource(MICRONAUT_ICON)
                            .leftHtmlText("<b>" + name + "</b>")
                            .sortPriority(10);
                    if (returnType != null) {
                        builder.onSelect(ctx -> {
                            final Document doc = ctx.getComponent().getDocument();
                            try {
                                doc.remove(offset, ctx.getComponent().getCaretPosition() - offset);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            String template = "${PAR#1 default=\"" + returnType + "\"} " + name + "${cursor completionInvoke}()";
                            CodeTemplateManager.get(doc).createTemporary(template).insert(ctx.getComponent());
                        });
                    } else {
                        builder.startOffset(offset);
                    }
                    return builder.build();
                }

                @Override
                public CompletionItem createFinderMethodNameItem(String prefix, String name, int offset) {
                    return CompletionUtilities.newCompletionItemBuilder(prefix + name)
                            .startOffset(offset)
                            .iconResource(MICRONAUT_ICON)
                            .leftHtmlText(prefix + "<b>" + name + "</b>")
                            .sortPriority(10)
                            .sortText(name)
                            .build();
                }

                @Override
                public CompletionItem createSQLItem(CompletionItem item) {
                    return item;
                }

                @Override
                public CompletionItem createKeywordItem(String name, int offset) {
                    return CompletionUtilities.newCompletionItemBuilder(name)
                            .startOffset(offset)
                            .iconResource(MICRONAUT_ICON)
                            .leftHtmlText(KEYWORD_COLOR + "<b>" + name + "</b>" + COLOR_END)
                            .sortPriority(200)
                            .build();
                }

                @Override
                public CompletionItem createBuiltInItem(String name, String parenPair, int offset) {
                    return CompletionUtilities.newCompletionItemBuilder(name)
                            .iconResource(MICRONAUT_ICON)
                            .leftHtmlText(name + parenPair.charAt(0) + "..." + parenPair.charAt(1))
                            .sortPriority(100)
                            .onSelect(ctx -> {
                                final Document doc = ctx.getComponent().getDocument();
                                try {
                                    doc.remove(offset, ctx.getComponent().getCaretPosition() - offset);
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                                String template = name + parenPair.charAt(0) + "${cursor completionInvoke}" + parenPair.charAt(1);
                                CodeTemplateManager.get(doc).createTemporary(template).insert(ctx.getComponent());
                            }).build();
                }

                @Override
                public CompletionItem createPackageItem(String name, int offset) {
                    return CompletionUtilities.newCompletionItemBuilder(name)
                            .iconResource(PACKAGE_ICON)
                            .leftHtmlText(PACKAGE_COLOR + name + COLOR_END)
                            .sortPriority(400)
                            .insertText(name + '.')
                            .startOffset(offset)
                            .build();
                }

                @Override
                public CompletionItem createBeanPropertyItem(String name, String typeName, int offset) {
                    return CompletionUtilities.newCompletionItemBuilder(name)
                            .iconResource(PROPERTY)
                            .leftHtmlText(PROPERTY_COLOR + "<b>" + name + "</b>" + COLOR_END)
                            .sortPriority(50)
                            .startOffset(offset)
                            .rightHtmlText(typeName)
                            .build();
                }

                @Override
                public CompletionItem createEnvPropertyItem(String name, String documentation, int anchorOffset, int offset) {
                    return CompletionUtilities.newCompletionItemBuilder(name)
                            .iconResource(ATTRIBUTE_VALUE)
                            .leftHtmlText(ATTRIBUTE_VALUE_COLOR + name + COLOR_END)
                            .sortPriority(30)
                            .startOffset(anchorOffset)
                            .documentationTask(() -> {
                                return documentation == null ? null : new CompletionTask() {
                                    private CompletionDocumentation cd = new CompletionDocumentation() {
                                        @Override
                                        public String getText() {
                                            return documentation;
                                        }
                                        @Override
                                        public URL getURL() {
                                            return null;
                                        }
                                        @Override
                                        public CompletionDocumentation resolveLink(String link) {
                                            return null;
                                        }
                                        @Override
                                        public Action getGotoSourceAction() {
                                            return null;
                                        }
                                    };
                                    @Override
                                    public void query(CompletionResultSet resultSet) {
                                        resultSet.setDocumentation(cd);
                                        resultSet.finish();
                                    }
                                    @Override
                                    public void refresh(CompletionResultSet resultSet) {
                                        resultSet.setDocumentation(cd);
                                        resultSet.finish();
                                    }
                                    @Override
                                    public void cancel() {
                                    }
                                };
                            })
                            .build();
                }

                @Override
                public CompletionItem createJavaElementItem(CompilationInfo info, Element element, int offset) {
                    String simpleName = element.getSimpleName().toString();
                    if (element.getKind() == ElementKind.METHOD) {
                        Iterator<? extends VariableElement> it = ((ExecutableElement)element).getParameters().iterator();
                        Iterator<? extends TypeMirror> tIt = ((ExecutableType) element.asType()).getParameterTypes().iterator();
                        StringBuilder label = new StringBuilder();
                        StringBuilder insertText = new StringBuilder();
                        StringBuilder sortParams = new StringBuilder();
                        label.append("<b>").append(simpleName).append("</b>(");
                        insertText.append(simpleName).append("(");
                        sortParams.append('(');
                        int cnt = 0;
                        boolean asTemplate = false;
                        while(it.hasNext() && tIt.hasNext()) {
                            TypeMirror tm = tIt.next();
                            if (tm == null) {
                                break;
                            }
                            cnt++;
                            String paramTypeName = MicronautDataCompletionTask.getTypeName(info, tm, false, ((ExecutableElement)element).isVarArgs() && !tIt.hasNext()).toString();
                            String paramName = it.next().getSimpleName().toString();
                            label.append(escape(paramTypeName)).append(' ').append(PARAMETER_NAME_COLOR).append(paramName).append(COLOR_END);
                            sortParams.append(paramTypeName);
                            insertText.append("${").append(paramName).append("}");
                            asTemplate = true;
                            if (tIt.hasNext()) {
                                label.append(", ");
                                sortParams.append(',');
                                insertText.append(", ");
                            }
                        }
                        label.append(')');
                        insertText.append(')');
                        sortParams.append(')');
                        CompletionUtilities.CompletionItemBuilder builder = CompletionUtilities.newCompletionItemBuilder(simpleName)
                                .startOffset(offset)
                                .iconResource(element.getModifiers().contains(Modifier.STATIC) ? METHOD_ST_PUBLIC : METHOD_PUBLIC)
                                .leftHtmlText(label.toString())
                                .rightHtmlText(MicronautDataCompletionTask.getTypeName(info, ((ExecutableElement)element).getReturnType(), false, false).toString())
                                .sortPriority(100)
                                .sortText(String.format("%s#%02d%s", simpleName, cnt, sortParams.toString()))
                                .insertText(insertText.toString());
                        if (asTemplate) {
                            builder.onSelect(ctx -> {
                                final Document doc = ctx.getComponent().getDocument();
                                try {
                                    doc.remove(offset, ctx.getComponent().getCaretPosition() - offset);
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                                CodeTemplateManager.get(doc).createTemporary(insertText.toString()).insert(ctx.getComponent());
                            });
                        } else {
                            builder.insertText(insertText.toString());
                        }
                        return builder.build();
                    }
                    CompletionUtilities.CompletionItemBuilder builder = CompletionUtilities.newCompletionItemBuilder(simpleName).startOffset(offset);
                    switch (element.getKind()) {
                        case ENUM:
                            builder.iconResource(ENUM_ICON).leftHtmlText(CLASS_COLOR + simpleName + COLOR_END).sortPriority(300);
                            break;
                        case CLASS:
                            builder.iconResource(CLASS_ICON).leftHtmlText(CLASS_COLOR + simpleName + COLOR_END).sortPriority(300);
                            break;
                        case RECORD:
                            builder.iconResource(RECORD_ICON).leftHtmlText(CLASS_COLOR + simpleName + COLOR_END).sortPriority(300);
                            break;
                        case ANNOTATION_TYPE:
                            builder.iconResource(ANNOTATION_TYPE_ICON).leftHtmlText(INTERFACE_COLOR + simpleName + COLOR_END).sortPriority(300);
                            break;
                        case INTERFACE:
                            builder.iconResource(INTERFACE_ICON).leftHtmlText(INTERFACE_COLOR + simpleName + COLOR_END).sortPriority(300);
                            break;
                        case FIELD:
                            break;
                        default:
                            throw new IllegalStateException("Unexpected Java element kind: " + element.getKind());
                    }
                    return builder.build();
                }
            }));
            resultSet.setAnchorOffset(task.getAnchorOffset());
            resultSet.finish();
        }
    }

    private static String getHTMLColor(int r, int g, int b) {
        Color c = LFCustoms.shiftColor(new Color(r, g, b));
        return "<font color=#" //NOI18N
                + LFCustoms.getHexString(c.getRed())
                + LFCustoms.getHexString(c.getGreen())
                + LFCustoms.getHexString(c.getBlue())
                + ">"; //NOI18N
    }

    private static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toElementContent(s);
            } catch (CharConversionException ex) {}
        }
        return s;
    }
}
