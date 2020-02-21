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

package org.netbeans.modules.cnd.repository.support;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.repository.spi.*;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class  KeyFactory extends AbstractObjectFactory {
    
    /** default instance */
    private static KeyFactory defaultFactory;
    private static final Object lock = new Object();
    
    protected KeyFactory() {
    }
    
    /** Static method to obtain the factory.
     * @return the factory
     */
    public static KeyFactory getDefaultFactory() {
        if (defaultFactory == null) {
            synchronized (lock) {
                // double check is necessary because
                // it is possible to have concurrent creators serialized on lock
                if (defaultFactory == null) {
                    defaultFactory = Lookup.getDefault().lookup(KeyFactory.class);
                }
            }
            if (defaultFactory == null) {
                throw new UnsupportedOperationException("There is no KeyFactory implementation to be used"); //NOI18N
            }
        }
        return defaultFactory;
    }


    /** Method to serialize a key
     * @param aKey  A key
     * @param aStream A DataOutput Stream
     */
    abstract public void writeKey(Key aKey, RepositoryDataOutput aStream) throws IOException;
    
    /** Method to deserialize a key
     * @param aStream A DataOutput Stream
     * @return A key
     */
    abstract public Key readKey(RepositoryDataInput aStream) throws IOException;
    
    /** Method to serialize a colleaction of keys
     * @param aColliection   A collection of keys
     * @param aStream A DataOutput Stream
     */
    abstract public void writeKeyCollection(Collection<Key> aCollection, RepositoryDataOutput aStream ) throws IOException;
    
    /** Method to deserialize a colleaction of keys
     * @param aColliection   A collection of keys
     * @param aStream A DataOutput Stream
     */
    abstract public void readKeyCollection(Collection<Key> aCollection, RepositoryDataInput aStream) throws IOException;
}
