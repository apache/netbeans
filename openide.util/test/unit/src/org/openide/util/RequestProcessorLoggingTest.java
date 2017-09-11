/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.openide.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.Test;
import org.openide.util.RequestProcessor;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class RequestProcessorLoggingTest extends Handler {
    private Thread thread;
    private static RequestProcessor rp = new RequestProcessor("Does logging");
    private volatile boolean isRP = true;
    private volatile int cnt;

    @Override
    public void publish(LogRecord record) {
        if (Thread.currentThread() == thread) {
            cnt++;
            isRP &= rp.isRequestProcessorThread();
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    @Test public void isRequestProcessorThreadProperlySetDuringLogging() {
        Logger l = Logger.getLogger(RequestProcessor.class.getName());
        l.addHandler(this);
        l.setLevel(Level.ALL);
        setLevel(Level.ALL);
        
        class R implements Runnable {
            @Override
            public void run() {
                thread = Thread.currentThread();
            }
        }
        rp.post(new R()).waitFinished();
        
        assertNotNull("Thread remembered", thread);
        
        class Empty implements Runnable {
            @Override
            public void run() {
            }
        }
        rp.post(new Empty()).waitFinished();
        
        if (cnt == 0) {
            fail("At least some logging should occur");
        }
        assertTrue("isRequestProcessorThread worked OK", isRP);
    }
}
