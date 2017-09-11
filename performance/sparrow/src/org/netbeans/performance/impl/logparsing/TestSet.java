/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002, 2016 Oracle and/or its affiliates. All rights reserved.
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
