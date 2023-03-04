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
 * This represents one GC root. It has kind ({@link GCRoot#JNI_GLOBAL}, etc.) and also corresponding
 * {@link Instance}, which is actual GC root.
 * @author Tomas Hurka
 */
public interface GCRoot {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    /**
     * JNI global GC root kind.
     */
    public static final String JNI_GLOBAL = "JNI global"; // NOI18N

    /**
     * JNI local GC root kind.
     */
    public static final String JNI_LOCAL = "JNI local"; // NOI18N

    /**
     * Java frame GC root kind.
     */
    public static final String JAVA_FRAME = "Java frame"; // NOI18N

    /**
     * Native stack GC root kind.
     */
    public static final String NATIVE_STACK = "native stack"; // NOI18N

    /**
     * Sticky class GC root kind.
     */
    public static final String STICKY_CLASS = "sticky class"; // NOI18N

    /**
     * Thread block GC root kind.
     */
    public static final String THREAD_BLOCK = "thread block"; // NOI18N

    /**
     * Monitor used GC root kind.
     */
    public static final String MONITOR_USED = "monitor used"; // NOI18N

    /**
     * Thread object GC root kind.
     */
    public static final String THREAD_OBJECT = "thread object"; // NOI18N

    /**
     * Unknown GC root kind.
     */
    public static final String UNKNOWN = "unknown"; // NOI18N

    /**
     * Interned string GC root kind.
     */
    public static final String INTERNED_STRING = "interned string"; // NOI18N

    /**
     * Finalizing GC root kind.
     */
    public static final String FINALIZING = "finalizing"; // NOI18N

    /**
     * Debugger GC root kind.
     */
    public static final String DEBUGGER = "debugger"; // NOI18N

    /**
     * Reference cleanup GC root kind.
     */
    public static final String REFERENCE_CLEANUP = "reference cleanup"; // NOI18N

    /**
     * VM internal GC root kind.
     */
    public static final String VM_INTERNAL = "VM internal"; // NOI18N

    /**
     * JNI monitor GC root kind.
     */
    public static final String JNI_MONITOR = "JNI monitor"; // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * returns corresponding {@link Instance}, which is GC root.
     * <br>
     * Speed:normal
     * @return GC root instance
     */
    Instance getInstance();

    /**
     * returns kind of this GC root.
     * <br>
     * Speed:fast
     * @return human readable GC root kind.
     */
    String getKind();
}
