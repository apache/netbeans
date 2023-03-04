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

import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.ConditionalSection;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeEntityDecl extends TreeNodeDecl implements DTD.Child, ParameterEntityReference.Child, DocumentType.Child, ConditionalSection.Child {
    /** */
    public static final String PROP_PARAMETER     = "parameter"; // NOI18N
    /** */
    public static final String PROP_NAME          = "name"; // NOI18N
    /** */
    public static final String PROP_TYPE          = "type"; // NOI18N
    /** */
    public static final String PROP_INTERNAL_TEXT = "internalText"; // NOI18N
    /** */
    public static final String PROP_PUBLIC_ID     = "publicId"; // NOI18N
    /** */
    public static final String PROP_SYSTEM_ID     = "systemId"; // NOI18N
    /** */
    public static final String PROP_NOTATION_NAME = "notationName"; // NOI18N
    
    /** */
    public static final short TYPE_INTERNAL = 1;
    /** */
    public static final short TYPE_EXTERNAL = 2;
    /** */
    public static final short TYPE_UNPARSED = 3;
    
    /** */
    public static final boolean GENERAL_DECL   = false;
    /** */
    public static final boolean PARAMETER_DECL = true;
    
    
    /** */
    private boolean parameter;
    
    /** */
    private String name;
    
    /** */
    private short type;
    
    /** -- can be null. */
    private String internalText;
    
    /** -- can be null. */
    private String publicId;
    
    /** -- can be null. */
    private String systemId;
    
    /** -- can be null. */
    private String notationName;
    
    
    //
    // init
    //
    
    /** Creates new TreeEntityDecl.
     * @throws InvalidArgumentException
     */
    private TreeEntityDecl (boolean parameter, String name) throws InvalidArgumentException {
        super ();
        
        checkName (name);
        this.name      = name;
        this.parameter = parameter;
    }
    
    
    /** Creates new TreeEntityDecl.
     * @throws InvalidArgumentException
     */
    public TreeEntityDecl (boolean parameter, String name, String internalText) throws InvalidArgumentException {
        this (parameter, name);
        
        checkInternalText (internalText);
        this.type         = TYPE_INTERNAL;
        this.internalText = internalText;
        this.publicId     = null;
        this.systemId     = null;
        this.notationName = null;
    }
    
    
    /** Creates new TreeEntityDecl.
     * @throws InvalidArgumentException
     */
    public TreeEntityDecl (String name, String internalText) throws InvalidArgumentException {
        this (GENERAL_DECL, name, internalText);
    }
    
    
    /** Creates new TreeEntityDecl.
     * @throws InvalidArgumentException
     */
    public TreeEntityDecl (boolean parameter, String name, String publicId, String systemId) throws InvalidArgumentException {
        this (parameter, name);
        
        checkExternalDecl (publicId, systemId);
        this.type         = TYPE_EXTERNAL;
        this.internalText = null;
        this.publicId     = publicId;
        this.systemId     = systemId;
        this.notationName = null;
    }
    
    
    /** Creates new TreeEntityDecl.
     * @throws InvalidArgumentException
     */
    public TreeEntityDecl (String name, String publicId, String systemId) throws InvalidArgumentException {
        this (GENERAL_DECL, name, publicId, systemId);
    }
    
    
    /** Creates new TreeEntityDecl.
     * @throws InvalidArgumentException
     */
    public TreeEntityDecl (String name, String publicId, String systemId, String notationName) throws InvalidArgumentException {
        this (GENERAL_DECL, name);
        
        checkUnparsedDecl (publicId, systemId, notationName);
        
        this.type         = TYPE_UNPARSED;
        this.internalText = null;
        this.publicId     = publicId;
        this.systemId     = systemId;
        this.notationName = notationName;
    }
    
    
    
    /** Creates new TreeEntityDecl -- copy constructor. */
    protected TreeEntityDecl (TreeEntityDecl entityDecl) {
        super (entityDecl);
        
        this.parameter    = entityDecl.parameter;
        this.name         = entityDecl.name;
        this.type         = entityDecl.type;
        this.internalText = entityDecl.internalText;
        this.publicId     = entityDecl.publicId;
        this.systemId     = entityDecl.systemId;
        this.notationName = entityDecl.notationName;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeEntityDecl (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeEntityDecl peer = (TreeEntityDecl) object;
        if (!!! Util.equals (this.getName (), peer.getName ()))
            return false;
        if ( this.isParameter () != peer.isParameter ())
            return false;
        if ( this.getType () != peer.getType ())
            return false;
        if (!!! Util.equals (this.getPublicId (), peer.getPublicId ()))
            return false;
        if (!!! Util.equals (this.getSystemId (), peer.getSystemId ()))
            return false;
        if (!!! Util.equals (this.getInternalText (), peer.getInternalText ()))
            return false;
        if (!!! Util.equals (this.getNotationName (), peer.getNotationName ()))
            return false;
        
        return true;
    }
    
    /*
     * Wisely according peer type merge relevant properties.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeEntityDecl peer = (TreeEntityDecl) treeObject;
        
        setNameImpl (peer.getName ());
        setParameterImpl (peer.isParameter ());
        
        short peerType = peer.getType ();
        switch (peerType) {
            case TYPE_EXTERNAL:
                setExternalDeclImpl (peer.getPublicId (), peer.getSystemId ());
                break;
            case TYPE_INTERNAL:
                setInternalTextImpl (peer.getInternalText ());
                break;
            case TYPE_UNPARSED:
                setUnparsedDeclImpl (peer.getPublicId (), peer.getSystemId (), peer.getNotationName ());
                break;
        }
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final boolean isParameter () {
        return parameter;
    }
    
    /**
     */
    private final void setParameterImpl (boolean newParameter) {
        boolean oldParameter = this.parameter;
        
        this.parameter = newParameter;
        
        firePropertyChange (PROP_PARAMETER, oldParameter ? Boolean.TRUE : Boolean.FALSE, newParameter ? Boolean.TRUE : Boolean.FALSE);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidStateException
     * @throws InvalidArgumentException
     */
    public final void setParameter (boolean newParameter) throws ReadOnlyException, InvalidStateException, InvalidArgumentException {
        //
        // check new value
        //
        if ( this.parameter == newParameter )
            return;
        checkReadOnly ();
        if ( (newParameter == PARAMETER_DECL) && (type == TYPE_UNPARSED) ) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_ted_parameter_unparsed"));
        }
        
        //
        // set new value
        //
        setParameterImpl (newParameter);
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
        
        firePropertyChange (PROP_NAME, oldName, newName);
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
        TreeUtilities.checkEntityDeclName (name);
    }
    
    /**
     */
    public final short getType () {
        return type;
    }
    
    /**
     */
    public final String getInternalText () {
        return internalText;
    }
    
    /**
     */
    private final void setInternalTextImpl (String newInternalText) {
        short  oldType         = this.type;
        String oldInternalText = this.internalText;
        String oldPublicId     = this.publicId;
        String oldSystemId     = this.systemId;
        String oldNotationName = this.notationName;
        
        this.type         = TYPE_INTERNAL;
        this.internalText = newInternalText;
        this.publicId     = null;
        this.systemId     = null;
        this.notationName = null;
        
        firePropertyChange (PROP_TYPE, oldType, this.type);
        firePropertyChange (PROP_INTERNAL_TEXT, oldInternalText,     newInternalText);
        firePropertyChange (PROP_PUBLIC_ID,     oldPublicId,         this.publicId);
        firePropertyChange (PROP_SYSTEM_ID,     oldSystemId,         this.systemId);
        firePropertyChange (PROP_NOTATION_NAME, oldNotationName,     this.notationName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setInternalText (String newInternalText) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.internalText, newInternalText) )
            return;
        checkReadOnly ();
        checkInternalText (newInternalText);
        
        //
        // set new value
        //
        setInternalTextImpl (newInternalText);
    }
    
    /**
     */
    protected final void checkInternalText (String internalText) throws InvalidArgumentException {
        TreeUtilities.checkEntityDeclInternalText (internalText);
    }
    
    /**
     */
    public final String getPublicId () {
        return publicId;
    }
    
    /**
     */
    private final void setPublicIdImpl (String newPublicId) {
        String oldPublicId = this.publicId;
        
        this.publicId = newPublicId;
        
        firePropertyChange (PROP_PUBLIC_ID, oldPublicId, newPublicId);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidStateException
     * @throws InvalidArgumentException
     */
    public final void setPublicId (String newPublicId) throws ReadOnlyException, InvalidStateException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.publicId, newPublicId) )
            return;
        checkReadOnly ();
        if ( type == TYPE_INTERNAL ) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_ted_internal_public"));
        }
        checkPublicId (newPublicId);
        
        //
        // set new value
        //
        setPublicIdImpl (newPublicId);
    }
    
    /**
     */
    protected final void checkPublicId (String publicId) throws InvalidArgumentException {
        TreeUtilities.checkEntityDeclPublicId (publicId);
        
        checkExternalId (publicId, this.systemId);
    }
    
    /**
     */
    public final String getSystemId () {
        return systemId;
    }
    
    /**
     */
    private final void setSystemIdImpl (String newSystemId) {
        String oldSystemId = this.systemId;
        
        this.systemId = newSystemId;
        
        firePropertyChange (PROP_SYSTEM_ID, oldSystemId, newSystemId);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidStateException
     * @throws InvalidArgumentException
     */
    public final void setSystemId (String newSystemId) throws ReadOnlyException, InvalidStateException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.systemId, newSystemId) )
            return;
        checkReadOnly ();
        if ( type == TYPE_INTERNAL ) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_ted_internal_system"));
        }
        checkSystemId (newSystemId);
        
        //
        // set new value
        //
        setSystemIdImpl (newSystemId);
    }
    
    /**
     */
    protected final void checkSystemId (String systemId) throws InvalidArgumentException {
        TreeUtilities.checkEntityDeclSystemId (systemId);
        
        checkExternalId (this.publicId, systemId);
    }
    
    
    /**
     */
    private final void setExternalDeclImpl (String newPublicId, String newSystemId) {
        short  oldType         = this.type;
        String oldInternalText = this.internalText;
        String oldPublicId     = this.publicId;
        String oldSystemId     = this.systemId;
        String oldNotationName = this.notationName;
        
        this.type         = TYPE_EXTERNAL;
        this.internalText = null;
        this.publicId     = newPublicId;
        this.systemId     = newSystemId;
        this.notationName = null;
        
        firePropertyChange (PROP_TYPE, oldType, this.type);
        firePropertyChange (PROP_INTERNAL_TEXT, oldInternalText,     this.internalText);
        firePropertyChange (PROP_PUBLIC_ID,     oldPublicId,         newPublicId);
        firePropertyChange (PROP_SYSTEM_ID,     oldSystemId,         newSystemId);
        firePropertyChange (PROP_NOTATION_NAME, oldNotationName,     this.notationName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setExternalDecl (String newPublicId, String newSystemId) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        boolean setPublicId     = !!! Util.equals (this.publicId, newPublicId);
        boolean setSystemId     = !!! Util.equals (this.systemId, newSystemId);
        if ( !!! setPublicId &&
             !!! setSystemId ) {
            return;
        }
        checkReadOnly ();
        checkExternalDecl (newPublicId, newSystemId);
        
        //
        // set new value
        //
        setExternalDeclImpl (newPublicId, newSystemId);
    }
    
    /**
     */
    protected final void checkExternalDecl (String publicId, String systemId) throws InvalidArgumentException {
        TreeUtilities.checkEntityDeclPublicId (publicId);
        TreeUtilities.checkEntityDeclSystemId (systemId);
        
        checkExternalId (publicId, systemId);
    }
    
    /**
     */
    public final String getNotationName () {
        return notationName;
    }
    
    /**
     */
    private final void setNotationNameImpl (String newNotationName) {
        short  oldType         = this.type;
        String oldNotationName = this.notationName;
        
        if ( newNotationName == null ) {
            this.type = TYPE_EXTERNAL;
        } else {
            this.type = TYPE_UNPARSED;
        }
        this.notationName = newNotationName;
        
        firePropertyChange (PROP_TYPE, oldType, this.type);
        firePropertyChange (PROP_NOTATION_NAME, oldNotationName,     newNotationName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidStateException
     * @throws InvalidArgumentException
     */
    public final void setNotationName (String newNotationName) throws ReadOnlyException, InvalidStateException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.notationName, newNotationName) )
            return;
        checkReadOnly ();
        if ( type == TYPE_INTERNAL ) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_internal_notation"));
        }
        if ( parameter == PARAMETER_DECL ) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_ted_parameter_unparsed"));
        }
        checkNotationName (newNotationName);
        
        //
        // set new value
        //
        setNotationNameImpl (newNotationName);
    }
    
    /**
     */
    protected final void checkNotationName (String notationName) throws InvalidArgumentException {
        TreeUtilities.checkEntityDeclNotationName (notationName);
    }
    
    /**
     */
    private final void setUnparsedDeclImpl (String newPublicId, String newSystemId, String newNotationName) {
        short  oldType         = this.type;
        String oldInternalText = this.internalText;
        String oldPublicId     = this.publicId;
        String oldSystemId     = this.systemId;
        String oldNotationName = this.notationName;
        
        this.type         = TYPE_UNPARSED;
        this.internalText = null;
        this.publicId     = newPublicId;
        this.systemId     = newSystemId;
        this.notationName = newNotationName;
        
        firePropertyChange (PROP_TYPE, oldType, this.type);
        firePropertyChange (PROP_INTERNAL_TEXT, oldInternalText,     this.internalText);
        firePropertyChange (PROP_PUBLIC_ID,     oldPublicId,         newPublicId);
        firePropertyChange (PROP_SYSTEM_ID,     oldSystemId,         newSystemId);
        firePropertyChange (PROP_NOTATION_NAME, oldNotationName,     newNotationName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidStateException
     * @throws InvalidArgumentException
     */
    public final void setUnparsedDecl (String newPublicId, String newSystemId, String newNotationName) throws ReadOnlyException, InvalidStateException, InvalidArgumentException {
        //
        // check new value
        //
        boolean setPublicId     = !!! Util.equals (this.publicId, newPublicId);
        boolean setSystemId     = !!! Util.equals (this.systemId, newSystemId);
        boolean setNotationName = !!! Util.equals (this.notationName, newNotationName);
        if ( !!! setPublicId &&
             !!! setSystemId &&
             !!! setNotationName ) {
            return;
        }
        checkReadOnly ();
        if ( parameter == PARAMETER_DECL ) {
            throw new InvalidStateException (Util.THIS.getString ("EXC_ted_parameter_unparsed"));
        }
        checkUnparsedDecl (newPublicId, newSystemId, newNotationName);
        
        //
        // set new value
        //
        setUnparsedDeclImpl (newPublicId, newSystemId, newNotationName);
    }
    
    /**
     */
    protected final void checkUnparsedDecl (String publicId, String systemId, String notationName) throws InvalidArgumentException {
        TreeUtilities.checkEntityDeclPublicId (publicId);
        TreeUtilities.checkEntityDeclSystemId (systemId);
        
        checkExternalId (publicId, systemId);
        
        TreeUtilities.checkEntityDeclNotationName (notationName);
        if ( notationName == null ) {
            throw new InvalidArgumentException (Util.THIS.getString ("EXC_ted_unparsed_must_notation"),
            new NullPointerException ());
        }
    }
    
    
    /**
     */
    protected final void checkExternalId (String publicId, String systemId) throws InvalidArgumentException {
        if ( systemId == null ) {
            if ( publicId == null ) {
                throw new InvalidArgumentException (Util.THIS.getString ("EXC_ted_system_required"),
                new NullPointerException ());
            } else {
                throw new InvalidArgumentException (Util.THIS.getString ("EXC_ted_system_required"),
                new NullPointerException ());
            }
        }
    }
    
}
