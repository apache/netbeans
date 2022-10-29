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
package org.netbeans.modules.java.lsp.server.ui;

import java.util.concurrent.Callable;
import javax.swing.ActionMap;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Serves as a global context provider and directs to the relevant client's context.
 * The implementation hacks a 'sticky' context through {@link Lookups#executeWith(org.openide.util.Lookup, java.lang.Runnable)},
 * so even if the executed code spawns a {@link org.openide.util.RequestProcessor.Task}, the context will stick to it.
 * 
 * @author sdedic
 */
public class AbstractGlobalActionContext implements ContextGlobalProvider, Lookup.Provider {

    /**
     * Holder class that is inserted & looked up in the default Lookup.
     */
    static final class ContextHolder {
        final Lookup context;

        public ContextHolder(Lookup context) {
            this.context = context;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    @Override
    public Lookup createGlobalContext() {
        return Lookups.proxy(this);
    }
    
    /**
     * Execute the passed callable with the specified action context. The action context
     * affects just the calling thread and will be reset after the callable terminates. Any 
     * {@link org.openide.util.RequestProcessor.Task}s created by the called code will also carry this context.
     * 
     * @param <T> 
     * @param context
     * @param callable code to be executed
     * @return 
     */
    public static <T> T withActionContext(Lookup context, Callable<T> callable) {
        Lookup ctx = new ProxyLookup(Lookups.singleton(new ContextHolder(context)), Lookup.getDefault());
        Throwable[] err = new Throwable[1];
        Object[] result = new Object[1];
        
        try {
            Lookups.executeWith(ctx, () -> {
                try {
                    // possibly cause an event to be fired in a global tracker
                    Utilities.actionsGlobalContext().lookup(ActionMap.class);
                    result[0] = callable.call();
                } catch (RuntimeException | Error ex) {
                    throw ex;
                } catch (Exception ex2) {
                    sneakyThrow(ex2);
                }
            });
        } finally {
            // possibly cause an event to be fired in a global tracker
            Utilities.actionsGlobalContext().lookup(ActionMap.class);
        }
        return (T)result[0];
    }

    @Override
    public Lookup getLookup() {
        ContextHolder h = Lookup.getDefault().lookup(ContextHolder.class);
        if (h == null) {
            return Lookup.EMPTY;
        } else {
            return h.context;
        }
    }
}
