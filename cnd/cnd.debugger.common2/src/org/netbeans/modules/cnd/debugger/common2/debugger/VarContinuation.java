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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import javax.swing.SwingUtilities;

/**
 * Continuation to help fetch dbx shell variable values asynchronously. 
 */

public abstract class VarContinuation implements Runnable {
    private String name;
    private String result;

    public VarContinuation(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    /**
     * Override this method with the code to be executed when we receive
     * the value.
     */
    @Override
    public abstract void run();

    protected String getResult() {
	return result;
    }

    /**
     * Called from NativeDebuggerImpl.VarContinuations
     */
    void run(String result) {
	this.result = result;

	// Use invokeLater because if we bring up a dialog as a result 
	// of this then we'll block further glue messages.

	// SHOULD make it be parameteric instead of always doing this?
	SwingUtilities.invokeLater (new Runnable() {
            @Override
	    public void run() {
		VarContinuation.this.run();
	    }
	} );
    }

    void print() {
    }
}
