/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.debugger.jpda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.jpda.BreakpointsClassFilter;

/**
 * Test of {@link BreakpointsClassFilter}.
 * 
 * @author Martin Entlicher
 */
public class BreakpointsClassFilterTest extends NbTestCase {
    
    private static final String TEST_APP_PATH = System.getProperty ("test.dir.src") + 
        "org/netbeans/api/debugger/jpda/testapps/BreakpointsClassFilterApp.java";
    private static final String FILTER_GROUP_NAME = "filtered";
    private static final String APP_CLASS_NAME = "org.netbeans.api.debugger.jpda.testapps.BreakpointsClassFilterApp";
    private static final String APP2_CLASS_NAME = APP_CLASS_NAME + "2";
    
    private JPDASupport support;
    
    /*
    public static Test suite() {
        return JPDASupport.createTestSuite(BreakpointsClassFilterTest.class);
    }
    */

    public BreakpointsClassFilterTest(String s) {
        super(s);
    }

    /**
     * Two breakpoints of each kind are submitted,
     * the first is hit in BreakpointsClassFilterApp class only,
     * but the second is hit also in BreakpointsClassFilterApp2 class.
     * The latter has {@link #FILTER_GROUP_NAME} set as a group name to
     * distinguish the filtered breakpoints.
     * Hits in the two application classes are checked by TestBreakpointListener.
     * 
     * @throws Exception 
     */
    public void testFilteredBreakpoints() throws Exception {
        JPDASupport.removeAllBreakpoints ();
        List<JPDABreakpoint> breakpoints = new ArrayList<JPDABreakpoint>();
        Utils.BreakPositions bp = Utils.getBreakPositions(TEST_APP_PATH);
        List<LineBreakpoint> lineBreakpoints = bp.getLineBreakpoints();
        lineBreakpoints.get(1).setGroupName(FILTER_GROUP_NAME);
        
        MethodBreakpoint mb1 = MethodBreakpoint.create(APP_CLASS_NAME, "test");
        mb1.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        
        FieldBreakpoint fb1 = FieldBreakpoint.create(APP_CLASS_NAME, "field", FieldBreakpoint.TYPE_ACCESS);
        FieldBreakpoint fb2 = FieldBreakpoint.create(APP_CLASS_NAME, "field2", FieldBreakpoint.TYPE_ACCESS);
        fb2.setGroupName(FILTER_GROUP_NAME);
        
        ExceptionBreakpoint eb1 = ExceptionBreakpoint.create(NegativeArraySizeException.class.getName(), ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT_UNCAUGHT);
        ExceptionBreakpoint eb2 = ExceptionBreakpoint.create(ArithmeticException.class.getName(), ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT_UNCAUGHT);
        eb2.setGroupName(FILTER_GROUP_NAME);
        
        MethodBreakpoint mb2 = MethodBreakpoint.create(APP_CLASS_NAME, "test");
        mb2.setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
        mb2.setGroupName(FILTER_GROUP_NAME);
        
        breakpoints.add(lineBreakpoints.get(0));
        breakpoints.add(mb1);
        breakpoints.add(fb1);
        breakpoints.add(eb1);
        breakpoints.add(lineBreakpoints.get(1));
        breakpoints.add(mb2);
        breakpoints.add(fb2);
        breakpoints.add(eb2);
        
        List<TestBreakpointListener> listeners = new ArrayList<TestBreakpointListener>();
        for (JPDABreakpoint b : breakpoints) {
            TestBreakpointListener tbl = new TestBreakpointListener 
                (b, FILTER_GROUP_NAME.equals(b.getGroupName()), breakpoints);
            b.addJPDABreakpointListener(tbl);
            DebuggerManager.getDebuggerManager ().addBreakpoint (b);
            listeners.add(tbl);
        }

        support = JPDASupport.attach (
            APP_CLASS_NAME
        );
        support.waitState(JPDADebugger.STATE_DISCONNECTED);
        assertTrue(Arrays.toString(breakpoints.toArray()), breakpoints.isEmpty());
        for (TestBreakpointListener l : listeners) {
            assertTrue(l.getMessage(), l.isOK());
        }
    }
    
