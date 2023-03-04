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

package org.netbeans.modules.options.classic;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** Main explorer - the class remains here for backward compatibility
* with older serialization protocol. Its responsibilty is also
* to listen to the changes of "roots" nodes and open / close
* explorer's top components properly.
*
* @author Ian Formanek, David Simonek, Jaroslav Tulach
*/
public final class NbMainExplorer {

    static final long serialVersionUID=6021472310669753679L;
    //  static final long serialVersionUID=-9070275145808944151L;

    /** Minimal initial height of this top component */
    public static final int MIN_HEIGHT = 150;
    /** Default width of main explorer */
    public static final int DEFAULT_WIDTH = 350;

    /** Static method to obtains the shared instance of NbMainExplorer
    * @return the shared instance of NbMainExplorer
    */
    public static NbMainExplorer getExplorer () {
        if (explorer == null) {
            explorer = new NbMainExplorer ();
        }
        return explorer;
    }

    /** Shared instance of NbMainExplorer */
    private static NbMainExplorer explorer;


    /** Common explorer top component which composites bean tree view
    * to view given context. */
    public static class ExplorerTab extends ExplorerPanel {
        static final long serialVersionUID =-8202452314155464024L;
        /** confirmDelete property name */
        private static final String PROP_CONFIRM_DELETE = "confirmDelete"; // NOI18N
        /** composited view */
        protected TreeView view;
        /** listeners to the root context and IDE settings */
        private PropertyChangeListener weakRcL;
        private NodeListener weakNRcL;

        private NodeListener rcListener;
        /** validity flag */
        private boolean valid = true;
        private boolean rootVis = true;

        public ExplorerTab () {
            super();
            // complete initialization of composited explorer actions

            getActionMap().put("delete", ExplorerUtils.actionDelete(getExplorerManager(), getConfirmDelete()));

            getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
                public void preferenceChange(PreferenceChangeEvent evt) {
                    if (PROP_CONFIRM_DELETE.equals(evt.getKey())) {
                        getActionMap().put("delete", ExplorerUtils.actionDelete(getExplorerManager(), getConfirmDelete()));
                    }
                }
            });
        }

        private static Preferences getPreferences() {
            return NbPreferences.root().node("/org/netbeans/core");  //NOI18N
        }

        /** Getter for ConfirmDelete
         * @return true if the user should asked for confirmation of object delete, false otherwise */
        private static boolean getConfirmDelete() {
            return getPreferences().getBoolean(PROP_CONFIRM_DELETE, true);//NOI18N
        }

        @Override
        public void addNotify () {
            super.addNotify();
            if (view == null) {
                view = initGui ();
                view.setRootVisible(rootVis);

                view.getAccessibleContext().setAccessibleName(NbBundle.getBundle(NbMainExplorer.class).getString("ACSN_ExplorerBeanTree"));
                view.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(NbMainExplorer.class).getString("ACSD_ExplorerBeanTree"));
            }
        }

        /** Transfer focus to view. */
        @SuppressWarnings("deprecation")
        @Override
        public void requestFocus () {
            super.requestFocus();
            if (view != null) {
                view.requestFocus();
            }
        }

        /** Transfer focus to view. */
        @SuppressWarnings("deprecation")
        @Override
        public boolean requestFocusInWindow () {
            super.requestFocusInWindow();
            if (view != null) {
                return view.requestFocusInWindow();
            } else {
                return false;
            }
        }

        /** Initializes gui of this component. Subclasses can override
        * this method to install their own gui.
        * @return Tree view that will serve as main view for this explorer.
        */
        protected TreeView initGui () {
            TreeView v = new BeanTreeView();
            v.setDragSource (true);
            setLayout(new BorderLayout());
            add (v);
            return v;
        }

        /** Sets new root context to view. Name, icon, tooltip
        * of this top component will be updated properly */
        public void setRootContext (Node rc) {
            Node oldRC = getExplorerManager().getRootContext();
            // remove old listener, if possible
            if (weakRcL != null) {
                oldRC.removePropertyChangeListener(weakRcL);
            }
            if (weakNRcL != null) {
                oldRC.removeNodeListener(weakNRcL);
            }
            getExplorerManager().setRootContext(rc);
        }

        public void setRootContext(Node rc, boolean rootVisible) {
            rootVis = rootVisible;
            if (view != null) {
                view.setRootVisible(rootVisible);
            }
            setRootContext(rc);
        }

        public Node getRootContext () {
            return getExplorerManager().getRootContext();
        }

        // Bugfix #5891 04 Sep 2001 by Jiri Rechtacek
        // the title is derived from the root context
        // it isn't changed by a selected node in the tree
        /** Called when the explored context changes.
        * Overriden - we don't want title to change in this style.
        */
        protected void updateTitle () {
            // set name by the root context
            setName(getExplorerManager ().getRootContext().getDisplayName());
        }

        /* Updated accessible name of the tree view */
        @Override
        public void setName(String name) {
            super.setName(name);
            if (view != null) {
                view.getAccessibleContext().setAccessibleName(name);
            }
        }

        /* Updated accessible description of the tree view */
        @Override
        public void setToolTipText(String text) {
            super.setToolTipText(text);
            if (view != null) {
                view.getAccessibleContext().setAccessibleDescription(text);
            }
        }

    } // end of ExplorerTab inner class

    /** Special class for tabs added by modules to the main explorer */

    /** Tab of main explorer. Tries to dock itself to main explorer mode
    * before opening, if it's not docked already.
    * Also deserialization is enhanced in contrast to superclass */
    public static class MainTab extends ExplorerTab implements HelpCtx.Provider {

        private static MainTab DEFAULT;

        public static synchronized MainTab getDefaultMainTab() {
            if (DEFAULT == null) {
                DEFAULT = new MainTab();
            }

            return DEFAULT;
        }

        public HelpCtx getHelpCtx () {
            return ExplorerUtils.getHelpCtx (getExplorerManager ().getSelectedNodes (),
                    new HelpCtx (EnvironmentNode.class));
        }

        /** Called when the explored context changes.
        * Overriden - we don't want title to chnage in this style.
        */
        @Override
        protected void updateTitle () {
            // empty to keep the title unchanged
        }

    } // end of MainTab inner class

}
