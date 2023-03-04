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

package org.netbeans.core.spi.multiview;

import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.multiview.MultiViewElementCallbackDelegate;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


    /** Requester type of class, allowing implementors of MultiViewElement
     * to send requests back to enclosing component and window system. Enclosing
     * component or other part of window system will set the instance of this class to elements upon
     * instantiation or deserialization of the element to receive requests properly. 
     * Implementors of MultiViewElement shall not attempt to serialize
     * the passed callback instance.
     * 
     */
    public final class MultiViewElementCallback {
        
        static {
            AccessorImpl.createAccesor();
        }
        
        private MultiViewElementCallbackDelegate delegate;
        
        MultiViewElementCallback(MultiViewElementCallbackDelegate del) {
            delegate = del;
        }
        
        /** Activates this multi view element in enclosing multi view component
         * context, if enclosing multi view top component is opened.
         */
        public void requestActive() {
            delegate.requestActive();
        }

        /** Selects this multi view element in enclosing component context,
         * if component is opened, but does not activate it 
         * unless enclosing component is in active mode already.
         */
        public void requestVisible () {
            delegate.requestVisible();
        }
        
        /**
         * Creates the default TopComponent actions as defined by the Window System.
         * Should be used by the element when constructing it's own getActions() return value.
         */
        public Action[] createDefaultActions() {
            return delegate.createDefaultActions();
        }
        
        /**
         * Update the multiview's topcomponent title.
         */
        public void updateTitle(String title) {
            delegate.updateTitle(title);
        }
        
        /**
         * Element can check if it's currently the selected element.
         */
        public boolean isSelectedElement() {
            return delegate.isSelectedElement();
        }
        
        /**
         * Returns the enclosing Multiview's topcomponent.
         */
        public TopComponent getTopComponent() {
            return delegate.getTopComponent();
        }
        
    } // end of ActionRequestListener
    
