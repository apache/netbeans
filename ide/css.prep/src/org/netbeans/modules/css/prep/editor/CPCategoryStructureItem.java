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
package org.netbeans.modules.css.prep.editor;

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
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class CPCategoryStructureItem implements StructureItem {

    //let the CP items to be at the top of the navigator
    private static final String SORT_TEXT_PREFIX = "0_"; //NOI18N
    
    @Override
    public String getSortText() {
        return new StringBuilder().append(SORT_TEXT_PREFIX).append(getName()).toString();
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


    public abstract static class ChildrenSetStructureItem extends CPCategoryStructureItem {

        private Collection<StructureItem> items;

        public ChildrenSetStructureItem(Collection<StructureItem> items) {
            this.items = items;
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return new ArrayList<>(items);
        }
    }

    @NbBundle.Messages("navigator.item.name.variables=Variables")
    public static class Variables extends ChildrenSetStructureItem {

        private static final ImageIcon ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/prep/editor/resources/variables.gif")); //NOI18N
        
        public Variables(Set<StructureItem> children) {
            super(children);
        }

        @Override
        public ImageIcon getCustomIcon() {
            return ICON;
        }
        
        @Override
        public String getName() {
            return Bundle.navigator_item_name_variables();
        }
    }

    @NbBundle.Messages("navigator.item.name.mixins=Mixins")
    public static class Mixins extends ChildrenSetStructureItem {

        private static final ImageIcon ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/prep/editor/resources/methods.gif")); //NOI18N
        
        private FeatureContext context;

        public Mixins(Collection<StructureItem> items, FeatureContext context) {
            super(items);
            this.context = context;
        }

        @Override
        public ImageIcon getCustomIcon() {
            return ICON;
        }

        @Override
        public String getName() {
            return Bundle.navigator_item_name_mixins();
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
            Snapshot s = context.getSnapshot();
            return s.getOriginalOffset(s.getText().length());
        }
    }
}
