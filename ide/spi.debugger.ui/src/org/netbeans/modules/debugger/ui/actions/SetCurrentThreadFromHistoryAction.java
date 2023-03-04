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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.netbeans.modules.debugger.ui.views.debugging.KeyboardPopupSwitcher;
import org.netbeans.modules.debugger.ui.views.debugging.ThreadsHistoryAction;
import org.netbeans.modules.debugger.ui.views.debugging.ThreadsListener;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;

import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 *
 * @author martin
 */
public class SetCurrentThreadFromHistoryAction extends AbstractAction implements Presenter.Menu, Presenter.Popup {
    
    private static final Logger logger = Logger.getLogger(SetCurrentThreadFromHistoryAction.class.getName());

    public SetCurrentThreadFromHistoryAction() {
        putValue(NAME, NbBundle.getMessage(ThreadsHistoryAction.class, "CTL_ThreadsHistoryAction"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new ThreadsHistoryAction().actionPerformed(e);
    }

    @Override
    public JMenuItem getMenuPresenter() {
        final JMenuItem item = new JMenuItem();
        Mnemonics.setLocalizedText(item, NbBundle.getMessage(SetCurrentThreadFromHistoryAction.class, "CTL_SetCurrentThreadFromHistoryAction"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showWindow();
            }
        });
        item.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("hierarchy changed: "+e+"\n  item.getParent() = "+item.getParent()+", item.isDisplayable() = "+item.isDisplayable());
                }
                if (item.getParent() == null || !item.isDisplayable()) return;
                item.setEnabled(ThreadsHistoryAction.getThreads().size() > 0);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("  threads size = "+ThreadsHistoryAction.getThreads().size()+
                                " => item.isEnabled() = "+item.isEnabled());
                }
            }
        });
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getMenuPresenter(): item = "+item);
        }
        //item.setAccelerator(KeyStroke.getKeyStroke('T', InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        // For the case that we're not asked for the menu presenter again,
        // we need to update the enabled state of the menu item:
        PropertyChangeListener pchl = new ThreadsChangeListener(item);
        item.putClientProperty(ThreadsChangeListener.class.getName(), pchl); // Hold the listener so that it's not collected
        ThreadsListener.addPropertyChangeListener(WeakListeners.propertyChange(pchl, ThreadsListener.class));
        return item;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getMenuPresenter();
    }

    private void showWindow() {
        List<DVThread> threads = ThreadsHistoryAction.getThreads();
        int threadsCount = threads.size();
        if (threadsCount < 1) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        int triggerKey = KeyEvent.VK_DOWN;
        int releaseKey = KeyEvent.VK_ENTER;
        KeyboardPopupSwitcher.selectItem(
                ThreadsHistoryAction.createSwitcherItems(threads),
                releaseKey, triggerKey, true, true);
    }
    
    private static final class ThreadsChangeListener implements PropertyChangeListener {
        
        private final WeakReference<JMenuItem> itemRef;
        
        ThreadsChangeListener(JMenuItem item) {
            itemRef = new WeakReference<JMenuItem>(item);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JMenuItem item = itemRef.get();
            if (item == null) {
                return ;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Threads have changed: "+evt.getPropertyName()+": "+evt.getNewValue());
                logger.fine("item is showing: "+item.isShowing());
            }
            if (item.isShowing()) {
                item.setEnabled(ThreadsHistoryAction.getThreads().size() > 0);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("  threads size = "+ThreadsHistoryAction.getThreads().size()+
                                " => item.isEnabled() = "+item.isEnabled());
                }
            }
        }
        
    }

}
