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
package org.netbeans.modules.refactoring.php.delete;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.model.*;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public final class SafeDeleteSupport {

    private Collection<? extends ClassScope> declaredClasses;
    private Collection<? extends ConstantElement> declaredConstants;
    private Collection<? extends FunctionScope> declaredFunctions;
    private Collection<? extends InterfaceScope> declaredInterfaces;
    private Collection<? extends VariableName> declaredVariables;
    private Set<FileObject> relevantFiles;
    private ElementQuery.Index idx;
    private final Model model;
    private Set<ModelElement> visibleElements;

    private SafeDeleteSupport(final Index idx, final Model model) {
        this.idx = idx;
        this.model = model;
    }

    public static SafeDeleteSupport getInstance(final PHPParseResult info) {
        Model model = ModelFactory.getModel(info);
        final Index indexQuery = ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(info));
        return new SafeDeleteSupport(indexQuery, model);
    }

    public Model getModel() {
        return model;
    }

    public ElementQuery.Index getIdx() {
        return idx;
    }

    public boolean hasVisibleElements() {
        return !getVisibleElements().isEmpty();
    }

    public Set<ModelElement> getVisibleElements() {
        if (visibleElements == null) {
            final FileScope fileScope = model.getFileScope();
            final ElementFilter[] filters = new ElementFilter[]{
                ElementFilter.forKind(PhpElementKind.CLASS),
                ElementFilter.forKind(PhpElementKind.IFACE),
                ElementFilter.forKind(PhpElementKind.FUNCTION),
                ElementFilter.forKind(PhpElementKind.CONSTANT),
                ElementFilter.forKind(PhpElementKind.VARIABLE)
            };
            declaredClasses = ModelUtils.getDeclaredClasses(fileScope);
            declaredInterfaces = ModelUtils.getDeclaredInterfaces(fileScope);
            declaredFunctions = ModelUtils.getDeclaredFunctions(fileScope);
            declaredConstants = ModelUtils.getDeclaredConstants(fileScope);
            declaredVariables = ModelUtils.getDeclaredVariables(fileScope);

            final Set<ModelElement> elements = new HashSet<>();
            elements.addAll(declaredClasses);
            elements.addAll(declaredInterfaces);
            elements.addAll(declaredFunctions);
            elements.addAll(declaredConstants);
            elements.addAll(declaredConstants);

            visibleElements = ElementFilter.anyOf(filters).filter(elements);
        }
        return visibleElements;
    }

    public FileObject getFile() {
        return model.getFileScope().getFileObject();
    }

    Set<FileObject> getRelevantFiles() {
        if (relevantFiles == null) {
            relevantFiles = new HashSet<>();
            for (ModelElement element : getVisibleElements()) {
                relevantFiles.addAll(idx.getLocationsForIdentifiers(element.getName()));
            }
            relevantFiles.remove(getFile());
        }
        return relevantFiles;
    }
}
