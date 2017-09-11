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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
     * @param index index of widget to be found
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
