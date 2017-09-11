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
 * License. When distributing the software, include this License Header
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
