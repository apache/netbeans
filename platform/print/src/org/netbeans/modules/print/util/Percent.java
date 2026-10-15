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
package org.netbeans.modules.print.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.03.13
 */
public final class Percent extends JComboBox implements ActionListener {

    public interface Listener {
        double getCustomValue(int index);
        void valueChanged(double value, int index);
        void invalidValue(String value);
    }

    public Percent(Listener listener, double initValue, int[] values, final int defaultCustomIndex, String[] customs, String toolTip) {
//out("<New> Percent: " + initValue);
        for (int value : values) {
            addItem(value + PERCENT);
        }
        if (customs != null) {
            for (String custom : customs) {
                addItem(custom);
            }
        }
        setToolTipText(toolTip);

        setEditable(true);
        myCustoms = customs;
        myListener = listener;

        JTextComponent editor = (JTextComponent) getEditor().getEditorComponent();
        InputMap inputMap = editor.getInputMap();
        ActionMap actionMap = editor.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke('+'), INCREASE);
        inputMap.put(KeyStroke.getKeyStroke('='), INCREASE);
        inputMap.put(KeyStroke.getKeyStroke('-'), DECREASE);
        inputMap.put(KeyStroke.getKeyStroke('_'), DECREASE);
        inputMap.put(KeyStroke.getKeyStroke('/'), NORMAL);
        inputMap.put(KeyStroke.getKeyStroke('*'), CUSTOM);

        actionMap.put(INCREASE, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                increaseValue();
            }
        });
        actionMap.put(DECREASE, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                decreaseValue();
            }
        });
        actionMap.put(NORMAL, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                normalValue();
            }
        });
        actionMap.put(CUSTOM, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                customValue(defaultCustomIndex);
            }
        });
        addActionListener(this);
        setValue(initValue);
        selectValue();
    }

    private void selectValue() {
        String text = getEditorItem();

        for (int i = 0; i < getItemCount(); i++) {
            if (text.equals(getItemAt(i))) {
                setSelectedIndex(i);
            }
        }
    }

    public boolean isCustomValue() {
        return getCustomIndex(getEditorItem()) != -1;
    }

    public String getEditorItem() {
        return getEditor().getItem().toString();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
//out();
        String value = getEditorItem();
//out("Action: " + value);
        int k = getCustomIndex(value);

        if (k != -1) {
//out("  it is custom");
            valueChanged(getCustomValue(k), k);
            return;
        }
        double percent = parsePercent(value);

        if (isValid(percent)) {
//out("  it's valid value");
            int i = getCustomIndex(percent);

            if (i != -1) {
                setValue(percent, i);
            }
            else {
                valueChanged(percent, i);
            }
        }
        else {
//out("  it's invalid value");
            myListener.invalidValue(value);
//out("    restore.");
            setText(myCurrentText);
        }
    }

    private int getCustomIndex(String value) {
        if (myCustoms == null) {
            return -1;
        }
        for (int i = 0; i < myCustoms.length; i++) {
            if (myCustoms[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public double getValue() {
        return myCurrentValue;
    }

    public void increaseValue() {
        if (myCurrentValue < THRESHOLD) {
            setValue(myCurrentValue + SUBTRAHEND);
        }
        else {
            setValue(myCurrentValue * FACTOR);
        }
    }

    public void decreaseValue() {
        if (myCurrentValue > THRESHOLD) {
            setValue(myCurrentValue / FACTOR);
        }
        else {
            setValue(myCurrentValue - SUBTRAHEND);
        }
    }

    public void normalValue() {
        setValue(1.0);
    }

    public void customValue(int index) {
        setValue(getCustomValue(index), index);
    }

    public void setValue(double value) {
//out("SET value: " + value);
        setValue(value, getCustomIndex(value));
    }

    private void setValue(double value, int index) {
//out("  set value: " + value + " " + index);
        String text = valueChanged(value, index);

        if (text != null) {
            setText(text);
        }
    }

    private String valueChanged(double value, int index) {
        boolean isCustomValue = index != -1;
//out("  value changed: " + value + " " + isCustomValue);

        if ( !isCustomValue && !isValid(value)) {
//out("  it's not valid value");
            return null;
        }
        myCurrentValue = value;
//out("myCurrentValue: " + myCurrentValue);

        if (isCustomValue && myCustoms != null) {
            myCurrentText = myCustoms[index];
        }
        else {
            myCurrentText = getPercent(value);
        }
//out("myCurrentText: " + myCurrentText);
        myListener.valueChanged(value, index);

        return myCurrentText;
    }

    private void setText(String text) {
        getEditor().setItem(text);
    }

    private int getCustomIndex(double value) {
        if (myCustoms == null) {
            return -1;
        }
//out("-- " + getPercent(value) + " " + getPercent(getCustomValue()));
        for (int i = 0; i < myCustoms.length; i++) {
            if (getPercent(value).equals(getPercent(getCustomValue(i)))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isValid(double value) {
//out("is valid: " + value);
        return MIN_VALUE <= value && value <= MAX_VALUE;
    }

    private double getCustomValue(int index) {
        return myListener.getCustomValue(index);
    }

    private double parsePercent(String text) {
        String value = text;

        if (value.endsWith(PERCENT)) {
            value = value.substring(0, value.length() - 1);
        }
        return getInt(value.trim()) / YUZ;
    }

    private String getPercent(double value) {
        return (int) Math.round(YUZ * value) + PERCENT;
    }

    public static double getZoomFactor(double zoom, double defaultValue) {
        if (0 < zoom && zoom <= MAX_VALUE) {
            return zoom;
        }
        return defaultValue;
    }

    public static int getZoomWidth(double zoom, int defaultValue) {
        if (BOUND_1 <= zoom && zoom < BOUND_2) {
            return round(zoom - BOUND_1);
        }
        return defaultValue;
    }

    public static int getZoomHeight(double zoom, int defaultValue) {
        if (BOUND_2 <= zoom) {
            return round(zoom - BOUND_2);
        }
        return defaultValue;
    }

    public static boolean isZoomFactor(double zoom) {
        return zoom < BOUND_1;
    }

    public static boolean isZoomWidth(double zoom) {
        return BOUND_1 <= zoom && zoom < BOUND_2;
    }

    public static boolean isZoomHeight(double zoom) {
        return BOUND_2 <= zoom;
    }

    public static boolean isZoomPage(double zoom) {
        return zoom == 0.0;
    }

    public static double createZoomWidth(double zoom) {
        return BOUND_1 + zoom;
    }

    public static double createZoomHeight(double zoom) {
        return BOUND_2 + zoom;
    }

    private Listener myListener;
    private String[] myCustoms;
    private String myCurrentText;
    private double myCurrentValue;
    private static final int BOUND_1 = 1000;
    private static final int BOUND_2 = 2000;
    private static final double THRESHOLD = 0.2;
    private static final double SUBTRAHEND = 0.01;
    private static final double MIN_VALUE = 0.01;
    private static final double MAX_VALUE = 15.0;
    private static final String CUSTOM = "custom";   // NOI18N
    private static final String NORMAL = "normal";   // NOI18N
    private static final String INCREASE = "increase"; // NOI18N
    private static final String DECREASE = "decrease"; // NOI18N
    private static final double YUZ = 100.0;
    private static final String PERCENT = "%"; // NOI18N
    public static final double FACTOR = 1.09;
}
