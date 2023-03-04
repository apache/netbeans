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
package org.netbeans.modules.debugger.jpda.visual.remote;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Jaroslav Bachorik
 */
public class RemoteFXService {
//    final private static Logger LOGGER = Logger.getAnonymousLogger();
    
    private static Method runLater, isFxThread;
    private static volatile boolean fxAccess = false;
    
    static {
        try {
            Class.forName("javafx.scene.image.Image", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
        try {
            Class.forName("com.sun.media.jfxmedia.AudioClip", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
        try {
            Class.forName("com.sun.media.jfxmedia.MediaManager", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
        try {
            Class.forName("com.sun.media.jfxmedia.MediaPlayer", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
        try {
            Class.forName("com.sun.media.jfxmedia.events.PlayerStateEvent$PlayerState", true, RemoteFXService.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // throw away
        }
        
        try {
            Class platformClz = Class.forName("javafx.application.Platform");
            runLater = platformClz.getMethod("runLater", new Class[]{Runnable.class});
            runLater.setAccessible(true);
            isFxThread = platformClz.getMethod("isFxApplicationThread", new Class[0]);
            isFxThread.setAccessible(true);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }
    }
    
    public static boolean startAccessLoop() {
        Thread accessThread;
        try {
            accessThread = new Thread(new FXAccessLoop(), "FX Access Thread (Visual Debugger)"); // NOI18N
        } catch (SecurityException se) {
            return false;
        }
        accessThread.setDaemon(true);
        accessThread.setPriority(Thread.MIN_PRIORITY);
        accessThread.start();
        return true;
    }
    
    private static void access() {
        // A breakpoint is submitted on this method.
        // When fxAccess field is set to true, this breakpoint is hit in FX application thread
        // and methods can be executed via debugger.
    };
    
    private static class FXAccessLoop implements Runnable {
        public void run() {
            if (isFxThread == null || runLater == null) {
                System.err.println("JavaFX VisualDebugger: Leaving FXAccessLoop");
            }
            
            try {
                if (((Boolean) isFxThread.invoke(null, new Object[0])).booleanValue()) {
                    access();
                    return;
                }
                while (!Thread.interrupted()) {
                    if (fxAccess) {
                        runLater.invoke(null, new Object[]{this});
                        fxAccess = false;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                System.err.println("JavaFX VisualDebugger: Leaving FXAccessLoop");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                System.err.println("JavaFX VisualDebugger: Leaving FXAccessLoop");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                System.err.println("JavaFX VisualDebugger: Leaving FXAccessLoop");
            }
        }
    }
}
