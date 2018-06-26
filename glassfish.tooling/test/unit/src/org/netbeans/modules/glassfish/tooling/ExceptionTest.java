/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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

    ////////////////////////////////////////////////////////////////////////////
    // Test methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

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
