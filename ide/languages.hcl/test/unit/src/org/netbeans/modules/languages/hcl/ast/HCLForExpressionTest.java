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
package org.netbeans.modules.languages.hcl.ast;

import static org.junit.Assert.*;
import static org.netbeans.modules.languages.hcl.ast.HCLExpressionTestSupport.*;
import org.junit.Test;

/**
 *
 * @author lkishalmi
 */
public class HCLForExpressionTest {

    @Test
    public void testForTuple() {
        HCLExpression expr = parse("[for i in local.t : i]");
        if (expr instanceof HCLForExpression.Tuple t) {
            assertNull(t.keyVar());
            assertNull(t.condition());
        } else {
            fail();
        }
    }

    @Test
    public void testForTupleSelf() {
        assertExpr("[for i in l:i]");
        assertExpr("[for i,j in l:i]");
        assertExpr("[for i in l:i if i>0]");
    }

    @Test
    public void testForObjectSelf() {
        assertExpr("{for k,v in l:k=>v}");
        assertExpr("{for k,v in l:k=>v if v!=null}");
        assertExpr("{for k,v in l:k=>v... if v!=null}");
    }

    @Test
    public void testForObject() {
        HCLExpression expr = parse("{for k, v in local.m : k => v}");
        if (expr instanceof HCLForExpression.Object o) {
            assertEquals("k", HCLExpression.asString(o.keyVar()));
            assertNull(o.condition());
        } else {
            fail();
        }
    }

    @Test
    public void testForObjectCondition() {
        HCLExpression expr = parse("{for k, v in local.m : k => v if v > 0}");
        if (expr instanceof HCLForExpression.Object o) {
            assertExpr("k", o.keyVar());
            assertExpr("v>0", o.condition());
        } else {
            fail();
        }
    }

}
