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

package org.netbeans.modules.openide.text;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.modules.OnStop;

/**
 * Log number of editors opened during IDE session by mime type.
 * Logging is performed at IDE shutdown.
 *
 * @author Marek Slama
 */
@OnStop
public class Installer implements Runnable {
    
    private static Map<String,Integer> mimeTypes = new HashMap<>();

    public static void add (String mimeType) {
        if (mimeTypes.containsKey(mimeType)) {
            Integer v = mimeTypes.get(mimeType);
            mimeTypes.put(mimeType, v + 1);
        } else {
            mimeTypes.put(mimeType, 1);
        }
    }
    
    @Override
    public void run() {
        for (Map.Entry<String,Integer> entry : mimeTypes.entrySet()) {
            Logger logger = Logger.getLogger("org.netbeans.ui.metrics.editor"); //NOI18N
            LogRecord rec = new LogRecord(Level.INFO, "USG_EDITOR_MIME_TYPE"); //NOI18N
            rec.setParameters(new Object[] { entry.getKey(), entry.getValue() });
            rec.setLoggerName(logger.getName());
            logger.log(rec);
        }
    }
    
}
