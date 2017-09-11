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

package org.netbeans.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.core.NbErrorManager.Exc;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach
 */
public class NotifyExcPanelTest extends NbTestCase {
    Logger main;
    
    public NotifyExcPanelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        main = Logger.getLogger("");
        for (Handler h : main.getHandlers()) {
            main.removeHandler(h);
        }
    }
    
    public void testHandlesThatImplementCallableForJButtonAreIncluded() throws Exception {
        class H extends Handler 
        implements Callable<JButton> {
            public JButton button = new JButton("Extra");
        
            public void publish(LogRecord arg0) {
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }

            public JButton call() throws Exception {
                return button;
            }
        } // end of H
        
        H handler = new H();
        
        main.addHandler(handler);
        
        List<Object> options = Arrays.asList(NotifyExcPanel.computeOptions("prev", "next"));
        
        assertTrue("Contains our button: " + options, options.contains(handler.button));
    }

    public void testHandlesThatImplementCallableForOtherObjectsAreNotIncluded() throws Exception {
        class H extends Handler 
        implements Callable<Object> {
            public JButton button = new JButton("Extra");
        
            public void publish(LogRecord arg0) {
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }

            public JButton call() throws Exception {
                return button;
            }
        } // end of H
        
        H handler = new H();
        
        main.addHandler(handler);
        
        List<Object> options = Arrays.asList(NotifyExcPanel.computeOptions("prev", "next"));
        
        assertFalse("Does not contain our button: " + options, options.contains(handler.button));
    }
    
    public void testLimitOf20() {
        NotifyExcPanel.ArrayListPos arr = new NotifyExcPanel.ArrayListPos();
        for (int i = 0; i < (NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE-1); i++) {
            arr.add(null);
        }
        assertEquals("Nineteen elements", NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE-1, arr.size());
        arr.add(null);
        assertEquals("Twenty", NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE, arr.size());
        
        Exc ex = new Exc(null, Level.OFF, new LogRecord[0], new LogRecord[0]);
        arr.add(ex);
        
        assertEquals("Still twenty", NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE, arr.size());
        assertEquals(ex, arr.get(NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE-1));
        
    }
    
    public void testManyExceptionsReported() {
        int numExceptions = 10 * NotifyExcPanel.ArrayListPos.HARD_MAX_SIZE;
        NbErrorManager errorManager = new NbErrorManager();
        Logger excLogger = Logger.getLogger(Exceptions.class.getName());
        final LogRecord[] logRecordRef = new LogRecord[] { null };
        Handler logHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logRecordRef[0] = record;
            }
            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        excLogger.addHandler(logHandler);
        for (int i = 0; i < numExceptions; i++) {
            Exception exc = new ClassCastException(Integer.toString(i));
            exc = Exceptions.attachMessage(exc, "A message "+i);
            Exceptions.printStackTrace(exc);
            errorManager.publish(logRecordRef[0]);
        }
        // Wait for the Exc creations...
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override public void run() {}
            });
        } catch (InterruptedException | InvocationTargetException ex) {}
        int excSize = NotifyExcPanel.exceptions.size();
        assertTrue("We have less exceptions than the hard limit", excSize <= NotifyExcPanel.ArrayListPos.HARD_MAX_SIZE);
        for (int i = 0; i < excSize; i++) {
            NbErrorManager.Exc exc = (NbErrorManager.Exc) NotifyExcPanel.exceptions.get(i);
            LogRecord[] arr = exc.arr;
            LogRecord[] arrAll = exc.arrAll;
            // Size should be the exception + annotation = 2
            assertEquals("Records size", 2, arr.length);
            assertEquals("All records size", 2, arrAll.length);
            if (i >= (excSize - 10)) {  // Check that the last 10 exceptions are the last 10 thrown:
                int fromEnd = excSize - i;
                assertEquals("Correct record", Integer.toString(numExceptions - fromEnd), exc.getMessage());
                assertEquals("Correct record", "A message "+(numExceptions - fromEnd), arr[0].getMessage());
            }
        }
        excLogger.removeHandler(logHandler);
    }
}
