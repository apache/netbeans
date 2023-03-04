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
package org.netbeans.modules.maven.j2ee.utils;

import org.junit.Test;
import org.openide.util.NbBundle;
import static org.junit.Assert.*;

/**
 * 
 * @author Martin Janicek
 */
public class LoggingUtilsTest {
    
    @Test
    public void getInstanceTest() {
        LoggingUtils.logUI(LoggingUtilsTest.class, "UI_LOG_MESSAGE", new Object[] {});
        LoggingUtils.logUI(LoggingUtilsTest.class, "UI_LOG_MESSAGE", new Object[] {}, "maven");
        LoggingUtils.logUI(NbBundle.getBundle(LoggingUtilsTest.class), "UI_LOG_MESSAGE", new Object[] {});
        LoggingUtils.logUI(NbBundle.getBundle(LoggingUtilsTest.class), "UI_LOG_MESSAGE", new Object[] {}, "maven");
        
        LoggingUtils.logUsage(LoggingUtilsTest.class, "USG_LOG_MESSAGE", new Object[] {});
        LoggingUtils.logUsage(LoggingUtilsTest.class, "USG_LOG_MESSAGE", new Object[] {}, "maven");
        LoggingUtils.logUsage(NbBundle.getBundle(LoggingUtilsTest.class), "USG_LOG_MESSAGE", new Object[] {});
        LoggingUtils.logUsage(NbBundle.getBundle(LoggingUtilsTest.class), "USG_LOG_MESSAGE", new Object[] {}, "maven");
        
        assertEquals("org.netbeans.ui", LoggingUtils.createUiLogger(null).getName());
        assertEquals("org.netbeans.ui", LoggingUtils.createUiLogger("").getName());
        assertEquals("org.netbeans.ui.metrics", LoggingUtils.createUsageLogger(null).getName());
        assertEquals("org.netbeans.ui.metrics", LoggingUtils.createUsageLogger("").getName());
        
        assertEquals("org.netbeans.ui.maven", LoggingUtils.createUiLogger("maven").getName());
        assertEquals("org.netbeans.ui.metrics.maven", LoggingUtils.createUsageLogger("maven").getName());
    }
}
