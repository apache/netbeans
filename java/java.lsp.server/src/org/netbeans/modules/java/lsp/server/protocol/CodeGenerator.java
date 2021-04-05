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
package org.netbeans.modules.java.lsp.server.protocol;

import com.sun.source.tree.LineMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.xtext.xbase.lib.Pure;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.source.ElementHandleAccessor;

/**
 *
 * @author Dusan Balek
 */
public abstract class CodeGenerator {

    public static final String CODE_GENERATOR_KIND = "source.generate";
    protected static final String ERROR = "<error>"; //NOI18N

    public abstract List<CodeAction> getCodeActions(CompilationInfo info, CodeActionParams params);

    public abstract Set<String> getCommands();

    public abstract CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments);

    protected static int getOffset(CompilationInfo info, Position pos) {
        LineMap lm = info.getCompilationUnit().getLineMap();
        return (int) lm.getPosition(pos.getLine() + 1, pos.getCharacter() + 1);
    }

    protected static CodeAction createCodeAction(String name, String kind, String command, Object... args) {
        CodeAction action = new CodeAction(name);
        action.setKind(kind);
        action.setCommand(new Command(name, command, Arrays.asList(args)));
        return action;
    }

    protected static String createLabel(CompilationInfo info, TypeElement e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getSimpleName());
        List<? extends TypeParameterElement> typeParams = e.getTypeParameters();
        if (typeParams != null && !typeParams.isEmpty()) {
            sb.append("<"); // NOI18N
            for(Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
                TypeParameterElement tp = it.next();
                sb.append(tp.getSimpleName());
                List<? extends TypeMirror> bounds = tp.getBounds();
                if (!bounds.isEmpty()) {
                    if (bounds.size() > 1 || !"java.lang.Object".equals(bounds.get(0).toString())) { // NOI18N
                        sb.append(" extends "); // NOI18N
                        for (Iterator<? extends TypeMirror> bIt = bounds.iterator(); bIt.hasNext();) {
                            sb.append(Utilities.getTypeName(info, bIt.next(), false));
                            if (bIt.hasNext()) {
                                sb.append(" & "); // NOI18N
                            }
                        }
                    }
                }
                if (it.hasNext()) {
                    sb.append(", "); // NOI18N
                }
            }
            sb.append(">"); // NOI18N
        }
        return sb.toString();
    }

    protected static String createLabel(CompilationInfo info, VariableElement e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getSimpleName());
        if (e.getKind() != ElementKind.ENUM_CONSTANT) {
            sb.append(" : "); // NOI18N
            sb.append(Utilities.getTypeName(info, e.asType(), false));
        }
        return sb.toString();
    }

    protected static String createLabel(CompilationInfo info, ExecutableElement e) {
        StringBuilder sb = new StringBuilder();
        if (e.getKind() == ElementKind.CONSTRUCTOR) {
            sb.append(e.getEnclosingElement().getSimpleName());
        } else {
            sb.append(e.getSimpleName());
        }
        sb.append("("); // NOI18N
        for (Iterator<? extends VariableElement> it = e.getParameters().iterator(); it.hasNext();) {
            VariableElement param = it.next();
            if (!it.hasNext() && e.isVarArgs() && param.asType().getKind() == TypeKind.ARRAY) {
                sb.append(Utilities.getTypeName(info, ((ArrayType) param.asType()).getComponentType(), false));
                sb.append("...");
            } else {
                sb.append(Utilities.getTypeName(info, param.asType(), false));
            }
            sb.append(" "); // NOI18N
            sb.append(param.getSimpleName());
            if (it.hasNext()) {
                sb.append(", "); // NOI18N
            }
        }
        sb.append(")"); // NOI18N
        if (e.getKind() != ElementKind.CONSTRUCTOR) {
            TypeMirror rt = e.getReturnType();
            if (rt.getKind() != TypeKind.VOID) {
                sb.append(" : "); // NOI18N
                sb.append(Utilities.getTypeName(info, e.getReturnType(), false));
            }
        }
        return sb.toString();
    }

    public static class ElementData {

        private String kind;
        private String[] signature;

        public ElementData() {
        }

        public ElementData(Element element) {
            ElementHandle<Element> handle = ElementHandle.create(element);
            this.kind = handle.getKind().name();
            this.signature = ElementHandleAccessor.getInstance().getJVMSignature(handle);
        }

        Element resolve(CompilationInfo info) {
            ElementHandle handle = ElementHandleAccessor.getInstance().create(ElementKind.valueOf(kind), signature);
            return handle.resolve(info);
        }

        @Pure
        public String getKind() {
            return kind;
        }

        public void setKind(final String kind) {
            this.kind = kind;
        }

        @Pure
        public String[] getSignature() {
            return signature;
        }

        public void setSignature(final String[] signature) {
            this.signature = signature;
        }

        @Override
        @Pure
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.kind);
            hash = 97 * hash + Arrays.deepHashCode(this.signature);
            return hash;
        }

        @Override
        @Pure
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ElementData other = (ElementData) obj;
            if (this.kind != other.kind) {
                return false;
            }
            if (!Arrays.deepEquals(this.signature, other.signature)) {
                return false;
            }
            return true;
        }
    }
}
