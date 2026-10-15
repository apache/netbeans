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

package org.netbeans.core.startup;

import org.netbeans.Module;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** The default lookup for the system.
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.util.Lookup.class)
public final class MainLookup extends ProxyLookup {
    private static boolean started = false;
    /** currently effective ClassLoader */
    private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    /** inner access to dynamic lookup service for this top mangager */
    private static InstanceContent instanceContent = new InstanceContent ();
    /** dynamic lookup service for this top mangager */
    private static Lookup instanceLookup = new AbstractLookup (instanceContent);

    /**
     * GuiRunLevel has started up.
     * That means that subsequent calls to lookup on ModuleInfo
     * need not try to get it again.
     */
    public static void started() {
        started = true;
    }
    
    /** Schedule all {@link Runnable} in WarmUp folder for invocation.
     * @since 1.30
     * @param delay time in ms to wait with warmup
     * @return task one can wait for
     */
    public static Task warmUp(long delay) {
        return WarmUpSupport.warmUp(delay);
    }
    
    /** Checks whether everything is started.
     */
    static boolean isStarted() {
        return started;
    } 

    public MainLookup () {
        super (new Lookup[] {
                   // #14722: pay attention also to META-INF/services/class.Name resources:
                   Lookups.metaInfServices(classLoader),
                   Lookups.singleton(classLoader),
                   Lookup.EMPTY, // will be moduleLookup
                   instanceLookup
               });
    }

    /** Called when a system classloader changes.
     */
    public static final void systemClassLoaderChanged (ClassLoader nue) {
        if (!(Lookup.getDefault() instanceof MainLookup)) {
            // May be called from MockServices.setServices even though we are not main lookup.
            return;
        }
        if (classLoader != nue) {
            classLoader = nue;
            MainLookup l = (MainLookup)Lookup.getDefault();
            Lookup[] delegates = l.getLookups();
            Lookup[] newDelegates = delegates.clone();
            // Replace classloader.
            newDelegates[0] = Lookups.metaInfServices(classLoader);
            newDelegates[1] = Lookups.singleton(classLoader);
            l.changeLookups(newDelegates);
        } else {
            moduleClassLoadersUp();
        }
    }

    /** Called when modules are about to be turned on.
     */
    public static final void moduleClassLoadersUp() {
        MainLookup l = (MainLookup)Lookup.getDefault();
        Lookup[] newDelegates = null;
        Lookup[] delegates = l.getLookups();
        newDelegates = delegates.clone();
        newDelegates[0] = Lookups.metaInfServices(classLoader);
        l.changeLookups(newDelegates);
    }

    /** Called when Lookup&lt;ModuleInfo&gt; is ready from the ModuleManager.
     * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=28465">28465</a>
     */
    public static final void moduleLookupReady(Lookup moduleLookup) {
        if (Lookup.getDefault() instanceof MainLookup) {
            MainLookup l = (MainLookup)Lookup.getDefault();
            Lookup[] newDelegates = l.getLookups().clone();
            newDelegates[2] = moduleLookup;
            l.changeLookups(newDelegates);
        }
    }

    /** When all module classes are accessible thru systemClassLoader, this
     * method is called to initialize the FolderLookup.
     */

    public static final void modulesClassPathInitialized () {
        modulesClassPathInitialized(CoreBridge.getDefault().lookupCacheLoad());
    }
    static final void modulesClassPathInitialized(Lookup services) {
        // replace the lookup by new one
        Lookup lookup = Lookup.getDefault ();
        StartLog.logProgress ("Got Lookup"); // NOI18N

        ((MainLookup)lookup).doInitializeLookup(services);
    }

    //
    // 
    //
    
    /** Register new instance.
     */
    public static void register (Object obj) {
        instanceContent.add (obj);
    }
    
    /** Register new instance.
     * @param obj source
     * @param conv convertor which postponing an instantiation
     */
    public static <T,R> void register(T obj, InstanceContent.Convertor<T,R> conv) {
        instanceContent.add(obj, conv);
    }
    
    /** Unregisters the service.
     */
    public static void unregister (Object obj) {
        instanceContent.remove (obj);
    }
    /** Unregisters the service registered with a convertor.
     */
    public static <T,R> void unregister (T obj, InstanceContent.Convertor<T,R> conv) {
        instanceContent.remove (obj, conv);
    }
    
    
    
    

    private final void doInitializeLookup(Lookup services) {
        //System.err.println("doInitializeLookup");

        // extend the lookup
        Lookup[] arr = new Lookup[] {
            getLookups()[0], // metaInfServicesLookup
            getLookups()[1], // ClassLoader lookup
            getLookups()[2], // ModuleInfo lookup
            instanceLookup, 
            services,
        };
        StartLog.logProgress ("prepared other Lookups"); // NOI18N

        changeLookups (arr);
        StartLog.logProgress ("Lookups set"); // NOI18N
    //StartLog.logEnd ("MainLookup: initialization of FolderLookup"); // NOI18N
    }
    
    private final ThreadLocal<Boolean> changing = new ThreadLocal<Boolean>();
    final void changeLookups(Lookup[] arr) {
        Boolean prev = changing.get(); 
        try {
            changing.set(true);
            setLookups(arr);
        } finally {
            changing.set(prev);
        }
    }

    @Override
    protected void beforeLookup(Lookup.Template templ) {
        if (Boolean.TRUE.equals(changing.get())) {
            return;
        }
        Class type = templ.getType();

        // Force module system to be initialize by looking up ModuleInfo or Modules.
        // Good for unit tests, etc.
        if (!started && (type == ModuleInfo.class || type == Module.class || type == Modules.class)) {
            Main.getModuleSystem ();
        }
    }
}
    
