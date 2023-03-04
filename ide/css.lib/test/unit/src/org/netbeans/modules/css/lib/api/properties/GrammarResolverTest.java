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
package org.netbeans.modules.css.lib.api.properties;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.properties.GrammarResolver.Feature;
import org.netbeans.modules.css.lib.properties.GrammarParser;

/**
 *
 * @author marekfukala
 */
public class GrammarResolverTest extends CssTestBase {

    public GrammarResolverTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
//        PRINT_INFO_IN_ASSERT_RESOLVE = true;
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
//        GrammarParseTreeBuilder.DEBUG = true;
    }

    @Override
    protected Collection<Feature> getEnabledGrammarResolverFeatures() {
        return Collections.singletonList(GrammarResolver.Feature.keepAnonymousElementsInParseTree);
    }
    
    public void testParseVerySimpleGrammar() {
        String g = "a | b";
        assertResolve(g, "a");
        assertResolve(g, "b");
    }
    
    public void testParseSimpleGrammar() {
        String grammar = " function ( [ !string | !identifier ] )";

        assertResolve(grammar, "function(ahoj)");
        assertResolve(grammar, "function(\"ahoj\")");
    }

    public void testParseSimpleAmbiguousGrammar() {
        String grammar = " [ function ( !string ) ] | [ function ( !identifier ) ]";

        assertResolve(grammar, "function(ahoj)");
        assertResolve(grammar, "function(\"ahoj\")");
    }
    
    public void testParseSimpleAmbiguousGrammar2() {
        String grammar = "[ a ] | [ a b ]";
        assertResolve(grammar, "a b");
    }
    
    public void testParseSimpleAmbiguousGrammar3() {
        String grammar = "[ a b c ] | [ a b ] | [ a b d ]";
        assertResolve(grammar, "a b");
        assertResolve(grammar, "a b c");
        assertResolve(grammar, "a b d");
    }

    public void testOptinalMemberInSet() {
        String g = " a? | [ a b ]";
        
        assertResolve(g, "");
        assertResolve(g, "a");
        assertResolve(g, "a b");
    }
    
    public void testAmbiguousGrammarParsingPrecendence() {
        String g = " [ !identifier b ] | [ keyword b ]";
        
        
        GroupGrammarElement tree = GrammarParser.parse(g);
        
        ResolvedProperty v = assertResolve(tree, "keyword b");
        
        assertTrue(v.isResolved());
        
        List<ResolvedToken> resolved = v.getResolvedTokens();
        
        assertNotNull(resolved);
        assertEquals(2, resolved.size());
        
        ResolvedToken first = resolved.get(0); 
        
        //now check the group, matching keywords should have a precedence before
        //property acceptors
        
        GrammarElement ge1 = first.getGrammarElement();
        assertNotNull(ge1);
        
        assertEquals("[S0]/[L2]/keyword", ge1.path());

        ResolvedToken second = resolved.get(1); 
        
        GrammarElement ge2 = second.getGrammarElement();
        assertNotNull(ge2);
        
        assertEquals("[S0]/[L2]/b", ge2.path());

    }
    
    public void testAmbiguousGrammarParsingPrecendence2() {
        //just test if we can handle such situation - two same paths
        //in such case the first found path shoud be used
        
        String g = " [ !identifier b ] | [ !identifier b ]";
                
        GroupGrammarElement tree = GrammarParser.parse(g);
        
        ResolvedProperty v = assertResolve(tree, "keyword b");
        
        assertTrue(v.isResolved());
        
        List<ResolvedToken> resolved = v.getResolvedTokens();
        
        assertNotNull(resolved);
        assertEquals(2, resolved.size());
        
        ResolvedToken first = resolved.get(0); 
        
        GrammarElement ge1 = first.getGrammarElement();
        assertNotNull(ge1);
        
        assertEquals("[S0]/[L1]/!identifier", ge1.path());

        ResolvedToken second = resolved.get(1); 
        
        GrammarElement ge2 = second.getGrammarElement();
        assertNotNull(ge2);
        
        assertEquals("[S0]/[L1]/b", ge2.path());

    }
    
    public void testAmbiguousGrammarParsingPrecendence3() {
        //just test if we can handle such situation - two same paths
        //in such case the first found path shoud be used
        
        String g = " [ keyword b ] | [ keyword b ]";
        
        GroupGrammarElement tree = GrammarParser.parse(g);
        
        ResolvedProperty v = assertResolve(tree, "keyword b");
        
        assertTrue(v.isResolved());
        
        List<ResolvedToken> resolved = v.getResolvedTokens();
        
        assertNotNull(resolved);
        assertEquals(2, resolved.size());
        
        ResolvedToken first = resolved.get(0); 
        
        GrammarElement ge1 = first.getGrammarElement();
        assertNotNull(ge1);
        
        assertEquals("[S0]/[L1]/keyword", ge1.path());

        ResolvedToken second = resolved.get(1); 
        
        GrammarElement ge2 = second.getGrammarElement();
        assertNotNull(ge2);
        
        assertEquals("[S0]/[L1]/b", ge2.path());

    }
    
    public void testAmbiguousGrammarInList() {
        String g = "[ a ] || [ a c ] || [ a d ]";
        assertResolve(g, "a c");
    }
    
     public void testAmbiguousGrammarListParsingPrecendence() {
        String g = " [ !identifier b ] || [ keyword b ]";
        
        GroupGrammarElement tree = GrammarParser.parse(g);
        
        ResolvedProperty v = assertResolve(tree, "keyword b");
        
        assertTrue(v.isResolved());
        
        List<ResolvedToken> resolved = v.getResolvedTokens();
        
        assertNotNull(resolved);
        assertEquals(2, resolved.size());
        
        ResolvedToken first = resolved.get(0); 
        
        //now check the group, matching keywords should have a precedence before
        //property acceptors
        
        GrammarElement ge1 = first.getGrammarElement();
        assertNotNull(ge1);
        
        assertEquals("[C0]/[L2]/keyword", ge1.path());

        ResolvedToken second = resolved.get(1); 
        
        GrammarElement ge2 = second.getGrammarElement();
        assertNotNull(ge2);
        
        assertEquals("[C0]/[L2]/b", ge2.path());

    }
    
    public void testAmbiguousGrammarListParsingPrecendence2() {
        //just test if we can handle such situation - two same paths
        //in such case the first found path shoud be used

        String g = " [ !identifier b ] || [ !identifier b ]";
        
        GroupGrammarElement tree = GrammarParser.parse(g);
        
        ResolvedProperty v = assertResolve(tree, "keyword b");
        
        assertTrue(v.isResolved());
        
        List<ResolvedToken> resolved = v.getResolvedTokens();
        
        assertNotNull(resolved);
        assertEquals(2, resolved.size());
        
        ResolvedToken first = resolved.get(0); 
        
        GrammarElement ge1 = first.getGrammarElement();
        assertNotNull(ge1);
        
        assertEquals("[C0]/[L2]/!identifier", ge1.path());

        ResolvedToken second = resolved.get(1); 
        
        GrammarElement ge2 = second.getGrammarElement();
        assertNotNull(ge2);
        
        assertEquals("[C0]/[L2]/b", ge2.path());

    }
    
    public void testAmbiguousGrammarListParsingPrecendence3() {
        //just test if we can handle such situation - two same paths
        //in such case the first found path shoud be used
        
        String g = " [ keyword b ] || [ keyword b ]";
        
        GroupGrammarElement tree = GrammarParser.parse(g);
        
        ResolvedProperty v = assertResolve(tree, "keyword b");
        
        assertTrue(v.isResolved());
        
        List<ResolvedToken> resolved = v.getResolvedTokens();
        
        assertNotNull(resolved);
        assertEquals(2, resolved.size());
        
        ResolvedToken first = resolved.get(0); 
        
        GrammarElement ge1 = first.getGrammarElement();
        assertNotNull(ge1);
        
        assertEquals("[C0]/[L2]/keyword", ge1.path());

        ResolvedToken second = resolved.get(1); 
        
        GrammarElement ge2 = second.getGrammarElement();
        assertNotNull(ge2);
        
        assertEquals("[C0]/[L2]/b", ge2.path());

    }
    
    public void testAmbiguousGrammarAnimation() {
        PropertyDefinition p = Properties.getPropertyDefinition( "@animation-arg");
        assertResolve(p.getGrammar(), "cubic-bezier");
        
    }
    
    public void testParseMultiplicity() {
        String grammar = " [ x ]{1,2} ";

        assertResolve(grammar, "x");
        assertResolve(grammar, "x x");
    }

    public void testParseMultiplicity1() {
        String grammar = " [ x ]? ";

        assertResolve(grammar, "");
        assertResolve(grammar, "x");
    }

    public void testParseMultiplicity2() {
        String grammar = " [ x ]+ ";  // 1 - inf

        assertResolve(grammar, "x");
        assertResolve(grammar, "x x x");
    }

    public void testParseMultiplicity3() {
        String grammar = " [ x ]* ";  // 0 - inf

        assertResolve(grammar, "");
        assertResolve(grammar, "x");
        assertResolve(grammar, "x x x");
    }

    public void testParseMultiplicity4() {
        String grammar = " [ x ]* y ";

        assertResolve(grammar, "x y");
        assertResolve(grammar, "x x x x y");
        assertResolve(grammar, "y");
    }

    public void testParseMultiplicity5() {
        String grammar = " [ x ]*  [ y ]* ";

        assertResolve(grammar, "x y");
        assertResolve(grammar, "x x x x");
        assertResolve(grammar, "y y y y");
        assertResolve(grammar, "x x y y");
    }

    public void testSetWithArbitraryMember() {
        String grammar = " [ a | b? ] c?";
        assertResolve(grammar, "a");
        assertResolve(grammar, "b");
        assertResolve(grammar, "a c");
        assertResolve(grammar, "b c");
        
        //bit weird, but the [ a | b? ] is matched by empty input 
        //resolving the arbitrary b element
        assertResolve(grammar, "c"); 
    }

    public void testListWithArbitraryMember() {
        String grammar = " [ a  b?  c? d ]";
        assertResolve(grammar, "a b c d");
        assertResolve(grammar, "a c d");
        assertResolve(grammar, "a b d");
        assertResolve(grammar, "a d");
    }

    public void testParseMultiplicity_error1() {
        //causing OOM - some infinite loop
        String grammar = "[ [ x ]* ]+";
        assertResolve(grammar, "");
    }

    public void testCollection() {
        String grammar = " [ a || b || c ]";
        assertResolve(grammar, "a b c");
        assertResolve(grammar, "c b a");
        assertResolve(grammar, "b c a");
        assertResolve(grammar, "a c b");
        assertResolve(grammar, "c a");
        assertResolve(grammar, "a c");
        assertResolve(grammar, "b a");
        assertResolve(grammar, "a b");
        assertResolve(grammar, "b c");
        assertResolve(grammar, "c b");
        assertResolve(grammar, "a");
        assertResolve(grammar, "b");
        assertResolve(grammar, "c");
    }

    public void testCollection2() {
        String grammar = " [ a || b ] c";
        assertResolve(grammar, "a b c");
        assertResolve(grammar, "a c");
        assertResolve(grammar, "b c");
        assertResolve(grammar, "b a c");
    }

    public void testCollectionWithArbitraryMembers() {
        String grammar = " [ a || b? || c ]";
        assertResolve(grammar, "a b c");
        assertResolve(grammar, "c b a");
        assertResolve(grammar, "c a");
        assertResolve(grammar, "a");
        assertResolve(grammar, "b");
        assertResolve(grammar, "c");
        assertResolve(grammar, "b c");
    }

    public void testCase1() {
        String g = "[ a || [ b | c] ]";
        assertResolve(g, "a");
        assertResolve(g, "b");
        assertResolve(g, "c");
        assertResolve(g, "a b");
        assertResolve(g, "a c");
        assertResolve(g, "c a");
        assertResolve(g, "b a");
    }

    public void testCase2() {
        String g = "[ a | b ]{1,4}";

        assertResolve(g, "a");
        assertResolve(g, "a b");
        assertResolve(g, "a b a");
        assertResolve(g, "a b a a");

        assertNotResolve(g, "");
        assertNotResolve(g, "a a a b b");

    }

    public void testCase3() {
        String g = "[ [ a | b ] ]{1,4}";

        assertResolve(g, "a");
        assertResolve(g, "a b");
        assertResolve(g, "a b a");
        assertResolve(g, "a b a a");

        assertNotResolve(g, "");
        assertNotResolve(g, "a a a b b");

    }

    public void testSimpleSet() {
        GroupGrammarElement e = GrammarParser.parse("one | two | three");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());
    }

    public void testSimpleList() {
        GroupGrammarElement e = GrammarParser.parse("one || two || three");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());
    }

    public void testMultiplicity() {
        GroupGrammarElement e = GrammarParser.parse("one+ two? three{1,4}");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());

        GrammarElement e1 = e.elements().get(0);
        assertEquals(1, e1.getMinimumOccurances());
        assertEquals(Integer.MAX_VALUE, e1.getMaximumOccurances());

        GrammarElement e2 = e.elements().get(1);
        assertEquals(0, e2.getMinimumOccurances());
        assertEquals(1, e2.getMaximumOccurances());

        GrammarElement e3 = e.elements().get(2);
        assertEquals(1, e3.getMinimumOccurances());
        assertEquals(4, e3.getMaximumOccurances());
    }

    public void testGroupsNesting() {
        GroupGrammarElement e = GrammarParser.parse("one [two] [[three] || [four]]");
        assertNotNull(e.elements());
        assertEquals(3, e.elements().size());
    }

    public void testConsume() {
        String rule = "[ [color]{1,4} | transparent ] | inherit ";

        assertTrue(assertResolve(rule, "color").isResolved());
        assertTrue(assertResolve(rule, "inherit").isResolved());
        assertTrue(assertResolve(rule, "color color").isResolved());
        assertTrue(assertResolve(rule, "color color color color").isResolved());
    }

    public void testConsumeFails() {
        String rule = "[ [color]{1,4} | transparent ] | inherit ";

        assertNotResolve(rule, "color inherit");
        assertNotResolve(rule, "color color color color color");
        assertNotResolve(rule, "transparent inherit");
    }

    public void testSequence() {
        String rule = "[marek]{1,2} jitka";
        String text = "marek marek jitka";

        ResolvedProperty csspv = assertResolve(rule, text);

        assertTrue(csspv.isResolved());
    }

    public void testSequenceFails() {
        assertResolve("marek jitka", "jitka", false);
    }

    public void testFont() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font");
        ResolvedProperty pv = assertResolve(p, "20% serif");
        assertTrue(pv.isResolved());
    }
   
    public void testZeroMultiplicity() {
        String rule = "[marek]?  [jitka]?  [ovecka]";
        String text = "ovecka";
        ResolvedProperty csspv = assertResolve(rule, text);
        assertTrue(csspv.isResolved());
    }

    public void testFontFamily() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font-family");

        assertTrue(new ResolvedProperty(p, "serif").isResolved());
        assertTrue(new ResolvedProperty(p, "cursive, serif").isResolved());
        
        //resolves since the "cursive serif" can be considered as unquoted custom family name
        assertTrue(new ResolvedProperty(p, "cursive serif").isResolved());

    }

    public void testFontFamilyWithQuotedValue() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font-family");
        ResolvedProperty csspv = assertResolve(p, "'Times New Roman',serif");
