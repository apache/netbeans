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
