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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.versioning.util;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

// XXX could be replaced by a @OptionsPanelController.ContainerRegistration
// if OptionsDialog/Versioning could be used instead of VersioningOptionsDialog
// and a specialized GUI were not required

/**
 * Versioning options panel combined from panels for various settings.
 *
 * @author Pavel Buzek
 */
@OptionsPanelController.SubRegistration(
    id=VcsAdvancedOptions.ID,
    displayName="#LBL_OptionsPanelName",
    location="Team",
    keywords="#KW_VersioningOptions",
    keywordsCategory="Team/Versioning")
public class VcsAdvancedOptions extends OptionsPanelController {
    
    public static final String ID = "Versioning"; //NOI18N
    private VcsAdvancedOptionsPanel panel;
    private boolean initialized = false;
    private final Map<String, OptionsPanelController> categoryToController = new HashMap<String, OptionsPanelController>();

    private void init(Lookup masterLookup) {
        if (initialized) return;
        initialized = true;
        panel = new VcsAdvancedOptionsPanel();

        Lookup lookup = Lookups.forPath("VersioningOptionsDialog"); // NOI18N
        Iterator<? extends AdvancedOption> it = lookup.lookup(new Lookup.Template<AdvancedOption> (AdvancedOption.class)).
                allInstances().iterator();
        while (it.hasNext()) {
            AdvancedOption option = it.next();
            registerOption(option, masterLookup);
        }
    }

    private void registerOption (AdvancedOption option, Lookup masterLookup) {
        String category = option.getDisplayName();
        OptionsPanelController controller = option.create();
        synchronized (categoryToController) {
            categoryToController.put(category, controller);
        }
        panel.addPanel(category, controller.getComponent(masterLookup));
        if ("org.netbeans.modules.versioning.ui.options.GeneralAdvancedOption".equals(option.getClass().getName())) {
            panel.addPanel(category, controller.getComponent(masterLookup));
        }
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        init(masterLookup);
        return panel;
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void update() {
        for (OptionsPanelController c : getControllers()) {
            c.update();
        }
    }

    @Override
    public void applyChanges() {
        for (OptionsPanelController c : getControllers()) {
            c.applyChanges();
        }
    }

    @Override
    public void cancel() {
        for (OptionsPanelController c : getControllers()) {
            c.cancel();
        }
    }

    @Override
    public boolean isValid() {
        for (OptionsPanelController c : getControllers()) {
            if (!c.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isChanged() {
        for (OptionsPanelController c : getControllers()) {
            if (c.isChanged()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.versioning.util.VcsAdvancedOptions"); //NOI18N
    }

    @Override
    public void handleSuccessfulSearch (String searchText, List<String> matchedKeywords) {
        Map<String, OptionsPanelController> m;
        synchronized (categoryToController) {
            m = new HashMap<String, OptionsPanelController>(categoryToController);
        }
        for (Map.Entry<String, OptionsPanelController> e : m.entrySet()) {
            OptionsPanelController c = e.getValue();
            if (c instanceof VCSOptionsKeywordsProvider) {
                if (((VCSOptionsKeywordsProvider) c).acceptKeywords(matchedKeywords)) {
                    panel.selectCategory(e.getKey());
                    break;
                }
            }
        }
    }

    @Override
    protected void setCurrentSubcategory (String subpath) {
        Map<String, OptionsPanelController> m;
        synchronized (categoryToController) {
            m = new HashMap<String, OptionsPanelController>(categoryToController);
        }
        for (Map.Entry<String, OptionsPanelController> e : m.entrySet()) {
            if (e.getKey().equals(subpath)) {
                panel.selectCategory(e.getKey());
                break;
            }
        }
    }

    private OptionsPanelController[] getControllers () {
        synchronized (categoryToController) {
            return categoryToController.values().toArray(new OptionsPanelController[categoryToController.values().size()]);
        }
    }
}
