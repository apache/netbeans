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
package org.netbeans.modules.html.editor.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.*;

/**
 *
 * @author marekfukala
 */
public class RootNode implements FeaturedNode {

    private CharSequence source;
    private Collection<Element> children;

    public RootNode(CharSequence source) {
        this(source, new LinkedList<Element>());
    }

    public RootNode(CharSequence source, Collection<Element> children) {
        this.source = source;
        this.children = children;
    }

    @Override
    public Collection<Element> children() {
        return children;
    }

    @Override
    public Collection<Element> children(ElementType type) {
        Collection<Element> filtered = new ArrayList<>();
        for (Element child : children()) {
            if (child.type() == type) {
                filtered.add(child);
            }
        }
        return filtered;
    }

    @Override
    public Node parent() {
        return null;
    }

    @Override
    public int from() {
        return 0;
    }

    @Override
    public int to() {
        return source.length();
    }

    @Override
    public ElementType type() {
        return ElementType.ROOT;
    }

    @Override
    public CharSequence image() {
        return source;
    }

    @Override
    public Collection<ProblemDescription> problems() {
        return Collections.emptyList();
    }

    @Override
    public Object getProperty(String propertyName) {
        return null;
    }

    @Override
    public CharSequence id() {
        return type().name();
    }

    @Override
    public Collection<Element> children(ElementFilter filter) {
        Collection<Element> filtered = new ArrayList<>();
        for (Element e : children()) {
            if (filter.accepts(e)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    @Override
    public <T extends Element> Collection<T> children(Class<T> type) {
        Collection<T> filtered = new ArrayList<>();
        for (Element child : children()) {
            if (type.isAssignableFrom(child.getClass())) {
                filtered.add(type.cast(child));
            }
        }
        return filtered;
    }
}
