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

import org.junit.Test;
import static org.junit.Assert.*;

import static org.netbeans.modules.languages.hcl.ast.HCLExpression.parse;

/**
 *
 * @author lkishalmi
 */
public class HCLOperationsTest {

    @Test
    public void testResolveVar() throws Exception {
        HCLExpression exp = parse("var.key");
        assertTrue(exp instanceof HCLResolveOperation.Attribute);
        HCLResolveOperation.Attribute  resolve = (HCLResolveOperation.Attribute) exp;
        assertEquals("key", resolve.attr().id());
        assertTrue(resolve.base() instanceof HCLVariable);
        assertEquals("var", ((HCLVariable)resolve.base()).name().id());
    }

    @Test
    public void testResolveIndex1() throws Exception {
        HCLExpression exp = parse("a[0]");
        assertTrue(exp instanceof HCLResolveOperation.Index);
        HCLResolveOperation.Index  resolve = (HCLResolveOperation.Index) exp;
        assertEquals("0", ((HCLLiteral.NumericLit)resolve.index()).value());
        assertTrue(resolve.base() instanceof HCLVariable);
        assertEquals("a", ((HCLVariable)resolve.base()).name().id());
    }
    
    @Test
    public void testResolveIndex2() throws Exception {
        HCLExpression exp = parse("a.1.b");
        assertTrue(exp instanceof HCLResolveOperation.Attribute);
        HCLResolveOperation.Attribute  resolve = (HCLResolveOperation.Attribute) exp;

        assertTrue(resolve.base() instanceof HCLResolveOperation.Index);
        HCLResolveOperation.Index  resolve2 = (HCLResolveOperation.Index) resolve.base();
        assertEquals("1", ((HCLLiteral.NumericLit)resolve2.index()).value());
        assertTrue(resolve2.base() instanceof HCLVariable);
        assertEquals("a", ((HCLVariable)resolve2.base()).name().id());
    }
    
    @Test
    public void testConditional() throws Exception {
        HCLExpression exp = parse("a == b ? 1 : var");
        assertTrue(exp instanceof HCLConditionalOperation);
        HCLConditionalOperation cond = (HCLConditionalOperation) exp;
        assertTrue(cond.condition() instanceof HCLArithmeticOperation.Binary);
        assertTrue(cond.trueValue() instanceof HCLLiteral.NumericLit);
        assertTrue(cond.falseValue() instanceof HCLVariable);
        
        
    }
}
