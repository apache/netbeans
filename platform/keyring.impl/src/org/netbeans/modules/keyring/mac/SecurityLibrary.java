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

package org.netbeans.modules.keyring.mac;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * @see <a href="http://developer.apple.com/mac/library/DOCUMENTATION/Security/Reference/keychainservices/Reference/reference.html">Security Framework Reference</a>
 */
public interface SecurityLibrary extends Library {

    SecurityLibrary LIBRARY = Native.load("Security", SecurityLibrary.class);

    int SecKeychainAddGenericPassword(
            Pointer keychain,
            int serviceNameLength,
            byte[] serviceName,
            int accountNameLength,
            byte[] accountName,
            int passwordLength,
            byte[] passwordData,
            Pointer itemRef
            );

    int SecKeychainItemModifyContent(
            Pointer/*SecKeychainItemRef*/ itemRef,
            Pointer/*SecKeychainAttributeList**/ attrList,
            int length,
            byte[] data
    );

    int SecKeychainFindGenericPassword(
            Pointer keychainOrArray,
            int serviceNameLength,
            byte[] serviceName,
            int accountNameLength,
            byte[] accountName,
            int[] passwordLength,
            Pointer[] passwordData,
            Pointer/*SecKeychainItemRef*/[] itemRef
            );

    int SecKeychainItemDelete(
            Pointer itemRef
            );

    Pointer/*CFString*/ SecCopyErrorMessageString(
            int status,
            Pointer reserved
            );

    // http://developer.apple.com/library/mac/#documentation/CoreFoundation/Reference/CFStringRef/Reference/reference.html

    long/*CFIndex*/ CFStringGetLength(
            Pointer/*CFStringRef*/ theString
    );

    char/*UniChar*/ CFStringGetCharacterAtIndex(
            Pointer/*CFStringRef*/ theString,
            long/*CFIndex*/ idx
    );

    void CFRelease(
            Pointer/*CFTypeRef*/ cf
    );

}
