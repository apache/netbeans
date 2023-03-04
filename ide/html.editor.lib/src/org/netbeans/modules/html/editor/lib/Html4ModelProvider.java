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
package org.netbeans.modules.html.editor.lib;

import java.util.*;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.*;
import org.netbeans.modules.html.editor.lib.dtd.DTD;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides {@link HtmlModel} for HTML 3.2 to 4.01
 *
 * @author marekfukala
 */
@ServiceProvider(service = HtmlModelProvider.class, position = 20)
public class Html4ModelProvider implements HtmlModelProvider {

    private static final Map<HtmlVersion, Collection<HtmlTag>> ALL_TAGS_MAP = new EnumMap<>(HtmlVersion.class);

    private static synchronized Collection<HtmlTag> getAllTags(HtmlVersion version) {
        Collection<HtmlTag> value = ALL_TAGS_MAP.get(version);
        if (value == null) {
            DTD dtd = version.getDTD();
            assert dtd != null;

            List<org.netbeans.modules.html.editor.lib.dtd.DTD.Element> all = dtd.getElementList("");
            value = new ArrayList<>();
            for (org.netbeans.modules.html.editor.lib.dtd.DTD.Element e : all) {
                value.add(DTD2HtmlTag.getTagForElement(dtd, e));
            }
            ALL_TAGS_MAP.put(version, value);
        }
        return value;
    }

    @Override
    public HtmlModel getModel(final HtmlVersion version) {
        switch (version) {
            case HTML32:
            case HTML40_FRAMESET:
            case HTML40_STRICT:
            case HTML40_TRANSATIONAL:
            case HTML41_FRAMESET:
            case HTML41_STRICT:
            case HTML41_TRANSATIONAL:
            case XHTML10_FRAMESET:
            case XHTML10_STICT:
            case XHTML10_TRANSATIONAL:
            case XHTML11:
                return new Html4Model(version);
            default:
                return null;
        }
    }

    private static class Html4Model implements HtmlModel {

        private HtmlVersion version;

        public Html4Model(HtmlVersion version) {
            this.version = version;
        }

        @Override
        public Collection<HtmlTag> getAllTags() {
            return Html4ModelProvider.getAllTags(version);
        }

        @Override
        public HtmlTag getTag(String tagName) {
            DTD.Element element = version.getDTD().getElement(tagName);
            if (element == null) {
                return null;
            }
            return DTD2HtmlTag.getTagForElement(version.getDTD(), element);
        }

        @Override
        public Collection<NamedCharRef> getNamedCharacterReferences() {
            return version.getDTD().getCharRefList("");
        }

        @Override
        public String getModelId() {
            return "html4model"; //NOI18N
        }
    }

    private static class DTD2HtmlTag {

        private static final Logger LOGGER = Logger.getLogger(DTD2HtmlTag.class.getName());
        private static HashMap<DTD.Element, HtmlTag> MAP = new HashMap<>();
        private static HashMap<DTD.Attribute, HtmlTagAttribute> ATTRS_MAP = new HashMap<>();

        private static synchronized HtmlTag getTagForElement(DTD dtd, DTD.Element elementName) {
            HtmlTag impl = MAP.get(elementName);
            if (impl == null) {
                impl = new DTDElement2HtmlTagAdapter(dtd, elementName);
                MAP.put(elementName, impl);
            }
            return impl;
        }

        private static Collection<HtmlTag> convert(DTD dtd, Collection<DTD.Element> elements) {
            Collection<HtmlTag> converted = new ArrayList<>();
            for (DTD.Element element : elements) {
                assert element != null;
                converted.add(getTagForElement(dtd, element));
            }
            return converted;
        }

        private static synchronized HtmlTagAttribute getHtmlTagAttribute(DTD.Attribute attribute) {
            HtmlTagAttribute attr = ATTRS_MAP.get(attribute);
            if (attr == null) {
                attr = new Attribute2HtmlTagAttribute(attribute);
                ATTRS_MAP.put(attribute, attr);
            }
            return attr;
        }

        private static class DTDElement2HtmlTagAdapter implements HtmlTag {

