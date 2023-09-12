/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
     * key for {@link #getBooleanValue(Object)}
     * is search in comments requested?
     */
    public static final String SEARCH_IN_COMMENTS = "SEARCH_IN_COMMENTS"; // NOI18N
    /**
     * key for {@link #getBooleanValue(Object)}
     * is find references requested?
     */
    public static final String FIND_REFERENCES = "FIND_REFERENCES"; // NOI18N
    
    /**
     * Creates a new instance of WhereUsedQuery.
     * WhereUsedQuery implementations currently understand following types:
     * <table>
     * <caption>WhereUsedQuery types supported</caption>
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Java Refactoring</td><td><ul>
     *                                    <li>{@link org.openide.filesystems.FileObject} with content type text/x-java (class references)
     *                                    <li><a href="@org-netbeans-modules-java-source-base@/org/netbeans/api/java/source/TreePathHandle.html">org.netbeans.api.java.source.TreePathHandle</a> (class, field, method references)
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

