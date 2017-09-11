/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
