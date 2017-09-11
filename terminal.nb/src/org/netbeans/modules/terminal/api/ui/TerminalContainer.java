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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.terminal.api.ui;

import java.awt.Component;
import java.util.List;
import javax.swing.JComponent;

import org.openide.windows.IOContainer;
import org.openide.windows.TopComponent;

import org.netbeans.modules.terminal.iocontainer.TerminalContainerTabbed;
import org.netbeans.modules.terminal.iocontainer.TerminalContainerMuxable;

/**
 * Help a {@link org.openide.windows.TopComponent} be a an
 * {@link org.openide.windows.IOContainer} of "Terminal"s.
 * <p>
 * Use {@link #create} to get one.
 * <p>
 * Recipe for enhancing a <code>TopComponent</code> ...
 * <ul>
 * <li>
 * Create a stock TopComponent with the IDE.
 * <li>
 * Change it's Layout to be BorderLayout.
 * <li>
 * Optionally have it implement
 * {@link org.netbeans.modules.terminal.api.ui.IOTopComponent}.
 * <li>
 * Add the following code to it:
 * <pre>
 * private TerminalContainer tc;
 *
 * public IOContainer ioContainer() {
 * return tc.ioContainer();
 * }
 *
 * public TopComponent topComponent() {
 * return this;
 * }
 *
 * private void initComponents2() {
 * tc = TerminalContainer.create(this, getName());
 * add(tc);
 * }
 * </pre>
 * <li>
 * Call <code>initComponents2()</code> at the end of the constructor of your
 * TopComponent.
 * <li>
 * Delegate <code>componentActivated()</code> and
 * <code>componentDeactivated()</code> from the <code>TopComponent</code> to the
 * <code>TerminalContainer</code> as follows:
 * <pre>
 * protected void componentActivated() {
 * super.componentActivated();
 * tc.componentActivated();
 * }
 *
 * protected void componentDeactivated() {
 * super.componentDeactivated();
 * tc.componentDeactivated();
 * }
 * </pre>
 * </ul>
 *
 * @author ivan
 */
public abstract class TerminalContainer extends JComponent {

    public static TerminalContainer create(TopComponent tc, String name) {
        return new TerminalContainerTabbed(tc, name);
    }

    public static TerminalContainer createMuxable(TopComponent tc, String name) {
        return new TerminalContainerMuxable(tc, name);
    }

    public abstract IOContainer ioContainer();

    /**
     * Handle delegation from containing TopComponent.
     */
    public abstract void componentActivated();

    /**
     * Handle delegation from containing TopComponent.
     */
    public abstract void componentDeactivated();

    /**
     * Activate search bar in component
     */
    public abstract void activateSearch(JComponent component);

    /**
     * @return all active tab components.
     */
    public abstract List<? extends Component> getAllTabs();
}
