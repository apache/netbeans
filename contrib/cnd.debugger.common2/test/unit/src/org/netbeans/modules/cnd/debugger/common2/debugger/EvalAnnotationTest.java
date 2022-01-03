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
package org.netbeans.modules.cnd.debugger.common2.debugger;

import org.junit.Test;
import org.netbeans.modules.cnd.debugger.common2.debugger.test.CndBaseTestCase;
import org.openide.text.Line;

/**
 *
 */
public class EvalAnnotationTest extends CndBaseTestCase {
    
    public EvalAnnotationTest(String name) {
        super(name);
    }
    
    private Line.Part getLinePart(final int col) {
        return new Line.Part() {
            @Override
            public int getColumn() {
                return col;
            }

            @Override
            public int getLength() {
                return 0;
            }

            @Override
            public Line getLine() {
                return null;
            }

            @Override
            public String getText() {
                return "";
            }
        };
    }

    @Test
    public void test206740() {
        String expr = EvalAnnotation.extractExpr(getLinePart(11), "case ABC::DEF:");
        assertEquals("ABC::DEF", expr);
    }
    
    @Test
    public void test206740_2() {
        String expr = EvalAnnotation.extractExpr(getLinePart(6), "case ABC::DEF:");
        assertEquals("ABC::DEF", expr);
    }
}
