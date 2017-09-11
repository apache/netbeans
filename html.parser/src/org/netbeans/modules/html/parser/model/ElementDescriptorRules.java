/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Some additional html5 metadata
 *
 * @author marekfukala
 */
public class ElementDescriptorRules {

    private static final Map<ContentType, Collection<ElementDescriptor>> CONTENTTYPE2ELEMENTS = new EnumMap<ContentType, Collection<ElementDescriptor>>(ContentType.class);

    //manually extracted from http://www.whatwg.org/specs/web-apps/current-work/multipage/syntax.html#optional-tags
    //
    //XXX in most cases the rules are not valid in all contexts, but only under
    //some circumstances depending on the context.
    public static final Collection<ElementDescriptor> OPTIONAL_OPEN_TAGS =
            EnumSet.of(ElementDescriptor.HTML, ElementDescriptor.HEAD,
            ElementDescriptor.BODY, ElementDescriptor.COLGROUP,
            ElementDescriptor.TBODY);

    public static final Collection<ElementDescriptor> OPTIONAL_END_TAGS =
            EnumSet.of(ElementDescriptor.HTML, ElementDescriptor.HEAD,
            ElementDescriptor.BODY, ElementDescriptor.LI,
            ElementDescriptor.DT, ElementDescriptor.DD,
            ElementDescriptor.P, ElementDescriptor.RP,
            ElementDescriptor.RT, ElementDescriptor.OPTGROUP,
            ElementDescriptor.OPTION, ElementDescriptor.COLGROUP,
            ElementDescriptor.THEAD, ElementDescriptor.TBODY,
            ElementDescriptor.TFOOT, ElementDescriptor.TR,
            ElementDescriptor.TH, ElementDescriptor.TD);

    //manually regexped from http://www.w3.org/TR/MathML/appendixl.html#index.elem
    public static final Collection<String> MATHML_TAG_NAMES = new HashSet<String>(Arrays.asList(new String[]{
        "abs", "and", "annotation", "annotation-xml", "apply", "approx", "arccos", "arccosh",
        "arccot", "arccoth", "arccsc", "arccsch", "arcsec", "arcsech", "arcsin", "arcsinh",
        "arctan", "arctanh", "arg", "bvar", "card", "cartesianproduct", "ceiling", "ci", "cn",
        "codomain", "complexes", "compose", "condition", "conjugate", "cos", "cosh", "cot", "coth",
        "csc", "csch", "csymbol", "curl", "declare", "degree", "determinant", "diff", "divergence",
        "divide", "domain", "domainofapplication", "emptyset", "encoding", "eq", "equivalent",
        "eulergamma", "exists", "exp", "exponentiale", "factorial", "factorof", "false", "floor",
        "fn", "forall", "function", "gcd", "geq", "grad", "gt", "ident", "image", "imaginary",
        "imaginaryi", "implies", "in", "infinity", "int", "integers", "intersect", "interval",
        "inverse", "lambda", "laplacian", "lcm", "leq", "limit", "list", "ln", "log", "logbase",
        "lowlimit", "lt", "apply", "mrow", "maction", "malign", "maligngroup", "malignmark",
        "malignscope", "math", "matrix", "matrixrow", "max", "mean", "median", "menclose", "merror",
        "mfenced", "mfrac", "mfraction", "mglyph", "mi", "min", "minus", "mlabeledtr", "mmultiscripts",
        "mn", "mo", "mode", "moment", "momentabout", "mover", "mpadded", "mphantom", "mprescripts",
        "mroot", "mrow", "ms", "mspace", "msqrt", "mstyle", "msub", "msubsup", "msup", "mtable",
        "mtd", "mtext", "mtr", "munder", "munderover", "naturalnumbers", "neq", "none", "not",
        "notanumber", "notin", "notprsubset", "notsubset", "or", "otherwise", "outerproduct",
        "partialdiff", "pi", "piece", "piecewice", "piecewise", "plus", "power", "primes", "product",
        "prsubset", "quotient", "rationals", "real", "reals", "reln", "rem", "root", "scalarproduct",
        "sdev", "sec", "sech", "selector", "semantics", "sep", "set", "setdiff", "sin", "sinh", "subset",
        "sum", "tan", "tanh", "tendsto", "times", "transpose", "true", "union", "uplimit", "variance",
        "vector", "vectorproduct", "xor"}));//NOI18N

    //manually regexped from http://www.w3.org/TR/SVGTiny12/elementTable.html
    public static final Collection<String> SVG_TAG_NAMES = new HashSet<String>(Arrays.asList(new String[]{
        "a", "animate", "animateColor", "animateMotion", "animateTransform", "animation", "audio",
        "circle", "defs", "desc", "discard", "ellipse", "font", "font-face", "font-face-src",
        "font-face-uri", "foreignObject", "g", "glyph", "handler", "hkern", "image", "line",
        "linearGradient", "listener", "metadata", "missing-glyph", "mpath", "path", "polygon",
        "polyline", "prefetch", "radialGradient", "rect", "script", "set", "solidColor", "stop",
        "svg", "switch", "tbreak", "text", "textArea", "title", "tspan", "use", "video"
    }));//NOI18N

    public static synchronized Collection<ElementDescriptor> getElementsByContentType(ContentType ctype) {
        //the mapping needs to be extracted from the reverse information element->ctype first
        Collection<ElementDescriptor> members = CONTENTTYPE2ELEMENTS.get(ctype);
        if(members == null) {
            //init
            members = new LinkedList<ElementDescriptor>();
            for(ElementDescriptor ed : ElementDescriptor.values()) {
                if(ed.getCategoryTypes().contains(ctype)) {
                    members.add(ed);
                }
            }
            CONTENTTYPE2ELEMENTS.put(ctype, members);
        }
        return members;
    }

}
