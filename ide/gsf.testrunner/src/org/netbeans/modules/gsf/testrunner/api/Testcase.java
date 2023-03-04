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

package org.netbeans.modules.gsf.testrunner.api;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.Parameters;

/**
 * Represents a single test case.
 */
public class Testcase {

    /**
     * The type of this test case.
     */
    private final String type;
    /**
     * The name of the class that contains this test case.
     */
    private String className;
    private final String name;
    private final String displayName;
    private long timeMillis;
    private Trouble trouble;
    private Status status;
    /**
     * The lines outputted during the execution of this test case.
     */
    private final List<OutputLine> output = new ArrayList<OutputLine>();
    /**
     * The location, i.e. the file and line number of this test case.
     */
    private String location;
    private TestSession session;

    public Testcase(String name, String type, TestSession session) {
        this(name, name, type, session);
    }
    
    /**
     * Creates a new Testcase.
     *
     * @param name the name of this test case.
     * @param displayName The display name of this test case.
     * @param type the type of the test case, e.g. for Ruby it might be
     * <code>"RSPEC"</code> or <code>"TEST/UNIT"</code>. May be <code>null</code>.
     * @param session the session where this test case is executed.
     */
    public Testcase(String name, String displayName, String type, TestSession session) {
        Parameters.notNull("name", name);
        Parameters.notNull("displayName", displayName);
        Parameters.notNull("session", session);
        this.name = name;
        this.displayName = displayName;
        this.session = session;
        this.type = type;
    }

    public TestSession getSession() {
        return session;
    }

    /**
     * @return the type of this test case.
     * @see #type
     */
    public String getType() {
        return type;
    }

    /**
     * @see #location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Adds the given lines as output for this test case.
     * 
     * @param lines lines outputted while executing this test case.
     */
    public void addOutputLines(List<String> lines) {
        for (String line : lines) {
            if(output.size() == TestSuite.MAX_TOOLTIP_LINES + 1) {
                // Do not add any more output to prevent OOME, as it will not be used in the end, as at most MAX_TOOLTIP_LINES will be shown as a tooltip
                return;
            }
            output.add(new OutputLine(line, false));
        }
    }

    /**
     * @return lines outputted while executing this test case.
     */
    public List<OutputLine> getOutput() {
        return output;
    }


    /**
     * Gets the location, i.e. the path to the file and line number of the test case.
     * May be null if such info is not available.
     * @return
     */
    public String getLocation() {
        return location;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        if (status != null) {
            return status;
        }
        if (trouble == null) {
            return Status.PASSED;
        }
        return trouble.isError() ? Status.ERROR : Status.FAILED;
    }

    /**
     * @return the class name; may return <code>null</code>.
     * @see #className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set.
     * @see #className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the name of this test case.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return The display name of this test case.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return the timeMillis
     */
    public long getTimeMillis() {
        return timeMillis;
    }

    /**
     * @param timeMillis the timeMillis to set
     */
    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    /**
     * @return the trouble
     */
    public Trouble getTrouble() {
        return trouble;
    }

    /**
     * @param trouble the trouble to set
     */
    public void setTrouble(Trouble trouble) {
        this.trouble = trouble;
    }

    @Override
    public String toString() {
        return Testcase.class.getSimpleName() + "[class: " + className + ", name: " + name + "]"; //NOI18N
    }

}
