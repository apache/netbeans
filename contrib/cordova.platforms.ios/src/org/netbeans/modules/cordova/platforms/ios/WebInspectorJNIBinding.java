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
