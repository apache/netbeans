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
package org.netbeans.modules.web.webkit.debugging.api.debugger;

import java.util.Collections;
import java.util.List;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.openide.util.NbBundle;

/**
 * Runtime.RemoteObject
 */
@NbBundle.Messages({
    "TYPE_OBJECT=Object",
    "TYPE_FUNCTION=Function",
    "TYPE_UNDEFINED=Undefined",
    "TYPE_STRING=String",
    "TYPE_NUMBER=Number",
    "TYPE_BOOLEAN=Boolean"
})
public class RemoteObject extends AbstractObject {
    
    private static final String PROP_TYPE = "type";                 // NOI18N
    private static final String PROP_SUBTYPE = "subtype";           // NOI18N
    private static final String PROP_VALUE = "value";               // NOI18N
    private static final String PROP_DESCRIPTION = "description";   // NOI18N
    private static final String PROP_CLASS_NAME = "className";      // NOI18N
    private static final String PROP_OBJECT_ID = "objectId";        // NOI18N
    
    public static enum Type {
        OBJECT(Bundle.TYPE_OBJECT()),
        FUNCTION(Bundle.TYPE_FUNCTION()),
        UNDEFINED(Bundle.TYPE_UNDEFINED()),
        STRING(Bundle.TYPE_STRING()),
        NUMBER(Bundle.TYPE_NUMBER()),
        BOOLEAN(Bundle.TYPE_BOOLEAN());

        private String name;
        
        private Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
    }
    
    public static enum SubType {
        ARRAY,
        DATE,
        NODE,
        NULL,
        REGEXP,
        ERROR
    }
        
    private JSONObject property;
    private List<PropertyDescriptor> properties;

    public RemoteObject(JSONObject remoteObject, WebKitDebugging webkit) {
        this(remoteObject, webkit, null);
    }
    
    RemoteObject(JSONObject remoteObject, WebKitDebugging webkit, JSONObject property) {
        super(remoteObject, webkit);
        this.property = property;
    }

    public Type getType() {
        String t = (String)getObject().get(PROP_TYPE);
        if ("object".equals(t)) {
            return Type.OBJECT;
        } else if ("function".equals(t)) {
            return Type.FUNCTION;
        } else if ("undefined".equals(t)) {
            return Type.UNDEFINED;
        } else if ("string".equals(t)) {
            return Type.STRING;
        } else if ("number".equals(t)) {
            return Type.NUMBER;
        } else if ("boolean".equals(t)) {
            return Type.BOOLEAN;
        } else {
            assert false : "what type is this?? '"+t+"'";
            return Type.OBJECT;
        }
    }
    
    public SubType getSubType() {
        String st = (String) getObject().get(PROP_SUBTYPE);
        if (st == null) {
            return null;
        }
        switch (st) {
            case "array" : return SubType.ARRAY;
            case "date"  : return SubType.DATE;
            case "node"  : return SubType.NODE;
            case "null"  : return SubType.NULL;
            case "regexp": return SubType.REGEXP;
            case "error" : return SubType.ERROR;
            default:
                assert false: "Unknown sub type: '"+st+"'";
                return null;
        }
    }

    public JSONObject getOwningProperty() {
        return property;
    }
    
    public String getClassName() {
        return (String)getObject().get(PROP_CLASS_NAME);
    }
    
    public String getDescription() {
        return (String)getObject().get(PROP_DESCRIPTION);
    }
    
    public boolean isMutable() {
        return (getType() == Type.STRING || getType() == Type.NUMBER || 
                getType() == Type.BOOLEAN);
    }
    
    private Object getValue() {
        return getObject().get(PROP_VALUE);
    }
    
    public String getValueAsString() {
        switch (getType()) {
            case STRING: return (String)getValue();
            case NUMBER: 
                Object n = getValue();
                // java.lang.Number, or "NaN".
                if (n == null) {
                    return "";
                }
                return n.toString();
            case BOOLEAN: 
                Boolean b = (Boolean)getValue();
                if (b == null) {
                    return "";
                }
                return b.toString();
        }
        return "";
    }

    public String getObjectID() {
        String remoteObjectId = (String)getObject().get(PROP_OBJECT_ID);
        if (remoteObjectId == null) {
            return null;
        }
        if (remoteObjectId.trim().length() == 0) {
            return null;
        }
        return remoteObjectId;
    }
    
    public boolean hasFetchedProperties() {
        assert getType() == Type.OBJECT;
        return properties != null || getObjectID() == null;
    }
    
    public List<PropertyDescriptor> getProperties() {
        assert getType() == Type.OBJECT;
        if (properties != null) {
            return properties;
        }
        String remoteObjectId = getObjectID();
        if (remoteObjectId == null) {
            return Collections.emptyList();
        }
        properties = getWebkit().getRuntime().getRemoteObjectProperties(this, true);
        return properties;
    }

    /**
     * Clears the fetched properties. Fresh properties will be loaded when
     * {@code getProperties()} method is invoked next time.
     */
    public void resetProperties() {
        assert getType() == Type.OBJECT;
        properties = null;
    }

    /**
     * Invokes the function with the given declaration on this object
     * (i.e. invokes the function with its {@code this} set to this object).
     * 
     * @param functionDeclaration declaration of the function.
     * @return {@code RemoteObject} representing the return value of the function.
     */
    public RemoteObject apply(String functionDeclaration) {
        return getWebkit().getRuntime().callFunctionOn(this, functionDeclaration);
    }

    public void release() {
        getWebkit().getRuntime().releaseObject(this);
    }

}
