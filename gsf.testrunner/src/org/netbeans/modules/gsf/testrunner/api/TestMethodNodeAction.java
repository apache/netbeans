/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.gsf.testrunner.api;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

/**
 * Action for execution of an arbitrary command using a project's
 * {@code ActionProvider}.
 *
 * @author  Marian Petras
 */
public final class TestMethodNodeAction implements Action {

    private final ActionProvider actionProvider;
    private final Lookup context;
    private final String command;
    private final String name;

    public TestMethodNodeAction(ActionProvider actionProvider,
                                Lookup context,
                                String command,
                                String name) {
        this.actionProvider = actionProvider;
        this.context = context;
        this.command = command;
        this.name = name;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        actionProvider.invokeAction(command, context);
    }

    @Override
    public Object getValue(String key) {
        if (key == null) {
            return null;
        }

        if (key.equals(Action.NAME)) {
            return name;
        } else if (key.equals(Action.ACTION_COMMAND_KEY)) {
            return command;
        } else {
            return null;
        }
    }

    @Override
    public void putValue(String key, Object value) {
        throw new UnsupportedOperationException(
                "This should not be called.");                          //NOI18N
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean b) {
        throw new UnsupportedOperationException(
                "This should not be called.");                          //NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //no property changes - no listeners
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //no property changes - no listeners
    }

}
