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
package org.netbeans.spi.lexer.antlr4;

import org.antlr.grammars.dummy.DummyLexer;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.netbeans.spi.lexer.antlr4.AntlrTokenSequence.DEFAULT_CHANNEL;

/**
 *
 * @author Laszlo Kishalmi
 */
public class AntlrTokenSequenceTest {

    public AntlrTokenSequenceTest() {
    }

    /**
     * Test of seekTo method, of class AntlrTokenSequence.
     */
    @Test
    public void testSeekTo1() {
        System.out.println("seekTo");
        int offset = 0;
        AntlrTokenSequence instance = sequence("");
        instance.seekTo(offset);
        assertTrue(instance.isEmpty());
    }

    @Test
    public void testSeekTo2() {
        System.out.println("seekTo");
        int offset = 0;
        AntlrTokenSequence instance = sequence("# Properties");
        instance.seekTo(offset);
        assertFalse(instance.isEmpty());
        assertTrue(instance.next().isPresent());
    }

    @Test
    public void testSeekTo3() {
        System.out.println("seekTo");
        int offset = 4;
        AntlrTokenSequence instance = sequence("/**/");
        instance.seekTo(offset);
        assertFalse(instance.isEmpty());
        assertFalse(instance.next().isPresent());
        assertTrue(instance.hasPrevious());
    }

    @Test
    public void testSeekTo4() {
        System.out.println("seekTo");
        int offset = 5;
        AntlrTokenSequence instance = sequence("/* */lexer");
        instance.seekTo(offset);
        assertFalse(instance.isEmpty());
        assertTrue(instance.next().isPresent());
        assertTrue(instance.hasPrevious());
    }

    @Test
    public void testSeekTo5() {
        System.out.println("seekTo");
        AntlrTokenSequence instance = sequence("/* */lexer");
        instance.seekTo(10);
        assertFalse(instance.next().isPresent());
        instance.seekTo(5);
        assertFalse(instance.isEmpty());
        assertTrue(instance.next().isPresent());
        assertTrue(instance.previous().isPresent());
        assertFalse(instance.hasPrevious());
    }

    /**
     * Test of isEmpty method, of class AntlrTokenSequence.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        AntlrTokenSequence instance = sequence("");
        assertTrue(instance.isEmpty());
    }

    @Test
    public void testHasNext1() {
        System.out.println("hasNext");
        AntlrTokenSequence instance = sequence("lexer");
        assertTrue(instance.hasNext());
    }

    @Test
    public void testHasNext2() {
        System.out.println("hasNext");
        AntlrTokenSequence instance = sequence("");
        assertFalse(instance.hasNext());
    }

    @Test
    public void testHasNext3() {
        System.out.println("hasNext");
        AntlrTokenSequence instance = sequence("lexer");
        assertTrue(instance.hasNext());
        instance.next();
        assertFalse(instance.hasNext());
    }

    @Test
    public void testHasPrevious1() {
        AntlrTokenSequence instance = sequence("lexer");
        instance.next();
        assertTrue(instance.hasPrevious());
    }

    @Test
    public void testHasPrevious2() {
        AntlrTokenSequence instance = sequence("");
        assertFalse(instance.hasPrevious());
    }

    @Test
    public void testGetOffset1() {
        AntlrTokenSequence instance = sequence("/* */lexer");
        instance.seekTo("/* */le".length());
        assertTrue(instance.hasNext());
        assertEquals("/* */".length(), instance.getOffset());
        instance.previous();
        assertEquals(0, instance.getOffset());
    }

    @Test
    public void testNextPredicate1() {
        AntlrTokenSequence instance = sequence("/* */lexer grammar");
        assertTrue(instance.hasNext());
        instance.next(DEFAULT_CHANNEL).ifPresent((t) -> assertEquals("lexer", t.getText()));
        instance.next(DEFAULT_CHANNEL).ifPresent((t) -> assertEquals("grammar", t.getText()));
        assertFalse(instance.hasNext());
    }

    @Test
    public void testPreviousPredicate1() {
        AntlrTokenSequence instance = sequence("/* */lexer grammar");
        instance.seekTo(18);
        assertFalse(instance.hasNext());
        instance.previous(DEFAULT_CHANNEL).ifPresent((t) -> assertEquals("grammar", t.getText()));
        instance.previous(DEFAULT_CHANNEL).ifPresent((t) -> assertEquals("lexer", t.getText()));
        assertTrue(instance.hasPrevious());
    }

    private AntlrTokenSequence sequence(String s) {
        return new AntlrTokenSequence(new DummyLexer(CharStreams.fromString(s)));
    }
}
