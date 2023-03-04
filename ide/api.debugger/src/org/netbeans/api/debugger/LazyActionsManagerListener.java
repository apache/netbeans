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

package org.netbeans.api.debugger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.netbeans.debugger.registry.ContextAwareServiceHandler;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * This {@link ActionsManagerListener} modification is designed to be
 * registered in {@code "META-INF/debugger/"}, or preferably via the
 * {@link Registration} annotation.
 * LazyActionsManagerListener should be registered for some concrete
 * {@link DebuggerEngine} - use {@code "<DebuggerEngine-id>"} as a path of the
 * annotation, or create
 * {@code "META-INF/debugger/<DebuggerEngine-id>/LazyActionsManagerListener"} file.
 * For global {@link ActionsManager} (do not use the path parameter, or use
 * {@code "META-INF/debugger/LazyActionsManagerListener"} file.
 * New instance of LazyActionsManagerListener implementation is loaded
 * when the new instance of {@link ActionsManager} is created, and its registered
 * automatically to all properties returned by {@link #getProperties}. 
 *
 * @author   Jan Jancura
 */
public abstract class LazyActionsManagerListener extends ActionsManagerAdapter {

        
    /**
     * This method is called when engine dies.
     */
    protected abstract void destroy ();

    /**
     * Returns list of properties this listener is listening on.
     *
     * @return list of properties this listener is listening on
     */
    public abstract String[] getProperties ();
    
    /**
     * Declarative registration of a LazyActionsManagerListener implementation.
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
         * Usually the {@code "<DebuggerEngine-id>"}.
         */
        String path() default "";

    }

    static class ContextAware extends LazyActionsManagerListener implements ContextAwareService<LazyActionsManagerListener> {

        private String serviceName;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        public LazyActionsManagerListener forContext(ContextProvider context) {
            return (LazyActionsManagerListener) ContextAwareSupport.createInstance(serviceName, context);
        }

        @Override
        protected void destroy() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String[] getProperties() {
            throw new UnsupportedOperationException("Not supported.");
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
            return new LazyActionsManagerListener.ContextAware(serviceName);
        }

    }
}
