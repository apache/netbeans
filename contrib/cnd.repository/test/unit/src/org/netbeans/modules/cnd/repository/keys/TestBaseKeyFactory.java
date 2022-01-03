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
package org.netbeans.modules.cnd.repository.keys;

import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.repository.KeyObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = KeyFactory.class, position = 1)
public class TestBaseKeyFactory extends KeyObjectFactory {

    public static final short SMALL_KEY_HANDLER = 657;
    public static final short LARGE_KEY_HANDLER = 658;

    @Override
    protected short getHandler(Object object) {
        if (object instanceof TestAbstractKey) {
            return ((TestAbstractKey) object).getHandler();
        }
        return super.getHandler(object);
    }

    @Override
    protected SelfPersistent createObject(short handler, RepositoryDataInput stream) throws IOException {
        switch (handler) {
            case SMALL_KEY_HANDLER:
                return new TestSmallKey(stream);
            case LARGE_KEY_HANDLER:
                return new TestLargeKey(stream);
            default:
                return super.createObject(handler, stream);
        }
    }
}
