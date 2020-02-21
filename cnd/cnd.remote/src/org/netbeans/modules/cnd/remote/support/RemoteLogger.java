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

package org.netbeans.modules.cnd.remote.support;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RemoteLogger {

    private static final Logger LOGGER = Logger.getLogger("cnd.remote.logger"); //NOI18N
    
    private RemoteLogger() {
    }

    public static Logger getInstance() {
        return LOGGER;
    }
    
    public static void log(Level level, String message, Object... args) {
        if (LOGGER.isLoggable(level)) {
            LOGGER.log(level, message, args);
        }
    }
    
    public static void severe(String msg, Object... params) {
        log(Level.SEVERE, msg, params);
    }

    public static void warning(String msg, Object... params) {
        log(Level.WARNING, msg, params);
    }

    public static void info(String msg, Object... params) {
        log(Level.INFO, msg, params);
    }

    public static void fine(String msg, Object... params) {
        log(Level.FINE, msg, params);
    }

    public static void finer(String msg, Object... params) {
        log(Level.FINER, msg, params);
    }
    
    public static void finest(String msg, Object... params) {
        log(Level.FINEST, msg, params);
    }        
    
}
