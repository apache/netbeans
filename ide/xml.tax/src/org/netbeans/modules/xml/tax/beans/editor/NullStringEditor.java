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
package org.netbeans.modules.xml.tax.beans.editor;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.beans.FeatureDescriptor;

import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class NullStringEditor extends PropertyEditorSupport implements ExPropertyEditor {

    /** */
    protected static final String DEFAULT_NULL = Util.THIS.getString ("TEXT_DEFAULT");

    /** */
    private boolean editable;


    //
    // init
    //

    /** Creates new NullStringEditor */
    public NullStringEditor () {
        super();
        editable = true;
    }

    
    //
    // PropertyEditor
    //

    /**
     */
    public void setAsText (String text) throws IllegalArgumentException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("NullStringEditor::setAsText: text = " + text); // NOI18N

	if ( DEFAULT_NULL.equals (text) ) {
	    setValue (null);
	} else if ( text.length() == 0 ) {
	    setValue (null);
	} else {
	    setValue (text);
	}
    }

    /**
     */
    public String getAsText () {
	Object value = super.getValue();

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("NullStringEditor::getAsText: value = " + value); // NOI18N

	if ( value == null ) {
	    return DEFAULT_NULL;
	} else {
	    String text = value.toString();
	    if ( text.length() == 0) {
		return DEFAULT_NULL;
	    }
	    return text;
	}
    }

    /**
     */
    public boolean supportsCustomEditor () {
        return true;
    }

    /**
     */
    public Component getCustomEditor () {
        return new NullStringCustomEditor (this);
    }

    /**
     */
    public String getJavaInitializationString () {
        String s = (String) getValue ();
        return "\"" + toAscii (s) + "\""; // NOI18N
    }


    //
    // ExPropertyEditor
    //

    /**
     */
    public void attachEnv (PropertyEnv env) {
        FeatureDescriptor desc = env.getFeatureDescriptor();

        if (desc instanceof Node.Property){
            Node.Property prop = (Node.Property)desc;

            editable = prop.canWrite();
        }
    }


    //
    // EnhancedPropertyEditor
    //
    
    /**
     */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    /**
     */
    public Component getInPlaceCustomEditor () {
        return null;
    }

    /**
     */
    public boolean supportsEditingTaggedValues () {
        return false;
    }


    //
    // itself
    //

    /**
     */
    public boolean isEditable () {
        return editable;
    }

    /**
     */
    private static String toAscii (String str) {
        StringBuffer buf = new StringBuffer (str.length() * 6); // x -> \u1234
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
            case '\b': buf.append ("\\b"); break; // NOI18N
            case '\t': buf.append ("\\t"); break; // NOI18N
            case '\n': buf.append ("\\n"); break; // NOI18N
            case '\f': buf.append ("\\f"); break; // NOI18N
            case '\r': buf.append ("\\r"); break; // NOI18N
            case '\"': buf.append ("\\\""); break; // NOI18N
//  	    case '\'': buf.append ("\\'"); break; // NOI18N
            case '\\': buf.append ("\\\\"); break; // NOI18N
            default:
                if (c >= 0x0020 && c <= 0x007f)
                    buf.append (c);
                else {
                    buf.append ("\\u"); // NOI18N
                    String hex = Integer.toHexString (c);
                    for (int j = 0; j < 4 - hex.length(); j++)
                        buf.append ('0');
                    buf.append (hex);
                }
            }
        }
        return buf.toString();
    }

}
