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
package org.netbeans.modules.css.model.api;

import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public class ModelUtilsTest extends ModelTestBase {

    public ModelUtilsTest(String name) {
        super(name);
    }
    
    public void testFindMatchingMedia() {
        final Model m1 = createModel("@media screen { div {} } @media xxx { div {} } @media print { } ");
        final AtomicReference<Media> mr = new AtomicReference<>();
        ModelVisitor visitor = new ModelVisitor.Adapter() {

            @Override
            public void visitMedia(Media media) {
                CharSequence mql =  LexerUtils.trim(m1.getElementSource(media.getMediaQueryList()));
                if(mql.equals("print")) {
                    mr.set(media);
                }
            }
            
        };
        m1.getStyleSheet().accept(visitor);
        
        Media print = mr.get();
        assertNotNull(print);
        
        final Model m2 = createModel("@media xxx { div {} } .clz {}  @media print { } ");
        m2.getStyleSheet().accept(visitor);
        
        Media print2 = mr.get();
        assertNotNull(print2);
        
        ModelUtils utils = new ModelUtils(m2);
        Media match = utils.findMatchingMedia(m1, print);
        
        assertNotNull(match);
        assertSame(print2, match);
        
    }
}
