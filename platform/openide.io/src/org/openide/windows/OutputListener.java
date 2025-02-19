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

package org.openide.windows;

/** Listener to actions taken on a line in the Output Window.
*
* @author Jaroslav Tulach
* @version 0.11 Dec 01, 1997
*/
public interface OutputListener extends java.util.EventListener {
    /** Called when a line is selected.
    * @param ev the event describing the line
    */
    public default void outputLineSelected(OutputEvent ev) {}

    /** Called when some sort of action is performed on a line.
    * @param ev the event describing the line
    */
    public default void outputLineAction(OutputEvent ev) {}

    /** Called when a line is cleared from the buffer of known lines.
    * @param ev the event describing the line
    */
    public default void outputLineCleared(OutputEvent ev) {}
}
