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

package org.netbeans.api.debugger.jpda;

import org.netbeans.junit.NbTestCase;

/**
 * Tests attaching cookie and connector. Launches a VM in server mode and tries to attach to it.
 * After successfuly attaching to the VM and stopping in main, this test finished debugging sessiona and terminates.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class ConnectorsTest extends NbTestCase {

    public ConnectorsTest (String s) {
        super (s);
    }

    public void testAttach () throws Exception {

        JPDASupport support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.EmptyApp"
        );
        support.doFinish ();
    }

    public void testListen () throws Exception {

        JPDASupport support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.EmptyApp"
        );
        support.doFinish ();
    }
}
