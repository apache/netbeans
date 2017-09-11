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
package org.netbeans.modules.nativeexecution.api.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 *
 * @author vkvashin
 */
class SlowListenerDetector {
    
    private final Logger logger;
    private final Level level;
    private final Timer timer;
    private final int timeout;
    
    private final Object lock = new Object();
    private Thread thread;
    private StackTraceElement[] stackTrace;
    private long startTime;
    private String methodName;

    public SlowListenerDetector(int timeout, Logger log, Level level) {
        this.logger = log;
        this.level = level;
        this.timeout = timeout;
        timer = new Timer(timeout, new TimerListener());
    }

    private void clear() {
        synchronized (lock) {
            thread = null;
            stackTrace = null;
            startTime = 0;
        }
    }
    
    public void start(String methodName) {
        synchronized (lock) {
            clear();
            this.methodName = methodName;
            thread = Thread.currentThread();
            startTime = System.currentTimeMillis();
        }
        timer.start();
    }
    
    public void stop() {
        timer.stop();
        Exception ex = null;
        synchronized (lock) {
            long time = System.currentTimeMillis() - startTime;
            if (time > timeout && stackTrace != null) {
                ex = new SlowListenerException("Too much time spent in " +  //NOI18N
                        methodName + ": " + time + " ms", stackTrace); //NOI18N
            }
            clear();
        }
        if (ex != null) {
            logger.log(level, ex.getMessage(), ex);
        }
    }

    private class TimerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            synchronized(lock) {
                stackTrace = thread.getStackTrace();
            }
        }
    }
    
    private static class SlowListenerException extends RuntimeException {
        public SlowListenerException(String message, StackTraceElement[] stackTrace) {
            super(message);
            setStackTrace(stackTrace);
        }        
    }
}
