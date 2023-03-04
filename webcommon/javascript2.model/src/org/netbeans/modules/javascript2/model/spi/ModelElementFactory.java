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
package org.netbeans.modules.javascript2.model.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.model.JsArrayReference;
import org.netbeans.modules.javascript2.model.JsFunctionImpl;
import org.netbeans.modules.javascript2.model.JsFunctionReference;
import org.netbeans.modules.javascript2.model.JsObjectImpl;
import org.netbeans.modules.javascript2.model.JsObjectReference;
import org.netbeans.modules.javascript2.model.ModelElementFactoryAccessor;
import org.netbeans.modules.javascript2.model.api.JsArray;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsElement.Kind;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public final class ModelElementFactory {

    static {
        ModelElementFactoryAccessor.setDefault(new ModelElementFactoryAccessor() {

            @Override
            public ModelElementFactory createModelElementFactory() {
                return new ModelElementFactory();
            }
        });
    }

    private ModelElementFactory() {
        super();
    }

    public JsFunction newGlobalObject(FileObject fileObject, int length) {
        return JsFunctionImpl.createGlobal(fileObject, length, null);
    }

    public JsObject loadGlobalObject(FileObject fileObject, int length,
            String sourceLabel, URL defaultDocURL) throws IOException {
        try (InputStream is = fileObject.getInputStream()) {
            return loadGlobalObject(is, sourceLabel, defaultDocURL);
        }
    }

    public JsObject loadGlobalObject(InputStream is, String sourceLabel, URL defaultDocURL) throws IOException {
        JsFunction global = newGlobalObject(null, Integer.MAX_VALUE);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            for (JsObject object : Model.readModel(reader, global, sourceLabel, defaultDocURL)) {
                putGlobalProperty(global, object);
            }
            return global;
        }
    }

    public JsObject putGlobalProperty(JsFunction global, JsObject property) {
        if (property.getParent() != global) {
            throw new IllegalArgumentException("Property is not child of global");
        }
        JsObject wrapped;
        if (property instanceof JsFunction) {
            GlobalFunction real = new GlobalFunction((JsFunction) property);
            real.setParentScope(global);
            real.setParent(global);
            wrapped = real;
        } else {
            GlobalObject real = new GlobalObject(property);
            real.setParent(global);
            wrapped = real;
        }
        global.addProperty(wrapped.getName(), wrapped);
        return wrapped;
    }

    public JsObject newObject(JsObject parent, String name, OffsetRange offsetRange, boolean isDeclared) {
        return newObject(parent, name, offsetRange, isDeclared, null);
    }

    public JsObject newObject(JsObject parent, String name, OffsetRange offsetRange, boolean isDeclared, String sourceLabel) {
        return new JsObjectImpl(parent, new Identifier(name, offsetRange), offsetRange, isDeclared, null, sourceLabel);
    }

    public JsFunction newFunction(DeclarationScope scope, JsObject parent, String name, Collection<String> params) {
        return newFunction(scope, parent, name, params, null);
    }

    public JsFunction newFunction(DeclarationScope scope, JsObject parent, String name, Collection<String> params, String sourceLabel) {
        List<Identifier> realParams = new ArrayList<>();
        for (String param : params) {
            realParams.add(new Identifier(param, OffsetRange.NONE));
        }
        return newFunction(scope, parent, new Identifier(name, OffsetRange.NONE), realParams, OffsetRange.NONE, sourceLabel);
    }

    public JsFunction newFunction(DeclarationScope scope, JsObject parent, String name, Collection<String> params, OffsetRange range, String sourceLabel) {
        List<Identifier> realParams = new ArrayList<>();
        for (String param : params) {
            realParams.add(new Identifier(param, OffsetRange.NONE));
        }
        return newFunction(scope, parent, new Identifier(name, OffsetRange.NONE), realParams, range, sourceLabel);
    }

    public JsFunction newFunction(DeclarationScope scope, JsObject parent, Identifier name, List<Identifier> params, OffsetRange range) {
        return newFunction(scope, parent, name, params, range, null);
    }

    public JsFunction newFunction(DeclarationScope scope, JsObject parent, Identifier name, List<Identifier> params, OffsetRange range, String sourceLabel) {
        return new JsFunctionImpl(scope, parent, name, params, range, null, sourceLabel);
    }

    public JsObject newReference(JsObject parent, String name, OffsetRange offsetRange,
            JsObject original, boolean isDeclared, @NullAllowed Set<Modifier> modifiers) {
        if (original instanceof JsFunction) {
            return new JsFunctionReference(parent, new Identifier(name, offsetRange),
                    (JsFunction) original, isDeclared, modifiers);
        } else if (original instanceof JsArray) {
            return new JsArrayReference(parent, new Identifier(name, offsetRange),
                    (JsArray) original, isDeclared, modifiers);
        }
        return new JsObjectReference(parent, new Identifier(name, offsetRange),
                original, isDeclared, modifiers);
    }

    public JsObject newReference(String name, JsObject original, boolean isDeclared) {
        if (original instanceof JsFunction) {
            return new OriginalParentFunctionReference(new Identifier(name, OffsetRange.NONE), (JsFunction) original, isDeclared);
        } else if (original instanceof JsArray) {
            return new OriginalParentArrayReference(new Identifier(name, OffsetRange.NONE), (JsArray) original, isDeclared);
        }
        return new OriginalParentObjectReference(new Identifier(name, OffsetRange.NONE), original, isDeclared);
    }

    public JsObject newReference(String name, JsObject original, boolean isDeclared, boolean isVirtual) {
        JsObject object = newReference(name, original, isDeclared);
        ((JsObjectImpl)object).setVirtual(isVirtual);
        return object;
    }

    public TypeUsage newType(String name, int offset, boolean resolved) {
        return new TypeUsage(name, offset, resolved);
    }

    private static class OriginalParentFunctionReference extends JsFunctionReference {

        public OriginalParentFunctionReference(Identifier declarationName, JsFunction original,
                boolean isDeclared) {
            super(original.getParent(), declarationName, original, isDeclared, null);
        }

        @Override
        public JsObject getParent() {
            return getOriginal().getParent();
        }
    }

    private static class OriginalParentArrayReference extends JsArrayReference {

        public OriginalParentArrayReference(Identifier declarationName, JsArray original,
                boolean isDeclared) {
            super(original.getParent(), declarationName, original, isDeclared, null);
        }

        @Override
        public JsObject getParent() {
            return getOriginal().getParent();
        }
    }

    private static class OriginalParentObjectReference extends JsObjectReference {

        public OriginalParentObjectReference(Identifier declarationName, JsObject original, boolean isDeclared) {
            super(original.getParent(), declarationName, original, isDeclared, null);
        }

        @Override
        public JsObject getParent() {
            return getOriginal().getParent();
        }
    }

    private static class GlobalObject implements JsObject {

        private final JsObject delegate;

        private JsObject parent;

        public GlobalObject(JsObject delegate) {
            this.delegate = delegate;
            this.parent = delegate.getParent();
        }

        @Override
        public JsObject getParent() {
            return this.parent;
        }

        public void setParent(JsObject parent) {
            this.parent = parent;
        }

        // pure delegation follows

        @Override
        public Identifier getDeclarationName() {
            return delegate.getDeclarationName();
        }

        @Override
        public Map<String, ? extends JsObject> getProperties() {
            return delegate.getProperties();
        }

        @Override
        public void addProperty(String name, JsObject property) {
            delegate.addProperty(name, property);
        }

        @Override
        public JsObject getProperty(String name) {
            return delegate.getProperty(name);
        }

        @Override
        public List<Occurrence> getOccurrences() {
            return delegate.getOccurrences();
        }

        @Override
        public void addOccurrence(OffsetRange offsetRange) {
            delegate.addOccurrence(offsetRange);
        }

        @Override
        public String getFullyQualifiedName() {
            return delegate.getFullyQualifiedName();
        }

        @Override
        public Collection<? extends TypeUsage> getAssignmentForOffset(int offset) {
            return delegate.getAssignmentForOffset(offset);
        }

        @Override
        public Collection<? extends TypeUsage> getAssignments() {
            return delegate.getAssignments();
        }

        @Override
        public int getAssignmentCount() {
            return delegate.getAssignmentCount();
        }

        @Override
        public void addAssignment(TypeUsage typeName, int offset) {
            delegate.addAssignment(typeName, offset);
        }

        @Override
        public void clearAssignments() {
            delegate.clearAssignments();
        }

        @Override
        public boolean isAnonymous() {
            return delegate.isAnonymous();
        }

        @Override
        public void setAnonymous(boolean value) {
            delegate.setAnonymous(value);
        }

        @Override
        public boolean isDeprecated() {
            return delegate.isDeprecated();
        }

        @Override
        public boolean hasExactName() {
            return delegate.hasExactName();
        }

        @Override
        public Documentation getDocumentation() {
            return delegate.getDocumentation();
        }

        @Override
        public void setDocumentation(Documentation documentation) {
            delegate.setDocumentation(documentation);
        }

        @Override
        public int getOffset() {
            return delegate.getOffset();
        }

        @Override
        public OffsetRange getOffsetRange() {
            return delegate.getOffsetRange();
        }

        @Override
        public Kind getJSKind() {
            return delegate.getJSKind();
        }

        @Override
        public boolean isDeclared() {
            return delegate.isDeclared();
        }

        @Override
        public String getSourceLabel() {
            return delegate.getSourceLabel();
        }

        @Override
        public boolean isPlatform() {
            return delegate.isPlatform();
        }

        @Override
        public FileObject getFileObject() {
            return delegate.getFileObject();
        }

        @Override
        public String getMimeType() {
            return delegate.getMimeType();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getIn() {
            return delegate.getIn();
        }

        @Override
        public ElementKind getKind() {
            return delegate.getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            return delegate.getModifiers();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return delegate.signatureEquals(handle);
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return delegate.getOffsetRange(result);
        }

        @Override
        public boolean containsOffset(int offset) {
            return delegate.containsOffset(offset);
        }

        @Override
        public boolean isVirtual() {
            return false;
        }

        @Override
        public boolean moveProperty(String name, JsObject newParent) {
            return delegate.moveProperty(name, newParent);
        }

    }

    private static class GlobalFunction implements JsFunction {

        private final JsFunction delegate;

        private DeclarationScope inScope;

        private JsObject parent;

        public GlobalFunction(JsFunction delegate) {
            this.delegate = delegate;
            this.inScope = delegate.getParentScope();
            this.parent = delegate.getParent();
        }

        @Override
        public DeclarationScope getParentScope() {
            return this.inScope;
        }

        protected void setParentScope(DeclarationScope inScope) {
            this.inScope = inScope;
        }

        @Override
        public JsObject getParent() {
            return this.parent;
        }

        public void setParent(JsObject parent) {
            this.parent = parent;
        }

        // pure delegation follows

        @Override
        public JsObject getProperty(String name) {
            return delegate.getProperty(name);
        }

        @Override
        public Collection<? extends DeclarationScope> getChildrenScopes() {
            return delegate.getChildrenScopes();
        }

        @Override
        public void addDeclaredScope(DeclarationScope scope) {
            delegate.addDeclaredScope(scope);
        }

        @Override
        public Collection<? extends JsObject> getParameters() {
            return delegate.getParameters();
        }

        @Override
        public JsObject getParameter(String name) {
            return delegate.getParameter(name);
        }

        @Override
        public void addReturnType(TypeUsage type) {
            delegate.addReturnType(type);
        }

        @Override
        public Collection<? extends TypeUsage> getReturnTypes() {
            return delegate.getReturnTypes();
        }

        @Override
        public Identifier getDeclarationName() {
            return delegate.getDeclarationName();
        }

        @Override
        public Map<String, ? extends JsObject> getProperties() {
            return delegate.getProperties();
        }

        @Override
        public void addProperty(String name, JsObject property) {
            delegate.addProperty(name, property);
        }

        @Override
        public List<Occurrence> getOccurrences() {
            return delegate.getOccurrences();
        }

        @Override
        public void addOccurrence(OffsetRange offsetRange) {
            delegate.addOccurrence(offsetRange);
        }

        @Override
        public String getFullyQualifiedName() {
            return delegate.getFullyQualifiedName();
        }

        @Override
        public Collection<? extends TypeUsage> getAssignmentForOffset(int offset) {
            return delegate.getAssignmentForOffset(offset);
        }

        @Override
        public Collection<? extends TypeUsage> getAssignments() {
            return delegate.getAssignments();
        }

        @Override
        public int getAssignmentCount() {
            return delegate.getAssignmentCount();
        }

        @Override
        public void addAssignment(TypeUsage typeName, int offset) {
            delegate.addAssignment(typeName, offset);
        }

        @Override
        public void clearAssignments() {
            delegate.clearAssignments();
        }

        @Override
        public boolean isAnonymous() {
            return delegate.isAnonymous();
        }

        @Override
        public void setAnonymous(boolean value) {
            delegate.setAnonymous(value);
        }

        @Override
        public boolean isDeprecated() {
            return delegate.isDeprecated();
        }

        @Override
        public boolean hasExactName() {
            return delegate.hasExactName();
        }

        @Override
        public Documentation getDocumentation() {
            return delegate.getDocumentation();
        }

        @Override
        public void setDocumentation(Documentation documentation) {
            delegate.setDocumentation(documentation);
        }

        @Override
        public int getOffset() {
            return delegate.getOffset();
        }

        @Override
        public OffsetRange getOffsetRange() {
            return delegate.getOffsetRange();
        }

        @Override
        public JsElement.Kind getJSKind() {
            return delegate.getJSKind();
        }

        @Override
        public boolean isDeclared() {
            return delegate.isDeclared();
        }

        @Override
        public String getSourceLabel() {
            return delegate.getSourceLabel();
        }

        @Override
        public boolean isPlatform() {
            return delegate.isPlatform();
        }

        @Override
        public FileObject getFileObject() {
            return delegate.getFileObject();
        }

        @Override
        public String getMimeType() {
            return delegate.getMimeType();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getIn() {
            return delegate.getIn();
        }

        @Override
        public ElementKind getKind() {
            return delegate.getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            return delegate.getModifiers();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return delegate.signatureEquals(handle);
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return delegate.getOffsetRange(result);
        }

        @Override
        public boolean containsOffset(int offset) {
            return delegate.containsOffset(offset);
        }

        @Override
        public boolean isVirtual() {
            return false;
        }

        @Override
        public boolean moveProperty(String name, JsObject newParent) {
            return delegate.moveProperty(name, newParent);
        }

    }
}
