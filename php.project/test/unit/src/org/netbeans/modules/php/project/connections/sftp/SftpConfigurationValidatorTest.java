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
package org.netbeans.modules.php.project.connections.sftp;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.validation.ValidationResult;

public class SftpConfigurationValidatorTest extends NbTestCase {

    private static final String HOST = "localhost";
    private static final String PORT = "22";
    private static final String USER = "john";
    private static final String INITIAL_DIRECTORY = "/pub";
    private static final String TIMEOUT = "30";
    private static final String KEEP_ALIVE_INTERVAL = "10";

    private final String identityFile;
    private final String knownHostsFile;


    public SftpConfigurationValidatorTest(String name) {
        super(name);
        File dummyTxt = new File(getDataDir(), "dummy.txt");
        assertTrue(dummyTxt.isFile());

        identityFile = dummyTxt.getAbsolutePath();
        knownHostsFile = dummyTxt.getAbsolutePath();
    }

    public void testValidate() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidHost() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(null, PORT, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("host", result.getErrors().get(0).getSource());
    }

    public void testInvalidPort() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, null, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port", result.getErrors().get(0).getSource());
    }

    public void testInvalidUser() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, null, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("user", result.getErrors().get(0).getSource());
    }

    public void testInvalidIdentityFile() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, "nofile", knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("identityFile", result.getErrors().get(0).getSource());
    }

    public void testNoIdentityFile() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, null, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidKnownHostsFile() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, "nofile", INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("knownHostsFile", result.getErrors().get(0).getSource());
    }

    public void testNoKnownHostsFile() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, null, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidInitialDirectory() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, knownHostsFile, null, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("initialDirectory", result.getErrors().get(0).getSource());
    }

    public void testInvalidTimeout() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, null, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("timeout", result.getErrors().get(0).getSource());
    }

    public void testInvalidKeepAliveInterval() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, null)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("keepAliveInterval", result.getErrors().get(0).getSource());
    }

}
