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
package org.openide.util.datatransfer;

import java.awt.datatransfer.*;


/** Event describing change of clipboard content.
*
* @see ExClipboard
*
* @author Jaroslav Tulach
* @version 0.11, May 22, 1997
*/
public final class ClipboardEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -468077075889138021L;

    /** consumed */
    private boolean consumed = false;

    /**
    * @param c the clipboard
    */
    ClipboardEvent(ExClipboard c) {
        super(c);
    }

    /** Get the clipboard where operation occurred.
    * @return the clipboard
    */
    public ExClipboard getClipboard() {
        return (ExClipboard) getSource();
    }

    /** Marks this event consumed. Can be
    * used by listeners that are sure that their own reaction to the event
    * is really significant, to inform other listeners that they need not do anything.
    */
    public void consume() {
        consumed = true;
    }

    /** Has this event been consumed?
     * @return <code>true</code> if it has
    */
    public boolean isConsumed() {
        return consumed;
    }
}
