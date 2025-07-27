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
package org.netbeans.modules.glassfish.tooling;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Common GlassFish IDE SDK Exception functional test.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@Test(groups = {"unit-tests"})
public class ExceptionTest {

    // Test methods                                                           //
    /**
     * Test GlassFishException with no parameters specified throwing
     * and logging.
     */
    @Test
    public void testGlassFishExceptionWithNothing() {
        // this message must match GlassFishIdeException() constructor
        // log message.
        String gfieMsg = "Caught GlassFishIdeException.";
        java.util.logging.Logger logger = Logger.getLogger();
        Level logLevel = logger.getLevel();
        OutputStream logOut = new ByteArrayOutputStream(256);
        Handler handler = new StreamHandler(logOut, new SimpleFormatter());
        handler.setLevel(Level.WARNING);
        logger.addHandler(handler);       
        logger.setLevel(Level.WARNING);
        try {
            throw new GlassFishIdeException();
        } catch (GlassFishIdeException gfie) {
            handler.flush();
        } finally {
            logger.removeHandler(handler);
            handler.close();
            logger.setLevel(logLevel);
        }
        String logMsg = logOut.toString();
        int contains = logMsg.indexOf(gfieMsg);
        assertTrue(contains > -1);
    }

    /**
     * Test GlassFishException with message throwing and logging.
     */
    @Test
    public void testGlassFishExceptionWithMsg() {
        String gfieMsg = "Test exception";
        java.util.logging.Logger logger = Logger.getLogger();
        Level logLevel = logger.getLevel();
        OutputStream logOut = new ByteArrayOutputStream(256);
        Handler handler = new StreamHandler(logOut, new SimpleFormatter());
        handler.setLevel(Level.WARNING);
        logger.addHandler(handler);       
        logger.setLevel(Level.WARNING);
        try {
            throw new GlassFishIdeException(gfieMsg);
        } catch (GlassFishIdeException gfie) {
            handler.flush();
        } finally {
            logger.removeHandler(handler);
            handler.close();
            logger.setLevel(logLevel);
        }
        String logMsg = logOut.toString();
        int contains = logMsg.indexOf(gfieMsg);
        assertTrue(contains > -1);
    }

    /**
     * Test GlassFishException with message and cause <code>Throwable</code>
     * throwing and logging.
     */
    @Test
    public void testGlassFishExceptionWithMsgAndCause() {
        String gfieMsg = "Test exception";
        String causeMsg = "Cause exception";
        java.util.logging.Logger logger = Logger.getLogger();
        Level logLevel = logger.getLevel();
        OutputStream logOut = new ByteArrayOutputStream(256);
        Handler handler = new StreamHandler(logOut, new SimpleFormatter());
        handler.setLevel(Level.WARNING);
        logger.addHandler(handler);       
        logger.setLevel(Level.WARNING);
        try {
            try {
                throw new Exception(causeMsg);
            } catch (Exception e) {
                throw new GlassFishIdeException(gfieMsg, e);
            }
        } catch (GlassFishIdeException gfie) {
            handler.flush();
        } finally {
            logger.removeHandler(handler);
            handler.close();
            logger.setLevel(logLevel);
        }
        String logMsg = logOut.toString();
        int containsGfieMsg = logMsg.indexOf(gfieMsg);
        int containsCauseMsg = logMsg.indexOf(causeMsg);
        assertTrue(containsGfieMsg > -1 && containsCauseMsg > -1);
    }

}
