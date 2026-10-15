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
package org.netbeans.modules.terminal.nb.actions;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle;

/**
 *
 * @author igromov
 */
public abstract class TerminalAction extends AbstractAction implements ContextAwareAction {

    private Terminal context;
    private ActionEvent event;

    public TerminalAction(Terminal context) {
        this.context = context;
    }

    protected Terminal getTerminal() {
        return context;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        if (context == null) {
            Object source = e.getSource();
            /*
	    Kind of a hack, but I can't think up a better way to get a current
	    terminal when Action is performed with the shortcut. Thus it's context
	    independent and we need to find an active Terminal. Getting it from TC
	    won't work because we can have multiple active Terminals on screen
	    (debugger console and terminalemulator, for example).
	    Luckily, we can get some useful information from the caller`s source
             */
            if (source instanceof Component) {
                Container container = SwingUtilities.getAncestorOfClass(Terminal.class, (Component) source);
                if (container instanceof Terminal) {
                    this.context = (Terminal) container;
                }
            }
        }

        if (getTerminal() == null) {
            throw new IllegalStateException("No valid terminal component was provided"); // NOI18N
        }

        this.event = e;
        performAction();
    }

    protected abstract void performAction();

    protected static String getMessage(String key) {
        return NbBundle.getMessage(TerminalAction.class, key);
    }

    protected ActionEvent getEvent() {
        return event;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
