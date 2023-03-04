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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
 * Tests field breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class DummyTests extends NbTestCase {

    public DummyTests (String s) {
        super (s);
    }

    public void testFinalInActionsProviderSupport () throws Exception {
        final int[] test = {0};
        ActionsProviderSupport aps = new ActionsProviderSupport () {
            public Set getActions () {
                return new HashSet ();
            }
            public void doAction (Object a) {
            }
            public boolean isEnabled (Object a) {
                test[0]++;
                return true;
            }
        };
        aps.isEnabled (null);
        assertEquals ("ActionsProviderSupport test ", 1, test[0]);
    }
}
