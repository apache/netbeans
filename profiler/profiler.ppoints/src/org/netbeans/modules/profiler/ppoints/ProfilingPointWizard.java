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

package org.netbeans.modules.profiler.ppoints;

import java.awt.BorderLayout;
import org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel;
import org.netbeans.modules.profiler.ppoints.ui.ValidityListener;
import org.netbeans.modules.profiler.ppoints.ui.WizardPanel1UI;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.Component;
import java.awt.Dimension;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProfilingPointWizard_AnotherPpEditedMsg=Another Profiling Point is currently being edited!",
    "ProfilingPointWizard_NoPpsFoundMsg=No registered Profiling Points found!",
    "ProfilingPointWizard_WizardTitle=New Profiling Point",
    "ProfilingPointWizard_WizardStep1Caption=Choose Type & Project",
    "ProfilingPointWizard_WizardStep2Caption=Customize Properties"
})
public class ProfilingPointWizard implements WizardDescriptor.Iterator {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    // --- WizardPanel for selecting Profiling Point type & Project --------------
    class WizardPanel1 extends WizardPanel {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Lookup.Provider selectedProjectRef;
        private int selectedPPFactoryIndexRef;

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public HelpCtx getHelp() {
            Component customizer = getComponent();

            if (!(customizer instanceof HelpCtx.Provider)) {
                return null;
            }

            return ((HelpCtx.Provider) customizer).getHelpCtx();
        }

        public String getName() {
            return Bundle.ProfilingPointWizard_WizardStep1Caption();
        }

        public Component createComponent() {
            WizardPanel1UI component = new WizardPanel1UI();
            component.addValidityListener(this);
            component.init(ppFactories);
            setValid(component.areSettingsValid());
            
            if (component.hasDefaultScope())
                selectedProject = component.getSelectedProject();

            return component;
        }

        public void hiding(boolean cancelled) {
            selectedPPFactoryIndex = ((WizardPanel1UI) getComponent()).getSelectedIndex();

            if (selectedPPFactoryIndex != selectedPPFactoryIndexRef) {
                settingsChanged = true;
            }

            selectedProject = ((WizardPanel1UI) getComponent()).getSelectedProject();

            if ((selectedProject == null) || !selectedProject.equals(selectedProjectRef)) {
                settingsChanged = true;
            }
        }

        public void showing() {
            selectedPPFactoryIndexRef = selectedPPFactoryIndex;
            ((WizardPanel1UI) getComponent()).setSelectedIndex(selectedPPFactoryIndex);
            selectedProjectRef = selectedProject;

            if (selectedProject == null) {
                selectedProject = Utils.getCurrentProject();
            }

            ((WizardPanel1UI) getComponent()).setSelectedProject(selectedProject);
        }
    }

    // --- WizardPanel for customizing Profiling Point properties ----------------
    class WizardPanel2 extends WizardPanel {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private ValidityAwarePanel customizer;

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public HelpCtx getHelp() {
            if (!(customizer instanceof HelpCtx.Provider)) {
                return null;
            }

            return ((HelpCtx.Provider) customizer).getHelpCtx();
        }

        public String getName() {
            return Bundle.ProfilingPointWizard_WizardStep2Caption();
        }

        public Component createComponent() {
            JScrollPane customizerScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            customizerScrollPane.setBorder(BorderFactory.createEmptyBorder());
            customizerScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
            customizerScrollPane.setOpaque(false);
            customizerScrollPane.getViewport().setOpaque(false);
            String hint = ppFactories[selectedPPFactoryIndex].getHint();
            if (hint != null && !hint.isEmpty()) {
                JPanel panel = new JPanel(new BorderLayout(0, 0));
                panel.setOpaque(false);
                panel.add(customizerScrollPane, BorderLayout.CENTER);
                JTextArea area = new JTextArea(hint);
                area.setOpaque(false);
                area.setWrapStyleWord(true);
                area.setLineWrap(true);
                area.setEnabled(false);
                area.setFont(UIManager.getFont("Label.font")); //NOI18N
                area.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
                panel.add(area, BorderLayout.SOUTH);
                return panel;
            } else {
                return customizerScrollPane;
            }
        }

