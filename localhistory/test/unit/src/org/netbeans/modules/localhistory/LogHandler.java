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

package org.netbeans.modules.localhistory;

import java.text.MessageFormat;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.openide.util.Exceptions;

/**
 *
 * @author tomas
 */
public class LogHandler extends Handler {
    private long TIMEOUT = 30 * 1000;
    private final String messageToWaitFor;
    private String interceptedMessage;
    private int hits = 0;
    private final Compare compare;
    private long blockTO = -1;

    public enum Compare {
        STARTS_WITH,
        ENDS_WITH
    }

    public LogHandler(String msg, Compare compare) {
        this(msg, compare, -1);
    }

    public LogHandler(String msg, Compare compare, int timeout) {
        this.messageToWaitFor = msg;
        this.compare = compare;
        LocalHistory.LOG.addHandler(this);
        if(timeout > -1) {
            TIMEOUT = timeout * 1000;
        }
    }

    @Override
    public void publish(LogRecord record) {
        if(hits < 1) {
            String message = record.getMessage();
            if(message == null) {
                return;
            }
            message = MessageFormat.format(message, record.getParameters());
            switch (compare) {
                case STARTS_WITH :
                    if(message.startsWith(messageToWaitFor)) {
                        hits++;
                    }
                    break;
                case ENDS_WITH :
                    if(message.endsWith(messageToWaitFor)) {
                        hits++;
                    }
                    break;
                default:
                    throw new IllegalStateException("wrong value " + compare);
            }
            if(hits > 0) {
                long t = System.currentTimeMillis();
                while(blockTO > -1 && System.currentTimeMillis() - t < blockTO) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                interceptedMessage = message;
            }
        }
    }

    public void reset() {
        hits = 0;
    }

    public void block(long blockTO) {
        this.blockTO = blockTO;
    }
    
    public void unblock() {
        this.blockTO = -1;
    }
    
    public boolean isDone() {
        return hits > 0;
    }

    public int getHits() {
        return hits;
    }

    public String getInterceptedMessage() {
        return interceptedMessage;
    }

    @Override
    public void flush() { }
    @Override
    public void close() throws SecurityException { }

    public void waitUntilDone() throws InterruptedException, TimeoutException {        
        long t = System.currentTimeMillis();
        while(hits < 1) {
            Thread.sleep(100);
            if(System.currentTimeMillis() - t > TIMEOUT) {
                throw new TimeoutException("timout while waiting for log message containing '" + messageToWaitFor + "'");
            }
        }
    }
    
    public void waitForHits(int hits, long timeout) throws InterruptedException, TimeoutException {        
        if(timeout < 0) {
            timeout = TIMEOUT;
        }
        long t = System.currentTimeMillis();
        while(this.hits < hits) {
            Thread.sleep(100);
            if(System.currentTimeMillis() - t > timeout) {
                throw new TimeoutException("timout while waiting for log message containing '" + messageToWaitFor + "'");
            }
        }
    }
}
