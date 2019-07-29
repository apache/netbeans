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

package org.netbeans.modules.gradle.javaee.api.ui.support;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.openide.util.NbBundle;

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
//        assert (defaultValue != null);
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
        if (defaultValue != null && defaultValue.equals(value)) {
            comboBoxLabel.setFont(comboBoxLabel.getFont().deriveFont(Font.PLAIN));
            comboBox.setToolTipText(null);
        } else {
            comboBoxLabel.setFont(comboBoxLabel.getFont().deriveFont(Font.BOLD));
            comboBox.setToolTipText(Bundle.MSG_ComboBox_Value_Changed());
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
