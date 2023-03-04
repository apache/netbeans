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

package org.netbeans.test.j2ee.lib;

import org.netbeans.junit.diff.LineDiff;

/**
 *   Same as org.netbeans.junit.diff.LineDiff except of compareLines method.
 *
 * @author jungi
 */
public class FilteringLineDiff extends LineDiff {
    
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
     *  Lines beginning with " * Created " or " * @author " are treated equals.
     *
     * @param l1 first line to compare
     * @param l2 second line to compare
     * @return true if lines equal
     */
    @Override
    protected boolean compareLines(String l1,String l2) {
        if (super.compareLines(l1, l2)) {
            return true;
        }
        //we're not interested in changes in whitespaces, only content is important
        if (super.compareLines(l1.trim(), l2.trim())) {
            return true;
        }
        // ignore some specific comments
        if (((l1.indexOf(" * Created ") == 0) && (l2.indexOf(" * Created ") == 0))
                || ((l1.indexOf(" * @author ") == 0) && (l2.indexOf(" * @author ") == 0))
                || ((l1.indexOf("Created-By: ") == 0) && (l2.indexOf("Created-By: ") == 0))
                || (l1.contains("* To change this template") && l2.contains("* To change this template"))  // leading template comment (row 1)
                || (l1.contains(" the editor.") && l2.contains(" the editor."))) {  // leading template comment (row 2)
            return true;
        }
        return false;
    }
    
}
