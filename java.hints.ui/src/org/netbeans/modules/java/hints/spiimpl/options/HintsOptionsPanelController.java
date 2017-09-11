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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.java.hints.spiimpl.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities.ClassPathBasedHintWrapper;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

//XXX: not finished!
public final class HintsOptionsPanelController extends OptionsPanelController {
    
    private HintsSettings settings;
    private HintsPanel panel;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
                    
    public void update() {
        if (panel != null) {
            panel.update(false);
            panel.setOverlayPreferences(settings, false);
        }
    }
    
    public void applyChanges() {
        if ( isChanged() ) {
            panel.applyChanges();
            FileHintPreferences.fireChange();
        }
    }
    
    public void cancel() {
        if (panel != null) {
            panel.cancel();
        }
    }
    
    public boolean isValid() {
        return true; 
    }
    
    public boolean isChanged() {
        return panel == null ? false : panel.isChanged();
    }
    
    public HelpCtx getHelpCtx() {
	return new HelpCtx("netbeans.optionsDialog.java.hints");
    }
    
    public synchronized HintsPanel getComponent(Lookup masterLookup) {
        Preferences prefs = null;
        if(masterLookup != null) {
            prefs = masterLookup.lookup(Preferences.class);
        }
        if (prefs != null) {
            settings = HintsSettings.createPreferencesBasedHintsSettings(prefs, true, null);
        } else {
            settings = null;
        }
        if (panel == null || settings != null) {
            panel = new HintsPanel(masterLookup.lookup(OptionsFilter.class), settings);
        }
        return panel;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }

    @Override
    protected void setCurrentSubcategory(String subpath) {
        for (HintMetadata hm : RulesManager.getInstance().readHints(null, null, null).keySet()) {
            if (hm.id.equals(subpath)) {
                HintsPanel c = getComponent(null);
                c.select(hm, true);
                return;
            }
        }

        Logger.getLogger(HintsOptionsPanelController.class.getName()).log(Level.WARNING, "setCurrentSubcategory: cannot find: {0}", subpath);
    }

    void changed() {
	if (!changed) {
	    changed = true;
	    pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
	}
	pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
    
}
