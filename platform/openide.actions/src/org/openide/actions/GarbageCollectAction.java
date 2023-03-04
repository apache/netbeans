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
package org.openide.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;

import java.awt.*;


import javax.swing.*;


// Toolbar presenter like MemoryMeterAction, except:
// 1. Does not have a mark etc.
// 2. But pressing it runs GC.
// 3. Slim profile fits nicely in the menu bar (at top level).
// 4. Displays textual memory usage directly, not via tooltip.
// Intended to be unobtrusive enough to leave on for daily use.

/**
 * Perform a system garbage collection.
 * @author Jesse Glick, Tim Boudreau
 */
public class GarbageCollectAction extends CallableSystemAction {
    @Override
    public String getName() {
        return NbBundle.getBundle(GarbageCollectAction.class).getString("CTL_GarbageCollect"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(GarbageCollectAction.class);
    }

    @Override
    public void performAction() {
        gc();
    }

    private static RequestProcessor RP;
    private static void gc() {
        if (RP == null) {
            RP = new RequestProcessor("GarbageCollectAction");
        }
        // Can be slow, would prefer not to block on it.
        RP.post(
            new Runnable() {
            @Override
                public void run() {
                    System.gc();
                    System.runFinalization();
                    System.gc();
                }
            }
        );
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public Component getToolbarPresenter() {
        return new HeapViewWrapper();
//        return new MemButton();
    }

    private static final boolean NIMBUS_LAF = "Nimbus".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private static final class HeapViewWrapper extends JComponent {
        public HeapViewWrapper() {
            add(new HeapView());
            setLayout(null);
        }
        
        @Override
        public boolean isOpaque() {
            return false;
        }
        
        @Override
        public Dimension getMinimumSize() {
            return calcPreferredSize();
        }

        @Override
        public Dimension getPreferredSize() {
            return calcPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension pref = calcPreferredSize();
            Container parent = getParent();
            if (parent != null && parent.getHeight() > 0) {
                pref.height = parent.getHeight();
            }
            return pref;
        }
        
        public Dimension calcPreferredSize() {
            Dimension pref = getHeapView().heapViewPreferredSize();
            pref.height += 1;
            pref.width += 6;
            return pref;
        }

        @SuppressWarnings("deprecation")
        @Override public void layout() {
            int w = getWidth();
            int h = getHeight();
            HeapView heapView = getHeapView();
            if( NIMBUS_LAF ) {
                heapView.setBounds(0, 0, w, h);
            } else {
                heapView.setBounds(4, 2, w - 6, h - 4);
            }
        }

        private HeapView getHeapView() {
            return (HeapView)getComponent(0);
        }
    }
    
}
