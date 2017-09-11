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

import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;


public class ChromeBrowser extends ExtWebBrowser implements PropertyChangeListener {

    /** storage for starting browser timeout property */
    //protected int browserStartTimeout = 6000;

    private static final long serialVersionUID = -4553174676787993831L;

    /** Creates new ExtWebBrowser */
    public ChromeBrowser() {
        super(PrivateBrowserFamilyId.CHROME);
        ddeServer = ExtWebBrowser.CHROME;
    }
    
    public static Boolean isHidden () {
        if (Utilities.isWindows()) {
            String detectedPath = getLocalAppPath().getPath();
            try {
                if ( detectedPath == null ){
                    detectedPath = NbDdeBrowserImpl.getBrowserPath("chrome");       // NOI18N
                }
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log(Level.FINEST, "Cannot detect chrome : " + e);   // NOI18N
            }
            if ((detectedPath != null) && (detectedPath.trim().length() > 0)) {
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }

    /** Getter for browser name
     *  @return name of browser
     */
    @Override
    public String getName () {
        if (name == null) {
            this.name = NbBundle.getMessage(ChromeBrowser.class, "CTL_ChromeBrowserName");  // NOI18N
        }
        return name;
    }

    /**
     * Returns a new instance of BrowserImpl implementation.
     * @throws UnsupportedOperationException when method is called and OS is not Windows.
     * @return browserImpl implementation of browser.
     */
    @Override
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        ExtBrowserImpl impl = null;

        if (org.openide.util.Utilities.isWindows ()) {
            impl = new NbDdeBrowserImpl (this);
        } else if (Utilities.isMac()) {
            impl = new MacBrowserImpl(this);
        } else if (Utilities.isUnix() && !Utilities.isMac()) {
            impl = new UnixBrowserImpl(this);
        } else {
            throw new UnsupportedOperationException (NbBundle.
                    getMessage(FirefoxBrowser.class, "MSG_CannotUseBrowser"));  // NOI18N
        }
        
        return impl;
    }

    /** Default command for browser execution.
     * Can be overriden to return browser that suits to platform and settings.
     *
     * @return process descriptor that allows to start browser.
     */
    @Override
    protected NbProcessDescriptor defaultBrowserExecutable() {
        String b;
        String params = "";     // NOI18N
        NbProcessDescriptor retValue = null;
        
        //Windows
        if (Utilities.isWindows()) {
            params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";  // NOI18N
            File file = getLocalAppPath();
            if ( file.exists() && file.canExecute() ){
                setDDEServer(ExtWebBrowser.CHROME);
                return new NbProcessDescriptor (file.getPath(), params);
            }
            /*
             * Chrome is installed at the moment in the local user directory.
             * Other user could also install Chrome later. In the latter case 
             * NbDdeBrowserImpl.getBrowserPath() returns path with latest 
             * chrome installation. As result the executable path could
             * be inside other user local dir and will not work at all.
             */
            try {
                try {
                    b = NbDdeBrowserImpl.getBrowserPath("chrome"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(ExtWebBrowser.CHROME);
                        return new NbProcessDescriptor(b, params);
                    }
                } catch (NbBrowserException e) {
                    if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                        ExtWebBrowser.getEM().log(Level.FINE, "Cannot get Path for Chrome: " + e);   // NOI18N
                    }
                    File chrome = new File("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"); // NOI18N
                    if (!chrome.isFile()) {
                        chrome = new File("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"); // NOI18N
                    }
                    if (chrome.isFile()
                            && chrome.canExecute()) {
                        setDDEServer(ExtWebBrowser.CHROME);
                        return new NbProcessDescriptor(chrome.getPath(), params);
                    }
                }

            } catch (UnsatisfiedLinkError e) {
                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                    ExtWebBrowser.getEM().log(Level.FINE, "Some problem here:" + e);   // NOI18N
                }
            }
         // Mac
        } else if (Utilities.isMac()) {
            params += "-b com.google.chrome {" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}"; // NOI18N
            retValue = new NbProcessDescriptor ("/usr/bin/open", params, // NOI18N
                    ExtWebBrowser.UnixBrowserFormat.getHint());
            return retValue;

        
        //Unix
        } else {
            boolean found = false;
            b = "chrome"; // NOI18N
            java.io.File f = new java.io.File ("/usr/local/bin/google-chrome"); // NOI18N
            if (f.exists()) {
                found = true;
                b = f.getAbsolutePath();
            }
            f = new java.io.File ("/usr/bin/google-chrome"); // NOI18N
            if ( !found && f.exists()) {
                found = true;
                b = f.getAbsolutePath();
            }
            f = new java.io.File ("/opt/google/chrome/google-chrome"); // NOI18N
            if ( !found && f.exists()) {
                found = true;
                b = f.getAbsolutePath();
            }
            f = new java.io.File ("/opt/google/chrome/chrome"); // NOI18N
            if ( ! found && f.exists()) {
                found = true;
                b = f.getAbsolutePath();
            }
            retValue = new NbProcessDescriptor (
                b, "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getMessage (ChromeBrowser.class, "MSG_BrowserExecutorHint")
            );                
        }
        
        return retValue;        
    }

    private static File getLocalAppPath(){
        String localFiles = System.getenv("LOCALAPPDATA");              // NOI18N
        String chrome = localFiles+"\\Google\\Chrome\\Application\\chrome.exe";     // NOI18N
        
        return new File( chrome );
    }

}
