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
package org.netbeans.modules.java.graph;

import java.util.List;

/**
 *
 * @author Tomas Stupka
 * @param <I>
 */
public interface GraphNodeVisitor<I extends GraphNodeImplementation> {
    boolean visit(I dn);
    boolean endVisit(I dn);
    
    default boolean accept(I d) {
        if(visit(d)) {
            List<I> chs = d.getChildren();
            if(chs != null) {
                for (I ch : chs) {
                    if(!accept(ch)){
                        break;
                    }                    
                }
            }
        }
        return endVisit(d);
    }    
}
