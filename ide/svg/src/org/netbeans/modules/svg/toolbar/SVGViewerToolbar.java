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
package org.netbeans.modules.svg.toolbar;

import java.awt.Color;
import org.netbeans.modules.svg.toolbar.actions.CustomZoomAction;
import org.netbeans.modules.svg.toolbar.actions.ZoomInAction;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import org.netbeans.modules.svg.BackgroundMode;
import org.netbeans.modules.svg.SVGViewerElement;
import org.netbeans.modules.svg.toolbar.actions.ZoomOutAction;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Christian Lenz
 */
public class SVGViewerToolbar {

    /**
     * collection of all buttons in the toolbar
     */
    private final Collection<JButton> toolbarButtons = new ArrayList<>(11);
    private SVGViewerElement svgViewerElement;
    private final DecimalFormat formatter = new DecimalFormat("#.##"); //NOI18N
    private CustomZoomAction customZoomAction;

    public JToolBar createToolbar(SVGViewerElement svgViewerElement) {
        this.svgViewerElement = svgViewerElement;

        JToolBar toolBar = new JToolBar();

        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        toolBar.setFloatable(false);
        toolBar.setName(NbBundle.getMessage(SVGViewerToolbar.class, "ACSN_Toolbar"));
        toolBar.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SVGViewerToolbar.class, "ACSD_Toolbar"));

        JButton outButton = new JButton(SystemAction.get(ZoomOutAction.class));
        outButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/image/zoomOut.gif", false));
        outButton.setToolTipText(NbBundle.getMessage(ZoomOutAction.class, "LBL_ZoomOut"));
        outButton.setMnemonic(NbBundle.getMessage(SVGViewerToolbar.class, "ACS_Out_BTN_Mnem").charAt(0));
        outButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SVGViewerToolbar.class, "ACSD_Out_BTN"));
        outButton.setText("");
        toolBar.add(outButton);
        toolbarButtons.add(outButton);

        JButton inButton = new JButton(SystemAction.get(ZoomInAction.class));
        inButton.setToolTipText(NbBundle.getMessage(ZoomInAction.class, "LBL_ZoomIn"));
        inButton.setMnemonic(NbBundle.getMessage(SVGViewerToolbar.class, "ACS_In_BTN_Mnem").charAt(0));
        inButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SVGViewerToolbar.class, "ACSD_In_BTN"));
        inButton.setText("");
        toolBar.add(inButton);
        toolbarButtons.add(inButton);

        toolBar.addSeparator(new Dimension(11, 0));

        JButton button;
        toolBar.add(button = createZoomButton(1, 1));
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(button = createZoomButton(1, 3));
        toolbarButtons.add(button);
        toolBar.add(button = createZoomButton(1, 5));
        toolbarButtons.add(button);
        toolBar.add(button = createZoomButton(1, 7));
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(button = createZoomButton(3, 1));
        toolbarButtons.add(button);
        toolBar.add(button = createZoomButton(5, 1));
        toolbarButtons.add(button);
        toolBar.add(button = createZoomButton(7, 1));
        toolbarButtons.add(button);

        toolBar.addSeparator(new Dimension(11, 0));

        toolBar.add(button = createZoomButton());
        toolbarButtons.add(button);

        toolBar.addSeparator(new Dimension(11, 0));

        // Backgrounds
        toolBar.add(button = createCustomBackgroundButton(BackgroundMode.BLACK, Color.BLACK));
        toolbarButtons.add(button);
        toolBar.add(button = createCustomBackgroundButton(BackgroundMode.WHITE, Color.WHITE));
        toolbarButtons.add(button);
        toolBar.add(button = createCustomBackgroundButton(BackgroundMode.TRANSPARENT, Color.LIGHT_GRAY));
        toolbarButtons.add(button);
        toolBar.add(button = createCustomBackgroundButton(BackgroundMode.DARK_TRANSPARENT, Color.DARK_GRAY));
        toolbarButtons.add(button);
        toolBar.add(button = createCustomBackgroundButton(BackgroundMode.DEFAULT, Color.WHITE));
        toolbarButtons.add(button);

        // Image Dimension
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(new JLabel(NbBundle.getMessage(SVGViewerToolbar.class, "LBL_ImageDimensions", svgViewerElement.getImageWidth(), svgViewerElement.getImageHeight())));

        // Image File Size in KB, MB
        if (svgViewerElement.getImageSize() != -1) {
            toolBar.addSeparator(new Dimension(11, 0));
            double kb = 1024.0;
            double mb = kb * kb;
            final double size;
            final String label;
            if (svgViewerElement.getImageSize() >= mb) {
                size = svgViewerElement.getImageSize() / mb;
                label = "LBL_ImageSizeMb"; // NOI18N
            } else if (svgViewerElement.getImageSize() >= kb) {
                size = svgViewerElement.getImageSize() / kb;
                label = "LBL_ImageSizeKb"; // NOI18N
            } else {
                size = svgViewerElement.getImageSize();
                label = "LBL_ImageSizeBytes"; //NOI18N
            }

            toolBar.add(new JLabel(NbBundle.getMessage(SVGViewerToolbar.class, label, formatter.format(size))));
        }
        for (JButton jb : toolbarButtons) {
            jb.setFocusable(false);
        }
        return toolBar;
    }

    /**
     * Creates zoom button.
     */
    private JButton createZoomButton(final int xf, final int yf) {
        // PENDING buttons should have their own icons.
        JButton button = new JButton("" + xf + ":" + yf); // NOI18N
        if (xf < yf) {
            button.setToolTipText(NbBundle.getMessage(ZoomOutAction.class, "LBL_ZoomOut") + " " + xf + " : " + yf);
        } else {
            button.setToolTipText(NbBundle.getMessage(ZoomInAction.class, "LBL_ZoomIn") + " " + xf + " : " + yf);
        }
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SVGViewerToolbar.class, "ACS_Zoom_BTN"));
        button.addActionListener((ActionEvent evt) -> {
            svgViewerElement.customZoom(xf, yf);
        });

        return button;
    }

    private JButton createZoomButton() {
        // PENDING buttons should have their own icons.
        JButton button = new JButton(SystemAction.get(CustomZoomAction.class));
        button.setToolTipText(NbBundle.getMessage(CustomZoomAction.class, "LBL_CustomZoom"));
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SVGViewerToolbar.class, "ACS_Zoom_BTN"));

        return button;
    }

    /**
     * Creates custom background button.
     */
    private JButton createCustomBackgroundButton(BackgroundMode bgMode, Color color) {
        final JButton button = new JButton(); // NOI18N
        button.setIcon(new BackgroundIcon(bgMode, color));
        button.setToolTipText(NbBundle.getMessage(SVGViewerToolbar.class, "LBL_ChangeBackground", bgMode.name()));
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SVGViewerToolbar.class, "ACS_Background_BTN"));
        button.addActionListener((ActionEvent evt) -> {
            svgViewerElement.changeBackground(bgMode);
        });

        return button;
    }
}
