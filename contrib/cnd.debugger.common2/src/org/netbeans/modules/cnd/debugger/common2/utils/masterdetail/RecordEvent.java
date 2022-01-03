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
 * Defines an event that encapsulates changes to a Picklist.
 */
public class RecordEvent extends EventObject {
    /** Identifies one or more changes in the lists contents. */
    public static final int CONTENTS_MODIFIED = 0;
    public static final int CONTENTS_UNMODIFIED = 1;
    public static final int CONTENTS_VALID = 2;
    public static final int CONTENTS_INVALID = 3;
    public static final int CONTENTS_ERROR = 4;
    public static final int CONTENTS_NOERROR = 5;
    public static final int CONTENTS_OPTIONS = 6;

    /** * The type of this event; */
    private int type;
    private String text;

    /**
     * Constructs a PicklistDataEvent object.
     *
     * @param source  the source Object (typically <code>this</code>)
     * @param type    an int specifying one of <code>CONTENTS_*</code>.
     */
    public RecordEvent(Object source, int type) {
	super(source);
	this.type = type;
	this.text = null;
    }

    /**
     * Constructs a PicklistDataEvent object.
     *
     * @param source  the source Object (typically <code>this</code>)
     * @param type    an int specifying one of <code>CONTENTS_*</code>.
     */
    public RecordEvent(Object source, int type, String text) {
	super(source);
	this.type = type;
	this.text = text;
    }

    /**
     * Returns the event type. The possible values are one of 'CONTENTS_*'.
     *
     * @return an int representing the type value
     */
    public int getType() {
	return type;
    }

    /**
     * Returns information associated with the event.
     *
     * @return an String representing the informational text
     */
    public String getText() {
	return text;
    }
}
