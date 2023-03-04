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

import org.netbeans.api.debugger.ActionsManagerListener;

import java.util.*;

/**
 * A test actions manager listener.
 *
 * @author Maros Sandor
 */
public class TestActionsManagerListener implements ActionsManagerListener {

    private List performed = new ArrayList();

    public void actionPerformed(Object action) {
        performed.add(action);
    }

    public void actionStateChanged(Object action, boolean enabled) {
    }

    public List getPerformedActions() {
        List listCopy = new ArrayList(performed);
        performed.clear();
        return listCopy;
    }
}
