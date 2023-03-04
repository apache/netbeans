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
package org.netbeans.modules.web.el;

import junit.framework.TestCase;

/**
 *
 * @author marekfukala
 */
public class ELPreprocessorTest extends TestCase {
    
    public ELPreprocessorTest(String name) {
        super(name);
    }

    public void testUnusualInputs() {
        String source = "";
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(preprocessed, source);
    }
    
    public void testConversionTablesBasic1() {
        //single rule in conversion table
        String[][] table = new String[][]{{"XX","A"}};
        String source = "XXBC";
        //               012345
        String result = "ABC";
        
        ELPreprocessor elp = new ELPreprocessor(source, table);
        
        assertEquals(result, elp.getPreprocessedExpression());
        
        assertEquals(1, elp.getPreprocessedOffset(2));
        assertEquals(2, elp.getOriginalOffset(1));
        
        assertEquals(2, elp.getPreprocessedOffset(3));
        assertEquals(3, elp.getOriginalOffset(2));
    }

    public void testConversionTablesBasic2() {
        //one conversion table with two rules
        String[][] table = new String[][]{{"XX","A"}, {"YY","B"}};
        String source = "XXYYC";
        //               012345
        String result = "ABC";
        
        ELPreprocessor elp = new ELPreprocessor(source, table);
        
        
        assertEquals(result, elp.getPreprocessedExpression());
        
        assertEquals(1, elp.getPreprocessedOffset(2));
        assertEquals(2, elp.getOriginalOffset(1));
        
        assertEquals(2, elp.getPreprocessedOffset(4));
        assertEquals(4, elp.getOriginalOffset(2));
    }
    
    public void testConversionTablesBasic3() {
        //two conversion tables with one rule
        String[][] table1 = new String[][]{{"XX","A"}};
        String[][] table2 = new String[][]{{"YY","B"}};
        String source = "XXYYC";
        //               012345
        String result = "ABC";
        
        ELPreprocessor elp = new ELPreprocessor(source, table1, table2);
        
        
        
        assertEquals(result, elp.getPreprocessedExpression());
        
        assertEquals(1, elp.getPreprocessedOffset(2));
        assertEquals(2, elp.getOriginalOffset(1));
        
        assertEquals(2, elp.getPreprocessedOffset(4));
        assertEquals(4, elp.getOriginalOffset(2));
    }
    
    public void testNothingToPreprocess() {
        String source = "#{myBean.property}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(source, preprocessed);
    }
    
    public void testAmp() {
        String source = "#{myBean.property &amp; myBean.secondproperty}";
        String result = "#{myBean.property & myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }
    
    public void testAmpAmp() {
        String source = "#{myBean.property &amp;&amp; myBean.secondproperty}";
        String result = "#{myBean.property && myBean.secondproperty}";

        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);

        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }

