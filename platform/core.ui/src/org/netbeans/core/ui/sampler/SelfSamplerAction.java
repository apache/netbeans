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
package org.netbeans.core.ui.sampler;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingWorker;
import org.netbeans.modules.sampler.Sampler;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik, Tomas Hurka
 */
@ActionID(id = "org.netbeans.modules.profiler.actions.SelfSamplerAction", category = "Profile")
@ActionRegistration(iconInMenu = true, displayName = "#SelfSamplerAction_ActionNameStart", iconBase = "org/netbeans/core/ui/sampler/selfSampler.png")
@ActionReferences({
    @ActionReference(path = "Toolbars/Memory", position = 2000),
    @ActionReference(path = "Shortcuts", name = "AS-Y")
})
public class SelfSamplerAction extends AbstractAction implements AWTEventListener {
    // -----
    // I18N String constants
    private static final String ACTION_NAME_START = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ActionNameStart");
    private static final String ACTION_NAME_STOP = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ActionNameStop");
//    private static final String ACTION_DESCR = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_ActionDescription");
    private static final String NOT_SUPPORTED = NbBundle.getMessage(SelfSamplerAction.class, "SelfSamplerAction_NotSupported");
    private final AtomicReference<Sampler> RUNNING = new AtomicReference<Sampler>();

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public SelfSamplerAction() {
        putValue(Action.NAME, ACTION_NAME_START);
        putValue(Action.SHORT_DESCRIPTION, ACTION_NAME_START);
        putValue ("iconBase", "org/netbeans/core/ui/sampler/selfSampler.png"); // NOI18N
        if (System.getProperty(SelfSamplerAction.class.getName() + ".sniff") != null) { //NOI18N
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when an action occurs.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        Sampler c = Sampler.createManualSampler("Self Sampler");  // NOI18N
        if (c != null) {
            if (RUNNING.compareAndSet(null, c)) {
                putValue(Action.NAME, ACTION_NAME_STOP);
                putValue(Action.SHORT_DESCRIPTION, ACTION_NAME_STOP);
                putValue ("iconBase", "org/netbeans/core/ui/sampler/selfSamplerRunning.png"); // NOI18N
                c.start();
            } else if ((c = RUNNING.getAndSet(null)) != null) {
                final Sampler controller = c;

                setEnabled(false);
                SwingWorker worker = new SwingWorker() {

                    @Override
                    protected Object doInBackground() throws Exception {
                        controller.stop();
                        return null;
                    }

                    @Override
                    protected void done() {
                        putValue(Action.NAME, ACTION_NAME_START);
                        putValue(Action.SHORT_DESCRIPTION, ACTION_NAME_START);
                        putValue ("iconBase", "org/netbeans/core/ui/sampler/selfSampler.png"); // NOI18N
                        SelfSamplerAction.this.setEnabled(true);
                    }
                };
                worker.execute();
            }
        } else {
            NotifyDescriptor d = new NotifyDescriptor.Message(NOT_SUPPORTED, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        KeyEvent kevent = (KeyEvent) event;
        if (kevent.getID() == KeyEvent.KEY_RELEASED && kevent.getKeyCode() == KeyEvent.VK_ALT_GRAPH) { // AltGr
            actionPerformed(new ActionEvent(this, event.getID(), "shortcut")); //NOI18N
            kevent.consume();
        }
    }

    final boolean isProfileMe(Sampler c) {
        return c == RUNNING.get();
    }
}
