/**
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
package org.netbeans.api.java.source.support;

import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CaseTree.CaseKind;
import com.sun.source.util.TreeScanner;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorAwareTreeScanner<R,P> extends TreeScanner<R,P> {

    @Override
    public R visitErroneous(ErroneousTree et, P p) {
        return scan(et.getErrorTrees(), p);
    }

    @Override
    public R visitCase(CaseTree node, P p) {
        R r = scan(node.getLabels(), p);
        r = reduce(scan(node.getGuard(), p), r);
        if (node.getCaseKind() == CaseKind.STATEMENT) {
            r = reduce(scan(node.getStatements(), p), r);
        } else {
            r = reduce(scan(node.getBody(), p), r);
        }
        return r;
    }
}
