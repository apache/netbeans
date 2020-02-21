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

package org.netbeans.modules.cnd.debugger.common2.utils.masterdetail;

import java.util.EventObject;

/**
 * Defines an event that encapsulates changes to a Debug Target List.
 */
public class RecordListEvent extends EventObject {
    /** Identifies one or more changes in the lists contents. */
    static int CONTENTS_CHANGED = 0;

    /** * The type of this event; */
    private int type;
    private String hostname;

    /**
     * Constructs a RecordListEvent object.
     *
     * @param source  the source Object (typically <code>this</code>)
     * @param type    an int specifying {@link #CONTENTS_CHANGED}
     * @param hostname remote host that newly modified 
     */
    public RecordListEvent(Object source, int type, String hostname) {
	super(source);
	this.type = type;
	this.hostname = hostname;
    }

    /**
     * Returns the event type. The possible values are:
     * <ul>
     * <li> {@link #CONTENTS_CHANGED}
     * </ul>
     *
     * @return an int representing the type value
     */
    public int getType() {
	return type;
    }

    /**
     * Returns the hostname. The possible values are:
     *
     * @return String representing the remote host name
     */
    public String getHostName() {
	return hostname;
    }
}
