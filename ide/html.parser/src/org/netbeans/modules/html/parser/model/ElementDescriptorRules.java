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
