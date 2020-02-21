
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
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public abstract class AbstractObjectFactory {

    protected abstract short getHandler(Object object);
    protected abstract SelfPersistent createObject(short handler, RepositoryDataInput stream) throws IOException;
    
    protected final void writeSelfPersistent(SelfPersistent object, RepositoryDataOutput output) throws IOException
    {
        if (object == null) {
            output.writeShort(NULL_POINTER);
        } else {
            int handler = getHandler(object);
            assert LAST_INDEX < handler && handler <= Short.MAX_VALUE;
            output.writeShort(handler);
            object.write(output);
        }
    }
    
    protected final SelfPersistent readSelfPersistent(RepositoryDataInput input) throws IOException
    {
        short handler = input.readShort();
        SelfPersistent object = null;
        if (handler != NULL_POINTER) {
            object = createObject(handler, input);
            assert object != null;
        }
        return object;
    }
    
    public static final short NULL_POINTER = -1;
    
    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX
    public static final short LAST_INDEX = 0; 
}
