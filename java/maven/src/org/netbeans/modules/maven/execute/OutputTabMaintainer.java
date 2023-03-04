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

package org.netbeans.modules.maven.execute;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import org.netbeans.modules.maven.options.MavenSettings;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * an output tab manager. 
 * @author mkleint
 * @param <TabContext> custom information to store associated with a tab
 */
public abstract class OutputTabMaintainer<TabContext> {

    private static class AllContext<TabContext> {
        final String name;
        final Class<TabContext> tabContextType;
        final TabContext tabContext;
        AllContext(String name, Class<TabContext> tabContextType, TabContext tabContext) {
            this.name = name;
            this.tabContextType = tabContextType;
            this.tabContext = tabContext;
        }
    }

    protected abstract Class<TabContext> tabContextType();

    /**
     * All tabs which were used for some process which has now ended.
     * These are closed when you start a fresh process.
     */
    private static final Map<InputOutput,AllContext<?>> freeTabs = new WeakHashMap<InputOutput,AllContext<?>>();
    
    protected InputOutput io;
    private final String name;
    
    protected OutputTabMaintainer(String name) {
        assert name != null;
        this.name = name;
    }
    
    
    protected final void markFreeTab() {
        if (MavenSettings.getDefault().isReuseOutputTabs()) { //given that the freeTabs is weak this might be unnecessary but who knows..
            synchronized (freeTabs) {
                assert io != null;
                freeTabs.put(io, new AllContext<TabContext>(name, tabContextType(), createContext()));
            }
        }
    }
    
    protected abstract void reassignAdditionalContext(TabContext tabContext);
    
    protected abstract TabContext createContext();
    
    protected Action[] createNewTabActions() {
        return new Action[0];
    }
    
    public final InputOutput getInputOutput() {
        if (io == null) {
            io = createInputOutput();
        }
        return io;
    }
    
    protected final InputOutput createInputOutput() {
        if (MavenSettings.getDefault().isReuseOutputTabs()) {
            synchronized (freeTabs) {
                for (Map.Entry<InputOutput,AllContext<?>> entry : freeTabs.entrySet()) {
                    InputOutput free = entry.getKey();
                    AllContext<?> allContext = entry.getValue();
                    if (io == null && allContext.name.equals(name) && allContext.tabContextType == tabContextType()) {
                        // Reuse it.
                        io = free;
                        reassignAdditionalContext(tabContextType().cast(allContext.tabContext));
                        try {
                            io.getOut().reset();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        // useless: io.flushReader();
                    } else {
                        // Discard it.
                        free.closeInputOutput();
                    }
                }
                freeTabs.clear();
            }
        }
        //                }
        if (io == null) {
            io = IOProvider.getDefault().getIO(name, createNewTabActions());
            io.setInputVisible(true);
        }
        return io;
    }    

}
