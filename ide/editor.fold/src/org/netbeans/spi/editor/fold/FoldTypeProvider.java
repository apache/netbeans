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
package org.netbeans.spi.editor.fold;

import java.util.Collection;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Provider of FoldType constants for the MimeType. 
 * The Provider should enumerate FoldTypes that apply to the given MIME type.
 * There can be multiple providers for a MIME type - some advanced constructions in
 * the language can be recognized / folded by extension modules. Consider Java vs.
 * Bean Patterns, or XML vs. Spring bean config.
 * <p/>
 * FoldTypes will be collected and some pieces of UI can present the folds, such 
 * as Auto-folding options.
 * <p/>
 * The Provider may specify inheritable=true; in that case the contributed FoldTypes
 * will become available for more specific MIME types, too. For example, if a FoldTypeProvider
 * for text/xml registers FoldTypes TAG and COMMENT with inheritable=true,
 * those FoldTypes will be listed also for text/x-ant+xml. This feature allows 
 * to "inject" Fold types and FoldManager on general MIME type ("") for all 
 * types of files.
 * 
 * @author sdedic
 * @since 1.35
 */
@MimeLocation(subfolderName = "FoldManager")
public interface FoldTypeProvider {
    /**
     * Enumerates values for the given type.
     * @return FoldType values.
     */
    public Collection getValues(Class type);
    
    /**
     * Determines whether the folds propagate to child mime types(paths).
     * If the method returns true, then more specific MIME types will also
     * list FoldTypes returned by this Provider. 
     * 
     * @return whether the provided FoldTypes should be inherited (true).
     */
    public boolean inheritable();
    
}
