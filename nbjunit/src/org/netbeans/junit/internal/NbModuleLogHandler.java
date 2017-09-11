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
package org.netbeans.junit.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;

/** Registered in 'META-INF/services/java.util.logging.Handler' via
 * special classloader that delegates to FakeMetaInf.txt
 * 
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class NbModuleLogHandler extends Handler {
    private static StringBuffer text;
    private static Level msg;
    private static Level exc;

    public static Test registerBuffer(Level msg, Level exc) {
        if (msg == null) {
            msg = Level.OFF;
        }
        if (exc == null) {
            exc = Level.OFF;
        }

        if (exc == Level.OFF && msg == Level.OFF) {
            return null;
        }

        NbModuleLogHandler.msg = msg;
        NbModuleLogHandler.exc = exc;
        NbModuleLogHandler.text = new StringBuffer();
        Logger l = Logger.getLogger("");
        Level min = msg;
        if (min.intValue() > exc.intValue()) {
            min = exc;
        }
        l.setLevel(min);
        return new FailOnException(msg, exc);
    }

    public static void finish() {
        text = null;
    }

    public static StringBuffer toString(LogRecord record) {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(record.getLoggerName());
        sb.append("] THREAD: ");
        sb.append(Thread.currentThread().getName());
        sb.append(" MSG: ");
        String txt = record.getMessage();
        ResourceBundle b = record.getResourceBundle();
        if (b != null) {
            try {
                txt = b.getString(txt);
            } catch (MissingResourceException ex) {
                // ignore
            }
        }
        if (txt != null && record.getParameters() != null) {
            txt = MessageFormat.format(txt, record.getParameters());
        }
        sb.append(txt);
        Throwable t = record.getThrown();
        if (t != null) {
            sb.append('\n');
            StringWriter w = new StringWriter();
            t.printStackTrace(new PrintWriter(w));
            sb.append(w.toString().replace("\tat ", "  ").replace("\t... ", "  ... "));
        }
        sb.append('\n');
        return sb;
    }

    private static final List<String> hexes = new ArrayList<String>();
    private static final String integerToHexString = "[0-9a-fA-F]{5,8}";
    private static final Pattern hex = Pattern.compile("(?<=@(?:" + integerToHexString + ":)?)" + integerToHexString);
    public static synchronized String normalize(StringBuffer txt, String workDirPath) {
        Matcher m = hex.matcher(txt.toString().replace(workDirPath, "WORKDIR"));
        @SuppressWarnings("StringBufferMayBeStringBuilder")
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            String id = m.group().toLowerCase(Locale.ENGLISH);
            int i = hexes.indexOf(id);
            if (i == -1) {
                i = hexes.size();
                hexes.add(id);
            }
            m.appendReplacement(b, Integer.toHexString(i));
        }
        m.appendTail(b);
        return b.toString();
    }

    public NbModuleLogHandler() {
    }

    /** number of threads currently running {@link NbTestCase#assertGC(String, Reference, Set)} */
    private static final AtomicInteger ignoreOOME = new AtomicInteger();
    /**
     * Run a thunk while ignoring any OOME that might be thrown.
     */
    public static void whileIgnoringOOME(Runnable run) {
        ignoreOOME.incrementAndGet();
        try {
            run.run();
        } finally {
            ignoreOOME.decrementAndGet();
        }
    }

    @Override
    public void publish(LogRecord record) {
        StringBuffer t = text;
        if (t == null) {
            return;
        }

        if (record.getThrown() != null) {
            if (ignoreOOME.get() > 0 && record.getThrown() instanceof OutOfMemoryError) {
                return;
            }
            if (exc.intValue() <= record.getLevel().intValue()) {
                t.append(toString(record));
            }
        } else {
            if (msg.intValue() <= record.getLevel().intValue()) {
                t.append(toString(record));
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    public static void checkFailures(TestCase test, TestResult res, String workDirPath) {
        StringBuffer t = text;
        if (t == null) {
            return;
        }
        synchronized (t) {
            if (t.length() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("NbModuleSuite has been started with failOnMessage(");
                sb.append(msg);
                sb.append(") and failOnException(").append(exc);
                sb.append("). The following failures have been captured:\n");
                sb.append(normalize(text, workDirPath));
                res.addFailure(test, new AssertionFailedError(sb.toString()));
                t.setLength(0);
            }
        }
    }

    private static final class FailOnException extends TestCase {
        private final Level msg;
        private final Level exc;

        private FailOnException(Level msg, Level exc) {
            super("testNoWarningsReportedDuringExecution");
            this.msg = msg;
            this.exc = exc;
        }
        @Override
        public int countTestCases() {
            return 1;
        }

        @Override
        public void run(TestResult res) {
            checkFailures(this, res, /* XXX is this called? */Manager.getWorkDirPath());
        }

    }

}
