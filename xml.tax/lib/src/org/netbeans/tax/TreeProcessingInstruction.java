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
