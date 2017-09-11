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
package org.netbeans.modules.performance.utilities;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;

/**
 *
 * @author petr cyhelsky
 */
public class PerformanceTestCase2 extends JellyTestCase {

    public PerformanceTestCase2(String testName) {
        super(testName);
    }

    @Override
    public void openDataProjects(String... projects) throws IOException {
        String[] fullPaths = new String[projects.length];
        String altPath = System.getProperty("nb_perf_alt_path");
        for (int i = 0; i < projects.length; i++) {
            if (altPath != null) {
                fullPaths[i] = mergePaths(altPath, getDataDir().getAbsolutePath()) + File.separator + projects[i];
            } else {
                fullPaths[i] = getDataDir().getAbsolutePath() + File.separator + projects[i];
            }
        }

        openProjects(fullPaths);
    }

    private String mergePaths(String altPath, String absolutePath) {
        if (absolutePath.startsWith(altPath)) {
            return absolutePath;
        }
        final String PERF = "performance"; //NOI18N
        StringBuilder sb;
        if (absolutePath.contains(PERF)) {
            sb = new StringBuilder(absolutePath);
            sb.replace(0, absolutePath.indexOf(PERF) + 11, altPath);
        } else {
            sb = new StringBuilder(altPath);
        }
        return sb.toString();
    }

}
