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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Shlwapi extends StdCallLibrary {
    Shlwapi INSTANCE = Native.load("Shlwapi", Shlwapi.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * do not remap clsids to progids
     */
    public static int ASSOCF_INIT_NOREMAPCLSID = 0x00000001;
    /**
     * executable is being passed in
     */
    public static int ASSOCF_INIT_BYEXENAME = 0x00000002;
    /**
     * executable is being passed in
     */
    public static int ASSOCF_OPEN_BYEXENAME = 0x00000002;
    /**
     * treat "*" as the BaseClass
     */
    public static int ASSOCF_INIT_DEFAULTTOSTAR = 0x00000004;
    /**
     * treat "Folder" as the BaseClass
     */
    public static int ASSOCF_INIT_DEFAULTTOFOLDER = 0x00000008;
    /**
     * dont use HKCU
     */
    public static int ASSOCF_NOUSERSETTINGS = 0x00000010;
    /**
     * dont truncate the return string
     */
    public static int ASSOCF_NOTRUNCATE = 0x00000020;
    /**
     * verify data is accurate (DISK HITS)
     */
    public static int ASSOCF_VERIFY = 0x00000040;
    /**
     * actually gets info about rundlls target if applicable
     */
    public static int ASSOCF_REMAPRUNDLL = 0x00000080;
    /**
     * attempt to fix errors if found
     */
    public static int ASSOCF_NOFIXUPS = 0x00000100;
    /**
     * dont recurse into the baseclass
     */
    public static int ASSOCF_IGNOREBASECLASS = 0x00000200;
    /**
     * dont use the "Unknown" progid
     */
    public static int ASSOCF_INIT_IGNOREUNKNOWN          = 0x00000400;

    /**
     * shell\verb\command string
     */
    public static int ASSOCSTR_COMMAND = 1;
    /**
     * the executable part of command string
     */
    public static int ASSOCSTR_EXECUTABLE = 2;
    /**
     * friendly name of the document type
     */
    public static int ASSOCSTR_FRIENDLYDOCNAME = 3;
    /**
     * friendly name of executable
     */
    public static int ASSOCSTR_FRIENDLYAPPNAME = 4;
    /**
     * noopen value
     */
    public static int ASSOCSTR_NOOPEN = 5;
    /**
     * query values under the shellnew key
     */
    public static int ASSOCSTR_SHELLNEWVALUE = 6;
    /**
     * template for DDE commands
     */
    public static int ASSOCSTR_DDECOMMAND = 7;
    /**
     * DDECOMMAND to use if just create a process
     */
    public static int ASSOCSTR_DDEIFEXEC = 8;
    /**
     * Application name in DDE broadcast
     */
    public static int ASSOCSTR_DDEAPPLICATION = 9;
    /**
     * Topic Name in DDE broadcast
     */
    public static int ASSOCSTR_DDETOPIC = 10;
    /**
     * info tip for an item, or list of properties to create info tip from
     */
    public static int ASSOCSTR_INFOTIP = 11;
    /**
     * same as ASSOCSTR_INFOTIP, except, this list contains only quickly
     * retrievable properties
     */
    public static int ASSOCSTR_QUICKTIP = 12;
    /**
     * similar to ASSOCSTR_INFOTIP - lists important properties for tileview
     */
    public static int ASSOCSTR_TILEINFO = 13;
    /**
     * MIME Content type
     */
    public static int ASSOCSTR_CONTENTTYPE = 14;
    /**
     * Default icon source
     */
    public static int ASSOCSTR_DEFAULTICON = 15;
    /**
     * Guid string pointing to the Shellex\Shellextensionhandler value.
     */
    public static int ASSOCSTR_SHELLEXTENSION = 16;
    /**
     * The CLSID of DropTarget
     */
    public static int ASSOCSTR_DROPTARGET = 17;
    /**
     * The CLSID of DelegateExecute
     */
    public static int ASSOCSTR_DELEGATEEXECUTE = 18;

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
    HRESULT AssocQueryString(int flags, int str, String pszAssoc, String pszExtra, Pointer pszout, WinDef.DWORDByReference pcchOut);
}
