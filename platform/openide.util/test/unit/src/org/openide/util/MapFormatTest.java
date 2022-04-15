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
package org.openide.util;

import java.text.ParsePosition;
import static java.util.Collections.emptyMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.junit.Before;
import org.junit.Test;
import static org.openide.util.MapFormat.format;

/**
 *
 * @author Jaroslav Tulach, Lukasz Bownik
 */
public class MapFormatTest {

    private final Map johnDoe = new HashMap();
    private final Map dateNumberAndClass = new HashMap();

    //--------------------------------------------------------------------------
    @Before
    public void setUp() {

        this.johnDoe.put("first", "John");
        this.johnDoe.put("last", "Doe");
        this.johnDoe.put("prefix", "Mr");

        this.dateNumberAndClass.put("date", new Date(1234, 7, 6, 12, 23, 51));
        this.dateNumberAndClass.put("number", 999);
        this.dateNumberAndClass.put("class", Void.class);
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_throwsNullPointer_whenGivenNullArgument() {

        try {
            format(null, new HashMap());
            fail();
        } catch (final NullPointerException e) {
            //good
        }
        try {
            format("{name}", null);
            fail();
        } catch (final NullPointerException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_returnEmptyString_whenGivenEmptyFormatString() {

        assertEquals("", format("", this.johnDoe));
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_returnsFormatStringUnchanged_whenGivenNoArgumentMap() {

        assertEquals("{name}", format("{name}", emptyMap()));
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_throwsIllegalArgument_whenKeyIsNotFound_andThrowExceptionFlagIsTrue() {

        MapFormat mf = new MapFormat(emptyMap());
        mf.setThrowExceptionIfKeyWasNotFound(true);

        try {
            mf.format("{name}");
        } catch (final IllegalArgumentException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_returnsFormatedString_whenFivenProperParameters() {

        assertEquals("My name is John Doe.", format("My name is {first} {last}.", this.johnDoe));
        assertEquals("Hello Mr Doe.", format("Hello {prefix} {last}.", this.johnDoe));
        assertEquals("Hello {prefix Doe.", format("Hello {prefix {last}.", this.johnDoe));
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_preservesUnknownTagsAndEmptyBraces() {

        assertEquals("My {strange} name {} is John {{{{{}}}}}{Doe.",
                format("My {strange} name {} is {first} {{{{{}}}}}{{last}.", this.johnDoe));
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_throwsIllegalArgument_whenGivenTagsWithMissingRightBraces() {

        try {
            format("Hello {prefix} {last.", this.johnDoe);
            fail();
        } catch (IllegalArgumentException e) {
            //good
        }
        try {
            format("Hello {prefix {last.", this.johnDoe);
            fail();
        } catch (IllegalArgumentException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_preservesTagsWithMissingLefttBraces() {

        assertEquals("Hello prefix} Doe.", format("Hello prefix} {last}.", this.johnDoe));
        assertEquals("Hello Mr last}.", format("Hello {prefix} last}.", this.johnDoe));
        assertEquals("Hello prefix} last}.", format("Hello prefix} last}.", this.johnDoe));
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_propertlyRecognizes_NonCustomBraces() {

        MapFormat mf = new MapFormat(this.johnDoe);
        mf.setLeftBrace("$1$");
        mf.setRightBrace("*");

        assertEquals("Hello Mr Doe.", mf.format("Hello $1$prefix* $1$last*."));

        assertEquals("Hello prefix* Doe.", mf.format("Hello prefix* $1$last*."));

        assertEquals("Hello $1$prefix Doe.", mf.format("Hello $1$prefix $1$last*."));
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_throwsIllegalArgument_whenGivenTagsWithMissingCustomRightBraces() {

        MapFormat mf = new MapFormat(this.johnDoe);
        mf.setLeftBrace("$1$");
        mf.setRightBrace("*");

        try {
            mf.format("Hello $1$prefix* $1$last.");
            fail();
        } catch (IllegalArgumentException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_ignoresEmptySequanceOfMultipleBraces() {

        MapFormat mf = new MapFormat(this.johnDoe);
        mf.setExactMatch(false);
        mf.setLeftBrace("__");
        mf.setRightBrace("__");

        String s = "/*___________________________________________________________________________*/";
        assertEquals(s, mf.format(s));

        assertEquals("/*_____________________*/\n/*John*/",
                mf.format("/*_____________________*/\n/*__first__*/"));

    }

    //--------------------------------------------------------------------------
    @Test
    public void format_acceptsFormatStringWithMissingRightBraces_ifExactMatchIsFalse() {

        MapFormat mf = new MapFormat(this.johnDoe);
        mf.setExactMatch(false);
        assertEquals("Hello {prefix Doe.", mf.format("Hello {prefix {last}."));
        assertEquals("Hello Mr {last.", mf.format("Hello {prefix} {last."));
    }

    //--------------------------------------------------------------------------
    @Test
    public void format_worksWithDatesAndNumbersAsArgumentsAndOther() {

        String result = format("{date} {number}.", this.dateNumberAndClass);
        // Since exact format depends on default locale we need to verify this 
        // the result indirectly.
        System.out.println(result);
        assertTrue(result.contains("34"));
        assertTrue(result.contains("8"));
        assertTrue(result.contains("6"));
        assertTrue(result.contains("12"));
        assertTrue(result.contains("23"));
        assertTrue(result.contains("999"));

        assertEquals("class java.lang.Void", format("{class}", this.dateNumberAndClass));
    }
    //--------------------------------------------------------------------------
    @Test
    public void parseObject_throwsNullPointer_whenGivenNullArgument() {

        MapFormat mf = new MapFormat(this.johnDoe);
        
        try {
            mf.parseObject(null, new ParsePosition(0));
            fail();
        } catch (final NullPointerException e) {
            //good
        }
    }
    //--------------------------------------------------------------------------
    @Test
    public void parseObject_returnsEmptyString_whenGivenEmptyString() {
        
        MapFormat mf = new MapFormat(this.johnDoe);
        
        assertEquals("", mf.parseObject("", new ParsePosition(0)));
    }
    //--------------------------------------------------------------------------
    @Test
    public void parseObject_returnsPatternString_whenProperMessageString() {
        
        MapFormat mf = new MapFormat(this.johnDoe);
        
        assertEquals("Hello {first}.", mf.parseObject("Hello John.", new ParsePosition(0)));
    }
    //--------------------------------------------------------------------------
    @Test
    public void parseObject_returnsItsArgument_whenNoTagsCanBeResolved() {
        
        MapFormat mf = new MapFormat(this.johnDoe);
        
        assertEquals("Hello Adam.", mf.parseObject("Hello Adam.", new ParsePosition(0)));
    }
    //--------------------------------------------------------------------------
    @Test
    public void getMap_returnsMapPassedThroughConstructor() {
        
        MapFormat mf = new MapFormat(this.johnDoe);
        
        assertEquals(this.johnDoe, mf.getMap());
    }
    //--------------------------------------------------------------------------
    @Test
    public void setMap_properlyAssignsNewMapToFormatter() {
        
        MapFormat mf = new MapFormat(this.johnDoe);
        mf.setMap(this.dateNumberAndClass);
        
        assertEquals(this.dateNumberAndClass, mf.getMap());
    }
}
