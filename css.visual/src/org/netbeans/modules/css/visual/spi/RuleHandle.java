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
