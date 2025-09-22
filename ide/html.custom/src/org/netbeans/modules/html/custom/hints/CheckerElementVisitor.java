/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.custom.hints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.custom.conf.Configuration;
import org.netbeans.modules.html.custom.conf.Tag;
import org.netbeans.modules.html.editor.api.HtmlEditorUtils;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marek
 */
public class CheckerElementVisitor {

    public static ElementVisitor getChecker(RuleContext context, Configuration conf, Snapshot snapshot, List<Hint> hints) {
        Collection<ElementVisitor> visitors = new ArrayList<>();
        visitors.add(new UnknownAttributesChecker(context, conf, snapshot, hints));
        visitors.add(new MissingRequiredAttributeChecker(context, conf, snapshot, hints));

        return new AggregatedVisitor(visitors);
    }

    public static class AggregatedVisitor implements ElementVisitor {

        private final Collection<ElementVisitor> visitors;

        public AggregatedVisitor(Collection<ElementVisitor> visitors) {
            this.visitors = visitors;
        }

        @Override
        public void visit(Element node) {
            for(ElementVisitor visitor : visitors) {
                visitor.visit(node);
            }
        }

    }

    protected abstract static class Checker implements ElementVisitor {

        protected RuleContext context;
        protected Configuration conf;
        protected Snapshot snapshot;
        protected List<Hint> hints;

        public Checker(RuleContext context, Configuration conf, Snapshot snapshot, List<Hint> hints) {
            this.context = context;
            this.conf = conf;
            this.snapshot = snapshot;
            this.hints = hints;
        }

    }

    private static class UnknownAttributesChecker extends Checker {

        public UnknownAttributesChecker(RuleContext context, Configuration conf, Snapshot snapshot, List<Hint> hints) {
            super(context, conf, snapshot, hints);
        }

        @Override
        public void visit(Element node) {
            switch (node.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) node;
                    String name = ot.name().toString();
                    Tag tagModel = conf.getTag(name);
                    //check just the custom elements
                    if (tagModel != null) {
                        //some attributes are specified in the conf, lets check
                        Collection<Attribute> tagAttrs = ot.attributes();
                        Collection<String> unknownAttributeNames = new ArrayList<>();
                        for (Attribute a : tagAttrs) {
                            String attrName = a.name().toString();
                            if (tagModel.getAttribute(attrName) == null) {
                                //not found in the context element attr list, but still may be defined as contextfree attribute
                                if (conf.getAttribute(attrName) == null) {
                                    //unknown attribute in known element w/ some other attributes specified -> show error annotation
                                    unknownAttributeNames.add(attrName);
                                }
                            }
                        }

                        if (!unknownAttributeNames.isEmpty()) {
                            //if there's no attribute defined in the conf, it may be a user decision not to specify the attributes
                            //in such case just show the hint as linehint
//                                boolean lineHint = tagModel.getAttributesNames().isEmpty();
                            boolean lineHint = false;

                            //use the whole element offsetrange so multiple unknown attributes can be handled
                            OffsetRange range = new OffsetRange(snapshot.getOriginalOffset(ot.from()), snapshot.getOriginalOffset(ot.to()));
                            hints.add(new UnknownAttributes(unknownAttributeNames, tagModel.getName(), context, range, lineHint));
                        }
                    }

            }
        }
    }

    private static class MissingRequiredAttributeChecker extends Checker {

        public MissingRequiredAttributeChecker(RuleContext context, Configuration conf, Snapshot snapshot, List<Hint> hints) {
            super(context, conf, snapshot, hints);
        }

        @Override
        public void visit(Element node) {
            switch (node.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) node;
                    String name = ot.name().toString();
                    Tag tagModel = conf.getTag(name);
                    //check just the custom elements
                    if (tagModel != null) {
                        Collection<org.netbeans.modules.html.custom.conf.Attribute> toAdd = new ArrayList<>();
                        for(org.netbeans.modules.html.custom.conf.Attribute modelAttribute : tagModel.getAttributes()) {
                            if(modelAttribute.isRequired()) {
                                if(ot.getAttribute(modelAttribute.getName()) == null) {
                                    //missing required attribute
                                    toAdd.add(modelAttribute);
                                }
                             }
                        }
                        if(!toAdd.isEmpty()) {
                            hints.add(new MissingRequiredAttributes(toAdd, ot, context, HtmlEditorUtils.getDocumentOffsetRange(node, snapshot), false));
                        }
                    }

            }
        }
    }


}
