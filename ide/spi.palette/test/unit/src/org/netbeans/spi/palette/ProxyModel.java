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
