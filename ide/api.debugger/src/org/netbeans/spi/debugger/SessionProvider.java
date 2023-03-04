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

package org.netbeans.spi.debugger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.netbeans.debugger.registry.ContextAwareServiceHandler;
import org.netbeans.spi.debugger.ContextAwareSupport;

/**
 * Creates a new instance of {@link org.netbeans.api.debugger.Session}
 * for some {@link org.netbeans.api.debugger.DebuggerInfo}.
 *
 * @author Jan Jancura
 */
public abstract class SessionProvider {


    /**
     * Name of the new session.
     *
     * @return name of new session
     */
    public abstract String getSessionName ();

    /**
     * Location of the new session.
     *
     * @return location of new session
     */
    public abstract String getLocationName ();

    /**
     * Returns identifier of {@link org.netbeans.api.debugger.Session} crated
     * by thisprovider.
     *
     * @return identifier of new Session
     */
    public abstract String getTypeID ();

    /**
     * Returns array of services for 
     * {@link org.netbeans.api.debugger.Session} provided by this 
     * SessionProvider.
     *
     * @return array of services
     */
    public abstract Object[] getServices ();

    /**
     * Declarative registration of an SessionProvider implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     * @since 1.16
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        /**
         * An optional path to register this implementation in.
         */
        String path() default "";

    }

    static class ContextAware extends SessionProvider implements ContextAwareService<SessionProvider> {

        private String serviceName;
        private ContextProvider context;
        private SessionProvider delegate;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        private ContextAware(String serviceName, ContextProvider context) {
            this.serviceName = serviceName;
            this.context = context;
        }

        private synchronized SessionProvider getDelegate() {
            if (delegate == null) {
                delegate = (SessionProvider) ContextAwareSupport.createInstance(serviceName, context);
            }
            return delegate;
        }

        public SessionProvider forContext(ContextProvider context) {
            if (context == this.context) {
                return this;
            } else {
                return new SessionProvider.ContextAware(serviceName, context);
            }
        }

        @Override
        public String getSessionName() {
            return getDelegate().getSessionName();
        }

        @Override
        public String getLocationName() {
            return getDelegate().getLocationName();
        }

        @Override
        public String getTypeID() {
            return getDelegate().getTypeID();
        }

        @Override
        public Object[] getServices() {
            return getDelegate().getServices();
        }

        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(ContextAwareServiceHandler.SERVICE_NAME);
            return new SessionProvider.ContextAware(serviceName);
        }
        
        @Override
        public synchronized String toString() {
            return "SessionProvider.ContextAware for service "+serviceName+", context = "+context+", delegate = "+delegate;
        }
        
    }

}

