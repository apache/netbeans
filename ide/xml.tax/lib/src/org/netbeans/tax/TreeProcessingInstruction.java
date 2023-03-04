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

import org.netbeans.tax.spec.Document;
import org.netbeans.tax.spec.DocumentFragment;
import org.netbeans.tax.spec.Element;
import org.netbeans.tax.spec.GeneralEntityReference;
import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.ConditionalSection;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeProcessingInstruction extends TreeData implements Document.Child, DocumentFragment.Child, Element.Child, GeneralEntityReference.Child, DTD.Child, ParameterEntityReference.Child, DocumentType.Child, ConditionalSection.Child {
    /** */
    public static final String PROP_TARGET = "target"; // NOI18N
    
    /** */
    private String target;
    
    
    //
    // init
    //
    
    /** Creates new TreeProcessingInstruction.
     * @throws InvalidArgumentException
     */
    public TreeProcessingInstruction (String target, String data) throws InvalidArgumentException {
        super (data);
        
        checkTarget (target);
        this.target = target;
    }
    
    
    /** Creates new TreeProcessingInstruction -- copy constructor. */
    protected TreeProcessingInstruction (TreeProcessingInstruction processingInstruction) {
        super (processingInstruction);
        
        this.target = processingInstruction.target;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeProcessingInstruction (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeProcessingInstruction peer = (TreeProcessingInstruction) object;
        if (!!! Util.equals (this.getTarget (), peer.getTarget ())) {
            return false;
        }
        
        return true;
    }
    
    /*
     * Merges target property.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeProcessingInstruction peer = (TreeProcessingInstruction) treeObject;
        setTargetImpl (peer.getTarget ());
    }
    
    
    
    //
    // from TreeData
    //
    
    /**
     */
    protected final void checkData (String data) throws InvalidArgumentException {
        TreeUtilities.checkProcessingInstructionData (data);
    }
    
    /**
     * @throws InvalidArgumentException
     */
    protected TreeData createData (String data) throws InvalidArgumentException {
        return new TreeProcessingInstruction (this.target, data);
    }
    
    //
    // itself
    //
    
    /**
     */
    public final String getTarget () {
        return target;
    }
    
    /**
     */
    private final void setTargetImpl (String newTarget) {
        String oldTarget = this.target;
        
        this.target = newTarget;
        
        firePropertyChange (PROP_TARGET, oldTarget, newTarget);
    }
    
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setTarget (String newTarget) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.target, newTarget) )
            return;
        checkReadOnly ();
        checkTarget (newTarget);
        
        //
        // set new value
        //
        setTargetImpl (newTarget);
    }
    
    
    /**
     */
    public final void checkTarget (String target) throws InvalidArgumentException {
        TreeUtilities.checkProcessingInstructionTarget (target);
    }
    
}
