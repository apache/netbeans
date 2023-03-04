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

package org.netbeans.modules.autoupdate.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;
import org.openide.modules.Dependency;

/**
 *
 * @author Jirka Rechtacek
 */
public class DependencyAggregatorTest extends NbTestCase {

    public DependencyAggregatorTest (String testName) {
        super (testName);
    }

    private Dependency dep1;
    private Dependency dep2;
    
    @Before @Override
    public void setUp() {
        dep1 = Dependency.create (Dependency.TYPE_MODULE, "org.yourorghere.module.a > 1.0").iterator ().next ();
        dep2 = Dependency.create (Dependency.TYPE_MODULE, "org.yourorghere.module.a > 1.1").iterator ().next ();
    }

    @After @Override
    public void tearDown() {
    }

    /**
     * Test of getAggregator method, of class DependencyWrapper.
     */
    @Test
    public void testGetDependencyDecorator () {
        DependencyAggregator dec1 = DependencyAggregator.getAggregator (dep1);
        DependencyAggregator dec2 = DependencyAggregator.getAggregator (dep2);
        assertEquals ("Both DependencyDecorator are equal", dec2, dec1);
    }
}
