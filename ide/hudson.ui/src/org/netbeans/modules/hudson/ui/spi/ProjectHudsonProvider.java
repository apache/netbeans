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

package org.netbeans.modules.hudson.ui.spi;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.api.HudsonFolder;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.Utilities;
import org.openide.util.Lookup;

/**
 * Documents existence of a Hudson job associated with the project.
 * Should be registered to global lookup.
 */
public abstract class ProjectHudsonProvider {

    /**
     * Produces a singleton which delegates to all registered providers.
     * @return a proxy
     */
    public static ProjectHudsonProvider getDefault() {
        return new ProjectHudsonProvider() {
            public @Override Association findAssociation(Project p) {
                for (ProjectHudsonProvider php : Lookup.getDefault().lookupAll(ProjectHudsonProvider.class)) {
                    Association a = php.findAssociation(p);
                    if (a != null) {
                        return a;
                    }
                }
                return null;
            }
            public @Override boolean recordAssociation(Project p, Association a) {
                for (ProjectHudsonProvider php : Lookup.getDefault().lookupAll(ProjectHudsonProvider.class)) {
                    if (php.recordAssociation(p, a)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Determines whether a given project is associated with any Hudson instance.
     * @param project a source project
     * @return an association with Hudson, or null if none is known
     */
    public abstract Association findAssociation(Project project);

    /**
     * Tries to find a source project matching a given association.
     * The default implementation just checks open projects.
     * @param assoc a possible association
     * @return a matching project, or null
     */
    public Project findAssociatedProject(Association assoc) {
        Map<Association,Project> assocs = new HashMap<Association,Project>();
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            Association found = findAssociation(p);
            if (assoc.equals(found)) {
                return p;
            }
            if (!assocs.containsKey(assoc)) {
                assocs.put(found, p);
            } else {
                // In case of ambiguity, do not return anything.
                // XXX better would be for this method to return Collection<Project>
                // so Hyperlinker could try all the alternatives in turn
                assocs.put(found, null);
            }
        }
        if (assoc.getJobName() != null) {
            // May happen that the PHP only knows the server URL, not the specific job.
            Project p = assocs.get(Association.fromString(assoc.getServerUrl()));
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    /**
     * Attempts to record an association between a project and a job.
     * @param project a source project
     * @param assoc a Hudson job, perhaps newly created; or null to clear any association
     * @return true if the association was in fact recorded, false if not
     */
    public abstract boolean recordAssociation(Project project, Association assoc);

    /**
     * An association with a Hudson job.
     */
    public static final class Association {

        private static final Pattern URL_PATTERN = Pattern.compile(
                "(https?://.+?/)(?:view/([^/]+)/)?((?:job/(?:[^/]+)/)*)");//NOI18N

        private final String jobURL;
        /** Server URL (first), sequence of folders, job name (last). */
        private final String[] jobPath;
        private final String viewName;

        /**
         * Creates an association.
         * @param serverURL as {@link #getServerUrl}
         * @param jobName as {@link #getJobName}
         * @throws IllegalArgumentException if parameters have invalid syntax
         */
        public Association(String serverURL, String jobName) throws IllegalArgumentException {
            URI.create(serverURL); // check syntax
            if (!serverURL.endsWith("/")) { // NOI18N
                throw new IllegalArgumentException(serverURL + " must end in a slash"); // NOI18N
            }
            if (jobName != null && (jobName.length() == 0 || !jobName.trim().equals(jobName))) {
                throw new IllegalArgumentException("Must provide a nonempty or null job name: " + jobName); // NOI18N
            }
            if (jobName != null && jobName.indexOf('/') != -1) {
                throw new IllegalArgumentException("No slashes permitted in job name: " + jobName);
            }
            this.jobURL = getStandardJobUrl(serverURL, jobName);
            this.jobPath = new String[]{serverURL, jobName};
            this.viewName = null;
        }

        private static String getStandardJobUrl(String serverURL, String jobName) {
            return jobName != null
                    ? serverURL + "job/" + Utilities.uriEncode(jobName) + "/" // NOI18N
                    : serverURL;
        }

        /**
         * Creates an association.
         *
         * @param jobURL Hudson Job (or View) URL.
         * @throws IllegalArgumentException if parameter has invalid syntax
         * @since hudson/1.32
         */
        public Association(String jobURL) throws IllegalArgumentException {
            URI.create(jobURL); // check syntax
            if (!jobURL.endsWith("/")) { // NOI18N
                throw new IllegalArgumentException(jobURL + " must end in a slash"); // NOI18N
            }
            this.jobURL = jobURL;
            Matcher m = URL_PATTERN.matcher(jobURL);
            if (!m.matches()) {
                throw new IllegalArgumentException("Cannot extract job path: " + jobURL); //NOI18N
            }

            String rawViewName = m.group(2);
            this.viewName = rawViewName == null || rawViewName.isEmpty()
                    ? null
                    : Utilities.uriDecode(rawViewName);

            String rawJobPath = m.group(3);
            this.jobPath = extractJobPath(m.group(1), rawJobPath);
        }

        private static String[] extractJobPath(String rootURL, String rawPath)
                throws IllegalArgumentException {
            if (rawPath == null || rawPath.isEmpty()) {
                return new String[]{rootURL};
            } else {
                String[] elements = rawPath.split("/");                 //NOI18N
                assert elements.length > 0 && (elements.length % 2) == 0;
                String[] result = new String[(elements.length / 2) + 1];
                result[0] = rootURL; // server URL
                for (int i = 0; i < elements.length; i++) {
                    String element = elements[i];
                    if (i % 2 == 0) {
                        assert "job".equals(element);                   //NOI18N
                    } else {
                        String decoded = Utilities.uriDecode(element);
                        if (decoded.trim().isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Empty job name: " + rawPath);      //NOI18N
                        }
                        result[(i / 2) + 1] = decoded;
                    }
                }
                return result;
            }
        }

        /**
         * Creates an association based on a known job.
         * @param job a Hudson job
         * @return an association with the same server URL and job name
         */
        public static Association forJob(HudsonJob job) {
            return new Association(job.getUrl());
        }

        /**
         * @return the root URL of the server ending in slash, e.g. {@code http://deadlock.netbeans.org/}
         */
        public String getServerUrl() {
            return jobPath[0];
        }

        /**
         * @return the code name of the job on that server; may be null
         */
        public String getJobName() {
            if (jobPath.length == 2) {
                return jobPath[1];
            } else if (jobPath.length > 2) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < jobPath.length; i++) {
                    if (sb.length() > 0) {
                        sb.append("/");                                 //NOI18N
                    }
                    sb.append(jobPath[i]);
                }
                return sb.toString();
            } else {
                return null;
            }
        }

        /**
         * Return view name if it was specified in the URL. Can be null.
         *
         * @return The view name if available, null otherwise.
         */
        public String getViewName() {
            return this.viewName;
        }

        /**
         * Get copy of job path. Not private - called from tests.
         */
        String[] getJobPath() {
            return Arrays.copyOf(jobPath, jobPath.length);
        }

        /**
         * Finds the corresponding job on a registered server, if any.
         * @return a job with the name {@link #getJobName} on the server with the same {@link #getServerUrl}, or null
         */
        public @CheckForNull HudsonJob getJob() {
            if (jobPath == null || jobPath.length < 2) { // no job name
                return null;
            }
            HudsonInstance instance = HudsonManager.getInstance(jobPath[0]);
            if (instance == null) {
                return null;
            }
            if (jobPath.length == 2) {
                return findJobByName(instance.getJobs(), jobPath[1]);
            } else {
                HudsonFolder lastFolder = null;
                for (int i = 1; i < jobPath.length; i++) {
                    String name = jobPath[i];
                    if (i == 1) {
                        lastFolder = findFolderByName(instance.getFolders(), name);
                    } else if (i < jobPath.length - 1 && lastFolder != null) {
                        lastFolder = findFolderByName(lastFolder.getFolders(), name);
                    } else if (lastFolder != null) {
                        return findJobByName(lastFolder.getJobs(), name);
                    }
                }
            }
            return null;
        }

        /**
         * Find a folder of specified name in a collection of folders.
         *
         * @return The folder with name {@code name}, or null if not found.
         */
        private HudsonFolder findFolderByName(Collection<HudsonFolder> folders,
                String name) {
            for (HudsonFolder folder : folders) {
                if (name.equals(folder.getName())) {
                    return folder;
                }
            }
            return null;
        }

        /**
         * Find a job of specified name in a collection of jobs.
         *
         * @return The job with name {@code name}, or null if not found.
         */
        private HudsonJob findJobByName(Collection<HudsonJob> jobs,
                String name) {
            for (HudsonJob job : jobs) {
                if (name.equals(job.getName())) {
                    return job;
                }
            }
            return null;
        }

        public @Override boolean equals(Object obj) {
            if (!(obj instanceof Association)) {
                return false;
            }
            return toString().equals(obj.toString());
        }

        public @Override int hashCode() {
            return toString().hashCode();
        }

        /**
         * URL of either job or server root.
         */
        public @Override String toString() {
            return jobURL;
        }

        /**
         * Inverse of {@link #toString}.
         * @return an association based on parsing a Hudson job or root URL, or null
         */
        public static Association fromString(String s) {
            try {
                return new Association(s);
            } catch (IllegalArgumentException x) {
                Logger.getLogger(ProjectHudsonProvider.class.getName()).log(Level.WARNING, "Bad URL: {0}", s);
                return null;
            }
        }

    }

}
