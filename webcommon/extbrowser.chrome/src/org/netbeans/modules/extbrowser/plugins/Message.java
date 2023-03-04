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
package org.netbeans.modules.extbrowser.plugins;

import java.util.Locale;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.openide.util.Exceptions;


/**
 * @author ads
 *
 */
public class Message {

    private static final String MESSAGE = "message";        // NOI18N
    
    public enum MessageType {
        INIT,
        RELOAD,
        URLCHANGE,
        CLOSE,
        INSPECT,
        ATTACH_DEBUGGER,
        DETACH_DEBUGGER,
        DEBUGGER_COMMAND,
        DEBUGGER_COMMAND_RESPONSE,
        DEBUGGER_DETACHED,
        LOAD_RESIZE_OPTIONS,
        SAVE_RESIZE_OPTIONS,
        READY,
        RESOURCE_CHANGED,
        ;
        
        @Override
        public String toString() {
            return super.toString().toLowerCase( Locale.US);
        }
        
        public static MessageType forString( String str ){
            for( MessageType type : values() ){
                if ( type.toString().equals( str )){
                    return type;
                }
            }
            return null;
        }
    }
    
    Message(MessageType type , JSONObject data ){
        this.type = type;
        this.data = data;
    }
    
    Message(MessageType type , Map map ){
        this.type = type;
        this.data = new JSONObject(map);
    }

    public int getTabId() {
        Number n = (Number)getValue().get(Message.TAB_ID);
        if (n == null) {
            return -1;
        }
        return n.intValue();
    }
    
    public static Message parse( String message ){
        try {
            JSONObject json = (JSONObject)JSONValue.parseWithException(message);
            return new Message(MessageType.forString((String)json.get("message")), json);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public MessageType getType(){
        return type;
    }
    
    public String toStringValue() {
        JSONObject result = new JSONObject(data);
        result.put("message", type.toString());
        return result.toJSONString();
    }
    
    public JSONObject getValue() {
        return data;
    }
    
    
    private final MessageType type;
    private final JSONObject data;
    static final String TAB_ID = "tabId";       // NOI18N    
    
}
