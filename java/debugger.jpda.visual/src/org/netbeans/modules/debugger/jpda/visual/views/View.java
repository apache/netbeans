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

package org.netbeans.modules.debugger.jpda.visual.views;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.netbeans.spi.debugger.ui.ViewFactory;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


public class View extends TopComponent implements org.openide.util.HelpCtx.Provider {
    
    public static final String EVENTS_VIEW_NAME = "EventsView";
    
    /**
     * The serializing class.
     * For compatibility reasons.
     */
    private static final class ResolvableHelper implements Externalizable {
        
        private String name;
        
        private static final long serialVersionUID = 1L;
        
        public ResolvableHelper(String name) {
            this.name = name;
        }
        
        public ResolvableHelper() {
            // Just for the purpose of deserialization
        }
        
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(name);
        }
        
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            name = (String) in.readObject();
        }
        
        public Object readResolve() {
            return View.getView(name);
        }
    }
    
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    public static synchronized TopComponent getEventsView() {
        return ViewFactory.getDefault().createViewTC(
            "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint.gif",
            EVENTS_VIEW_NAME,
            "NetbeansDebuggerEventNode",
            null,
            NbBundle.getMessage(View.class, "CTL_Events_view"),
            NbBundle.getMessage(View.class, "CTL_Events_view_tooltip")
        );
    }
    
    public static TopComponent getView(String viewName) {
        if (viewName.equals(EVENTS_VIEW_NAME)) {
            return getEventsView();
        }
        throw new IllegalArgumentException(viewName);
    }
    
}
