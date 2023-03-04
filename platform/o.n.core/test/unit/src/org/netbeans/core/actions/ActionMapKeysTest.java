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

package org.netbeans.core.actions;

import junit.framework.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;

/** Checks that the keys defined in API are really working.
 *
 * @author Jaroslav Tulach
 */
public class ActionMapKeysTest extends TestCase {

    public ActionMapKeysTest (String testName) {
        super (testName);
    }

    public void testJumpNextAction () {
        JumpNextAction a = JumpNextAction.get(JumpNextAction.class);
        assertEquals ("jumpNext", a.getActionMapKey ());
    }

    public void testJumpPrevAction () {
        JumpPrevAction a = JumpPrevAction.get(JumpPrevAction.class);
        assertEquals ("jumpPrev", a.getActionMapKey ());
    }
    
}
