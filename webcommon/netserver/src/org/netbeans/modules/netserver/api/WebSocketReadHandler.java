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
package org.netbeans.modules.netserver.api;

import java.nio.channels.SelectionKey;


/**
 * Handler of data read (and connections accepted/closed)
 * by a WebSocket server.
 * 
 * @author ads
 * @author Jan Stola
 */
public interface WebSocketReadHandler {

    /**
     * Invoked when a new connection is accepted.
     * 
     * @param key selection key corresponding to the connected channel/socket.
     */
    void accepted(SelectionKey key);

    /**
     * Invoked when some data are received from the peer.
     * 
     * @param key selection key corresponding to the channel/socket
     * from which the data were read.
     * @param message data received from the peer.
     * @param dataType type of the data.
     */
    void read( SelectionKey key , byte[] message , Integer dataType );

    /**
     * Invoked when some connection is closed.
     * 
     * @param key selection key corresponding to the closed channel/socket.
     */
    void closed(SelectionKey key);

}
