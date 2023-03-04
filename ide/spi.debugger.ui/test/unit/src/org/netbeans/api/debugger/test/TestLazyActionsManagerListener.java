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

package org.netbeans.api.debugger.test;

import org.netbeans.api.debugger.LazyActionsManagerListener;

import java.util.*;

/**
 * A test lazy actions manager listener.
 *
 * @author Maros Sandor
 */
public class TestLazyActionsManagerListener extends LazyActionsManagerListener {

    private TestActionsManagerListener amListener;

    public TestLazyActionsManagerListener() {
        this.amListener = new TestActionsManagerListener();
    }

    protected void destroy() {
    }

    public String[] getProperties() {
        return null;
    }

    public List getPerformedActions() {
        return amListener.getPerformedActions();
    }

    public void actionPerformed(Object action) {
        amListener.actionPerformed(action);
    }

    public void actionStateChanged(Object action, boolean enabled) {
        amListener.actionStateChanged(action, enabled);
    }
}
