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

package org.netbeans.modules.hudson.api;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.awt.ActionReference;

/**
 * Instance of the the Hudson Server
 *
 * @author Michal Mocnak
 */
public interface HudsonInstance extends Comparable<HudsonInstance> {

    /**
     * Path used to load actions for the server instance.
     * A {@code HudsonInstance} object should be in the context lookup.
     * May be used e.g. for the context menu of an instance node.
     * @see ActionReference#path
     * @since 1.12
     */
    String ACTION_PATH = "org-netbeans-modules-hudson/Actions/instance";
    
    /**
     * Name of the Hudson instance
     *
     * @return instance name
     */
    public String getName();
    
    /**
     * Returns version of the hudson instance
     *
     * @return hudson version
     */
    public HudsonVersion getVersion();
    
    /**
     * Returns state of the hudson instance
     * 
     * @return true if the instance is connected
     */
    public boolean isConnected();
    
    /**
     * URL of the Hudson instance
     *
     * @return instance url
     */
    public String getUrl();
    
    /**
     * Returns all Hudson jobs from registered instance
     *
     * @return collection of all jobs
     */
    public Collection<HudsonJob> getJobs();

    /** @since hudson/1.31 */
    Collection<HudsonFolder> getFolders();

    /**
     * Returns all Hudson views from registered instance
     *
     * @return collection of all views
     */
    public Collection<HudsonView> getViews();
    
    HudsonView getPrimaryView();
    
    /**
     * Register HudsonChangeListener
     *
     * @param l HudsonChangeListener
     */
    public void addHudsonChangeListener(HudsonChangeListener l);
    
    /**
     * Unregister HudsonChangeListener
     *
     * @param l HudsonChangeListener
     */
    public void removeHudsonChangeListener(HudsonChangeListener l);

    /**
     * Checks whether this instance's configuration is persisted to disk.
     */
    boolean isPersisted();

    /**
     * Per-instance preferences.
     * @return preferences for various customizations
     */
    Preferences prefs();

    /**
     * Get list of preferred jobs.
     *
     * @return List of preferred jobs (can be empty), or null if no preferred
     * jobs have been set (i.e. all jobs are preferred).
     */
    @CheckForNull List<String> getPreferredJobs();

    /**
     * Set list of preferred jobs. Null can be passed, see
     * {@link #getPreferredJobs()}.
     *
     * Use with caution. Safer way to work with preferred jobs is using
     * {@link HudsonJob#setSalient(boolean)}.
     *
     * @param preferredJobs List of names of preferred jobs, can be null (i.e.
     * all jobs are preferred), or empty list (no job is preferred).
     */
    void setPreferredJobs(@NullAllowed List<String> preferredJobs);

    /**
     * Initiate synchronization: fetching refreshed job data from the server.
     * Will run asynchronously.
     *
     * @param login To prompt for login if the anonymous user cannot even see
     * the job list; set to true for explicit user gesture, false otherwise.
     */
    void synchronize(boolean login);

    /**
     * @return True if connection to the server if forbidden.
     */
    boolean isForbidden();

    /**
     * @return Info about persistence, and persistence-related features of the
     * instance.
     */
    public Persistence getPersistence();

    /**
     * @return Synchronization interval in minutes.
     */
    public int getSyncInterval();

    /**
     * Set synchronization interval.
     *
     * @param syncInterval New synchronization interval in minutes.
     */
    public void setSyncInterval(int syncInterval);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Class holding info about Hudson instance persistence.
     *
     * @author jhavlin
     */
    public static final class Persistence {

        private static final Persistence TRANSIENT_INSTANCE =
                new Persistence(false);
        private static final Persistence PERSISTENT_INSTANCE =
                new Persistence(true);
        private boolean isPersistent;
        private String info;
        private Action newJobAction = null;

        /**
         * Constructor for persistence settings that use default info message.
         */
        private Persistence(boolean isPersistent) {
            this.isPersistent = isPersistent;
            this.info = null;
        }

        /**
         * Constructor for persistence settings that use custom info message.
         *
         * @since 1.27.
         */
        public Persistence(boolean isPersistent, String info, Action newJob) {
            this.isPersistent = isPersistent;
            this.info = (info == null ? "" : info);                     //NOI18N
            this.newJobAction = newJob;
        }

        /**
         * Settings for persistent instances, with default info message.
         */
        public static Persistence persistent() {
            return PERSISTENT_INSTANCE;
        }

        /**
         * Settings for transient instances.
         */
        public static Persistence tranzient() {
            return TRANSIENT_INSTANCE;
        }

        /**
         * Transient instance with a custom info message.
         */
        public static Persistence tranzient(String info) {
            return new Persistence(false, info, null);
        }

        /**
         * Transient instance with a custom info message and custom "New Build"
         * action.
         *
         * @since 1.27
         */
        public static Persistence tranzient(String info, Action newJob) {
            return new Persistence(false, info, newJob);
        }

        /**
         * Instance with default info message.
         *
         * @param persistent True for persistent instance, false for transient
         * instance.
         */
        public static Persistence instance(boolean persistent) {
            if (persistent) {
                return PERSISTENT_INSTANCE;
            } else {
                return TRANSIENT_INSTANCE;
            }
        }

        /**
         * Get info message, or specified default message if no custom message
         * was set.
         */
        public String getInfo(String defaultInfo) {
            if (info == null) {
                return defaultInfo;
            } else {
                return info;
            }
        }

        /**
         * Return true if the instance is persistent, false otherwise.
         */
        public boolean isPersistent() {
            return isPersistent;
        }

        /**
         * @return Custom "New Build" action, can be null.
         * @since 1.27
         */
        public Action getNewJobAction() {
            return newJobAction;
        }
    }
}
