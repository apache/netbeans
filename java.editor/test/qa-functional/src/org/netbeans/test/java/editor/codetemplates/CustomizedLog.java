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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.test.java.editor.codetemplates;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jiri Prox
 */
public class CustomizedLog extends Handler {
    
    public CustomizedLog() {   
    }

    public void publish(LogRecord record) {
        throw new UnsupportedOperationException("publish");
    }

    public void flush() {
        throw new UnsupportedOperationException("flush");
    }

    public static void enableInstances(Logger log, String msg, Level level) {
        if (log == null) {
            log = Logger.getLogger("TIMER"); // NOI18N
        }

        log.addHandler(new InstancesHandler(msg, level,2));

        if (log.getLevel() == null || log.getLevel().intValue() > level.intValue()) {
            log.setLevel(level);
        }       
    }

    public static CharSequence enable(String loggerName, Level level) {
        return Log.enable(loggerName, level);
    }

    public static void controlFlow(Logger listenTo, Logger reportTo, String order, int timeout) {
        Log.controlFlow(listenTo, reportTo, order, timeout);
    }

    public void close() {
        throw new UnsupportedOperationException("close");
    }

    public static void assertInstances(String msg, String... names) {
        InstancesHandler.assertGC(msg, names);
    }

    public static void assertInstances(String msg) {
        InstancesHandler.assertGC(msg);
    }
    

    private static class InstancesHandler extends Handler {
        static final Map<Object,String> instances = Collections.synchronizedMap(new WeakHashMap<Object,String>());
        static int cnt;

        private final String msg;

        private static int treshhold;

        public InstancesHandler(String msg, Level level,int treshhold) {
            setLevel(level);
            this.msg = msg;
            InstancesHandler.treshhold = treshhold;
        }

        @Override
        public void publish(LogRecord record) {
            Object[] param = record.getParameters();
            if (param == null) {
                return;
            }
            if (msg != null && !msg.equals(record.getMessage())) {
                return;
            }
            cnt++;
            for (Object o : param) {
                instances.put(o, record.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public static void assertGC(String msg, String... names) {
            AssertionFailedError t = null;

            List<Reference> refs = new ArrayList<Reference>();
            List<String> txts = new ArrayList<String>();
            int count = 0;
            Set<String> nameSet = names == null || names.length == 0 ? null : new HashSet<String>(Arrays.asList(names));
            synchronized (instances) {
                for (Iterator<Map.Entry<Object, String>> it = instances.entrySet().iterator(); it.hasNext();) {
                    Entry<Object, String> entry = it.next();
                    if (nameSet != null && !nameSet.contains(entry.getValue())) {
                        continue;
                    }

                    refs.add(new WeakReference<Object>(entry.getKey()));
                    txts.add(entry.getValue());
                    it.remove();
                    count++;
                }
            }

            if (count == 0) {
                Assert.fail("No instance of this type reported");
            }

            int cannotBeCollected = 0;
            for (int i = 0; i < count; i++) {
                Reference<?> r = refs.get(i);
                try {
                    NbTestCase.assertGC(msg + " " + txts.get(i), r);
                } catch (AssertionFailedError ex) {
                    cannotBeCollected++;
                    if (t == null) {
                        t = ex;
                    } else {
                        Throwable last = t;
                        while (last.getCause() != null) {
                            last = last.getCause();
                        }
                        last.initCause(ex);
                    }
                }
            }            
            if (t != null && cannotBeCollected > treshhold) {
                throw t;
            }
        }

    }
}
