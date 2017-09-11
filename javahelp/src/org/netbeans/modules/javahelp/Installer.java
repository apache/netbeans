/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
