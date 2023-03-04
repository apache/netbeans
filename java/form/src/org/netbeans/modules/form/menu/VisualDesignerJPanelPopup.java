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

package org.netbeans.modules.form.menu;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.Popup;

/** A custom Popup container for menu items that doesn't use a real top level component.
 * Instead it uses a JPanel which is lightweight and can be put into the MenuEditLayer
 *
 * @author joshua.marinacci@sun.com
 */
class VisualDesignerJPanelPopup extends Popup {
    JComponent cont;
    JMenu menu;
    VisualDesignerPopupFactory fact;

    
    public VisualDesignerJPanelPopup(JComponent cont, JMenu menu, VisualDesignerPopupFactory fact) {
        this.cont = cont;
        this.menu = menu;
        this.fact = fact;
    }
    
    // when this menu is shown hide all of the other menus
    @Override
    public void show() {
        // hide all menus except this one
        fact.hideOtherMenus(menu);
        cont.setVisible(true);
    }

    @Override
    public void hide() {
        // This method probably will do something once someone brave
        // reviews/rewrites the menu designer. By now, it just fixes issue 122672.
    }
    
}
