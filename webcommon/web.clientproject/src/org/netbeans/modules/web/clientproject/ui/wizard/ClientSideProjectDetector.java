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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Simple detector for existing client side projects.
 */
public final class ClientSideProjectDetector {

    private final Detector detector;


    public ClientSideProjectDetector(File siteRoot) {
        this.detector = findDetector(siteRoot);
    }

    public boolean detected() {
        return detector != Detector.NULL_DETECTOR;
    }

    @CheckForNull
    public String getName() {
        return detector.getName();
    }

    @CheckForNull
    public String getProjectDirPath() {
        return detector.getProjectDirPath();
    }

    @CheckForNull
    public String getTestDirPath() {
        return detector.getTestDirPath();
    }

    private Detector findDetector(File siteRoot) {
        if (siteRoot.isDirectory()) {
            // detect only for existing files
            for (Detector det : createDetectors(siteRoot)) {
                if (det.detected(siteRoot)) {
                    return det;
                }
            }
        }
        return Detector.NULL_DETECTOR;
    }

    private List<Detector> createDetectors(File siteRoot) {
        return Arrays.<Detector>asList(
                new AngularJsDetector(siteRoot),
                // "fallback"
                new StandardJsProjectDetector(siteRoot));
    }

    //~ Inner classes

    private interface Detector {
        /**
         * Check the given site root and return {@code true} if it is "part"
         * of this detector.
         * @param siteRoot site root to be checked
         * @return {@code true} if it is "part" of this detector
         */
        boolean detected(File siteRoot);
        String getName();
        String getProjectDirPath();
        String getTestDirPath();

        Detector NULL_DETECTOR = new Detector() {
            @Override
            public boolean detected(File siteRoot) {
                throw new UnsupportedOperationException();
            }
            @Override
            public String getName() {
                return null;
            }
            @Override
            public String getProjectDirPath() {
                return null;
            }
            @Override
            public String getTestDirPath() {
                return null;
            }
        };
    }

    private static final class StandardJsProjectDetector implements Detector {

        private static final Collection<String> WELL_KNOWN_SITE_ROOTS = Arrays.asList(
                "public_html", // NOI18N
                "public", // NOI18N
                "web", // NOI18N
                "www"); // NOI18N

        private final File siteRoot;


        public StandardJsProjectDetector(File siteRoot) {
            this.siteRoot = siteRoot;
        }

        @Override
        public boolean detected(File siteRoot) {
            if (siteRoot != this.siteRoot) {
                throw new IllegalArgumentException("Unexpected site root given (" + siteRoot + "), expected " + this.siteRoot); //NOI18N
            }
            return WELL_KNOWN_SITE_ROOTS.contains(siteRoot.getName().toLowerCase());
        }

        @Override
        public String getName() {
            return siteRoot.getParentFile().getName();
        }

        @Override
        public String getProjectDirPath() {
            return siteRoot.getParentFile().getAbsolutePath();
        }

        @Override
        public String getTestDirPath() {
            return null;
        }

    }

    private static final class AngularJsDetector implements Detector {

        private static final String NAME = "AngularJsApplication"; // NOI18N
        private static final String SITE_ROOT = "app"; // NOI18N
        private static final String TEST_ROOT = "test"; // NOI18N

        private final File siteRoot;


        private AngularJsDetector(File siteRoot) {
            this.siteRoot = siteRoot;
        }

        @Override
        public boolean detected(File siteRoot) {
            if (siteRoot != this.siteRoot) {
                throw new IllegalArgumentException("Unexpected site root given (" + siteRoot + "), expected " + this.siteRoot); //NOI18N
            }
            if (!SITE_ROOT.equals(siteRoot.getName())) {
                return false;
            }
            if (!getTestDir().isDirectory()) {
                return false;
            }
            return true;
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getProjectDirPath() {
            return getProjectDir().getAbsolutePath();
        }

        @Override
        public String getTestDirPath() {
            return getTestDir().getAbsolutePath();
        }

        private File getProjectDir() {
            return siteRoot.getParentFile();
        }

        private File getTestDir() {
            return new File(getProjectDir(), TEST_ROOT);
        }

    }

}
