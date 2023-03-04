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

package org.netbeans.installer.utils.system.windows;

import java.util.Map;
import java.util.Random;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.NativeException;

/**
 *
 * @author Dmitry Lipin
 * @author Kirill Sorokin
 */
public class WindowsRegistry {
    private int mode;
    private Boolean wow64process;
    /////////////////////////////////////////////////////////////////////////////
    // Instance
    
    // constructor //////////////////////////////////////////////////////////////
    /**
     *
     */
    public WindowsRegistry() {
        setMode(MODE_DEFAULT);        
    }
    
    // queries //////////////////////////////////////////////////////////////////////
    /**
     * Checks whether the specified key exists in the registry (can be read).
     *
     * @param section The section of the registry
     * @param key The specified key
     * @return <i>true</i> if the specified key exists (can be read), <i>false</i> otherwise
     */
    public boolean keyExists(int section, String key) throws NativeException {
        validateSection(section);
        validateKey(key);
        
        try {
            return checkKeyAccess0(mode,section, key, KEY_READ_LEVEL);
        } catch (UnsatisfiedLinkError e) {
            throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
        }
    }
    
    public boolean keyExists(int section, String parent, String child) throws NativeException {
        //validateSection(section);
        validateKey(parent);
        validateKeyName(child);
        validateParenthood(parent, child);
        
        try {
            return keyExists(section, parent + SEPARATOR + child);
        } catch (UnsatisfiedLinkError e) {
            throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
        }
    }
    
    /**
     * Checks whether the specified value exists in the registry.
     *
     * @param section The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return <i>true</i> if the specified value exists, <i>false</i> otherwise
     */
    public boolean valueExists(int section, String key, String name) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        validateValueName(name);
        
        if (keyExists(section, key)) {
            try {
                return valueExists0(mode,section, key, name);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot check for value existance - key does not exist");
        }
    }
    
