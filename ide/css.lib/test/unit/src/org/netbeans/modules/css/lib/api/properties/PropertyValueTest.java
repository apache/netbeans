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
package org.netbeans.modules.css.lib.api.properties;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.properties.GrammarParser;

/**
 *
 * @author mfukala@netbeans.org
 */
public class PropertyValueTest extends CssTestBase {

    public PropertyValueTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
//        GrammarResolver.setLogging(GrammarResolver.Log.ALTERNATIVES, true);
//        PRINT_INFO_IN_ASSERT_RESOLVE = true;
    }
    
    @Override
    protected Collection<GrammarResolver.Feature> getEnabledGrammarResolverFeatures() {
        return Collections.singletonList(GrammarResolver.Feature.keepAnonymousElementsInParseTree);
    }
    

    public void testAlternativesComplicated1() {
        String grammar1 = "[ marek  jitka  [ [ ovecka | bubu ]? nee ] ] | [ marek jitka [ tobik | bibik ] ] ";
        assertAlternatives(grammar1, "marek jitka", "ovecka", "bubu", "nee", "tobik", "bibik");
    }

    public void testAlternativesComplicated2() {
        String grammar2 = "[ marek  jitka  [ [ ovecka | bubu ]? nee ] ] | [ marek [ jitka | x ] [ tobik | bibik ] ]  | [ marek tohlene ]";
        assertAlternatives(grammar2, "marek jitka tobik");
    }

    public void testAlternativesComplicated21() {
        String grammar2 = "[ marek  jitka  [ [ ovecka | bubu ]? nee ] ] | [ marek [ jitka | x ] [ tobik | bibik ] ]  | [ marek tohlene ]";
        assertAlternatives(grammar2, "marek jitka", "ovecka", "bubu", "nee", "tobik", "bibik");
    }

    public void testAlternativesComplicated3() {
        String grammar2 = "[ [ a b ] ] | [ a c ]";

        assertAlternatives(grammar2, "a", "b", "c");
        assertAlternatives(grammar2, "a b");
    }

    public void testAlternativesComplicated4() {
        String grammar2 = "[ a b c  ] | [ a X ]";
        assertAlternatives(grammar2, "a b", "c");
    }

    public void testAlternativesComplicated41() {
        String grammar2 = "[ a b C ] | [ a b D ]  | [ a E ]";
        assertAlternatives(grammar2, "a b", "C", "D");
    }

    public void testAlternativesSet() {
        //set
        assertAlternatives("a | b | c", "", "a", "b", "c", "initial", "inherit", "unset");
        assertAlternatives("a | b | c", "a");
        assertAlternatives("a | b | c", "b");
        assertAlternatives("a | b | c", "c");

    }

    public void testAlternativesList() {
        //list
        assertAlternatives("a || b || c", "", "a", "b", "c", "initial", "inherit", "unset");
        assertAlternatives("a || b || c", "a", "b", "c");
        assertAlternatives("a || b || c", "b", "a", "c");
        assertAlternatives("a || b || c", "c", "a", "b");
        assertAlternatives("a || b || c", "a b", "c");
        assertAlternatives("a || b || c", "b a", "c");
        assertAlternatives("a || b || c", "a c", "b");
        assertAlternatives("a || b || c", "c a", "b");
        assertAlternatives("a || b || c", "b c", "a");
        assertAlternatives("a || b || c", "c b", "a");

    }

    public void testAlternativesSequence() {
        //sequence
        assertAlternatives("a b c", "", "a", "inherit", "initial", "unset");
        assertAlternatives("a b c", "a", "b");
        assertAlternatives("a b c", "a b", "c");
        assertAlternatives("a b c", "a b c");
    }

    public void testAlternativesOfSequenceInSequence() {
        String grammar = "marek  jitka  [ ovecka beranek ]";

        assertAlternatives(grammar, "marek jitka", "ovecka");
        assertAlternatives(grammar, "marek jitka ovecka", "beranek");
        assertAlternatives(grammar, "marek jitka beranek");
    }

    public void testFontAlternatives() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font");
        String text = "italic small-caps 30px";

        assertAlternatives(p.getGrammar(), text,
                "fantasy", "serif", "!string", "sans-serif", "monospace", "/", 
                "!identifier", "cursive", "var");

    }

