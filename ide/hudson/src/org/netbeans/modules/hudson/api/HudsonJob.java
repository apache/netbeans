/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.hudson.api;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileSystem;

/**
 * Instance of the Hudson Job in specified instance
 */
public interface HudsonJob extends Comparable<HudsonJob> {

    /**
     * Describes state of the Jenkins Job.
     * See {@code hudson.model.BallColor}.
     */
    public enum Color {
        blue("blue"), blue_anime("blue_run"), // NOI18N
        yellow("yellow") { // NOI18N
            public @Override String colorizeDisplayName(String displayName) {
                return "<font color='#989800'>" + displayName + "</font>"; // NOI18N
            }
        }, yellow_anime("yellow_run") { // NOI18N
            public @Override String colorizeDisplayName(String displayName) {
                return "<font color='#989800'>" + displayName + "</font>"; // NOI18N
            }
        },
        red("red") { // NOI18N
            public @Override String colorizeDisplayName(String displayName) {
                return "<font color='#A40000'>" + displayName + "</font>"; // NOI18N
            }
        }, red_anime("red_run") { // NOI18N
            public @Override String colorizeDisplayName(String displayName) {
                return "<font color='#A40000'>" + displayName + "</font>"; // NOI18N
            }
        },
        disabled("grey"), disabled_anime("grey"), // NOI18N
        aborted("grey"), aborted_anime("grey"), // NOI18N
        grey("grey"), grey_anime("grey"), // NOI18N
        notbuilt("grey"), notbuilt_anime("grey"), // JENKINS-11013
        secured("secured"); // fake color
        public static @NonNull Color find(@NonNull String name) {
            try {
                // Convert green to blue, see bug 235415.
                String legacyName = name.replace("green", "blue");      //NOI18N
                return valueOf(legacyName);
            } catch (IllegalArgumentException x) {
                Logger.getLogger(HudsonJob.class.getName()).log(Level.WARNING, "#126166/#203886: no known job color {0}", name);
                return grey;
            }
        }
        private final String iconBaseName;
        private Color(String iconBaseName) {this.iconBaseName = iconBaseName;}
        /**
         * Suitable for {@link AbstractNode#setIconBaseWithExtension(String)}.
         */
        public String iconBase() {
            return "org/netbeans/modules/hudson/resources/" + iconBaseName + ".png"; // NOI18N
        }
        /**
         * Adds color to a label if necessary.
         * @param displayName an HTML display name (must have already escaped HTML metachars)
         */
        public String colorizeDisplayName(String displayName) {
            return displayName;
        }
        /**
         * Checks whether this represents a running job.
         */
        public boolean isRunning() {
            return name().endsWith("_anime");
        }
    }
    
    /**
     * Display name of the Hudson Job
     *
     * @return job display name
     */
    public String getDisplayName();
    
    /**
     * Name of the Jenkins Job
     *
     * @return job's name
     */
    public String getName();
    
    /**
     * URL of the Jenkins Job
     *
     * @return job url
     */
    public String getUrl();
    
    /**
     * Views where the job is situated
     * 
     * @return views
     */
    public Collection<HudsonView> getViews();
    
    /**
     * Color of the Hudson Job's state
     *
     * @return job color (state)
     */
    public Color getColor();
    
    /**
     * Returns job's queue state
     *
     * @return true if the job is in queue
     */
    public boolean isInQueue();
    
    /**
     * Returns job's buildable state
     *
     * @return true if the job is buildable
     */
    public boolean isBuildable();
    
    /**
     * Returns number of the last build
     * 
     * @return last build number, or -1 for none
     */
    public int getLastBuild();
    
    /**
     * Returns number of the last stable build
     * 
     * @return last stable build number, or -1 for none
     */
    public int getLastStableBuild();
    
    /**
     * Returns number of the last successful build
     * 
     * @return last successful build number, or -1 for none
     */
    public int getLastSuccessfulBuild();
    
    /**
     * Returns number of the last failed build
     *
     * @return last failed build number, or -1 for none
     */
    public int getLastFailedBuild();

    /**
     * Returns number of the last completed build
     *
     * @return last completed build number, or -1 for none
     */
    public int getLastCompletedBuild();

    /**
     * Obtains a list of recorded builds for the job.
     * @return a possibly empty set of builds
     */
    Collection<? extends HudsonJobBuild> getBuilds();

    /**
     * Starts Hudson job
     */
    public void start();
    
    /**
     * Obtains Hudson server instance owning the job.
     */
    HudsonInstance getInstance();

    /**
     * Obtains a filesystem representing the remote workspace as accessed by Hudson web services.
     */
    FileSystem getRemoteWorkspace();

    /**
     * Normally true, but may be false if a job is not considered interesting on this server.
     */
    boolean isSalient();

    void setSalient(boolean b);

}
