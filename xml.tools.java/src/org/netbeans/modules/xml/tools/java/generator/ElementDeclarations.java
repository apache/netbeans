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
package org.netbeans.modules.xml.tools.java.generator;

import java.util.*;

import org.netbeans.tax.*;

/**
 * Maps element declaration name => Entry.
 *
 * @author  Petr Kuzel
 * @version
 */
public class ElementDeclarations extends HashMap {

    /** Serial Version UID */
    private static final long serialVersionUID =2385299250969298335L;

    /**
     * Creates new ElementDeclarations from TreeElementDecl iterator.
     */
    public ElementDeclarations(Iterator it) {
        if (it == null) return;
        while (it.hasNext()) {
            TreeElementDecl next = (TreeElementDecl) it.next();
            put(next.getName(), new Entry(next.allowText(), next.allowElements()));
        }
    }
    
    /**
     * Get Entry by declaration name.
     */
    public final Entry getEntry(String element) {
        return (Entry) get(element);
    }
    
    /**
     * Entry represents one value keyed by element declaration name. 
     */
    public static class Entry {

        public static final int EMPTY = 0;
        public static final int DATA = 1;
        public static final int CONTAINER = 2;
        public static final int MIXED = 3;
        
        private int type;
        
        public Entry(boolean at, boolean ae) {
            type = at ? DATA : 0;
            type += ae ? CONTAINER : 0;
        }

        public int getType() {
            return type;
        }
    }
}
