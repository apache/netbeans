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

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public class PioPack extends InternalTerminalPack {
    private final TermComponent pio;

    public PioPack(TermComponent console, TermComponent pio, ExecutionEnvironment exEnv) {
        super(console, pio.getIO(), exEnv);
        this.pio = pio;
    }

    public TermComponent pio() {
	return pio;
    }

    @Override
    public void open() {
	pio.open();
	super.open();
    }

    @Override
    public void bringDown() {
	pio.bringDown();
	super.bringDown();
    }

    @Override
    public void bringUp() {
	pio.bringUp();
	super.bringUp();
    }

    @Override
    public void switchTo() {
	pio.switchTo();
	super.switchTo();
    }

    public static TermComponent makePio(int flags) {
	return TermComponentFactory.createNewTermComponent(PioTopComponent.findInstance(), flags);
    }
}
