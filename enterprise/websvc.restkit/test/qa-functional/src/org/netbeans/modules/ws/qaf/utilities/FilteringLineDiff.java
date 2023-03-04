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
package org.netbeans.modules.ws.qaf.utilities;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.diff.LineDiff;

/**
 *   Same as org.netbeans.junit.diff.LineDiff except of compareLines method.
 *
 * @author jungi
 */
public class FilteringLineDiff extends LineDiff {

    private static final Logger LOGGER = Logger.getLogger(FilteringLineDiff.class.getName());
    
    /**
     * Creates a new instance of FilteringLineDiff
     *
     */
    public FilteringLineDiff() {
        this(false, false);
    }

    public FilteringLineDiff(boolean ignoreCase) {
        this(ignoreCase, false);
    }

    public FilteringLineDiff(boolean ignoreCase, boolean ignoreEmptyLines) {
        super(ignoreCase, ignoreEmptyLines);
    }

    /**
     *  Lines beginning with " * Created ", " * @author " or "import " and empty
     * lines are treated equals. One can check import statements by building
     * a project.
     *
     * @param l1 first line to compare
     * @param l2 second line to compare
     * @return true if lines equal
     */
    @Override
    protected boolean compareLines(String l1, String l2) {
        if (super.compareLines(l1, l2)) {
            return true;
        }
        if (((l1.indexOf(" * Created ") == 0) && (l2.indexOf(" * Created ") == 0))
                || ((l1.indexOf(" * @author ") == 0) && (l2.indexOf(" * @author ") == 0))
                || ((l1.indexOf("Created-By: ") == 0) && (l2.indexOf("Created-By: ") == 0))
                || ((l1.indexOf("import ") == 0) && (l2.indexOf("import ") == 0))
                ) {
            return true;
        }
        //we're not interested in changes in whitespaces, only content is important
        if (super.compareLines(l1.trim(), l2.trim())) {
            return true;
        }
        //WA: there's some strange random issue which causes that some
        //"randomly" chosen types are used with FQN
        String pkg = "o.n.m.ws.qaf.rest.crud.service.";//NOI18N
        if (l1.replaceAll(pkg, "").equals(l2.replaceAll(pkg, ""))) {
            LOGGER.log(Level.WARNING, "skiping \"{0}\" and \"{1}\"", new Object[]{l1, l2}); //NOI18N
            return true;
        }
        pkg = "o.n.m.ws.qaf.rest.crud.converter.";//NOI18N
        if (l1.replaceAll(pkg, "").equals(l2.replaceAll(pkg, ""))) {
            LOGGER.log(Level.WARNING, "skiping \"{0}\" and \"{1}\"", new Object[]{l1, l2}); //NOI18N
            return true;
        }
        pkg = "org.codehaus.jettison.json.";//NOI18N
        if (l1.replaceAll(pkg, "").equals(l2.replaceAll(pkg, "")) || l1.startsWith("import " + pkg) || l2.startsWith("import " + pkg)) {
            LOGGER.log(Level.WARNING, "skiping \"{0}\" and \"{1}\"", new Object[]{l1, l2}); //NOI18N
            return true;
        }
        //to avoid having two sets of golden files (for ant/maven based projects)
        if (l1.contains("private static String DEFAULT_PU = ")) { //NOI18N
            LOGGER.log(Level.WARNING, "skiping \"{0}\" and \"{1}\"", new Object[]{l1, l2}); //NOI18N
            return l2.trim().startsWith("private static String DEFAULT_PU = "); //NOI18N
        }
        return false;
    }
}
