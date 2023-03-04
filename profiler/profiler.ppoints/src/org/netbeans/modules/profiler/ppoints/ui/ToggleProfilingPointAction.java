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

package org.netbeans.modules.profiler.ppoints.ui;

import org.netbeans.lib.profiler.ui.components.ThinBevelBorder;
import org.netbeans.modules.profiler.ppoints.CodeProfilingPoint;
import org.netbeans.modules.profiler.ppoints.GlobalProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPointFactory;
import org.netbeans.modules.profiler.ppoints.ProfilingPointsManager;
import org.netbeans.modules.profiler.ppoints.Utils;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import org.netbeans.modules.profiler.api.EditorSupport;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ToggleProfilingPointAction_ActionName=Toggle Profiling Point",
    "ToggleProfilingPointAction_ActionDescr=Toggles Profiling Point",
    "ToggleProfilingPointAction_ProfilingProgressMsg=Cannot create new Profiling Point during profiling session.",
    "ToggleProfilingPointAction_BadSourceMsg=Profiling Points cannot be created in this source file.",
    "ToggleProfilingPointAction_CancelString=Cancel",
    "ToggleProfilingPointAction_SwitcherWindowCaption=New Profiling Point",
    "ToggleProfilingPointAction_InvalidShortcutMsg=<html><b>Invalid shortcut assigned to {0} action.</b><br><br>Make sure that exactly one shortcut is assigned to the action<br>and at least one modifier key is used in the shortcut.</html>"
})
public class ToggleProfilingPointAction extends AbstractAction implements AWTEventListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class ProfilingPointsSwitcher extends JFrame {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static final String NO_ACTION_NAME = Bundle.ToggleProfilingPointAction_CancelString();
        private static final Icon NO_ACTION_ICON = null;

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Dimension size;
        private JLabel label;
        private JPanel previewPanel;
        private ProfilingPointFactory ppFactory;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ProfilingPointsSwitcher() {
            super(Bundle.ToggleProfilingPointAction_SwitcherWindowCaption());
            initProperties();
            initComponents();
            setProfilingPointFactory(null, -1);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void setProfilingPointFactory(ProfilingPointFactory ppFactory, int index) {
            this.ppFactory = ppFactory;

            if (ppFactory != null) {
                label.setText(ppFactory.getType());
                label.setIcon(ppFactory.getIcon());
            } else {
                label.setText(NO_ACTION_NAME);
                label.setIcon(NO_ACTION_ICON);
            }

            Component selected = null;

            if ((index >= 0) && (index < previewPanel.getComponentCount())) {
                selected = previewPanel.getComponent(index);
            }

            for (Component c : previewPanel.getComponents()) {
                if (c == selected) {
                    Border empt1 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
                    Border sel = BorderFactory.createMatteBorder(1, 1, 1, 1, SystemColor.textHighlight);
                    Border empt2 = BorderFactory.createEmptyBorder(0, 2, 0, 2);
                    Border comp1 = BorderFactory.createCompoundBorder(empt2, sel);
                    Border comp2 = BorderFactory.createCompoundBorder(comp1, empt1);
                    ((JComponent) c).setBorder(comp2);
                } else {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
                }
            }
        }

        public ProfilingPointFactory getProfilingPointFactory() {
            return ppFactory;
        }

        public void setVisible(boolean visible) {
            if (visible) {
                if (size == null) {
                    size = getSize();
                }

                TopComponent editor = WindowManager.getDefault().getRegistry().getActivated();
                Rectangle b = editor.getBounds();
                Point location = new Point((b.x + (b.width / 2)) - (size.width / 2), (b.y + (b.height / 2)) - (size.height / 2));
                SwingUtilities.convertPointToScreen(location, editor);
                setLocation(location);
            }

            super.setVisible(visible);
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            previewPanel = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
            previewPanel.setBorder(BorderFactory.createEmptyBorder(4, 7, 2, 7));

            label = new JLabel();
            label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 7, 7, 7),
                                                               new ThinBevelBorder(BevelBorder.LOWERED)));
            label.setBorder(BorderFactory.createCompoundBorder(label.getBorder(), BorderFactory.createEmptyBorder(4, 3, 4, 3)));
            label.setFont(label.getFont().deriveFont(Font.BOLD));

            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createRaisedBevelBorder());
            p.add(previewPanel, BorderLayout.NORTH);
            p.add(label, BorderLayout.CENTER);

            add(p, BorderLayout.CENTER);
        }

        private void initPanel(ProfilingPointFactory[] ppFactories) {
            Dimension prefSize = new Dimension(230, 0);

            for (int i = 0; i < ppFactories.length; i++) {
                JLabel previewIcon = new JLabel(ppFactories[i].getIcon());
                previewIcon.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
                previewPanel.add(previewIcon);

                setProfilingPointFactory(ppFactories[i], i);
                pack();

                Dimension currPrefSize = getPreferredSize();
                prefSize = new Dimension(Math.max(prefSize.width, currPrefSize.width),
                                         Math.max(prefSize.height, currPrefSize.height));
            }

            setProfilingPointFactory(null, ppFactories.length);
            setSize(prefSize);
        }

        private void initProperties() {
            setAlwaysOnTop(true);
            setUndecorated(true);
            setResizable(false);
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                public void run() { setIconImage(WindowManager.getDefault().getMainWindow().getIconImage()); }
            });
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    
    private static ToggleProfilingPointAction instance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ProfilingPointsSwitcher ppSwitcher;
    private ProfilingPointFactory[] ppFactories;
    private volatile boolean warningDialogOpened = false;
    private int currentFactory;

    private KeyStroke acceleratorKeyStroke;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ToggleProfilingPointAction() {
        putValue(Action.NAME, Bundle.ToggleProfilingPointAction_ActionName());
        putValue(Action.SHORT_DESCRIPTION, Bundle.ToggleProfilingPointAction_ActionDescr());
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public static synchronized ToggleProfilingPointAction getInstance() {
        if (instance == null) instance = new ToggleProfilingPointAction();
        return instance;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isEnabled() {
        return true;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent e) {
        acceleratorKeyStroke = Utilities.stringToKey(e.getActionCommand());
        if (acceleratorKeyStroke == null || acceleratorKeyStroke.getModifiers() == 0) {
            ProfilerDialogs.displayError(
                    Bundle.ToggleProfilingPointAction_InvalidShortcutMsg(
                    Bundle.ToggleProfilingPointAction_ActionName()));
            return;
        }
        
        if (warningDialogOpened) {
            return;
        }

        if (ProfilingPointsManager.getDefault().isProfilingSessionInProgress()) {
            warningDialogOpened = true;
            ProfilerDialogs.displayWarning(
                    Bundle.ToggleProfilingPointAction_ProfilingProgressMsg());
            warningDialogOpened = false;

            return;
        }

        if (Utils.getCurrentLocation(0).equals(CodeProfilingPoint.Location.EMPTY)) {
            warningDialogOpened = true;
            ProfilerDialogs.displayWarning(
                    Bundle.ToggleProfilingPointAction_BadSourceMsg());
            warningDialogOpened = false;

            return;
        }
        
        ProfilingPointsSwitcher chooserFrame = getChooserFrame();
        
        boolean toggleOff = false;
        CodeProfilingPoint.Location location = Utils.getCurrentLocation(CodeProfilingPoint.Location.OFFSET_START);
        for(CodeProfilingPoint pp : ProfilingPointsManager.getDefault().getProfilingPoints(CodeProfilingPoint.class, Utils.getCurrentProject(), false, false)) {
            if (location.equals(pp.getLocation())) {
                ProfilingPointsManager.getDefault().removeProfilingPoint(pp);
                toggleOff = true;
            }
        }
        if (!toggleOff) {
            if (chooserFrame.isVisible()) {
                nextFactory();
                chooserFrame.setProfilingPointFactory((currentFactory == ppFactories.length) ? null : ppFactories[currentFactory],
                                                      currentFactory);
            } else {
                if (EditorSupport.currentlyInJavaEditor()) {
                    resetFactories();
                    chooserFrame.setProfilingPointFactory((currentFactory == ppFactories.length) ? null : ppFactories[currentFactory],
                                                          currentFactory);
                    Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
                    chooserFrame.setVisible(true);
                }
            }
        }
    }

    public void eventDispatched(AWTEvent event) {
        if (!(event instanceof KeyEvent)) return;
        
        KeyStroke eventKeyStroke = KeyStroke.getKeyStrokeForEvent((KeyEvent)event);
        if (acceleratorKeyStroke == null || eventKeyStroke == null) return;
        
        int acceleratorModifiers = acceleratorKeyStroke.getModifiers();
        if (acceleratorModifiers == 0) return;
        
        if (acceleratorModifiers != eventKeyStroke.getModifiers()) modifierKeyStateChanged();
    }
    
    private synchronized ProfilingPointsSwitcher getChooserFrame() {
        if (ppSwitcher == null) {
            ppSwitcher = new ProfilingPointsSwitcher();
            ppSwitcher.addWindowListener(new WindowAdapter() {
                public void windowDeactivated(WindowEvent event) {
                    ppSwitcher.setVisible(false);
                }
            });
        }
        
        return ppSwitcher;
    }

    private synchronized void modifierKeyStateChanged() {
        if (ProfilingPointsManager.getDefault().isProfilingSessionInProgress()) {
            return;
        }

        ProfilingPointsSwitcher chooserFrame = getChooserFrame();

        if (chooserFrame.isVisible()) {
            ProfilingPointFactory ppFactory = chooserFrame.getProfilingPointFactory();
            Lookup.Provider project = Utils.getCurrentProject();

            if ((ppFactory != null) && (project != null)) {
                ProfilingPoint ppoint = ppFactory.create(project);

                if (ppoint != null) {
                    ProfilingPointsManager.getDefault().addProfilingPoint(ppoint);

                    if (ppoint instanceof GlobalProfilingPoint) {
                        SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (!ProfilingPointsWindow.getDefault().isOpened()) {
                                        ProfilingPointsWindow.getDefault().open();
                                        ProfilingPointsWindow.getDefault().requestVisible();
                                    }
                                }
                            });
                    }

                    ppoint.customize(true, true);
                }
            }
        }

        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        chooserFrame.setVisible(false);
    }

    private void nextFactory() {
        currentFactory++;

        if (currentFactory > ppFactories.length) {
            currentFactory = 0;
        }
    }

    private void resetFactories() {
        if (ppFactories == null) {
            ppFactories = ProfilingPointsManager.getDefault().getProfilingPointFactories();
            getChooserFrame().initPanel(ppFactories);
        }

        currentFactory = 0;
    }
}
