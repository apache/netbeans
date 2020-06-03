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

package org.netbeans.modules.git.remote.ui.menu;

import javax.swing.JMenu;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator.ActionDestination;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Container menu for export actions.
 *
 */
public final class ShelveMenu extends DynamicMenu {
    private final Lookup lkp;
    private final VCSContext ctx;

    @NbBundle.Messages({
        "CTL_MenuItem_ShelveMenu.popup=Shelve Changes"
    })
    public ShelveMenu (ActionDestination dest, Lookup lkp, VCSContext ctx) {
        super(Bundle.CTL_MenuItem_ShelveMenu_popup());
        this.lkp = lkp;
        this.ctx = ctx;
    }

    @Override
    protected JMenu createMenu () {
        JMenu menu = new JMenu(this);
        //See bug #249105
        //for (JComponent item : ShelveUtils.getShelveMenuItems(ctx, lkp)) {
        //    if (item == null) {
        //        menu.addSeparator();
        //    } else {
        //        menu.add(item);
        //    }
        //}
        return menu;
    }
}
