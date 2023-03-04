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

package org.netbeans.api.debugger.providers;

import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.debugger.LazyActionsManagerListener;

/**
 *
 * @author Martin Entlicher
 */
@LazyActionsManagerListener.Registration(path="unittest/annotated")
public class TestLazyActionsManagerListenerAnnotated extends LazyActionsManagerListener {

    public static Object ACTION_OBJECT = new Object();

    public static Set<TestLazyActionsManagerListenerAnnotated> INSTANCES = new HashSet<TestLazyActionsManagerListenerAnnotated>();

    public TestLazyActionsManagerListenerAnnotated() {
        INSTANCES.add(this);
    }

    @Override
    protected void destroy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
