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
package org.netbeans.test.jsf.editor;

import junit.framework.Test;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class JavaEE7Suite extends J2eeTestCase {

    public JavaEE7Suite(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        conf = addServerTests(J2eeTestCase.Server.GLASSFISH, conf, new String[0]);
        conf = conf.addTest(ExpressionLang30Test.class, "testOpenProject", "testNoErrors", "testCollection", "testChainedCall", "testCollectionDetailed", "testCollectionConstructor");
        conf = conf.addTest(NamespacesTest.class, "testOpenProjectEE7", "testAutoinsertEE7", "testMarkedUnusedEE7", "testNamespaceUsedEE7");
        return conf.suite();
    }
}
