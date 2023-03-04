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

package org.netbeans.api.debugger.providers;

import org.netbeans.spi.debugger.SessionProvider;

/**
 *
 * @author Martin Entlicher
 */
@SessionProvider.Registration(path="unittest")
public class TestSessionProvider extends SessionProvider {

    public static Object ACTION_OBJECT = new Object();

    @Override
    public String getSessionName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocationName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTypeID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object[] getServices() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
