/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.completion.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedField;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod;
import org.netbeans.modules.groovy.editor.completion.AccessLevel;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.spi.completion.CompletionProvider;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 * @author Martin Janicek
 */
public final class GroovyElementsProvider implements CompletionProvider {

    @Override
    public Map<MethodSignature, CompletionItem> getMethods(CompletionContext context) {
        final GroovyIndex index = getIndex(context);
        final Map<MethodSignature, CompletionItem> result = new HashMap<>();
        
        if (index != null) {
            Set<IndexedMethod> methods;

            if ("".equals(context.getPrefix())) { // NOI18N
                methods = index.getMethods(".*", context.getTypeName(), QuerySupport.Kind.REGEXP); // NOI18N
            } else {
                methods = index.getMethods(context.getPrefix(), context.getTypeName(), QuerySupport.Kind.PREFIX);
            }

            for (IndexedMethod indexedMethod : methods) {
                if (accept(context.access, indexedMethod)) {
                    result.put(getMethodSignature(indexedMethod), CompletionItem.forJavaMethod(
                            context.getTypeName(),
                            indexedMethod.getName(),
                            indexedMethod.getParameterTypes(),
                            indexedMethod.getReturnType(),
                            Utilities.gsfModifiersToModel(indexedMethod.getModifiers(), Modifier.PUBLIC),
                            context.getAnchor(),
                            false,
                            context.isNameOnly()));
                }
            }
        }

        return result;
    }

    @Override
    public Map<MethodSignature, CompletionItem> getStaticMethods(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<FieldSignature, CompletionItem> getFields(CompletionContext context) {
        final GroovyIndex index = getIndex(context);
        final Map<FieldSignature, CompletionItem> result = new HashMap<>();
        
        if (index != null) {
            Set<IndexedField> fields;

            if ("".equals(context.getPrefix())) { // NOI18N
                fields = index.getAllFields(context.getTypeName());
            } else {
                fields = index.getFields(context.getPrefix(), context.getTypeName(), QuerySupport.Kind.PREFIX);
            }

            for (IndexedField indexedField : fields) {
                result.put(getFieldSignature(indexedField), new CompletionItem.FieldItem(
                        indexedField.getTypeName(),
                        indexedField.getName(),
                        indexedField.getModifiers(),
                        context.getAnchor()));
            }
        }

        return result;
    }

    @Override
    public Map<FieldSignature, CompletionItem> getStaticFields(CompletionContext context) {
        return Collections.emptyMap();
    }
    
    private GroovyIndex getIndex(CompletionContext context) {
        final FileObject fo = context.getSourceFile();
        
        if (fo != null) {
            return GroovyIndex.get(QuerySupport.findRoots(fo, Collections.singleton(ClassPath.SOURCE), null, null));
        }
        return null;
    }

    private MethodSignature getMethodSignature(IndexedMethod method) {
        String[] parameters = method.getParameterTypes().toArray(new String[method.getParameterTypes().size()]);
        return new MethodSignature(method.getName(), parameters);
    }

    private FieldSignature getFieldSignature(IndexedField field) {
        return new FieldSignature(field.getName());
    }

    private boolean accept(Set<AccessLevel> levels, IndexedElement element) {
        for (AccessLevel accessLevel : levels) {
            if (accessLevel.accept(element.getModifiers())) {
                return true;
            }
        }

        return false;
    }
}
