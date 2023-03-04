/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.core.network.proxy.pac;

/**
 * Entry points for PAC script.
 * 
 * @author lbruun
 */
public enum PacJsEntryFunction {
   
    /**
     * Main entry point to JavaScript PAC script as defined by Netscape.
     * This is JavaScript function name {@code FindProxyForURL()}.
     */
    STANDARD("FindProxyForURL"),

    /**
     * Main entry point to JavaScript PAC script for IPv6 support, 
     * as defined by Microsoft. 
     * This is JavaScript function name {@code FindProxyForURLEx()}.
     */
    IPV6_AWARE("FindProxyForURLEx");
    
    private final String jsFunctionName;
    
    PacJsEntryFunction(String jsFunctionName) {
        this.jsFunctionName = jsFunctionName;    
    }

    /**
     * Gets name of JavaScript function.
     * @return 
     */
    public String getJsFunctionName() {
        return jsFunctionName;
    }
}
