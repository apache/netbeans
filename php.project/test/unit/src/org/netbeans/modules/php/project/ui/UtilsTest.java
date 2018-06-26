/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 * @author Tomas Mysik
 */
public class UtilsTest extends NbTestCase {

    public UtilsTest(String name) {
        super(name);
    }

    public void testValidUrl() throws Exception {
        final String[] correctUrls = new String[] {
            "http://localhost",
            "http://localhost/",
            "http://localhost/phpProject1",
            "http://localhost:8080/phpProject1",
            "http://localhost/phpProject1?a=b",
            "http://localhost/phpProject1?a=b#c",
            "http://www.swiz.cz/phpProject1#bb45",
            "https://localhost/phpProject1/subdir1/subdir2",
            "https://localhost/phpProject1/subdir1/subdir2/",
            "https://user:pwd@localhost/phpProject1",
            "http://[::1]/",
        };
        final String[] incorrectUrls = new String[] {
            null,
            "",
            "http:/localhost/test",
            "http://local host/test",
            " http://localhost/test",
            "http:/localhost/test ",
            "ftp://www:localhost/test",
            "aaa:/www:localhost/test",
            "test",
            "https://user : pwd @ localhost/phpProject1",
            "https://localhost/phpProject1/s u b d i r 1 / s u b  d i r 2 /",
        };

        for (String url : correctUrls) {
            assertTrue("should be correct url: [" + url + "]", Utils.isValidUrl(url));
        }
        for (String url : incorrectUrls) {
            assertFalse("should be incorrect url: [" + url + "]", Utils.isValidUrl(url));
        }
    }

    // #131023
    public void testValidateSourcesAndCopyTarget() throws Exception {
        final Map<String, String> correctDirs = new HashMap<>();
        correctDirs.put("/home/test/NetBeansProjects/PHPProject", "/home/test/NetBeansProjects/PHPProjectCopy");
        correctDirs.put("/home/test/NetBeansProjects/a", "/home/test/NetBeansProjects/b");
        correctDirs.put("/home/test/NetBeansProjects/PHPProject", "/var/www/PHPProject");
        correctDirs.put("/tmp/PHPProject", "/PHPProject");
        correctDirs.put("C:\\test", "D:\\test");
        final Map<String, String> incorrectDirs = new HashMap<>();
        incorrectDirs.put("/tmp/PHPProject", "/tmp/PHPProject/copy");
        incorrectDirs.put("/tmp/PHPProject/web", "/tmp/PHPProject");

        for (Map.Entry<String, String> entry : correctDirs.entrySet()) {
            String sources = entry.getKey();
            String copyTarget = entry.getValue();
            assertNull("incorrect entry: [" + sources + ", " + copyTarget + "]",
                    Utils.validateSourcesAndCopyTarget(sources, copyTarget));
        }
        for (Map.Entry<String, String> entry : incorrectDirs.entrySet()) {
            String sources = entry.getKey();
            String copyTarget = entry.getValue();
            assertNotNull("correct entry: [" + sources + ", " + copyTarget + "]",
                    Utils.validateSourcesAndCopyTarget(sources, copyTarget));
        }
    }
}
