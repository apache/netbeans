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

package org.netbeans.modules.form;

/**
 * Interface for a property value stored externally, e.g. in a properties file,
 * not generated into the source code. The value is identified by a key.
 * 
 * @author Tomas Pavek
 */
public interface ExternalValue {
    /**
     * Special key representing a request to provide a valid key by form editor.
     * Can be used when a copy of the value is created that should have a new
     * key corresponding e.g. to the component and property names. Form editor
     * will compute and set the key automatically.
     */
    String COMPUTE_AUTO_KEY = "#auto"; // NOI18N

    /**
     * Returns the key identifying the value.
     * @return String key of the represented value
     */
    String getKey();

    /**
     * Returns the represented value.
     * @return represented value
     */
    Object getValue();
}
