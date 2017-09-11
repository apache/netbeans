/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.schema.model.visitor;

import java.util.*;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 * Resolves substitutions for {@link GlobalElement}s which are referenced by
 * other {@link GlobalElement}s as substitution groups.
 *
 * @author Daniel Bell (dbell@netbeans.org)
 * @see #resolveSubstitutions
 * @since org.netbeans.modules.xml.schema.model/1 1.17
 */
public final class FindSubstitutions {

    private FindSubstitutions() {
    }

    /**
     * Find all substitutions for a {@link GlobalElement} that is referenced by
     * other {@link GlobalElement}s as a substitution group. All referenced
     * schemas (via &lt;import&gt; / &lt;include&gt; /&lt;redefine&gt;) are
     * searched for substitution possibilities.
     *
     * @param model the model to search for substitutions for the specified
     * substitution group head
     * @param namespaceUri the namespace URI of the substitution group head
     * @param localName local name of the substitution group head (must resolve
     * to a global element)
     * @return all possible substitutions for the specified type
     */
    public static Set<GlobalElement> resolveSubstitutions(SchemaModel model, String namespaceUri, String localName) {
        GlobalElement element = model.resolve(namespaceUri, localName, GlobalElement.class);
        if (element == null) {
            return Collections.emptySet();
        } else {
            return resolveSubstitutions(model, element);
        }
    }

    /**
     * Find all substitutions for a {@link GlobalElement} that is referenced by
     * other {@link GlobalElement}s as a substitution group. All referenced
     * schemas (via &lt;import&gt; / &lt;include&gt; /&lt;redefine&gt;) are
     * searched for substitution possibilities.
     *
     * @param model the model to search for substitutions for the specified
     * substitution group head
     * @param substitutionGroupHead the element for which to search for
     * substitutable elements
     * @return all elements reachable from the provided model that specify the
     * provided element as their {@code substitutionGroup}
     */
    static Set<GlobalElement> resolveSubstitutions(SchemaModel model, GlobalElement substitutionGroupHead) {
        Schema startSchema = model.getSchema();
        Collection<Schema> schemasToSearch = getSelfAndReferencedSchemas(startSchema);

        Visitor visitor = new Visitor(substitutionGroupHead);
        for (Schema schema : schemasToSearch) {
            schema.accept(visitor);
        }
        return Collections.unmodifiableSet(visitor.substitutions);
    }

    private static Collection<Schema> getSelfAndReferencedSchemas(Schema startSchema) {
        Set<Schema> referencedSchemas = new HashSet<Schema>();
        referencedSchemas.add(startSchema);
        collectReferencedSchemas(startSchema, referencedSchemas);
        return referencedSchemas;
    }

    private static void collectReferencedSchemas(Schema start, Set<Schema> schemas) {
        Collection<SchemaModelReference> referencedSchemas = start.getSchemaReferences();
        for (SchemaModelReference reference : referencedSchemas) {
            try {
                SchemaModel referencedModel = reference.resolveReferencedModel();
                Schema referencedSchema = referencedModel.getSchema();
                // issue #212265; referencedSchema is sometimes null, reason not known (yet)
                if (referencedSchema != null && schemas.add(referencedSchema)) { //Don't add schema references more than once
                    collectReferencedSchemas(referencedSchema, schemas);
                }
            } catch (@SuppressWarnings("unused") CatalogModelException ex) {
                //Could not resolve schema reference
            }
        }
    }

    /**
     * A {@link SchemaVisitor visitor} to find all global elements specifying
     * the target element as their substitution group.
     */
    private static class Visitor extends DeepSchemaVisitor {

        private final Set<GlobalElement> substitutions = new LinkedHashSet<GlobalElement>();
        private final GlobalElement substitutionGroupBase;

        private Visitor(GlobalElement substitutionGroupBase) {
            this.substitutionGroupBase = substitutionGroupBase;
        }

        @Override
        public void visit(ElementReference element) {
            NamedComponentReference<GlobalElement> reference = element.getRef();
            if (!reference.isBroken()) {
                addIfInSubstitutionGroup(reference.get());
            }
            super.visit(element);
        }

        @Override
        public void visit(GlobalElement element) {
            addIfInSubstitutionGroup(element);
            super.visit(element);
        }

        private void addIfInSubstitutionGroup(GlobalElement potentialSubstitute) {
            NamedComponentReference<GlobalElement> substitutionGroupReference = potentialSubstitute.getSubstitutionGroup();
            boolean hasResolvableSubGroup = !(substitutionGroupReference == null || substitutionGroupReference.isBroken());
            if (hasResolvableSubGroup) {
                GlobalElement referencedSubstitutionGroup = substitutionGroupReference.get();
                if (substitutionGroupBase.equals(referencedSubstitutionGroup)) {
                    substitutions.add(potentialSubstitute);
                }
            }
        }
    }
}
