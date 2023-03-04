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
            new ArrayList<>(1);

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
    public final synchronized void addChangeListener(
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
    public final synchronized void removeChangeListener(
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
            listenersCopy = new ArrayList<>(changeListeners);
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
