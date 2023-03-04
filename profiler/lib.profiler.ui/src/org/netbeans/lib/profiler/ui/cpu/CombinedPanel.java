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

package org.netbeans.lib.profiler.ui.cpu;

import java.awt.Color;
import org.netbeans.lib.profiler.results.ExportDataDumper;
import org.netbeans.lib.profiler.ui.UIUtils;
import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;


/**
 *
 * @author Jaroslav Bachorik
 */
public class CombinedPanel extends JSplitPane implements ScreenshotProvider {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CombinedPanel(int splitOrientation, Component component1, Component component2) {
        super(splitOrientation, component1, component2);
        tweakUI();
    }

    public void exportData(int exportedFileType, ExportDataDumper eDD, String viewName) {
        ((CCTDisplay)leftComponent).exportData(exportedFileType, eDD, true, viewName);
        ((SnapshotFlatProfilePanel)rightComponent).exportData(exportedFileType, eDD, true, viewName);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public BufferedImage getCurrentViewScreenshot(boolean onlyVisible) {
        return UIUtils.createScreenshot(this);
    }

    public String getDefaultViewName() {
        return "cpu-combined"; // NOI18N
    }

    public boolean fitsVisibleArea() {
        return true;
    }
    
    private void tweakUI() {
        setBorder(null);
        setDividerSize(5);

        if (!(getUI() instanceof BasicSplitPaneUI)) return;

        BasicSplitPaneDivider divider = ((BasicSplitPaneUI)getUI()).getDivider();
        if (divider != null) {
            Color c = UIUtils.isNimbus() ? UIUtils.getDisabledLineColor() :
                    new JSeparator().getForeground();
            divider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, c));
        }
    }
}
