/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.OptionsPanelController;

/**
 * Common functionality of {@link OptionsPanelController}.
 */
abstract class BaseOptionsPanelController extends OptionsPanelController implements ChangeListener {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    volatile boolean changed = false;


    /**
     * Validate the panel (component).
     * @return {@code true} if the panel is valid, {@code false} otherwise
     */
    protected abstract boolean validateComponent();

    /**
     * @see OptionsPanelController#update()
     */
    protected abstract void updateInternal();

    /**
     * @see OptionsPanelController#applyChanges()
     */
    protected abstract void applyChangesInternal();
    
    /**
     * Determine if the panel is modified by the user. This will help the infrastructure
     * to enable or disable the Apply button in options window.
     * @return {@code true} if the panel is modified by the user through the UI, {@code false} otherwise
     */
    protected abstract boolean areOptionsChanged();

    @Override
    public final void update() {
        updateInternal();
        changed = false;
    }

    @Override
    public final void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                applyChangesInternal();
                changed = false;
            }
        });
    }

    @Override
    public final void cancel() {
        changed = false;
    }

    @Override
    public final boolean isValid() {
        return validateComponent();
    }

    @Override
    public final boolean isChanged() {
        return areOptionsChanged();
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public final void stateChanged(ChangeEvent e) {
        if (!changed) {
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    /**
     * Return interger or {@code null} if the input cannot be parsed.
     * @param input input to be parsed
     * @return interger or {@code null} if the input cannot be parsed
     */
    protected Integer parseInteger(String input) {
        Integer number = null;
        try {
            number = Integer.parseInt(input);
        } catch (NumberFormatException exc) {
            // ignored
        }
        return number;
    }

    /**
     * Get PHP options.
     * @return PHP options
     */
    protected final PhpOptions getPhpOptions() {
        return PhpOptions.getInstance();
    }

}
