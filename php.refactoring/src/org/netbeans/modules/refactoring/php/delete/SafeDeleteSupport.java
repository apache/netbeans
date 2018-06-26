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
