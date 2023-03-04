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
class ThreadObjectHprofGCRoot extends HprofGCRoot implements ThreadObjectGCRoot {
    
    ThreadObjectHprofGCRoot(HprofGCRoots r, long offset) {
        super(r, offset);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public StackTraceElement[] getStackTrace() {
        int stackTraceSerialNumber = getStackTraceSerialNumber();
        
        if (stackTraceSerialNumber != 0) {
            StackTrace stackTrace = roots.heap.getStackTraceSegment().getStackTraceBySerialNumber(stackTraceSerialNumber);
            if (stackTrace != null) {
                StackFrame[] frames = stackTrace.getStackFrames();
                StackTraceElement[] stackElements = new StackTraceElement[frames.length];

                for (int i=0;i<frames.length;i++) {
                    StackFrame f = frames[i];
                    String className = f.getClassName();
                    String method = f.getMethodName();
                    String source = f.getSourceFile();
                    int number = f.getLineNumber();

                    if (number == StackFrame.NATIVE_METHOD) {
                        number = -2;
                    } else if (number == StackFrame.NO_LINE_INFO || number == StackFrame.UNKNOWN_LOCATION) {
                        number = -1;
                    }
                    stackElements[i] = new StackTraceElement(className,method,source,number);
                }
                return stackElements;
            }
        }
        return null;
    }
    
    int getThreadSerialNumber() {
        return getHprofBuffer().getInt(fileOffset + 1 + getHprofBuffer().getIDSize());
    }

    private int getStackTraceSerialNumber() {
        return getHprofBuffer().getInt(fileOffset + 1 + getHprofBuffer().getIDSize() + 4);
    }    

}
