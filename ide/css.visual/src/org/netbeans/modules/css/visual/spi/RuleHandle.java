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
package org.netbeans.modules.css.visual.spi;

import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.ElementHandle;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class RuleHandle extends Location {
    
    private String selectorsImage;
    private ElementHandle handle;

    /**
     * Must be called under model's read lock!
     */
    public static RuleHandle createRuleHandle(Rule rule) {
        Model model = rule.getModel();
        Lookup lookup = model.getLookup();
        Snapshot snapshot = lookup.lookup(Snapshot.class);
        FileObject file = lookup.lookup(FileObject.class);
        //in case of very erroneous rule the selectorgroup node may not be present,
        //use the rule node itself for getting the rule name.
        SelectorsGroup selectorsGroup = rule.getSelectorsGroup();
        Element element = selectorsGroup == null ? rule : selectorsGroup;
        String img = model.getElementSource(element).toString();
        
        int offset = snapshot.getOriginalOffset(rule.getStartOffset());
        
        return new RuleHandle(file, rule, offset, img);
    }
    
    private RuleHandle(FileObject styleSheet, Rule rule, int offset, String selectorsImage) {
        super(styleSheet, offset);
        this.handle = rule.getElementHandle();
        this.selectorsImage = selectorsImage;
    }
    
    public Rule getRule(Model model) {
        return (Rule)handle.resolve(model);
    }
    
    public String getDisplayName() {
        return selectorsImage;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.selectorsImage != null ? this.selectorsImage.hashCode() : 0);
        hash = 43 * hash + super.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RuleHandle other = (RuleHandle) obj;
        if ((this.selectorsImage == null) ? (other.selectorsImage != null) : !this.selectorsImage.equals(other.selectorsImage)) {
            return false;
        }
        return super.equals(obj);
    }
  
 
}
