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

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.debugger.registry.ContextAwareServiceHandler;
import org.netbeans.spi.debugger.ContextAwareSupport;

/**
 * Creates a new instance of {@link org.netbeans.api.debugger.DebuggerEngine}
 * for session. DebuggerEngine implements support for one debugger language
 * for session.
 *
 * @author Jan Jancura
 */
public abstract class DebuggerEngineProvider {

    /**
     * Returns set of language names supported by
     * {@link org.netbeans.api.debugger.DebuggerEngine} provided by this
     * DebuggerEngineProvider.
     *
     * @return language name
     */
    public abstract String[] getLanguages ();

    /**
     * Returns identifier of {@link org.netbeans.api.debugger.DebuggerEngine}.
     *
     * @return identifier of DebuggerEngine
     */
    public abstract String getEngineTypeID ();
       
    /**
     * Returns array of services for 
     * {@link org.netbeans.api.debugger.DebuggerEngine} provided by this 
     * DebuggerEngineProvider.
     * If there are instanceof of {@link java.beans.beancontext.BeanContextChildComponentProxy},
     * the provided components are opened when the engine starts and are closed
     * when the debugging session finishes.
     *
     * @return array of services
     */
    public abstract Object[] getServices ();
    
    /**
     * Sets destructor for new {@link org.netbeans.api.debugger.DebuggerEngine} 
     * provided by this instance of DebuggerEngineProvider.
     *
     * @param desctuctor a desctuctor to be used for DebuggerEngine created
     *        by this instance
     */
    public abstract void setDestructor (DebuggerEngine.Destructor desctuctor);
    
    /**
     * Declarative registration of an DebuggerEngineProvider implementation.
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
         * Usually the session ID.
         */
        String path() default "";

    }

    static class ContextAware extends DebuggerEngineProvider implements ContextAwareService<DebuggerEngineProvider> {

        private String serviceName;
        private ContextProvider context;
        private DebuggerEngineProvider delegate;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        private ContextAware(String serviceName, ContextProvider context) {
            this.serviceName = serviceName;
            this.context = context;
        }

        private synchronized DebuggerEngineProvider getDelegate() {
            if (delegate == null) {
                delegate = (DebuggerEngineProvider) ContextAwareSupport.createInstance(serviceName, context);
            }
            return delegate;
        }

        public DebuggerEngineProvider forContext(ContextProvider context) {
            if (context == this.context) {
                return this;
            } else {
                return new DebuggerEngineProvider.ContextAware(serviceName, context);
            }
        }

        @Override
        public String[] getLanguages() {
            return getDelegate().getLanguages();
        }

        @Override
        public String getEngineTypeID() {
            return getDelegate().getEngineTypeID();
        }

        @Override
        public Object[] getServices() {
            return getDelegate().getServices();
        }

        @Override
        public void setDestructor(DebuggerEngine.Destructor desctuctor) {
            getDelegate().setDestructor(desctuctor);
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
            return new DebuggerEngineProvider.ContextAware(serviceName);
        }

    }

}

