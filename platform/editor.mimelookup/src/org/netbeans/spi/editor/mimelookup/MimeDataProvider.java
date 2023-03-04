/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.editor.mimelookup;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Lookup;

/**
 * Provides a <code>Lookup</code> for the specific <code>MimePath</code>.
 *
 * <p>The implementations of this interface should be registered among the services
 * in the default lookup using {@link org.openide.util.lookup.ServiceProvider}.
 *
 *  @author Miloslav Metelka, Vita Stejskal
 */
public interface MimeDataProvider {

    /**
     * Retrieves a <code>Lookup</code> for the given <code>MimePath</code>.
     * 
     * <p>The <code>Lookup</code> returned by this method should hold a reference
     * to the <code>MimePath</code> it was created for.
     *
     * <p>The implementors should consider caching of the <code>Lookup</code> instances
     * returned by this method for performance reasons. The <code>MimePath</code>
     * object can be used as a stable key for such a cache, because it implements
     * its <code>equals</code> and <code>hashCode</code> method in the suitable way.
     *
     * @param mimePath The mime path to get the <code>Lookup</code> for. The mime
     * path passed in can't be <code>null</code>, but it can be the 
     * {@link MimePath#EMPTY} mime path.
     *
     * @return The <code>Lookup</code> for the given <code>MimePath</code> or
     * <code>null</code> if there is no lookup available for this mime path.
     */
    public Lookup getLookup(MimePath mimePath);

}
