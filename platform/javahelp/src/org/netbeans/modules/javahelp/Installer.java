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

package org.netbeans.modules.javahelp;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.netbeans.api.javahelp.Help;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;

public class Installer extends ModuleInstall {

    public static final Logger log = Logger.getLogger("org.netbeans.modules.javahelp"); // NOI18N
    public static final Logger UI = Logger.getLogger("org.netbeans.ui.javahelp"); // NOI18N
    public static final Logger USG = Logger.getLogger("org.netbeans.ui.metrics.javahelp"); // NOI18N

    public void restored() {
        log.fine("restored module");
        // This ensures the static block will be called ASAP, hence that
        // the AWT listener will actually be started quickly and there
        // will already have been interesting mouse-entered events
        // by the time F1 is first pressed. Otherwise only the second
        // F1 actually gets anything other than the main window help.
        HelpAction.WindowActivatedDetector.install();

        // XXX(-ttran) quick fix for #25470: Help viewer frozen on first open
        // over modal dialogs.  JavaHelp seems to try to be lazy with the
        // installation of its Dialog detector (an AWTEventListener) but it
        // doesn't work on Windows.  Here we force JavaHelp instance to be
        // created and thus its AWTEventListener be registered early enough.
        
        Lookup.getDefault().lookup(Help.class);
    }
    
    public void uninstalled() {
        log.fine("uninstalled module");
        Help help = Lookup.getDefault().lookup(Help.class);
        if (help instanceof JavaHelp) {
            ((JavaHelp) help).deactivate();
        }
        HelpAction.WindowActivatedDetector.uninstall();
        // UIManager is too aggressive about caching, and we get CCE's,
        // since JavaHelp's HelpUtilities sets up these defaults, and UIManager
        // caches the actual classes (probably incorrectly). #4675772
        cleanDefaults(UIManager.getDefaults());
        cleanDefaults(UIManager.getLookAndFeelDefaults());
    }
    private static void cleanDefaults(UIDefaults d) {
        Set<Object> badKeys = new HashSet<Object>(10);
        Iterator<Map.Entry<Object, Object>> it = d.entrySet().iterator();
        ClassLoader aboutToDie = Installer.class.getClassLoader();
        while (it.hasNext()) {
            Map.Entry<Object, Object> e;
            try {
                e = it.next();
            } catch (ConcurrentModificationException x) {
                // Seems to be possible during shutdown. Just skip the hack in this case.
                return;
            }
            Object k = e.getKey();
            Object o = e.getValue();
            if (o instanceof Class) {
                Class c = (Class)o;
                if (c.getClassLoader() == aboutToDie) {
                    badKeys.add(k);
                }
            } else if (k instanceof Class) {
                Class c = (Class)k;
                if (c.getClassLoader() == aboutToDie) {
                    badKeys.add(k);
                }
            }
        }
        if (!badKeys.isEmpty()) {
            log.fine("Cleaning up old UIDefaults keys (JRE bug #4675772): " + badKeys);
            for (Object o: badKeys) {
                d.put(o, null);
            }
        }
    }
    
}
