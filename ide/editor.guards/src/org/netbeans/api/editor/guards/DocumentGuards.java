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

package org.netbeans.api.editor.guards;

/**
 * Allows to inspect document's inaccessible areas.
 * 
 * @author sdedic
 */
public interface DocumentGuards {
    /**
     * Checks whether the particular position is protected.
     * 
     * @param forInsertion
     * @return 
     */
    public boolean isPositionGuarded(int position, boolean forInsertion);
    
    /**
     * Adjust a position to point outside the guarded area.
     * 
     * @param position offset in the document
     * @param direction true for end of the guarded block, false for the beginning
     * @return 
     */
    public int adjustPosition(int position, boolean direction);
    
    /**
     * Finds boundary of the nearest guarded block, starting from the position.
     * Returns {@code -1}, if there is no guarded block in the specified direction.
     * 
     * @param position the position to start at
     * @param direction true forward, false backward
     * @return boundary offset of the nearest guarded block or -1
     */
    public int findNextBlock(int position, boolean direction);
}
