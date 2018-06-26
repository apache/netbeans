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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.j2ee.ui.customizer.Bundle.*;

/**
 * Decorator class which enhanced normal {@link JComboBox} with ability to store current value.
 * <p>
 *
 * Clients needs to provide implementation of the {@link Store} interface so the updater will
 * know how exactly should it store the combo box value when {@link ComboBoxUpdater#storeValue()}
 * is called.
 * <p>
 *
 * Clients are not supposed to create an instance directly using constructor but they should
 * use one of the factory method instead.
 * <p>
 *
 * This class is <i>immutable</i> and thus thread safe.
 *
 * @see CheckBoxUpdater
 * @see Store
 * @see Verify
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public final class ComboBoxUpdater<T> implements ItemListener {

    /**
     * Interface used by the updater to store current combo box value.
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
        void storeValue(Object value);
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
        boolean verifyValue(Object value);
    }


    private final Store store;
    private final Verify verifier;
    private final JComboBox<T> comboBox;
    private final JLabel comboBoxLabel;
    private final T defaultValue;


    /**
     * Creates an instance of the {@link ComboBoxUpdater}.
     *
     * <p>
     * The combo box will be initialized using the given default value, but the model needs to be set
     * before the {@link ComboBoxUpdater#create(JComboBox, java.lang.Object, ComboBoxUpdater.Store)}
     * method gets called.
     *
     * <p>
     * If the client want to store current value, {@link ComboBoxUpdater#storeValue()}
     * method should be used.
     *
     * @param comboBox we want to decorate
     * @param comboBoxLabel label before the combo box
     * @param defaultValue default value for the check box
     * @param store implementation used to store check box value
     * @return instance of the {@link ComboBoxUpdater}
     */
    public static <T> ComboBoxUpdater create(JComboBox<T> comboBox, JLabel comboBoxLabel, T defaultValue, Store store) {
        ComboBoxUpdater instance = new ComboBoxUpdater(comboBox, comboBoxLabel, defaultValue, store, null);
        instance.comboBox.addItemListener(instance);

        return instance;
    }

    /**
     * Creates an instance of the {@link ComboBoxUpdater}.
     *
     * <p>
     * The combo box will be initialized using the given default value, but the model needs to be set
     * before the {@link ComboBoxUpdater#create(JComboBox, java.lang.Object, ComboBoxUpdater.Store)}
     * method gets called.
     *
     * <p>
     * If the client want to store current value, {@link ComboBoxUpdater#storeValue()}
     * method should be used.
     *
     * <p>
     * For the value verification, pass instance of {@link ComboBoxUpdater.Verify} which will be called
     * before the new value will be set in combo box model.
     *
     * @param comboBox we want to decorate
     * @param comboBoxLabel label before the combo box
     * @param defaultValue default value for the check box
     * @param store implementation used to store check box value
     * @param verifier implementation used to verify new check box value
     * @return instance of the {@link ComboBoxUpdater}
     */
    public static <T> ComboBoxUpdater create(JComboBox<T> comboBox, JLabel comboBoxLabel, T defaultValue, Store store, Verify verifier) {
        ComboBoxUpdater instance = new ComboBoxUpdater(comboBox, comboBoxLabel, defaultValue, store, verifier);
        instance.comboBox.addItemListener(instance);

        return instance;
    }

    private ComboBoxUpdater(JComboBox<T> comboBox, JLabel comboBoxLabel, T defaultValue, Store store, Verify verifier) {
        assert (comboBox != null);
        assert (comboBoxLabel != null);
        assert (defaultValue != null);
        assert (store != null);

        this.comboBox = comboBox;
        this.comboBox.setSelectedItem(defaultValue);
        this.comboBoxLabel = comboBoxLabel;
        this.defaultValue = defaultValue;
        this.store = store;
        this.verifier = verifier;
        setValue(defaultValue);
    }

    /**
     * Stores the current value of the decorated combo box.
     */
    public void storeValue() {
        Object selectedItem = comboBox.getSelectedItem();
        if (!selectedItem.equals(defaultValue)) {
            store.storeValue(selectedItem);
        }
    }

    @NbBundle.Messages("MSG_ComboBox_Value_Changed=This value had been changed from the default one")
    private void setValue(Object value) {
        if (defaultValue.equals(value)) {
            comboBoxLabel.setFont(comboBoxLabel.getFont().deriveFont(Font.PLAIN));
            comboBox.setToolTipText(null);
        } else {
            comboBoxLabel.setFont(comboBoxLabel.getFont().deriveFont(Font.BOLD));
            comboBox.setToolTipText(MSG_ComboBox_Value_Changed());
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        // The method is called twice for each item-change (once as SELECTED, for
        // the second time as DESELECTED) and we want to propagate only one event
        if (ItemEvent.SELECTED == e.getStateChange()) {
            Object newValue = comboBox.getSelectedItem();
            if (verifier != null) {
                if (verifier.verifyValue(newValue)) {
                    setValue(newValue);
                }
            } else {
                // If there is no defined verifier, simply change the value
                setValue(newValue);
            }
        }
    }
}
