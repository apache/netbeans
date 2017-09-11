/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.terminal;

import java.awt.Component;
import javax.swing.Action;
import org.netbeans.modules.dlight.terminal.action.TerminalSupportImpl;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.windows.IOContainer;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class TerminalSupport {

    private TerminalSupport() {
    }

    /**
     * opens terminal tab in tab container for specified host in default
     * location
     *
     * @param ioContainer
     * @param env
     */
    public static void openTerminal(IOContainer ioContainer, String termTitle, ExecutionEnvironment env) {
        TerminalSupportImpl.openTerminalImpl(ioContainer, termTitle, env, null, false, false, 0);
    }

    /**
     * opens terminal tab in tab container and change dir into specified
     * directory
     *
     * @param ioContainer
     * @param env
     */
    public static void openTerminal(IOContainer ioContainer, String termTitle, ExecutionEnvironment env, String dir) {
        TerminalSupportImpl.openTerminalImpl(ioContainer, termTitle, env, dir, false, false, 0);
    }

    /**
     * opens terminal tab in default terminals container and change dir into
     * specified directory
     *
     * @param env
     */
    public static void openTerminal(String termTitle, ExecutionEnvironment env, String dir) {
        openTerminal(termTitle, env, dir, true);
    }

    /**
     * opens terminal tab in default terminals container sets and change dir
     * into specified directory. If pwdFlag terminal tries to set title to
     * user@host - `pwd`
     */
    public static void openTerminal(String termTitle, ExecutionEnvironment env, String dir, boolean pwdFlag) {
        final TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
        Object prev = instance.getClientProperty(TerminalContainerTopComponent.AUTO_OPEN_LOCAL_PROPERTY);
        instance.putClientProperty(TerminalContainerTopComponent.AUTO_OPEN_LOCAL_PROPERTY, Boolean.FALSE);
        try {
            instance.open();
            instance.requestActive();
            IOContainer ioContainer = instance.getIOContainer();
            TerminalSupportImpl.openTerminalImpl(ioContainer, termTitle, env, dir, false, pwdFlag, 0);
        } finally {
            instance.putClientProperty(TerminalContainerTopComponent.AUTO_OPEN_LOCAL_PROPERTY, prev);
        }
    }

    /**
     * Open terminal tab with specified id (try to restore pinned tab state from
     * Preferences)
     *
     * @param id ID of a stored tab, from which we try to restore a state.
     * Default value = 0 which means don't restore the state
     */
    public static void restoreTerminal(String termTitle, ExecutionEnvironment env, String dir, boolean pwdFlag, long id) {
        final TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
        Object prev = instance.getClientProperty(TerminalContainerTopComponent.AUTO_OPEN_LOCAL_PROPERTY);
        instance.putClientProperty(TerminalContainerTopComponent.AUTO_OPEN_LOCAL_PROPERTY, Boolean.FALSE);
        try {
            instance.open();
            instance.requestActive();
            IOContainer ioContainer = instance.getIOContainer();
            TerminalSupportImpl.openTerminalImpl(ioContainer, termTitle, env, dir, false, pwdFlag, id);
        } finally {
            instance.putClientProperty(TerminalContainerTopComponent.AUTO_OPEN_LOCAL_PROPERTY, prev);
        }
    }

    public static Component getToolbarPresenter(Action action) {
        return TerminalSupportImpl.getToolbarPresenter(action);
    }
}
