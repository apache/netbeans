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

package org.netbeans.modules.websvc.design.view;

import java.awt.Point;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit Bhate
 */
public class DesignViewPopupProvider implements PopupMenuProvider{
    
    private Action[] actions;
    /** 
     * Creates a new instance of DesignPopupProvider 
     * @param actions actions represented by this.
     */
    public DesignViewPopupProvider(Action[] actions) {
        this.actions = actions;
    }
    
    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        return Utilities.actionsToPopup(actions, 
                widget.getScene().getView().getComponentAt(point));
    }

}
