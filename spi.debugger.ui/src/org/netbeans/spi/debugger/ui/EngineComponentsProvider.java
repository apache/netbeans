/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
         * @param tcName The ID of the {@link TopComponent}
         * @return A component information.
         */
        public static ComponentInfo create(String tcId) {
            return create(tcId, true);
        }
        
        /**
         * Create a component information about a {@link TopComponent}.
         * @param tcName The ID of the {@link TopComponent}
         * @param opened <code>true</code> if the component should be opened,
         *               <code>false</code> otherwise.
         * @return A component information.
         */
        public static ComponentInfo create(String tcId, boolean opened) {
            return create(tcId, opened, false);
        }
        
        /**
         * Create a component information about a {@link TopComponent}.
         * @param tcName The ID of the {@link TopComponent}
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
