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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
     * Describes state of the Hudson Job.
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
     * Name of the Hudson Job
     *
     * @return job's name
     */
    public String getName();
    
    /**
     * URL of the Hudson Job
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
