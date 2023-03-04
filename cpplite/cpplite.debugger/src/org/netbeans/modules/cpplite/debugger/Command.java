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
package org.netbeans.modules.cpplite.debugger;

import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MICommand;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIUserInteraction;

class Command extends MICommand {

    private static final AtomicInteger LAST_TOKEN = new AtomicInteger(1);

    public Command(String command) {
        super(LAST_TOKEN.incrementAndGet(), command);
    }

    @Override
    protected void onDone(MIRecord record) {
    }

    @Override
    protected void onRunning(MIRecord record) {
    }

    @Override
    protected void onError(MIRecord record) {
    }

    @Override
    protected void onExit(MIRecord record) {
    }

    @Override
    protected void onStopped(MIRecord record) {
    }

    @Override
    protected void onOther(MIRecord record) {
    }

    @Override
    protected void onUserInteraction(MIUserInteraction ui) {
    }

}
