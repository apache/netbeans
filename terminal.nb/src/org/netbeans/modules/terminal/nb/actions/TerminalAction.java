/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
                if (container != null && container instanceof Terminal) {
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
