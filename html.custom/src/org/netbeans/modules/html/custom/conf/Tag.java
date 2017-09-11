/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.custom.conf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author marek
 */
public class Tag extends Element {
    
    /* attribute name to instance map*/
    private final Map<String, Attribute> attrsMap = new HashMap<>();
    /* tag name to instance map*/
    private final Map<String, Tag> childrenMap = new HashMap<>();

    public Tag(String name) {
        this(name, null, null, null, null, false);
    }
    
    public Tag(String name, String description, String documentation, String documentationURL, Tag parent, boolean required, String... contexts) {
        super(name, description, documentation, documentationURL, parent, required, contexts);
    }

    public void setAttributes(Collection<Attribute> attributes) {
        for(Attribute a : attributes) {
            this.attrsMap.put(a.getName(), a);
        }
    }
    
     /**
     * Gets a collection of the attributes registered to the root context.
     *
     * @return
     */
    public Collection<String> getAttributesNames() {
        return attrsMap.keySet();
    }
    
    public Collection<Attribute> getAttributes() {
        return attrsMap.values();
    }
    
    public Attribute getAttribute(String name) {
        return attrsMap.get(name);
    }

    public void add(Attribute a) {
        attrsMap.put(a.getName(), a);
    }

    public void remove(Attribute a) {
        attrsMap.remove(a.getName());
    }

    public void setChildren(Collection<Tag> children) {
        for(Tag t : children) {
            childrenMap.put(t.getName(), t);
        }
    }

       /**
     * Gets a collection of the tags registered to the root context.
     *
     * @return
     */
    public Collection<String> getTagsNames() {
        return childrenMap.keySet();
    }

    public Collection<Tag> getTags() {
        return childrenMap.values();
    }
    
    public Tag getTag(String tagName) {
        return childrenMap.get(tagName);
    }

    public void add(Tag t) {
        childrenMap.put(t.getName(), t);
    }

    public void remove(Tag t) {
        childrenMap.remove(t.getName());
    }

 
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tag[");
        sb.append(super.toString());
         sb.append(',');
        sb.append("children={");
        for(Tag t : childrenMap.values()) {
            sb.append(t.toString());
            sb.append(',');
        }
        sb.append('}');
        sb.append(',');
        sb.append("attributes={");
        for(Attribute a : attrsMap.values()) {
            sb.append(a.toString());
            sb.append(',');
        }
        sb.append('}');
        sb.append("]");
        
        return sb.toString();
    }
    
}
