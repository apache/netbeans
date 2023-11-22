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

import java.util.Iterator;
import java.util.function.Consumer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.modules.micronaut.expression.MicronautExpressionLanguageUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.lsp.CompletionCollector;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionCollector.class)
public class MicronautDataCompletionCollector implements CompletionCollector {

    @Override
    public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
        new MicronautDataCompletionTask().query(doc, offset, new MicronautDataCompletionTask.ItemFactory<Completion>() {
            @Override
            public Completion createFinderMethodItem(String name, String returnType, int offset) {
                Builder builder = CompletionCollector.newBuilder(name).kind(Completion.Kind.Method).sortText(String.format("%04d%s", 10, name));
                if (returnType != null) {
                    builder.insertText(new StringBuilder("${1:").append(returnType).append("} ").append(name).append("$0()").toString());
                    builder.insertTextFormat(Completion.TextFormat.Snippet);
                }
                return builder.build();
            }
            @Override
            public Completion createFinderMethodNameItem(String prefix, String name, int offset) {
                return CompletionCollector.newBuilder(prefix + name).kind(Completion.Kind.Method).sortText(String.format("%04d%s", 10, name)).build();
            }
            @Override
            public Completion createSQLItem(CompletionItem item) {
                return CompletionCollector.newBuilder(item.getInsertPrefix().toString())
                        .insertText(item.getInsertPrefix().toString().replace("\"", "\\\""))
                        .kind(Completion.Kind.Property)
                        .sortText(String.format("%010d%s",
                                Long.valueOf(item.getSortPriority()) + Math.abs(Long.valueOf(Integer.MIN_VALUE)),
                                item.getSortText())
                        )
                        .build();
            }
            @Override
            public Completion createKeywordItem(String name, int offset) {
                return CompletionCollector.newBuilder(name).kind(Completion.Kind.Keyword).sortText(String.format("%04d%s", 200, name)).build();
            }
            @Override
            public Completion createBuiltInItem(String name, String parenPair, int offset) {
                return CompletionCollector.newBuilder(name + parenPair.charAt(0) + "..." + parenPair.charAt(1))
                        .kind('(' == parenPair.charAt(0) ? Completion.Kind.Function : Completion.Kind.Variable)
                        .sortText(String.format("%04d%s", 100, name))
                        .insertText(name + parenPair.charAt(0) + "$0" + parenPair.charAt(1))
                        .insertTextFormat(Completion.TextFormat.Snippet)
                        .build();
            }
            @Override
            public Completion createPackageItem(String name, int offset) {
                return CompletionCollector.newBuilder(name)
                        .kind(Completion.Kind.Folder)
                        .sortText(String.format("%04d%s", 400, name))
                        .insertText(name + '.')
                        .build();
            }
            @Override
            public Completion createBeanPropertyItem(String name, String typeName, int offset) {
                return CompletionCollector.newBuilder(name + " : " + typeName)
                        .kind(Completion.Kind.Property)
                        .sortText(String.format("%04d%s", 50, name))
                        .insertText(name)
                        .build();
            }

            @Override
            public Completion createEnvPropertyItem(String name, String documentation, int anchorOffset, int offset) {
                return CompletionCollector.newBuilder(name)
                        .kind(Completion.Kind.Text)
                        .sortText(name)
                        .textEdit(new TextEdit(anchorOffset, offset, name))
                        .documentation(documentation)
                        .build();
            }
            @Override
            public Completion createJavaElementItem(CompilationInfo info, Element element, int offset) {
                String simpleName = element.getSimpleName().toString();
                if (element.getKind() == ElementKind.METHOD) {
                    Iterator<? extends VariableElement> it = ((ExecutableElement)element).getParameters().iterator();
                    Iterator<? extends TypeMirror> tIt = ((ExecutableType) element.asType()).getParameterTypes().iterator();
                    StringBuilder label = new StringBuilder();
                    StringBuilder insertText = new StringBuilder();
                    StringBuilder sortParams = new StringBuilder();
                    label.append(simpleName).append("(");
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
                        label.append(paramTypeName).append(' ').append(paramName);
                        sortParams.append(paramTypeName);
                        insertText.append("${").append(cnt).append(":").append(paramName).append("}");
                        asTemplate = true;
                        if (tIt.hasNext()) {
                            label.append(", ");
                            sortParams.append(',');
                            insertText.append(", ");
                        }
                    }
                    label.append(") : ").append(MicronautDataCompletionTask.getTypeName(info, ((ExecutableElement)element).getReturnType(), false, false).toString());
                    insertText.append(')');
                    sortParams.append(')');
                    return CompletionCollector.newBuilder(label.toString())
                            .kind(Completion.Kind.Method)
                            .sortText(String.format("%04d%s#%02d%s", 100, simpleName, cnt, sortParams.toString()))
                            .insertText(insertText.toString())
                            .insertTextFormat(asTemplate ? Completion.TextFormat.Snippet : Completion.TextFormat.PlainText)
                            .documentation(() -> MicronautExpressionLanguageUtilities.getJavadocText(info, element, false, 3))
                            .build();
                }
                Builder builder = CompletionCollector.newBuilder(simpleName);
                switch (element.getKind()) {
                    case ENUM:
                        builder.kind(Completion.Kind.Enum).sortText(String.format("%04d%s", 300, simpleName));
                        break;
                    case CLASS:
                        builder.kind(Completion.Kind.Class).sortText(String.format("%04d%s", 300, simpleName));
                        break;
                    case RECORD:
                        builder.kind(Completion.Kind.Struct).sortText(String.format("%04d%s", 300, simpleName));
                        break;
                    case ANNOTATION_TYPE:
                    case INTERFACE:
                        builder.kind(Completion.Kind.Interface).sortText(String.format("%04d%s", 300, simpleName));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected Java element kind: " + element.getKind());
                }
                return builder.documentation(() -> MicronautExpressionLanguageUtilities.getJavadocText(info, element, false, 3)).build();
            }
        }).stream().forEach(consumer);
        return true;
    }
}
