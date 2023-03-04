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
package org.openide.text;

import org.openide.util.Lookup;


/**
 * A provider of annotations for given context.
 *
 * Implementations of this interface are looked up in the global lookup
 * and called to let them attach annotations to the lines in the set.
 * The call is performed during opening of given context.
 *
 * @author Petr Nejedly
 * @since 4.30
 */
public interface AnnotationProvider {
    /**
     * Attach annotations to the Line.Set for given context.
     *
     * @param set the Line.Set to attach annotations to.
     * @param context a Lookup describing the context for the Line.Set.
     *        If the Line.Set is associated with a document originating from
     *        a file, the context shall contain the respective FileObject.
     */
    public void annotate(Line.Set set, Lookup context);
}
