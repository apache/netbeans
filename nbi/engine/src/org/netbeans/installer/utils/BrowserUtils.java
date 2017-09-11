/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author Dmitry Lipin
 */
public class BrowserUtils {

    private final static boolean isBrowseSupported;
    private final static Method browseMethod;
    private final static Object desktop;


    static {
        LogManager.log("Checking if java.awt.Desktop.getDesktop().browse() functionality is supported");
        boolean supported = false;
        Method browseM = null;
        Object desktopObj = null;
        try {
            // Determine if java.awt.Desktop is supported
            Class desktopCls = Class.forName("java.awt.Desktop", true, null);
            Method getDesktopM = desktopCls.getMethod("getDesktop");
            browseM = desktopCls.getMethod("browse", URI.class);

            Class actionCls = Class.forName("java.awt.Desktop$Action", true, null);
            Method isDesktopSupportedMethod = desktopCls.getMethod("isDesktopSupported");
            Method isSupportedMethod = desktopCls.getMethod("isSupported", actionCls);
            Field browseField = actionCls.getField("BROWSE");

            // support only if Desktop.isDesktopSupported() and
            // Desktop.isSupported(Desktop.Action.BROWSE) return true.
            Boolean result = (Boolean) isDesktopSupportedMethod.invoke(null);
            if (result.booleanValue()) {
                desktopObj = getDesktopM.invoke(null);
                result = (Boolean) isSupportedMethod.invoke(desktopObj, browseField.get(null));
                supported = result.booleanValue();
            }
        } catch (ClassNotFoundException e) {
            LogManager.log("... browser not supported", e);
        } catch (NoSuchMethodException e) {
            LogManager.log("... browser not supported", e);
        } catch (NoSuchFieldException e) {
            LogManager.log("... browser not supported", e);
        } catch (IllegalAccessException e) {
            // should never reach here
            InternalError x =
                    new InternalError("Desktop.getDesktop() method not found");
            x.initCause(e);
            LogManager.log("... browser not supported", e);
        } catch (InvocationTargetException e) {
            LogManager.log("... browser not supported", e);
        }
        isBrowseSupported = supported;
        browseMethod = browseM;
        desktop = desktopObj;
    }

    public static boolean openBrowser(URI uri) {
        LogManager.log("... opening in the browser: " + uri);
        boolean result = false;
        try {
            if (isBrowseSupported) {
                LogManager.log("... browse (bs): " + uri);
                result = browse(uri);
            } else if (SystemUtils.getNativeUtils().isBrowseSupported()) {
                LogManager.log("... browse (fb): " + uri);
                result = SystemUtils.getNativeUtils().openBrowser(uri);
            } else {
                LogManager.log("... browser is not supported");
            }
        } catch (Exception ex) {
            LogManager.log("Cannot open browser", ex);
        } 
        return result;
    }

    public static boolean isBrowseSupported() {
        return isBrowseSupported || SystemUtils.getNativeUtils().isBrowseSupported();
    }

    private static boolean browse(URI uri) throws IOException {
        if (uri == null) {
            throw new NullPointerException("null uri");
        }
        if (!isBrowseSupported) {
            throw new UnsupportedOperationException("Browse operation is not supported");
        }

        // Call Desktop.browse() method
        try {
            browseMethod.invoke(desktop, uri);
            return true;
        } catch (IllegalAccessException e) {
            // should never reach here
            InternalError x =
                    new InternalError("Desktop.getDesktop() method not found");
            x.initCause(e);
            throw x;
        } catch (InvocationTargetException e) {
            Throwable x = e.getCause();
            if (x != null) {
                if (x instanceof UnsupportedOperationException) {
                    throw (UnsupportedOperationException) x;
                } else if (x instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) x;
                } else if (x instanceof IOException) {
                    throw (IOException) x;
                } else if (x instanceof SecurityException) {
                    throw (SecurityException) x;
                } else {
                    // ignore
                }
            }
        }
        return false;
    }

    public static HyperlinkListener createHyperlinkListener() {
        return new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (HyperlinkEvent.EventType.ACTIVATED == hlevt.getEventType()) {
                    final URL url = hlevt.getURL();
                    if (url != null) {
                        try {
                            openBrowser(url.toURI());
                        } catch (URISyntaxException e) {
                            LogManager.log(e);
                        }
                    }
                }
            }
        };
    }
}
