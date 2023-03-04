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