    /**
     * Checks whether the specified value exists in the registry.
     *
     * @param section The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return <i>true</i> if the specified value exists, <i>false</i> otherwise
     */
    public boolean keyEmpty(int section, String key) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        
        if (keyExists(section, key)) {
            try {
                return keyEmpty0(mode,section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot check -- key does not exist");
        }
    }
    
    /**
     * Get the number of the subkeys of the specified key.
     *
     * @param section The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return -1.
     * <br>Otherwise return the number of subkeys
     */
    public int countSubKeys(int section, String key) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        
        if (keyExists(section, key)) {
            try {
                return countSubKeys0(mode,section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot count subkeys -- key does not exist");
        }
    }
    
    /** Get the number of the values of the specified key.
     * @param section The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return -1.
     * <br>Otherwise return the number of values
     */
    public int countValues(int section, String key) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        
        if (keyExists(section, key)) {
            try {
                return countValues0(mode,section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
            
        } else {
            throw new NativeException("Cannot count values -- key does not exist");
        }
    }
    
    public String[] getSubKeys(int section, String key) throws NativeException {
        String[] names   = getSubKeyNames(section, key);
        String[] subkeys = new String[names.length];
        
        for (int i = 0; i < names.length; i++) {
            subkeys[i] = constructKey(key, names[i]);
        }
        
        return subkeys;
    }
    
    /**
     * Get the array of subkey names of the specified key.
     *
     * @param section The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return <i>null</i>
     * <br>Otherwise return the array of subkey names
     */
    public String[] getSubKeyNames(int section, String key) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        
        if (keyExists(section, key)) {
            try {
                return getSubkeyNames0(mode,section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot get subkey names -- key does not exist");
        }
    }
    
    /** Get the array of values names of the specified key.
     * @param section The section of the registry
     * @param key The specified key
     * @return If the key doesn`t exist or can`t be accessed then return <i>null</i>
     * <br>Otherwise return the array of value names
     */
    public String[] getValueNames(int section, String key) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        
        if (keyExists(section, key)) {
            try {
                return getValueNames0(mode,section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot list value names -- key does not exist");
        }
    }
    
    /**
     * Returns the type of the value.
     *
     * @param section The section of the registry
     * @param key     The specified key
     * @param value   The specified value
     *
     * @return The possible values are:<br>
     *
     * <code>REG_NONE</code><br>
     * <code>REG_SZ</code><br>
     * <code>REG_EXPAND_SZ</code><br>
     * <code>REG_BINARY</code><br>
     * <code>REG_DWORD</code>=<code>REG_DWORD_LITTLE_ENDIAN</code><br>
     * <code>REG_DWORD_BIG_ENDIAN</code><br>
     * <code>REG_LINK</code><br>
     * <code>REG_MULTI_SZ</code><br>
     * <code>REG_RESOURCE_LIST</code><br>
     * <code>REG_FULL_RESOURCE_DESCRIPTOR</code><br>
     * <code>REG_RESOURCE_REQUIREMENTS_LIST</code><br>
     * <code>REG_QWORD</code>=<code>REG_QWORD_LITTLE_ENDIAN</code>
     */
    public int getValueType(int section, String key, String name) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        //validateValueName(name);
        
        if (keyExists(section, key)) {
            if (valueExists(section, key, name)) {
                try {
                    return getValueType0(mode,section, key, name);
                } catch (UnsatisfiedLinkError e) {
                    throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
                }
            } else {
                throw new NativeException("Cannot get value type -- value does not exist");
            }
        } else {
            throw new NativeException("Cannot get value type -- key does not exist");
        }
    }
    
    // key operations ///////////////////////////////////////////////////////////////
    /**
     * Create the new key in the registry.
     *
     * @param section The section of the registry
     * @param key The specified key
     * @return <i>true</i> if the key was successfully created,
     * <br> <i>false</i> otherwise
     */
    public void createKey(int section, String key) throws NativeException {
        createKey(section, getKeyParent(key), getKeyName(key));
    }
    
    /**
     * Create the new key in the registry.
     *
     * @param section The section of the registry
     * @param parent key The specified parent key
     * @param parent key The specified child key
     * @return <i>true</i> if the key was successfully created,
     * <br> <i>false</i> otherwise
     */
    public void createKey(int section, String parent, String child) throws NativeException {
        //validateSection(section);
        //validateKey(parent);
        //validateKeyName(child);
        //validateParenthood(parent, child);
        
        if (!keyExists(section, parent, child)) {
            if (!keyExists(section, parent)) {
                createKey(section, getKeyParent(parent), getKeyName(parent));
            }
            
            try {
                createKey0(mode,section, parent, child);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        }
    }
    
    /**
     * Delete the specified key exists in the registry. Note that if the key
     * contains subkeys then it would not be deleted.
     *
     * @param section The section of the registry
     * @param key The specified key
     * @return <i>true</i> if the specified key was deleted, <i>false</i> otherwise
     */
    public void deleteKey(int section, String key) throws NativeException {
        deleteKey(section, getKeyParent(key), getKeyName(key));
    }
    
    /**
     * Delete the specified key exists in the registry.
     *
     * @param section The section of the registry
     * @param parentKey The specified parent key
     * @param childKey The specified child key
     * @return <i>true</i> if the specified key was deleted, <i>false</i> otherwise
     */
    public void deleteKey(int section, String parent, String child) throws NativeException {
        //validateSection(section);
        //validateKey(parent);
        //validateKeyName(child);
        //validateParenthood(parent, child);
        
        if (keyExists(section, parent, child)) {
            try {
                deleteKey0(mode,section, parent, child);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        }
    }
    
    // value operations /////////////////////////////////////////////////////////////
    /**
     * Delete the specified value exists in the registry.
     *
     * @param section The section of the registry
     * @param key The specified key
     * @param value The specified value
     * @return <i>true</i> if the specified value was deleted, <i>false</i> otherwise
     */
    public void deleteValue(int section, String key, String name) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        //validateValueName(name);
        
        if (keyExists(section, key)) {
            if (valueExists(section, key, name)) {
                try {
                    deleteValue0(mode,section, key, name);
                } catch (UnsatisfiedLinkError e) {
                    throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
                }
            }
        } else {
            throw new NativeException("Cannot delete value -- key does not exist");
        }
    }
    
    /**
     *
     * @param section
     * @param key
     * @param name
     * @return
     */
    public String getStringValue(int section, String key, String name) throws NativeException {
        return getStringValue(section, key, name, false);
    }
    
    /** Get string value.
     * @param section The section of the registry
     * @param key The specified key
     * @param name The specified value
     * @param expandable
     *      If <code>expandable</code> is <i>true</i> and
     *      the type of the value is REG_EXPAND_SZ the value would be expanded
     * @return The value of the name, <i>null</i> in case of any error
     */
    public String getStringValue(int section, String key, String name, boolean expand) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        //validateValueName(name);
        
        if (keyExists(section, key)) {
            if (valueExists(section, key, name)) {
                try {
                    return getStringValue0(mode,section, key, name, expand);
                } catch (UnsatisfiedLinkError e) {
                    throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
                }
            } else {
                throw new NativeException("Cannot get string value -- value does not exist");
            }
        } else {
            throw new NativeException("Cannot get string value -- key does not exist");
        }
    }
    
    public void setStringValue(int section, String key, String name, Object value) throws NativeException {
        setStringValue(section, key, name, value.toString());
    }
    
    /**
     *
     * @param section
     * @param key
     * @param name
     * @param value
     */
    public void setStringValue(int section, String key, String name, String value) throws NativeException {
        setStringValue(section, key, name, value, false);
    }
    
    /** Set string value.
     * @param section The section of the registry
     * @param key The specified key
     * @param name The specified value
     * @param value The specified value of the <code>name</code>
     * @param expandable
     *      If <code>expandable</code> is <i>true</i> then the type would be
     *       <code>REG_EXPAND_SZ</code> or <code>REG_SZ</code> otherwise
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public void setStringValue(int section, String key, String name, String value, boolean expandable) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        validateValueName(name);
        validateStringValue(value);
        
        if (keyExists(section, key)) {
            try {
                setStringValue0(mode,section, key, name, value, expandable);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot set string value -- key does not exist");
        }
    }
    
    /** Get integer value.
     * @param section The section of the registry
     * @param key The specified key
     * @param name The specified value
     * @return The value of the name, <i>-1</i> in case of any error
     */
    public int get32BitValue(int section, String key, String name) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        //validateValueName(name);
        
        if (keyExists(section, key)) {
            if (valueExists(section, key, name)) {
                try {
                    return get32BitValue0(mode,section, key, name);
                } catch (UnsatisfiedLinkError e) {
                    throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
                }
            } else {
                throw new NativeException("Cannot get 32-bit value -- value does not exist");
            }
        } else {
            throw new NativeException("Cannot get 32-bit value -- key does not exist");
        }
    }
    
    /** Set REG_DWORD value.
     * @param section The section of the registry
     * @param key The specified key
     * @param name The specified value
     * @param value The specified value of the <code>name</code>
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public void set32BitValue(int section, String key, String name, int value) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        validateValueName(name);
        validate32BitValue(value);
        
        if (keyExists(section, key)) {
            try {
                set32BitValue0(mode,section, key, name, value);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot set 32-bit value -- key does not exist");
        }
    }
    
    /** Get the array of strings of the specified value
     * @param section The section of the registry
     * @param key The specified key
     * @param name The specified value
     * @return The multri-string value of the name, <i>null</i> in case of any error
     */
    public String[] getMultiStringValue(int section, String key, String name) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        //validateValueName(name);
        
        if (keyExists(section, key)) {
            if (valueExists(section, key, name)) {
                try {
                    return getMultiStringValue0(mode,section, key, name);
                } catch (UnsatisfiedLinkError e) {
                    throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
                }
            } else {
                throw new NativeException("Cannot get multistring value -- value does not exist");
            }
        } else {
            throw new NativeException("Cannot get multistring value -- key does not exist");
        }
    }
    
    /** Set REG_MULTI_SZ value.
     * @param section The section of the registry
     * @param key The specified key
     * @param name The specified value
     * @param value The specified value of the <code>name</code>
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public void setMultiStringValue(int section, String key, String name, String[] value) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        validateValueName(name);
        validateMultiStringValue(value);
        
        if (keyExists(section, key)) {
            try {
                setMultiStringValue0(mode,section, key, name, value);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot set multistring value -- key does not exist");
        }
    }
    
    /**
     * Get binary value.
     *
     * @param section The section of the registry
     * @param key The specified key
     * @param name The specified value
     * @return The binary value of the name, <i>null</i> in case of any error
     */
    public byte[] getBinaryValue(int section, String key, String name) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        //validateValueName(name);
        
        if (keyExists(section, key)) {
            if (valueExists(section, key, name)) {
                try {
                    return getBinaryValue0(mode,section, key, name);
                } catch (UnsatisfiedLinkError e) {
                    throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
                }
            } else {
                throw new NativeException("Cannot get binary value -- value does not exist");
            }
        } else {
            throw new NativeException("Cannot get binary value -- key does not exist");
        }
    }
    
    /** Set binary (REG_BINARY) value.
     * @param section The section of the registry
     * @param key The specified key
     * @param name The specified value
     * @param value The specified value of the <code>name</code>
     * @return <i>true</i> if the value was successfully set
     * <br> <i>false</i> otherwise
     */
    public void setBinaryValue(int section, String key, String name, byte[] value) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        validateValueName(name);
        validateBinaryValue(value);
        
        if (keyExists(section, key)) {
            try {
                setBinaryValue0(mode,section, key, name, value);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot set binary value -- key does not exist");
        }
    }
    
    /**
     * Set new value of REG_NONE type
     *
     * @param section The section of the registry
     * @param key The specified key
     * @param value The specified value
     */
    public void setNoneValue(int section, String key, String name, byte ... bytes) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        validateValueName(name);
        
        if (keyExists(section, key)) {
            try {
                setNoneValue0(mode,section, key, name, bytes);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
            }
        } else {
            throw new NativeException("Cannot access value -- key does not exist");
        }
    }
    
