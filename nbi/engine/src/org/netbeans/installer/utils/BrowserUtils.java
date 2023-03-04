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

    private static final boolean isBrowseSupported;
    private static final Method browseMethod;
    private static final Object desktop;


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
