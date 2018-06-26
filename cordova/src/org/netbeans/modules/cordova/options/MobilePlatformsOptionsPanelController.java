/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

@OptionsPanelController.SubRegistration(
        id = "MobilePlatforms", // NOI18N
        location = "Html5", // NOI18N
        displayName = "#AdvancedOption_DisplayName_MobilePlatforms", // NOI18N
        keywords = "#AdvancedOption_Keywords_MobilePlatforms", // NOI18N
        keywordsCategory = "Advanced/MobilePlatforms") // NOI18N
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_MobilePlatforms=Mobile Platforms",
    "AdvancedOption_Keywords_MobilePlatforms=mobile platform ios android phonegap cordova iphone ipad"
})
public final class MobilePlatformsOptionsPanelController extends OptionsPanelController {

    private MobilePlatformsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ChangeSupport cs = new ChangeSupport(this);
    private boolean changed;
    private static final RequestProcessor RP = new RequestProcessor(MobilePlatformsOptionsPanelController.class);

    @Override
    public void update() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                getPanel().load();
            }
        });
        changed = false;
    }

    @Override
    public void applyChanges() {
        getPanel().store();
        changed = false;
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }
    
    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.cordova.options.MobilePlatformsPanel");
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    

    private MobilePlatformsPanel getPanel() {
        if (panel == null) {
            panel = new MobilePlatformsPanel(this);
        }
        return panel;
    }

    void changed(boolean isChanged) {
        if (!changed) {
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        changed = isChanged;
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        cs.fireChange();
    }
}
