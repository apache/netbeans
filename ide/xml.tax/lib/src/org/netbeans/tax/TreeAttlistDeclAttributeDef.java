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
package org.netbeans.tax;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeAttlistDeclAttributeDef extends TreeNodeDecl.Content implements TreeNamedObjectMap.NamedObject {

    /** */
    public static final String PROP_NAME            = "name"; // NOI18N
    /** */
    public static final String PROP_TYPE            = "type"; // NOI18N
    /** */
    public static final String PROP_ENUMERATED_TYPE = "enumeratedType"; // NOI18N
    /** */
    public static final String PROP_DEFAULT_TYPE    = "defaultType"; // NOI18N
    /** */
    public static final String PROP_DEFAULT_VALUE   = "defaultValue"; // NOI18N
    
    /** */
    public static final short TYPE_CDATA      = 0;
    /** */
    public static final short TYPE_ID         = 1;
    /** */
    public static final short TYPE_IDREF      = 2;
    /** */
    public static final short TYPE_IDREFS     = 3;
    /** */
    public static final short TYPE_ENTITY     = 4;
    /** */
    public static final short TYPE_ENTITIES   = 5;
    /** */
    public static final short TYPE_NMTOKEN    = 6;
    /** */
    public static final short TYPE_NMTOKENS   = 7;
    /** */
    public static final short TYPE_ENUMERATED = 8;
    /** */
    public static final short TYPE_NOTATION   = 9;
    
    /** */
    public static final short DEFAULT_TYPE_NULL     = 0;
    /** */
    public static final short DEFAULT_TYPE_REQUIRED = 1;
    /** */
    public static final short DEFAULT_TYPE_IMPLIED  = 2;
    /** */
    public static final short DEFAULT_TYPE_FIXED    = 3;
    
    /** */
    public static final String[] NAMED_TYPE_LIST = new String [] {
        "CDATA",     // TYPE_CDATA // NOI18N
        "ID",        // TYPE_ID // NOI18N
        "IDREF",     // TYPE_IDREF // NOI18N
        "IDREFS",    // TYPE_IDREFS // NOI18N
        "ENTITY",    // TYPE_ENTITY // NOI18N
        "ENTITIES",  // TYPE_ENTITIES // NOI18N
        "NMTOKEN",   // TYPE_NMTOKEN // NOI18N
        "NMTOKENS",  // TYPE_NMTOKENS // NOI18N
        null,        // TYPE_ENUMERATED
        "NOTATION"   // TYPE_NOTATION // NOI18N
    };
    
    /** */
    public static final String[] NAMED_DEFAULT_TYPE_LIST = new String [] {
        null,        // DEFAULT_TYPE_NULL
        "#REQUIRED", // DEFAULT_TYPE_REQUIRED // NOI18N
        "#IMPLIED",  // DEFAULT_TYPE_IMPLIED // NOI18N
        "#FIXED"     // DEFAULT_TYPE_FIXED // NOI18N
    };
    
    
    /** */
    private String name;
    
    /** */
    private short type;
    
    /** */
    private String[] enumeratedType;
    
    /** */
    private short defaultType;
    
    /** */
    private String defaultValue;
    
    /** */
    private TreeNamedObjectMap.KeyListener mapKeyListener;
    
    
    //
    // init
    //
    
    /** Creates new TreeAttlistDeclAttributeDef. */
    public TreeAttlistDeclAttributeDef (String name, short type, String[] enumeratedType, short defaultType, String defaultValue) throws InvalidArgumentException {
        super ();
        
        checkName (name);
        checkType (type, enumeratedType);
        checkDefaultType (defaultType, defaultValue);
        this.name           = name;
        this.type           = type;
        this.enumeratedType = enumeratedType;
        this.defaultType    = defaultType;
        this.defaultValue   = defaultValue;
    }
    
    /** Creates new TreeAttlistDeclAttributeDef -- copy constructor. */
    protected TreeAttlistDeclAttributeDef (TreeAttlistDeclAttributeDef attributeDef) {
        super (attributeDef);
        
        this.name           = attributeDef.name;
        this.type           = attributeDef.type;
        this.enumeratedType = arraycopy (attributeDef.enumeratedType);
        this.defaultType    = attributeDef.defaultType;
        this.defaultValue   = attributeDef.defaultValue;
    }
    
    
    //
    // itself
    //
    
    /**
     */
    private String[] arraycopy (String[] array) {
        if ( array == null )
            return null;
        
        int length = array.length;
        String[] arrayCopy = new String [length];
        System.arraycopy (array, 0, arrayCopy, 0, length);
        
        return arrayCopy;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeAttlistDeclAttributeDef (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeAttlistDeclAttributeDef peer = (TreeAttlistDeclAttributeDef) object;
        if (!!! Util.equals (this.name, peer.name))
            return false;
        if (this.type != peer.type)
            return false;
        if (!!! Util.equals (this.enumeratedType, peer.enumeratedType))
            return false;
        if (this.defaultType != peer.defaultType)
            return false;
        if (!!! Util.equals (this.defaultValue, peer.defaultValue))
            return false;
        
        return true;
    }
    
    /*
     * Merge name, type, enumeration, default type and default value properties.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeAttlistDeclAttributeDef peer = (TreeAttlistDeclAttributeDef) treeObject;
        setNameImpl (peer.getName ());
        setDefaultTypeImpl (peer.getDefaultType (), peer.getDefaultValue ());
        setTypeImpl (peer.getType (), peer.getEnumeratedType ());
    }
    
    
    //
    // context
    //
    
    /**
     */
    public final void removeFromContext () throws ReadOnlyException {
        if ( isInContext () ) {
            getOwnerAttlistDecl ().removeAttributeDef (this.getName ());
        }
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final TreeAttlistDecl getOwnerAttlistDecl () {
        return (TreeAttlistDecl)getNodeDecl ();
    }
    
    
    //
    // name
    //
    
    /**
     */
    public final String getElementName () {
        if ( getNodeDecl () == null )
            return null;
        return getOwnerAttlistDecl ().getElementName ();
    }
    
    /**
     */
    public final String getName () {
        return name;
    }
    
    /**
     */
    private final void setNameImpl (String newName) {
        String oldName = this.name;
        
        this.name = newName;
        
        fireMapKeyChanged (oldName);
//         firePropertyChange (PROP_NAME, oldName, newName);
//         getNodeDecl().firePropertyChange (TreeAttlistDecl.PROP_ATTRIBUTE_DEF_MAP_CONTENT, this, this); //!!!
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setName (String newName) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.name, newName) )
            return;
        checkReadOnly ();
        checkName (newName);
        
        //
        // set new value
        //
        setNameImpl (newName);
    }
    
    /**
     */
    protected final void checkName (String name) throws InvalidArgumentException {
        TreeUtilities.checkAttlistDeclAttributeName (name);
    }
    
    
    //
    // type
    //
    
    /**
     */
    public final short getType () {
        return type;
    }
    
    /**
     */
    public final String getTypeName () {
        try {
            return NAMED_TYPE_LIST [type];
        } catch (ArrayIndexOutOfBoundsException exc) {
            return null;
        }
    }
    
    /**
     */
    public final String[] getEnumeratedType () {
        return enumeratedType;
    }
    
    /**
     */
    public final String getEnumeratedTypeString () {
        if ( enumeratedType == null ) {
            return null;
        }
        StringBuffer sb = new StringBuffer ();
        sb.append ("( ").append (enumeratedType[0]); // NOI18N
        for ( int i = 1; i < enumeratedType.length; i++ ) {
            sb.append (" | ").append (enumeratedType [i]); // NOI18N
        }
        sb.append (" )"); // NOI18N
        return sb.toString ();
    }
    
    public static final String[] createEnumeratedType (String enumeratedType) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeAttlistDeclAttributeDef.createEnumeratedType: enumeratedType = " + enumeratedType);

        if ( enumeratedType == null ) {
            return null;
        }
        int begin = enumeratedType.indexOf ("("); // NOI18N
        int end = enumeratedType.indexOf (")"); // NOI18N

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    begin = " + begin);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    end   = " + end);

        if ( ( begin == -1 ) ||
             ( end   == -1 ) ) {
            return null;
        }
        String noParenthesis = enumeratedType.substring (begin + 1, end);
        StringTokenizer st = new StringTokenizer (noParenthesis, "|"); // NOI18N
        List tokens = new LinkedList ();
        while (st.hasMoreTokens ()) {
            tokens.add (st.nextToken ().trim ());
        }

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    tokens = " + tokens);

        if ( tokens.isEmpty () )
            return null;

        String[] arrayType = (String[])tokens.toArray (new String[0]);

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    RETURN arrayType = " + arrayType);

        return arrayType;
    }
    
    /**
     */
    private final void setTypeImpl (short newType, String[] newEnumeratedType) {
//         short    oldType           = this.type;
//         String[] oldEnumeratedType = this.enumeratedType;
        
        this.type           = newType;
        this.enumeratedType = newEnumeratedType;
        
//         firePropertyChange (PROP_???, old???, new???);
//         getNodeDecl().firePropertyChange (TreeAttlistDecl.PROP_ATTRIBUTE_DEF_MAP_CONTENT, this, this); //!!!
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setType (short newType, String[] newEnumeratedType) throws ReadOnlyException, InvalidArgumentException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeAttlistDeclAttributeDef.setType");
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    newType           = " + newType);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    newEnumeratedType = " + ( newEnumeratedType == null ? null : Arrays.asList (newEnumeratedType)));

        //
        // check new value
        //
        boolean setType           = this.type != newType;
        boolean setEnumeratedType = !!! Arrays.equals (this.enumeratedType, newEnumeratedType);

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    setEnumeratedType = " + setEnumeratedType);

        if ( !!! setType &&
             !!! setEnumeratedType ) {
            return;
        }
        checkReadOnly ();
        checkType (newType, newEnumeratedType);
        
        //
        // set new value
        //
        setTypeImpl (newType, newEnumeratedType);
    }
    
    
    /**
     */
    protected final void checkType (short type, String[] enumeratedType) throws InvalidArgumentException {
        TreeUtilities.checkAttlistDeclAttributeType (type);
        TreeUtilities.checkAttlistDeclAttributeEnumeratedType (enumeratedType);
    }
    
    /**
     */
    public static final short findType (String type) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeAttlistDeclAttributeDef::findType: type = " + type); // NOI18N

        for ( short i = 0; i < NAMED_TYPE_LIST.length; i++ ) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    test_type = " + NAMED_TYPE_LIST[i]); // NOI18N

            if ( Util.equals (NAMED_TYPE_LIST[i], type) ) {
                return i;
            }
        }
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    type[" + type + "] not found"); // NOI18N

        return -1;
    }
    
    //
    // default decl
    //
    
    /**
     */
    public final short getDefaultType () {
        return defaultType;
    }
    
    /**
     */
    public final String getDefaultTypeName () {
        try {
            return NAMED_DEFAULT_TYPE_LIST [defaultType];
        } catch (ArrayIndexOutOfBoundsException exc) {
            return null;
        }
    }
    
    /**
     */
    public final String getDefaultValue () {
        return defaultValue;
    }
    
    /**
     */
    private final void setDefaultTypeImpl (short newDefaultType, String newDefaultValue) {
        //  	    short  oldDefaultType  = this.defaultType;
        //  	    String oldDefaultValue = this.defaultValue;
        
        this.defaultType  = newDefaultType;
        this.defaultValue = newDefaultValue;
        
//         firePropertyChange (PROP_???, old???, new???);
//         getNodeDecl().firePropertyChange (TreeAttlistDecl.PROP_ATTRIBUTE_DEF_MAP_CONTENT, this, this); //!!!
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setDefaultType (short newDefaultType, String newDefaultValue) throws ReadOnlyException, InvalidArgumentException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeAttlistDeclAttributeDef.setDefaultType");
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    newDefaultType  = " + NAMED_DEFAULT_TYPE_LIST [newDefaultType]);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    newDefaultValue = " + newDefaultValue);

        //
        // check new value
        //
        boolean setDefaultType  = this.defaultType != newDefaultType;
        boolean setDefaultValue = !!! Util.equals (this.defaultValue, newDefaultValue);
        if ( !!! setDefaultType &&
             !!! setDefaultValue ) {
            return;
        }
        checkReadOnly ();
        checkDefaultType (newDefaultType, newDefaultValue);
        
        //
        // set new value
        //
        setDefaultTypeImpl (newDefaultType, newDefaultValue);
    }
    
    /**
     */
    protected final void checkDefaultType (short defaultType, String defaultValue) throws InvalidArgumentException {
        TreeUtilities.checkAttlistDeclAttributeDefaultType (defaultType);
        TreeUtilities.checkAttlistDeclAttributeDefaultValue (defaultValue);
    }
    
    /**
     */
    public static final short findDefaultType (String defaultType) {
        for ( short i = 0; i < NAMED_DEFAULT_TYPE_LIST.length; i++ ) {
            if ( Util.equals (NAMED_DEFAULT_TYPE_LIST[i], defaultType) )
                return i;
        }
        return -1;
    }
    
    
    //
    // TreeNamedObjectMap.NamedObject
    //
    
    /**
     */
    public Object mapKey () {
        return getName ();
    }
    
    /**
     */
    //    public String mapKeyPropertyName () {
    //        return PROP_NAME;
    //    }
    
    /** Attach NamedObject to NamedObject Map.  */
    public void setKeyListener (TreeNamedObjectMap.KeyListener keyListener) {
        mapKeyListener = keyListener;
    }
    
    private void fireMapKeyChanged (Object oldKey) {
        if ( mapKeyListener == null ) {
            return;
        }
        mapKeyListener.mapKeyChanged (oldKey);
    }
    
}