        public void hiding(boolean cancelled) {
            if (!cancelled && profilingPoint != null && customizer != null) {
                profilingPoint.setValues(customizer);
            }

            unregisterCustomizerListener();
        }

        public void notifyClosed(boolean cancelled) {
            releaseCurrentCustomizer();
            profilingPoint = null;
        }

        public void showing() {
            if ((customizer == null) || settingsChanged) {
                releaseCurrentCustomizer();
                createNewCustomizer();
                settingsChanged = false;
            }

            setValid(customizer.areSettingsValid());
            registerCustomizerListener();
        }

        private void createNewCustomizer() {
            // TODO: selectedPPFactoryIndex or selectedProject could be -1/null, create() can return null
            profilingPoint = ppFactories[selectedPPFactoryIndex].create(selectedProject);
            customizer = profilingPoint.getCustomizer();
            getContainer().setViewportView(customizer);
        }

        private void registerCustomizerListener() {
            if (customizer != null) {
                customizer.addValidityListener(this);
            }
        }

        private void releaseCurrentCustomizer() {
            resetComponent();
            customizer = null;
        }

        private void unregisterCustomizerListener() {
            if (customizer != null) {
                customizer.removeValidityListener(this);
            }
        }
        
        private JScrollPane getContainer() {
            Component container = getComponent();
            if (!(container instanceof JScrollPane))
                container = ((JComponent)container).getComponent(0);
            if (!(container instanceof JScrollPane))
                container = ((JComponent)getComponent()).getComponent(1);
            return (JScrollPane)container;
        }
    }

    // --- Abstract WizardPanel implementation -----------------------------------
    private abstract class WizardPanel implements WizardDescriptor.Panel, ValidityListener {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        protected boolean valid = true;
        private Component component;
        private EventListenerList listenerList = new EventListenerList();

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public abstract String getName();

        public Component getComponent() {
            if (component == null) {
                component = createComponent();
                component.setName(getName());
                component.setPreferredSize(preferredPanelSize);
            }

            return component;
        }
        
        protected void resetComponent() {
            component = null;
        }

        public void setValid(boolean valid) {
            if (this.valid != valid) {
                this.valid = valid;
                fireChangeListenerStateChanged(this);
            }

            ;
        }

        public boolean isValid() {
            return valid;
        }

        public abstract Component createComponent();

        /** Registers ChangeListener to receive events.
         * @param listener The listener to register.
         */
        public synchronized void addChangeListener(ChangeListener listener) {
            listenerList.add(ChangeListener.class, listener);
        }

        public void hiding(boolean cancelled) {
        }

        public void notifyClosed(boolean cancelled) {
        }

        public void readSettings(Object settings) {
        }

        /** Removes ChangeListener from the list of listeners.
         * @param listener The listener to remove.
         */
        public synchronized void removeChangeListener(ChangeListener listener) {
            listenerList.remove(ChangeListener.class, listener);
        }

        public void showing() {
        }

        public void storeSettings(Object settings) {
        }

        public void validityChanged(boolean isValid) {
            setValid(isValid);
        }

        /** Notifies all registered listeners about the event.
         *
         * @param param Parameter #1 of the <CODE>ChangeEvent<CODE> constructor.
         */
        protected void fireChangeListenerStateChanged(Object param) {
            ChangeEvent e = null;
            Object[] listeners = listenerList.getListenerList();

            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChangeListener.class) {
                    if (e == null) {
                        e = new ChangeEvent(param);
                    }

                    ((ChangeListener) listeners[i + 1]).stateChanged(e);
                }
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static ProfilingPointWizard defaultInstance;
    private static final Dimension DEFAULT_PREFERRED_PANEL_SIZE = new Dimension(440, 330);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Dimension preferredPanelSize = null;
    private ProfilingPoint profilingPoint;
    private Lookup.Provider selectedProject;
    private WizardDescriptor wizardDescriptor;

