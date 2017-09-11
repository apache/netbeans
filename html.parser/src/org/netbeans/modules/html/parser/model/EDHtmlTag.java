/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.netbeans.modules.html.editor.lib.api.DefaultHelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;
import org.netbeans.modules.html.parser.HtmlDocumentation;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class EDHtmlTag implements HtmlTag {

    private ElementDescriptor descriptor;
    private Map<String, HtmlTagAttribute> attrs; //attr name to HtmlTagAttribute instance map
    private HtmlTagType type;
    private Collection<HtmlTag> children;

    public EDHtmlTag(ElementDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    private void initAttributes() {
        attrs = new HashMap<String, HtmlTagAttribute>();
        for (Attribute attr : descriptor.getAttributes()) {
            attrs.put(attr.getName(), HtmlTagProvider.getHtmlTagAttributeInstance(attr));
        }
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        hash = 61 * hash + (this.getTagClass() != null ? this.getTagClass().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HtmlTag)) {
            return false;
        }
        HtmlTag other = (HtmlTag)obj;
        
        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        if (this.getTagClass() != other.getTagClass()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("ElementName2HtmlTagAdapter{name=%s, type=%s}", getName(), getTagClass());//NOI18N
    }

    @Override
    public synchronized Collection<HtmlTagAttribute> getAttributes() {
        if (attrs == null) {
            initAttributes();
        }
        return attrs.values();
    }

    @Override
    public boolean isEmpty() {
        return descriptor.isEmpty();
    }

    @Override
    public boolean hasOptionalOpenTag() {
        return descriptor.hasOptionalOpenTag();
    }

    @Override
    public boolean hasOptionalEndTag() {
        return descriptor.hasOptionalEndTag();
    }

    @Override
    public synchronized HtmlTagAttribute getAttribute(String name) {
        if (attrs == null) {
            initAttributes();
        }
        return attrs.get(name);
    }

    @Override
    public HtmlTagType getTagClass() {
        return descriptor.getTagType();
    }

    @Override
    public synchronized Collection<HtmlTag> getChildren() {
        if (children == null) {
                //add all directly specified children
                Collection<ElementDescriptor> directChildren = descriptor.getChildrenElements();
                children = new LinkedList<HtmlTag>(HtmlTagProvider.convert(directChildren));
                //add all members of children content types
                for (ContentType ct : descriptor.getChildrenTypes()) {
                    Collection<ElementDescriptor> contentTypeChildren = ElementDescriptorRules.getElementsByContentType(ct);
                    children.addAll(HtmlTagProvider.convert(contentTypeChildren));
                }
        }
        return children;
    }

    @Override
    public HelpItem getHelp() {
        StringBuilder header = new StringBuilder();
        header.append("<h2>");
        header.append(NbBundle.getMessage(HtmlTagProvider.class, "MSG_ElementPrefix"));//NOI18N
        header.append(" '");//NOI18N
        header.append(descriptor.getName());
        header.append("'</h2>");//NOI18N

        return descriptor.getHelpLink() != null
                ? new DefaultHelpItem(
                HtmlDocumentation.getDefault().resolveLink(descriptor.getHelpLink()),
                HtmlDocumentation.getDefault(),
                header.toString())
                : null;

    }
}