//        dumpResult(csspv);
        assertTrue(csspv.isResolved());
    }

    public void testFontSize() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font-size");
        String text = "xx-small";

        ResolvedProperty csspv = assertResolve(p, text);

        assertTrue(csspv.isResolved());
    }

    public void testBorder() {
        PropertyDefinition p = Properties.getPropertyDefinition( "border");
        String text = "20px double";
        ResolvedProperty csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());
    }

    public void testMarginWidth() {
        PropertyDefinition p = Properties.getPropertyDefinition( "margin");
        String text = "20px 10em 30px 30em";
        ResolvedProperty csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());
    }

    public void testPaddingWidth() {
        PropertyDefinition p = Properties.getPropertyDefinition( "padding");
        String text = "20px 10em 30px 30em";
        ResolvedProperty csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());
    }

    public void testTimeUnit() {
        PropertyDefinition p = Properties.getPropertyDefinition( "pause-after");
        assertNotNull(p);
        String text = "200ms";
        ResolvedProperty csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());

        text = "200";
        assertResolve(p, text, false);

        text = "AAms";
        assertResolve(p, text, false);
        
    }

    public void testFrequencyUnit() {
        PropertyDefinition p = Properties.getPropertyDefinition( "pitch");
        String text = "200kHz";
        ResolvedProperty csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());

        text = "200";
        assertResolve(p, text, false);

        text = "AAHz";
        assertResolve(p, text, false);

    }

    public void testIdentifierUnit() {
        PropertyDefinition p = Properties.getPropertyDefinition( "counter-increment");
        String text = "ovecka";
        ResolvedProperty csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());

        text = "10ovecek";
        assertResolve(p, text, false);
        

        text = "-beranek";
        assertResolve(p, text);

    }

    public void testBackgroundImageURL() {
        PropertyDefinition p = Properties.getPropertyDefinition( "background-image");
        String text = "url('/images/v6/tabs-bg.png')";
        ResolvedProperty csspv = assertResolve(p, text);

//        dumpResult(csspv);

        assertTrue(csspv.isResolved());

        text = "url'/images/v6/tabs-bg.png')";
        assertResolve(p, text, false);
        

        text = "ury('/images/v6/tabs-bg.png')";
        assertResolve(p, text, false);
        
    }

    public void testAbsoluteLengthUnits() {
        PropertyDefinition p = Properties.getPropertyDefinition( "font");
        String text = "12px/14cm sans-serif";
        ResolvedProperty csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());
    }

    public void testUnquotedURL() {
        PropertyDefinition p = Properties.getPropertyDefinition( "@uri");
        String text = "url(http://www.redballs.com/redball.png)";
        ResolvedProperty csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());
    }

    public void testBackroundImage() {
        PropertyDefinition p = Properties.getPropertyDefinition( "background-image");
        assertNotResolve(p.getGrammar(), "");
    }

    public void testBackroundPositionOrder() {
        // TODO: fix #142254 and enable this test again
        PropertyDefinition p = Properties.getPropertyDefinition( "@bg-pos");
        assertResolve(p.getGrammar(), "center top");
    }

    public void testBorderColor() {
        PropertyDefinition p = Properties.getPropertyDefinition( "border-color");
        assertResolve(p.getGrammar(), "red yellow black yellow");
        assertResolve(p.getGrammar(), "red yellow black");
        assertResolve(p.getGrammar(), "red yellow");
        assertResolve(p.getGrammar(), "red");

        assertNotResolve(p.getGrammar(), "xxx");
        assertNotResolve(p.getGrammar(), "");
    }

    public void testIssue185995() {
        PropertyDefinition p = Properties.getPropertyDefinition( "border-color");
        assertResolve(p.getGrammar(), "transparent transparent");
    }

    public void testBorder_Top_Style() {
        PropertyDefinition p = Properties.getPropertyDefinition( "border-top-style");
        assertNotNull(p);
        assertResolve(p, "dotted dotted dashed dashed", false);
        assertResolve(p, "dotted");
        
    }

    public void testCaseSensitivity() {
        PropertyDefinition p = Properties.getPropertyDefinition( "azimuth");
        String text = "behind";
        ResolvedProperty csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());

        text = "BEHIND";
        csspv = assertResolve(p, text);
        assertTrue(csspv.isResolved());

    }

    public void testJindrasCase() {
        String g = "[ [ x || y ] || b";

        assertResolve(g, "x");
        assertResolve(g, "x y");
        assertResolve(g, "y");
        assertResolve(g, "y x");
        assertResolve(g, "y x b");
        assertResolve(g, "x b");
        assertResolve(g, "y b");
        assertResolve(g, "y x b");

        assertNotResolve(g, "x b y");
    }
    
    public void testContentFailure() {
        String g = "[ uri , ]* normal";
        
        assertResolve(g, "normal");
        assertResolve(g, "uri, normal");
        assertResolve(g, "uri, uri, normal");
    }
    
    public void testCase4() {
         String grammar2 = "[ a b ] | [ a c ]";
         assertNotResolve(grammar2, "a");
    }
    
    public void testMultiplicityOfList() {
        String g = " [ a || b ]*";
        
        assertResolve(g, "a");
        assertResolve(g, "a b");
        assertResolve(g, "a b a");
    }
    
    public void testAllGroup() {
        String g = "a && b";
        
        assertResolve(g, "a b");
        assertResolve(g, "b a");
        
        assertNotResolve(g, "");
        assertNotResolve(g, "a");
        assertNotResolve(g, "b");
        assertNotResolve(g, "b b");
        assertNotResolve(g, "a a");
        assertNotResolve(g, "x");
        assertNotResolve(g, "a x");
        assertNotResolve(g, "x x b");
    }
    
    public void testAllGroupComplex() {
        String g = "a [ a && b ] b";        
        assertResolve(g, "a a b b");
        assertResolve(g, "a b a b");        
    }

    public void testAllGroupComplex2() {
        String g = "a [ a && b ]? b";        
        assertResolve(g, "a a b b");
        assertResolve(g, "a b a b");        
        assertResolve(g, "a b");        
        
        assertNotResolve(g, "a a b");        
        assertNotResolve(g, "a b b");        
    }
    
    public void testAllGroupComplex3() {
        String g = "[ a && b ]*";
        assertResolve(g, "");
        assertResolve(g, "a b a b a b");
        assertResolve(g, "b a a b");        
        assertResolve(g, "b a");        
        
        assertNotResolve(g, "a b b");        
        assertNotResolve(g, "b a a b a");        
    }
 
    public void testAllGroupComplex4() {
        String g = "a && c?";
        assertResolve(g, "a");
        assertResolve(g, "a c");
        assertResolve(g, "c a");

        String g2 = "d? && e && f";
        assertResolve(g2, "e f");
        assertResolve(g2, "d e f");
        assertResolve(g2, "e f d");
        assertResolve(g2, "f e d");
    }
    
    public void testBackground() {
        PropertyDefinition pm = Properties.getPropertyDefinition( "background");
        assertResolve(pm.getGrammarElement(null), "url(images/shadow.gif) no-repeat bottom right");
    }
    
    public void testBorder2() {
        assertResolve(Properties.getPropertyDefinition( "border"), "red solid");
    }
    
    //Bug 206035 - Incorrect background property value validation/completion
    public void testBackground2() {
        PropertyDefinition pm = Properties.getPropertyDefinition( "background");
        assertResolve(pm.getGrammarElement(null), "#fff url(\"../images/google\") no-repeat center left");
    }

    
