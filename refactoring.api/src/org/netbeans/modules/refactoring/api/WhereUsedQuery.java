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
package org.netbeans.modules.refactoring.api;

import java.util.Hashtable;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Where used query does not do any "real" refactoring.
 * It just encapsulates parameters for Find Usages, which is implemented by
 * plugins.
 * Refactoring itself is implemented in plugins
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public final class WhereUsedQuery extends AbstractRefactoring {
    /**
     * key for {@link #getBooleanValue()}
     * is search in comments requested?
     */
    public static final String SEARCH_IN_COMMENTS = "SEARCH_IN_COMMENTS"; // NOI18N
    /**
     * key for {@link #getBooleanValue()}
     * is find references requested?
     */
    public static final String FIND_REFERENCES = "FIND_REFERENCES"; // NOI18N
    
    /**
     * Creates a new instance of WhereUsedQuery.
     * WhereUsedQuery implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Java Refactoring</td><td><ul>
     *                                    <li>{@link org.openide.filesystems.FileObject} with content type text/x-java (class references)
     *                                    <li>{@link org.netbeans.api.java.source.TreePathHandle} (class, field, method references)
     *                                    </ul>
     *                              <td>Finds references</td></tr>
     * </table>
     * @param lookup put object for which you request references into Lookup instance.
     */
    public WhereUsedQuery(@NonNull Lookup lookup) {
        super(lookup);
        putValue(FIND_REFERENCES, true);
    }

   /**
     * Setter for searched item
     * @param lookup put object for which you request references into Lookup instance.
     */
    public final void setRefactoringSource(@NonNull Lookup lookup) {
        Parameters.notNull("lookup", lookup); // NOI18N
        this.refactoringSource = lookup;
    }
    
    private Hashtable hash = new Hashtable();
    
    /**
     * getter for various properties
     * @param key 
     * @return value for given key
     * @see WhereUsedQuery#SEARCH_IN_COMMENTS
     * @see WhereUsedQuery#FIND_REFERENCES
     */
    public final boolean getBooleanValue(@NonNull Object key) {
        Parameters.notNull("key", key); // NOI18N
        Object o = hash.get(key);
        if (o instanceof Boolean) 
            return (Boolean)o;
        return false;
    }
    
    /**
     * setter for various properties
     * @param key 
     * @param value set value for given key
     * @see WhereUsedQuery#SEARCH_IN_COMMENTS
     * @see WhereUsedQuery#FIND_REFERENCES
     */
    public final void putValue(@NonNull Object key, Object value) {
        Parameters.notNull("key", key); // NOI18N
        hash.put(key, value);
    }
}

