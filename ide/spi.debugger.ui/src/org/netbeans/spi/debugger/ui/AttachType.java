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
 * Support for "Attach ..." dialog. Represents one type of attaching.
 *
 * @author   Jan Jancura
 */
public abstract class AttachType {

    /**
     * Provide the display name of this attach type.
     * The return value is read from "displayName" attribute of the registry file
     * when this implementation is registered via {@link Registration} annotation.
     * Therefore in this case the implementation should NOT override this method
     * as it's not called.
     *
     * @return display name of this Attach Type
     */
    public String getTypeDisplayName () {
        return null;
    }

    /**
     * Returns visual customizer for this Attach Type.
     * <pre style="background-color: rgb(255, 255, 102);">
     * Customizer can not implement the {@link Controller} interface any more,
     * due to a clash of {@link Controller#isValid()} method with
     * {@link javax.swing.JComponent#isValid()}.
     * Override {@link #getController()} method instead.
     * </pre>
     * The customizer can provide help by implementing {@link org.openide.util.HelpCtx.Provider}
     *
     * @return visual customizer for this Attach Type
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
     * Declarative registration of an AttachType implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     * <p>
     * The implementation is always registered in the empty default path.
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
         * An optional position in which to register this service relative to others.
         * Lower-numbered services are returned in the lookup result first.
         * Services with no specified position are returned last.
         */
        int position() default Integer.MAX_VALUE;
    }

    static class ContextAware extends AttachType implements ContextAwareService<AttachType> {

        private String serviceName;
        private String displayName;
        private ContextProvider context;
        private AttachType delegate;

        private ContextAware(String serviceName, String displayName) {
            this.serviceName = serviceName;
            this.displayName = displayName;
        }

        private ContextAware(String serviceName, String displayName, ContextProvider context) {
            this.serviceName = serviceName;
            this.displayName = displayName;
            this.context = context;
        }

        private synchronized AttachType getDelegate() {
            if (delegate == null) {
                delegate = (AttachType) ContextAwareSupport.createInstance(serviceName, context);
            }
            return delegate;
        }

        public AttachType forContext(ContextProvider context) {
            if (context == this.context) {
                return this;
            } else {
                return new AttachType.ContextAware(serviceName, displayName, context);
            }
        }

        @Override
        public String getTypeDisplayName() {
            // We ask the delegate also when an empty displayName is declared.
            // Studio is using this.
            if (displayName != null && !displayName.isEmpty()) {
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

        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static AttachType createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
            String displayName = (String) attrs.get("displayName");
            return new AttachType.ContextAware(serviceName, displayName);
        }
    }

}
