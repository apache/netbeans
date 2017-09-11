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

package org.openide;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.netbeans.junit.*;

/** Verify that things delegation of ErrorManager to logging and back does not cause
 * stack overflows.
 *
 * @author Jaroslav Tulach
 */
public class ErrorManagerCyclicDepTest extends NbTestCase {

    
    public ErrorManagerCyclicDepTest(java.lang.String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Logger l = new LoggerAdapter("double");
        LogManager.getLogManager().addLogger(l);
    }

    public void testSendLogMsg() {
        ErrorManager e = ErrorManager.getDefault().getInstance("double");
        e.log(ErrorManager.WARNING, "Ahoj");
    }

    public void testSendNotify() {
        ErrorManager e = ErrorManager.getDefault().getInstance("double");
        e.notify(ErrorManager.WARNING, new Exception("Ahoj"));
    }

    /** based on
     * https://thinnbeditor.dev.java.net/source/browse/thinnbeditor/thinnbeditor/src/net/java/dev/thinnbeditor/logging/LoggerAdapter.java?rev=1.1&view=auto&content-type=text/vnd.viewcvs-markup
     */
    private static final class LoggerAdapter extends Logger {
        private static final Map<Level,Integer> levelMap = new HashMap<Level,Integer>();
        private static final Map<Integer,Level> errorManagerMap = new TreeMap<Integer,Level>();
        private static final Map<Level,Integer> exceptionLevelMap = new HashMap<Level,Integer>();
        
        static {
            levelMap.put(Level.SEVERE, new Integer(ErrorManager.ERROR));
            levelMap.put(Level.WARNING, new Integer(ErrorManager.WARNING));
            levelMap.put(Level.INFO, new Integer(ErrorManager.INFORMATIONAL));
            levelMap.put(Level.CONFIG, new Integer(ErrorManager.INFORMATIONAL));
            levelMap.put(Level.FINE, new Integer(3));
            levelMap.put(Level.FINER, new Integer(2));
            levelMap.put(Level.FINEST, new Integer(1));
            
            for (Iterator<Map.Entry<Level,Integer>> i = levelMap.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<Level,Integer> entry = i.next();
                errorManagerMap.put(entry.getValue(), entry.getKey());
            }
            
            errorManagerMap.put(new Integer(ErrorManager.INFORMATIONAL), Level.CONFIG);
            
            exceptionLevelMap.put(Level.SEVERE, new Integer(ErrorManager.USER));
            exceptionLevelMap.put(Level.WARNING, new Integer(ErrorManager.USER));
            exceptionLevelMap.put(Level.INFO, new Integer(ErrorManager.INFORMATIONAL));
            exceptionLevelMap.put(Level.CONFIG, new Integer(ErrorManager.INFORMATIONAL));
            exceptionLevelMap.put(Level.FINE, new Integer(3));
            exceptionLevelMap.put(Level.FINER, new Integer(2));
            exceptionLevelMap.put(Level.FINEST, new Integer(1));
        }
        
        private ErrorManager errorManager;
        private final Formatter formatter = new SimpleFormatter();
        
        public LoggerAdapter(String name) {
            super(name, null);
        }

        private void init() {
            if (errorManager != null) {
                return;
            }
            
            errorManager = ErrorManager.getDefault().getInstance(getName());
            
            for (Iterator i = errorManagerMap.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry entry = (Map.Entry)i.next();
                
                int level = ((Integer)entry.getKey()).intValue();
                
                if (errorManager.isLoggable(level)) {
                    setLevel((Level)entry.getValue());
                    break;
                }
            }
        }
        
        public void log(LogRecord record) {
            init();

            errorManager.log(((Integer)levelMap.get(record.getLevel())).intValue(),
                formatter.format(record));
            
            if (record.getThrown() != null) {
                errorManager.notify(((Integer)exceptionLevelMap.get(
                    record.getLevel())).intValue(), record.getThrown());
            }
        }
    }

}
