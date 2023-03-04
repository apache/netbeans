/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.ws.qaf.designer;

import javax.swing.SwingUtilities;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.widgets.LabelWidgetOperator;
import org.netbeans.jellytools.widgets.WidgetOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.modules.websvc.design.view.widget.ButtonWidget;
import org.netbeans.modules.websvc.design.view.widget.ExpanderWidget;

/**
 *
 * @author lukas
 */
public final class WsDesignerUtilities {

    private static final String sourceLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.multiview.Bundle", "LBL_sourceView_name");
    private static final String designLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.multiview.Bundle", "LBL_designView_name");
    private static final String addOpLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.view.actions.Bundle", "LBL_AddOperation");
    private static final String gotoLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.view.actions.Bundle", "LBL_GotoSource");
    private static final String advancedLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.view.Bundle", "LBL_Wsit_Advanced");
    private static final String removeOpLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.view.actions.Bundle", "LBL_RemoveOperation");
    private static final String opsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.view.widget.Bundle", "LBL_Operations");

    private WsDesignerUtilities() {
    }

    public static void invokeAddOperation(String wsName) {
        final LabelWidgetOperator lwo = new LabelWidgetOperator(design(wsName), addOpLabel);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                lwo.clickMouse(1);
            }
        });
    }

    public static void invokeRemoveOperation(String wsName, String operationName, boolean usePopup) {
        TopComponentOperator tco = design(wsName);
        final LabelWidgetOperator lwo = new LabelWidgetOperator(tco, operationName);
        if (!usePopup) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    lwo.clickMouse(1);
                }
            });
            final LabelWidgetOperator lwo1 = new LabelWidgetOperator(tco, removeOpLabel);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    lwo1.clickMouse(1);
                }
            });
        } else {
            lwo.performPopupActionNoBlock(removeOpLabel);
        }
    }

    public static void invokeGoToSource(String wsName, String opName) {
        TopComponentOperator tco = design(wsName);
        final LabelWidgetOperator lwo = new LabelWidgetOperator(tco, opName);
        lwo.performPopupActionNoBlock(gotoLabel);
    }

    public static void invokeAdvanced(String wsName) {
        final LabelWidgetOperator lwo = new LabelWidgetOperator(design(wsName), advancedLabel);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                lwo.clickMouse(1);
            }
        });
    }

    /**
     *
     * @param wsName
     * @param opName
     * @param index button position
     */
    public static void clickOnButton(String wsName, String opName, int index) {
        TopComponentOperator tco = design(wsName);
        final LabelWidgetOperator lwo = new LabelWidgetOperator(tco, opName);
        Widget w = WidgetOperator.findWidget(lwo.getParent().getParent().getWidget(), new WidgetOperator.WidgetChooser() {

            public boolean checkWidget(Widget widget) {
                return widget instanceof ButtonWidget;
            }

            public String getDescription() {
                return "Button Chooser"; //NOI18N
            }
        }, index);
        final WidgetOperator wo = new WidgetOperator(w);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                wo.clickMouse(1);
            }
        });
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    public static void clickOnExpander(String wsName, String opName) {
        TopComponentOperator tco = design(wsName);
        final LabelWidgetOperator lwo = new LabelWidgetOperator(tco, opName);
        Widget w = WidgetOperator.findWidget(lwo.getParent().getParent().getWidget(), new WidgetOperator.WidgetChooser() {

            public boolean checkWidget(Widget widget) {
                return widget instanceof ExpanderWidget;
            }

            public String getDescription() {
                return "Expander Chooser"; //NOI18N
            }
        }, 0);
        final WidgetOperator wo = new WidgetOperator(w);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                wo.clickMouse(1);
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    public static TopComponentOperator design(String wsName) {
        TopComponentOperator tco = new TopComponentOperator(wsName);
        new JToggleButtonOperator(tco, designLabel).pushNoBlock();
        try {
            //slow down a bit
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            //ignore
        }
        return new TopComponentOperator(wsName);
    }

    public static TopComponentOperator source(String wsName) {
        TopComponentOperator tco = new TopComponentOperator(wsName);
        new JToggleButtonOperator(tco, sourceLabel).pushNoBlock();
        try {
            //slow down a bit
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            //ignore
        }
        return new TopComponentOperator(wsName);
    }

    public static int operationsCount(String wsName) {
        TopComponentOperator tco = design(wsName);
        final LabelWidgetOperator lwo = new LabelWidgetOperator(tco, opsLabel);
        String countSt = new LabelWidgetOperator(lwo.getParent(), 1).getLabel();
        return Integer.valueOf(countSt.substring(1, countSt.lastIndexOf(')'))); //NOI18N
    }
}
