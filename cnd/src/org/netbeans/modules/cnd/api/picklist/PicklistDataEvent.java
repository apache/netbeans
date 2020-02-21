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

package org.netbeans.modules.cnd.api.picklist;

import java.util.EventObject;

/**
 * Defines an event that encapsulates changes to a Picklist.
 */
public class PicklistDataEvent extends EventObject {
    /** Identifies one or more changes in the lists contents. */
    static final int CONTENTS_CHANGED = 0;

    /** * The type of this event; */
    private final int type;

    /**
     * Constructs a PicklistDataEvent object.
     *
     * @param source  the source Object (typically <code>this</code>)
     * @param type    an int specifying {@link #CONTENTS_CHANGED}
     */
    public PicklistDataEvent(Object source, int type) {
	super(source);
	this.type = type;
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
}
