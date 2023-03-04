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

package org.netbeans.modules.web.common.api;

import org.netbeans.junit.NbTestCase;

public class UsageLoggerTest extends NbTestCase {

    public UsageLoggerTest(String name) {
        super(name);
    }

    public void testLogAllMessages() {
        UsageLogger logger = new UsageLogger.Builder("mylogger")
                .message(UsageLoggerTest.class, "allmessages({0})")
                .firstMessageOnly(false)
                .create();
        assertTrue(logger.isLoggingEnabled());
        logger.log(1);
        assertTrue(logger.isLoggingEnabled());
        logger.log(2);
        assertTrue(logger.isLoggingEnabled());
    }

    public void testLogFirstMessageOnly() {
        UsageLogger logger = new UsageLogger.Builder("mylogger")
                .message(UsageLoggerTest.class, "onmessage({0})")
                .create();
        assertTrue(logger.isLoggingEnabled());
        assertTrue(logger.isLoggingEnabled());
        logger.log(1);
        assertFalse(logger.isLoggingEnabled());
        logger.log(2);
        assertFalse(logger.isLoggingEnabled());
        logger.reset();
        assertTrue(logger.isLoggingEnabled());
        assertTrue(logger.isLoggingEnabled());
        logger.log(3);
        assertFalse(logger.isLoggingEnabled());
    }

    public void testDefaultMessage() {
        UsageLogger logger = new UsageLogger.Builder("mylogger")
                .message(UsageLoggerTest.class, "defaultmessage")
                .create();
        try {
            logger.log();
            logger.log();
        } catch (IllegalStateException ex) {
            fail("Should not get here");
        }
    }

    public void testMoreMessages() {
        UsageLogger logger = new UsageLogger.Builder("mylogger")
                .create();
        logger.log(UsageLoggerTest.class, "message1");
        logger.log(UsageLoggerTest.class, "message2");
        try {
            logger.log();
            fail("should not get here");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

}
