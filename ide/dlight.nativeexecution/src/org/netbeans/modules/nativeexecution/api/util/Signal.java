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
package org.netbeans.modules.nativeexecution.api.util;

/**
 *
 * @author ak119685
 */
public enum Signal {
    // IDs are taken from signal_iso.h on Solaris

    NULL(0),
    SIGHUP(1),
    SIGINT(2),
    SIGQUIT(3),
    SIGILL(4),
    SIGTRAP(5),
    SIGIOT(6),
    SIGABRT(6),
    SIGEMT(7),
    SIGFPE(8),
    SIGKILL(9),
    SIGBUS(10),
    SIGSEGV(11),
    SIGSYS(12),
    SIGPIPE(13),
    SIGALRM(14),
    SIGTERM(15),
    SIGUSR1(16),
    SIGUSR2(17),
    SIGCLD(18),
    SIGCHLD(18),
    SIGPWR(19),
    SIGWINCH(20),
    SIGURG(21),
    SIGPOLL(22),
    SIGIO(22),
    SIGSTOP(23),
    SIGTSTP(24),
    SIGCONT(25),
    SIGTTIN(26),
    SIGTTOU(27),
    SIGVTALRM(28),
    SIGPROF(29),
    SIGXCPU(30),
    SIGXFSZ(31),
    SIGWAITING(32),
    SIGLWP(33),
    SIGFREEZE(34),
    SIGTHAW(35),
    SIGCANCEL(36),
    SIGLOST(37),
    SIGXRES(38),
    SIGJVM1(39),
    SIGJVM2(40);
    private final int id;

    private Signal(int id) {
        this.id = id;
    }

    /*
     * Signal ID is system dependent and could differ from system to system;
     * It's better to use signal names (as NbKillUtility does)
     *
     * package-visible
     */
    int getID() {
        return id;
    }
    
    public static Signal valueOf(int id) {
        for (Signal signal : Signal.values()) {
            if (signal.getID() == id) {
                return signal;
            }
        }
        return NULL;
    }
}
