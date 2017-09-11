/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep.editor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.prep.editor.model.CPElement;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class CPStructureItem implements StructureItem {

    private CPElementHandle handle;
    private CPCslElementHandle cslHandle;
    private OffsetRange range;

    public CPStructureItem(CPElement element) {
        this.handle = element.getHandle();
        this.range = element.getRange();

        this.cslHandle = new CPCslElementHandle(handle.getFile(), handle.getName());
    }

    @Override
    public long getPosition() {
        return range.getStart();
    }

    @Override
    public long getEndPosition() {
        return range.getEnd();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public ElementHandle getElementHandle() {
        return cslHandle;
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        switch (handle.getType()) {
            case VARIABLE_GLOBAL_DECLARATION:
                formatter.appendHtml("<font color=000000><b>"); //NOI18N
                break;
        }
        
        formatter.appendText(getName());
        
        switch (handle.getType()) {
            case VARIABLE_GLOBAL_DECLARATION:
                formatter.appendHtml("</b></font>"); //NOI18N);
                break;
        }
        
        return formatter.getText();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(handle.getName());
        hash = 37 * hash + Objects.hashCode(handle.getType());
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
        final CPStructureItem other = (CPStructureItem) obj;
        if (!Objects.equals(this.handle.getName(), other.handle.getName())) {
            return false;
        }
        if (!Objects.equals(this.handle.getType(), other.handle.getType())) {
            return false;
        }
        return true;
    }

    
    
    public static class Mixin extends CPStructureItem {

        public Mixin(CPElement element) {
            super(element);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }
    }

    public static class Variable extends CPStructureItem {

        public Variable(CPElement element) {
            super(element);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }
    }
}
