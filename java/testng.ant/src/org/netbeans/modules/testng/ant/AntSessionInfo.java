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
package org.netbeans.modules.testng.ant;

import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;

/**
 * Holds information about an <code>AntSession</code>.
 *
 * @author  Marian Petras
 * @see  TestNGAntLogger
 */
final class AntSessionInfo {

    TestNGOutputReader outputReader = null;
    /** */
    private long timeOfTestTaskStart;
    /** */
    private SessionType currentSessionType;
    /**
     * type of the session - one of the <code>SESSION_TYPE_xxx</code> constants
     */
    private SessionType sessionType;

    /** Suite name, defaults to "Ant suite" */
    private String sessionName = "Ant suite";

    /**
     */
    AntSessionInfo() {
    }

    /**
     */
    long getTimeOfTestTaskStart() {
        return timeOfTestTaskStart;
    }

    void setTimeOfTestTaskStart(long time) {
        timeOfTestTaskStart = time;
    }

    SessionType getCurrentSessionType() {
        return currentSessionType;
    }

    void setCurrentSessionType(SessionType currentTaskType) {
        this.currentSessionType = currentTaskType;
    }

    SessionType getSessionType() {
        return sessionType;
    }

    void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    String getSessionName() {
        return sessionName;
    }

    void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

}
