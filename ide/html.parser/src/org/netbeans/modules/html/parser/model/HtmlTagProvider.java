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
