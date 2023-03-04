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

package org.netbeans.modules.web.freeform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

// XXX testContextActions
// XXX testLogicalViewActions

/**
 * Test functionality of actions in FreeformProject.
 * This class just tests the basic functionality found in the "jakarta" project.
 * @author Pavel Buzek
 */
public class ActionsWebTest extends TestBaseWeb {

    public ActionsWebTest (String name) {
        super(name);
    }
        
    public void testBasicActions() throws Exception {
        
        
        
        // #79853 - commenting out this test till problem is resovled
        if (true) return ;
        
        
        ActionProvider ap = jakarta.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        List<String> actionNames = new ArrayList<String>(Arrays.asList(ap.getSupportedActions()));
        Collections.sort(actionNames);
        assertEquals("right action names", Arrays.asList(new String[] {"build", "clean", "compile.single", "copy", "debug", "delete", "javadoc", "move", "rebuild", "redeploy", "rename", "run", "test"}), actionNames);
        assertTrue("clean is enabled", ap.isActionEnabled("clean", Lookup.EMPTY));
        try {
            ap.isActionEnabled("frobnitz", Lookup.EMPTY);
            fail("Should throw IAE for unrecognized commands");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        try {
            ap.invokeAction("goetterdaemmerung", Lookup.EMPTY);
            fail("Should throw IAE for unrecognized commands");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        // XXX actually test running the action? how to know when it is done though? there is no API for that...
        // when Ant logger API is available, could provide a null InputOutput impl, and test that the right messages are logged
    }
    
}
