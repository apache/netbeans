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

import java.awt.GraphicsEnvironment;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jindrich Sedek
 */
public class ScreenSizeTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ScreenSizeTest.class);
    }

    private Object[] params = null;

    public ScreenSizeTest(String name) {
        super(name);
    }

    public void testScreenResolutionLogging() {
        Logger logger = Logger.getLogger(Installer.UI_LOGGER_NAME);
        logger.setLevel(Level.ALL);
        logger.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                if (ScreenSize.MESSAGE.equals(record.getMessage())) {
                    params = record.getParameters();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        ScreenSize.logScreenSize();
        assertNotNull(params);
        assertEquals(3, params.length);
        for (Object object : params) {
            assertTrue(object instanceof Number);
        }
    }

    public static List<LogRecord> removeExtraLogs(List<LogRecord> logs){
        Iterator<LogRecord> it = logs.iterator();
        while (it.hasNext()){
            LogRecord logRecord = it.next();
            if (logRecord.getMessage().equals(ScreenSize.MESSAGE)) {
                it.remove();
            } else if (logRecord.getMessage().equals(CPUInfo.MESSAGE)){
                it.remove();
            } else if (logRecord.getMessage().equals(Installer.IDE_STARTUP)){
                it.remove();
            }
        }
        return logs;
    }
}


