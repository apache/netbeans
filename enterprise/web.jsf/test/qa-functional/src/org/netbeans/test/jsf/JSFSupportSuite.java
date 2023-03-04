/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.jsf;

import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;

/**
 * Run all tests in the same instance of the IDE.
 *
 * @author Jiri Skrivanek
 */
public class JSFSupportSuite extends J2eeTestCase {

    public JSFSupportSuite(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        conf = addServerTests(Server.GLASSFISH, conf, JsfFunctionalEE5Test.class, JsfFunctionalEE5Test.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, JsfFunctionalEE6Test.class, JsfFunctionalEE6Test.TESTS);
        conf = addServerTests(Server.GLASSFISH, conf, JsfFunctionalTest.class, JsfFunctionalTest.TESTS);
        return conf.suite();
    }
}
