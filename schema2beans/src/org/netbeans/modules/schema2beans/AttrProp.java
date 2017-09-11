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

package org.netbeans.modules.schema2beans;


import java.util.*;

/**
 *  This class hold the information about a property attribute.
 *
 *  This class is used by the TreeBuilder to build a representation of the
 *  attributes, by the BeanClass to convert these information into
 *  generated attribute code, and by the BeanProp class to hold at runtime
 *  the information about the attributes of a property.
 *
 */
public class AttrProp implements BaseAttribute {

    static public final int MASK_KIND 		= 0x00FF;
    static public final int CDATA 		= 0x0001;
    static public final int ENUM 		= 0x0002;
    static public final int NMTOKEN 		= 0x0003;
    static public final int ID 			= 0x0004;
    static public final int IDREF 		= 0x0005;
    static public final int IDREFS 		= 0x0006;
    static public final int ENTITY 		= 0x0007;
    static public final int ENTITIES 		= 0x0008;
    static public final int NOTATION 		= 0x0009;
    
    static final String[] kinds =
	new String[] {"CDATA", "ENUM", "NMTOKEN", "ID", "IDREF",	// NOI18N
		      "IDREFS", "ENTITY", "ENTITIES", "NOTATION"};	// NOI18N
    
    static final int[] kindValues =
	new int[] {CDATA, ENUM, NMTOKEN, ID, IDREF,
		   IDREFS, ENTITY, ENTITIES, NOTATION};
    
    static public final int MASK_OPTION 	= 0x0F00;
    static public final int REQUIRED 		= 0x0100;
    static public final int IMPLIED 		= 0x0200;
    static public final int FIXED 		= 0x0300;
    
    static public final int TRANSIENT		= 0x1000;
    
    static final String[] options =
        new String[] {"#REQUIRED", "#IMPLIED", "#FIXED"};	// NOI18N
    
    static final int[] optionValues = new int[] {REQUIRED, IMPLIED, FIXED};
    
    //	Property this attribure belongs to
    String		propertyName;
    
    //	Name of the attribute
    String 		name;
    
    //	Name of the attribute
    String 		dtdName;

    String 		namespace;
    
    //	Its type (CDATA, ID, ...)
    int			type;

    // Proposed java class for it.
    String javaType;
    
    //	Enum values if any (null if none)
    ArrayList		values;
    
    //	The default value of the attribute
    String		defaultValue;

    //
    //	The attribute content is populated only calling addValue()
    //	assuming it is built from a left to right parsing. This state
    //	is used to know which value is being added.
    //
    private int		state;
    
    private int 	enumMode;

    private List extraData;
    //private GraphNode sourceGraphNode;
    
    //	The state values when the attribute is populated
    private static final int NEED_NAME 		= 0;
    private static final int NEED_TYPE		= 1;
    private static final int NEED_ENUM		= 2;
    private static final int NEED_OPTION	= 3;
    private static final int NEED_DEFVAL	= 4;
    private static final int NEED_VALUE		= 5;
    private static final int DONE		= 6;
    
    public AttrProp() {
        this.values = null;
        this.state = NEED_NAME;
        this.type = 0;
        this.enumMode = 0;
    }
    
    public AttrProp(String propName) {
        this();
        this.propertyName = propName;
    }
    
    public AttrProp(String propName, String dtdName, String name, int type,
                    String[] values, String defValue) {
			
        this.dtdName = dtdName;
        this.name = name;
        this.propertyName = propName;
	
        if (values != null && values.length > 0) {
            this.values = new ArrayList();
            for (int i=0; i<values.length; i++)
                this.values.add(values[i]);
        }
	
        this.defaultValue = defValue;
        this.state = DONE;
        this.type = type;
    }

    /**
     * @return Common.TYPE_1, Common.TYPE_0_1, or Common.TYPE_0 (maybe)
     */
    public int getInstance() {
        if (defaultValue != null)
            return Common.TYPE_1;
        switch (type & MASK_OPTION) {
        case FIXED:
        case REQUIRED:
            return Common.TYPE_1;
        }
        // optional
        return Common.TYPE_0_1;
    }
    
    public void setEnum(boolean enume) {
        enumMode += (enume?1:-1);
        if (enumMode == 1) {
            if (this.values == null)
                this.values = new ArrayList();
            this.type = ENUM;
            this.state = NEED_ENUM;
        }
        else
            if (enumMode == 0) {
                this.state = NEED_OPTION;
            }
            else
                this.failed(Common.getMessage("WrongEnumDecl_msg"));
    }
    
    public void addValue(String value) {
        addValue(value, null);
    }
    
