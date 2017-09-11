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
