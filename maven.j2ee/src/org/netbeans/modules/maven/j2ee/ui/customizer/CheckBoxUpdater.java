/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ui.customizer;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.j2ee.ui.customizer.Bundle.*;

/**
 * Decorator class which enhanced normal {@link JCheckBox} with ability to store current value.
 * <p>
 *
 * Clients needs to provide implementation of the {@link Store} interface so the updater will
 * know how exactly should it store the check box value when {@link CheckBoxUpdater#storeValue()}
 * is called.
 * <p>
 *
 * Clients are not supposed to create an instance directly using constructor but they should
 * use {@link CheckBoxUpdater#create(javax.swing.JCheckBox, boolean, org.netbeans.modules.maven.j2ee.ui.customizer.CheckBoxUpdater.Store) instead.
 * <p>
 *
 * This class is <i>immutable</i> and thus thread safe.
 *
 * @see ComboBoxUpdater
 * @see Store
 * @see Verify
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public final class CheckBoxUpdater implements ItemListener {


    /**
     * Interface used by the updater to store current check box value.
     */
    public interface Store {

        /**
         * Stores value given by the parameter.
         * <p>
         *
         * Please be aware that this method will gets called only in case if the
         * value have been changed to something different than the default one.
         *
         * @param value to store
         */
        void storeValue(boolean value);
    }

    /**
     * Interface used by the updater to verify if the new value can be stored in the combo box.
     * <p>
     *
     * Please be aware that if this method returns {@code false}, the {@link Store#storeValue(java.lang.Object)}
     * won't be called.
     */
    public interface Verify {

        /**
         * Verifies value given by the parameter.
         *
         * @param value to be verified
         * @return {@code true} if the value is OK, {@code false} otherwise
         */
        boolean verifyValue(boolean value);
    }


    private final Store store;
    private final Verify verifier;
    private final JCheckBox checkBox;
    private final boolean defaultValue;


    /**
     * Creates an instance of the {@link CheckBoxUpdater}.
     *
     * The check box will be initialized using the given default value. If the client
     * want to store current value, {@link CheckBoxUpdater#storeValue()} method has to
     * be called.
     *
     * @param checkBox we want to decorate
     * @param defaultValue default value for the check box
     * @param store implementation used to store check box value
     * @return instance of the {@link CheckBoxUpdater}
     */
    public static CheckBoxUpdater create(JCheckBox checkBox, boolean defaultValue, Store store) {
        return create(checkBox, defaultValue, store, null);
    }

    /**
     * Creates an instance of the {@link CheckBoxUpdater}.
     *
     * <p>
     * The check box will be initialized using the given default value. If the client
     * want to store current value, {@link CheckBoxUpdater#storeValue()} method has to
     * be called.
     *
     * <p>
     * For the value verification, pass instance of {@link CheckBoxUpdater.Verify} which
     * will be called before the new value will be set into the check box.
     *
     * @param checkBox we want to decorate
     * @param defaultValue default value for the check box
     * @param store implementation used to store check box value
     * @param verifier implementation used to verify new check box value
     * @return instance of the {@link CheckBoxUpdater}
     */
    public static CheckBoxUpdater create(JCheckBox checkBox, boolean defaultValue, Store store, Verify verifier) {
        CheckBoxUpdater instance = new CheckBoxUpdater(checkBox, defaultValue, store, verifier);
        instance.checkBox.addItemListener(instance);

        return instance;
    }

    private CheckBoxUpdater(JCheckBox checkBox, boolean defaultValue, Store store, Verify verifier) {
        assert (checkBox != null);
        assert (store != null);

        this.checkBox = checkBox;
        this.checkBox.setSelected(defaultValue);
        this.defaultValue = defaultValue;
        this.store = store;
        this.verifier = verifier;
        setValue(defaultValue);
    }

    /**
     * Stores the current value of the decorated check box.
     */
    public void storeValue() {
        boolean currentValue = checkBox.isSelected();
        if (currentValue != defaultValue) {
            store.storeValue(currentValue);
        }
    }


    @Override
    public void itemStateChanged(ItemEvent e) {
        if (ItemEvent.SELECTED == e.getStateChange() || ItemEvent.DESELECTED == e.getStateChange()) {
            boolean isSelected = checkBox.isSelected();
            if (verifier != null) {
                if (verifier.verifyValue(isSelected)) {
                    setValue(isSelected);
                }
            } else {
                // If there is no defined verifier, simply change the value
                setValue(isSelected);
            }
        }
    }

    @NbBundle.Messages("MSG_CheckBox_Value_Changed=This value had been changed from the default one")
    private void setValue(boolean value) {
        // We don't want to notify listeners about state changes made in this method
        ItemListener[] itemListeners = checkBox.getItemListeners();
        for (ItemListener listener : itemListeners) {
            checkBox.removeItemListener(listener);
        }

        if (defaultValue == value) {
            checkBox.setFont(checkBox.getFont().deriveFont(Font.PLAIN));
            checkBox.setToolTipText(null);
        } else {
            checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD));
            checkBox.setToolTipText(MSG_CheckBox_Value_Changed());
        }
        checkBox.setSelected(value);

        // Register listeners back to have them consistent
        for (ItemListener listener : itemListeners) {
            checkBox.addItemListener(listener);
        }
    }
}
