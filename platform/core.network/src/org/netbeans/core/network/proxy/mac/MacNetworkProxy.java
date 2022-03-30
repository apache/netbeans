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

import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.network.proxy.NetworkProxyResolver;
import org.netbeans.core.network.proxy.NetworkProxySettings;

/**
 *
 * @author lfischme
 */
public class MacNetworkProxy implements NetworkProxyResolver {
    
    private static final Logger LOGGER = Logger.getLogger(MacNetworkProxy.class.getName());
    
    private static final MacNetworkProxyLibrary cfNetworkLibrary = MacNetworkProxyLibrary.LIBRARY;
    private static final MacCoreFoundationLibrary cfLibrary = MacCoreFoundationLibrary.LIBRARY;
    
    private static final String COMMA = ","; //NOI18N
    
    private static final NativeLibrary NETWORK_LIBRARY = NativeLibrary.getInstance("CoreServices"); //NOI18N
    private static final String KEY_AUTO_DISCOVERY_ENABLE = "kCFNetworkProxiesProxyAutoDiscoveryEnable"; //NOI18N
    private static final String KEY_PAC_ENABLE = "kCFNetworkProxiesProxyAutoConfigEnable"; //NOI18N
    private static final String KEY_PAC_URL = "kCFNetworkProxiesProxyAutoConfigURLString"; //NOI18N
    private static final String KEY_HTTP_ENABLE = "kCFNetworkProxiesHTTPEnable"; //NOI18N
    private static final String KEY_HTTP_HOST = "kCFNetworkProxiesHTTPProxy"; //NOI18N
    private static final String KEY_HTTP_PORT = "kCFNetworkProxiesHTTPPort"; //NOI18N
    private static final String KEY_HTTPS_ENABLE = "kCFNetworkProxiesHTTPSEnable"; //NOI18N
    private static final String KEY_HTTPS_HOST = "kCFNetworkProxiesHTTPSProxy"; //NOI18N
    private static final String KEY_HTTPS_PORT = "kCFNetworkProxiesHTTPSPort"; //NOI18N
    private static final String KEY_SOCKS_ENABLE = "kCFNetworkProxiesSOCKSEnable"; //NOI18N
    private static final String KEY_SOCKS_HOST = "kCFNetworkProxiesSOCKSProxy"; //NOI18N
    private static final String KEY_SOCKS_PORT = "kCFNetworkProxiesSOCKSPort"; //NOI18N
    private static final String KEY_EXCEPTIONS_LIST = "kCFNetworkProxiesExceptionsList"; //NOI18N
    
    @Override
    public NetworkProxySettings getNetworkProxySettings() {
        boolean resolved = false;
        
        LOGGER.log(Level.FINE, "Mac system proxy resolver started."); //NOI18N
        Pointer settingsDictionary = cfNetworkLibrary.CFNetworkCopySystemProxySettings();

        Pointer autoDiscoveryEnable = cfLibrary.CFDictionaryGetValue(settingsDictionary, getKeyCFStringRef(KEY_AUTO_DISCOVERY_ENABLE));
        if (getIntFromCFNumberRef(autoDiscoveryEnable) != 0) {
            LOGGER.log(Level.INFO, "Mac system proxy resolver: auto detect"); //NOI18N                                    
            resolved = true;
        }
        
        Pointer pacEnable = cfLibrary.CFDictionaryGetValue(settingsDictionary, getKeyCFStringRef(KEY_PAC_ENABLE));
        if (getIntFromCFNumberRef(pacEnable) != 0) {
            Pointer[] pacUrlPointer = new Pointer[1];
            if (cfLibrary.CFDictionaryGetValueIfPresent(settingsDictionary, getKeyCFStringRef(KEY_PAC_URL), pacUrlPointer)) {
                String pacUrl = getStringFromCFStringRef(pacUrlPointer[0]);
                
                LOGGER.log(Level.INFO, "Mac system proxy resolver: auto - PAC ({0})", pacUrl); //NOI18N
                return new NetworkProxySettings(pacUrl);
            }
        }
        
        Pointer httpEnable = cfLibrary.CFDictionaryGetValue(settingsDictionary, getKeyCFStringRef(KEY_HTTP_ENABLE));
        Pointer httpsEnable = cfLibrary.CFDictionaryGetValue(settingsDictionary, getKeyCFStringRef(KEY_HTTPS_ENABLE));
        Pointer socksEnable = cfLibrary.CFDictionaryGetValue(settingsDictionary, getKeyCFStringRef(KEY_SOCKS_ENABLE));
        if (getIntFromCFNumberRef(httpEnable) != 0 || getIntFromCFNumberRef(httpsEnable) != 0 || getIntFromCFNumberRef(socksEnable) != 0) {
            String httpHost = getStringFromCFStringRef(getValueIfExists(settingsDictionary, KEY_HTTP_HOST));
            String httpPort = getStringFromCFNumberRef(getValueIfExists(settingsDictionary, KEY_HTTP_PORT));
            String httpsHost = getStringFromCFStringRef(getValueIfExists(settingsDictionary, KEY_HTTPS_HOST));
            String httpsPort = getStringFromCFNumberRef(getValueIfExists(settingsDictionary, KEY_HTTPS_PORT));
            String socksHost = getStringFromCFStringRef(getValueIfExists(settingsDictionary, KEY_SOCKS_HOST));
            String socksPort = getStringFromCFNumberRef(getValueIfExists(settingsDictionary, KEY_SOCKS_PORT));            
            String[] noProxyHosts = getNoProxyHosts(getValueIfExists(settingsDictionary, KEY_EXCEPTIONS_LIST));
            
            LOGGER.log(Level.INFO, "Mac system proxy resolver: manual - http host ({0})", httpHost); //NOI18N
            LOGGER.log(Level.INFO, "Mac system proxy resolver: manual - http port ({0})", httpPort); //NOI18N
            LOGGER.log(Level.INFO, "Mac system proxy resolver: manual - https host ({0})", httpsHost); //NOI18N
            LOGGER.log(Level.INFO, "Mac system proxy resolver: manual - https port ({0})", httpsPort); //NOI18N
            LOGGER.log(Level.INFO, "Mac system proxy resolver: manual - socks host ({0})", socksHost); //NOI18N
            LOGGER.log(Level.INFO, "Mac system proxy resolver: manual - socks port ({0})", socksPort); //NOI18N
            LOGGER.log(Level.INFO, "Mac system proxy resolver: manual - no proxy hosts ({0})", getStringFromArray(noProxyHosts)); //NOI18N
            
            return new NetworkProxySettings(httpHost, httpPort, httpsHost, httpsPort, socksHost, socksPort, noProxyHosts);
        }
        
        return new NetworkProxySettings(resolved);
    }
    
