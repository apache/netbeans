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

package org.netbeans.modules.extbrowser;

import java.awt.EventQueue;
import java.net.URL;
import org.openide.util.Utilities;

/**
 * @author  snajper
 */
public class DelegatingWebBrowserImpl extends ExtBrowserImpl {

    private NbDdeBrowserImpl ddeImpl;
    private UnixBrowserImpl unixImpl;
    private SimpleExtBrowserImpl simpleImpl;

    /** Creates a new instance of DelegatingWebBrowserImpl */
    public DelegatingWebBrowserImpl() {
    }

    /** Creates a new instance of DelegatingWebBrowserImpl
     * @param extBrowserFactory factory to use
     */
    public DelegatingWebBrowserImpl(ExtWebBrowser extBrowserFactory) {
        this.extBrowserFactory = extBrowserFactory;
    }

    public ExtBrowserImpl getImplementation() {
        String pName = extBrowserFactory.getBrowserExecutable().getProcessName().toUpperCase();
                
        if (pName != null) {
            
            // Windows -> DDE browser if it is Mozilla, or Netscape 4.x or Netscape 7.x or Internet Explorer
            // Netscape6 is also simple command-line
            if (Utilities.isWindows()) {
                if (pName.indexOf("IEXPLORE.EXE") > -1 ||       // NOI18N
                    pName.indexOf("NETSCP.EXE") > -1 ||         // NOI18N
                    pName.indexOf("MOZILLA.EXE") > -1 ||        // NOI18N
                    pName.indexOf("FIREFOX.EXE") > -1 ||        // NOI18N
                    pName.indexOf("NETSCAPE.EXE") > -1) {       // NOI18N
                        if (ddeImpl == null) {
                            ddeImpl = new NbDdeBrowserImpl(extBrowserFactory);
                        }
                        return ddeImpl;
                }

            // Unix (but not MacOSX) -> if Netscape or Mozilla, create Unix browser
            } else if (Utilities.isUnix() && !Utilities.isMac()) {
                if (pName.indexOf("MOZILLA") > -1 ||            // NOI18N
                    pName.indexOf("NETSCAPE") > -1 || 
                    pName.indexOf("FIREFOX") > -1) {           // NOI18N
                        if (unixImpl == null) {
                            unixImpl = new UnixBrowserImpl(extBrowserFactory);
                        }
                        return unixImpl;
                }
            }
        }
        
        // otherwise simple command-line browser
        if (simpleImpl == null) {
            simpleImpl = new SimpleExtBrowserImpl(extBrowserFactory);
        }
        return simpleImpl;
    }

    /** 
     *  Sets current URL.
     * @param url URL to show in the browser.
     */
    @Override
    protected void loadURLInBrowserInternal(URL url) {
        assert !EventQueue.isDispatchThread();
        getImplementation().loadURLInBrowserInternal(url);
    }
        
}
