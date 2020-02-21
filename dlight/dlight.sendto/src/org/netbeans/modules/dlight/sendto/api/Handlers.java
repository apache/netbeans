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
package org.netbeans.modules.dlight.sendto.api;

import org.netbeans.modules.dlight.sendto.spi.Handler;
import java.util.Collection;
import org.openide.util.Lookup;

/**
 *
 */
public final class Handlers {

    private Handlers() {
    }

    public static Handler getHandler(String id) {
        for (Handler handler : getHandlers()) {
            if (handler.getID().equals(id)) {
                return handler;
            }
        }

        return null;
    }

    public static Handler[] getHandlers() {
        Collection<? extends Handler> all = Lookup.getDefault().lookupAll(Handler.class);
        return all.toArray(new Handler[all.size()]);
    }
}
