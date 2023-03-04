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
