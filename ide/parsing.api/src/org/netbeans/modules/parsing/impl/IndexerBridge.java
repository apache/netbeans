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

package org.netbeans.modules.parsing.impl;

/**
 * This interface is a pseudoAPI between Parsing and Indexing support.
 * The Parsing support needs to communicate with Indexing and coordinate task planning.
 * Indexing API can use impl dependency on Parsing API to call the necessary services,
 * but Parsing API must use a bridge implemented by Indexing to call back.
 * <p/>
 * This pseudoAPI can be changed at any time without any warning! 
 * 
 * @author sdedic
 * @since 9.2
 */
public interface IndexerBridge {
    /**
     * @return True if the indexer is in any other state than idle.
     */
    public boolean isIndexing();
    
    /**
     * 
     * @return true, if the calling thread executes in the indexer's protected mode.
     */
    public boolean ownsProtectedMode();
}
