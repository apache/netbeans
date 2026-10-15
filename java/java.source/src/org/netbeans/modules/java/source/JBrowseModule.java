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

package org.netbeans.modules.java.source;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.autoupdate.PluginInstaller;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author Petr Hrebejk
 * @author Tomas Zezula
 */
public class JBrowseModule extends ModuleInstall {

    @StaticResource
    private static final String WARNING_ICON = "org/netbeans/modules/java/source/resources/icons/warning.png"; //NOI18N

    private static volatile boolean closed;
        
    /** Creates a new instance of JBrowseModule */
    public JBrowseModule() {
    }

    public static final String KEY_WARNING_SHOWN = "nb-javac.warning.shown";

    @Override
    @NbBundle.Messages({
        "TITLE_FeaturesLimited=Java features limited",
        "DESC_FeaturesLimited=<html>No supported javac library available." +
                             " Most Java editing features are disabled." +
                             " Please either:" +
                             "<ul>" +
                                 "<li>install nb-javac library (<b>highly recommended</b>)</li>" +
                                 "<li>run NetBeans on JDK "+NoJavacHelper.REQUIRED_JAVAC_VERSION+" or later</li>" +
                             "</ul>",
        "BN_Install=Install nb-javac",
        "DN_nbjavac=nb-javac library",
        "DESC_InstallNbJavac=It is recommended to install nb-javac Library to improve Java editing experience and enable compile on save."
    })
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                if (GraphicsEnvironment.isHeadless()) {
                    //no UI, ignore (let's assume whoever run in this mode know
                    //what they are doing)
                    return;
                }
                Preferences prefs = NbPreferences.forModule(NoJavacHelper.class);
                if (!NoJavacHelper.hasWorkingJavac() && !prefs.getBoolean(KEY_WARNING_SHOWN, false)) {
                    String install = Bundle.BN_Install();
                    try {
                        NotifyDescriptor msg = new NotifyDescriptor(Bundle.DESC_FeaturesLimited(), Bundle.TITLE_FeaturesLimited(), 
                            NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.WARNING_MESSAGE, new Object[] {install, DialogDescriptor.CANCEL_OPTION}, install);
                        Object r = DialogDisplayer.getDefault().notify(msg);
                        if (r == install) {
                            PluginInstaller.getDefault().install("org.netbeans.libs.nbjavacapi", null, null, Bundle.DN_nbjavac()); // NOI18N
                        }
                    } catch (HeadlessException ex) {
                        Exceptions.printStackTrace(Exceptions.attachSeverity(ex, Level.FINE));
                    }
//                    prefs.putBoolean(KEY_WARNING_SHOWN, true); // show warning on every boot until fixed
                }
            });
        });
        super.restored();
    }

    @Override
    public void close () {
        super.close();
        closed = true;
        ClassIndexManager.getDefault().close();
    }

    public static boolean isClosed() {
        return closed;
    }
}
