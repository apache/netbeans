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
package org.netbeans.modules.php.zend2.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.zend2.options.Zend2Options;
import org.netbeans.modules.php.zend2.validation.OptionsValidator;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Zend 2 IDE Options.
 */
@UiUtils.PhpOptionsPanelRegistration(
    id=Zend2OptionsPanelController.ID,
    displayName="#Zend2OptionsPanelController.options.name",
//    toolTip="#LBL_OptionsTooltip"
    position=299
)
public class Zend2OptionsPanelController extends OptionsPanelController implements ChangeListener {

    static final String ID = "Zend2"; // NOI18N
    public static final String OPTIONS_SUBPATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH+"/"+ID; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private Zend2OptionsPanel zend2OptionsPanel = null;
    private volatile boolean changed = false;
    private boolean firstOpening = true;


    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUBPATH; // NOI18N
    }

    @Override
    public void update() {
        if(firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            zend2OptionsPanel.setSkeleton(getOptions().getSkeleton());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        getOptions().setSkeleton(zend2OptionsPanel.getSkeleton());

        changed = false;
    }

    @Override
    public void cancel() {
        if(isChanged()) { // if panel is modified by the user and options window closes, discard any changes
            zend2OptionsPanel.setSkeleton(getOptions().getSkeleton());
        }
    }

    @Override
    public boolean isValid() {
        // clean up
        zend2OptionsPanel.setError(" "); // NOI18N

        // validate
        ValidationResult validationResult = new OptionsValidator()
                .validate(zend2OptionsPanel.getSkeleton())
                .getResult();
        String warning = null;
        // get first message
        if (validationResult.hasErrors()) {
            for (ValidationResult.Message message : validationResult.getErrors()) {
                warning = message.getMessage();
                break;
            }
        } else if (validationResult.hasWarnings()) {
            for (ValidationResult.Message message : validationResult.getWarnings()) {
                warning = message.getMessage();
                break;
            }
        }
        zend2OptionsPanel.setWarning(warning);

        // everything ok
        return true;
    }

    @Override
    public boolean isChanged() {
        String saved = getOptions().getSkeleton();
        String current = zend2OptionsPanel.getSkeleton().trim();
        return (saved == null ? !current.isEmpty() : !saved.equals(current));
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (zend2OptionsPanel == null) {
            zend2OptionsPanel = new Zend2OptionsPanel();
            zend2OptionsPanel.addChangeListener(this);
        }
        return zend2OptionsPanel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.zend2.options.Zend2Options"); // NOI18N
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

    private Zend2Options getOptions() {
        return Zend2Options.getInstance();
    }

}
