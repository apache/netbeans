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
