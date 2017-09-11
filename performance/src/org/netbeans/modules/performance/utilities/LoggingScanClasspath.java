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
package org.netbeans.modules.performance.utilities;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author mmirilovic@netbeans.org
 */
public class LoggingScanClasspath {

    private static ArrayList<PerformanceScanData> data = new ArrayList<PerformanceScanData>();

    /**
     * Creates a new instance of LoggingScanClasspath
     */
    public LoggingScanClasspath() {
    }

    public void reportScanOfFile(String file, Long measuredTime) {
        data.add(new PerformanceScanData(file, measuredTime));
    }

    public static ArrayList<PerformanceScanData> getData() {
        return data;
    }

    public static void printMeasuredValues(PrintStream ps) {
        for (PerformanceScanData performanceScanData : data) {
            ps.println(performanceScanData);
        }
    }

    public class PerformanceScanData {

        private String name;
        private long value;
        private String fullyQualifiedName;

        public PerformanceScanData() {
        }

        public PerformanceScanData(String name, Long value) {
            this(name, value, name);
        }

        public PerformanceScanData(String name, Long value, String fullyQualifiedName) {
            // jar:file:/path_to_jdk/src.zip!/
            // jar:file:/C:/path_jdk/src.zip!/
            // file:/path_to_project/jEdit41/src/

            int beginIndex;
            int endIndex;

            try {
                beginIndex = name.substring(0, name.lastIndexOf('/') - 1).lastIndexOf('/') + 1;
                endIndex = name.indexOf('!', beginIndex); // it's jar and it ends with '!/'
                if (endIndex == -1) { // it's directory and it ends with '/'
                    endIndex = name.length() - 1;
                    beginIndex = name.lastIndexOf('/', beginIndex - 2) + 1; // log "jedit41/src" not only "src" 
                }

                this.setName(name.substring(beginIndex, endIndex));
            } catch (Exception exc) {
                exc.printStackTrace(System.err);
                this.setName(name);
            }

            this.setValue(value.longValue());
            this.setFullyQualifiedName(fullyQualifiedName);
        }

        @Override
        public String toString() {
            return "name =[" + getName() + "] value=" + getValue() + " FQN=[" + getFullyQualifiedName() + "]";
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }

        public String getFullyQualifiedName() {
            return fullyQualifiedName;
        }

        public void setFullyQualifiedName(String fullyQualifiedName) {
            this.fullyQualifiedName = fullyQualifiedName;
        }

    }
}
