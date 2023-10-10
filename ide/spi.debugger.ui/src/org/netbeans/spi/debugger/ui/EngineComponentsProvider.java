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

import java.awt.Component;
import java.util.List;
import org.netbeans.modules.debugger.ui.ComponentInfoFromBeanContext;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Provider of components associated with a debugger engine.
 * An instance of this class needs to be registered under the appropriate session/engine
 * ID path by {@link DebuggerServiceRegistration}.
 * <p>
 * The provided components are checked when the appropriate debugger engine is started
 * and opened if necessary. After the debugger engine is finished,
 * {@link EngineComponentsProvider#willCloseNotify(java.util.List)} is called
 * with components that are not used by other running engines and which are subsequently
 * closed.
 * 
 * @author Martin Entlicher
 * @since 2.35
 */
public interface EngineComponentsProvider {
    
    /**
     * Provide a list of components associated with the debugger engine.
     * The components are opened when the debugger engine starts and closed
     * when the debugger engine finishes.
     * 
     * @return The list of components associated with the debugger engine.
     */
    List<ComponentInfo> getComponents();
    
    /**
     * This method is called when the debugger engine finishes with the list
     * of components that are about to be closed.
     * The implementation might test them for opened state to decide if they
     * should be opened the next time when {@link #getComponents()} is called.
     * @param components The components that are to be closed. It's guaranteed
     * that only components returned by {@link #getComponents()} are passed in
     * and only those, that are not needed by another running engine.
     */
    void willCloseNotify(List<ComponentInfo> components);
    
    /**
     * Information about a component associated with a debugger engine.
     */
    public static final class ComponentInfo {
        
        static {
            initHelperTransform();
        }
        
        private final ComponentProvider provider;
        private final boolean opened;
        private final boolean minimized;
        //private int order = Integer.MAX_VALUE;
        
        private ComponentInfo(ComponentProvider provider, boolean opened, boolean minimized) {
            this.provider = provider;
            this.opened = opened;
            this.minimized = minimized;
        }
        
        /**
         * Get the component.
         * @return The component
         */
        public Component getComponent() {
            return provider.getComponent();
        }
        
        /**
         * Test if the component is to be opened when the debugger engine starts.
         * @return <code>true</code> when this component is to be opened on engine start,
         *         <code>false</code> otherwise.
         */
        public boolean isOpened() {
            return opened;
        }
        
        /**
         * Test if the component is to be opened in a minimized state when the
         * debugger engine starts.
         * @return <code>true</code> when this component is to be opened in a minimized state,
         *         <code>false</code> otherwise.
         */
        public boolean isMinimized() {
            return minimized;
        }
        
        /* can be added when needed
        public void setOrder(int order) {
            this.order = order;
        }
        
        public int getOrder() {
            return order;
        }
        */
        
        /* When generic Component support is required
        public static ComponentInfo create(ComponentProvider componentProvider) {
            return new ComponentInfo(componentProvider);
        }
        */

        @Override
        public String toString() {
            return "ComponentInfo opened = "+opened+", minimized = "+minimized+", provider = "+provider;
        }
        
        /**
         * Create a component information about a {@link TopComponent},
         * which is opened by default.
         * @param tcId The ID of the {@link TopComponent}
         * @return A component information.
         */
        public static ComponentInfo create(String tcId) {
            return create(tcId, true);
        }
        
        /**
         * Create a component information about a {@link TopComponent}.
         * @param tcId The ID of the {@link TopComponent}
         * @param opened <code>true</code> if the component should be opened,
         *               <code>false</code> otherwise.
         * @return A component information.
         */
        public static ComponentInfo create(String tcId, boolean opened) {
            return create(tcId, opened, false);
        }
        
        /**
         * Create a component information about a {@link TopComponent}.
         * @param tcId The ID of the {@link TopComponent}
         * @param opened <code>true</code> if the component should be opened,
         *               <code>false</code> otherwise.
         * @param minimized <code>true</code> if the component should be opened in a minimized state,
         *                  <code>false</code> otherwise.
         * @return A component information.
         */
        public static ComponentInfo create(String tcId, boolean opened, boolean minimized) {
            return new ComponentInfo(new TopComponentProvider(tcId), opened, minimized);
        }
        
        private static void initHelperTransform() {
            ComponentInfoFromBeanContext.TRANSFORM = new ComponentInfoFromBeanContext.Transform() {
                @Override
                public ComponentInfo transform(final Component c, boolean opened, boolean minimized) {
                    return new ComponentInfo(new ComponentProvider() {
                        @Override
                        public Component getComponent() {
                            return c;
                        }
                    }, opened, minimized);
                }
            };
        }
        
    }
    
    /*public - when generic Component suport is required. */
    static interface ComponentProvider {
        
        Component getComponent();
    }
    
    static class TopComponentProvider implements ComponentProvider {
        
        private final String tcId;
        
        private TopComponentProvider(String tcId) {
            this.tcId = tcId;
        }

        @Override
        public java.awt.Component getComponent() {
            return WindowManager.getDefault().findTopComponent(tcId);
        }

        @Override
        public String toString() {
            return "TopComponentProvider tcId = '"+tcId+"'";
        }
        
    }
    
}