//    public void testVoiceFamily() {
//        PropertyDefinition p = Properties.getPropertyDefinition( "voice-family");
//        assertAlternatives(p.getGrammar(), "male", ",", "!integer");
//        assertAlternatives(p.getGrammar(), "male,", "old","!ident","!integer","!string","neutral","young","child","!ident","female","!integer","!string","male");
//
//        assertAlternatives(p.getGrammar(), "", "child","!ident","female","!integer","!string","male");
//    }
    public void testPaddingAlternatives() {
        PropertyDefinition p = Properties.getPropertyDefinition( "padding");
        assertAlternatives(p.getGrammar(), "", "auto", "!percentage", "!length",
                "-", "calc", "inherit", "initial", "unset", "var", "mod", "log",
                "cos", "sign", "atan", "min", "sqrt", "hypot", "sin", "pow",
                "rem", "exp", "clamp", "atan2", "tan", "max", "acos", "abs",
                "asin", "round");

    }

    public void testAlternativesInGroupMultiplicitySimple() {
        assertAlternatives("[ marek ]*", "marek", "marek");
        assertAlternatives("[ marek ]*", "marek marek", "marek");
    }

    public void testAlternativesInGroupMultiplicity() {
        assertAlternatives("[ marek | jitka ]*", "marek", "marek", "jitka");
        assertAlternatives("[ marek | jitka ]*", "jitka", "marek", "jitka");
        assertAlternatives("[ marek | jitka ]*", "jitka marek jitka", "marek", "jitka");

        assertAlternatives("[ marek jitka? ovecka ]*", "marek jitka", "ovecka");
        assertAlternatives("[ marek jitka? ovecka ]*", "marek ovecka", "marek");

    }

    public void testFontThoroughly() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font");
        assertAlternatives(p.getGrammar(), "20px",
                "fantasy", "serif", "!string", "sans-serif", "monospace", "/", "!identifier", "cursive", "var");
        assertAlternatives(p.getGrammar(), "20px /",
                "normal", "none", "!number", "!length", "!percentage", "-", "calc", "var",
                "mod", "log", "cos", "sign", "atan", "min", "sqrt", "hypot", "sin", "pow",
                "rem", "exp", "clamp", "atan2", "tan", "max", "acos", "abs", "asin",
                "round");
        assertAlternatives(p.getGrammar(), "20px / 5pt",
                "fantasy","serif","!string","sans-serif","monospace","!identifier","cursive", "var");
        assertAlternatives(p.getGrammar(), "20px / 5pt cursive", 
                ",", "!identifier");
    }

    public void testFontThoroughly2() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font");
        assertAlternatives(p.getGrammar(), "italic",
                "small-caps", "800", "normal", "lighter", "smaller", "600", "bold",
                "700", "!length", "-", "xx-small", "bolder", "100", "300", "!percentage",
                "200", "larger", "medium", "500", "x-large", "x-small", "400",
                "xx-large", "900", "small", "large", "calc",  "fantasy","monospace",
                "!identifier","!string","/","cursive","serif","sans-serif", "var",
                "mod", "log", "cos", "sign", "atan", "min", "sqrt", "hypot", "sin",
                "pow", "rem", "exp", "clamp", "atan2", "tan", "max", "acos", "abs",
                "asin", "round");

        assertAlternatives(p.getGrammar(), "italic large",
                "fantasy", "serif", "sans-serif", "monospace", "/", "cursive", "!string", "!identifier", "var");

        assertAlternatives(p.getGrammar(), "italic large / ",
                "!percentage", "normal", "!length", "-", "none", "!number", "calc",
                "var", "mod", "log", "cos", "sign", "atan", "min", "sqrt", "hypot",
                "sin", "pow", "rem", "exp", "clamp", "atan2", "tan", "max", "acos",
                "abs", "asin", "round");

        assertAlternatives(p.getGrammar(), "italic large / normal",
                "fantasy", "serif", "sans-serif", "monospace", "cursive", "!string", "!identifier", "var");
    }

    public void testBackgroundRGBAlternatives() {
        PropertyDefinition p = Properties.getPropertyDefinition( "background");
        assertAlternatives(p.getGrammar(), "rgb", "(");
        assertAlternatives(p.getGrammar(), "rgb(",
                "!percentage", "!number", "-", "calc", "var", "mod", "log", "cos",
                "sign", "atan", "min", "sqrt", "hypot", "sin", "pow", "rem",
                "exp", "clamp", "atan2", "tan", "max", "acos", "abs", "asin",
                "round");
        assertAlternatives(p.getGrammar(), "rgb(10%", ",");
        assertAlternatives(p.getGrammar(), "rgb(", "!percentage", "!number", 
                "-", "calc", "var", "mod", "log", "cos", "sign", "atan", "min",
                "sqrt", "hypot", "sin", "pow", "rem", "exp", "clamp", "atan2",
                "tan", "max", "acos", "abs", "asin", "round");
        assertAlternatives(p.getGrammar(), "rgb(10%, 20", ",");
        assertAlternatives(p.getGrammar(), "rgb(10%, 20, 6%", ")");
    }

    public void testAlternativesOfPartialyResolvedSequenceInListGroup() {
        String g = "[ [ Ema ma misu] || prd";
        assertAlternatives(g, "Ema", "ma");
        assertAlternatives(g, "Ema ma", "misu");
        assertAlternatives(g, "Ema ma misu", "prd");
        assertAlternatives(g, "prd", "Ema");
        assertAlternatives(g, "prd Ema", "ma");
        assertAlternatives(g, "prd Ema ma", "misu");
        assertAlternatives(g, "prd Ema ma misu");
    }

    public void testJindrasCase() {
        //#142254
        String g = "[ [ x || y ] || b";
        assertAlternatives(g, "x b"); //no alternatives
    }

    public void testFontFamily2() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font-family");

        assertAlternatives(p.getGrammar(), "",
                "fantasy", "serif", "sans-serif", "monospace", "cursive",
                "!string", "!identifier", "var", "initial", "inherit", "unset");

    }

    public void testFontFamilyInheritProblem() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font-family");
        assertAlternatives(p.getGrammar(), "inherit");
