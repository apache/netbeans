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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.debug.DebugStackFrame;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Collects stack frame information.
 * 
 * @author martin
 */
final class FrameInfo {

    final DebugStackFrame frame;  // the top frame instance
    final DebugStackFrame[] stackTrace; // All but the top frame
    final String topFrame;
    final Object[] topVariables;
    // TODO: final Object[] thisObjects;

    FrameInfo(DebugStackFrame topStackFrame, Iterable<DebugStackFrame> stackFrames, boolean supportsJavaFrames) {
        SourceSection topSS = topStackFrame.getSourceSection();
        SourcePosition position = new SourcePosition(topSS, topStackFrame.getLanguage());
        ArrayList<DebugStackFrame> stackFramesArray = new ArrayList<>();
        for (DebugStackFrame sf : stackFrames) {
            if (sf == topStackFrame) {
                continue;
            }
            SourceSection ss = sf.getSourceSection();
            // Ignore frames without sources:
            boolean isHost = supportsJavaFrames && isHost(sf);
            if (!isHost && (ss == null || ss.getSource() == null)) {
                continue;
            }
            stackFramesArray.add(sf);
        }
        frame = topStackFrame;
        stackTrace = stackFramesArray.toArray(new DebugStackFrame[0]);
        LanguageInfo sfLang = topStackFrame.getLanguage();
        boolean isHost = supportsJavaFrames && isHost(topStackFrame);
        topFrame = topStackFrame.getName() + "\n" + isHost + "\n" +
                   ((sfLang != null) ? sfLang.getId() + " " + sfLang.getName() : "") + "\n" +
                   DebuggerVisualizer.getSourceLocation(topStackFrame, isHost) + "\n" +
                   position.id + "\n" + position.name + "\n" + position.path + "\n" +
                   position.hostClassName + "\n" + position.hostMethodName + "\n" +
                   position.uri + "\n" + position.mimeType + "\n" + position.sourceSection + "\n" +
                   isInternal(topStackFrame);
        topVariables = JPDATruffleAccessor.getVariables(topStackFrame);
    }
    
    /** Calls DebugStackFrame.isInternal() with workarounds for NPEs */
    static boolean isInternal(DebugStackFrame sf) {
        boolean isInternal = false;
        try {
            isInternal = sf.isInternal();
        } catch (Exception ex) {
            LangErrors.exception("Frame "+sf.getName()+" .isInternal()", ex);
            //System.err.println("Is Internal blew up for "+sf+", name = "+sf.getName()+", source = "+DebuggerVisualizer.getSourceLocation(sf.getSourceSection()));
            //System.err.println("  source section = "+sf.getSourceSection());
            try {
                Method findCurrentRootMethod = DebugStackFrame.class.getDeclaredMethod("findCurrentRoot");
                findCurrentRootMethod.setAccessible(true);
                RootNode rn = (RootNode) findCurrentRootMethod.invoke(sf);
                //System.err.println("  root node = "+rn);
                //System.err.println("  source section = "+rn.getSourceSection());
                isInternal = rn.getSourceSection() == null;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                     NoSuchMethodException | SecurityException ex2) {
                LangErrors.exception("Frame "+sf.getName()+" findCurrentRoot() invocation", ex2);
            }
        }
        return isInternal;
    }
    
    static boolean isHost(DebugStackFrame sf) {
        try {
            Method isHostMethod = DebugStackFrame.class.getMethod("isHost");
            return (Boolean) isHostMethod.invoke(sf);
        } catch (Exception ex) {
            return false;
        }
    }

    static StackTraceElement getHostTraceElement(DebugStackFrame sf) {
        try {
            Method getHostTraceElementMethod = DebugStackFrame.class.getMethod("getHostTraceElement");
            return (StackTraceElement) getHostTraceElementMethod.invoke(sf);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
