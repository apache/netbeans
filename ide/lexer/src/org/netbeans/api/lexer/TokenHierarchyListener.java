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

package org.netbeans.api.lexer;

/**
 * Listener for chagnes in the token hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenHierarchyListener extends java.util.EventListener {

    /**
     * Token hierarchy has changed in a way described by the event.
     *
     * @param evt event describing the change in the token hierarchy.
     */
    public void tokenHierarchyChanged(TokenHierarchyEvent evt);

}
