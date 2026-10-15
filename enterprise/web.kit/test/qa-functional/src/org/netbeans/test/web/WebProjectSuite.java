/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.web;

import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server;
import org.netbeans.junit.NbModuleSuite;

/**
 * Run all tests in the same instance of the IDE.
 *
 * @author Jiri Skrivanek
 */
public class WebProjectSuite extends J2eeTestCase {

    public WebProjectSuite(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        conf = addServerTests(Server.GLASSFISH, conf, WebProjectValidation14.class, WebProjectValidation14.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, WebProjectValidationEE5.class, WebProjectValidationEE5.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, WebProjectValidationEE6.class, WebProjectValidationEE6.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, WebProjectValidation.class, WebProjectValidation.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, WebProjectValidationNb36WebModule.class, WebProjectValidationNb36WebModule.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, WebSpringProjectValidation.class, WebSpringProjectValidation.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, MavenWebProjectValidationEE5.class, MavenWebProjectValidationEE5.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, MavenWebProjectValidationEE6.class, MavenWebProjectValidationEE6.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, MavenWebProjectValidation.class, MavenWebProjectValidation.TESTS);
        return conf.suite();
    }
}
