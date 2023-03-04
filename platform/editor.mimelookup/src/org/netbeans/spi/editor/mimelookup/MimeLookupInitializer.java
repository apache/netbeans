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

import org.openide.util.Lookup;

/**
 *  Provides an initialization of MimeLookup on either global or mime-type
 *  specific level.
 *  <br>
 *  The implementations of this class should be registed to default lookup using {@link org.openide.util.lookup.ServiceProvider}.
 *  <br>
 *  Such registered instance serves as a global level initializer
 *  which can further be asked for children by {@link #child(String)}
 *  which will lead to forming of a tree initializers hierarchy.
 *  <br>
 *  The contents provided by {@link #lookup()} of the global-level initializer
 *  (the one registered in the layer) will automatically appear
 *  in all the results returned by <code>MimeLookup</code> for any particular mime type.
 *  <br>
 *  Once someone asks for a <code>MimeLookup</code> for a specific mime-type
 *  by using {@link org.netbeans.api.editor.mimelookup.MimeLookup#getMimeLookup(String)}
 *  the global level initializer will be asked for {@link #child(String)}
 *  and the {@link #lookup()} on the returned children
 *  will define the result data (together with the global-level initializer's lookup).
 *  <br>
 *  This process can be arbitrarily nested for embedded mime-types.
 *  
 * <p> 
 *  An example implementation of MimeLookupInitializer
 *  that works over xml layer file system can be found at mime lookup module
 *  implementation <a href="http://editor.netbeans.org/source/browse/editor/mimelookup/src/org/netbeans/modules/editor/mimelookup/Attic/LayerMimeLookupImplementation.java">LayerMimeLookupInitializer</a>
 *
 *  @author Miloslav Metelka, Martin Roskanin
 *  @deprecated Use {@link MimeDataProvider} instead.
 */
@Deprecated
public interface MimeLookupInitializer {

    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     * 
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    Lookup lookup();
    
    /**
     * Retrieves a Lookup.Result of MimeLookupInitializers for the given sub-mimeType.
     *
     * @param mimeType mime-type string representation e.g. "text/x-java"
     * @return non-null lookup result of MimeLookupInitializer(s).
     *  <br/>
     *  Typically there should be just one child initializer although if there
     *  will be more than one all of them will be taken into consideration.
     *  <br/>
     *  If there will be no specific initializers for the particular mime-type
     *  then an empty result should be returned.
     */
    Lookup.Result<MimeLookupInitializer> child(String mimeType);

}
