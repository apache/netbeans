/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.server.config;

import java.io.File;
import java.util.Map;
import org.netbeans.modules.glassfish.tooling.server.parser.HttpData;
import org.netbeans.modules.glassfish.tooling.server.parser.HttpListenerReader;
import org.netbeans.modules.glassfish.tooling.server.parser.NetworkListenerReader;
import org.netbeans.modules.glassfish.tooling.server.parser.TargetConfigNameReader;
import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
@Test(groups = {"unit-tests"})
public class DomainConfigReadersTest {

    private static final String DOMAIN_CONFIG_FILE = System.getProperty("user.dir")
            + "/src/test/java/org/netbeans/modules/glassfish/tooling/server/config/domain.xml";

    @Test
    public void testReadAdminPort() {
        File domainXML = new File(DOMAIN_CONFIG_FILE);
        TargetConfigNameReader configNameReader = new TargetConfigNameReader();
        TreeParser.readXml(domainXML, configNameReader);
        String targetConfigName = configNameReader.getTargetConfigName();
        HttpListenerReader httpReader = new HttpListenerReader(targetConfigName);
        NetworkListenerReader networkReader = new NetworkListenerReader(targetConfigName);
        TreeParser.readXml(domainXML, httpReader, networkReader);
        Map<String, HttpData> result = httpReader.getResult();
        result.putAll(networkReader.getResult());
        HttpData adminData = result.get("admin-listener");
        assertTrue(adminData.getPort() == 4848);
    }

}