    // --- Wizard runtime implementation -----------------------------------------
    private ProfilingPointFactory[] ppFactories;
    private WizardPanel[] wizardPanels;
    private String[] wizardSteps;
    private boolean settingsChanged;
    private int currentPanel;
    private int selectedPPFactoryIndex;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private ProfilingPointWizard() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    private static class Singleton {
        private static final ProfilingPointWizard INSTANCE = new ProfilingPointWizard();
    }
    
    public static ProfilingPointWizard getDefault() {
        return Singleton.INSTANCE;
    }

    public WizardDescriptor getWizardDescriptor() {
        return getWizardDescriptor(null);
    }

    public WizardDescriptor getWizardDescriptor(Lookup.Provider project) {
        ValidityAwarePanel showingCustomizer = ProfilingPointsManager.getDefault().getShowingCustomizer();

        if (showingCustomizer != null) {
            ProfilerDialogs.displayWarning(
                    Bundle.ProfilingPointWizard_AnotherPpEditedMsg());
            SwingUtilities.getWindowAncestor(showingCustomizer).requestFocus();
            showingCustomizer.requestFocusInWindow();

            return null;
        } else {
            //      profilingPoint = null;
            settingsChanged = true;
            currentPanel = 0;
            selectedPPFactoryIndex = 0;
            selectedProject = project;
            initWizardDescriptor();
            initWizardPanels();

            if (ppFactories.length > 0) {
                getCurrentWizardPanel().showing();

                return wizardDescriptor;
            } else {
                ProfilerDialogs.displayError(
                        Bundle.ProfilingPointWizard_NoPpsFoundMsg());

                return null;
            }
        }
    }

    // --- WizardDescriptor.Iterator implementation ------------------------------
    public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {
    }

    public WizardDescriptor.Panel current() {
        return getCurrentWizardPanel();
    }

    public ProfilingPoint finish(boolean cancelled) {
        ProfilingPoint result = cancelled ? null : profilingPoint;

        if (wizardPanels != null) {
            wizardPanels[currentPanel].hiding(cancelled);

            for (int i = 0; i < wizardPanels.length; i++) {
                wizardPanels[i].notifyClosed(cancelled); // Will invoke profilingPoint = null
            }
        }

        return result;
    }

    public boolean hasNext() {
        return currentPanel < (wizardSteps.length - 1);
    }

    public boolean hasPrevious() {
        return currentPanel > 0;
    }

    public String name() {
        return getCurrentWizardPanel().getName();
    }

    public void nextPanel() {
        getCurrentWizardPanel().hiding(false);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(++currentPanel)); // NOI18N
        getCurrentWizardPanel().showing();
    }

    public void previousPanel() {
        getCurrentWizardPanel().hiding(false);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(--currentPanel)); // NOI18N
        getCurrentWizardPanel().showing();
    }

    public synchronized void removeChangeListener(javax.swing.event.ChangeListener listener) {
    }

    private WizardPanel getCurrentWizardPanel() {
        return wizardPanels[currentPanel];
    }

    private void initWizardDescriptor() {
        wizardDescriptor = new WizardDescriptor(this);
        wizardDescriptor.setTitle(Bundle.ProfilingPointWizard_WizardTitle());
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N

        wizardDescriptor.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(0)); // NOI18N
    }

    private void initWizardPanels() {
        ppFactories = ProfilingPointsManager.getDefault().getProfilingPointFactories();
        wizardPanels = new WizardPanel[] { new WizardPanel1(), new WizardPanel2() };
        wizardSteps = new String[wizardPanels.length];

        for (int i = 0; i < wizardPanels.length; i++) {
            wizardSteps[i] = wizardPanels[i].getName();
        }

        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, wizardSteps); // NOI18N

        if (preferredPanelSize == null) {
            preferredPanelSize = new Dimension(DEFAULT_PREFERRED_PANEL_SIZE);

            Dimension firstPanelSize = ((WizardPanel1UI) (wizardPanels[0].getComponent())).getMinSize();
            preferredPanelSize.width = Math.max(preferredPanelSize.width, firstPanelSize.width);
            preferredPanelSize.height = Math.max(preferredPanelSize.height, firstPanelSize.height);
        }
    }
}
