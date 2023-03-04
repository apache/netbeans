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

package org.netbeans.modules.gsf.testrunner.api;

import java.util.Set;
import javax.swing.event.ChangeListener;

/**
 * Handles rerunning a test execution.
 *
 * @author Erno Mononen
 */
public interface RerunHandler {

    /**
     * Reruns the test execution.
     */
    void rerun();

    /**
     * Reruns the provided tests.
     * @param type the type of rerun to be executed
     */
    void rerun(Set<Testcase> tests);

    /**
     * @return true if re-running is enabled (i.e. it is possible to
     * rerun the execution and it has finished).
     * @param type the type of rerun to verify
     */
    boolean enabled(RerunType type);

    /**
     * Adds a listener for getting notified about the enabled state.
     * @param listener the listener to add.
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Removes the given listener.
     * 
     * @param listener the listener to remove.
     */
    void removeChangeListener(ChangeListener listener);

}
