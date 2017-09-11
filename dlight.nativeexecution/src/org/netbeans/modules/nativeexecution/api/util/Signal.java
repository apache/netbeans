/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
