/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor.api.elements.index;

import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.elements.common.ClassElement;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;

/**
 * A class describing a Groovy class that is in "textual form" (signature, filename, etc.)
 * obtained from the code index.
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
public final class IndexedClass extends IndexedElement implements ClassElement {

    /** This class is a module rather than a proper class */
    public static final int MODULE = 1 << 6;

    private final String simpleName;

    protected IndexedClass(IndexResult result, String fqn, String simpleName, String attributes, int flags) {
        super(result, fqn, attributes, flags);
        this.simpleName = simpleName;
    }

    public static IndexedClass create(String simpleName, String fqn, IndexResult result,
        String attributes, int flags) {
        IndexedClass c = new IndexedClass(result, fqn, simpleName, attributes, flags);
        return c;
    }

    // XXX Is this necessary?
    @Override
    public String getSignature() {
        return in;
    }

    @Override
    public String getName() {
        return simpleName;
    }

    @Override
    public ElementKind getKind() {
        return (flags & MODULE) != 0 ? ElementKind.MODULE : ElementKind.CLASS;
    }
    
    @Override 
    public boolean equals(Object o) {
        if (o instanceof IndexedClass && in != null) {
            return in.equals(((IndexedClass) o).in);
        }
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return in == null ? super.hashCode() : in.hashCode();
    }

    @Override
    public String getFqn() {
        return in;
    }
}
