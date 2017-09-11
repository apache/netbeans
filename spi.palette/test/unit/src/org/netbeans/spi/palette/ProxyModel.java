/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.spi.palette;
import javax.swing.Action;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.ModelListener;
import org.openide.util.Lookup;



/**
 *
 * @author Stanislav Aubrecht
 */
public class ProxyModel implements Model {

    boolean showCustomizerCalled = false;
    private Model original;

    /** Creates a new instance of DummyModel */
    public ProxyModel( Model original ) {
        this.original = original;
    }

    public void showCustomizer(PaletteController pc, org.netbeans.modules.palette.Settings settings) {
        showCustomizerCalled = true;
        //super.showCustomizer(settings);
    }

    public void addModelListener(ModelListener listener) {
        original.addModelListener( listener );
    }

    public void removeModelListener(ModelListener listener) {
        original.removeModelListener( listener );
    }

    public boolean moveCategory( Category source, Category target, boolean moveBefore ) {
        return original.moveCategory( source, target, moveBefore );
    }

    public void refresh() {
        original.refresh();
    }

    public Action[] getActions() {
        return original.getActions();
    }

    public Category[] getCategories() {
        return original.getCategories();
    }

    public String getName() {
        return original.getName();
    }

    public Lookup getRoot() {
        return original.getRoot();
    }

    public Category getSelectedCategory() {
        return original.getSelectedCategory();
    }

    public Item getSelectedItem() {
        return original.getSelectedItem();
    }

    public void setSelectedItem(Lookup category, Lookup item) {
        original.setSelectedItem( category, item );
    }

    public void clearSelection() {
        original.clearSelection();
    }

    public boolean canReorderCategories() {
        return original.canReorderCategories();
    }
}
