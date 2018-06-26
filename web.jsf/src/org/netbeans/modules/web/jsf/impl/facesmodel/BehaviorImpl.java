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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.BehaviorExtension;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class BehaviorImpl extends DescriptionGroupImpl implements FacesBehavior {

    BehaviorImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    BehaviorImpl( JSFConfigModelImpl model ) {
        this(model, createElementNS(model, JSFConfigQNames.BEHAVIOR));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior#addBehaviorExtension(org.netbeans.modules.web.jsf.api.facesmodel.BehaviorExtension)
     */
    public void addBehaviorExtension( BehaviorExtension extension ) {
        appendChild( BEHAVIOR_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior#addBehaviorExtension(int, org.netbeans.modules.web.jsf.api.facesmodel.BehaviorExtension)
     */
    public void addBehaviorExtension( int index, BehaviorExtension extension ) {
        insertAtIndex( BEHAVIOR_EXTENSION,  extension, index, BehaviorExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior#getBehaviorExtensions()
     */
    public List<BehaviorExtension> getBehaviorExtensions() {
        return getChildren( BehaviorExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior#removeBehaviorExtension(org.netbeans.modules.web.jsf.api.facesmodel.BehaviorExtension)
     */
    public void removeBehaviorExtension( BehaviorExtension extension ) {
        removeChild( BEHAVIOR_EXTENSION,extension );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior#setBehaviorClass(java.lang.String)
     */
    public void setBehaviorClass( String clazz ) {
        setChildElementText(BEHAVIOR_CLASS, clazz, 
                JSFConfigQNames.BEHAVIOR_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesBehavior#setBehaviorId(java.lang.String)
     */
    public void setBehaviorId( String id ) {
        setChildElementText(BEHAVIOR_ID, id, 
                JSFConfigQNames.BEHAVIOR_ID.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit(this );
    }

    /**
     * Gets behavior-class of the faces-config-behaviorType.
     * @return trimmed behavior-class if any, {@code null} otherwise
     */
    public String getBehaviorClass() {
        String behaviorClass = getChildElementText(JSFConfigQNames.BEHAVIOR_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(behaviorClass);
    }

    /**
     * Gets behavior-id of the faces-config-behaviorType.
     * @return trimmed behavior-id if any, {@code null} otherwise
     */
    public String getBehaviorId() {
        String behaviorId = getChildElementText(JSFConfigQNames.BEHAVIOR_ID.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(behaviorId);
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(
            DESCRIPTION_GROUP_SORTED_ELEMENTS.size() + 5 );
    
    static {
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS);
        SORTED_ELEMENTS.add( BEHAVIOR_ID);
        SORTED_ELEMENTS.add( BEHAVIOR_CLASS );  
        SORTED_ELEMENTS.add( ATTRIBUTE );
        SORTED_ELEMENTS.add( PROPERTY );
        SORTED_ELEMENTS.add( BEHAVIOR_EXTENSION );
    }

}
