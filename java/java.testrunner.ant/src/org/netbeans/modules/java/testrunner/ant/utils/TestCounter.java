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

package org.netbeans.modules.java.testrunner.ant.utils;

import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.TaskStructure;

/**
 * Counts test classes to be executed by the current Ant test task.
 *
 * @author  Marian Petras
 */
public final class TestCounter {
    
    
    // --------------- static members -----------------
    
    
    /**
     * Counts test classes.
     *
     * @param  event  Ant event holding information about the current context
     *                of the Ant task
     * @return  (approximate) number of test classes that are going to be
     *          executed by the task
     */
    public static int getTestClassCount(AntEvent event) {
        TestCounter counter = new TestCounter(event);
        return counter.countTestClasses();
    }
    
    
    // ------------ non-static members ----------------
    
    
    /**
     * Ant event holding information about the current context of the running
     * Ant task.
     */
    private final AntEvent event;
    
    /**
     * Creates a new instance of the test counter.
     *
     * @param  event  Ant event holding information about the current context
     *                of the Ant task
     */
    private TestCounter(AntEvent event) {
        this.event = event;
    }
    
    /**
     * Counts test classes going to be executed by the current test task.
     *
     * @return  (approximate) number of test classes that are going to be
     *          executed by the task
     */
    private int countTestClasses() {
        final String taskName = event.getTaskName();
        
        if (taskName.equals(AntLoggerUtils.TASK_JUNIT)) {
            return countTestClassesInJUnitTask();
        } else if (taskName.equals(AntLoggerUtils.TASK_JAVA)) {
            return countTestClassesInJavaTask();
        }
        
        assert false : "Unhandled task name";                           //NOI18N
        return -1;
    }
    
    /**
     * Counts number of test classes that are going to be executed
     * by the current {@code <junit>} task.
     *
     * @param  event  event produced by the currently running Ant session
     * @return  approximate number of test classes;
     *          or {@code -1} if the number is unknown
     */
    private int countTestClassesInJUnitTask() {
        int count = 0;
        
        TaskStructure taskStruct = event.getTaskStructure();
        for (TaskStructure child : taskStruct.getChildren()) {
            String childName = child.getName();
            if (childName.equals("test")) {                             //NOI18N
                if (conditionsMet(child)) {
                    count++;
                }
                continue;
            }
            if (childName.equals("batchtest")) {                        //NOI18N
                if (conditionsMet(child)) {
                    AntProject project = new AntProject(event);
                    BatchTest batchTest = new BatchTest(project);
                    batchTest.handleChildrenAndAttrs(child);
                    int n = batchTest.countTestClasses();
                    if (n > 0) {
                        count += n;
                    }
                }
                continue;
            }
        }
        return count;
    }
    
    /**
     * Checks whether {@code if} and {@code unless} conditions of the given
     * Ant XML element are met.
     *
     * @param  struct  Ant XML element to be probed
     * @param  event  Ant event which allows evaluation of Ant variables
     * @return  {@code false} if there are conditions that are not met,
     *          {@code true} otherwise
     */
    private boolean conditionsMet(TaskStructure struct) {
        String ifPropName = struct.getAttribute("if");                  //NOI18N
        String unlessPropName = struct.getAttribute("unless");          //NOI18N
        
        if ((ifPropName != null)
                && (event.getProperty(ifPropName) == null)) {
            return false;
        }
        if ((unlessPropName != null)
                && (event.getProperty(unlessPropName) != null)) {
            return false;
        }
        return true;
    }
    
    /**
     * Counts number of test classes that are going to be executed
     * by the current {@code <java>} task.
     *
     * @param  event  event produced by the currently running Ant session
     * @return  approximate number of test classes;
     *          or {@code -1} if the number is unknown
     */
    private int countTestClassesInJavaTask() {
        return 1;
    }
    
}
