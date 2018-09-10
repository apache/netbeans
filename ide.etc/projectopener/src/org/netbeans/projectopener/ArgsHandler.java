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

package org.netbeans.projectopener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Milan Kubec
 */
public class ArgsHandler {
    
    // Arguments will be:
    // projecturl ... project(s) zip file URL, HTTP protocol, REQUIRED parameter
    // minversion ... minumum version of NetBeans that could open the project(s)
    // mainproject ... path (in the zip file) to the project folder that will be opened as main
    
    private String args[];
    private Map argMap = new HashMap();
    private List addArgs = new ArrayList();
    
    public ArgsHandler(String[] args) {
        this.args = args;
        int index = 0;
        while (index < args.length) {
            String arg = args[index];
            if (arg.startsWith("-")) {
                String argName = arg.substring(1);
                if ("projecturl".equals(argName) || "minversion".equals(argName) || "mainproject".equals(argName)) {
                    if (args.length >= index + 2) {
                        String argVal = args[++index];
                        if (!argVal.startsWith("-")) {
                            argMap.put(argName, argVal);
                        } else {
                            // arg is missing value
                            argMap.put(argName, null);
                        }
                    } else {
                        // arg is missing value
                        argMap.put(argName, null);
                        index++;
                    }
                } else {
                    // unknown args beginning with '-'
                    addArgs.add(argName);
                    index++;
                }
            } else {
                // there are some args that do not begin with '-'
                index++;
            }
        }
    }
    
    public String getArgValue(String argName) {
        return (String) argMap.get(argName);
    }
    
    public List getAdditionalArgs() {
        return addArgs;
    }
    
    public String getAllArgs() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i] + " ");
        }
        return sb.toString().trim();
    }
    
}
