/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser.model;

import java.util.*;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;

/**
 *
 * @author marekfukala
 */
public class HtmlTagProvider {

    //global maps
    private static Map<ElementDescriptor, HtmlTag> ED_TAGS = new EnumMap<ElementDescriptor, HtmlTag>(ElementDescriptor.class);
    private static Map<String, HtmlTag> TAGS = new HashMap<String, HtmlTag>();
    
    private static Map<Attribute, HtmlTagAttribute> ATTRS = new EnumMap<Attribute, HtmlTagAttribute>(Attribute.class);

    public static synchronized HtmlTag forElementDescriptor(ElementDescriptor descriptor) {
        HtmlTag impl = ED_TAGS.get(descriptor);
        if (impl == null) {
            impl = new EDHtmlTag(descriptor);
            ED_TAGS.put(descriptor, impl);
        }
        return impl;
    }
    
    public static synchronized HtmlTag forUnknownTag(String name) {
        HtmlTag impl = TAGS.get(name);
        if (impl == null) {
            impl = new UnknownHtmlTag(name);
            TAGS.put(name, impl);
        }
        return impl;
    }
    
    public static synchronized HtmlTag getTagForElement(String name) {
        assert name != null;
        ElementDescriptor descriptor = ElementDescriptor.forName(name);
        
        return descriptor != null ? forElementDescriptor(descriptor) : forUnknownTag(name);
    }

    static synchronized Collection<HtmlTag> convert(Collection<ElementDescriptor> elements) {
        Collection<HtmlTag> converted = new LinkedList<HtmlTag>();
        for(ElementDescriptor element : elements) {
            converted.add(forElementDescriptor(element));
        }
        return converted;
    }

    static synchronized HtmlTagAttribute getHtmlTagAttributeInstance(Attribute attr) {
        HtmlTagAttribute htmlTagAttribute = ATTRS.get(attr);
        if(htmlTagAttribute == null) {
            htmlTagAttribute = new EDHtmlTagAttribute(attr);
            ATTRS.put(attr, htmlTagAttribute);
        }
        return htmlTagAttribute;
    }
    

}
