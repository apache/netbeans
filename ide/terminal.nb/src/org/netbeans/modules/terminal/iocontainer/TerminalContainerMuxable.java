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
package org.netbeans.modules.terminal.iocontainer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openide.windows.TopComponent;

/**
 * Corresponds to core.io.ui...IOWindow.
 * @author ivan
 */
public final class TerminalContainerMuxable extends TerminalContainerCommon {

    private final JPanel cardContainer = new JPanel();
    private final CardLayout cardLayout = new CardLayout();

    private final Set<JComponent> components = new HashSet<JComponent>();

    private volatile JComponent currentComponent;

    public TerminalContainerMuxable(TopComponent owner, String originalName) {
        super(owner, originalName);
        initComponents();
    }

    @Override
    protected void initComponents() {
	super.initComponents();

	add(cardContainer, BorderLayout.CENTER);
	cardContainer.setLayout(cardLayout);
    }

    @Override
    protected boolean contains(JComponent comp) {
	return components.contains(comp);
    }

    @Override
    protected void restoreAttrsFor(JComponent comp) {
	// no-op
    }

    @Override
    protected final void addTabWork(JComponent comp) {
	final String cardName = cardName(comp);
	cardContainer.add(cardName, comp);
	cardLayout.addLayoutComponent(comp, cardName);
	components.add(comp);
	notify(comp);
    }

    @Override
    protected final void removeTabWork(JComponent comp) {
	cardContainer.remove(comp);
	cardLayout.removeLayoutComponent(comp);
	components.remove(comp);

	JComponent vc = figureVisibleComponent();
	notify(vc);
    }

    //
    // Overrides of TerminalContainer
    //

    @Override
    protected void selectLite(JComponent comp) {
	cardLayout.show(cardContainer, cardName(comp));
	notify(comp);
    }

    @Override
    public JComponent getSelected() {
	return currentComponent;
    }

    @Override
    public List<JComponent> getAllTabs() {
        return new ArrayList<JComponent>(components);
    }

    @Override
    public void setTitleWork(JComponent comp, String title) {
	updateWindowName(title);
    }




    /**
     * Create a unique name for the given component.
     */
    private static String cardName(JComponent comp) {
        return Integer.toString(comp.hashCode());
    }

    /**
     * Declare that 'component' is now the visible component.
     * @param comp
     */
    private void notify(JComponent comp) {
	currentComponent = comp;
	if (currentComponent == null)
	    updateWindowName(null);
	else
	    updateWindowName(currentComponent.getName());
	checkSelectionChange();
    }

    private JComponent figureVisibleComponent() {
	int n = cardContainer.getComponentCount();
	if (n == 0) {
	    assert components.isEmpty();
	    return null;
	}
	assert ! components.isEmpty();
	for (int i = 0; i < n; i++) {
	    JComponent candidate = (JComponent) cardContainer.getComponent(i);
	    if (candidate.isVisible()) {
		return candidate;
	    }
	}
	assert false : "No component is visible";
	return null;
    }
}
