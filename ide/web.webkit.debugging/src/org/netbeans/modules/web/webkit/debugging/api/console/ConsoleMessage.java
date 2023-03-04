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

package org.netbeans.modules.web.webkit.debugging.api.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Representation of console message.
 */
public class ConsoleMessage {

    private final JSONObject msg;
    private List<StackFrame> stackTrace;
    private boolean stackTraceLoaded;

    ConsoleMessage(JSONObject msg) {
        this.msg = msg;
    }

    public String getSource() {
        if (msg != null) {
            return (String)msg.get("source");
        } else {
            return "";
        }
    }
    
    public String getLevel() {
        if (msg != null) {
            return (String)msg.get("level");
        } else {
            return "";
        }
    }
    
    public String getType() {
        if (msg != null) {
            return (String)msg.get("type");
        } else {
            return "";
        }
    }
    
    public String getText() {
        if (msg != null) {
            return (String)msg.get("text");
        } else {
            return "";
        }
    }
    
    public String getURLString() {
        if (msg != null) {
            return (String)msg.get("url");
        } else {
            return "";
        }
    }
    
    public int getLine() {
        if (msg != null) {
            Number n = (Number)msg.get("line");
            if (n != null) {
                return n.intValue();
            }
        }
        return -1;
    }

    public List<StackFrame> getStackTrace() {
        if (msg == null) {
            return Collections.EMPTY_LIST;
        }
        if (!stackTraceLoaded) {
            JSONArray stack = (JSONArray)msg.get("stackTrace");
            if (stack == null) {
                JSONObject stackObj = (JSONObject) msg.get("stack");            // NOI18N
                if (stackObj != null) {
                    stack = (JSONArray) stackObj.get("callFrames");             // NOI18N
                }
            }
            if (stack != null && stack.size() > 0) {
                stackTrace = new ArrayList<StackFrame>();
                for (Object o : stack) {
                    JSONObject json = (JSONObject)o;
                    stackTrace.add(new StackFrame(json));
                }
            }
            stackTraceLoaded = true;
        }
        return stackTrace;
    }
    
    /**
     * @since 1.19
     * @return A list of sub-messages.
     */
    public List<ConsoleMessage> getSubMessages() {
        return Collections.EMPTY_LIST;
    }
    
    
    public static final class StackFrame {

        private JSONObject stack;

        public StackFrame(JSONObject stack) {
            this.stack = stack;
        }
        
        public String getFunctionName() {
            String s = (String)stack.get("functionName");
            if (s == null || s.isEmpty()) {
                s = "(anonymous function)";
            }
            return s;
        }
        
        public String getURLString() {
            return (String)stack.get("url");
        }
        
        public int getLine() {
            Number n = (Number)stack.get("lineNumber");
            if (n != null) {
                return n.intValue();
            }
            return -1;
        }

        public int getColumn() {
            Number n = (Number)stack.get("columnNumber");
            if (n != null) {
                return n.intValue();
            }
            return -1;
        }
        
    }
}
