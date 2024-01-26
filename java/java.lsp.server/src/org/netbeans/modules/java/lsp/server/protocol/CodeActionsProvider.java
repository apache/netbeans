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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static final String CODE_ACTIONS_PROVIDER_CLASS = "providerClass";
    public static final String DATA = "data";
    protected static final String ERROR = "<error>"; //NOI18N

    public abstract List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception;

    public CompletableFuture<CodeAction> resolve(NbCodeLanguageClient client, CodeAction codeAction, Object data) {
        return CompletableFuture.completedFuture(codeAction);
    }

    public Set<String> getCommands() {
        return Collections.emptySet();
    }

    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        return CompletableFuture.completedFuture(false);
    }

    protected CodeAction createCodeAction(NbCodeLanguageClient client, String name, String kind, Object data, String command, Object... commandArgs) {
        return createCodeAction(client, name, kind, data, command, Arrays.asList(commandArgs));
    }

    protected CodeAction createCodeAction(NbCodeLanguageClient client, String name, String kind, Object data, String command, List<Object> commandArgs) {
        CodeAction action = new CodeAction(name);
        action.setKind(kind);
        if (command != null) {
            action.setCommand(new Command(name, Utils.encodeCommand(command, client.getNbCodeCapabilities()), commandArgs));
        }
        if (data != null) {
            Map<String, Object> map = new HashMap<>();
            map.put(CODE_ACTIONS_PROVIDER_CLASS, getClass().getName());
            map.put(DATA, data);
            action.setData(map);
        }
        return action;
    }

    protected static int getOffset(CompilationInfo info, Position pos) {
        LineMap lm = info.getCompilationUnit().getLineMap();
        return (int) lm.getPosition(pos.getLine() + 1, pos.getCharacter() + 1);
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
        String detail = Utils.detail(info, e, fqn);
        if (detail != null) {
            sb.append(detail);
        }
        return sb.toString();
    }

    protected static String createLabel(CompilationInfo info, ExecutableElement e) {
        return createLabel(info, e, false);
    }

    protected static String createLabel(CompilationInfo info, ExecutableElement e, boolean fqn) {
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.label(info, e, fqn));
        String detail = Utils.detail(info, e, fqn);
        if (detail != null) {
            sb.append(detail);
        }
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
