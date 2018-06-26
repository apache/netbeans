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
