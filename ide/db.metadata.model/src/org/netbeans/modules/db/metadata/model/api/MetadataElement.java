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

package org.netbeans.modules.db.metadata.model.api;

/**
 * Encapsulates a metadata element (catalog, schema, table, etc.).
 *
 * @author Andrei Badea
 */
public abstract class MetadataElement {

    MetadataElement() {}

    /**
     * Returns the metadata element which is the parent of this metadata
     * element.
     *
     * @return the parent.
     */
    public abstract MetadataElement getParent();

    /**
     * Returns the name of this metadata element or {@code null} if
     * this element has no name.
     *
     * @return the name.
     */
    public abstract String getName();

    /**
     * This can be overriden by elements that can have names that are null.  The default
     * is to just use the name provided by the database.
     * @return
     */
    String getInternalName() {
        return getName();
    }
}
