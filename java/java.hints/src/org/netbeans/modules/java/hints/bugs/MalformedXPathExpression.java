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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.util.TreePath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;

import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * The hint checks XPaths explicitly evaluated or compiled. It will not check any other places where
 * XPath might be passed as a String.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - xpath text",
    "# {1} - XPath parser error message",
    "ERR_MalformedXPathExpression=Malformed XPath expression {0}: {1}"
})
@Hint(
    displayName = "#DN_MalformedXPathExpression",
    description = "#DESC_MalformedXPathExpression",
    enabled = true,
    category = "bugs",
    options = Hint.Options.QUERY,
    suppressWarnings = { "MalformedXPath" }
)
public class MalformedXPathExpression {
    @TriggerPatterns({
        @TriggerPattern(value = "$xpath.evaluate($expr, $params)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "javax.xml.xpath.XPath")
            }),
        @TriggerPattern(value = "$xpath.compile($expr)", constraints = {
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "javax.xml.xpath.XPath")
        }),
        
        // Xalan-J APIs
        @TriggerPattern(value = "org.apache.xpath.XPathAPI.eval($n, $expr)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "org.apache.xpath.XPathAPI.eval($n, $expr, $ns)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "org.apache.xpath.XPathAPI.eval($n, $expr, $pr)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "org.apache.xpath.XPathAPI.selectNodeIterator($n, $expr)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "org.apache.xpath.XPathAPI.selectNodeIterator($n, $expr, $ns)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "org.apache.xpath.XPathAPI.selectNodeList($n, $expr)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "org.apache.xpath.XPathAPI.selectNodeList($n, $expr, $ns)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "org.apache.xpath.XPathAPI.selectSingleNode($n, $expr)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "org.apache.xpath.XPathAPI.selectSingleNode($n, $expr, $ns)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "new org.apache.xpath.XPath($expr)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),
        @TriggerPattern(value = "new org.apache.xpath.XPath($expr, $params$)", constraints = @ConstraintVariableType(variable = "$expr", type = "java.lang.String")),

        // Apache commons JXPath
        @TriggerPattern(value = "org.apache.commons.jxpath.JXPathContext.compile($expr)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String")
            }),
        @TriggerPattern(value = "$xpath.createPath($expr, $params)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.getNodeSetByKey($expr, $params)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.getPointer($expr)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.getValue($expr)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.getValue($expr, $params)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.iterate($expr)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.iteratePointers($expr)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.removeAll($expr)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.removePath($expr)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.selectNodes($expr)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.selectSingleNode($expr)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
        @TriggerPattern(value = "$xpath.setValue($expr, $val)" , 
            constraints = { 
                @ConstraintVariableType(variable = "$expr", type = "java.lang.String"), 
                @ConstraintVariableType(variable = "$xpath", type = "org.apache.commons.jxpath.JXPathContext")
            }),
    })
    
    public static ErrorDescription run(HintContext ctx) {
        TreePath exprPath = ctx.getVariables().get("$expr"); // NOI18N
        Object o = ArithmeticUtilities.compute(ctx.getInfo(), exprPath, true, true);
        if (!(o instanceof String)) {
            // not a constant, or not a known String value
            return null;
        }
        XPathFactory f = XPathFactory.newInstance();
        try {
            f.newXPath().compile(o.toString());
            return null;
        } catch (XPathExpressionException ex) {
            return ErrorDescriptionFactory.forTree(ctx, exprPath, Bundle.ERR_MalformedXPathExpression(o.toString(), ex.getLocalizedMessage()));
        }
    }
}