    public void testAmpAmpAfterString1() {
        String source = "#{x=='A' &amp;&amp; y==null}";
        String result = "#{x=='A' && y==null}";

        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);

        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }

    public void testAmpAmpAfterString2() {
        String source = "#{x=='A'&amp;&amp; y==null}";
        String result = "#{x=='A'&& y==null}";

        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }

    public void testOffsetsConversionAmp() {
        String source = "#{myBean.property &amp; myBean.secondproperty}";
        //               01234567890123456789012345678901234567890123456789
        //               0         1         2         3         4
        String result = "#{myBean.property & myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        //before the pattern
        assertEquals(0, elp.getPreprocessedOffset(0));
        assertEquals(1, elp.getPreprocessedOffset(1));
        assertEquals(18, elp.getPreprocessedOffset(18));
        
        //inside the pattern
        assertEquals(18, elp.getPreprocessedOffset(19));
        assertEquals(18, elp.getPreprocessedOffset(20));
        assertEquals(18, elp.getPreprocessedOffset(21));
        assertEquals(18, elp.getPreprocessedOffset(22));
        
        //after the pattern
        assertEquals(19, elp.getPreprocessedOffset(23));
        assertEquals(20, elp.getPreprocessedOffset(24));
        assertEquals(26, elp.getPreprocessedOffset(30));
        assertEquals(41, elp.getPreprocessedOffset(45));
        
        
    }
    
    public void testOffsetsConversionAmpAmp() {
        String source = "#{myBean.property &amp;&amp; myBean.secondproperty}";
        //               0123456789012345678901234567890123456789012345678901
        //               0         1         2         3         4         5
        String result = "#{myBean.property && myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        //before the pattern
        assertEquals(0, elp.getPreprocessedOffset(0));
        assertEquals(1, elp.getPreprocessedOffset(1));
        assertEquals(18, elp.getPreprocessedOffset(18));
        
        //inside the pattern
        assertEquals(18, elp.getPreprocessedOffset(19));
        assertEquals(18, elp.getPreprocessedOffset(20));
        assertEquals(18, elp.getPreprocessedOffset(21));
        assertEquals(18, elp.getPreprocessedOffset(22));
        
        //between the patterns
        assertEquals(19, elp.getPreprocessedOffset(23));
        
        //inside the second pattern
        assertEquals(19, elp.getPreprocessedOffset(24));
        assertEquals(19, elp.getPreprocessedOffset(25));
        assertEquals(19, elp.getPreprocessedOffset(26));
        assertEquals(19, elp.getPreprocessedOffset(27));
        
        //after the second pattern
        assertEquals(20, elp.getPreprocessedOffset(28));
        assertEquals(21, elp.getPreprocessedOffset(29));
        assertEquals(22, elp.getPreprocessedOffset(30));
        assertEquals(37, elp.getPreprocessedOffset(45));
        assertEquals(43, elp.getPreprocessedOffset(51));
        
    }
    
     public void testBackwardOffsetsConversionAmp() {
        String source = "#{myBean.property &amp; myBean.secondproperty}";
        //               01234567890123456789012345678901234567890123456789
        //               0         1         2         3         4
        String result = "#{myBean.property & myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        //before the pattern
        assertEquals(0, elp.getOriginalOffset(0));
        assertEquals(1, elp.getOriginalOffset(1));
        assertEquals(18, elp.getOriginalOffset(18));
        
        //after the pattern
        assertEquals(23, elp.getOriginalOffset(19));
        assertEquals(24, elp.getOriginalOffset(20));
        assertEquals(30, elp.getOriginalOffset(26));
        assertEquals(45, elp.getOriginalOffset(41));
        
        
    }
     
    public void testBackwardOffsetsConversionAmpAmp() {
        String source = "#{myBean.property &amp;&amp; myBean.secondproperty}";
        //               0123456789012345678901234567890123456789012345678901
        //               0         1         2         3         4         5
        String result = "#{myBean.property && myBean.secondproperty}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        
        
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        //before the pattern
        assertEquals(0, elp.getOriginalOffset(0));
        assertEquals(1, elp.getOriginalOffset(1));
        assertEquals(18, elp.getOriginalOffset(18));
        
        //between the patterns
        assertEquals(23, elp.getOriginalOffset(19));
        
        //after the second pattern
        assertEquals(28, elp.getOriginalOffset(20));
        assertEquals(29, elp.getOriginalOffset(21));
        assertEquals(51, elp.getOriginalOffset(43));
        
        
    }
    
    public void testEscapeSequenceConversion() {
        String source = "\\\\";
        String result = "\\";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.ESCAPED_CHARACTERS);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        source = "\\\"";
        result = "\"";
        
        elp = new ELPreprocessor(source, ELPreprocessor.ESCAPED_CHARACTERS);
        
        preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
    }
    
    public void testEscapedAttributeValues1() {
        String source = "${fn:replace(\\\"hello\\\",\\\"\\\\\\\"\\\",\\\"&quot\\\")}";
        String result = "${fn:replace(\"hello\",\"\\\"\",\"&quot\")}";
        
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.ESCAPED_CHARACTERS);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }
    
    public void testEscapedAttributeValues2() {
        String source = "${fn:replace(\"hello\",\"\\\"\",\"&quot\")}";
        String result = "${fn:replace(\"hello\",\"\\\"\",\"&quot\")}";
                
        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.ESCAPED_CHARACTERS);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        
        assertEquals(result, preprocessed);
    }
    
    
    public void testMoreConversionTables() {
        String source = "#{myBean.property &amp; \\\"}";
        String result = "#{myBean.property & \"}";
        
        ELPreprocessor elp = new ELPreprocessor(source, 
                ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE,
                ELPreprocessor.ESCAPED_CHARACTERS);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }
    
    public void testMoreConversionTablesOffsetsConversion() {
        String source = "#{myBean.property &amp; \\\\\\\" &amp;A}";
        //               0123456789012345678901234 5 6 7 8901234567890123456789
        //               0         1         2             3         4
        //               012345678901234567890 1 2345
        String result = "#{myBean.property & \\\" &A}";
        
        ELPreprocessor elp = new ELPreprocessor(source, 
                ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE,
                ELPreprocessor.ESCAPED_CHARACTERS);
        
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
        
        //before the pattern
        assertEquals(0, elp.getPreprocessedOffset(0));
        assertEquals(1, elp.getPreprocessedOffset(1));
        assertEquals(18, elp.getPreprocessedOffset(18));
        
        //inside the pattern
        assertEquals(18, elp.getPreprocessedOffset(19));
        assertEquals(18, elp.getPreprocessedOffset(20));
        assertEquals(18, elp.getPreprocessedOffset(21));
        assertEquals(18, elp.getPreprocessedOffset(22));
        
        //at the second pattern
        assertEquals(20, elp.getPreprocessedOffset(24));
        assertEquals(20, elp.getPreprocessedOffset(25));
        
        
        //in the second pattern
        assertEquals(23, elp.getPreprocessedOffset(29));
        
        //after the second pattern
        assertEquals(21, elp.getPreprocessedOffset(26));
        
        //at the A char
        assertEquals(24, elp.getPreprocessedOffset(34));
        
    }

    public void testNotEscapedStringValues1() {
        String source = "#{cc.attrs.clearForm ? '$(\\'#form\\').clearForm();' : ' '}";
        String result = "#{cc.attrs.clearForm ? '$(\\'#form\\').clearForm();' : ' '}";

        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.ESCAPED_CHARACTERS, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }

    public void testNotEscapedStringValues2() {
        String source = "#{myBean.translate('AAA\\\\BBB')}";
        String result = "#{myBean.translate('AAA\\\\BBB')}";

        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.ESCAPED_CHARACTERS, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }

    public void testNotEscapedStringValues3() {
        String source = "#{myBean.property &amp;&amp; myBean.escapedText == \"\\\"text\\\"\"}";
        //               0123456789012345678901234567890123456789012345678901
        //               0         1         2         3         4         5
        String result = "#{myBean.property && myBean.escapedText == \"\\\"text\\\"\"}";

        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.ESCAPED_CHARACTERS, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);

        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);

        //before the pattern
        assertEquals(0, elp.getOriginalOffset(0));
        assertEquals(1, elp.getOriginalOffset(1));
        assertEquals(18, elp.getOriginalOffset(18));

        //between the patterns
        assertEquals(23, elp.getOriginalOffset(19));

        //after the second pattern
        assertEquals(28, elp.getOriginalOffset(20));
        assertEquals(29, elp.getOriginalOffset(21));
        assertEquals(51, elp.getOriginalOffset(43));
    }

    public void testNotEscapedStringValues4() {
        String source = "#{myBean.escapedText == \"\\\"text\\\"\" && myBean.quotes == \\\"}";
        //               0123456789012345678901234567890123456789012345678901
        //               0         1         2         3         4         5
        String result = "#{myBean.escapedText == \"\\\"text\\\"\" && myBean.quotes == \"}";

        ELPreprocessor elp = new ELPreprocessor(source, ELPreprocessor.ESCAPED_CHARACTERS, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);

        String preprocessed = elp.getPreprocessedExpression();
        assertNotNull(preprocessed);
        assertEquals(result, preprocessed);
    }
    
}
