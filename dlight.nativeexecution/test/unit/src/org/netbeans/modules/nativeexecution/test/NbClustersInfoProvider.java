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
package org.netbeans.modules.nativeexecution.test;

import java.io.File;
import java.io.FileFilter;
import java.util.TreeSet;
import junit.framework.Assert;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author akrasny
 */
public final class NbClustersInfoProvider {

    private NbClustersInfoProvider() {
    }

    public static String getClusters() {
        // Setting netbeans.dirs makes installedFileLocator work properly
        File[] clusters = findClusters();
        StringBuilder sb = new StringBuilder();
        for (File cluster : clusters) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(cluster.getPath());
        }
        return sb.toString();
    }

    // it's like what org.netbeans.junit.NbModuleSuite does,
    // but reusing NbModuleSuite will cause too massive changes in existing CND tests
    private static File[] findClusters() {
        File netbeans = findNetbeans();
        assert netbeans != null;
        File[] clusters = netbeans.listFiles(new FileFilter() {

            @Override
            public boolean accept(File dir) {
                if (dir.isDirectory()) {
                    File m = new File(new File(dir, "config"), "Modules");
                    return m.exists();
                }
                return false;
            }
        });
        return clusters;
    }

    // it's like what org.netbeans.junit.NbModuleSuite does,
    // but reusing NbModuleSuite will cause too massive changes in existing CND tests
    private static File findNetbeans() {
        try {
            Class<?> lookup = Class.forName("org.openide.util.Lookup"); // NOI18N
            File util = new File(lookup.getProtectionDomain().getCodeSource().getLocation().toURI());
            Assert.assertTrue("Util exists: " + util, util.exists());
            return util.getParentFile().getParentFile().getParentFile();
        } catch (Exception ex) {
            try {
                File nbjunit = new File(NbModuleSuite.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File harness = nbjunit.getParentFile().getParentFile();
                Assert.assertEquals("NbJUnit is in harness", "harness", harness.getName());
                TreeSet<File> sorted = new TreeSet<>();
                File[] listFiles = harness.getParentFile().listFiles();
                if (listFiles != null) {
                    for (File p : listFiles) {
                        if (p.getName().startsWith("platform")) {
                            sorted.add(p);
                        }
                    }
                }
                Assert.assertFalse("Platform shall be found in " + harness.getParent(), sorted.isEmpty());
                return sorted.last();
            } catch (Exception ex2) {
                Assert.fail("Cannot find utilities JAR: " + ex + " and: " + ex2);
            }
            return null;
        }
    }
}
