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

package org.netbeans.modules.profiler.snaptracer.impl.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import org.netbeans.modules.profiler.snaptracer.impl.swing.ScrollableContainer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Sedlacek
 */
final class TracerOptionsPanelController extends OptionsPanelController {

    private TracerOptions options = TracerOptions.getInstance();
    private TracerOptionsPanel panel;
    private JComponent component;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;


    public void update() {
        TracerOptionsPanel p = getPanel();

        p.setProbesApp(options.getProbesApp());

        p.setRefresh(options.getRefresh());
        p.setRefreshCustomizable(options.isRefreshCustomizable());

        p.setShowValuesEnabled(options.isShowValuesEnabled());
        p.setShowLegendEnabled(options.isShowLegendEnabled());
        p.setRowsDecorationEnabled(options.isRowsDecorationEnabled());
        p.setRowsSelectionEnabled(options.isRowsSelectionEnabled());

        p.setInitiallyOpened(options.getInitiallyOpened());
        p.setOnProbeAdded(options.getOnProbeAdded());
        p.setOnProbeAdded2(options.getOnProbeAdded2());
        p.setOnSessionStart(options.getOnSessionStart());
        p.setOnRowSelected(options.getOnRowSelected());
        p.setOnRowSelected2(options.getOnRowSelected2());

        p.setZoomMode(options.getZoomMode());
        p.setMouseWheelAction(options.getMouseWheelAction());

        p.setTimelineToolbar(options.getTimelineToolbar());
        p.setSelectionToolbar(options.getSelectionToolbar());
        p.setExtraToolbar(options.getExtraToolbar());

        p.setClearSelection(options.isClearSelection());

        p.update();
    }

    public void applyChanges() {
        TracerOptionsPanel p = getPanel();

        options.setProbesApp(p.getProbesApp());

        options.setRefresh(p.getRefresh());
        options.setRefreshCustomizable(p.isRefreshCustomizable());

        options.setShowValuesEnabled(p.isShowValuesEnabled());
        options.setShowLegendEnabled(p.isShowLegendEnabled());
        options.setRowsDecorationEnabled(p.isRowsDecorationEnabled());
        options.setRowsSelectionEnabled(p.isRowsSelectionEnabled());

        options.setInitiallyOpened(p.getInitiallyOpened());
        options.setOnProbeAdded(p.getOnProbeAdded());
        options.setOnProbeAdded2(p.getOnProbeAdded2());
        options.setOnSessionStart(p.getOnSessionStart());
        options.setOnRowSelected(p.getOnRowSelected());
        options.setOnRowSelected2(p.getOnRowSelected2());

        options.setZoomMode(p.getZoomMode());
        options.setMouseWheelAction(p.getMouseWheelAction());

        options.setTimelineToolbar(p.getTimelineToolbar());
        options.setSelectionToolbar(p.getSelectionToolbar());
        options.setExtraToolbar(p.getExtraToolbar());

        options.setClearSelection(p.isClearSelection());
    }

    public void cancel() {}

    public boolean isValid() {
        return getPanel().dataValid();
    }

    public boolean isChanged() {
        TracerOptionsPanel p = getPanel();

        if (options.getProbesApp() != p.getProbesApp()) return true;

        if (options.getRefresh() != p.getRefresh()) return true;
        if (options.isRefreshCustomizable() != p.isRefreshCustomizable()) return true;

        if (options.isShowValuesEnabled() != p.isShowValuesEnabled()) return true;
        if (options.isShowLegendEnabled() != p.isShowLegendEnabled()) return true;
        if (options.isRowsDecorationEnabled() != p.isRowsDecorationEnabled()) return true;
        if (options.isRowsSelectionEnabled() != p.isRowsSelectionEnabled()) return true;

        if (!options.getInitiallyOpened().equals(p.getInitiallyOpened())) return true;
        if (!options.getOnProbeAdded().equals(p.getOnProbeAdded())) return true;
        if (!options.getOnProbeAdded2().equals(p.getOnProbeAdded2())) return true;
        if (!options.getOnSessionStart().equals(p.getOnSessionStart())) return true;
        if (!options.getOnRowSelected().equals(p.getOnRowSelected())) return true;
        if (!options.getOnRowSelected2().equals(p.getOnRowSelected2())) return true;

        if (!options.getZoomMode().equals(p.getZoomMode())) return true;
        if (!options.getMouseWheelAction().equals(p.getMouseWheelAction())) return true;

        if (options.getTimelineToolbar() != p.getTimelineToolbar()) return true;
        if (options.getSelectionToolbar() != p.getSelectionToolbar()) return true;
        if (options.getExtraToolbar() != p.getExtraToolbar()) return true;

        if (options.isClearSelection() != p.isClearSelection()) return true;

        return false;
    }


    public HelpCtx getHelpCtx() {
        return null;

    }


    public JComponent getComponent(Lookup masterLookup) {
        return getComponent();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }


    private TracerOptionsPanel getPanel() {
        if (panel == null) panel = new TracerOptionsPanel(this);
        return panel;
    }

    private JComponent getComponent() {
        if (component == null) {
            ScrollableContainer container = new ScrollableContainer(getPanel(),
                                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            container.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            container.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 5));
            component = container;
        }
        return component;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

}
