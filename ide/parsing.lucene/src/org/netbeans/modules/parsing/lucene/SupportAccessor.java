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
package org.netbeans.modules.parsing.lucene;

import org.apache.lucene.util.BytesRef;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManager;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SupportAccessor {
    
    //@GuardedBy("SupportAccessor.class")
    private static volatile SupportAccessor instance;
    
    public static synchronized SupportAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(
                    IndexManager.class.getName(),
                    true,
                    SupportAccessor.class.getClassLoader());
                
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        assert instance != null;
        return instance;
    }
    
    public static void setInstance(@NonNull SupportAccessor _instance) {
        assert _instance != null;
        instance = _instance;
    }
    
    
    public abstract Index.WithTermFrequencies.TermFreq newTermFreq();
    
    @NonNull
    public abstract Index.WithTermFrequencies.TermFreq setTermFreq(
            @NonNull Index.WithTermFrequencies.TermFreq into,
            @NonNull BytesRef term,
            int freq);
    
}
