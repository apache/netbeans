/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla;

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author tomas
 */
public class LogHandler extends Handler {
    public static long DEFAULT_TIMEOUT = 30 * 1000;
    private long TIMEOUT = DEFAULT_TIMEOUT;
    private final String messageToWaitFor;
    private String interceptedMessage;
    private boolean done = false;
    private final Compare compare;
    private final int expectedCount;
    private int interceptedCount = 0;
    private boolean reset = false;
    public enum Compare {
        STARTS_WITH,
        ENDS_WITH
    }

    public LogHandler(String msg, Compare compare) {
        this(msg, compare, DEFAULT_TIMEOUT);
    }
    
    public LogHandler(String msg, Compare compare, long timeout) {
        this(msg, compare, timeout, 1);
    }

    public LogHandler(String msg, Compare compare, long timeout, int count) {
        this.expectedCount = count;
        this.messageToWaitFor = msg;
        this.compare = compare;
        Bugzilla.LOG.addHandler(this);
        if(timeout > -1) {
            TIMEOUT = timeout * 1000;
        }
    }

    @Override
    public void publish(LogRecord record) {
        if(!done) {
            String message = record.getMessage();
            if(message == null) {
                return;
            }
            message = MessageFormat.format(message, record.getParameters());
            boolean intercepted = false;
            switch (compare) {
                case STARTS_WITH :
                    intercepted = message.startsWith(messageToWaitFor);
                    break;
                case ENDS_WITH :
                    intercepted = message.endsWith(messageToWaitFor);
                    break;
                default:
                    throw new IllegalStateException("wrong value " + compare);
            }
            if(intercepted) {
                interceptedCount++;
                interceptedMessage = message;
            }
            done = intercepted && interceptedCount >= expectedCount;
        }
    }

    public int getInterceptedCount() {
        return interceptedCount;
    }
    
    public boolean isDone() {
        return done;
    }

    public void reset() {
        reset = true;
    }

    public String getInterceptedMessage() {
        return interceptedMessage;
    }

    @Override
    public void flush() { }
    @Override
    public void close() throws SecurityException { }

    public void waitUntilDone() throws InterruptedException {
        reset = false;
        long t = System.currentTimeMillis();
        while(!done && !reset && interceptedCount < expectedCount) {
            Thread.sleep(200);
            if(System.currentTimeMillis() - t > TIMEOUT) {
                throw new IllegalStateException("Timeout > " + TIMEOUT + " for " + messageToWaitFor);
            }
        }
    }
}
