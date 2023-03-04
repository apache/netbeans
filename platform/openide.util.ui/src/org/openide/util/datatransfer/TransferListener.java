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


/** Allows listening to progress of manipulation with ExTransferable.
* So it is notified when the transferable is accepted/rejected by
* an operation or if it is released from a clipboard.
*
* @author Jaroslav Tulach
*/
public interface TransferListener extends java.util.EventListener {
    /** Accepted by a drop operation.
    * @param action One of java.awt.dnd.DndConstants like ACTION_COPY, ACTION_MOVE,
    * ACTION_LINK.
    */
    public void accepted(int action);

    /** The transfer has been rejected.
    */
    public void rejected();

    /** Released from a clipboard.
    */
    public void ownershipLost();
}