    public void addValue(String value, String namespace) {
        //
        //	Get rid of both heading and trailing " character
        //	(we assume that they live in pair)
        //
        int valueLen = value.length();
        if (value.charAt(0) == '"')	{ // NOI18N
            if (valueLen == 1)
                failed(Common.getMessage("TooLittleDeclaration_msg", value));
            value = value.substring(1, value.length()-1);
        } else if (value.charAt(0) == '\'')	{ // NOI18N
            if (valueLen == 1)
                failed(Common.getMessage("TooLittleDeclaration_msg", value));
            value = value.substring(1, value.length()-1);
        }
	
	//	Name Type_OR_Enums DefValue_OR_#Opt [Val for #FIXED]
        switch(this.state) {
	    case NEED_NAME:
            //	name of the attribute
            this.dtdName = value;
            this.namespace = namespace;
            this.name = Common.convertName(value);
            this.state = NEED_TYPE;
            break;
	    case NEED_TYPE:
            //	Should be the type
            this.type = this.stringToInt(value, kinds, kindValues);
            this.state = NEED_OPTION;
            if (this.type == -1)
                this.failed(Common.getMessage("UnknownType_msg", value));
            break;
	    case NEED_ENUM:
            this.values.add(value);
            break;
	    case NEED_OPTION:
            int opt = this.stringToInt(value, options, optionValues);
            if (opt != -1) {
                this.type |= opt;
                if (opt == FIXED)
                    this.state = NEED_VALUE;
                else
                    this.state = DONE;
                break;
            }
            //	do not break - no option this is a default value;
	    case NEED_VALUE:
            this.defaultValue = value;
            this.state = DONE;
            break;
	    case DONE:
            this.failed(Common.getMessage("TooMuchDeclaration_msg"));
        }
    }
    
    //
    //	The attribute is transient if it has been added on the fly
    //	when reading the XML document (was not defined in the DTD)
    //
    public boolean isTransient() {
        return ((this.type & TRANSIENT) == TRANSIENT);
    }
    
    //	Return true if this instance of attrProp is properly and completly
    //	built from the DTD declaration.
    public boolean isComplete() {
        return (this.state == DONE);
    }
    
    //	true if the name is either the mangled name or the dtd name
    public boolean hasName(String name) {
        if (name.equals(this.name) || name.equals(this.dtdName))
            return true;
        else
            return false;
    }
    
    //
    //	Return the list of possible values (enum)
    //	If there is no value defined, the array is empty
    //
    public String[] getValues() {
        int size = 0;
	
        if (this.values != null)
            size = this.values.size();
	
        String[] ret = new String[size];
	
        if (size > 0)
            return (String[])this.values.toArray(ret);
        else
            return ret;
    }

    public void setDefaultValue(String d) {
        defaultValue = d;
    }
    
    public String getDefaultValue() {
        return this.defaultValue;
    }
    
    public String getPropertyName() {
	return this.propertyName;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String n) {
        name = n;
    }
    /*
    public void setSourceGraphNode(GraphNode node) {
        sourceGraphNode = node;
    }

    public GraphNode getSourceGraphNode() {
        return sourceGraphNode;
    }
    */
    
    public String getDtdName() {
        return this.dtdName;
    }

    public String getNamespace() {
        return this.namespace;
    }
    
    public String typeAsString() {
        String str = "AttrProp." +		// NOI18N
            intToString(this.type & MASK_KIND, kinds, kindValues);
	
        int opt = this.type & MASK_OPTION;
        if (opt != 0) {
            str += " | " + "AttrProp." +	// NOI18N
                intToString(opt, options, optionValues).substring(1);
        }
	
        return str;
    }
    
    String enumsToString() {
        String[] e = this.getValues();
        StringBuffer ret = new StringBuffer();
        for (int i=0; i<e.length; i++) {
            ret.append(e[i]);
            ret.append(" ");	// NOI18N
        }
        return ret.toString();
    }
    
    public void validate() {
        //	Called at the end of the element parsing
        if (this.state != DONE)
            this.failed(Common.getMessage("BadAttributeDecl_msg"));
    }
    
    public void checkEnum() {
        //	Called when a | is found
        if (this.enumMode == 0)
            this.failed(Common.getMessage("UseCharORWithEnum_msg"));
    }
    
    public boolean isEnum() {
        return ((this.type & MASK_KIND) == ENUM);
    }
    
    public boolean isFixed() {
        return ((this.type & MASK_OPTION) == FIXED);
    }
    
    public int getType() {
        return (this.type & MASK_KIND);
    }
    
    public int getOption() {
        return (this.type & MASK_OPTION);
    }
    
    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String jt) {
        javaType = jt;
    }

    int stringToInt(String str, String[] map, int[] val) {
        for(int i=0; i<map.length; i++) {
            if (str.equals(map[i]))
                return val[i];
        }
        return -1;
    }
    
    String intToString(int id, String[] map, int[] val) {
        for(int i=0; i<val.length; i++) {
            if (id == val[i])
                return map[i];
        }
        return "?";	// NOI18N
    }
    
    private void failed(String err) {
        throw new RuntimeException(Common.getMessage("ATTLISTParseError_msg", this.name, err));
    }

    public void addExtraData(Object data) {
        if (extraData == null)
            extraData = new ArrayList();
        extraData.add(data);
    }

    public List getExtraData() {
        if (extraData == null)
            return Collections.EMPTY_LIST;
        return extraData;
    }
    
    public String toString() {
        String str = this.dtdName + " " +					// NOI18N
            intToString(this.type & MASK_KIND, kinds, kindValues) + " ";	// NOI18N
	
        int opt = this.type & MASK_OPTION;
        if (opt != 0)
            str += intToString(opt, options, optionValues) + " ";	// NOI18N
	
        if (this.values != null) {
            int size = this.values.size();
            str += "( ";	// NOI18N
            for(int i=0; i<size; i++) {
                str += this.values.get(i) + " ";	// NOI18N
            }
            str += ") ";	// NOI18N
        }
        if (this.defaultValue != null)
            str += this.defaultValue;
	
        if (this.isTransient())
            str += " (transient)";	// NOI18N
        if (javaType != null)
            str += " : " + javaType;
        return str;
    }
}

