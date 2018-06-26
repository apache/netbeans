/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
