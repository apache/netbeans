/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
