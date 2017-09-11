/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging.api.debugger;

import java.util.Collections;
import java.util.List;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "TYPE_OBJECT=Object",
    "TYPE_FUNCTION=Function",
    "TYPE_UNDEFINED=Undefined",
    "TYPE_STRING=String",
    "TYPE_NUMBER=Number",
    "TYPE_BOOLEAN=Boolean"
})
/**
 * Runtime.RemoteObject
 */
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
