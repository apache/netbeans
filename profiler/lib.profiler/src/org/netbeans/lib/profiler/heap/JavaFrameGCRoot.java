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
 * This represents one Java Frame GC root. It has kind ({@link GCRoot#JAVA_FRAME}) and also corresponding
 * {@link Instance}, which is actual GC root and represent a local variable held on the stack. 
 * @author Tomas Hurka
 */
public interface JavaFrameGCRoot extends GCRoot {

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * returns Thread root GC object for the thread where this local variable is held.
     * <br>
     * speed:normal
     * @return {@link ThreadObjectGCRoot} for the corresponding thread. 
     */
    ThreadObjectGCRoot getThreadGCRoot();
    
    /**
     * frame number in stack trace.
     * <br>
     * Speed:fast
     * @return frame number in stack trace (-1 for empty)
     */
    int getFrameNumber();
}
