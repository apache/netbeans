/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.bower.ui.options;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javascript.bower.options.BowerOptions;
import org.netbeans.modules.javascript.bower.options.BowerOptionsValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@NbBundle.Messages("BowerOptionsPanelController.name=Bower")
@OptionsPanelController.SubRegistration(
    location = BowerOptionsPanelController.OPTIONS_CATEGORY,
    id = BowerOptionsPanelController.OPTIONS_SUBCATEGORY,
    displayName = "#BowerOptionsPanelController.name" // NOI18N
)
public class BowerOptionsPanelController extends OptionsPanelController implements ChangeListener {

    public static final String OPTIONS_CATEGORY = "Html5"; // NOI18N
    public static final String OPTIONS_SUBCATEGORY = "Bower"; // NOI18N
    public static final String OPTIONS_PATH = OPTIONS_CATEGORY + "/" + OPTIONS_SUBCATEGORY; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // @GuardedBy("EDT")
    private BowerOptionsPanel bowerOptionsPanel;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    @Override
    public void update() {
        assert EventQueue.isDispatchThread();
        if (firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            getPanel().setBower(getBowerOptions().getBower());
            getPanel().setIgnoreBowerComponents(getBowerOptions().isIgnoreBowerComponents());
        }
        changed = false;
    }

    @Override
    public void applyChanges() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                getBowerOptions().setBower(getPanel().getBower());
                getBowerOptions().setIgnoreBowerComponents(getPanel().isIgnoreBowerComponents());
                changed = false;
            }
        });
    }

    @Override
    public void cancel() {
        if (isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            getPanel().setBower(getBowerOptions().getBower());
            getPanel().setIgnoreBowerComponents(getBowerOptions().isIgnoreBowerComponents());
        }
    }

    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        BowerOptionsPanel panel = getPanel();
        ValidationResult result = new BowerOptionsValidator()
                .validateBower(panel.getBower())
                .getResult();
        // errors
        if (result.hasErrors()) {
            panel.setError(result.getFirstErrorMessage());
            return false;
        }
        // warnings
        if (result.hasWarnings()) {
            panel.setWarning(result.getFirstWarningMessage());
            return true;
        }
        // everything ok
        panel.setError(" "); // NOI18N
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getBowerOptions().getBower();
        String current = getPanel().getBower().trim();
        if (saved == null ? !current.isEmpty() : !saved.equals(current)) {
            return true;
        }
        return getBowerOptions().isIgnoreBowerComponents() != getPanel().isIgnoreBowerComponents();
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        assert EventQueue.isDispatchThread();
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.javascript.bower.ui.options.BowerOptionsPanelController"); // NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!changed) {
            changed = true;
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private BowerOptionsPanel getPanel() {
        assert EventQueue.isDispatchThread();
        if (bowerOptionsPanel == null) {
            bowerOptionsPanel = new BowerOptionsPanel();
            bowerOptionsPanel.addChangeListener(this);
        }
        return bowerOptionsPanel;
    }

    private BowerOptions getBowerOptions() {
        return BowerOptions.getInstance();
    }

}
