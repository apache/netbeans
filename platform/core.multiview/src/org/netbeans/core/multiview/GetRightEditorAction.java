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

package org.netbeans.core.multiview;

import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import java.beans.PropertyChangeListener;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;


/**
 *
 * @author  Milos Kleint
 */
public class GetRightEditorAction extends AbstractAction {

    public GetRightEditorAction() {
        putValue(Action.NAME, NbBundle.getMessage(GetRightEditorAction.class, "GetRightEditorAction.name"));
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        WindowManager wm = WindowManager.getDefault();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(wm.getRegistry().getActivated());
        if (handler != null) {
            MultiViewPerspective pers = handler.getSelectedPerspective();
            MultiViewPerspective[] all = handler.getPerspectives();
            for (int i = 0; i < all.length; i++) {
                if (pers.equals(all[i])) {
                    int newIndex = i != all.length  - 1 ? i + 1 : 0;
		    MultiViewDescription selectedDescr = Accessor.DEFAULT.extractDescription(pers);
		    if (selectedDescr instanceof ContextAwareDescription) {
			if (((ContextAwareDescription) selectedDescr).isSplitDescription()) {
			    newIndex = i != all.length  - 1 ? i + 2 : 1;
			} else {
			    newIndex = i != all.length  - 2 ? i + 2 : 0;
			}
		    }
                    handler.requestActive(all[newIndex]);
		    break;
                }
            }
        } else {
            Utilities.disabledActionBeep();
        }
    }
    
}
