/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;

/**
 *
 * @author Petr Hejl
 */
public class Deadlock126640Test extends ServerRegistryTestBase {

    private static final Logger CONTROL_LOGGER = Logger.getLogger("org.netbeans.modules.j2ee.deployment.impl"); // NOI18N

    private static final long DEADLOCK_TIMEOUT = 10000;

    public Deadlock126640Test(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }

    public void testMain() throws InterruptedException {

        Log.controlFlow(CONTROL_LOGGER, null,
                "THREAD: LOOKUP MSG: Registered bridging listener" // NOI18N
                 + "THREAD: INIT MSG: Entering registry initialization" // NOI18N
                 + "THREAD: LOOKUP MSG: Updating the lookup content" // NOI18N
                 + "THREAD: INIT MSG: Loading server plugins", // NOI18N
                0);

        Thread lookupThread = new LookupThread();
        Thread initThread = new InitThread();
        lookupThread.start();
        initThread.start();

        lookupThread.join(DEADLOCK_TIMEOUT);
        initThread.join(DEADLOCK_TIMEOUT);

        assertFalse(lookupThread.isAlive());
        assertFalse(initThread.isAlive());
    }

    private static class LookupThread extends Thread {

        public LookupThread() {
            super("LOOKUP"); // NOI18N
        }

        @Override
        public void run() {
            ServerInstanceProviderLookup lookup = ServerInstanceProviderLookup.getInstance();
            lookup.lookup(org.netbeans.api.server.ServerInstance.class);
        }
    }

    private static class InitThread extends Thread {

        public InitThread() {
            super("INIT"); // NOI18N
        }

        @Override
        public void run() {
            ServerRegistry.getInstance().getInstances();
        }
    }
}
