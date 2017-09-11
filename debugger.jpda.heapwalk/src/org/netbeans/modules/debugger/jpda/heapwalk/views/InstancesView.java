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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.debugger.jpda.heapwalk.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Martin Entlicher
 */
public class InstancesView extends TopComponent {
    
    private javax.swing.JPanel hfwPanel;
    private HeapFragmentWalker hfw;
    private HeapFragmentWalkerProvider provider;
    private DebuggerSessionListener listener;
    
    /** Creates a new instance of InstancesView */
    public InstancesView() {
        setIcon (ImageUtilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/root.gif")); // NOI18N
        setLayout (new BorderLayout ());
        // Remember the location of the component when closed.
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
    }

    @Override
    protected void componentShowing() {
        super.componentShowing ();
        listener = new DebuggerSessionListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(listener);
        showContent(listener.getState());
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        provider = null;
    }
    
    private void showContent(final int state) {
        if (SwingUtilities.isEventDispatchThread()) {
            showTheContent(state);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showTheContent(state);
                }
            });
        }
    }
    
    private void showTheContent(int state) {
        if (state == JPDADebugger.STATE_STOPPED) {
            ClassesCountsView cc = (ClassesCountsView) WindowManager.getDefault().findTopComponent("classes");
            HeapFragmentWalker hfw;
            if (cc != null && (hfw = cc.getCurrentFragmentWalker()) != null) {
                setHeapFragmentWalker(hfw);
                provider = null;
            } else if (provider != null) {
                setHeapFragmentWalker(provider.getHeapFragmentWalker());
            } else if (this.hfw != null) {
                setHeapFragmentWalker(this.hfw);
            } else {
                close(); // We can not show any meaningful content
            }
        } else if (state == JPDADebugger.STATE_RUNNING) {
            if (hfwPanel != null) {
                remove(hfwPanel);
            }
            this.hfw = null;
            hfwPanel = new SuspendInfoPanel();
            add(hfwPanel, "Center");
        } else {
            if (hfwPanel != null) {
                remove(hfwPanel);
                hfwPanel = null;
            }
            this.hfw = null;
            close(); // We can not show any meaningful content
        }
    }
    
    @Override
    protected void componentHidden () {
        super.componentHidden ();
        if (hfwPanel != null) {
            remove(hfwPanel);
            hfwPanel = null;
        }
        hfw = null;
        DebuggerManager.getDebuggerManager().removeDebuggerListener(listener);
        listener = null;
    }
    
    public void setHeapFragmentWalkerProvider(HeapFragmentWalkerProvider hfwp) {
        provider = hfwp;
        setHeapFragmentWalker(hfwp.getHeapFragmentWalker());
    }
    
    private void setHeapFragmentWalker(HeapFragmentWalker hfw) {
        if (hfwPanel != null) {
            remove(hfwPanel);
            hfwPanel = null;
        }
        this.hfw = hfw;
        if (hfw == null) {
            return ;
        }
        hfwPanel = hfw.getInstancesController().getPanel();
        add(hfwPanel, "Center");
        revalidate();
        repaint();
    }
    
    void assureSubViewsVisible() {
        // Assure that the sub-views are visible:
        hfw.getInstancesController().getInstancesListController().getPanel().setVisible(true);
        hfw.getInstancesController().getFieldsBrowserController().getPanel().setVisible(true);
        hfw.getInstancesController().getReferencesBrowserController().getPanel().setVisible(true);
    }
    
    public HeapFragmentWalker getCurrentFragmentWalker() {
        return hfw;
    }
    
    @Override
    public String getName () {
        return NbBundle.getMessage (InstancesView.class, "CTL_Instances_view");
    }
    
    @Override
    public String getToolTipText () {
        return NbBundle.getMessage (InstancesView.class, "CTL_Instances_tooltip");// NOI18N
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    public static interface HeapFragmentWalkerProvider {
        
        HeapFragmentWalker getHeapFragmentWalker();
        
    }
    
    private class DebuggerSessionListener extends DebuggerManagerAdapter {
        
        private Set<JPDADebugger> attachedTo = new WeakSet<JPDADebugger>();
        private int lastState = -1;
        
        public int getState() {
            int state = getTheState();
            synchronized (this) {
                lastState = state;
            }
            return state;
        }
        
        private int getTheState() {
            int state = JPDADebugger.STATE_DISCONNECTED;
            DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
            if (de != null) {
                JPDADebugger d = de.lookupFirst(null, JPDADebugger.class);
                if (d != null) {
                    state = getThreadsState(d);
                    synchronized (this) {
                        if (!attachedTo.contains(d)) {
                            attachedTo.add(d);
                            d.addPropertyChangeListener(this);
                        }
                    }
                }
            }
            return state;
        }
        
        private int getThreadsState(JPDADebugger d) {
            if (d.getState() != JPDADebugger.STATE_STOPPED) {
                return d.getState();
            }
            // Verify whether really all threads are stopped
            try {
                java.lang.reflect.Method allThreadsMethod =
                        d.getClass().getMethod("getAllThreads", new Class[] {});
                List<JPDAThread> threads = (List<JPDAThread>) allThreadsMethod.invoke(d, new Object[]{});
                for (JPDAThread t : threads) {
                    if (!t.isSuspended()) {
                        return JPDADebugger.STATE_RUNNING;
                    }
                }
                return JPDADebugger.STATE_STOPPED;
            } catch (Exception ex) {
                return d.getState();
            }
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            int state;
            if (propertyName.equals(DebuggerManager.PROP_CURRENT_ENGINE) ||
                propertyName.equals(JPDADebugger.PROP_STATE)) {
                
                state = getTheState();
                synchronized (this) {
                    if (state != lastState) {
                        lastState = state;
                    } else {
                        return ;
                    }
                }
                showContent(state);
            }
        }
        
    }
    
    private static class SuspendInfoPanel extends JPanel {
        
        public SuspendInfoPanel() {
            setLayout(new java.awt.GridBagLayout());
            JTextArea infoText = new JTextArea(NbBundle.getMessage(InstancesView.class, "MSG_NotSuspendedApp"));
            infoText.setEditable(false);
            infoText.setEnabled(false);
            infoText.setBackground(getBackground());
            infoText.setDisabledTextColor(new JLabel().getForeground());
            infoText.setLineWrap(true);
            infoText.setWrapStyleWord(true);
            infoText.setPreferredSize(
                    new Dimension(
                        infoText.getFontMetrics(infoText.getFont()).stringWidth(infoText.getText()),
                        infoText.getPreferredSize().height));
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            //gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            //gridBagConstraints.weightx = 1.0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
            add(infoText, gridBagConstraints);
            infoText.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstancesView.class, "MSG_NotSuspendedApp"));
            
            JButton pauseButton = new JButton();
            pauseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doStopCurrentDebugger();
                }
            });
            org.openide.awt.Mnemonics.setLocalizedText(pauseButton, NbBundle.getMessage(InstancesView.class, "CTL_Pause"));
            pauseButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/actions/Pause.gif", false));
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
            add(pauseButton, gridBagConstraints);
        }
        
        private void doStopCurrentDebugger() {
            DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
            if (de != null) {
                de.getActionsManager().postAction(ActionsManager.ACTION_PAUSE);
            }
        }

    }
    
}
