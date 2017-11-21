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
package org.netbeans.api.java.source;

import com.sun.source.tree.Tree;
import org.apache.lucene.store.FSDirectory;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.modules.parsing.lucene.LuceneIndex;
import org.netbeans.modules.parsing.lucene.support.IndexManagerTestUtilities;

/**
 *
 * @author Jan Lahoda
 */
public final class SourceUtilsTestUtil2 {

    private SourceUtilsTestUtil2() {
    }

    public static <R, P> void run(WorkingCopy wc, Transformer<R, P> t) {
//        if (afterCommit)
//            throw new IllegalStateException ("The run method can't be called on a WorkingCopy instance after the commit");   //NOI18N
        t.init();
        t.attach(wc.impl.getJavacTask().getContext(), wc);
        t.apply(wc.getCompilationUnit());
        t.release();
        t.destroy();
    }
    
    public static <R, P> void run(WorkingCopy wc, Transformer<R, P> t, Tree tree) {
//        if (afterCommit)
//            throw new IllegalStateException ("The run method can't be called on a WorkingCopy instance after the commit");   //NOI18N
        t.init();
        t.attach(wc.impl.getJavacTask().getContext(), wc);
        t.apply(tree);
        t.release();
        t.destroy();
    }
    
    public static void disableLocks() {
    }
    
}
