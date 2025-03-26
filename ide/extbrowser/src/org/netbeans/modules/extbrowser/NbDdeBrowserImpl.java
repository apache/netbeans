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

package org.netbeans.modules.extbrowser;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APITypeMapper;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import org.netbeans.modules.extbrowser.impl.Shlwapi;
import org.netbeans.modules.extbrowser.impl.ShlwapiUtil;
import org.openide.execution.NbProcessDescriptor;


/**
 * Class that uses DDE to communicate with web browser through DDE.
 * Currently three browsers are supported:
 * <UL>
 * <LI>Netscape Navigator</LI>
 * <LI>Internet Explorer</LI>
 * <LI>Mozilla</LI>
 * </UL>
 *
 * <P>Limitations: Mozilla doesn't support WWW_Activate now
 * IE has different implementation on Win9x and on WinNT/Win2000.
 * WWW_Activate creates always new window on Win9x so we don't use it.
 * Also it accepts only "0xFFFFFFFF" for WWW_Activate on WinNT/Win2K.
 *
 * <P>Documentation can be found
 * <a href="http://developer.netscape.com/docs/manuals/communicator/DDE/ddevb.htm">
 * here</a>.
 *
 * @author  Radim Kubacki
 */
public class NbDdeBrowserImpl extends ExtBrowserImpl {

    /** Creates new NbDdeBrowserImpl
     * @param extBrowserFactory factory to use
     */
    public NbDdeBrowserImpl (ExtWebBrowser extBrowserFactory) {
        super ();
        this.extBrowserFactory = extBrowserFactory;
        logFine("NbDdeBrowserImpl created with factory:", extBrowserFactory); // NOI18N
    }

    /**
     * finds registry entry for browser opening
     *
     * @param browser
     * @return
     * @throws org.netbeans.modules.extbrowser.NbBrowserException
     */
    public static String getBrowserPath(String browser) throws NbBrowserException {
        String key = String.format("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\%s.exe", browser);

        try {
            return getKeyValueExpanded(WinReg.HKEY_LOCAL_MACHINE, key, null);
        } catch (Win32Exception ex) {
            throw new NbBrowserException("Error when retrieving path to browser, failed to read registry entry for " + key, ex);
        }
    }

    /**
     * returns the command that executes default application for opening of
     * .html files
     *
     * @return
     * @throws org.netbeans.modules.extbrowser.NbBrowserException
     */
    public static String getDefaultOpenCommand() throws NbBrowserException {
        try {
            String path = ShlwapiUtil.AssocQueryString(0, Shlwapi.ASSOCSTR_COMMAND, ".html", null);
            return path;
        } catch (Win32Exception ex) {
            throw new NbBrowserException("Failed to determine default open command", ex);
        }
    }

    private static String getKeyValueExpanded(HKEY root, String key, String value) throws Win32Exception, NbBrowserException {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            IntByReference data = new IntByReference();
            IntByReference type = new IntByReference();
            rc = Advapi32.INSTANCE.RegQueryValueEx(phkKey.getValue(), value, 0, type, (Pointer) null, data);
            if (rc != W32Errors.ERROR_SUCCESS
                    && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                throw new Win32Exception(rc);
            }
            if (type.getValue() != WinNT.REG_SZ
                    && type.getValue() != WinNT.REG_EXPAND_SZ) {
                throw new NbBrowserException("Unexpected registry type "
                        + type.getValue()
                        + "for: " + key);
            }
            if (data.getValue() == 0) {
                return "";
            }
            assert W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE;
            Memory mem = new Memory(data.getValue() + Native.WCHAR_SIZE);
            mem.clear();
            rc = Advapi32.INSTANCE.RegQueryValueEx(phkKey.getValue(), value, 0, type, mem, data);
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
            String result = mem.getWideString(0);
            if(type.getValue() == WinNT.REG_EXPAND_SZ) {
                return Kernel32Util.expandEnvironmentStrings(result);
            }
            return result;
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /** Sets current URL.
     *
     * @param url URL to show in the browser.
     */
    @Override
    protected void loadURLInBrowserInternal(URL url) {
        assert !EventQueue.isDispatchThread();
        logFine("NbDdeBrowserImpl.setUrl:", url); // NOI18N
        if (url == null) {
            return;
        }
        NbProcessDescriptor np = extBrowserFactory.getBrowserExecutable();
        try {
            url = URLUtil.createExternalURL(url, false);
            String urlTxt;
            if (url == null) {
                urlTxt = ""; // NOI18N;
            } else {
                try {
                    urlTxt = url.toURI().toASCIIString();
                } catch (URISyntaxException ex) {
                    logInfo(ex);
                    urlTxt = url.toString();
                }
            }
            if (np != null) {
                np.exec(new SimpleExtBrowser.BrowserFormat(urlTxt));
            }
        } catch (IOException ex) {
            logInfo(ex);
            BrowserUtils.notifyMissingBrowser(np.getProcessName());
        }
    }

    private static void logInfo(Exception ex) {
        ExtWebBrowser.getEM().log(Level.INFO, null, ex);
    }

    private static void logFine(String message) {
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "{0} {1}", new Object[]{System.currentTimeMillis(), message});
        }
    }

    private static void logFine(String message, Object object) {
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "{0} {1} {2}", new Object[]{System.currentTimeMillis(), message, object});
        }
    }

    @Override
    protected PrivateBrowserFamilyId detectPrivateBrowserFamilyId(){
        PrivateBrowserFamilyId id = super.detectPrivateBrowserFamilyId();
        if (id == PrivateBrowserFamilyId.UNKNOWN){
            switch (realDDEServer()) {
                case ExtWebBrowser.FIREFOX: id = PrivateBrowserFamilyId.FIREFOX; break;
                case ExtWebBrowser.CHROME: id = PrivateBrowserFamilyId.CHROME; break;
                default: id = PrivateBrowserFamilyId.UNKNOWN;
            }
        }
        return id;
    }

    /** Finds the name of DDE server.
     *  If <Default system browser> is set then it resolves it into either
     *  Netscape or IExplore
     */
    private String realDDEServer() {
        logFine("NbDdeBrowserImpl.realDDEServer"); // NOI18N

        try {
            String cmd = getDefaultOpenCommand();
            if (cmd != null) {
                cmd = cmd.toUpperCase();
                if (cmd.contains(ExtWebBrowser.IEXPLORE)) {
                    return ExtWebBrowser.IEXPLORE;
                }

                if (cmd.contains("CHROME")) { // NOI18N
                    return ExtWebBrowser.CHROME;
                }

                // check FF before Mozilla becuase FF is Mozilla but Mozills itself is old one
                if (cmd.contains(ExtWebBrowser.FIREFOX)) {
                    return ExtWebBrowser.FIREFOX;
                }

                if (cmd.contains(ExtWebBrowser.MOZILLA)) {
                    return ExtWebBrowser.MOZILLA;
                }
            }
        } catch (Exception ex) {
            // some problem in native code likely
            logInfo(ex);
        }
        // guess IE
        return ExtWebBrowser.IEXPLORE;
    }

}
