/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.editor.module.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class TopLevelStructureItem implements StructureItem {

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        return getName();
    }

    @Override
    public ElementHandle getElementHandle() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PACKAGE; //xxx fix - add mode categories to csl
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public long getPosition() {
        return -1;
    }

    @Override
    public long getEndPosition() {
        return -1;
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    public abstract static class ChildrenSetStructureItem extends TopLevelStructureItem {

        private Collection<StructureItem> items;

        public ChildrenSetStructureItem(Collection<StructureItem> items) {
            this.items = items;
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return new ArrayList<>(items);
        }
    }

    public abstract static class ChildrenListStructureItem extends TopLevelStructureItem {

        private List<StructureItem> items;

        public ChildrenListStructureItem(List<StructureItem> items) {
            this.items = items;
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return items;
        }
    }

    public static class Rules extends ChildrenListStructureItem {

        private final Snapshot snapshot;

        public Rules(List<StructureItem> children, FeatureContext context) {
            super(children);
            this.snapshot = context.getSnapshot();
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(TopLevelStructureItem.class, "Rules"); //NOI18N
        }

        //return the element range 0 - source lenght to ensure the recursive
        //leaf node search mechanism in CSL navigator will try to match
        //the rule children
        @Override
        public long getPosition() {
            return 0;
        }

        @Override
        public long getEndPosition() {
            return snapshot.getOriginalOffset(snapshot.getText().length());
        }
    }

    public static class Elements extends ChildrenSetStructureItem {

        public Elements(Collection<StructureItem> items) {
            super(items);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(TopLevelStructureItem.class, "Elements"); //NOI18N
        }
    }

    public static class Classes extends ChildrenSetStructureItem {

        public Classes(Collection<StructureItem> children) {
            super(children);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(TopLevelStructureItem.class, "Classes"); //NOI18N
        }
    }

    public static class Ids extends ChildrenSetStructureItem {

        public Ids(Collection<StructureItem> children) {
            super(children);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(TopLevelStructureItem.class, "Ids"); //NOI18N
        }
    }

    public static class Namespaces extends ChildrenListStructureItem {

        public Namespaces(List<StructureItem> children) {
            super(children);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(TopLevelStructureItem.class, "Namespaces"); //NOI18N
        }
    }

    public static class AtRules extends ChildrenListStructureItem {

        public AtRules(List<StructureItem> children) {
            super(children);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(TopLevelStructureItem.class, "AtRules"); //NOI18N
        }
    }

    @NbBundle.Messages("imports=Imports")
    public static class Imports extends ChildrenSetStructureItem {

        public Imports(Collection<StructureItem> children) {
            super(children);
        }

        @Override
        public String getName() {
            return Bundle.imports();
        }
    }
}
