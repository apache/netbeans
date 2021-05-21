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
package org.netbeans.modules.payara.tooling.admin;

import java.util.HashMap;
import java.util.Map;

/**
 * Payara administration command result.
 * <p>
 * Stores administration command result values and command execution state.
 * Result value is <code>Map</code>.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResultMap<K, V> extends Result<Map<K, V>> {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Value returned by administration command execution. */
    HashMap<K, V> value;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara administration command result for
     * <code>Map</code> result value.
     */
    ResultMap() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get value returned by administration command execution.
     * @return Value returned by administration command execution.
     */
    @Override
    public Map<K, V> getValue() {
        return value;
    }
    
}
