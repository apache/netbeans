/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.uihandler;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Jindrich Sedek
 */
class SlownessData {
    private static final String SLOWNESS_DATA = "SlownessData";

    private final long time;
    private final byte[] npsContent;
    private final String latestActionName;
    private final String slownessType;
    
    public SlownessData(long time, byte[] npsContent, String slownessType, String latestActionClassName) {
        this.time = time;
        this.npsContent = npsContent;
        this.slownessType = slownessType;
        this.latestActionName = latestActionClassName;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @return the npsContent
     */
    public byte[] getNpsContent() {
        return npsContent;
    }

    public LogRecord getLogRec(){
        LogRecord rec = new LogRecord(Level.CONFIG, SLOWNESS_DATA);
        rec.setParameters(new Object[]{time, latestActionName, slownessType});
        return rec;
    }

    /**
     * @return the latestActionClassName
     */
    public String getLatestActionName() {
        return latestActionName;
    }

    public String getSlownessType() {
        if (slownessType == null){
            return "LowPerformance";
        }
        return slownessType;
    }

}
