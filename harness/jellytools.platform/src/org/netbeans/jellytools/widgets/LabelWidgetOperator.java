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
package org.netbeans.jellytools.widgets;

import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Handle org.netbeans.api.visual.widget.LabelWidget object which represents 
 * widget with textual label.
 * <p>
 * Usage:<br>
 * <pre>
        TopComponentOperator tco = new TopComponentOperator("My scene");
        LabelWidgetOperator lwo0 = new LabelWidgetOperator(tco, "Label 0");
        lwo0.performPopupAction("An action");
        LabelWidgetOperator lwo1 = new LabelWidgetOperator(tco, "Label 1");
        // drag from one widget to another
        lwo0.dragNDrop(lwo1);
 * </pre>
 * 
 * @see WidgetOperator
 * @author Jiri Skrivanek
 */
public class LabelWidgetOperator extends WidgetOperator {

    /** Creates operator for given LabelWidget.
     * @param widget LabelWidget to create operator for
     */
    public LabelWidgetOperator(LabelWidget widget) {
        super(widget);
    }

    /** Waits for index-th LabelWidget under given parent.
     * @param parentWidgetOper parent WidgetOperator
     * @param index index of widget to be found
     */
    public LabelWidgetOperator(WidgetOperator parentWidgetOper, int index) {
        super(parentWidgetOper, new LabelWidgetChooser(), index);
    }

    /** Waits for LabelWidget with specified label under given parent.
     * @param parentWidgetOper parent WidgetOperator
     * @param label label of widget
     */
    public LabelWidgetOperator(WidgetOperator parentWidgetOper, String label) {
        this(parentWidgetOper, label, 0);
    }

    /** Waits for index-th LabelWidget with specified label under given parent.
     * @param parentWidgetOper parent WidgetOperator
     * @param label label of widget
     * @param index index of widget to be found
     */
    public LabelWidgetOperator(WidgetOperator parentWidgetOper, String label, int index) {
        super(parentWidgetOper, new LabelWidgetChooser(parentWidgetOper, label), index);
    }

    /** Waits for index-th LabelWidget with specified label under given TopComponent.
     * @param tco TopComponentOperator to find widgets in
     * @param index index of widget to be found
     */
    public LabelWidgetOperator(TopComponentOperator tco, int index) {
        super(tco, new LabelWidgetChooser(), index);
    }

    /** Waits for index-th LabelWidget with specified label under given TopComponent.
     * @param tco TopComponentOperator to find widgets in
     * @param label label of widget
     */
    public LabelWidgetOperator(TopComponentOperator tco, String label) {
        this(tco, label, 0);
    }

    /** Waits for index-th LabelWidget with specified label under given TopComponent.
     * @param tco TopComponentOperator to find widgets in
     * @param label label of widget
     * @param index index of widget to be found
     */
    public LabelWidgetOperator(TopComponentOperator tco, String label, int index) {
        super(tco, new LabelWidgetChooser(tco, label), index);
    }

    /** WidgetChooser to find LabelWidget with specified label. */
    private static final class LabelWidgetChooser implements WidgetChooser {

        private Operator operator;
        private String label;

        public LabelWidgetChooser(Operator operator, String label) {
            this.label = label;
            this.operator = operator;
        }

        /** Used to find LabelWidget without regards to label. */
        public LabelWidgetChooser() {
        }

        public boolean checkWidget(Widget widget) {
            if (widget instanceof LabelWidget) {
                if (label != null) {
                    String labelToCompare = ((LabelWidget) widget).getLabel();
                    return labelToCompare != null && operator.getComparator().equals(labelToCompare, label);
                }
                return true;
            }
            return false;
        }

        public String getDescription() {
            return "LabelWidget" + (label == null ? "" : " with label " + label);
        }
    }

    /** Returns label of this widget.
     * @return label of this widget.
     */
    public String getLabel() {
        return ((LabelWidget) widget).getLabel();
    }

    /** Returns class name of this widget, its location, bounds and label.
     * @return class name of this widget, its location, bounds and label.
     */
    @Override
    public String toString() {
        return super.toString() + ",label=" + getLabel();
    }
}
