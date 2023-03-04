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

package org.netbeans.modules.web.core.jsploader;

/** Reference that holds a value that may expire, because it is outdated.
 *
 * @author Petr Jiricka
 */
public class TimeReference {

    private Object value;
    private long timestamp;

    /** Creates a new instance of TimeBasedCache */
    public TimeReference() {
        value = null;
        timestamp = -1;
    }

    /** Returns the value, if it is still up to date.
     * If the value is not set, or if it is out of date, returns null.
     */
    public synchronized Object get(long currentTimestamp) {
        if (currentTimestamp > timestamp) {
            // out of date
            value = null;
        }
        return value;
    }
    
    /** Puts the given value to the reference,
     * or updates the value if it already exists. As the 
     * timestamp of the value it uses the supplied long value.
     */
    public synchronized void put(Object value, long newTimestamp) {
        if (newTimestamp > timestamp) {
            this.timestamp = newTimestamp;
            this.value = value;
        }
    }
    
}
