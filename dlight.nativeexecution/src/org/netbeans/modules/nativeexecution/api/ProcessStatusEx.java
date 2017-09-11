/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api;

import java.util.concurrent.TimeUnit;
import org.netbeans.modules.nativeexecution.ProcessStatusAccessor;

/**
 *
 * @author Andrew
 */
public final class ProcessStatusEx {

    static {
        ProcessStatusAccessor.setDefault(new Accessor());
    }
    private final int exitCode;
    private final String termSignal;
    private final boolean ifCoreDump;
    private final long rtime;
    private final long utime;
    private final long stime;
    private final long maxRSS;

    private ProcessStatusEx(String[] data) {
        int _exitCode = -1;
        String _termSignal = null;
        boolean _ifCoreDump = false;
        long _rtime = 0;
        long _utime = 0;
        long _stime = 0;
        long _maxRSS = 0;
        int pos;

        String param, value;

        for (String line : data) {
            pos = line.indexOf(':');
            if (pos < 0) {
                continue;
            }
            param = line.substring(0, pos);
            value = line.substring(pos + 1).trim();

            if ("RC".equals(param)) { // NOI18N
                _exitCode = Integer.parseInt(value);
            } else if ("SIG".equals(param)) { // NOI18N
                _termSignal = "-".equals(value) ? null : value; // NOI18N
            } else if ("CORE".equals(param)) { // NOI18N
                _ifCoreDump = "1".equals(value); // NOI18N
            } else if ("RTIME".equals(param)) { // NOI18N
                _rtime = Long.parseLong(value);
            } else if ("STIME".equals(param)) { // NOI18N
                _stime = Long.parseLong(value);
            } else if ("UTIME".equals(param)) { // NOI18N
                _utime = Long.parseLong(value);
            } else if ("MAXRSS".equals(param)) { // NOI18N
                _maxRSS = Long.parseLong(value);
            }
        }

        exitCode = _exitCode;
        termSignal = _termSignal;
        ifCoreDump = _ifCoreDump;
        rtime = _rtime;
        stime = _stime;
        utime = _utime;
        maxRSS = _maxRSS;
    }

    public int getExitCode() {
        return exitCode;
    }

    public long realTime(TimeUnit unit) {
        return unit.convert(rtime, TimeUnit.MILLISECONDS);
    }

    public long usrTime(TimeUnit unit) {
        return unit.convert(utime, TimeUnit.MILLISECONDS);
    }

    public long sysTime(TimeUnit unit) {
        return unit.convert(stime, TimeUnit.MILLISECONDS);
    }

    public long maxRSS() {
        return maxRSS;
    }

    public boolean ifCoreDump() {
        return ifCoreDump;
    }

    public boolean ifSignalled() {
        return termSignal != null;
    }

    public boolean ifExited() {
        return !ifSignalled();
    }

    public String termSignal() {
        return termSignal;
    }

    private static class Accessor extends ProcessStatusAccessor {

        @Override
        public ProcessStatusEx create(String[] data) {
            return new ProcessStatusEx(data);
        }
    }
}
