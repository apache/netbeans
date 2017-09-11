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
package org.netbeans.modules.css.prep.editor;

import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;

/**
 *
 * @author marekfukala
 */
public class MixinCompletionItem extends CPCompletionItem {
    
    /**
     * 
     * @param elementHandle
     * @param handle
     * @param anchorOffset
     * @param origin Origin is null for current file. File displayname otherwise.
     */
    public MixinCompletionItem(ElementHandle elementHandle, CPElementHandle handle, int anchorOffset, String origin) {
        super(elementHandle, handle, anchorOffset ,origin);
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.METHOD;
    }

    @Override
    public String getInsertPrefix() {
        return handle.getName();
    }

    @Override
    public String getName() {
        return handle.getName();
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.origin != null ? this.origin.hashCode() : 0);
        hash = 89 * hash + getName().hashCode();
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
        final MixinCompletionItem other = (MixinCompletionItem) obj;
        if ((this.origin == null) ? (other.origin != null) : !this.origin.equals(other.origin)) {
            return false;
        }
        if (!getName().equals(other.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public int getSortPrioOverride() {
        int prio = 70;
        if(origin == null) {
            prio -= 40; //current file items have precedence
        }
        return prio;
    }
}
