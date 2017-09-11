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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;


/**
 * DOM based implementation
 * @author Chris Webster
 * @author Vidhya Narayanan
 */
public class UnionImpl extends SchemaComponentImpl implements Union {
    protected UnionImpl(SchemaModelImpl model){
        this(model, createNewComponent(SchemaElements.UNION, model));
    }
    
    public UnionImpl(SchemaModelImpl model,Element el){
        super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Union.class;
	}
    
    public java.util.List<NamedComponentReference<GlobalSimpleType>> getMemberTypes() {
        
        String val = getAttribute(SchemaAttributes.MEMBER_TYPES);
	if (val == null) {
	    return null;
	}
        List<NamedComponentReference<GlobalSimpleType>> gsts = new ArrayList<NamedComponentReference<GlobalSimpleType>>();
        if (val.trim().length()==0) return gsts;
        String[] ss = val.split("( |\t|\n|\r|\f)+");
        for(int i = 0; i < ss.length; i++){
            NamedComponentReference<GlobalSimpleType> ref =
                    new GlobalReferenceImpl(GlobalSimpleType.class, this, ss[i]);
            gsts.add(ref);
        }
        return gsts;
        
    }
    
    public void removeMemberType(NamedComponentReference<GlobalSimpleType> gst) {
        String refVal = getPrefixedName(gst.getEffectiveNamespace(),
                gst.get().getName());
        String val = getAttribute(SchemaAttributes.MEMBER_TYPES);
        StringBuffer sb = new StringBuffer();
        if (val != null) {
            String[] ss = val.split("( |\t|\n|\r|\f)+");
            boolean first = true;
            for (String s : ss) {
                if (!s.equals(refVal)) {
                    if (!first)
                        sb.append(" ");
                    else
                        first = false;
                    sb.append(s);
                }
            }
        }
        setAttribute(MEMBER_TYPES_PROPERTY, SchemaAttributes.MEMBER_TYPES, sb.length()==0?null:sb.toString());
    }
    
    public void addMemberType(NamedComponentReference<GlobalSimpleType> gst) {
        String val = getAttribute(SchemaAttributes.MEMBER_TYPES);
        String refVal = getPrefixedName(gst.getEffectiveNamespace(),
                gst.get().getName());
        if (val == null)
            val = refVal;
        else
            val = val.concat(" ").concat(refVal);
        setAttribute(MEMBER_TYPES_PROPERTY, SchemaAttributes.MEMBER_TYPES, val);
    }

    public void setMemberTypes(java.util.List<NamedComponentReference<GlobalSimpleType>> types) {
        String val = types == null ? null : getRefsString(types);
        setAttribute(MEMBER_TYPES_PROPERTY, SchemaAttributes.MEMBER_TYPES, val);
    }

    private String getRefsString(java.util.List<NamedComponentReference<GlobalSimpleType>> types) {
        StringBuilder refVal = new StringBuilder();
        for (NamedComponentReference<GlobalSimpleType> ref : types) {
            refVal.append(ref.getRefString());
            refVal.append(" ");
        }
        return refVal.toString().trim();
    }
    
     public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public void removeInlineType(LocalSimpleType type) {
        removeChild(INLINE_TYPE_PROPERTY, type);
    }
    
    public void addInlineType(LocalSimpleType type) {
        appendChild(INLINE_TYPE_PROPERTY, type);
    }
      
    public java.util.Collection<LocalSimpleType> getInlineTypes() {
        return getChildren(LocalSimpleType.class);
    }
}
