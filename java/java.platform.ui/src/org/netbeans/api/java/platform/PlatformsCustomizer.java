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
package org.netbeans.api.java.platform;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.*;

public final class PlatformsCustomizer {

    private PlatformsCustomizer () {

    }


    /**
     * Shows platforms customizer
     * @param  platform which should be selected, may be null
     * @return boolean for future extension, currently always true
     */
    public static boolean showCustomizer (JavaPlatform platform) {
        org.netbeans.modules.java.platform.ui.PlatformsCustomizer  customizer =
                new org.netbeans.modules.java.platform.ui.PlatformsCustomizer (platform);
        javax.swing.JButton close = new javax.swing.JButton(NbBundle.getMessage(PlatformsCustomizer.class,"CTL_Close"));
        close.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PlatformsCustomizer.class,"AD_Close"));
        DialogDescriptor descriptor = new DialogDescriptor (customizer,NbBundle.getMessage(PlatformsCustomizer.class,
                "TXT_PlatformsManager"), true, new Object[] {close},close,DialogDescriptor.DEFAULT_ALIGN, new HelpCtx (PlatformsCustomizer.class),null); // NOI18N
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog (descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null)
                dlg.dispose();
        }
        return true;
    }

}
