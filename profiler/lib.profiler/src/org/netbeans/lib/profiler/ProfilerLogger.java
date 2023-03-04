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

package org.netbeans.lib.profiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class serves as a wrapper for logging infrastructure
 * It should be used to log various profiler info messages
 * The logger used is identified as "org.netbeans.lib.profiler.infolog" and its level is automatically set to INFO
 * @author Jaroslav Bachorik
 */
public class ProfilerLogger {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger INSTANCE = Logger.getLogger("org.netbeans.lib.profiler.infolog"); // NOI18N
    private static final Level EXCEPTION_LEVEL = Level.SEVERE;
    private static volatile Level DEFAULT_LEVEL = Level.INFO;
    private static volatile boolean debugFlag = false;

    static {
        Level currentLevel = INSTANCE.getLevel();
        Level newLevel = currentLevel;

        if (DEFAULT_LEVEL.intValue() < EXCEPTION_LEVEL.intValue()) {
            newLevel = DEFAULT_LEVEL;
        } else {
            newLevel = EXCEPTION_LEVEL;
        }

        if ((currentLevel == null) || (newLevel.intValue() < currentLevel.intValue())) {
            INSTANCE.setLevel(newLevel);
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static boolean isDebug() {
        return debugFlag;
    }

    public static void setLevel(Level level) {
        INSTANCE.setLevel(level);

        if (level.intValue() <= Level.FINEST.intValue()) {
            debugFlag = true;
        } else {
            debugFlag = false;
        }
    }

    public static Level getLevel() {
        return INSTANCE.getLevel();
    }

    public static void debug(String message) {
        INSTANCE.finest(message);
    }

    public static void info(String message) {
        INSTANCE.info(message);
    }

    public static void log(String message) {
        INSTANCE.log(DEFAULT_LEVEL, message);
    }

    public static void log(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        INSTANCE.log(EXCEPTION_LEVEL, sw.toString());
    }

    public static void severe(String message) {
        INSTANCE.severe(message);
    }

    public static void warning(String message) {
        INSTANCE.warning(message);
    }
}