//        assertAlternatives(p.getGrammar(), "", "inherit");
    }

    public void testTheBorderCaseSimplified() {
        String g = " a || b || c";

        assertAlternatives(g, "", "a", "b", "c", "initial", "inherit", "unset");
        assertAlternatives(g, "a", "b", "c");

        assertAlternatives(g, "a b", "c");
        assertAlternatives(g, "b a", "c");

        assertAlternatives(g, "b c", "a");
        assertAlternatives(g, "c b", "a");

        assertAlternatives(g, "c a", "b");
        assertAlternatives(g, "a c", "b");

        assertAlternatives(g, "a b c");
        assertAlternatives(g, "c b a");
        assertAlternatives(g, "a c b");
        assertAlternatives(g, "b a c");
    }

    public void testTheBorderCase() {
        PropertyDefinition p = Properties.getPropertyDefinition( "border");
        assertAlternatives(p.getGrammar(), "red dashed",
                "thick", "thin", "!length", "-", "medium", "calc", "var", "mod",
                "log", "cos", "sign", "atan", "min", "sqrt", "hypot", "sin",
                "pow", "rem", "exp", "clamp", "atan2", "tan", "max", "acos",
                "abs", "asin", "round");

        assertAlternatives(p.getGrammar(), "red dashed 20px");
    }

    public void testTheBackgroundCase() {
        PropertyDefinition p = Properties.getPropertyDefinition( "background");
        assertResolve(p.getGrammar(), "aliceblue");
        assertAlternatives(p.getGrammar(), "aliceblue",
                "repeating-linear-gradient", "padding-box", "content-box", "round",
                "repeat", "!length", "-", "repeating-radial-gradient", "space", "!percentage",
                "fixed", "border-box", "center", "no-repeat", "none", "left", "right",
                "top", "element", "scroll", "repeat-y", "linear-gradient", "repeat-x",
                "image", "!uri", "cross-fade", "radial-gradient", "bottom", "local", 
                "calc", "var", "mod", "log", "cos", "sign", "atan", "min", "sqrt",
                "hypot", "sin", "pow", "rem", "exp", "clamp", "atan2", "tan", "max",
                "acos", "abs", "asin");
    }

    public void testTheBackgroundCase2() {
        PropertyDefinition p = Properties.getPropertyDefinition( "background");
        assertResolve(p.getGrammar(), "aliceblue bottom / auto");
        assertAlternatives(p.getGrammar(), "aliceblue bottom / auto",
                "repeating-linear-gradient", "element", "padding-box", "scroll", ""
                + "content-box", "repeat-y", "linear-gradient", "repeat-x", "image",
                "round", "!length", "-", "repeat", "repeating-radial-gradient", "space",
                "fixed", "!percentage", "!uri", "border-box", "cross-fade",
                "radial-gradient", "no-repeat", "auto", "none", "local", "calc",
                "var", "mod", "log", "cos", "sign", "atan", "min", "sqrt", "hypot",
                "sin", "pow", "rem", "exp", "clamp", "atan2", "tan", "max", "acos",
                "abs", "asin");


    }

    public void testTheBackgroundCaseSimplified() {
        String g = " [ a , ]* b";
        assertAlternatives(g, "", "a", "b", "inherit", "initial", "unset");
        assertAlternatives(g, "a", ",");
        assertAlternatives(g, "a,", "b", "a");
        assertAlternatives(g, "a,a,", "b", "a");
        assertAlternatives(g, "b");
        assertAlternatives(g, "a,b");
        assertAlternatives(g, "a,a,b");
    }

    public void testTheBackgroundCaseSimplified2() {
        String g = " [ [ a || b ] , ]* [ a || c ]";
        assertAlternatives(g, "", "a", "b", "c", "inherit", "initial", "unset");

        assertAlternatives(g, "a", ",", "b", "c");

        assertAlternatives(g, "a b ,", "a", "b", "c");

        assertAlternatives(g, "b", ",", "a");

        assertAlternatives(g, "c", "a");
    }

    //Bug 205893 - font-family completion issue
    public void testFontFamily() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font-family");
