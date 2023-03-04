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
package org.netbeans.modules.uihandler;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * It logs MTTF (Mean Time To failure) of exception
 * @author pzajac
 */
final class TimeToFailure {
    private static final String MTTF = "MTTF"; //NOI18N
    // ten minutes
    private static final long TRESHOLD = 60*1000*10;
    static Preferences prefs = NbPreferences.forModule(TimeToFailure.class);
    static long totalTime = 0;
    static long lastAction = 0;
    static void logAction() {
        if (totalTime == -1 ) {
            totalTime = prefs.getLong(MTTF, 0L);
            lastAction = System.currentTimeMillis();
        } else {
            long time = System.currentTimeMillis();
            if ( time - lastAction < TRESHOLD ) {
                totalTime += time - lastAction;
                if(Installer.preferencesWritable) {
                    prefs.putLong(MTTF,totalTime);
                }
            } 
            lastAction = time;
        }
    }

    static LogRecord logFailure() {
        LogRecord lr = new LogRecord(Level.CONFIG,TimeToFailure.class.getName() + ":" + totalTime);
        totalTime = 0;
        lastAction = 0;
        if(Installer.preferencesWritable) {
            prefs.putLong(MTTF,totalTime);
        }
        return lr;
    }
    
}
