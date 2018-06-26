/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.web.jsfapi.api.Library;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ImportData {

    public volatile boolean shouldShowNamespacesPanel;
    public volatile boolean isJsf22;

    private final List<DataItem> dataItems = new ArrayList<>();
    private final List<DataItem> dataItemsToReplace = new ArrayList<>();
    private final List<Attribute> dataToRemove = new ArrayList<>();


    public void add(DataItem item) {
        dataItems.add(item);
    }

    public void addToReplace(DataItem item) {
        dataItemsToReplace.add(item);
    }

    public void addToRemove(Attribute item) {
        dataToRemove.add(item);
    }

    public List<DataItem> getItems() {
        return new ArrayList<>(dataItems);
    }

    public List<DataItem> getItemsToReplace() {
        return new ArrayList<>(dataItemsToReplace);
    }

    public List<Attribute> getItemsToRemove() {
        return new ArrayList<>(dataToRemove);
    }

    public List<VariantItem> getDefaultVariants() {
        List<VariantItem> result = new ArrayList<>();
        for (DataItem dataItem : dataItems) {
            result.add(dataItem.getDefaultVariant());
        }
        return result;
    }

    public static class DataItem {

        private final String prefix;
        private final List<VariantItem> variants;
        private final VariantItem defaultVariant;

        public DataItem(String prefix, List<VariantItem> variants, VariantItem defaultVariant) {
            this.prefix = prefix;
            this.variants = variants;
            this.defaultVariant = defaultVariant;
        }

        public String getTypeName() {
            return prefix;
        }

        public List<VariantItem> getVariants() {
            return new ArrayList<>(variants);
        }

        public VariantItem getDefaultVariant() {
            return defaultVariant;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.prefix);
            hash = 97 * hash + Objects.hashCode(this.variants);
            hash = 97 * hash + Objects.hashCode(this.defaultVariant);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DataItem other = (DataItem) obj;
            if (!Objects.equals(this.prefix, other.prefix)) {
                return false;
            }
            if (!Objects.equals(this.variants, other.variants)) {
                return false;
            }
            if (!Objects.equals(this.defaultVariant, other.defaultVariant)) {
                return false;
            }
            return true;
        }
    }

    public static class VariantItem {

        private final String prefix;
        private final String namespace;
        private final Library library;

        public VariantItem(String prefix, String namespace, Library library) {
            this.prefix = prefix;
            this.namespace = namespace;
            this.library = library;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getNamespace() {
            return namespace;
        }

        public Library getLibrary() {
            return library;
        }

        @Override
        public String toString() {
            return getNamespace();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + Objects.hashCode(this.prefix);
            hash = 67 * hash + Objects.hashCode(this.namespace);
            hash = 67 * hash + Objects.hashCode(this.library);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final VariantItem other = (VariantItem) obj;
            if (!Objects.equals(this.prefix, other.prefix)) {
                return false;
            }
            if (!Objects.equals(this.namespace, other.namespace)) {
                return false;
            }
            if (!Objects.equals(this.library, other.library)) {
                return false;
            }
            return true;
        }

    }
}
