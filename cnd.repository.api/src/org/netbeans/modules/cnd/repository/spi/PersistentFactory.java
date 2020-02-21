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

package org.netbeans.modules.cnd.repository.spi;

import java.io.IOException;

/**
 * factory which can create/read/write persistent objects
 */
public interface PersistentFactory
{
    /**
     * Repository Serialization 
     * @param out DataOutput to write to
     * @param obj object to write
     * @throws java.io.IOException 
     */
    void write(RepositoryDataOutput out, Persistent obj) throws IOException; 

    /**
     * Repository Deserialization 
     * @param in DataInput to read from
     * @return read object
     * @throws java.io.IOException 
     */
    Persistent read(RepositoryDataInput in) throws IOException;     
}
