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

package org.netbeans.lib.terminalemulator;

import java.lang.reflect.Modifier;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.Term;

/**
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TermTest extends NbTestCase {

    public TermTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(TermTest.class));
    }
    
    /** This test assures compatibility with Jelly library.
     * Please contact QA or any JellyTools developer in case of failure.
     */    
    public void testJellyCompatibility() {
        try {
            assertTrue("Term class is public", Modifier.isPublic(Term.class.getModifiers()));
            assertTrue("Coord class is public", Modifier.isPublic(Coord.class.getModifiers()));
            Term term = new Term();
            term.getRowText(0);
            term.textWithin(Coord.make(0,0),Coord.make(0,0));
            term.getCursorRow();
            term.flush();
            Class.forName("org.netbeans.lib.terminalemulator.Screen");
        } catch (Exception e) {
            throw new AssertionFailedErrorException("JellyTools compatibility conflict, please contact QA or any JellyTools developer.", e);
        }
    }
    
    
}
