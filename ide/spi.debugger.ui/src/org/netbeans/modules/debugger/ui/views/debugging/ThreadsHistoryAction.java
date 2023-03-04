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


package org.netbeans.modules.debugger.ui.views.debugging;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.debugger.ui.DebuggingView.Deadlock;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


public final class ThreadsHistoryAction extends AbstractAction {

    /** Creates a new instance of ThreadsHistoryAction */
    public ThreadsHistoryAction() {
        putValue(NAME, NbBundle.getMessage(ThreadsHistoryAction.class, "CTL_ThreadsHistoryAction"));
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        List<DVThread> threads = getThreads();
        int threadsCount = threads.size();
        if (threadsCount < 1) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        if(!"immediately".equals(evt.getActionCommand()) && // NOI18N
                !(evt.getSource() instanceof javax.swing.JMenuItem)) {
            // #46800: fetch key directly from action command
            KeyStroke keyStroke = Utilities.stringToKey(evt.getActionCommand());
            
            if(keyStroke != null) {
                int triggerKey = keyStroke.getKeyCode();
                int releaseKey = 0;
                
                int modifiers = keyStroke.getModifiers();
                if((InputEvent.CTRL_MASK & modifiers) != 0) {
                    releaseKey = KeyEvent.VK_CONTROL;
                } else if((InputEvent.ALT_MASK & modifiers) != 0) {
                    releaseKey = KeyEvent.VK_ALT;
                } else if((InputEvent.META_MASK & modifiers) != 0) {
                    releaseKey = InputEvent.META_MASK;
                }
                
                if(releaseKey != 0) {
                    if (!KeyboardPopupSwitcher.isShown()) {
                        KeyboardPopupSwitcher.selectItem(
                                createSwitcherItems(threads),
                                releaseKey, triggerKey, true, true); // (evt.getModifiers() & KeyEvent.SHIFT_MASK) == 0
                    }
                    return;
                }
            }
        }
        
        if (threadsCount == 1) {
            threads.get(0).makeCurrent();
        } else {
            int index = (evt.getModifiers() & KeyEvent.SHIFT_MASK) == 0 ? 1 : threadsCount - 1;
            threads.get(index).makeCurrent();
        }
    }
    
    public static SwitcherTableItem[] createSwitcherItems(List<DVThread> threads) {
        ThreadsListener threadsListener = ThreadsListener.getDefault();
        DVSupport debugger = threadsListener.getDVSupport();
        DVThread currentThread = debugger != null ? debugger.getCurrentThread() : null;
        // collect all deadlocked threads
        Set<Deadlock> deadlocks = debugger != null ? debugger.getDeadlocks()
                : Collections.EMPTY_SET;
        if (deadlocks == null) {
            deadlocks = Collections.EMPTY_SET;
        }
        Set<DVThread> deadlockedThreads = new HashSet<DVThread>();
        for (Deadlock deadlock : deadlocks) {
            deadlockedThreads.addAll(deadlock.getThreads());
        }
        
        SwitcherTableItem[] items = new SwitcherTableItem[threads.size()];
        int i = 0;
        for (DVThread thread : threads) {
            String name = debugger.getDisplayName(thread);
            String htmlName = name;
            String description = ""; // tc.getToolTipText();
            Image image = debugger.getIcon(thread);//ImageUtilities.loadImage(DebuggingNodeModel.getIconBase(thread));
            Icon icon = null;
            if (image != null) {
                boolean isCurrent = thread == currentThread;
                boolean isAtBreakpoint = threadsListener.isBreakpointHit(thread);
                boolean isInDeadlock = deadlockedThreads.contains(thread);
                icon = new ThreadStatusIcon(image, isCurrent, isAtBreakpoint, isInDeadlock);
            }
            items[i] = new SwitcherTableItem(
                    new ActivatableElement(thread),
                    name,
                    htmlName,
                    icon,
                    false,
                    description != null ? description : name
            );
            i++;
        }
        return items;
    }
    
    private static class ActivatableElement implements SwitcherTableItem.Activatable {
        
        DVThread thread;
        
        private ActivatableElement(DVThread thread) {
            this.thread = thread;
        }
        @Override
        public void activate() {
            thread.makeCurrent();
        }
    }
    
    public static List<DVThread> getThreads() {
        ThreadsListener threadsListener = ThreadsListener.getDefault();
        if (threadsListener == null) {
            return Collections.emptyList();
        }
        List<DVThread> history = threadsListener.getCurrentThreadsHistory();
        List<DVThread> allThreads = threadsListener.getThreads();
        Set<DVThread> hitsSet = new HashSet<DVThread>();
        for (DVThread hit : threadsListener.getHits()) {
            hitsSet.add(hit);
        }
        Set set = new HashSet(history);
        List<DVThread> result = new LinkedList<DVThread>();
        result.addAll(history);
        for (DVThread thread : allThreads) {
            if (!set.contains(thread) && thread.isSuspended()) {
                result.add(thread);
            }
        }
        if (result.size() > 1 && hitsSet.size() > 0) {
            int index = 1;
            int size = result.size();
            for (int x = 1; x < size; x++) {
                DVThread t = result.get(x);
                if (hitsSet.contains(t)) {
                    if (x > index) {
                        result.remove(x);
                        result.add(index, t);
                    }
                    index++;
                }
            } // for
        }
        return result;
    }
    
    private static class ThreadStatusIcon implements Icon {
        
        private Image image;
        private ImageIcon iconBase;
        private boolean isCurrent;
        private boolean isAtBreakpoint;
        private boolean isInDeadlock;

        ThreadStatusIcon(Image image, boolean isCurrent, boolean isAtBreakpoint, boolean isInDeadlock) {
            this.image = image;
            this.isCurrent = isCurrent;
            this.isAtBreakpoint = isAtBreakpoint;
            this.isInDeadlock = isInDeadlock;
            iconBase = new ImageIcon(image);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            int width = iconBase.getIconWidth();
            int height = iconBase.getIconHeight();
            Color primaryColor = null;
            Color secondaryColor = null;
            if (isInDeadlock) {
                primaryColor = DebuggingViewComponent.deadlockColor;
            } else if (isCurrent) {
                primaryColor = DebuggingViewComponent.greenBarColor;
            } else if (isAtBreakpoint) {
                primaryColor = DebuggingViewComponent.hitsBarColor;
            }
            if (isCurrent && isInDeadlock) {
                secondaryColor = DebuggingViewComponent.greenBarColor;
            }
            
            Color originalColor = g.getColor();
            g.setColor(c.getBackground());
            g.fillRect(x, y, width, height);
            g.drawImage(image, x + width, y, iconBase.getImageObserver());
            if (primaryColor != null) {
                g.setColor(primaryColor);
                g.fillRect(x, y, DebuggingViewComponent.BAR_WIDTH, height);
            }
            if (secondaryColor != null) {
                g.setColor(secondaryColor);
                int w = DebuggingViewComponent.BAR_WIDTH / 2 + 1;
                g.fillRect(x + DebuggingViewComponent.BAR_WIDTH - w, y, w, height);
            }
            g.setColor(originalColor);
        }

        @Override
        public int getIconWidth() {
            return 2 * iconBase.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return iconBase.getIconHeight();
        }
        
    }
    
}

