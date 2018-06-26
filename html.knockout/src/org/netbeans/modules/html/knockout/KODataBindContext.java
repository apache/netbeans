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
package org.netbeans.modules.html.knockout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
public class KODataBindContext {

    private static final Logger LOGGER = Logger.getLogger(KODataBindContext.class.getName());

    private final KODataBindContext original;

    private final List<ParentContext> parents;

    private String data;

    private boolean inForEach;

    private String alias;

    public KODataBindContext() {
        this.original = null;
        this.parents = new ArrayList<>();
    }

    public KODataBindContext(KODataBindContext context) {
        this.original = context;
        this.parents = new ArrayList<>(context.parents);
        this.data = context.data;
        this.inForEach = context.inForEach;
        this.alias = context.alias;
    }

    // XXX we are intetionaly ignoring root of the child
    public static KODataBindContext combine(KODataBindContext parent, KODataBindContext child) {
        KODataBindContext result = new KODataBindContext(parent);
        for (int i = 1; i < child.parents.size(); i++) {
            ParentContext c = child.parents.get(i);
            result.push(c.getValue(), c.isInForEach(), c.getAlias(), true);
        }
        result.push(child.getData(), child.isInForEach(), child.getAlias(), true);
        return result;
    }

    public void push(String newData, boolean foreach, String alias) {
        push(newData, foreach, alias, false);
    }

    private void push(String newData, boolean foreach, String alias, boolean noExpansion) {
        assert !foreach || newData != null;
        assert alias == null || foreach;

        String replacement = (data == null || data.equals("$root")) ? "ko.$bindings" : data; // NOI18N
        String toAdd = newData.replaceAll("$data", replacement); // NOI18N

        if (!noExpansion && foreach) {
            toAdd = "(" + toAdd + ")[0]"; // NOI18N
        }
        if (data == null || "$root".equals(data)) { // NOI18N
            parents.add(new ParentContext("ko.$bindings", false, null)); // NOI18N
        } else {
            parents.add(new ParentContext(this.data, this.inForEach, this.alias));
        }
        this.data = toAdd;
        this.inForEach = foreach;
        this.alias = alias;
    }

    public void pop() {
        if (parents.isEmpty()) {
            LOGGER.log(Level.WARNING, "Invalid state detected. Please check the HTML document validity."); //NOI18N
            return;
        }
        ParentContext context = parents.remove(parents.size() - 1);
        data = context.getValue();
        inForEach = context.isInForEach();
        alias = context.getAlias();
    }

    public KODataBindContext getOriginal() {
        return original;
    }

    public void clear() {
        inForEach = false;
        data = null;
        alias = null;
        parents.clear();
    }

    public List<ParentContext> getParents() {
        return Collections.unmodifiableList(parents);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isInForEach() {
        return inForEach;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.original);
        hash = 53 * hash + Objects.hashCode(this.parents);
        hash = 53 * hash + Objects.hashCode(this.data);
        hash = 53 * hash + (this.inForEach ? 1 : 0);
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
        final KODataBindContext other = (KODataBindContext) obj;
        if (!Objects.equals(this.original, other.original)) {
            return false;
        }
        if (!Objects.equals(this.parents, other.parents)) {
            return false;
        }
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        if (this.inForEach != other.inForEach) {
            return false;
        }
        if (!Objects.deepEquals(this.parents.toArray(), other.parents.toArray())) {
            return false;
        }
        return true;
    }

    public static class ParentContext {

        private final String value;

        private final boolean inForEach;

        private final String alias;

        public ParentContext(String value, boolean inForEach, String alias) {
            this.value = value;
            this.inForEach = inForEach;
            this.alias = alias;
        }

        public String getValue() {
            return value;
        }

        public boolean isInForEach() {
            return inForEach;
        }

        public String getAlias() {
            return alias;
        }
    }
}
