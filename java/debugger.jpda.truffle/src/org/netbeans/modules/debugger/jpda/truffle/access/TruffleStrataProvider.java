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

package org.netbeans.modules.debugger.jpda.truffle.access;

import java.util.Collections;
import java.util.List;

import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.spi.StrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Provider of an artificial stratum for GraalVM guest language script.
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession", types = { StrataProvider.class })
public class TruffleStrataProvider implements StrataProvider {
    
    public static final String TRUFFLE_STRATUM = "GraalVM_Script";
    
    private static final String TRUFFLE_ACCESS_CLASS = TruffleAccess.BASIC_CLASS_NAME;
    private static final String TRUFFLE_ACCESS_METHOD = "executionHalted";
    
    @Override
    public String getDefaultStratum(CallStackFrameImpl csf) {
        if (isInTruffleAccessPoint(csf)) {
            return TRUFFLE_STRATUM;
        }
        return null;
    }

    @Override
    public List<String> getAvailableStrata(CallStackFrameImpl csf) {
        if (isInTruffleAccessPoint(csf)) {
            return Collections.singletonList(TRUFFLE_STRATUM);
        }
        return null;
    }
    
    private boolean isInTruffleAccessPoint(CallStackFrameImpl csf) {
        return TRUFFLE_ACCESS_CLASS.equals(csf.getClassName()) &&
               TRUFFLE_ACCESS_METHOD.equals(csf.getMethodName());
    }

    @Override
    public int getStrataLineNumber(CallStackFrameImpl csf, String stratum) {
        if (TRUFFLE_STRATUM.equals(stratum) && isInTruffleAccessPoint(csf)) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentGuestPCInfo(csf.getThread());
            if (currentPCInfo != null) {
                SourcePosition sourcePosition = currentPCInfo.getSourcePosition();
                if (sourcePosition != null) {
                    return sourcePosition.getStartLine();
                } else {
                    return 0;
                }
            }
        }
        return csf.getLineNumber(stratum);
    }
    
}
