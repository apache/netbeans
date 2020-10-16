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

package org.netbeans.spi.debugger.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import javax.swing.JComponent;

import org.netbeans.modules.debugger.ui.registry.DebuggerProcessor;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Support for "New Breakpoint" dialog and Breakpoint Customizer. Represents
 * one breakpoint type.
 *
 * @author   Jan Jancura
 */
public abstract class BreakpointType {

    /**
     * Display name of cathegory of this breakpoint type. Cathegory typically
     * represents one debugger language.
     *
     * @return display name of cathegory of this breakpoint type
     */
    public abstract String getCategoryDisplayName ();

    /**
     * Provide the display name of this breakpoint type.
     * The return value is read from "displayName" attribute of the registry file
     * when this implementation is registered via {@link Registration} annotation.
     * Therefore in this case the implementation should NOT override this method
     * as it's not called.
     *
     * @return display name of this breakpoint type
     */
    public String getTypeDisplayName () {
        return null;
    }

    /**
     * Returns visual customizer for this breakpoint type.
     * <pre style="background-color: rgb(255, 255, 102);">
     * Customizer can not implement the {@link Controller} interface any more,
     * due to a clash of {@link Controller#isValid()} method with
     * {@link javax.swing.JComponent#isValid()}.
     * Override {@link #getController()} method instead.
     * </pre>
     *
     * @return visual customizer for this breakpoint type
     */
    public abstract JComponent getCustomizer ();

    /**
     * Return the implementation of {@link Controller} interface.<br/>
     * It's not desired to implement the {@link Controller} interface
     * by JComponent returned from {@link #getCustomizer()} method, because
     * of the clash of {@link Controller#isValid()} method with
     * {@link javax.swing.JComponent#isValid()}. An explicit implementation
     * should be returned by overriding this method.
     * The default implementation returns <code>null</code>, in which case
     * no {@link Controller} is used.
     *
     * @return Controller implementation or <code>null</code>.
     * @since 2.14
     */
    public Controller getController() {
        return null;
    }

    /**
     * Should return true of this breakpoint type should be default one in 
     * the current context. Default breakpoint type is selected one when the
     * New Breakpoint dialog is opened.
     *
     * @return true of this breakpoint type should be default
     */
    public abstract boolean isDefault ();
    
    /**
     * Declarative registration of an BreakpointType implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     * @since 2.16
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {
        /**
         * The display name, in the form of either a general string to take as is,
         * or a resource bundle reference such as "my.module.Bundle#some_key",
         * or just "#some_key" to load from a "Bundle" in the same package
         * as the registered implementation.
         *
         * @return The display name or resource bundle reference
         */
        String displayName();
        
        /**
         * An optional path to register this implementation in.
         */
        String path() default "";

        /**
         * An optional position in which to register this service relative to others.
         * Lower-numbered services are returned in the lookup result first.
         * Services with no specified position are returned last.
         */
        int position() default Integer.MAX_VALUE;
    }

    static class ContextAware extends BreakpointType implements ContextAwareService<BreakpointType> {

        private String serviceName;
        private String displayName;
        private ContextProvider context;
        private BreakpointType delegate;

        private ContextAware(String serviceName, String displayName) {
            this.serviceName = serviceName;
            this.displayName = displayName;
        }

        private ContextAware(String serviceName, String displayName, ContextProvider context) {
            this.serviceName = serviceName;
            this.displayName = displayName;
            this.context = context;
        }

        private synchronized BreakpointType getDelegate() {
            if (delegate == null) {
                delegate = (BreakpointType) ContextAwareSupport.createInstance(serviceName, context);
            }
            return delegate;
        }

        public BreakpointType forContext(ContextProvider context) {
            if (context == this.context) {
                return this;
            } else {
                return new BreakpointType.ContextAware(serviceName, displayName, context);
            }
        }

        @Override
        public String getTypeDisplayName() {
            if (displayName != null) {
                return displayName;
            }
            return getDelegate().getTypeDisplayName();
        }

        @Override
        public JComponent getCustomizer() {
            return getDelegate().getCustomizer();
        }

        @Override
        public Controller getController() {
            return getDelegate().getController();
        }

        @Override
        public String getCategoryDisplayName() {
            return getDelegate().getCategoryDisplayName();
        }

        @Override
        public boolean isDefault() {
            return getDelegate().isDefault();
        }

        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService<BreakpointType> createService(Map<?, String> attrs) throws ClassNotFoundException {
            String serviceName = attrs.get(DebuggerProcessor.SERVICE_NAME);
            String displayName = attrs.get("displayName");
            return new BreakpointType.ContextAware(serviceName, displayName);
        }

    }
}
