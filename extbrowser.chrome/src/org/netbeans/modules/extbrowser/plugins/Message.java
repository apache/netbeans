/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