//        assertResolve(p.getGrammar(), "fantasy");
        assertAlternatives(p.getGrammar(), "fantasy", ",", "!identifier");

        assertAlternatives(p.getGrammar(), "fantasy, ", 
                "fantasy","serif","sans-serif","monospace","cursive", "!string","!identifier","var");
        
        assertAlternatives(p.getGrammar(), "fantasy, monospace", 
                ",", "!identifier");

        assertAlternatives(p.getGrammar(), "fantasy, monospace, ",
                "fantasy", "serif", "sans-serif", "monospace", "cursive", "!string", "!identifier","var");

    }

    public void testAnimation() {
        PropertyDefinition p = Properties.getPropertyDefinition( "animation");
//        assertResolve(p.getGrammar(), "fantasy");
        assertAlternatives(p.getGrammar(), 
                "cubic-bezier", "alternate", "linear", "cubic-bezier", "normal",
                "ease", "(", "!time", "ease-in", ",", "ease-in-out", "ease-out",
                "infinite", "var", "calc", "!number", "mod", "log", "cos",
                "sign", "atan", "min", "sqrt", "hypot", "sin", "pow", "rem",
                "exp", "clamp", "atan2", "tan", "max", "acos", "abs", "asin",
                "round");

    }

    public void testAltsMinus() {
        String g = "-? x";
        assertAlternatives(g, "", "x", "-", "initial", "inherit", "unset");
    }

    public void testGetParseTree() {
        String g = " a [ b [ c | d ]* ]";
        
        GrammarResolver resolver = new GrammarResolver(GrammarParser.parse(g));
        resolver.enableFeature(GrammarResolver.Feature.keepAnonymousElementsInParseTree);
        GrammarResolverResult result = resolver.resolve("a b d d d c d");
        
        Node root = result.getParseTree();
        
//        dumpTree(root);
        assertNotNull(root);
//        assertNull(root.parent()); //root node has no parent
        
        Collection<Node> ch = root.children();
        assertEquals(2, ch.size());
        
        Iterator<Node> i = ch.iterator();
        Node ch1 = i.next();
        assertNotNull(ch1);
        assertTrue(ch1 instanceof Node.ResolvedTokenNode);
        
        Node.ResolvedTokenNode tokenNode = (Node.ResolvedTokenNode)ch1;
        assertEquals("a", tokenNode.image().toString());
//        assertEquals(root, tokenNode.parent());
        
        Node ch2 = i.next();
        assertNotNull(ch2);
        assertTrue(ch2 instanceof Node.GroupNodeImpl);
//        assertEquals(root, ch2.parent());
        

    }
    
    public void testGetSimpleParseTree2() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font-family");
        ResolvedProperty pv = new ResolvedProperty(p, "fantasy, monospace");
        
        Node root = pv.getParseTree();
        dumpTree(root);
        
        assertNotNull(root);
        assertEquals("font-family", root.name());
        
        Collection<Node> ch = root.children();
        assertEquals(3, ch.size());
        
        Iterator<Node> i = ch.iterator();
        Node ch1 = i.next();
        assertNotNull(ch1);
        assertTrue(ch1 instanceof Node.GroupNodeImpl);
        assertEquals("@generic-family", ch1.name());
        
        ch = ch1.children();
        assertEquals(1, ch.size());
        
        i = ch.iterator();
        Node ch2 = i.next();
        assertNotNull(ch2);
        assertTrue(ch2 instanceof Node.ResolvedTokenNode);
        assertEquals("fantasy(IDENT;0-7)", ch2.toString());
        
    }

    public void testPerformance_Parse_Resolve() {
//        Last results from my MacBook Pro:
//        -------------------------------------------
//        Testing parsing a grammar and resolving an input by the grammar...
//        50 iterations took 516 ms.
//        50 iterations took 297 ms.
//        50 iterations took 170 ms.
//        Average time of 50 iterations is 327
//        Average run of one resolve is 6 ms

        PropertyDefinition p = Properties.getPropertyDefinition( "background");

        //dry run
        assertResolve(p.getGrammar(), "aliceblue bottom / auto");

        System.out.println("Testing parsing a grammar and resolving an input by the grammar...");

        int loops = 3;
        int iterations = 50;
        int sum = 0;

        for (int l = 0; l < loops; l++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                assertResolve(p.getGrammar(), "aliceblue bottom / auto");
            }
            long stop = System.currentTimeMillis();

            System.out.println(String.format("%s iterations took %s ms.", iterations, (stop - start)));

            sum += (stop - start);
        }

        System.out.println(String.format("Average time of %s iterations is %s", iterations, (sum / loops)));
        System.out.println(String.format("Average run of one resolve is %s ms", (sum / loops / iterations)));
    }

    public void testPerformance_Resolve() {
//        Last results from my MacBook Pro:
//        -------------------------------------------
//        Testing parsing a grammar and resolving an input by the grammar...
//        50 iterations took 188 ms.
//        50 iterations took 121 ms.
//        50 iterations took 87 ms.
//        Average time of 50 iterations is 132
//        Average run of one resolve is 2 ms


        PropertyDefinition p = Properties.getPropertyDefinition( "background");

        //dry run
        assertResolve(p.getGrammarElement(null), "aliceblue bottom / auto");

        System.out.println("Testing parsing a grammar and resolving an input by the grammar...");

        int loops = 3;
        int iterations = 50;
        int sum = 0;

        for (int l = 0; l < loops; l++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                assertResolve(p.getGrammarElement(null), "aliceblue bottom / auto");
            }
            long stop = System.currentTimeMillis();

            System.out.println(String.format("%s iterations took %s ms.", iterations, (stop - start)));

            sum += (stop - start);
        }

        System.out.println(String.format("Average time of %s iterations is %s", iterations, (sum / loops)));
        System.out.println(String.format("Average run of one resolve is %s ms", (sum / loops / iterations)));
    }
}
