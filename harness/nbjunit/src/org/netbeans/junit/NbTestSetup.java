/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.junit;

import junit.framework.*;

/**
 * A Decorator to set up and tear down additional fixture state.
 * Subclass TestSetup and insert it into your tests when you want
 * to set up additional state once before the tests are run.
 */
public class NbTestSetup extends NbTestDecorator {

	public NbTestSetup(Test test) {
		super(test);
	}


	public void run(final TestResult result) {
		Protectable p= new Protectable() {
			public void protect() throws Exception {
				setUp();
				basicRun(result);
				tearDown();
			}
		};
		result.runProtected(this, p);
	}
        
	/**
	 * Sets up the fixture. Override to set up additional fixture
	 * state.
         * @throws Exception if an exception occurs
	 */
	protected void setUp() throws Exception {
	}
	/**
	 * Tears down the fixture. Override to tear down the additional
	 * fixture state.
         * @throws Exception if an exception occurs
	 */
	protected void tearDown() throws Exception {
	}
}
