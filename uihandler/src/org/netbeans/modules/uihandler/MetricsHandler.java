/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.uihandler;

import java.util.List;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Marek Slama
 *
 */
public class MetricsHandler extends Handler {
    private static Task lastRecord = Task.EMPTY;
    private static RequestProcessor FLUSH = new RequestProcessor("Flush Metrics Logs"); // NOI18N
    private static boolean flushOnRecord;
    static final int MAX_LOGS = 400;
    /** Maximum number of days, after which we send the report. 40 days ± one week.*/
    static final int MAX_DAYS = 33 + new Random(System.currentTimeMillis()).nextInt(14);
    /** Maximum allowed size of backup log file 10MB */
    static final long MAX_LOGS_SIZE = 10L * 1024L * 1024L;
    
    public MetricsHandler() {
        setLevel(Level.FINEST);
    }

    @Override
    public void publish(LogRecord record) {

        class WriteOut implements Runnable {
            public LogRecord r;
            @Override
            public void run() {
                Installer.writeOutMetrics(r);
                r = null;
            }
        }
        WriteOut wo = new WriteOut();
        wo.r = record;
        lastRecord = FLUSH.post(wo);

        if (flushOnRecord) {
            waitFlushed();
        }
    }

    @Override
    public void flush() {
        waitFlushed();
    }
    
    static void flushImmediatelly() {
        flushOnRecord = true;
    }

    static void setFlushOnRecord (boolean flushOnRecord) {
        MetricsHandler.flushOnRecord = flushOnRecord;
    }
    
    static void waitFlushed() {
        try {
            lastRecord.waitFinished(1000);
        } catch (InterruptedException ex) {
            Installer.LOG.log(Level.FINE, null, ex);
        }
    }

    @Override
    public void close() throws SecurityException {
    }

    void publishEarlyRecords(List<LogRecord> earlyRecords) {
        for (LogRecord r : earlyRecords) {
            publish(r);
        }
    }
    
}
