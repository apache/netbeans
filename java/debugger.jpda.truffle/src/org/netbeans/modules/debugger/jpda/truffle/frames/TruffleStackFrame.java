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

package org.netbeans.modules.debugger.jpda.truffle.frames;

import com.sun.jdi.StringReference;
import java.io.InvalidObjectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.truffle.LanguageName;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleScope;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public final class TruffleStackFrame {

    private static final Logger LOG = Logger.getLogger(TruffleStackFrame.class.getName());
    
    private final JPDADebugger debugger;
    private final JPDAThread thread;
    private final int depth;
    private final ObjectVariable frameInstance;
    private final String methodName;
    private final LanguageName language;
    private final String sourceLocation;
    
    private final int    sourceId;
    private final String sourceName;
    private final String sourcePath;
    private final URI    sourceURI;
    private final String sourceSection;
    private final StringReference codeRef;
    private TruffleScope[] scopes;
    private final ObjectVariable thisObject;
    private final boolean isInternal;
    
    public TruffleStackFrame(JPDADebugger debugger, JPDAThread thread, int depth,
                             ObjectVariable frameInstance,
                             String frameDefinition, StringReference codeRef,
                             TruffleScope[] scopes, ObjectVariable thisObject,
                             boolean includeInternal) {
        if (LOG.isLoggable(Level.FINE)) {
            try {
                LOG.fine("new TruffleStackFrame("+depth+", "+
                         frameInstance.getToStringValue()+" of type "+frameInstance.getClassType().getName()+
                         ", "+frameDefinition+", vars = "+Arrays.toString(scopes)+
                         ", "+thisObject+")");
            } catch (InvalidExpressionException iex) {
                LOG.log(Level.FINE, iex.getMessage(), iex);
            }
        }
        this.debugger = debugger;
        this.thread = thread;
        this.depth = depth;
        this.frameInstance = frameInstance;
        boolean internalFrame = includeInternal;
        try {
            int i1 = 0;
            int i2 = frameDefinition.indexOf('\n');
            methodName = frameDefinition.substring(i1, i2);
            i1 = i2 + 1;
            i2 = frameDefinition.indexOf('\n', i1);
            language = LanguageName.parse(frameDefinition.substring(i1, i2));
            i1 = i2 + 1;
            i2 = frameDefinition.indexOf('\n', i1);
            sourceLocation = frameDefinition.substring(i1, i2);
            i1 = i2 + 1;
            i2 = frameDefinition.indexOf('\n', i1);
            sourceId = Integer.parseInt(frameDefinition.substring(i1, i2));
            i1 = i2 + 1;
            i2 = frameDefinition.indexOf('\n', i1);
            sourceName = frameDefinition.substring(i1, i2);
            i1 = i2 + 1;
            i2 = frameDefinition.indexOf('\n', i1);
            sourcePath = frameDefinition.substring(i1, i2);
            i1 = i2 + 1;
            i2 = frameDefinition.indexOf('\n', i1);
            try {
                sourceURI = new URI(frameDefinition.substring(i1, i2));
            } catch (URISyntaxException usex) {
                throw new IllegalStateException("Bad URI: "+frameDefinition.substring(i1, i2), usex);
            }
            i1 = i2 + 1;
            if (includeInternal) {
                i2 = frameDefinition.indexOf('\n', i1);
                sourceSection = frameDefinition.substring(i1, i2);
                i1 = i2 + 1;
                internalFrame = Boolean.valueOf(frameDefinition.substring(i1));
            } else {
                sourceSection = frameDefinition.substring(i1);
            }
        } catch (IndexOutOfBoundsException ioob) {
            throw new IllegalStateException("frameDefinition='"+frameDefinition+"'", ioob);
        }
        this.codeRef = codeRef;
        this.scopes = scopes;
        this.thisObject = thisObject;
        this.isInternal = internalFrame;
    }
    
    public final JPDADebugger getDebugger() {
        return debugger;
    }
    
    public final JPDAThread getThread() {
        return thread;
    }
    
    public final int getDepth() {
        return depth;
    }
    
    public String getMethodName() {
        return methodName;
    }

    public LanguageName getLanguage() {
        return language;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getDisplayName() {
        if (!methodName.isEmpty()) {
            return methodName + " ("+sourceLocation+")";
        } else {
            return sourceLocation;
        }
    }
    
    public SourcePosition getSourcePosition() {
        Source src = Source.getExistingSource(debugger, sourceId);
        if (src == null) {
            src = Source.getSource(debugger, sourceId, sourceName, sourcePath, sourceURI, codeRef);
        }
        SourcePosition sp = new SourcePosition(debugger, sourceId, src, sourceSection);
        return sp;
    }
    
    public ObjectVariable getStackFrameInstance() {
        return frameInstance;// also is: (ObjectVariable) stackTrace.getFields(0, Integer.MAX_VALUE)[depth - 1];
    }
    
    public TruffleScope[] getScopes() {
        if (scopes == null) {
            scopes = TruffleAccess.createFrameScopes(debugger, /*suspendedInfo,*/ getStackFrameInstance());
        }
        return scopes;
    }
    
    public void popToHere() {
        if (depth > 0) {
            boolean unwindScheduled = TruffleAccess.unwind(debugger, thread, depth - 1);
            if (unwindScheduled) {
                CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(thread);
                try {
                    currentPCInfo.getStepCommandVar().setFromMirrorObject(-1);
                    thread.resume();
                } catch (InvalidObjectException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    public ObjectVariable getThis() {
        return thisObject;
    }
    
    public boolean isInternal() {
        return isInternal;
    }
    
}
