/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2003-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.spi.search;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.search.provider.SearchInfo;

/**
 * Interface for obtaining information about scope of a search task.
 *
 * Instances are created by SearchScopeDefinitionProvider every time new Search
 * dialog is opened.
 *
 * @author Marian Petras
 */
public abstract class SearchScopeDefinition {

    private List<ChangeListener> changeListeners =
            new ArrayList<ChangeListener>(1);

    /**
     * Identifies type of search scope.
     */
    public abstract @NonNull String getTypeId();

    /**
     * Returns human-readable, localized name of this search scope.
     * 
     * @return  name of this search scope
     */
    public abstract @NonNull String getDisplayName();

    /**
     * Returns an additional information about this search scope.
     * This information may (but may not) be displayed by the scope's
     * display name, possibly rendered using a different font (style, colour).
     * The default implementation returns {@code null}.
     * 
     * @return  string with the additional information,
     *          or {@code null} if no additional information is available
     */
    public @CheckForNull String getAdditionalInfo() {
        return null;
    }
    
    /**
     * Is this search scope applicable at the moment?
     * For example, search scope of all open projects is not applicable if there
     * is no open project.
     * 
     * @return  {@code true} if this search scope is applicable,
     *          {@code false} otherwise
     */
    public abstract boolean isApplicable();
    
    /**
     * Registers a listener listening for changes of applicability. Registered
     * listeners should be notified each time this {@code SearchScope} changes.
     *
     * @param l listener to be registered
     * @see #isApplicable
     */
    public synchronized final void addChangeListener(
            @NonNull ChangeListener l) {
        if (!changeListeners.contains(l)) {
            changeListeners.add(l);
        }
    }

    /**
     * Unregisters a listener listening for changes of applicability. If the
     * passed listener is not currently registered or if the passed listener is {@code null},
     * this method has no effect.
     *
     * @param l listener to be unregistered
     * @see #addChangeListener
     * @see #isApplicable
     */
    public synchronized final void removeChangeListener(
            @NonNull ChangeListener l) {
        changeListeners.remove(l);
    }

    /**
     * This method should be called whenever the state of current search scope
     * changes.
     */
    protected final void notifyListeners() {
        ArrayList<ChangeListener> listenersCopy;
        synchronized (this) {
            listenersCopy = new ArrayList<ChangeListener>(changeListeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener cl : listenersCopy) {
            cl.stateChanged(ev);
        }
    }
    
    /**
     * Returns object defining the actual search scope, i.e. the iterator over
     * {@code FileObject}s to be searched.
     * 
     * @return  {@code SearchInfo} defining the search scope
     */
    public abstract @NonNull SearchInfo getSearchInfo();

    @Override
    public String toString() {
        return getDisplayName();
    }
    
    /**
     * Get priority of this search scope. The lower priority - the higher
     * position in the combo box.
     */
    public abstract int getPriority();

    /**
     * This method is called when this search scope definition is no longer
     * needed.
     */
    public abstract void clean();

    /**
     * This method is called when the search scope is selected in the UI.
     * Default implementation does nothing.
     */
    public void selected() {
    }

    /**
     * Get icon to show in the combo box. The default implementation returns
     * null.
     *
     * @since api.search/1.10
     * @return The icon, or null.
     */
    public @CheckForNull Icon getIcon() {
        return null;
    }
}
