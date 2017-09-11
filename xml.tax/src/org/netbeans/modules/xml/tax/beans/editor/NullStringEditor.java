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
 *
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
