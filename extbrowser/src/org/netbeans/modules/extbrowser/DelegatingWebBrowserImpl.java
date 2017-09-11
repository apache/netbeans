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
