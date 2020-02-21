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

import org.netbeans.modules.terminal.api.ui.IOTopComponent;

/**
 *
 */
public final class TermComponentFactory {
    public static final int ACTIVE = 1 << 0;
    public static final int PACKET_MODE = 1 << 1;
    public static final int RAW_PTY = 1 << 2;
    public static final int PTY = 1 << 3;

    /* package */ static TermComponent createNewTermComponent(IOTopComponent owner, int flags) {
	return new NewTermComponent(owner, flags);
    }

    public static boolean isPty(int flags) {
	return (flags & TermComponentFactory.PTY) == TermComponentFactory.PTY;
    }

    public static boolean isRaw(int flags) {
	return (flags & TermComponentFactory.RAW_PTY) == TermComponentFactory.RAW_PTY;
    }

    public static boolean isActive(int flags) {
	return (flags & TermComponentFactory.ACTIVE) == TermComponentFactory.ACTIVE;
    }

    public static boolean isPacketMode(int flags) {
	return (flags & TermComponentFactory.PACKET_MODE) == TermComponentFactory.PACKET_MODE;
    }

    public static void ckFlags(int flags) {
	assert isPty(flags) || !isRaw(flags);		// raw only if pty
	assert isPty(flags) || ! isPacketMode(flags);	// packet only if pty
	assert ! isPty(flags) || ! isActive(flags);	// active only if !pty
    }
}
