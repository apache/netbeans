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
package org.netbeans.core.network.proxy.pac.impl;

/**
 * Auto-generates JavaScript source for declaration of
 * helper functions.
 * 
 * @author lbruun
 */
class HelperScriptFactory {
    
    // Netscape functions 
    private static final JsHelperFunction[] JS_HELPER_FUNCTIONS_NS = new JsHelperFunction[]{
        new JsHelperFunction("isPlainHostName",     new String[]{"host"}, Boolean.class),
        new JsHelperFunction("dnsDomainIs",         new String[]{"host", "domain"}, Boolean.class),
        new JsHelperFunction("localHostOrDomainIs", new String[]{"host", "hostdom"}, Boolean.class),
        new JsHelperFunction("isResolvable",        new String[]{"host"}, Boolean.class),
        new JsHelperFunction("isInNet",             new String[]{"host", "pattern", "mask"}, Boolean.class),
        new JsHelperFunction("dnsResolve",          new String[]{"host"}, String.class),
        new JsHelperFunction("myIpAddress",         new String[]{}, String.class),
        new JsHelperFunction("dnsDomainLevels",     new String[]{"host"}, Integer.class),
        new JsHelperFunction("shExpMatch",          new String[]{"str", "shexp"}, Boolean.class),
        new JsHelperFunction("weekdayRange",        new String[]{"wd1", "wd2", "gmt"}, Boolean.class),
        new JsHelperFunction("dateRange",           new String[]{"day1", "month1", "year1", "day2", "month2", "year2", "gmt"}, Boolean.class),
        new JsHelperFunction("timeRange",           new String[]{"hour1", "min1", "sec1", "hour2", "min2", "sec2", "gmt"}, Boolean.class),
    };
    
    // Microsoft functions 
    private static final JsHelperFunction[] JS_HELPER_FUNCTIONS_MS = new JsHelperFunction[]{
        new JsHelperFunction("isResolvableEx",      new String[]{"host"}, Boolean.class),
        new JsHelperFunction("isInNetEx",           new String[]{"host", "ipPrefix"}, Boolean.class),
        new JsHelperFunction("dnsResolveEx",        new String[]{"host"}, String.class),
        new JsHelperFunction("myIpAddressEx",       new String[]{}, String.class),
        new JsHelperFunction("sortIpAddressList",   new String[]{"ipAddressList"}, String.class),
        new JsHelperFunction("getClientVersion",    new String[]{}, String.class)
    };
    
    // Debug functions (not part of any spec)
    private static final JsHelperFunction[] JS_HELPER_FUNCTIONS_DEBUG = new JsHelperFunction[]{
        new JsHelperFunction("alert",               new String[]{"txt"}, Void.class)
    };

    private HelperScriptFactory() {
    }
    
    /**
     * Gets JavaScript source with PAC Helper function declarations.
     * 
     * @param bridgeObjectName name of Java object which contains Java methods, 
     *   named similarly to the JavaScript PAC helper functions and with 
     *   similar arg list. This Java object acts as the bridge between the 
     *   JavaScript world and the Java world and must be an instance 
     *   of {@link org.netbeans.network.proxy.pac.PacHelperMethods PacHelperMethods}.
     * 
     * @return JavaScript source code
     */
    public static String getPacHelperSource(String bridgeObjectName) {
        StringBuilder sb = new StringBuilder(2000);
        addFunctionDecls(sb, JS_HELPER_FUNCTIONS_NS, bridgeObjectName);
        addFunctionDecls(sb, JS_HELPER_FUNCTIONS_MS, bridgeObjectName);
        addFunctionDecls(sb, JS_HELPER_FUNCTIONS_DEBUG, bridgeObjectName);
        return sb.toString();
    }
    
    
    private static void addFunctionDecls(StringBuilder sb, JsHelperFunction[] jsHelperFunctions, String bridgeObjectName) {
        for (JsHelperFunction f : jsHelperFunctions) {
            sb.append("function ");
            sb.append(f.functionName);
            sb.append('(');
            addArgList(sb, f.argList);
            sb.append(") {");
            sb.append('\n');
            sb.append("    return ");
            boolean encloseReturnValue = false;
            if (Number.class.isAssignableFrom(f.getClass())) {
                encloseReturnValue = true;
                sb.append("Number(");
            }
            if (f.returnType == String.class) {
                encloseReturnValue = true;
                sb.append("String(");
            }
            sb.append(bridgeObjectName);
            sb.append('.');
            sb.append(f.functionName);
            sb.append('(');
            addArgList(sb, f.argList);
            sb.append(')');
            if (encloseReturnValue) {
                sb.append(')');
            }
            sb.append(";\n");
            sb.append("}\n\n");
        }
    }
    
    private static void addArgList(StringBuilder sb, String[] argList) {
        if (argList != null && argList.length > 0) {
            for (int i = 0; i < argList.length; i++) {
                sb.append(argList[i]);
                if (i < argList.length - 1) {
                    sb.append(", ");
                }
            }
        }
    }
    
    private static class JsHelperFunction {
        String functionName;
        String[] argList;
        Class returnType;

        public JsHelperFunction(String functionName, String[] argList, Class returnType) {
            this.functionName = functionName;
            this.argList = argList;
            this.returnType = returnType;
        }
        
    }
    
}
