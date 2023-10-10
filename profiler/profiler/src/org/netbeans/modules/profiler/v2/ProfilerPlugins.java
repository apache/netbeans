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
package org.netbeans.modules.profiler.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.event.ProfilingStateAdapter;
import org.netbeans.lib.profiler.common.event.ProfilingStateEvent;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProfilerPlugins_PluginNotInitialized=<html><b>Profiler plugin failed to initialize:</b><br><br>{0}</html>",
    "ProfilerPlugins_PluginFailed=<html><b>Plugin {0} failed:</b><br><br>{1}</html>"
})
final class ProfilerPlugins {
    
    private final List<ProfilerPlugin> plugins;
    
    
    ProfilerPlugins(ProfilerSession session) {
        Collection<? extends ProfilerPlugin.Provider> providers =
                Lookup.getDefault().lookupAll(ProfilerPlugin.Provider.class);
        
        if (providers.isEmpty()) {
            plugins = null;
        } else {
            List<ProfilerPlugin> _plugins = new ArrayList<>();
            Lookup.Provider project = session.getProject();
            SessionStorage storage = session.getStorage();
            for (ProfilerPlugin.Provider provider : providers) {
                ProfilerPlugin plugin = null;
                try { plugin = provider.createPlugin(project, storage); }
                catch (Throwable t) { handleThrowable(plugin, t); }
                if (plugin != null) _plugins.add(plugin);
            }
            
            if (_plugins.isEmpty()) {
                plugins = null;
            } else {
                session.addListener(new ProfilingStateAdapter() {
                    public void profilingStateChanged(ProfilingStateEvent e) {
                        int state = e.getNewState();
                        if (state == Profiler.PROFILING_STARTED) notifyStarted();
                        else if (state == Profiler.PROFILING_INACTIVE) notifyStopped();
                    }
                });
                plugins = _plugins;
            }
        }
    }
    
    
    boolean hasPlugins() {
        return plugins != null;
    }
    
    List<JMenuItem> menuItems() {
        List<JMenuItem> menus = new ArrayList<>();
        
        if (plugins != null) for (ProfilerPlugin plugin : plugins) {
            try {
                JMenu menu = new JMenu(plugin.getName());
                plugin.createMenu(menu);
                if (menu.getItemCount() > 0) menus.add(menu);
            } catch (Throwable t) {
                handleThrowable(plugin, t);
            }
        }
        
        return menus;
    }
    
    
    void notifyStarting() {
        if (plugins != null) for (ProfilerPlugin plugin : plugins)
            try {
                plugin.sessionStarting();
            } catch (Throwable t) {
                handleThrowable(plugin, t);
            }
    }
    
    void notifyStarted() {
        if (plugins != null) for (ProfilerPlugin plugin : plugins)
            try {
                plugin.sessionStarted();
            } catch (Throwable t) {
                handleThrowable(plugin, t);
            }
    }
    
    void notifyStopping() {
        if (plugins != null) for (ProfilerPlugin plugin : plugins)
            try {
                plugin.sessionStopping();
            } catch (Throwable t) {
                handleThrowable(plugin, t);
            }
    }
    
    void notifyStopped() {
        if (plugins != null) for (ProfilerPlugin plugin : plugins)
            try {
                plugin.sessionStopped();
            } catch (Throwable t) {
                handleThrowable(plugin, t);
            }
    }
    
    private void handleThrowable(final ProfilerPlugin p, final Throwable t) {
        t.printStackTrace(System.err);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String log = t.getLocalizedMessage();
                String msg = p == null ? Bundle.ProfilerPlugins_PluginNotInitialized(log) :
                                  Bundle.ProfilerPlugins_PluginFailed(p.getName(), log);
                ProfilerDialogs.displayError(msg);
            }
        });
    }
    
}
