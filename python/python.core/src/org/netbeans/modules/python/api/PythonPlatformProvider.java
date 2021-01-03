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
package org.netbeans.modules.python.api;

import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class PythonPlatformProvider {

    private static final Logger LOGGER = Logger.getLogger(PythonPlatformProvider.class.getName());
    
    private final PropertyEvaluator evaluator;

    public PythonPlatformProvider(final PropertyEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public PythonPlatform getPlatform() {
        ensurePlatformsReady();
        String id = evaluator.getProperty("platform.active"); // NOI18N
        PythonPlatformManager manager = PythonPlatformManager.getInstance();
        if (id == null) {
            id = manager.getDefaultPlatform();
        }
        PythonPlatform platform = manager.getPlatform(id);
        if (platform == null) {
            LOGGER.info("Platform with id '" + id + "' does not exist. Using default platform.");
            platform = manager.getPlatform(manager.getDefaultPlatform());
        }
        return platform;
    }

    private void ensurePlatformsReady() {
        if (!Util.isFirstPlatformTouch()) {
            return;
        }
        String handleMessage = NbBundle.getMessage(PythonPlatformProvider.class, "PythonPlatformProvider.PythonPlatformAutoDetection");
        ProgressHandle ph = ProgressHandleFactory.createHandle(handleMessage);
        ph.start();
        try {
            Thread autoDetection = new Thread(new Runnable() {
                @Override
                public void run() {
                    PythonPlatformManager.getInstance().autoDetect();
                }
            }, "Python Platform AutoDetection"); // NOI18N
            autoDetection.start();
            autoDetection.join();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        ph.finish();
    }
}
