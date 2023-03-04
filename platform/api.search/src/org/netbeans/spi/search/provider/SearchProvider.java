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
package org.netbeans.spi.search.provider;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.NotificationLineSupport;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Search provider can register complex search feature to the IDE.
 * 
 * <div class="nonnormative">
 *   <p>Example:</p>
 *   <pre>
 * // use annotation to register this provider
 * {@code @}ServiceProvider(service = SearchProvider.class)
 * public class ExampleSearchProvider extends SearchProvider {
 *
 *   // Create presenter
 *   public Presenter createPresenter(boolean replaceMode) {
 *       return new BasicSearchPresenter(replaceMode);
 *   }
 *
 *   // Replacing is supported.
 *   public boolean isReplaceSupported() {
 *       return true;
 *   }
 *   
 *   // This search provider is always enabled.
 *   public boolean isEnabled() {
 *       return true; // could be disabled on some platforms
 *   }
 * }</pre>
 * </div>
 *
 * @author jhavlin
 */
public abstract class SearchProvider {

    /** Constructor for subclasses. */
    protected SearchProvider() {}

    /**
     * Create presenter for this search provider.
     *
     * @param replaceMode True if the presenter will be used in Replace dialog,
     * false if it will be used in Find dialog.
     * @return New presenter that will be used in the search dialog.
     */
    public abstract @NonNull Presenter createPresenter(boolean replaceMode);

    /**
     * Get title to show in a tab of the search dialog.
     *
     * @return Localized title for this search provider.
     */
    public abstract @NonNull String getTitle();

    /**
     * If replace is supported, this dialog will be shown in replace dialog.
     *
     * @return True if replace is supported by this type of search, false
     * otherwise.
     */
    public abstract boolean isReplaceSupported();

    /**
     * Tells whether this search provider is enabled. It can depend on operating
     * system, type of opened projects, or available databases.
     *
     * @return True if this provider is enabled at the moment, false otherwise.
     */
    public abstract boolean isEnabled();

    /**
     * Presenter for search provider. This class is used to show panel and start
     * the search task.
     * 
     * <div class="nonnormative">
     *   <p>Example:</p>
     *   <pre>
     * {@code
     * class ExampleSearchPresenter
     *       extends BasicSearchProvider.Presenter {
     *
     *   BasicSearchForm form = null;
     *   private boolean replacing;
     *
     *   public ExampleSearchPresenter(boolean replacing) {
     *       this.replacing = replacing;
     *   }

     *   public JComponent getForm() {
     *       if (form == null) {
     *           form = new Form(); // create a new with UI for specifying search settings
     *           form.setName("Example Provider");
     *       }
     *       return form;
     *   }
     *
     *   public SearchComposition<Def> composeSearch() {
     *       // here create a new search composition appropriate for settings 
     *       // specified in the form.
     *   }
     *
     *   public boolean isUsable() {
     *       return form.isUsable();
     *   }
     *
     *   public void addUsabilityChangeListener(ChangeListener cl) {
     *       form.setUsabilityChangeListener(cl);
     *   }
     *
     *   public void clean() {
     *       super.clean();
     *       form.clean();
     *   }    
     * }}</pre>
     * </div>
     */
    public abstract static class Presenter {

        private SearchProvider searchProvider;
        private boolean replacing;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        /**
         * Constructor for subclasses.
         *
         * @param searchProvider Provider this instance is created for.
         * @param replacing True if the presenter should be shown in replace
         * dialog, false if it should be shown in find dialog.
         */
        protected Presenter(SearchProvider searchProvider, boolean replacing) {
            this.searchProvider = searchProvider;
            this.replacing = replacing;
        }

        /**
         * Return a JComponent that contains controls for setting search
         * options. It will be shown as a tab in search dialog.
         *
         * All invocations of this method on this object should return the same
         * object.
         *
         * You should update inner state of this presenter from the component,
         * or store reference to created component in this presenter, so that
         * the current settings can be obtained from the form when a new search
         * is started, i.e. when {@link #composeSearch()} is called.
         */
        public abstract @NonNull JComponent getForm();

        /**
         * Performs search considering current settings in the panel that was
         * returned by {@link #getForm()}
         *
         * @return A new search composition.
         */
        public abstract @NonNull SearchComposition<?> composeSearch();

        /**
         * Test that the current settings specified in associated form are
         * usable for searching.
         *
         * @param notificationLineSupport You can set warning, info or error
         * message shown in the dialog using this object. If you set warning or
         * error messages, call {@link NotificationLineSupport#clearMessages()}
         * before this method returns true.
         * @return True if search can start with the current settings, false
         * otherwise.
         */
        public abstract boolean isUsable(
                @NonNull NotificationLineSupport notificationLineSupport);

        /**
         * Adds a <code>ChangeListener</code> to the listener list. The same
         * listener object may be added more than once, and will be called as
         * many times as it is added. If <code>listener</code> is null, no
         * exception is thrown and no action is taken.
         *
         * @param changeListener the <code>ChangeListener</code> to be added.
         */
        public final void addChangeListener(
                @NullAllowed ChangeListener changeListener) {
            changeSupport.addChangeListener(changeListener);
        }

        /**
         * Removes a <code>ChangeListener</code> from the listener list. If
         * <code>listener</code> was added more than once, it will be notified
         * one less time after being removed. If <code>listener</code> is null,
         * or was never added, no exception is thrown and no action is taken.
         *
         * @param listener the <code>ChangeListener</code> to be removed.
         */
        public final void removeChangeListener(
                @NullAllowed ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        /**
         * Fires a change event to all registered listeners.
         */
        protected final void fireChange() {
            changeSupport.fireChange();
        }

        /**
         * Checks if there are any listeners registered to
         * this<code>ChangeSupport</code>.
         *
         * @return true if there are one or more listeners for the given
         * property, false otherwise.
         */
        public final boolean hasListeners() {
            return changeSupport.hasListeners();
        }

        /**
         * Method called when the dialog is closed. It should release allocated
         * resources. The default implementation does nothing.
         */
        public void clean() {
        }

        /**
         * Get associated search provider.
         *
         * @return Provider this presenter was created for.
         */
        public final SearchProvider getSearchProvider() {
            return searchProvider;
        }

        /**
         * Get replace mode.
         *
         * @return True if the presenter should be shown in replace dialog,
         * false if it should be shown if find dialog.
         */
        public final boolean isReplacing() {
            return replacing;
        }

        /**
         * Get help ID for this type of search. Can return null if it is not
         * available.
         */
        public @CheckForNull HelpCtx getHelpCtx() {
            return null;
        }
    }
}
