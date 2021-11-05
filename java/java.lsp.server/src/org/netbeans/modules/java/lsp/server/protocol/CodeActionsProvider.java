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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.xtext.xbase.lib.Pure;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.parsing.api.ResultIterator;

/**
 *
 * @author Dusan Balek
 */
public abstract class CodeActionsProvider {

    public static final String CODE_GENERATOR_KIND = "source.generate";
    protected static final String ERROR = "<error>"; //NOI18N

    public abstract List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception;

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

    protected static String createLabel(CompilationInfo info, Element e) {
        return createLabel(info, e, false);
    }

    protected static String createLabel(CompilationInfo info, Element e, boolean fqn) {
        switch (e.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                return createLabel(info, (TypeElement) e, fqn);
            case CONSTRUCTOR:
            case METHOD:
                return createLabel(info, (ExecutableElement) e, fqn);
            case ENUM_CONSTANT:
            case FIELD:
                return createLabel(info, (VariableElement) e, fqn);
            default:
                return null;
        }
    }

    protected static String createLabel(CompilationInfo info, TypeElement e) {
        return createLabel(info, e, false);
    }

    protected static String createLabel(CompilationInfo info, TypeElement e, boolean fqn) {
        return Utils.label(info, e, fqn);
    }

    protected static String createLabel(CompilationInfo info, VariableElement e) {
        return createLabel(info, e, false);
    }

    protected static String createLabel(CompilationInfo info, VariableElement e, boolean fqn) {
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.label(info, e, fqn));
        sb.append(" : "); // NOI18N
        sb.append(Utils.detail(info, e, fqn));
        return sb.toString();
    }

    protected static String createLabel(CompilationInfo info, ExecutableElement e) {
        return createLabel(info, e, false);
    }

    protected static String createLabel(CompilationInfo info, ExecutableElement e, boolean fqn) {
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.label(info, e, fqn));
        sb.append(" : "); // NOI18N
        sb.append(Utils.detail(info, e, fqn));
        return sb.toString();
    }

    public static class ElementData {

        private String kind;
        private String[] signature;

        public ElementData() {
        }

        public ElementData(Element element) {
            this(ElementHandle.create(element));
        }

        public ElementData(ElementHandle<? extends Element> handle) {
            this.kind = handle.getKind().name();
            this.signature = ElementHandleAccessor.getInstance().getJVMSignature(handle);
        }

        public ElementHandle toHandle() {
            return ElementHandleAccessor.getInstance().create(ElementKind.valueOf(kind), signature);
        }

        public Element resolve(CompilationInfo info) {
            return toHandle().resolve(info);
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
