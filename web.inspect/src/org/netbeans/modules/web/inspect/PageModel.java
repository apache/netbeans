/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.Page;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Model of an inspected web-page.
 *
 * @author Jan Stola
 */
public abstract class PageModel extends Page {
    /** Name of the property that is fired when the selection mode is switched on/off. */
    public static final String PROP_SELECTION_MODE = "selectionMode"; // NOI18N
    /** Name of the property that is fired when the synchronization of the selection is switched on/off. */
    public static final String PROP_SYNCHRONIZE_SELECTION = "synchronizeSelection"; // NOI18N
    /** Name of the property that is fired when a rule is selected. */
    public static final String PROP_SELECTED_RULE = "selectedRule"; // NOI18N
    /** Name of the property that is fired when a rule is highlighted. */
    public static final String PROP_HIGHLIGHTED_RULE = "highlightedRule"; // NOI18N
    /** Property change support. */
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

    /**
     * Returns the document node.
     * 
     * @return document node.
     */
    @Override
    public abstract Node getDocumentNode();

    /**
     * Removes the specified node from the document.
     *
     * @param node node to remove.
     */
    public abstract void removeNode(Node node);

    /**
     * Sets (the selector of) the selected rule.
     * 
     * @param selector selector of a selected rule or {@code null} when
     * no rule is selected.
     */
    public abstract void setSelectedSelector(String selector);

    /**
     * Returns (the selector of) the selected rule.
     * 
     * @return selector of the selected rule or {@code null} when no rule
     * is selected.
     */
    public abstract String getSelectedSelector();

    /**
     * Returns the nodes matching the selector of the selected rule.
     *
     * @return nodes matching the selector of the selected rule.
     * Returns an empty list when there is no rule selected.
     */
    public abstract List<? extends Node> getNodesMatchingSelectedRule();

    /**
     * Sets (the selector of) the highlighted rule.
     * 
     * @param selector selector of a highlighted rule or {@code null}
     * when there is no such rule.
     */
    public abstract void setHighlightedSelector(String selector);

    /**
     * Returns (the selector of) the highlighted rule.
     * 
     * @return selector of the highlighted rule or {@code null}
     * when there is no such rule.
     */
    public abstract String getHighlightedSelector();

    /**
     * Switches the selection mode on or off.
     * 
     * @param selectionMode determines whether the selection mode should
     * be switched on or off.
     */
    public abstract void setSelectionMode(boolean selectionMode);

    /**
     * Determines whether the selection mode is switched on or off.
     * 
     * @return {@code true} when the selection mode is switched on,
     * returns {@code false} otherwise.
     */
    public abstract boolean isSelectionMode();

    /**
     * Sets whether the selection between the IDE and the browser pane should
     * be synchronized or not.
     *
     * @param synchronizeSelection determines whether the selection should
     * be synchronized or not.
     */
    public abstract void setSynchronizeSelection(boolean synchronizeSelection);

    /**
     * Determines whether the selection between the IDE and the browser pane
     * should be synchronized or not.
     *
     * @return {@code true} when the selection should be synchronized,
     * returns {@code false} otherwise.
     */
    public abstract boolean isSynchronizeSelection();

    /**
     * Returns CSS Styles view for this page.
     *
     * @return CSS Styles view for this page.
     */
    public abstract CSSStylesView getCSSStylesView();

    /**
     * Returns the owner project of this page.
     * 
     * @return the owner project of this page.
     */
    public abstract Project getProject();

    /**
     * Returns the context of this page.
     * 
     * @return the context of this page.
     */
    public abstract Lookup getPageContext();

    /**
     * Disposes this page model.
     */
    protected abstract void dispose();

    /**
     * Adds a property change listener.
     * 
     * @param listener listener to add.
     */
    @Override
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     * 
     * @param listener listener to remove.
     */
    @Override
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fires the specified property change.
     * 
     * @param propName name of the property.
     * @param oldValue old value of the property or {@code null}.
     * @param newValue new value of the property or {@code null}.
     */
    protected final void firePropertyChange(String propName, Object oldValue, Object newValue) {
        propChangeSupport.firePropertyChange(propName, oldValue, newValue);
    }

    /**
     * CSS Styles view.
     */
    public static interface CSSStylesView {

        /**
         * Returns the visual representation of CSS Styles.
         *
         * @return visual representation of CSS Styles.
         */
        JComponent getView();

        /**
         * Returns the lookup of this view. This lookup will be included
         * in the lookup of the enclosing {@code TopComponent}.
         * 
         * @return lookup of this view.
         */
        Lookup getLookup();

        /**
         * The enclosing {@code TopComponent} has been activated.
         */
        void activated();

        /**
         * The enclosing {@code TopComponent} has been deactivated.
         */
        void deactivated();

    }

}
