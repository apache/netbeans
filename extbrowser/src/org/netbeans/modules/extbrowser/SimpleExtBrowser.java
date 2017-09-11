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

import java.util.HashMap;
import org.netbeans.modules.extbrowser.PrivateBrowserFamilyId;

import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;

import org.openide.util.Utilities;

/** Simple external browser that uses new process for each URL request.
 *  Typically it runs command like <CODE>netscape [url]</CODE>.
 *
 * @author  Radim Kubacki, Martin Grebac
 */
public class SimpleExtBrowser extends ExtWebBrowser {

    private static final long serialVersionUID = -8494345762328555637L;
    
    /** Determines whether the browser should be visible or not
     *  @return false when OS is Windows or Unix.
     *          true in all other cases.
     */
    public static Boolean isHidden () {
        // #231723 - hide it also on mac
        return Utilities.isWindows() || Utilities.isUnix();
    }
    
    /** Creates new SimpleExtBrowser */
    public SimpleExtBrowser() {
        super(PrivateBrowserFamilyId.UNKNOWN);
        if (Utilities.getOperatingSystem () == Utilities.OS_OS2) {
            browserExecutable = new NbProcessDescriptor(
                "Netscape.exe", // NOI18N
                // {URL}
                " {" + BrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        } else if (Utilities.isMac()) {
            browserExecutable = new NbProcessDescriptor(
                "/usr/bin/open", // NOI18N
                // {URL}
                " {" + BrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        } else {
            browserExecutable = new NbProcessDescriptor(
                // empty string for process
                "", // NOI18N
                // {URL}
                " {" + BrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        }
    }
    
    /** Getter for browser name
     *  @return name of browser
     */
    public String getName() {
        if (name == null) {
            this.name = NbBundle.getMessage(SimpleExtBrowser.class, "CTL_SimpleExtBrowser");
        }
        return name;
    }
            
    /** Builds new implementation of specified browser.
     * If embeddable property is true, then it is expected
     * that returned implementation will implement org.openide.awt.HtmlBrowser.Impl
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        return new SimpleExtBrowserImpl(this);
    }

    /** Default format that can format tags related to execution. Currently this is only the URL.
     */
    public static class BrowserFormat extends org.openide.util.MapFormat {
        
        private static final long serialVersionUID = 5990981835151848381L;
        /** Tag replaced with the URL */
        public static final String TAG_URL = "URL";  // NOI18N
        
        
        /** @param info exec info about class to execute
         * @param classPath to substitute instead of CLASSPATH
         * @param bootClassPath boot class path
         * @param repository repository path
         * @param library library path
         */
        public BrowserFormat (String url) {
            super(new HashMap());
            java.util.Map map = getMap ();
            
            map.put (TAG_URL, url);
        }
        
    }
}
