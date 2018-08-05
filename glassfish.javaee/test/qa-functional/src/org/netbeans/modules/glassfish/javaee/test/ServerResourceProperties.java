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
 /*
 * @author davisn
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