    private class TestBreakpointListener implements JPDABreakpointListener {
        
        private final JPDABreakpoint breakpoint;
        private final boolean isFiltered;
        private final List<JPDABreakpoint> allBreakpoints;
        private Boolean ok = null;
        private String message = "Not hit yet.";
        private final Set<String> toBeHitIn;
        
        public TestBreakpointListener(JPDABreakpoint breakpoint, boolean isFiltered,
                                      List<JPDABreakpoint> allBreakpoints) {
            this.breakpoint = breakpoint;
            this.isFiltered = isFiltered;
            this.allBreakpoints = allBreakpoints;
            if (isFiltered) {
                toBeHitIn = new HashSet<String>();
                toBeHitIn.add(APP_CLASS_NAME);
                toBeHitIn.add(APP2_CLASS_NAME);
            } else {
                toBeHitIn = null;
            }
        }

        @Override
        public void breakpointReached(JPDABreakpointEvent event) {
            //System.err.println("breakpointReached("+event+", isFiltered = "+isFiltered+"), referenceType = "+event.getReferenceType());
            if (!isFiltered) {
                boolean removed = allBreakpoints.remove(breakpoint);
                if (ok == null && removed) {
                    ok = Boolean.TRUE;
                    message = "O.K.";
                } else {
                    if (ok != null) {
                        message += "Hit again at "+event.getReferenceType()+" ";
                        ok = Boolean.FALSE;
                    } else if (!removed) {
                        message += "BP not in list. ";
                        ok = Boolean.FALSE;
                    }
                }
            } else { // Filtered, expected to hit twice (in APP_CLASS_NAME and APP2_CLASS_NAME)
                //System.err.println("breakpointReached("+event+", isFiltered = "+isFiltered+"), referenceType = "+event.getReferenceType());
                String className = event.getReferenceType().name();//event.getThread().getClassName();
                boolean removed = toBeHitIn.remove(className);
                if (ok == null && removed && toBeHitIn.isEmpty()) {
                    allBreakpoints.remove(breakpoint);
                    ok = Boolean.TRUE;
                    message = "O.K.";
                } else {
                    if (!removed) {
                        message += "Hit again at "+className+" ";
                        ok = Boolean.FALSE;
                    }
                }
            }
            event.resume();
        }
        
        public boolean isOK() {
            return Boolean.TRUE.equals(ok);
        }
        
        public String getMessage() {
            return message + " " + breakpoint+" with group "+breakpoint.getGroupName();
        }
    
    }
    
    @BreakpointsClassFilter.Registration(path="netbeans-JPDASession")
    public static class TestBreakpointsClassFilter extends BreakpointsClassFilter {
        
        public TestBreakpointsClassFilter() {
            //System.err.println("NEW TestBreakpointsClassFilter()");
        }

        @Override
        public ClassNames filterClassNames(ClassNames classNames, JPDABreakpoint breakpoint) {
            //System.err.println("filterClassNames("+Arrays.toString(classNames.getClassNames()));
            String groupName = breakpoint.getGroupName();
            if (FILTER_GROUP_NAME.equals(groupName)) {
                String[] classes = classNames.getClassNames();
                String[] newClasses = new String[classes.length + 1];
                System.arraycopy(classes, 0, newClasses, 0, classes.length);
                if (breakpoint instanceof ExceptionBreakpoint) {
                    newClasses[classes.length] = NullPointerException.class.getName();
                } else {
                    newClasses[classes.length] = APP2_CLASS_NAME;
                }
                return new ClassNames(newClasses, classNames.getExcludedClassNames());
            } else {
                return classNames;
            }
        }
        
    }
    
}
