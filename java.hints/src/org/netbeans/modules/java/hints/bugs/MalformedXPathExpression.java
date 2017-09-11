/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
