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

package org.netbeans.modules.openide.loaders;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.util.Exceptions;

/** Don't use this class outside fo core, copy its impl, if you really think
 * you need it. Prefered way is to use DialogDisplayer.notifyLater(...);
 *
 * @author Jaroslav Tulach
 */
public final class UIException {
    
    /** Creates a new instance of UIException */
    private UIException() {
    }
    
    public static void annotateUser(
        Throwable t,
        String msg,
        String locMsg,
        Throwable stackTrace,
        Date date
    ) {
        AnnException ex = AnnException.findOrCreate(t, true);
        LogRecord rec = new LogRecord(OwnLevel.USER, msg);
        if (stackTrace != null) {
            rec.setThrown(stackTrace);
        }
        ex.addRecord(rec);
        
        if (locMsg != null) {
            Exceptions.attachLocalizedMessage(t, locMsg);
        }
    }
    private static final class OwnLevel extends Level {
        public static final Level USER = new OwnLevel("USER", 1973); // NOI18N

        private OwnLevel(String s, int i) {
            super(s, i);
        }
    } // end of UserLevel
    private static final class AnnException extends Exception implements Callable<LogRecord[]> {
        private List<LogRecord> records;

        static AnnException findOrCreate(final Throwable t, boolean create) {
            if (t instanceof AnnException) {
                return (AnnException)t;
            }
            synchronized (t) {
                if (t.getCause() == null) {
                    if (create) {
                        t.initCause(new AnnException());
                    }
                    return (AnnException) t.getCause();
                }
            }
            return findOrCreate(t.getCause(), create);
        }

        private AnnException() {
        }

        public synchronized void addRecord(LogRecord rec) {
            if (records == null) {
                records = new ArrayList<LogRecord>();
            }
            records.add(rec);
        }

        public LogRecord[] call() {
            List<LogRecord> r = records;
            LogRecord[] empty = new LogRecord[0];
            return r == null ? empty : r.toArray(empty);
        }
    } // end AnnException
}