            private DTD.Element element;
            private DTD dtd; //needed just because of the html-body child hack
            private Collection<HtmlTagAttribute> attrs;
            private Collection<HtmlTag> children;

            private DTDElement2HtmlTagAdapter(DTD dtd, DTD.Element element) {
                this.dtd = dtd;
                this.element = element;
                this.attrs = wrap(element.getAttributeList(null));
            }

            private Collection<HtmlTagAttribute> wrap(Collection<DTD.Attribute> attrNames) {
                if (attrNames == null) {
                    return Collections.emptyList();
                }
                Collection<HtmlTagAttribute> attributes = new LinkedList<>();
                for (DTD.Attribute an : attrNames) {
                    HtmlTagAttribute hta = getHtmlTagAttribute(an);
                    if (hta != null) {
                        attributes.add(hta);
                    } else {
                        LOGGER.info("Unknown attribute " + an + " requested.");//NOI18N
                    }
                }
                return attributes;
            }

            @Override
            public String getName() {
                return element.getName();
            }

            @Override
            public Collection<HtmlTagAttribute> getAttributes() {
                return attrs;
            }

            @Override
            public boolean isEmpty() {
                return element.isEmpty();
            }

            @Override
            public boolean hasOptionalOpenTag() {
                return element.hasOptionalStart();
            }

            @Override
            public boolean hasOptionalEndTag() {
                return element.hasOptionalEnd();
            }

            @Override
            public HtmlTagAttribute getAttribute(String name) {
                DTD.Attribute attr = element.getAttribute(name);
                if (attr == null) {
                    return null;
                }
                return getHtmlTagAttribute(attr);
            }

            @Override
            public HtmlTagType getTagClass() {
                return HtmlTagType.HTML;
            }

            @Override
            public synchronized Collection<HtmlTag> getChildren() {
                //logic copied from David Konecny's HtmlIndenter.
                if (children == null) {
                    Set<DTD.Element> set = new HashSet<>();
                    for (DTD.Element el : (Set<DTD.Element>) element.getContentModel().getIncludes()) {
                        if (el != null) {
                            set.add(el);
                        }
                    }
                    for (DTD.Element el : (Set<DTD.Element>) element.getContentModel().getExcludes()) {
                        if (el != null) {
                            set.remove(el);
                        }
                    }
                    for (DTD.Element el : (Set<DTD.Element>) element.getContentModel().getContent().getPossibleElements()) {
                        if (el != null) {
                            set.add(el);
                        }
                    }
                    if (element.getName().equalsIgnoreCase("HTML")) {
                        // XXXXXXXXXXXXXXXXX TODO:
                        DTD.Element bodyElement = dtd.getElement("BODY");
                        if(bodyElement != null) {
                            set.add(bodyElement);
                        }
                    }
                    children = convert(dtd, set);
                }
                return children;
            }

            @Override
            public HelpItem getHelp() {
                return null;
            }
        }

        private static class Attribute2HtmlTagAttribute implements HtmlTagAttribute {

            private DTD.Attribute attr;

            public Attribute2HtmlTagAttribute(DTD.Attribute attr) {
                this.attr = attr;
            }

            @Override
            public String getName() {
                return attr.getName();
            }

            @Override
            public boolean isRequired() {
                return attr.isRequired();
            }

            @Override
            public HtmlTagAttributeType getType() {
                switch (attr.getType()) {
                    case DTD.Attribute.TYPE_BOOLEAN:
                        return HtmlTagAttributeType.BOOLEAN;
                    case DTD.Attribute.TYPE_SET:
                        return HtmlTagAttributeType.SET;
                    case DTD.Attribute.TYPE_BASE:
                        return HtmlTagAttributeType.GENERIC;
                    default:
                        return HtmlTagAttributeType.GENERIC;
                }
            }

            @Override
            public Collection<String> getPossibleValues() {
                Collection<DTD.Value> values = attr.getValueList(null);
                if (values == null) {
                    return Collections.emptyList();
                }
                Collection<String> res = new LinkedList<>();
                for (DTD.Value v : values) {
                    res.add(v.getName());
                }
                return res;
            }

            @Override
            public HelpItem getHelp() {
                return null;
            }
        }
    }
}
