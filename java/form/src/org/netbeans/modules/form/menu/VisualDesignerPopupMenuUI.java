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

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.plaf.PopupMenuUI;

/** A custom PopupMenuUI which uses our special hacked popup factory.
 * We use this rather than replacing the global popup factory so that it won't
 * affect NetBeans itself.
 *
 * @author joshua.marinacci@sun.com
 */
class VisualDesignerPopupMenuUI extends PopupMenuUI {
    private final MenuEditLayer layer;

    PopupMenuUI ui;
    public VisualDesignerPopupMenuUI(MenuEditLayer layer, PopupMenuUI ui) {
        this.layer = layer;
        this.ui = ui;
    }
    
    @Override
    public boolean isPopupTrigger(MouseEvent e) {
        return ui.isPopupTrigger(e);
    }
    @Override
    public Popup getPopup(JPopupMenu popup, int x, int y) {
        PopupFactory popupFactory = layer.hackedPopupFactory;
        if(popupFactory == null) {
            return super.getPopup(popup, x, y);
        }
        return popupFactory.getPopup(popup.getInvoker(), popup, x, y);
    }
}
