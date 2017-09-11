/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing the
 * software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javahelp;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/**
 *
 * @author jhavlin
 */
public class HelpSetRegistrationProcessorTest extends NbTestCase {

    public static final String PROP_TMP_DIR = "java.io.tmpdir";

    public HelpSetRegistrationProcessorTest(String name) {
        super(name);
    }

    /** Use safe temp directory if the default one contains problematic
     * characters (e.g. plus sign "+" on Mac).
     * 
     * #201194.
     * 
     * We have to use java.io.File and File.createTempFile instead of NetBeans
     * specific classes and utility methods, as the code is called at 
     * compile-time.
     *
     * @throws IOException 
     */
    public void testCreateTempFile() throws IOException {

        if (Utilities.isWindows()) {
            if (!new File("c:\\temp").isDirectory()) {
                return;
            }
        } else if (Utilities.isUnix()) {
            if (!new File("/tmp").isDirectory()) {
                return;
            }
        } else {
            return;
        }

        // Create a problematic directory.
        File tempRoot = File.createTempFile("temp+Root", "");
        tempRoot.delete();
        tempRoot.mkdir();

        // Set the problematic directory as default temp directory.
        String origTmpDir = System.getProperty(PROP_TMP_DIR);
        System.setProperty(PROP_TMP_DIR, tempRoot.getAbsolutePath());

        File tempFile = null;
        File safeFile = null;

        try {

            assertFalse(
                    HelpSetRegistrationProcessor.isUrlCompatible(
                    tempRoot));

            tempFile = File.createTempFile("tempFile", ".tmp");

            if (!tempRoot.getAbsolutePath().equals(
                    tempFile.getParentFile().getAbsolutePath())) {
                // Default temp directory location hasn't been affected by
                // changing system property java.io.tmpdir, skipping the test.
                return;
            }

            // File created in custom method should be created in 
            // a non-problematic directory.
            safeFile = HelpSetRegistrationProcessor.createTempFile(
                    "tempFile2", ".txt");

            System.out.println("Temp root: " + tempRoot.getAbsolutePath());
            System.out.println("Safe temp file: " + safeFile.getAbsolutePath());

            if (Utilities.isWindows() || Utilities.isMac()
                    || Utilities.isUnix()) {

                assertNotSame(tempRoot.getAbsolutePath(), tempFile.getParent());
                HelpSetRegistrationProcessor.isUrlCompatible(
                        safeFile);
            }
        } finally {
            System.setProperty(PROP_TMP_DIR, origTmpDir);
            if (tempFile != null && tempFile.isFile()) {
                tempFile.delete();
            }
            if (safeFile != null && safeFile.isFile()) {
                safeFile.delete();
            }
            tempRoot.delete();
        }
    }
}
