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
package org.netbeans.core.netigso;

import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import org.junit.Test;

public class NetigsoLogTest {
    @Test
    public void testConvertLogsSevere() {
        assertLog(Level.SEVERE, "1");
    }

    @Test
    public void testConvertLogsWarning() {
        assertLog(Level.WARNING, "2");
    }

    @Test
    public void testConvertLogsInfo() {
        assertLog(Level.INFO, "2");
    }

    @Test
    public void testConvertLogsConfig() {
        assertLog(Level.CONFIG, "3");
    }

    @Test
    public void testConvertLogsFineAndLess() {
        assertLog(Level.FINE, "4");
        assertLog(Level.FINER, "4");
        assertLog(Level.FINEST, "4");
    }

    private static void assertLog(Level level, String felixLevel) {
        Logger l = Logger.getLogger("my.test.logger." + level);
        l.setLevel(level);
        assertTrue("Level is loggable", l.isLoggable(level));
        Level less = new Level("", level.intValue() - 100) {};
        assertFalse("Lowever level isn't loggable", l.isLoggable(less));

        String convertedLevel = Netigso.felixLogLevel(l);
        assertEquals(felixLevel, convertedLevel);
    }

}
