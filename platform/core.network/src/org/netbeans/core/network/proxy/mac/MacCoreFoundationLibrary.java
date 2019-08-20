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
package org.netbeans.core.network.proxy.mac;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 *
 * @author lfischme
 */
public interface MacCoreFoundationLibrary extends Library {
    MacCoreFoundationLibrary LIBRARY = Native.loadLibrary("CoreFoundation", MacCoreFoundationLibrary.class);

    public boolean CFDictionaryGetValueIfPresent(Pointer dictionary, Pointer key, Pointer[] returnValue);

    public Pointer CFDictionaryGetValue(Pointer dictionary, Pointer key);

    public Pointer CFStringCreateWithCString(Pointer alloc, byte[] string, Pointer encoding);

    public long CFStringGetLength(Pointer cfStringRef);

    public long CFStringGetMaximumSizeForEncoding(long lenght, int encoding);

    public boolean CFStringGetCString(Pointer cfStringRef, Pointer buffer, long maxSize, int encoding);

    public Pointer CFNumberGetType(Pointer cfNumberRef);

    public boolean CFNumberGetValue(Pointer cfNumberRef, Pointer cfNumberType, Pointer value);

    public long CFNumberGetByteSize(Pointer cfNumberRef);
    
    public long CFArrayGetCount(Pointer cfArrayRef);
    
    public Pointer CFArrayGetValueAtIndex(Pointer cfArrayRef, Pointer cfIndex);
}