    /**
     * Converts String key object to CFString key object.
     * 
     * @param key String key
     * @return CFString key object pointer
     */
    private Pointer getKeyCFStringRef(String key) {
        return NETWORK_LIBRARY.getGlobalVariableAddress(key).getPointer(0L);
    }
    
    /**
     * Converts CFString object to String.
     * 
     * @param cfStringPointer Pointer to CFString object
     * @return String from CFString
     */
    private String getStringFromCFStringRef(Pointer cfStringPointer) {
        if (cfStringPointer != null) {
            long lenght = cfLibrary.CFStringGetLength(cfStringPointer);
            long maxSize = cfLibrary.CFStringGetMaximumSizeForEncoding(lenght, 0x08000100); // 0x08000100 = UTF-8

            Pointer buffer = new Memory(maxSize);

            if (cfLibrary.CFStringGetCString(cfStringPointer, buffer, maxSize, 0x08000100)) { // 0x08000100 = UTF-8
                return buffer.getString(0L);
            }
        }
        
        return null;
    }
    
    /**
     * Converts CFNumber to int.
     * 
     * @param cfNumberPointer pointer to CFNumber object
     * @return int from CFNumber
     */
    private int getIntFromCFNumberRef(Pointer cfNumberPointer) {
        if (cfNumberPointer != null) {
            Pointer cfNumberType = cfLibrary.CFNumberGetType(cfNumberPointer);
            
            long numberSize = cfLibrary.CFNumberGetByteSize(cfNumberPointer);
            Pointer numberValue = new Memory(numberSize);
            if (cfLibrary.CFNumberGetValue(cfNumberPointer, cfNumberType, numberValue)) {
                return numberValue.getInt(0L);
            }
        }
        
        return 0;
    }
    
    /**
     * Converts CFNumber to String.
     * 
     * @param cfNumberPointer pointer to CFNumber object
     * @return String from CFNumber
     */
    private String getStringFromCFNumberRef(Pointer cfNumberPointer) {
        if (cfNumberPointer != null) {
            Pointer cfNumberType = cfLibrary.CFNumberGetType(cfNumberPointer);
            
            long numberSize = cfLibrary.CFNumberGetByteSize(cfNumberPointer);
            Pointer numberValue = new Memory(numberSize);
            if (cfLibrary.CFNumberGetValue(cfNumberPointer, cfNumberType, numberValue)) {
                return String.valueOf(numberValue.getInt(0L));
            }
        }
        
        return null;
    }
    
    /**
     * Returns array of Strings with no proxy hosts retrieved from pointer to CFArray.
     * 
     * @param noProxyHostsPointer Pointer to CFArray of CFStrings with no proxy hosts
     * @return Array of Strings with no proxy hosts retrieved from pointer to CFArray.
     */
    private String[] getNoProxyHosts(Pointer noProxyHostsPointer) {
        if (noProxyHostsPointer != null) {
            long arrayLenght = cfLibrary.CFArrayGetCount(noProxyHostsPointer);
            String[] noProxyHosts = new String[(int) arrayLenght];
            for (long i = 0; i < arrayLenght; i++) {
                Pointer value = cfLibrary.CFArrayGetValueAtIndex(noProxyHostsPointer, new Pointer(i));
                String noProxyHost = getStringFromCFStringRef(value);
                noProxyHosts[(int) i] = noProxyHost;
            }
            // Much more better would be to use CFArrayGetValues method.
            // But I was unsuccessful to retrieve value correctly.
            // the const void **value is problem in this case
            // also CFRange wasn't easy to create (via Structure)
            
            return noProxyHosts;
        }
        
        return new String[0];
    }
    
    /**
     * Returns value from CFDictionary if exists.
     * 
     * @param settingsDictionary pointer to CFDictionary
     * @param key Key for which value should be returned
     * @return Value from CFDictionary if exists.
     */
    private Pointer getValueIfExists(Pointer settingsDictionary, String key) {
        Pointer[] returnValue = new Pointer[1];
        if (cfLibrary.CFDictionaryGetValueIfPresent(settingsDictionary, getKeyCFStringRef(key), returnValue)) {
            return returnValue[0];
        } else {
            return null;
        }
    }
    
    /**
     * Returns string from array of strings. Strings are sepparated by comma.
     * 
     * @param stringArray
     * @return String from array of strings. Strings are sepparated by comma.
     */
    private static String getStringFromArray(String[] stringArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            sb.append(stringArray[i]);
            if (i == stringArray.length - 1) {
                sb.append(COMMA);
            }
        }
        
        return sb.toString();
    }
}
