/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.platforms.ios;
 
import java.util.logging.Logger;
import org.openide.modules.InstalledFileLocator;


public class WebInspectorJNIBinding {
    
    private static final Logger LOG = Logger.getLogger(WebInspectorJNIBinding.class.getName());
    
    private native void nstart();

    private native void nstop();

    private native String nreceiveMessage(Integer timeout);

    private native void nsendMessage(String xml);
    
    private native boolean nisDeviceConnected();

    private boolean started = false;
    private final Object readLock;
    private final Object writeLock;
    
    private static WebInspectorJNIBinding instance;

    private static final String CODE_BASE = "org.netbeans.modules.cordova.platforms.ios";
    
    private WebInspectorJNIBinding() {
        this.writeLock = new Object();
        this.readLock = new Object();
        
        final InstalledFileLocator locator = InstalledFileLocator.getDefault();
        System.load(locator.locate("bin/libplist.1.dylib", CODE_BASE, false).getPath());
        System.load(locator.locate("bin/libimobiledevice.4.dylib", CODE_BASE, false).getPath());
        System.load(locator.locate("bin/libusbmuxd.2.dylib", CODE_BASE, false).getPath());
        System.load(locator.locate("bin/libiDeviceNativeBinding.dylib", CODE_BASE, false).getPath());
    }
    
    public static synchronized WebInspectorJNIBinding getDefault() {
        if (instance==null) {
            instance = new WebInspectorJNIBinding();
        }
        return instance;
    }

    public void start() {
        synchronized (writeLock) {
            synchronized (readLock) {
                if (!started) {
                    try {
                        nstart();
                        started = true;
                    } catch (IllegalStateException ise) {
                        throw ise;
                    }
                } else {
                    LOG.info("WebKit Debugging Service already started");
                }
            }
        }
    }

    public void stop() {
        synchronized (writeLock) {
            synchronized (readLock) {
                if (started) {
                    nstop();
                    started = false;
                } else {
                    LOG.info("WebKit Debugging Service not started");
                }
            }
        }
    }

    public String receiveMessage() {
        synchronized (readLock) {
            if (!started) {
                LOG.info("WebKit Debugging Service not started");
                return null;
            }
            final String receiveMessage = nreceiveMessage(100);
            return receiveMessage;
        }
    }

    public void sendMessage(String message) {
        synchronized (writeLock) {
            if (!started) {
                LOG.info("WebKit Debugging Service not started");
                return;
            }
            nsendMessage(message);
        }
    }
    
    public boolean isDeviceConnected() {
        return nisDeviceConnected();
    }
}
