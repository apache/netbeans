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
package org.netbeans.modules.websvc.jaxws.client;

import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;

/* This class provides access to the {@link WebServicesClientSupport}'s private constructor 
 * from outside in the way that this class is implemented by an inner class of 
 * {@link WebServicesClientSupport} and the instance is set into the {@link DEFAULT}.
 */
public abstract class JAXWSClientSupportAccessor {

    public static JAXWSClientSupportAccessor DEFAULT;
    
    // force loading of JAXWSClientSupport class. That will set DEFAULT variable.
    public static JAXWSClientSupportAccessor getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }

        // invokes static initializer of JAXWSClientSupport.class
        // that will assign value to the DEFAULT field above
        Class c = JAXWSClientSupport.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        assert DEFAULT != null : "The DEFAULT field must be initialized";
        return DEFAULT;
    }
    
    public abstract JAXWSClientSupport createJAXWSClientSupport(JAXWSClientSupportImpl spiJAXWSClientSupport);

    public abstract JAXWSClientSupportImpl getJAXWSClientSupportImpl(JAXWSClientSupport wscs);
}
