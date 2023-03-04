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
 
 /**
 * @author davisn
 */

package org.netbeans.modules.glassfish.javaee.test;

import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestCase;

public class ServerResourceProperties extends NbTestCase {
    private final int SLEEP = 10000;

    public ServerResourceProperties  (String testName) {
        super(testName);
    }


    public void V3PreludeServerProperties() {
        RuntimeTabOperator rto;
        String[] serverList;
        rto = RuntimeTabOperator.invoke();
        Node snode = new Node(rto.tree(), "Servers");
        snode.expand();
        serverList = snode.getChildren();
        for (String server : serverList) {
            if (server.compareTo("Prelude V3") == 0)
                System.out.printf("Found the %s:", server);
            else
                System.err.printf("Problem %s does not exist", server);
        }
        Util.sleep(SLEEP);
        //Node gfv3node = new Node(snode.tree(), "Prelude V3");
    }

    public void VerifyV3PreludeDerbyPool() {
        RuntimeTabOperator rto;
        rto = RuntimeTabOperator.invoke();
        Node derbynode = new Node(rto.tree(), "Servers|Prelude V3|Resources|JDBC|Connection Pools|DerbyPool");
        if (derbynode.isPresent()) {
            System.out.printf("Found JDBC Connection DerbyPool");
        }
        else {
            fail("JDBC Connection DerbyPool missing!");
        }
    }


    public void V3ServerProperties() {
        RuntimeTabOperator rto;
        String[] serverList;
        rto = RuntimeTabOperator.invoke();
        Node snode = new Node(rto.tree(), "Servers");
        snode.expand();
        serverList = snode.getChildren();
        for (String server : serverList) {
            if (server.compareTo("GlassFish V3") == 0)
                System.out.printf("Found the %s:", server);
            else
                System.err.printf("Problem %s does not exist", server);
        }
        Util.sleep(SLEEP);
        //Node gfv3node = new Node(snode.tree(), "GlassFish V3");
    }

    public void VerifyDefaultV3DerbyPool() {
        RuntimeTabOperator rto;
        rto = RuntimeTabOperator.invoke();
        Node derbynode = new Node(rto.tree(), "Servers|GlassFish V3|Resources|JDBC|Connection Pools|DerbyPool");
        if (derbynode.isPresent()) {
            System.out.printf("Found JDBC Connection DerbyPool");
        }
        else {
            fail("JDBC Connection DerbyPool missing!");
        }
    }

    public void VerifyDefaultTimerV3Resouce() {
        RuntimeTabOperator rto;
        rto = RuntimeTabOperator.invoke();
        Node derbynode = new Node(rto.tree(), "Servers|GlassFish V3|Resources|JDBC|JDBC Resources|jdbc/__TimerPool");
        if (derbynode.isPresent()) {
            System.out.printf("Found JDBC Resouce jdbc/sample");
        }
        else {
            fail("JDBC Resoucec jdbc/sample missing!");
        }
    }


}
