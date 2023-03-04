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
package org.netbeans.modules.web.webkit.debugging;

import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.AbstractObject;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Script;

/**
 *
 */
public final class APIFactory {
    
    private APIFactory() {
    }
    
    /**
     * Creates the API representation of the provided SPI instance.
     * 
     * @param impl the SPI instance
     * @return the API server instance representation
     */
    public static WebKitDebugging createWebKitDebugging(TransportHelper transport) {
        return Accessor.DEFAULT.createWebKitDebugging(transport);
    }

    public static PropertyDescriptor createPropertyDescriptor(JSONObject property, WebKitDebugging webkit) {
        return Accessor2.DEFAULT.createPropertyDescriptor(property, webkit);
    }
    
    public static Script createScript(JSONObject property, WebKitDebugging webkit) {
        return Accessor2.DEFAULT.createScript(property, webkit);
    }
    
    public static Breakpoint createBreakpoint(JSONObject property, WebKitDebugging webkit) {
        return Accessor2.DEFAULT.createBreakpoint(property, webkit);
    }
    
    public static void breakpointResolved(Breakpoint bp, JSONObject location) {
        Accessor2.DEFAULT.breakpointResolved(bp, location);
    }
    
    public static CallFrame createCallFrame(JSONObject property, WebKitDebugging webkit, TransportHelper transport) {
        return Accessor2.DEFAULT.createCallFrame(property, webkit, transport);
    }
    
    /**
     * The accessor pattern class.
     */
    public abstract static class Accessor {

        /** The default accessor. */
        public static Accessor DEFAULT;

        static {
            // invokes static initializer of WebKitDebugging.class
            // that will assign value to the DEFAULT field above
            Class c = WebKitDebugging.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
        }


        public abstract WebKitDebugging createWebKitDebugging(TransportHelper transport);

    }

    public abstract static class Accessor2 {

        /** The default accessor. */
        public static Accessor2 DEFAULT;

        static {
            // invokes static initializer of AbstractObject.class
            // that will assign value to the DEFAULT field above
            Class c = AbstractObject.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
        }


        public abstract PropertyDescriptor createPropertyDescriptor(JSONObject property, WebKitDebugging webkit);
        public abstract Script createScript(JSONObject property, WebKitDebugging webkit);
        public abstract Breakpoint createBreakpoint(JSONObject property, WebKitDebugging webkit);
        public abstract void breakpointResolved(Breakpoint bp, JSONObject location);
        public abstract CallFrame createCallFrame(JSONObject property, WebKitDebugging webkit, TransportHelper transport);

    }
    
}