//    //should be already fixed in easel (the grammar resolver uses antlr tokens)
//    public void testURI() {
//        PRINT_INFO_IN_ASSERT_RESOLVE = true;
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
//        
//        PropertyDefinition pm = CssModuleSupport.getPropertyDefinition("@uri");
//        assertResolve(pm.getGrammarElement(), "url(images/google)");
//        assertResolve(pm.getGrammarElement(), "url(../images/google)");        
//    }
//    
    
    public void testBgPosition() {
        PropertyDefinition pm = Properties.getPropertyDefinition( "@bg-pos");
        assertResolve(pm.getGrammarElement(null), "center left");
    }
    
    public void testBgPositionDetail_And() {
        //the minimized grammar to reproduce the bg-position resolving problem
        String grammar = "[ center | a ] && [ center | b ]";
        
        assertResolve(grammar, "center b");
        assertResolve(grammar, "b center");
        assertResolve(grammar, "a b");
        assertResolve(grammar, "b a");
        assertResolve(grammar, "center center");        
        assertResolve(grammar, "a center");
        
        assertResolve(grammar, "center a"); //this used to fail
        
    }
    
    public void testBgPositionDetail_Collection() {
        //the minimized grammar to reproduce the bg-position resolving problem
        String grammar = "[ center | a ] || [ center | b ]";
        
        assertResolve(grammar, "center");
        assertResolve(grammar, "b");
        assertResolve(grammar, "a");
        assertResolve(grammar, "b");
        
        assertResolve(grammar, "center b");
        assertResolve(grammar, "b center");
        assertResolve(grammar, "a b");
        assertResolve(grammar, "b a");
        assertResolve(grammar, "center center");        
        assertResolve(grammar, "a center");
        
        assertResolve(grammar, "center a"); //this used to fail
        
    }
    

    public void testGlobalValuesAreResolved() {
        String grammar = "a";

        // Baseline matching - validate, that grammar works
        assertResolve(grammar, "a");

        // Verify that the global values unset, inherit and initial are matched
        assertResolve(grammar, "unset");
        assertResolve(grammar, "inherit");
        assertResolve(grammar, "initial");
    }
}
