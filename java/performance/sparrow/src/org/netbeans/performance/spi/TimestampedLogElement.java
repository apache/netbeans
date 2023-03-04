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
/*
 * TimestampedLogElement.java
 *
 * Created on October 8, 2002, 2:42 PM
 */

package org.netbeans.performance.spi;

/**
 *
 * @author  Tim Boudreau
 */
public abstract class TimestampedLogElement extends AbstractLogElement implements Timestamped {
    long timestamp=0l;
    /** Creates a new instance of TimestampedLogElement */
    public TimestampedLogElement(String s) {
        super (s);
    }

    public synchronized long getTimeStamp() {
        checkParsed();
        return timestamp;
    }

    /** Parse the String passed to the constructor, which presumably consists
     * of a line from a log file.  This method should populate any instance
     * fields which subclasses will provide accessors for.
     * Subclasses should not call this method directly, but instead call
     * checkParse() in accessors which rely of fully parsed data being
     * available.
     *
     */
    protected void parse() throws ParseException {
    }

}
