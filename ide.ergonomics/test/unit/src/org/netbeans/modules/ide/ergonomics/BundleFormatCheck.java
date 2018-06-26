/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ide.ergonomics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class BundleFormatCheck extends NbTestCase {

    public BundleFormatCheck(String name) {
        super(name);
    }
    
    public void testVerifyBundleFormat() throws Exception {
        String clusters = System.getProperty("netbeans.dirs");
        assertNotNull("Clusters are specified", clusters);
        
        int cnt = 0;
        String[] paths = clusters.split(File.pathSeparator);
        for (String c : paths) {
            int last = c.lastIndexOf(File.separatorChar);
            String clusterName = c.substring(last + 1).replaceFirst("[0-9\\.]*$", "");
            String basename = "/org/netbeans/modules/ide/ergonomics/" + clusterName;
            String bundleName = basename + "/Bundle.properties";
            URL bundle = FeatureManager.class.getResource(bundleName);
            if (bundle != null) {
                checkBundle(bundle);
                cnt++;
            }
        }
        
        if (cnt == 0) {
            fail("Found no bundles to check for " + clusters);
        }
    }

    private void checkBundle(URL bundle) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(bundle.openStream()));
        for (int num = 0; ;num++) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("#")) {
                continue;
            }
            if (line.contains("=")) {
                while (line.endsWith("\\")) {
                    line = r.readLine();
                }
                continue;
            }
            if (line.trim().isEmpty()) {
                continue;
            }
            fail("Unexpected line " + num + " in " + bundle + ":\n" + line);
        }
    }
}
