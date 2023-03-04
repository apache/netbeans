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

package org.netbeans.api.debugger;

import org.netbeans.junit.NbTestCase;
import org.netbeans.api.debugger.test.TestDebuggerManagerListener;

import java.beans.PropertyChangeEvent;
import java.util.*;

/**
 * A base utility class for debugger unit tests.
 *
 * @author Maros Sandor
 */
public abstract class DebuggerApiTestBase extends NbTestCase {

    protected DebuggerApiTestBase(String s) {
        super(s);
    }

    protected void assertInstanceOf(String msg, Object obj, Class aClass) {
        assertNotNull("An object is not an instance of "+aClass+", because it is 'null'.", obj);
        if (!aClass.isAssignableFrom(obj.getClass()))
        {
            fail(msg);
        }
    }

    protected static void printEvents(List events) {
        System.out.println("events: " + events.size());
        for (Iterator i = events.iterator(); i.hasNext();) {
            TestDebuggerManagerListener.Event event1 = (TestDebuggerManagerListener.Event) i.next();
            System.out.println("event: " + event1.getName());
            if (event1.getParam() instanceof PropertyChangeEvent) {
                PropertyChangeEvent pce = (PropertyChangeEvent) event1.getParam();
                System.out.println("PCS name: " + pce.getPropertyName());
            }
            System.out.println(event1.getParam());
        }
    }
}
