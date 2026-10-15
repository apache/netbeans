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

package org.netbeans.modules.xml.api.model;

import java.util.List;

/**
 * This class instance is returned by DTDGrammarQueryProvider.getGrammar(GrammarEnvironment env) method.
 * It is used in xml/text-edit module by GrammarManager class to obtain a list of external entities 
 * resolved during the DTD parsing. The GrammarManager then listens on these files and invalidates 
 * the resulting grammar if any of the files changes.
 *
 * @author mfukala@netbeans.org
 */
public interface ExtendedGrammarQuery extends GrammarQuery {
    
    /** @return a List of resolved entities System id-s names.*/
    public List<String> getResolvedEntities();
    
}