    public void setAdditionalValues(int section, String key, Map<String,Object> values) throws NativeException {
        LogManager.log("setting " + values.size() + " values");
        
        for (Map.Entry<String,Object> entry: values.entrySet()) {
            final Object value = entry.getValue();
            String name = entry.getKey();
            
            LogManager.log(name + " = " + value.toString());
            
            if (value instanceof Short) {
                LogManager.log("Type is short. Set REG_DWORD value");

                set32BitValue(section, key, name, ((Short) value).intValue());
            } else if (value instanceof Integer) {
                LogManager.log("Type is integer. Set REG_DWORD value");
                
                set32BitValue(section, key, name, (Integer) value);
            } else if (value instanceof Long) {
                LogManager.log("Type is long. Set REG_DWORD value");
                
                set32BitValue(section, key, name, ((Long) value).intValue());
            } else if (value instanceof byte[]) {
                LogManager.log("Type is byte[]. Set REG_BINARY value");
                
                setBinaryValue(section, key, name, (byte[]) value);
            } else if (value instanceof String[]) {
                LogManager.log("Type is String[]. Set REG_MULTI_SZ value");
                
                setMultiStringValue(section, key, name, (String[]) value);
            } else if (value instanceof String) {
                LogManager.log("Type is String. Set REG_SZ value");
                
                setStringValue(section, key, name, (String) value, false);
            } else {
                LogManager.log("Type can't be determined. Set REG_SZ value");
                
                setStringValue(section, key, name, value.toString(), false);
            }
        }
    }
    
