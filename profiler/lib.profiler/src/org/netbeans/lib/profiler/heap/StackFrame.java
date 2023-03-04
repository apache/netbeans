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

package org.netbeans.lib.profiler.heap;


/**
 *
 * @author Tomas Hurka
 */
class StackFrame extends HprofObject {
    
    static final int NO_LINE_INFO = 0;
    static final int UNKNOWN_LOCATION = -1;
    static final int COMPILED_METHOD = -2;
    static final int NATIVE_METHOD = -3;
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    
    private final StackFrameSegment stackFrameSegment;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------
    
    StackFrame(StackFrameSegment segment, long offset) {
        super(offset);
        stackFrameSegment = segment;
        assert getHprofBuffer().get(offset) == HprofHeap.STACK_FRAME;
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    long getStackFrameID() {
        return getHprofBuffer().getID(fileOffset + stackFrameSegment.stackFrameIDOffset);
    }
    
    String getMethodName() {
        return getStringByOffset(stackFrameSegment.methodIDOffset);
    }
    
    String getMethodSignature() {
        return getStringByOffset(stackFrameSegment.methodSignatureIDOffset);
    }
    
    String getSourceFile() {
        return getStringByOffset(stackFrameSegment.sourceIDOffset);
    }
    
    String getClassName() {
        int classSerial = getHprofBuffer().getInt(fileOffset + stackFrameSegment.classSerialNumberOffset);
        return stackFrameSegment.getClassNameBySerialNumber(classSerial);
    }
    
    int getLineNumber() {
        return getHprofBuffer().getInt(fileOffset + stackFrameSegment.lineNumberOffset);
    }
    
    private HprofByteBuffer getHprofBuffer() {
        return stackFrameSegment.hprofHeap.dumpBuffer;
    }
    
    private String getStringByOffset(long offset) {
        long stringID = getHprofBuffer().getID(fileOffset + offset);
        return stackFrameSegment.hprofHeap.getStringSegment().getStringByID(stringID);
    }
}
