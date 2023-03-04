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
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.actions.Action;

/** Test FavoritesOperator.
 *
 * @author Jiri Skrivanek
 */
public class FavoritesOperatorTest extends JellyTestCase {

    private static FavoritesOperator favoritesOper;
    public static String[] tests = {
        "testInvoke",
        "testTree",
        "testVerify"
    };

    public FavoritesOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        return createModuleTest(FavoritesOperatorTest.class, tests);
    }

    /** Print out test name. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
    }

    /**
     * Test of invoke method.
     */
    public void testInvoke() {
        FavoritesOperator.invoke().close();
        favoritesOper = FavoritesOperator.invoke();
    }

    /**
     * Test of tree method.
     */
    public void testTree() {
        // open another tab
        new Action("Window|Services", null).perform();
        // has to make favorites tab visible
        favoritesOper.tree();
    }

    /**
     * Test of verify method.
     */
    public void testVerify() {
        favoritesOper.verify();
        favoritesOper.close();
    }
}
