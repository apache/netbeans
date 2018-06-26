/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
