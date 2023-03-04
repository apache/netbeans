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

package org.netbeans.modules.debugger.jpda.ui.views;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * Just an empty implementation for compatibility reasons.
 *
 * It delegates to org.netbeans.modules.debugger.ui.views.View.getSourcesView()
 */
public class SourcesView extends TopComponent implements org.openide.util.HelpCtx.Provider {
    
    
    public SourcesView () {
    }

    public Object readResolve() throws ObjectStreamException {
        try {
            Class c = Lookup.getDefault().lookup(ClassLoader.class).loadClass("org.netbeans.modules.debugger.ui.views.View"); // NOI18N
            Method m = c.getDeclaredMethod("getSourcesView", new Class[] {}); // NOI18N
            Object tc = m.invoke(null, new Object[] {});
            return tc;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerSourcesNode"); // NOI18N
    }

    @Override
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
}
