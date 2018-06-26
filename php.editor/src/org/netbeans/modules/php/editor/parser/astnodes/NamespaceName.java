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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents namespace name:
 * <pre>e.g.<pre>MyNamespace;
 *MyProject\Sub\Level;
 *namespace\MyProject\Sub\Level;
 */
public class NamespaceName extends Expression {

    protected List<Identifier> segments = new ArrayList<>();
    /** Whether the namespace name has '\' prefix, which means it relates to the global scope */
    private boolean global;
    /** Whether the namespace name has 'namespace' prefix, which means it relates to the current namespace scope */
    private boolean current;

    public NamespaceName(int start, int end, Identifier[] segments, boolean global, boolean current) {
        super(start, end);

        if (segments == null) {
            throw new IllegalArgumentException();
        }
        this.segments.addAll(Arrays.asList(segments));

        this.global = global;
        this.current = current;
    }

    public NamespaceName(int start, int end, List segments, boolean global, boolean current) {
        super(start, end);

        if (segments == null) {
            throw new IllegalArgumentException();
        }
        Iterator<Identifier> it = segments.iterator();
        while (it.hasNext()) {
            this.segments.add(it.next());
        }

        this.global = global;
        this.current = current;
    }

    /**
     * Returns whether this namespace name has global context (starts with '\')
     * @return
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * Returns whether this namespace name has current namespace context (starts with 'namespace')
     * @return
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * Retrieves names parts of the namespace
     * @return segments. If names list is empty, that means that this namespace is global.
     */
    public List<Identifier> getSegments() {
        return this.segments;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Identifier identifier : getSegments()) {
            sb.append(identifier).append("\\"); //NOI18N
        }
        return (isGlobal() ? "\\" : (isCurrent() ? "namespace " : "")) + sb.toString(); //NOI18N
    }

}
