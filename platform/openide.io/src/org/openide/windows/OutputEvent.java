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

package org.openide.windows;

/** Event fired when something happens to a line in the Output Window.
*
* @author Jaroslav Tulach, Petr Hamernik
* @version 0.11 Dec 01, 1997
*/
public abstract class OutputEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4809584286971828815L;
    /** Create an event.
    * @param src the tab in question
    */
    public OutputEvent (InputOutput src) {
        super (src);
    }

    /** Get the text on the line.
    * @return the text
    */
    public abstract String getLine ();

    /** Get the Output Window tab in question.
    * @return the tab
    */
    public InputOutput getInputOutput() {
        return (InputOutput) getSource();
    }
}
