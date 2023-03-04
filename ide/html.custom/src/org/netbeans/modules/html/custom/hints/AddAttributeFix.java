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
package org.netbeans.modules.html.custom.hints;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.html.custom.conf.Attribute;
import org.netbeans.modules.html.custom.conf.Configuration;
import org.netbeans.modules.html.custom.conf.Tag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
@NbBundle.Messages(value = {
    "# {0} - attribute name",
    "declareGlobalAttr=Declare global attribute \"{0}\"",
    "# {0} - attribute names list",
    "declareGlobalAttrs=Declare \"{0}\" attributes as global",
    "# {0} - attribute name",
    "# {1} - element name",
    "declareElementAttr=Declare \"{0}\" as attribute of element \"{1}\"",
    "# {0} - attribute names list",
    "# {1} - element name",
    "declareElementAttrs=Declare \"{0}\" as attributes of element \"{1}\""})
public final class AddAttributeFix implements HintFix {
    private final Collection<String> attributeNames;
    private final String elementContextName;
    private final Snapshot snapshot;

    public AddAttributeFix(Collection<String> attributeNames, String elementContextName, Snapshot snapshot) {
        this.attributeNames = attributeNames;
        this.elementContextName = elementContextName;
        this.snapshot = snapshot;
    }
    
    public AddAttributeFix(String attributeName, String elementContextName, Snapshot snapshot) {
        this(Collections.singleton(attributeName), elementContextName, snapshot);
    }

    @Override
    public String getDescription() {
        String attrNamesList = Utils.attributeNames2String(attributeNames);
        if(elementContextName == null) {
            return attributeNames.size() == 1 
                    ? Bundle.declareGlobalAttr(attrNamesList)
                    : Bundle.declareGlobalAttrs(attrNamesList);
        } else {
            return attributeNames.size() == 1
                ? Bundle.declareElementAttr(attrNamesList, elementContextName)
                : Bundle.declareElementAttrs(attrNamesList, elementContextName);
        }
    }
  
    @Override
    public void implement() throws Exception {
        Configuration conf = Configuration.get(snapshot.getSource().getFileObject());
        
        if(elementContextName != null) {
            //attr in context
            Tag tag = conf.getTag(elementContextName);
            if(tag == null) {
                //no custom element found => may be html element => just create attribute as global + specify context
                //contextfree attribute
                for(String aName : attributeNames) {
                    Attribute attribute = new Attribute(aName);
                    attribute.addContext(elementContextName);
                    conf.add(attribute);
                }
            } else {
                //in custom element
                for(String aName : attributeNames) {
                    tag.add( new Attribute(aName));
                }
            }
        } else {
            //contextfree attribute
            for(String aName : attributeNames) {
                conf.add(new Attribute(aName));
            }
        }
        conf.store();
        LexerUtils.rebuildTokenHierarchy(snapshot.getSource().getDocument(true));
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }
    
}
