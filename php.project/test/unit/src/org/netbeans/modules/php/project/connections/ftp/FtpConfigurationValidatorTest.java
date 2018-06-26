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
package org.netbeans.modules.php.project.connections.ftp;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.validation.ValidationResult;

public class FtpConfigurationValidatorTest extends NbTestCase {


    private static final String LOCALHOST = "localhost";
    private static final String FOREIGNHOST = "oracle.com";
    private static final String PORT = "22";
    private static final String USER = "john";
    private static final String INITIAL_DIRECTORY = "/pub";
    private static final String TIMEOUT = "30";
    private static final String KEEP_ALIVE_INTERVAL = "10";
    private static final String EXTERNAL_IP = "10.100.0.1";
    private static final String EXTERNAL_PORT_MIN = "100";
    private static final String EXTERNAL_PORT_MAX = "200";


    public FtpConfigurationValidatorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cleanupProxy();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanupProxy();
    }

    public void testValidate() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidHost() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(null, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("host", result.getErrors().get(0).getSource());
    }

    public void testInvalidPort() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, null, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port", result.getErrors().get(0).getSource());
    }

    public void testInvalidUser() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, false, null, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("user", result.getErrors().get(0).getSource());
    }

    public void testAnonymousLogin() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, null, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidInitialDirectory() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, null, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("initialDirectory", result.getErrors().get(0).getSource());
    }

    public void testInvalidTimeout() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, null, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("timeout", result.getErrors().get(0).getSource());
    }

    public void testInvalidKeepAliveInterval() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, null, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("keepAliveInterval", result.getErrors().get(0).getSource());
    }

    public void testNoProxyPassiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoProxyActiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testProxyPassiveModeLocalhost() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testProxyPassiveModeForeignhost() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(FOREIGNHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertEquals(1, result.getWarnings().size());
        assertEquals("proxy", result.getWarnings().get(0).getSource());
    }

    public void testProxyActiveModeLocalhost() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testProxyActiveModeForeignhost() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(FOREIGNHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals("proxy", result.getErrors().get(0).getSource());
        assertEquals("proxy", result.getWarnings().get(0).getSource());
    }

    public void testInvalidExternalIp() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, "invalid ip address", EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("external.ip", result.getErrors().get(0).getSource());
    }

    public void testNoExternalIp() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, "", EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidExternalIpPassiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, "invalid ip address", EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidPortRange01() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, "a", "b")
                .getResult();
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port.min", result.getErrors().get(0).getSource());
        assertEquals("port.max", result.getErrors().get(1).getSource());
    }

    public void testInvalidPortRange02() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, "50", "20")
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port.min", result.getErrors().get(0).getSource());
    }

    public void testInvalidPortRange03() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, "a", "")
                .getResult();
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port.min", result.getErrors().get(0).getSource());
        assertEquals("port.max", result.getErrors().get(1).getSource());
    }

    public void testEmptyPortRange() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, "", "")
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidPortRangePassiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, "a", "b")
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    private void setupProxy() {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "http://some.proxy.org");
        System.getProperties().put("proxyPort", "8080");
    }

    private void cleanupProxy() {
        System.getProperties().put("proxySet", "false");
        System.getProperties().put("proxyHost", "");
        System.getProperties().put("proxyPort", "");
    }

}
