/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup;

import java.awt.EventQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

public class TopLoggingAWTTest extends NbTestCase 
implements Thread.UncaughtExceptionHandler {
    static {
        assertNull("No handler yet", Thread.getDefaultUncaughtExceptionHandler());
        Thread.setDefaultUncaughtExceptionHandler(new TopLoggingAWTTest("handler"));
    }
    private static Throwable uncaught;
    private static LogRecord published;
    
    public TopLoggingAWTTest(String name) {
        super(name);
    }

    public void testAWTErrorReported() throws Exception {
        TopLogging.initialize();
        MockServices.setServices(MyHandler.class);
        
        final IllegalStateException ex = new IllegalStateException("I am broken");
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                throw ex;
            }
        });
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
        assertEquals("Exception provided", ex, getPublished().getThrown());
        assertNotNull("Our handler called", uncaught);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        uncaught = e;
    }
    
    public static synchronized LogRecord getPublished() throws InterruptedException {
        while (published == null) {
            TopLoggingAWTTest.class.wait();
        }
        return published;
    }
    
    public static synchronized void setPublished(LogRecord r) {
        published = r;
        TopLoggingAWTTest.class.notifyAll();
    }
    
    public static final class MyHandler extends Handler {
        
        @Override
        public void publish(LogRecord record) {
            setPublished(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
    }
}
