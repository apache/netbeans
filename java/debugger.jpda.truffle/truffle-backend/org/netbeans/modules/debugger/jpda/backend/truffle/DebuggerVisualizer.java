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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.debug.DebugStackFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

import java.lang.reflect.Method;

/**
 *
 * @author Martin
 */
final class DebuggerVisualizer {
    
    private DebuggerVisualizer() {}
    
    static String getDisplayName(CallTarget ct) {
        if (ct instanceof RootCallTarget) {
            RootNode rn = ((RootCallTarget) ct).getRootNode();
            return getMethodName(rn);
        } else {
            //System.err.println("Unexpected CallTarget: "+ct.getClass());
            return ct.toString();
        }
    }
    
    static String getMethodName(RootNode rn) {
        return rn.getName();
    }
    
    /** &lt;File name&gt;:&lt;line number&gt; */
    static String getSourceLocation(DebugStackFrame sf, boolean isHost) {
        if (!isHost) {
            SourceSection ss = sf.getSourceSection();
            if (ss == null) {
                //System.err.println("No source section for node "+n);
                return "unknown";
            }
            return ss.getSource().getName() + ":" + ss.getStartLine();
        } else {
            try {
                Method getHostTraceElementMethod = DebugStackFrame.class.getMethod("getHostTraceElement");
                StackTraceElement ste = (StackTraceElement) getHostTraceElementMethod.invoke(sf);
                return ste.getFileName() + ":" + ste.getLineNumber();
            } catch (Exception ex) {
                LangErrors.exception("getHostTraceElement", ex);
                return null;
            }
        }
    }

}
