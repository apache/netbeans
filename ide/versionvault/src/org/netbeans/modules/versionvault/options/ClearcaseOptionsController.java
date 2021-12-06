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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault.options;

import java.io.File;
import org.netbeans.spi.options.OptionsPanelController;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.openide.util.Lookup;
import org.openide.util.HelpCtx;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Clearcase Options Controller.
 * 
 * @author Maros Sandor
 */
class ClearcaseOptionsController extends OptionsPanelController {
    
    private ClearcaseOptionsPanel panel;

    public void update() {
        setOdc(ClearcaseModuleConfig.getOnDemandCheckout());
        panel.taExecutable.setText(ClearcaseModuleConfig.getExecutablePath());
        panel.cbCheckinViewPrivate.setSelected(ClearcaseModuleConfig.getAddViewPrivate());
        panel.taLabelFormat.setText(ClearcaseModuleConfig.getLabelsFormat());
    }

    public void applyChanges() {
        if (!isValid()) return;
        ClearcaseModuleConfig.setOnDemandCheckout(getOdc());
        ClearcaseModuleConfig.putExecutablePath(panel.taExecutable.getText().trim());
        boolean refreshIgnored = isAddViewPrivateChanged();
        boolean refreshAnnotations = isLabelsFormatChanged();
        
        ClearcaseModuleConfig.putAddViewPrivate(panel.cbCheckinViewPrivate.isSelected());
        ClearcaseModuleConfig.putLabelsFormat(panel.taLabelFormat.getText().trim());
        
        if(refreshIgnored) {
            refreshIgnored();            
        }   
        if (refreshAnnotations) {
            Clearcase.getInstance().getAnnotator().refresh();
        }
    }

    public void cancel() {
    }

    public boolean isValid() {
        return true;
    }

    public boolean isChanged() {
        if (getOdc() != ClearcaseModuleConfig.getOnDemandCheckout()) return true;
        if (!panel.taExecutable.getText().trim().equals(ClearcaseModuleConfig.getExecutablePath())) return true;
        if (isAddViewPrivateChanged()) return true;
        if (isLabelsFormatChanged()) return true;        
        return false;
    }

    public ClearcaseModuleConfig.OnDemandCheckout getOdc() {
        if (panel.rbDisabled.isSelected()) return ClearcaseModuleConfig.OnDemandCheckout.Disabled;
        if (panel.rbPrompt.isSelected()) return ClearcaseModuleConfig.OnDemandCheckout.Prompt;
        if (panel.rbHijack.isSelected()) return ClearcaseModuleConfig.OnDemandCheckout.Hijack;
        if (panel.rbUnreserved.isSelected()) {
            return panel.cbHijackAfterUnreserved.isSelected() ? ClearcaseModuleConfig.OnDemandCheckout.UnreservedWithFallback : ClearcaseModuleConfig.OnDemandCheckout.Unreserved;
        }
        if (panel.cbFallback.isSelected()) {
            return panel.cbHijackAfterReserved.isSelected() ?  ClearcaseModuleConfig.OnDemandCheckout.ReservedWithBothFallbacks : ClearcaseModuleConfig.OnDemandCheckout.ReservedWithUnreservedFallback;
        } else {
            return panel.cbHijackAfterReserved.isSelected() ?  ClearcaseModuleConfig.OnDemandCheckout.ReservedWithHijackFallback : ClearcaseModuleConfig.OnDemandCheckout.Reserved;
        }
    }

    private boolean isAddViewPrivateChanged() {
        return panel.cbCheckinViewPrivate.isSelected() != ClearcaseModuleConfig.getAddViewPrivate();
    }

    private boolean isLabelsFormatChanged() {
        return !panel.taLabelFormat.getText().trim().equals(ClearcaseModuleConfig.getLabelsFormat());
    }

    private void setOdc(ClearcaseModuleConfig.OnDemandCheckout odc) {
        panel.rbDisabled.setSelected(odc == ClearcaseModuleConfig.OnDemandCheckout.Disabled);
        panel.rbPrompt.setSelected(odc == ClearcaseModuleConfig.OnDemandCheckout.Prompt);
        panel.rbHijack.setSelected(odc == ClearcaseModuleConfig.OnDemandCheckout.Hijack);
        panel.rbUnreserved.setSelected(odc == ClearcaseModuleConfig.OnDemandCheckout.Unreserved || odc == ClearcaseModuleConfig.OnDemandCheckout.UnreservedWithFallback);
        panel.rbReserved.setSelected(odc == ClearcaseModuleConfig.OnDemandCheckout.Reserved || odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithHijackFallback ||
            odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithBothFallbacks || odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithUnreservedFallback);
        panel.cbFallback.setSelected(odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithBothFallbacks || odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithUnreservedFallback);
        panel.cbHijackAfterReserved.setSelected(odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithBothFallbacks || odc == ClearcaseModuleConfig.OnDemandCheckout.ReservedWithHijackFallback);
    }
    
    public JComponent getComponent(Lookup lookup) {
        if (panel == null) {
            panel = new ClearcaseOptionsPanel(); 
        }
        return panel;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ClearcaseOptionsController.class);
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }
    
    private void refreshIgnored() {
        Utils.post(new Runnable() {
            public void run() {
                FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
                int includeStatus = ClearcaseModuleConfig.getAddViewPrivate() ? FileInformation.STATUS_NOTVERSIONED_IGNORED : FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
                File[] files = cache.listFiles(null, includeStatus);
                cache.refreshLater(files);
            }
        });
    }
    
}
