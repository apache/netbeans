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
package examples;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;

/** Run all tests in the same instance of the IDE.
 *
 * @author shura
 * @author Jiri Skrivanek
 */
public class ExamplesSuite {

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration().
                addTest(ActionsTest.class).
                addTest(EmptyTest.class).
                addTest(NodesTest.class).
                addTest(OperatorsTest.class).
                addTest(OverallTest.class, OverallTest.tests).
                addTest(PropertiesTest.class).
                addTest(WizardsTest.class);
        return conf.clusters(".*").enableModules(".*").honorAutoloadEager(true).suite();
    }
}
