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

    /**
     * Creates a new Testcase.
     *
     * @param name the name of this test case.
     * @param type the type of the test case, e.g. for Ruby it might be
     * <code>"RSPEC"</code> or <code>"TEST/UNIT"</code>. May be <code>null</code>.
     * @param session the session where this test case is executed.
     */
    public Testcase(String name, String type, TestSession session) {
        Parameters.notNull("name", name);
        Parameters.notNull("session", session);
        this.name = name;
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
