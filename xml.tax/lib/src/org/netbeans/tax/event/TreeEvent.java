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
package org.netbeans.tax.event;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeObject;
import org.netbeans.tax.TreeNode;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeEvent extends PropertyChangeEvent {
    // toDo:
    // + implement *_PHASE type of event.

    /** Serial Version UID */
    private static final long serialVersionUID =-4899604850035092773L;

    /** */
    private boolean bubbling;

    /** */
    private TreeObject originalSource; // make sense in bubbling phase

    /** */
    private String originalPropertyName;
    
    
    // make sense in bubbling phase
    
    
    //
    // init
    //
    
    /** Creates new TreeEvent. */
    private TreeEvent (TreeObject source, String propertyName, Object oldValue, Object newValue, TreeObject originalSource, String originalPropertyName) {
        super (source, propertyName, oldValue, newValue);
        
        this.bubbling             = ( originalSource != null );
        this.originalSource       = originalSource;
        this.originalPropertyName = originalPropertyName;
    }
    
    /** Creates new TreeEvent. */
    public TreeEvent (TreeObject source, String propertyName, Object oldValue, Object newValue) {
        this (source, propertyName, oldValue, newValue, null, null);
    }
    
    
    //
    // itself
    //
    
    /**
     * Used to indicate whether or not an event is a bubbling event. If the
     * event can bubble the value is true, else the value is false.
     */
    public final boolean isBubbling () {
        return bubbling;
    }
    
    /**
     */
    public final TreeObject getOriginalSource () {
        return originalSource;
    }
    
    /**
     */
    public final String getOriginalPropertyName () {
        return originalPropertyName;
    }
    
    /**
     */
    public final TreeEvent createBubbling (TreeNode currentNode) {
        return new TreeEvent (currentNode, TreeNode.PROP_NODE, null, null, (TreeObject)this.getSource (), this.getPropertyName ());
    }
    
}
