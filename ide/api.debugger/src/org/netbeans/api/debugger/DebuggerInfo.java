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

package org.netbeans.api.debugger;

import java.util.List;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * Contains information needed to start new debugging. Process of starting of
 * debugger can create one or more {@link Session} and one or more
 * {@link DebuggerEngine} and register them to {@link DebuggerManager}. For
 * more information about debugger start process see:
 * {@link DebuggerManager#startDebugging}.
 *
 * @author   Jan Jancura
 */
public final class DebuggerInfo implements ContextProvider {

    private Lookup lookup;


    /**
     * Creates a new instance of DebuggerInfo. Takes varargs since version 1.60.
     *
     * @param typeID identification of DebuggerInfo type. Is used for 
     *      registration of external services.
     * @param services you can register additional services for this 
     *      DebuggerInfo here
     * @return returns a new instance of DebuggerInfo
     */
    public static DebuggerInfo create (
        String typeID,
        Object... services
    ) {
        return new DebuggerInfo (
            typeID, 
            services
        );
    }
    
    private DebuggerInfo (
        String typeID,
        Object[] services
    ) {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 0, services.length);
        s [s.length - 1] = this;
        lookup = new Lookup.Compound (
            new Lookup.Instance (s),
            new Lookup.MetaInf (typeID)
        );
    }

    /**
     * Returns type identification of this session. This parameter is used for
     * registration of additional services in Meta-inf/debugger.
     *
     * @return type identification of this session
     */
//    public String getTypeID () {
//        return typeID;
//    }
    
//    /**
//     * Returns list of services of given type.
//     *
//     * @param service a type of service to look for
//     * @return list of services of given type
//     */
//    public List lookup (Class service) {
//        return lookup.lookup (null, service);
//    }
//    
//    /**
//     * Returns one service of given type.
//     *
//     * @param service a type of service to look for
//     * @return ne service of given type
//     */
//    public Object lookupFirst (Class service) {
//        return lookup.lookupFirst (null, service);
//    }
    
    /**
     * Returns list of services of given type from given folder.
     *
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public <T> List<? extends T> lookup(String folder, Class<T> service) {
        return lookup.lookup (folder, service);
    }
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public <T> T lookupFirst(String folder, Class<T> service) {
        return lookup.lookupFirst (folder, service);
    }
    
    Lookup getLookup () {
        return lookup;
    }
}

