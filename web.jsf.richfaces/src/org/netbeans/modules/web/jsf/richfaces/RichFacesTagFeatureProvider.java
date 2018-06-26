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
package org.netbeans.modules.web.jsf.richfaces;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.api.TagFeature;
import org.netbeans.modules.web.jsfapi.spi.TagFeatureProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link TagFeatureProvider} for RichFaces.
 *
 * @author petrpodzimek
 */
@ServiceProvider(service = TagFeatureProvider.class)
public class RichFacesTagFeatureProvider implements TagFeatureProvider {

    private static final String RICHFACES_RICH_NAMESPACE = "http://richfaces.org/rich";
    private static final String RICHFACES_A4J_NAMESPACE = "http://richfaces.org/a4j";
    
    private static final String VAR = "var";
    private static final String VALUE = "value";

    @Override
    public <T extends TagFeature> Collection<T> getFeatures(final Tag tag, Library library, Class<T> clazz) {
        if (clazz.equals(TagFeature.IterableTagPattern.class)) {
            final RichFacesTagFeatureProvider.IterableTag iterableTag = resolveIterableTag(library, tag);
            if (iterableTag != null) {
                return Collections.singleton(clazz.cast(new TagFeature.IterableTagPattern() {
                    @Override
                    public Attribute getVariable() {
                        return tag.getAttribute(iterableTag.getVariableAtribute());
                    }

                    @Override
                    public Attribute getItems() {
                        return tag.getAttribute(iterableTag.getItemsAtribute());
                    }
                }));
            }

        }
        return Collections.emptyList();
    }

    private RichFacesTagFeatureProvider.IterableTag resolveIterableTag(Library library, Tag tag) {
        for (RichFacesTagFeatureProvider.IterableTag iterableTag : RichFacesTagFeatureProvider.IterableTag.values()) {
            if (library.getNamespace() != null
                    && iterableTag.getNamespace() != null
                    && library.getNamespace().equalsIgnoreCase(iterableTag.getNamespace())) {
                return iterableTag;
            }
        }
        return null;
    }

    private enum IterableTag {

        A4J_REPEAT(RICHFACES_A4J_NAMESPACE, "repeat", VALUE, VAR),
        RICH_DATAGRID(RICHFACES_RICH_NAMESPACE, "dataGrid", VALUE, VAR),
        RICH_DATA_TABLE(RICHFACES_RICH_NAMESPACE, "dataTable", VALUE, VAR),
        RICH_EXTENDED_DATA_TABLE(RICHFACES_RICH_NAMESPACE, "extendedDataTable", VALUE, VAR),
        RICH_LIST(RICHFACES_RICH_NAMESPACE, "list", VALUE, VAR),
        RICH_ORDERING_LIST(RICHFACES_RICH_NAMESPACE, "orderingList", VALUE, VAR),
        RICH_PICK_LIST(RICHFACES_RICH_NAMESPACE, "pickList", VALUE, VAR),
        RICH_TREE(RICHFACES_RICH_NAMESPACE, "tree", VALUE, VAR);
        
        private final String namespace;
        private final String name;
        private final String itemsAtribute;
        private final String variableAtribute;

        private IterableTag(String namespace, String name, String itemsAtribute, String variableAtribute) {
            this.namespace = namespace;
            this.name = name;
            this.itemsAtribute = itemsAtribute;
            this.variableAtribute = variableAtribute;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getName() {
            return name;
        }

        public String getItemsAtribute() {
            return itemsAtribute;
        }

        public String getVariableAtribute() {
            return variableAtribute;
        }
    }
}
