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
package org.netbeans.modules.javascript2.jquery.model;

import org.netbeans.modules.javascript2.jquery.SelectorsLoader;
import org.netbeans.modules.javascript2.jquery.editor.JQueryCodeCompletion;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Petr Pisl
 */
public class JQueryModel {

    @org.netbeans.api.annotations.common.SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static boolean skipInTest = false;

    private static JsObject jQuery = null;
    private static JsObject rjQuery = null;
    private static JsFunction globalObject = null;
    
    // XXX this should be synchronized I guess
    public static JsObject getGlobalObject(ModelElementFactory modelElementFactory) {
        if (skipInTest) {
            return null;
        }

        if (globalObject == null) {
            File apiFile = InstalledFileLocator.getDefault().locate(JQueryCodeCompletion.HELP_LOCATION, "org.netbeans.modules.javascript2.jquery", false); //NoI18N
            if (apiFile != null) {
                globalObject = modelElementFactory.newGlobalObject(
                        FileUtil.toFileObject(apiFile), (int) apiFile.length());
                JsFunction function = new JQFunction(modelElementFactory.newFunction(
                        (DeclarationScope) globalObject, globalObject, JQueryUtils.JQUERY, Collections.<String>emptyList())); // NOI18N
                jQuery =  modelElementFactory.putGlobalProperty(globalObject, function);
                rjQuery = modelElementFactory.newReference(JQueryUtils.JQUERY$, jQuery, false); // NOI18N

                SelectorsLoader.addToModel(apiFile, modelElementFactory, jQuery);
                globalObject.addProperty(rjQuery.getName(), rjQuery);
            }
        }
        return globalObject;
    }
    
    private static class JQFunction implements JsFunction {
        
        private final JsFunction delegate;

        public JQFunction(JsFunction delegate) {
            this.delegate = delegate;
        }

        @Override
        public JsObject getProperty(String name) {
            JsObject result = delegate.getProperty(name);
            if(result == null) {
                String lookingFor = name + "#";  //NOI18N
                for(String proName : getProperties().keySet()) {
                    if(proName.startsWith(lookingFor)) {
                        result = delegate.getProperty(proName);
                        break;
                    }
                }
            }
            return result;
        }

        public boolean isVirtual() {
            return false;
        }

        // pure delegation follows

        @Override
        public JsObject getParent() {
            return delegate.getParent();
        }

        @Override
        public void addDeclaredScope(DeclarationScope scope) {
            delegate.addDeclaredScope(scope);
        }

        @Override
        public DeclarationScope getParentScope() {
            return delegate.getParentScope();
        }

        @Override
        public Collection<? extends DeclarationScope> getChildrenScopes() {
            return delegate.getChildrenScopes();
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
        public Kind getJSKind() {
            if (JQueryUtils.JQUERY$.equals(getName()) || JQueryUtils.JQUERY.equals(getName())) {
                return Kind.METHOD;
            }
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
        public boolean moveProperty(String name, JsObject newParent) {
            return delegate.moveProperty(name, newParent);
        }
        
    }

}
