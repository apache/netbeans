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
final public class TerminalContainerMuxable extends TerminalContainerCommon {

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
    final protected void addTabWork(JComponent comp) {
	final String cardName = cardName(comp);
	cardContainer.add(cardName, comp);
	cardLayout.addLayoutComponent(comp, cardName);
	components.add(comp);
	notify(comp);
    }

    @Override
    final protected void removeTabWork(JComponent comp) {
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
