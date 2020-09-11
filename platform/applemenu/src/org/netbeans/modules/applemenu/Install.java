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

package org.netbeans.modules.applemenu;

import org.netbeans.modules.applemenu.spi.NbApplicationAdapter;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/** Module installer that installs listeners, which will interpret
 * apple events and call the appropriate action from the actions pool.
 *
 * @author  Tim Boudreau
 */
public class Install extends ModuleInstall {
    private CtrlClickHack listener;
    private NbApplicationAdapter adapter;

    @Override
    public void restored () {
        listener = new CtrlClickHack();
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
        if (Utilities.isMac() ) {
            String pn = "apple.laf.useScreenMenuBar"; // NOI18N
            if (System.getProperty(pn) == null) {
                System.setProperty(pn, "true"); // NOI18N
            }
            for (NbApplicationAdapter a : Lookup.getDefault().lookupAll(NbApplicationAdapter.class)) {
                try {
                    a.install();
                    this.adapter = a;
                } catch (Throwable ex) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                }
            }
        }
    }

    @Override
    public void uninstalled () {
         if (listener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
            listener = null;
         }
        if (Utilities.isMac() && adapter != null) {
            adapter.uninstall();
            adapter = null;
        }
    }
}
