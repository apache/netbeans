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
/*
 * TestSet.java
 *
 * Created on October 8, 2002, 12:40 PM
 */

package org.netbeans.performance.impl.logparsing;
import java.util.*;
import org.netbeans.performance.spi.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
/** Defines a particular type of test, such as tests of
 * garbage collection, and contains a method for
 * building the required data structures to represent all the
 * data for a run.
 *
 * @author  Tim Boudreau
 */
public abstract class TestSet extends FolderAggregation {
    /**String used by master logs built by the ant script to
     * separate test sets. */
    public static final String NB_TEST_SET_SEPARATOR="%%%";
    /**String used by master logs built by the ant script to
     * separate elements of per-test-run data. */
    public static final String NB_TEST_DATA_SEPARATOR="^^^";

    /**Create a new instance of TestSet.
     */
    protected TestSet (String name) {
        super (name);
    }

    /**Create test run data (parse log files & build aggregations of objects
     * from them, etc).  The default implementation knows the standard
     * separators for data in the run script and calls the abstract
     * createElementForData method with the name-value pairs from the
     * master log file.   */
    public void createRun (FolderAggregation ada, String runinfo) throws DataNotFoundException {
        for (StringTokenizer sk = new StringTokenizer (runinfo, NB_TEST_DATA_SEPARATOR); sk.hasMoreElements();) {
            String nv = sk.nextToken();
            if (nv.indexOf(":") != -1) {
                String[] nameVal = Utils.splitStringInTwo(nv,":");
                LogElement[] newElements = createElementsForData(nameVal[0],nameVal[1]);
                if (newElements != null) {
                    for (int i=0; i < newElements.length; i++) {
                        ada.addElement (newElements[i]);
                    }
                }
            }
        }
    }

    /**Given a unique test-type, create an instance of the appropriate TestSet
     * instance (one that knows how to properly assemble the data objects for
     * it).
     */
    public static final TestSet createRegisteredTestSet (String setname) {
        //XXX replace with registeration code and dynamic class lookup
        //so extenders can register their own types of test set 
        return new GcTestSet(setname);
    }
    /**Create an appropriate LogElement object for a given name value pair from
     * the master run log.  For example, create a wrapper for a log file.
     * Called by createRun().  Can return null, in which case nothing will be
     * added to the run object for the passed name. 
     */
    public abstract LogElement[] createElementsForData (String name, String value) throws BuildException;
    
}