    /**
     * Checks whether the specified key exists and can be modified in the registry.
     *
     * @param section The section of the registry
     * @param key The specified key
     * @return <i>true</i> if the specified key can exists and can be modified, <i>false</i> otherwise
     */
    public boolean canModifyKey(int section, String key) throws NativeException {
        //validateSection(section);
        //validateKey(key);
        try {
            if(keyExists(section,key)) {
                boolean check = checkKeyAccess0(mode,section, key, KEY_MODIFY_LEVEL);
                
                if(check) { 
                    // try to create/delete new sub key to be sure that we can modify the parent
                    // this will require in most cases of vista with UAC enabled
                    String randomKey = "rndkey" + new Random().nextLong();
                    try {
                        createKey0(mode,section, key, randomKey);
                        deleteKey0(mode,section, key, randomKey);
                    } catch (NativeException ex) {
                        check = false;
                    }
                }
                
                return check;
            } else {
                return canModifyKey(section,getKeyParent(key));
            }
        } catch (UnsatisfiedLinkError e) {
            throw new NativeException(ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING, e);
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public String constructKey(String parent, String child) {
        return parent + SEPARATOR + child;
    }
    
    /**
     *
     * @param key
     * @return
     */
    public String getKeyParent(String key) {
        String temp = key;
        
        // strip the trailing separators
        while (temp.endsWith(SEPARATOR)) {
            temp = temp.substring(0, temp.length() - 1);
        }
        
        int index = temp.lastIndexOf(SEPARATOR);
        if (index != -1) {
            return temp.substring(0, index);
        } else {
            return StringUtils.EMPTY_STRING;
        }
    }
    
    /**
     *
     * @param key
     * @return
     */
    public String getKeyName(String key) {
        String temp = key;
        
        // strip the trailing separators
        while (temp.endsWith(SEPARATOR)) {
            temp = temp.substring(0, temp.length() - 1);
        }
        
        int index = temp.lastIndexOf(SEPARATOR);
        if (index != -1) {
            return temp.substring(index + 1);
        } else {
            return temp;
        }
    }
    
    
    public void setMode(int m) {
        if(isModeSupported(m)) {            
            mode = m;
        }
    }
    
    public boolean isAlternativeModeSupported() {
        return (IsWow64Process() || System.getProperty("os.arch").equals("amd64"));                 
    }    
    
    public void setMode(Boolean modeChange) {
        setMode(modeBooleanToInteger(modeChange));
    }
    public boolean isModeSupported(int mode) {
        switch (mode) {
            case MODE_DEFAULT:
                return true;
            case MODE_64BIT:
            case MODE_32BIT:
                return isAlternativeModeSupported();
            default:
                return false;
        }        
    }    
    
    public int getMode() {        
        return mode;        
    }
    public Boolean isAlternativeMode() {        
        return modeIntegerToBoolean(mode);        
    }
    public boolean IsWow64Process() {
        if(wow64process == null) {
            wow64process = IsWow64Process0();
        }
        return wow64process;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private Boolean modeIntegerToBoolean(int im) {
        if(im==MODE_DEFAULT) {
            return null;
        } else if(im==MODE_32BIT) {
            return System.getProperty("os.arch").equals("amd64");
        } else if(im==MODE_64BIT) {
            return IsWow64Process();
        } else {
            return null;
        }
    }
    private int modeBooleanToInteger(Boolean bm) {
        if(bm == null) { 
            return MODE_DEFAULT;
        }
        //running 32-bit application in Windows x64
        if(IsWow64Process()) {
            return bm ? MODE_64BIT : MODE_32BIT;
        }
        //running 64-bit application in Windows x64
        if(System.getProperty("os.arch").equals("amd64")) {
            return !bm ? MODE_64BIT : MODE_32BIT;
        }
        return MODE_DEFAULT;
    }
    
    private void validateSection(int section) throws NativeException {
        if ((section < HKEY_CLASSES_ROOT) || (section > HKEY_PERFORMANCE_TEXT)) {
            throw new NativeException("Section \"" + section + "\" is " +
                    "invalid, should be between " + HKEY_CLASSES_ROOT + " " +
                    "and " + HKEY_PERFORMANCE_TEXT);
        }
    }
    
    private void validateKey(String key) throws NativeException {
        if (key == null) {
            throw new NativeException("Key cannot be null");
        }
    }
    
    private void validateKeyName(String name) throws NativeException {
        if (name == null) {
            throw new NativeException("Key name cannot be null");
        }
    }
    
    private void validateParenthood(String parent, String child) throws NativeException {
        if (parent.equals(child)) {
            throw new NativeException("Parent cannot be equal to child");
        }
    }
    
    private void validateValueName(String name) throws NativeException {
        if (name == null) {
            throw new NativeException("Value name cannot be null");
        }
    }
    
    private void validateStringValue(String value) throws NativeException {
        if (value == null) {
            throw new NativeException("String value cannot be null");
        }
    }
    
    private void validate32BitValue(int value) throws NativeException {
        // it cannot be wrong, but just in case
    }
    
    private void validateMultiStringValue(String[] value) throws NativeException {
        if (value == null) {
            throw new NativeException("Multistring value cannot be null");
        }
    }
    
    private void validateBinaryValue(byte[] value) throws NativeException {
        if (value == null) {
            throw new NativeException("Binary value cannot be null");
        }
    }
    
    // native declarations //////////////////////////////////////////////////////
    private native boolean keyExists0(int mode, int section, String key) throws NativeException;
    
    private native boolean valueExists0(int mode, int section, String key, String value) throws NativeException;
    
    private native boolean keyEmpty0(int mode, int section, String key) throws NativeException;
    
    private native int countSubKeys0(int mode, int section, String key) throws NativeException;
    
    private native int countValues0(int mode, int section, String key) throws NativeException;
    
    private native String[] getSubkeyNames0(int mode, int section, String key) throws NativeException;
    
    private native String[] getValueNames0(int mode, int section, String key) throws NativeException;
    
    private native int getValueType0(int mode, int section, String key, String value) throws NativeException;
    
    private native void createKey0(int mode, int section, String parent, String child) throws NativeException;
    
    private native void deleteKey0(int mode, int section, String parent, String child) throws NativeException;
    
    private native void deleteValue0(int mode, int section, String key, String value) throws NativeException;
    
    private native String getStringValue0(int mode, int section, String key, String name, boolean expand) throws NativeException;
    
    private native void setStringValue0(int mode, int section, String key, String name, String value, boolean expandable);
    
    private native int get32BitValue0(int mode, int section, String key, String name) throws NativeException;
    
    private native void set32BitValue0(int mode, int section, String key, String name, int value) throws NativeException;
    
    private native String[] getMultiStringValue0(int mode, int section, String key, String name) throws NativeException;
    
    private native void setMultiStringValue0(int mode, int section, String key, String name, String[] value) throws NativeException;
    
    private native byte[] getBinaryValue0(int mode, int section, String key, String name) throws NativeException;
    
    private native void setBinaryValue0(int mode, int section, String key, String name, byte[] value) throws NativeException;
    
    private native void setNoneValue0(int mode, int section, String key, String name, Object value) throws NativeException;
    
    private native boolean checkKeyAccess0(int mode, int section, String key, int level) throws NativeException;
    
    private native boolean IsWow64Process0();
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int HKEY_CLASSES_ROOT              = 0;
    public static final int HKEY_CURRENT_USER              = 1;
    public static final int HKEY_LOCAL_MACHINE             = 2;
    public static final int HKEY_USERS                     = 3;
    public static final int HKEY_CURRENT_CONFIG            = 4;
    
    public static final int HKEY_DYN_DATA                  = 5;
    public static final int HKEY_PERFORMANCE_DATA          = 6;
    public static final int HKEY_PERFORMANCE_NLSTEXT       = 7;
    public static final int HKEY_PERFORMANCE_TEXT          = 8;
    
    public static final int HKCR                           = HKEY_CLASSES_ROOT;
    public static final int HKCU                           = HKEY_CURRENT_USER;
    public static final int HKLM                           = HKEY_LOCAL_MACHINE;
    
    public static final int REG_NONE                       = 0;
    public static final int REG_SZ                         = 1;
    public static final int REG_EXPAND_SZ                  = 2;
    public static final int REG_BINARY                     = 3;
    public static final int REG_DWORD_LITTLE_ENDIAN        = 4;
    public static final int REG_DWORD                      = 4;
    public static final int REG_DWORD_BIG_ENDIAN           = 5;
    public static final int REG_LINK                       = 6;
    public static final int REG_MULTI_SZ                   = 7;
    public static final int REG_RESOURCE_LIST              = 8;
    public static final int REG_FULL_RESOURCE_DESCRIPTOR   = 9;
    public static final int REG_RESOURCE_REQUIREMENTS_LIST = 10;
    public static final int REG_QWORD_LITTLE_ENDIAN        = 11;
    public static final int REG_QWORD                      = 11;
    
    public static final int MODE_DEFAULT                   = 0;
    public static final int MODE_32BIT                     = 1;
    public static final int MODE_64BIT                     = 2;
    
    public static final String SEPARATOR = "\\";
    
    private static int KEY_READ_LEVEL = 0;
    private static int KEY_MODIFY_LEVEL = 1;
    
    private static final String ERROR_CANNOT_ACCESS_NATIVE_METHOD_STRING =
            ResourceUtils.getString(WindowsRegistry.class, 
            "WR.error.cannot.access.native");//NOI18N
}
