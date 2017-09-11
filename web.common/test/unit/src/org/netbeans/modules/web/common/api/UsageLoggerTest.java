/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
