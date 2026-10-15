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
package org.netbeans.modules.extbrowser.impl;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.W32APIOptions;

public abstract class ShlwapiUtil {

    /**
     * Searches for and retrieves a file or protocol association-related string
     * from the registry.
     *
     * @param flags The flags that can be used to control the search. It can be
     * any combination of ASSOCF values, except that only one ASSOCF_INIT value
     * can be included.
     * @param str The ASSOCSTR value that specifies the type of string that is
     * to be returned.
     * @param pszAssoc A pointer to a null-terminated string that is used to
     * determine the root key. The following four types of strings can be used:
     * <dl>
     * <dt>File name extension</dt>
     * <dd>A file name extension, such as .txt.</dd>
     * <dt>CLSID</dt>
     * <dd>A CLSID GUID in the standard "{GUID}" format.</dd>
     * <dt>ProgID</dt>
     * <dd>An application's ProgID, such as Word.Document.8.</dd>
     * <dt>Executable name</dt>
     * <dd>The name of an application's .exe file. The
     * {@link #ASSOCF_OPEN_BYEXENAME} flag must be set in flags.</dd>
     * </dl>
     * @param pszExtra An optional null-terminated string with additional
     * information about the location of the string. It is typically set to a
     * Shell verb such as open. Set this parameter to NULL if it is not used.
     * @param pszout Pointer to a null-terminated string that, when this
     * function returns successfully, receives the requested string. Set this
     * parameter to NULL to retrieve the required buffer size.
     * @param pcchOut A pointer to a value that, when calling the function, is
     * set to the number of characters in the pszOut buffer. When the function
     * returns successfully, the value is set to the number of characters
     * actually placed in the buffer.
     *
     * <p>
     * If the ASSOCF_NOTRUNCATE flag is set in flags and the buffer specified in
     * pszOut is too small, the function returns E_POINTER and the value is set
     * to the required size of the buffer.</p>
     * <p>
     * If pszOut is NULL, the function returns S_FALSE and pcchOut points to the
     * required size, in characters, of the buffer.</p>
     * @return
     */
    public static String AssocQueryString(int flags, int str, String pszAssoc, String pszExtra) {
        int internalFlags = flags | Shlwapi.ASSOCF_NOTRUNCATE;
        DWORDByReference pcchOut = new DWORDByReference();
        HRESULT result = Shlwapi.INSTANCE.AssocQueryString(internalFlags, str, pszAssoc, pszExtra, null, pcchOut);

        if(! (WinError.S_OK.equals(result) || WinError.S_FALSE.equals(result))) {
            throw new Win32Exception(result);
        }

        boolean wideApi = W32APIOptions.DEFAULT_OPTIONS == W32APIOptions.UNICODE_OPTIONS;

        Memory resultBuffer = new Memory((pcchOut.getValue().intValue() + 1) * (wideApi ? 2 : 1));

        result = Shlwapi.INSTANCE.AssocQueryString(internalFlags, str, pszAssoc, pszExtra, resultBuffer, pcchOut);

        if(! WinError.S_OK.equals(result)) {
            throw new Win32Exception(result);
        }

        if(wideApi) {
            return resultBuffer.getWideString(0);
        } else {
            return resultBuffer.getString(0);
        }
    }
}
