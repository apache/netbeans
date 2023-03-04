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
package org.netbeans.modules.xml.text.syntax.javacc;
import java.io.*;
import org.netbeans.modules.xml.text.syntax.javacc.lib.*;

public class XMLSyntaxTokenManager implements XMLSyntaxConstants {

    //!!! enter proper bridge
    public final class Bridge extends XMLSyntaxTokenManager implements JJSyntaxInterface, JJConstants {
        public Bridge() {
            super(null);
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~ TEXT BASED SHARING START ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private transient String myimage = "";  //contais image of last scanned [partial] token // NOI18N
    private transient String lastImage = ""; // NOI18N
    private transient int id;
    
    private int lastValidState; //contains last correct state
                                //state may become incorect if EOB is returned
                                //due to buffer end e.g.
                                //(a) <! moves to IN_DECL
                                //(b) <!-- moves to IN_COMMENT
                                //if (a) is followed by EOB that
                                //token manager enters illegal state

    /** Return current state of lexan. 
     * There will be probably necessary simple ID mappe among
     * Syntax's state IDs with reserved INIT(-1) and JavaCC DEFAULT(variable often the highest one).
     */
    public final int getState() {
        return curLexState;
    }

    /** Return length of last recognized token. ?? SKIP*/
    public final int getLength() {
        return myimage.length();
    }

    /** Return last token. */
    public final String getImage() {
        return myimage.toString();
    }

    /** Set state info to folowing one. */
    public final void setStateInfo(int[] state) {
        int[] newstate = new int[state.length];
        System.arraycopy(state, 0, newstate, 0, state.length);
        states = newstate;
        //    dumpStack("L"); // NOI18N
        lastValidState = popState(); //restore lastValidState
    }

    /** return copy of current state. */
    public final int[] getStateInfo() {
        pushState(lastValidState); // store lastValidState  !modifies states stack
        int[] state = new int[states.length];
        System.arraycopy(states, 0, state, 0, states.length);
        //    dumpStack("S"); // NOI18N
        popState();                // !restore the states stack
        return state;
    }


    private void dumpStack(String label) {
        StringBuffer s = new StringBuffer();
        s.append(label + " "); // NOI18N
        for (int i = 0; i<states.length; i++) {
            s.append(states[i] + ", "); // NOI18N
        }
        System.err.println(s.toString());
    }

    /** Set input stream to folowing one
     *  and reset initial state.
     */
    public final void init(CharStream input) {
        ReInit((UCode_CharStream)input);
        lastValidState = getState();
    }

    /** Set input stream to folowing one
     *  and set current state.
     */
    public final void init(CharStream in, int state) {
        ReInit((UCode_CharStream)in, state);
        lastValidState = getState();
    }

    /** Syntax would want restore state on buffer boundaries. */
    public final void setState(int state) {
        lastValidState = state;
        SwitchTo(state == -1 ? defaultLexState : state); //fix deleting at document start
    }

    //
    // push analyzer state to states stack
    //
    private void pushState(int state) {
        if (states == null) {
            states = new int[] {state};
        } else {
            int[] newstates = new int[states.length + 1];
            System.arraycopy(states, 0, newstates, 0, states.length);
            newstates[newstates.length - 1] = state;
            states = newstates;
        }
    }

    //
    // pop analyzer state from states stack
    //
    private int popState() {
        int state = states[states.length - 1];
        if (states.length == 1) {
            states = null;
        } else {
            int[] newstates = new int[states.length - 1];
            System.arraycopy(states, 0, newstates, 0, states.length - 1);
            states = newstates;
        }
        return state;
    }

    /** Prepare next token from stream. */
    public final void next() {
        try {
            Token tok = getNextToken();
            myimage = tok.image;
            id = tok.kind;
            if (id == EOF) { //??? EOF is visible just at Parser LEVEL
                setState(lastValidState);
                id = Bridge.JJ_EOF;
            }
            lastValidState = getState();

        } catch (TokenMgrError ex) {
            try {
                //is the exception caused by EOF?
                char ch = input_stream.readChar();
                input_stream.backup(1);

                myimage = input_stream.GetImage();
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    System.err.println(getClass().toString() + " ERROR:" + getState() + ":'" + ch + "'"); // NOI18N
                id = Bridge.JJ_ERR;

            } catch (IOException eof) {

                myimage = input_stream.GetImage();
                id = Bridge.JJ_EOF;
            }
        }
    }

    /** Return ID of the last recognized token. */
    public int getID() {
        return id;
    }

    /** Return name of the last token. */
    public final String getName() {
        return tokenImage[id];
    }

    /** For testing purposes only. */
    public static void main(String args[]) throws Exception {

        InputStream in;
        int dump = 0;
        int dump2 = 1000;

        System.err.println("Got " + args.length + " arguments."); // NOI18N

        if (args.length != 0) {
            in = new FileInputStream(args[0]);
            if (args.length == 2) { //dump just requested line
                dump = Integer.parseInt(args[1]) - 1;
                dump2 = dump;
                System.err.println("Line to be dumped:" + dump); // NOI18N
            }
        } else  {
            System.err.println("One argument required."); // NOI18N
            return;
        }

        CharStream input = new ASCIICharStream(in, 0, 0);
        Bridge lex = null; //new XMLSyntaxTokenManager(input);

        int i = 25; //token count
        int id;
        int toks = 0;
        long time = System.currentTimeMillis();

        while (i/*--*/>0) {

            lex.next();
            id = lex.getID();

            toks++;
            switch (id) {
            case Bridge.JJ_EOF:
                System.err.println("EOF at " + lex.getState() + " " + lex.getImage()); // NOI18N
                System.err.println("Line: " + input.getLine() ); // NOI18N
                System.err.println("Tokens: " + toks ); // NOI18N
                System.err.println("Time: " + (System.currentTimeMillis() - time) ); // NOI18N
                return;

            default:
                if (dump <= input.getLine() && input.getLine() <= dump2)
                    System.err.println(" " + id + "@" + lex.getState() + ":" + lex.getImage() ); // NOI18N
            }

        }

    }

    /**
     * The analyzer may store information about state in this
     * stack (array). These will be used as Syntax state info.
     */
    private int[] states = null;
    private final int jjStopStringLiteralDfa_9(int pos, long active0, long active1)
    {
        switch (pos)
            {
            case 0:
                if ((active0 & 0x6000000000000000L) != 0L)
                    {
                        jjmatchedKind = 64;
                        return 1;
                    }
                return -1;
            case 1:
                if ((active0 & 0x6000000000000000L) != 0L)
                    {
                        jjmatchedKind = 64;
                        jjmatchedPos = 1;
                        return 1;
                    }
                return -1;
            case 2:
                if ((active0 & 0x6000000000000000L) != 0L)
                    {
                        jjmatchedKind = 64;
                        jjmatchedPos = 2;
                        return 1;
                    }
                return -1;
            case 3:
                if ((active0 & 0x6000000000000000L) != 0L)
                    {
                        jjmatchedKind = 64;
                        jjmatchedPos = 3;
                        return 1;
                    }
                return -1;
            case 4:
                if ((active0 & 0x6000000000000000L) != 0L)
                    {
                        jjmatchedKind = 64;
                        jjmatchedPos = 4;
                        return 1;
                    }
                return -1;
            case 5:
                if ((active0 & 0x4000000000000000L) != 0L)
                    return 1;
                if ((active0 & 0x2000000000000000L) != 0L)
                    {
                        jjmatchedKind = 64;
                        jjmatchedPos = 5;
                        return 1;
                    }
                return -1;
            default :
                return -1;
            }
    }
    private final int jjStartNfa_9(int pos, long active0, long active1)
    {
        return jjMoveNfa_9(jjStopStringLiteralDfa_9(pos, active0, active1), pos + 1);
    }
    private final int jjStopAtPos(int pos, int kind)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        return pos + 1;
    }
    private final int jjStartNfaWithStates_9(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_9(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_9()
    {
        switch(curChar)
            {
            case 37:
                return jjStopAtPos(0, 86);
            case 73:
                return jjMoveStringLiteralDfa1_9(0x6000000000000000L);
            case 91:
                return jjStopAtPos(0, 65);
            default :
                return jjMoveNfa_9(3, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_9(long active0)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_9(0, active0, 0L);
            return 1;
        }
        switch(curChar)
            {
            case 71:
                return jjMoveStringLiteralDfa2_9(active0, 0x4000000000000000L);
            case 78:
                return jjMoveStringLiteralDfa2_9(active0, 0x2000000000000000L);
            default :
                break;
            }
        return jjStartNfa_9(0, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa2_9(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_9(0, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_9(1, active0, 0L);
            return 2;
        }
        switch(curChar)
            {
            case 67:
                return jjMoveStringLiteralDfa3_9(active0, 0x2000000000000000L);
            case 78:
                return jjMoveStringLiteralDfa3_9(active0, 0x4000000000000000L);
            default :
                break;
            }
        return jjStartNfa_9(1, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa3_9(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_9(1, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_9(2, active0, 0L);
            return 3;
        }
        switch(curChar)
            {
            case 76:
                return jjMoveStringLiteralDfa4_9(active0, 0x2000000000000000L);
            case 79:
                return jjMoveStringLiteralDfa4_9(active0, 0x4000000000000000L);
            default :
                break;
            }
        return jjStartNfa_9(2, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa4_9(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_9(2, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_9(3, active0, 0L);
            return 4;
        }
        switch(curChar)
            {
            case 82:
                return jjMoveStringLiteralDfa5_9(active0, 0x4000000000000000L);
            case 85:
                return jjMoveStringLiteralDfa5_9(active0, 0x2000000000000000L);
            default :
                break;
            }
        return jjStartNfa_9(3, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa5_9(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_9(3, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_9(4, active0, 0L);
            return 5;
        }
        switch(curChar)
            {
            case 68:
                return jjMoveStringLiteralDfa6_9(active0, 0x2000000000000000L);
            case 69:
                if ((active0 & 0x4000000000000000L) != 0L)
                    return jjStartNfaWithStates_9(5, 62, 1);
                break;
            default :
                break;
            }
        return jjStartNfa_9(4, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa6_9(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_9(4, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_9(5, active0, 0L);
            return 6;
        }
        switch(curChar)
            {
            case 69:
                if ((active0 & 0x2000000000000000L) != 0L)
                    return jjStartNfaWithStates_9(6, 61, 1);
                break;
            default :
                break;
            }
        return jjStartNfa_9(5, active0, 0L);
    }
    private final void jjCheckNAdd(int state)
    {
        if (jjrounds[state] != jjround)
            {
                jjstateSet[jjnewStateCnt++] = state;
                jjrounds[state] = jjround;
            }
    }
    private final void jjAddStates(int start, int end)
    {
        do {
            jjstateSet[jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }
    private final void jjCheckNAddTwoStates(int state1, int state2)
    {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }
    private final void jjCheckNAddStates(int start, int end)
    {
        do {
            jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }
    private final void jjCheckNAddStates(int start)
    {
        jjCheckNAdd(jjnextStates[start]);
        jjCheckNAdd(jjnextStates[start + 1]);
    }
    static final long[] jjbitVec0 = {
        0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
    };
    static final long[] jjbitVec2 = {
        0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
    };
    private final int jjMoveNfa_9(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 3;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 64)
                                                kind = 64;
                                            jjCheckNAdd(1);
                                        }
                                    else if ((0x580080c600000000L & l) != 0L)
                                        {
                                            if (kind > 64)
                                                kind = 64;
                                            jjCheckNAdd(2);
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 63)
                                                kind = 63;
                                            jjCheckNAdd(0);
                                        }
                                    break;
                                case 0:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 63)
                                        kind = 63;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 64)
                                        kind = 64;
                                    jjCheckNAdd(1);
                                    break;
                                case 2:
                                    if ((0x580080c600000000L & l) == 0L)
                                        break;
                                    kind = 64;
                                    jjCheckNAdd(2);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0xffffffffd7ffffffL & l) != 0L)
                                        {
                                            if (kind > 64)
                                                kind = 64;
                                            jjCheckNAdd(1);
                                        }
                                    else if (curChar == 93)
                                        {
                                            if (kind > 64)
                                                kind = 64;
                                            jjCheckNAdd(2);
                                        }
                                    break;
                                case 1:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 64;
                                    jjCheckNAdd(1);
                                    break;
                                case 2:
                                    if (curChar != 93)
                                        break;
                                    kind = 64;
                                    jjCheckNAdd(2);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                case 1:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 64)
                                        kind = 64;
                                    jjCheckNAdd(1);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjMoveStringLiteralDfa0_19()
    {
        return jjMoveNfa_19(2, 0);
    }
    private final int jjMoveNfa_19(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 3;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 15)
                                                kind = 15;
                                            jjCheckNAdd(0);
                                        }
                                    else if ((0x780000e700000000L & l) != 0L)
                                        {
                                            if (kind > 16)
                                                kind = 16;
                                            jjCheckNAdd(1);
                                        }
                                    else if (curChar == 47)
                                        {
                                            if (kind > 16)
                                                kind = 16;
                                            jjCheckNAdd(0);
                                        }
                                    break;
                                case 0:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    kind = 15;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x780000e700000000L & l) == 0L)
                                        break;
                                    kind = 16;
                                    jjCheckNAdd(1);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                    if ((0xffffffffd7ffffffL & l) != 0L)
                                        {
                                            if (kind > 15)
                                                kind = 15;
                                            jjCheckNAdd(0);
                                        }
                                    else if ((0x28000000L & l) != 0L)
                                        {
                                            if (kind > 16)
                                                kind = 16;
                                            jjCheckNAdd(1);
                                        }
                                    break;
                                case 0:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 15;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x28000000L & l) == 0L)
                                        break;
                                    kind = 16;
                                    jjCheckNAdd(1);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 15)
                                        kind = 15;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_2(int pos, long active0, long active1)
    {
        switch (pos)
            {
            default :
                return -1;
            }
    }
    private final int jjStartNfa_2(int pos, long active0, long active1)
    {
        return jjMoveNfa_2(jjStopStringLiteralDfa_2(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_2(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_2(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_2()
    {
        switch(curChar)
            {
            case 39:
                return jjStopAtPos(0, 95);
            default :
                return jjMoveNfa_2(0, 0);
            }
    }
    private final int jjMoveNfa_2(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 1;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    if ((0xffffff7fffffffffL & l) == 0L)
                                        break;
                                    kind = 94;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    kind = 94;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 94)
                                        kind = 94;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_12(int pos, long active0, long active1)
    {
        switch (pos)
            {
            default :
                return -1;
            }
    }
    private final int jjStartNfa_12(int pos, long active0, long active1)
    {
        return jjMoveNfa_12(jjStopStringLiteralDfa_12(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_12(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_12(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_12()
    {
        switch(curChar)
            {
            case 34:
                return jjStopAtPos(0, 99);
            case 37:
                return jjStopAtPos(0, 86);
            case 39:
                return jjStopAtPos(0, 93);
            case 62:
                return jjStopAtPos(0, 51);
            default :
                return jjMoveNfa_12(5, 0);
            }
    }
    private final int jjMoveNfa_12(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 19;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 5:
                                    if ((0x3fffff52fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 50)
                                                kind = 50;
                                            jjCheckNAdd(18);
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 50)
                                                kind = 50;
                                            jjCheckNAdd(17);
                                        }
                                    break;
                                case 17:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 50)
                                        kind = 50;
                                    jjCheckNAdd(17);
                                    break;
                                case 18:
                                    if ((0x3fffff52fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 50)
                                        kind = 50;
                                    jjCheckNAdd(18);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 5:
                                    if (kind > 50)
                                        kind = 50;
                                    jjCheckNAdd(18);
                                    if (curChar == 80)
                                        jjstateSet[jjnewStateCnt++] = 15;
                                    else if (curChar == 78)
                                        jjstateSet[jjnewStateCnt++] = 9;
                                    else if (curChar == 83)
                                        jjstateSet[jjnewStateCnt++] = 4;
                                    break;
                                case 0:
                                    if (curChar == 77 && kind > 49)
                                        kind = 49;
                                    break;
                                case 1:
                                    if (curChar == 69)
                                        jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                case 2:
                                    if (curChar == 84)
                                        jjstateSet[jjnewStateCnt++] = 1;
                                    break;
                                case 3:
                                    if (curChar == 83)
                                        jjstateSet[jjnewStateCnt++] = 2;
                                    break;
                                case 4:
                                    if (curChar == 89)
                                        jjstateSet[jjnewStateCnt++] = 3;
                                    break;
                                case 6:
                                    if (curChar == 65 && kind > 49)
                                        kind = 49;
                                    break;
                                case 7:
                                    if (curChar == 84)
                                        jjstateSet[jjnewStateCnt++] = 6;
                                    break;
                                case 8:
                                    if (curChar == 65)
                                        jjstateSet[jjnewStateCnt++] = 7;
                                    break;
                                case 9:
                                    if (curChar == 68)
                                        jjstateSet[jjnewStateCnt++] = 8;
                                    break;
                                case 10:
                                    if (curChar == 78)
                                        jjstateSet[jjnewStateCnt++] = 9;
                                    break;
                                case 11:
                                    if (curChar == 67 && kind > 49)
                                        kind = 49;
                                    break;
                                case 12:
                                    if (curChar == 73)
                                        jjstateSet[jjnewStateCnt++] = 11;
                                    break;
                                case 13:
                                    if (curChar == 76)
                                        jjstateSet[jjnewStateCnt++] = 12;
                                    break;
                                case 14:
                                    if (curChar == 66)
                                        jjstateSet[jjnewStateCnt++] = 13;
                                    break;
                                case 15:
                                    if (curChar == 85)
                                        jjstateSet[jjnewStateCnt++] = 14;
                                    break;
                                case 16:
                                    if (curChar == 80)
                                        jjstateSet[jjnewStateCnt++] = 15;
                                    break;
                                case 18:
                                    if (kind > 50)
                                        kind = 50;
                                    jjCheckNAdd(18);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 5:
                                case 18:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 50)
                                        kind = 50;
                                    jjCheckNAdd(18);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 19 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjMoveStringLiteralDfa0_3()
    {
        return jjMoveNfa_3(1, 0);
    }
    private final int jjMoveNfa_3(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 4;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 1:
                                    if ((0x7ff7f18fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 90)
                                                kind = 90;
                                            jjCheckNAdd(0);
                                        }
                                    else if ((0xd00080e600000000L & l) != 0L)
                                        {
                                            if (kind > 91)
                                                kind = 91;
                                        }
                                    else if (curChar == 59)
                                        {
                                            if (kind > 92)
                                                kind = 92;
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 92)
                                                kind = 92;
                                            jjCheckNAdd(3);
                                        }
                                    break;
                                case 0:
                                    if ((0x7ff7f18fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 90)
                                        kind = 90;
                                    jjCheckNAdd(0);
                                    break;
                                case 2:
                                    if (curChar == 59)
                                        kind = 92;
                                    break;
                                case 3:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 92)
                                        kind = 92;
                                    jjCheckNAdd(3);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 1:
                                case 0:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 90;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 1:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 90)
                                        kind = 90;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 4 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_8(int pos, long active0, long active1)
    {
        switch (pos)
            {
            case 0:
                if ((active1 & 0x7fc0L) != 0L)
                    {
                        jjmatchedKind = 79;
                        return 2;
                    }
                return -1;
            case 1:
                if ((active1 & 0x7c80L) != 0L)
                    {
                        if (jjmatchedPos != 1)
                            {
                                jjmatchedKind = 79;
                                jjmatchedPos = 1;
                            }
                        return 2;
                    }
                if ((active1 & 0x340L) != 0L)
                    return 2;
                return -1;
            case 2:
                if ((active1 & 0x7f80L) != 0L)
                    {
                        jjmatchedKind = 79;
                        jjmatchedPos = 2;
                        return 2;
                    }
                return -1;
            case 3:
                if ((active1 & 0x7f80L) != 0L)
                    {
                        jjmatchedKind = 79;
                        jjmatchedPos = 3;
                        return 2;
                    }
                return -1;
            case 4:
                if ((active1 & 0x380L) != 0L)
                    return 2;
                if ((active1 & 0x7c00L) != 0L)
                    {
                        if (jjmatchedPos != 4)
                            {
                                jjmatchedKind = 79;
                                jjmatchedPos = 4;
                            }
                        return 2;
                    }
                return -1;
            case 5:
                if ((active1 & 0x600L) != 0L)
                    return 2;
                if ((active1 & 0x7800L) != 0L)
                    {
                        jjmatchedKind = 79;
                        jjmatchedPos = 5;
                        return 2;
                    }
                return -1;
            case 6:
                if ((active1 & 0x3000L) != 0L)
                    return 2;
                if ((active1 & 0x4800L) != 0L)
                    {
                        if (jjmatchedPos != 6)
                            {
                                jjmatchedKind = 79;
                                jjmatchedPos = 6;
                            }
                        return 2;
                    }
                return -1;
            case 7:
                if ((active1 & 0x6800L) != 0L)
                    return 2;
                return -1;
            default :
                return -1;
            }
    }
    private final int jjStartNfa_8(int pos, long active0, long active1)
    {
        return jjMoveNfa_8(jjStopStringLiteralDfa_8(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_8(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_8(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_8()
    {
        switch(curChar)
            {
            case 34:
                return jjStopAtPos(0, 99);
            case 35:
                return jjMoveStringLiteralDfa1_8(0x38L);
            case 39:
                return jjStopAtPos(0, 93);
            case 62:
                return jjStopAtPos(0, 80);
            case 67:
                return jjMoveStringLiteralDfa1_8(0x80L);
            case 69:
                return jjMoveStringLiteralDfa1_8(0xc00L);
            case 73:
                return jjMoveStringLiteralDfa1_8(0x340L);
            case 78:
                return jjMoveStringLiteralDfa1_8(0x7000L);
            default :
                return jjMoveNfa_8(4, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_8(long active1)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_8(0, 0L, active1);
            return 1;
        }
        switch(curChar)
            {
            case 68:
                if ((active1 & 0x40L) != 0L)
                    {
                        jjmatchedKind = 70;
                        jjmatchedPos = 1;
                    }
                return jjMoveStringLiteralDfa2_8(active1, 0x380L);
            case 70:
                return jjMoveStringLiteralDfa2_8(active1, 0x20L);
            case 73:
                return jjMoveStringLiteralDfa2_8(active1, 0x10L);
            case 77:
                return jjMoveStringLiteralDfa2_8(active1, 0x3000L);
            case 78:
                return jjMoveStringLiteralDfa2_8(active1, 0xc00L);
            case 79:
                return jjMoveStringLiteralDfa2_8(active1, 0x4000L);
            case 82:
                return jjMoveStringLiteralDfa2_8(active1, 0x8L);
            default :
                break;
            }
        return jjStartNfa_8(0, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa2_8(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_8(0, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_8(1, 0L, active1);
            return 2;
        }
        switch(curChar)
            {
            case 65:
                return jjMoveStringLiteralDfa3_8(active1, 0x80L);
            case 69:
                return jjMoveStringLiteralDfa3_8(active1, 0x8L);
            case 73:
                return jjMoveStringLiteralDfa3_8(active1, 0x20L);
            case 77:
                return jjMoveStringLiteralDfa3_8(active1, 0x10L);
            case 82:
                return jjMoveStringLiteralDfa3_8(active1, 0x300L);
            case 84:
                return jjMoveStringLiteralDfa3_8(active1, 0x7c00L);
            default :
                break;
            }
        return jjStartNfa_8(1, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa3_8(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_8(1, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_8(2, 0L, active1);
            return 3;
        }
        switch(curChar)
            {
            case 65:
                return jjMoveStringLiteralDfa4_8(active1, 0x4000L);
            case 69:
                return jjMoveStringLiteralDfa4_8(active1, 0x300L);
            case 73:
                return jjMoveStringLiteralDfa4_8(active1, 0xc00L);
            case 79:
                return jjMoveStringLiteralDfa4_8(active1, 0x3000L);
            case 80:
                return jjMoveStringLiteralDfa4_8(active1, 0x10L);
            case 81:
                return jjMoveStringLiteralDfa4_8(active1, 0x8L);
            case 84:
                return jjMoveStringLiteralDfa4_8(active1, 0x80L);
            case 88:
                return jjMoveStringLiteralDfa4_8(active1, 0x20L);
            default :
                break;
            }
        return jjStartNfa_8(2, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa4_8(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_8(2, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_8(3, 0L, active1);
            return 4;
        }
        switch(curChar)
            {
            case 65:
                if ((active1 & 0x80L) != 0L)
                    return jjStartNfaWithStates_8(4, 71, 2);
                break;
            case 69:
                return jjMoveStringLiteralDfa5_8(active1, 0x20L);
            case 70:
                if ((active1 & 0x100L) != 0L)
                    {
                        jjmatchedKind = 72;
                        jjmatchedPos = 4;
                    }
                return jjMoveStringLiteralDfa5_8(active1, 0x200L);
            case 75:
                return jjMoveStringLiteralDfa5_8(active1, 0x3000L);
            case 76:
                return jjMoveStringLiteralDfa5_8(active1, 0x10L);
            case 84:
                return jjMoveStringLiteralDfa5_8(active1, 0x4c00L);
            case 85:
                return jjMoveStringLiteralDfa5_8(active1, 0x8L);
            default :
                break;
            }
        return jjStartNfa_8(3, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa5_8(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_8(3, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_8(4, 0L, active1);
            return 5;
        }
        switch(curChar)
            {
            case 68:
                if ((active1 & 0x20L) != 0L)
                    return jjStopAtPos(5, 69);
                break;
            case 69:
                return jjMoveStringLiteralDfa6_8(active1, 0x3000L);
            case 73:
                return jjMoveStringLiteralDfa6_8(active1, 0x4818L);
            case 83:
                if ((active1 & 0x200L) != 0L)
                    return jjStartNfaWithStates_8(5, 73, 2);
                break;
            case 89:
                if ((active1 & 0x400L) != 0L)
                    return jjStartNfaWithStates_8(5, 74, 2);
                break;
            default :
                break;
            }
        return jjStartNfa_8(4, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa6_8(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_8(4, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_8(5, 0L, active1);
            return 6;
        }
        switch(curChar)
            {
            case 69:
                return jjMoveStringLiteralDfa7_8(active1, 0x810L);
            case 78:
                if ((active1 & 0x1000L) != 0L)
                    {
                        jjmatchedKind = 76;
                        jjmatchedPos = 6;
                    }
                return jjMoveStringLiteralDfa7_8(active1, 0x2000L);
            case 79:
                return jjMoveStringLiteralDfa7_8(active1, 0x4000L);
            case 82:
                return jjMoveStringLiteralDfa7_8(active1, 0x8L);
            default :
                break;
            }
        return jjStartNfa_8(5, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa7_8(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_8(5, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_8(6, 0L, active1);
            return 7;
        }
        switch(curChar)
            {
            case 68:
                if ((active1 & 0x10L) != 0L)
                    return jjStopAtPos(7, 68);
                break;
            case 69:
                return jjMoveStringLiteralDfa8_8(active1, 0x8L);
            case 78:
                if ((active1 & 0x4000L) != 0L)
                    return jjStartNfaWithStates_8(7, 78, 2);
                break;
            case 83:
                if ((active1 & 0x800L) != 0L)
                    return jjStartNfaWithStates_8(7, 75, 2);
                else if ((active1 & 0x2000L) != 0L)
                    return jjStartNfaWithStates_8(7, 77, 2);
                break;
            default :
                break;
            }
        return jjStartNfa_8(6, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa8_8(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_8(6, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_8(7, 0L, active1);
            return 8;
        }
        switch(curChar)
            {
            case 68:
                if ((active1 & 0x8L) != 0L)
                    return jjStopAtPos(8, 67);
                break;
            default :
                break;
            }
        return jjStartNfa_8(7, 0L, active1);
    }
    private final int jjMoveNfa_8(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 4;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 4:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 79)
                                                kind = 79;
                                            jjCheckNAdd(2);
                                        }
                                    else if ((0x800006000000000L & l) != 0L)
                                        {
                                            if (kind > 79)
                                                kind = 79;
                                            jjCheckNAdd(3);
                                        }
                                    else if (curChar == 60)
                                        {
                                            if (kind > 66)
                                                kind = 66;
                                            jjCheckNAdd(0);
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 79)
                                                kind = 79;
                                            jjCheckNAdd(1);
                                        }
                                    break;
                                case 0:
                                    if (curChar != 60)
                                        break;
                                    kind = 66;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 79)
                                        kind = 79;
                                    jjCheckNAdd(1);
                                    break;
                                case 2:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 79)
                                        kind = 79;
                                    jjCheckNAdd(2);
                                    break;
                                case 3:
                                    if ((0x800006000000000L & l) == 0L)
                                        break;
                                    kind = 79;
                                    jjCheckNAdd(3);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 4:
                                    if ((0xffffffffd7ffffffL & l) != 0L)
                                        {
                                            if (kind > 79)
                                                kind = 79;
                                            jjCheckNAdd(2);
                                        }
                                    else if ((0x28000000L & l) != 0L)
                                        {
                                            if (kind > 79)
                                                kind = 79;
                                            jjCheckNAdd(3);
                                        }
                                    break;
                                case 2:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 79;
                                    jjCheckNAdd(2);
                                    break;
                                case 3:
                                    if ((0x28000000L & l) == 0L)
                                        break;
                                    kind = 79;
                                    jjCheckNAdd(3);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 4:
                                case 2:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 79)
                                        kind = 79;
                                    jjCheckNAdd(2);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 4 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_7(int pos, long active0, long active1)
    {
        switch (pos)
            {
            case 0:
                if ((active1 & 0x60000L) != 0L)
                    {
                        jjmatchedKind = 83;
                        return 1;
                    }
                return -1;
            case 1:
                if ((active1 & 0x60000L) != 0L)
                    {
                        jjmatchedKind = 83;
                        jjmatchedPos = 1;
                        return 1;
                    }
                return -1;
            case 2:
                if ((active1 & 0x60000L) != 0L)
                    {
                        jjmatchedKind = 83;
                        jjmatchedPos = 2;
                        return 1;
                    }
                return -1;
            case 3:
                if ((active1 & 0x60000L) != 0L)
                    {
                        jjmatchedKind = 83;
                        jjmatchedPos = 3;
                        return 1;
                    }
                return -1;
            case 4:
                if ((active1 & 0x60000L) != 0L)
                    {
                        jjmatchedKind = 83;
                        jjmatchedPos = 4;
                        return 1;
                    }
                return -1;
            default :
                return -1;
            }
    }
    private final int jjStartNfa_7(int pos, long active0, long active1)
    {
        return jjMoveNfa_7(jjStopStringLiteralDfa_7(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_7(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_7(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_7()
    {
        switch(curChar)
            {
            case 34:
                return jjStopAtPos(0, 99);
            case 37:
                return jjStopAtPos(0, 86);
            case 39:
                return jjStopAtPos(0, 93);
            case 80:
                return jjMoveStringLiteralDfa1_7(0x20000L);
            case 83:
                return jjMoveStringLiteralDfa1_7(0x40000L);
            default :
                return jjMoveNfa_7(3, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_7(long active1)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_7(0, 0L, active1);
            return 1;
        }
        switch(curChar)
            {
            case 85:
                return jjMoveStringLiteralDfa2_7(active1, 0x20000L);
            case 89:
                return jjMoveStringLiteralDfa2_7(active1, 0x40000L);
            default :
                break;
            }
        return jjStartNfa_7(0, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa2_7(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_7(0, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_7(1, 0L, active1);
            return 2;
        }
        switch(curChar)
            {
            case 66:
                return jjMoveStringLiteralDfa3_7(active1, 0x20000L);
            case 83:
                return jjMoveStringLiteralDfa3_7(active1, 0x40000L);
            default :
                break;
            }
        return jjStartNfa_7(1, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa3_7(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_7(1, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_7(2, 0L, active1);
            return 3;
        }
        switch(curChar)
            {
            case 76:
                return jjMoveStringLiteralDfa4_7(active1, 0x20000L);
            case 84:
                return jjMoveStringLiteralDfa4_7(active1, 0x40000L);
            default :
                break;
            }
        return jjStartNfa_7(2, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa4_7(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_7(2, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_7(3, 0L, active1);
            return 4;
        }
        switch(curChar)
            {
            case 69:
                return jjMoveStringLiteralDfa5_7(active1, 0x40000L);
            case 73:
                return jjMoveStringLiteralDfa5_7(active1, 0x20000L);
            default :
                break;
            }
        return jjStartNfa_7(3, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa5_7(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_7(3, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_7(4, 0L, active1);
            return 5;
        }
        switch(curChar)
            {
            case 67:
                if ((active1 & 0x20000L) != 0L)
                    return jjStartNfaWithStates_7(5, 81, 1);
                break;
            case 77:
                if ((active1 & 0x40000L) != 0L)
                    return jjStartNfaWithStates_7(5, 82, 1);
                break;
            default :
                break;
            }
        return jjStartNfa_7(4, 0L, active1);
    }
    private final int jjMoveNfa_7(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 4;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 83)
                                                kind = 83;
                                            jjCheckNAdd(1);
                                        }
                                    else if ((0x3800804200000000L & l) != 0L)
                                        {
                                            if (kind > 84)
                                                kind = 84;
                                            jjCheckNAdd(2);
                                        }
                                    else if (curChar == 62)
                                        {
                                            if (kind > 85)
                                                kind = 85;
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 83)
                                                kind = 83;
                                            jjCheckNAdd(0);
                                        }
                                    break;
                                case 0:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 83)
                                        kind = 83;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 83)
                                        kind = 83;
                                    jjCheckNAdd(1);
                                    break;
                                case 2:
                                    if ((0x3800804200000000L & l) == 0L)
                                        break;
                                    kind = 84;
                                    jjCheckNAdd(2);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0xffffffffd7ffffffL & l) != 0L)
                                        {
                                            if (kind > 83)
                                                kind = 83;
                                            jjCheckNAdd(1);
                                        }
                                    else if (curChar == 91)
                                        {
                                            if (kind > 85)
                                                kind = 85;
                                        }
                                    else if (curChar == 93)
                                        {
                                            if (kind > 84)
                                                kind = 84;
                                            jjCheckNAdd(2);
                                        }
                                    break;
                                case 1:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 83;
                                    jjCheckNAdd(1);
                                    break;
                                case 2:
                                    if (curChar != 93)
                                        break;
                                    kind = 84;
                                    jjCheckNAdd(2);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                case 1:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 83)
                                        kind = 83;
                                    jjCheckNAdd(1);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 4 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_18(int pos, long active0, long active1)
    {
        switch (pos)
            {
            default :
                return -1;
            }
    }
    private final int jjStartNfa_18(int pos, long active0, long active1)
    {
        return jjMoveNfa_18(jjStopStringLiteralDfa_18(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_18(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_18(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_18()
    {
        switch(curChar)
            {
            case 34:
                return jjStopAtPos(0, 102);
            case 39:
                return jjStopAtPos(0, 96);
            case 61:
                return jjStopAtPos(0, 20);
            default :
                return jjMoveNfa_18(3, 0);
            }
    }
    private final int jjMoveNfa_18(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 5;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 17)
                                                kind = 17;
                                            jjCheckNAdd(0);
                                        }
                                    else if ((0x9800806200000000L & l) != 0L)
                                        {
                                            if (kind > 18)
                                                kind = 18;
                                            jjCheckNAdd(1);
                                        }
                                    else if (curChar == 62)
                                        {
                                            if (kind > 21)
                                                kind = 21;
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 19)
                                                kind = 19;
                                            jjCheckNAdd(2);
                                        }
                                    else if (curChar == 47)
                                        jjstateSet[jjnewStateCnt++] = 4;
                                    break;
                                case 0:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 17)
                                        kind = 17;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x9800806200000000L & l) == 0L)
                                        break;
                                    if (kind > 18)
                                        kind = 18;
                                    jjCheckNAdd(1);
                                    break;
                                case 2:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 19)
                                        kind = 19;
                                    jjCheckNAdd(2);
                                    break;
                                case 4:
                                    if (curChar == 62)
                                        kind = 21;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0xffffffffd7ffffffL & l) != 0L)
                                        {
                                            if (kind > 17)
                                                kind = 17;
                                            jjCheckNAdd(0);
                                        }
                                    else if ((0x28000000L & l) != 0L)
                                        {
                                            if (kind > 18)
                                                kind = 18;
                                            jjCheckNAdd(1);
                                        }
                                    break;
                                case 0:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 17;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x28000000L & l) == 0L)
                                        break;
                                    kind = 18;
                                    jjCheckNAdd(1);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 17)
                                        kind = 17;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 5 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_16(int pos, long active0)
    {
        switch (pos)
            {
            case 0:
                if ((active0 & 0x8000000L) != 0L)
                    {
                        jjmatchedKind = 28;
                        return -1;
                    }
                return -1;
            default :
                return -1;
            }
    }
    private final int jjStartNfa_16(int pos, long active0)
    {
        return jjMoveNfa_16(jjStopStringLiteralDfa_16(pos, active0), pos + 1);
    }
    private final int jjStartNfaWithStates_16(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_16(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_16()
    {
        switch(curChar)
            {
            case 63:
                return jjMoveStringLiteralDfa1_16(0x8000000L);
            default :
                return jjMoveNfa_16(2, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_16(long active0)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_16(0, active0);
            return 1;
        }
        switch(curChar)
            {
            case 62:
                if ((active0 & 0x8000000L) != 0L)
                    return jjStopAtPos(1, 27);
                break;
            default :
                break;
            }
        return jjStartNfa_16(0, active0);
    }
    private final int jjMoveNfa_16(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 4;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 28)
                                                kind = 28;
                                            jjCheckNAdd(0);
                                        }
                                    else if ((0xe80080ee00000000L & l) != 0L)
                                        {
                                            if (kind > 28)
                                                kind = 28;
                                        }
                                    else if (curChar == 60)
                                        {
                                            if (kind > 29)
                                                kind = 29;
                                            jjCheckNAdd(3);
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 28)
                                                kind = 28;
                                            jjCheckNAdd(1);
                                        }
                                    break;
                                case 0:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 28)
                                        kind = 28;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 28)
                                        kind = 28;
                                    jjCheckNAdd(1);
                                    break;
                                case 3:
                                    if (curChar != 60)
                                        break;
                                    kind = 29;
                                    jjCheckNAdd(3);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                    if ((0xffffffffd7ffffffL & l) != 0L)
                                        {
                                            if (kind > 28)
                                                kind = 28;
                                            jjCheckNAdd(0);
                                        }
                                    else if ((0x28000000L & l) != 0L)
                                        {
                                            if (kind > 28)
                                                kind = 28;
                                        }
                                    break;
                                case 0:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 28;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 28)
                                        kind = 28;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 4 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_1(int pos, long active0, long active1)
    {
        switch (pos)
            {
            default :
                return -1;
            }
    }
    private final int jjStartNfa_1(int pos, long active0, long active1)
    {
        return jjMoveNfa_1(jjStopStringLiteralDfa_1(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_1(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_1(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_1()
    {
        switch(curChar)
            {
            case 34:
                return jjStopAtPos(0, 101);
            default :
                return jjMoveNfa_1(0, 0);
            }
    }
    private final int jjMoveNfa_1(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 1;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    if ((0xfffffffbffffffffL & l) == 0L)
                                        break;
                                    kind = 100;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    kind = 100;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 100)
                                        kind = 100;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_0(int pos, long active0, long active1)
    {
        switch (pos)
            {
            case 0:
                if ((active1 & 0x100000000000L) != 0L)
                    return 2;
                return -1;
            case 1:
                if ((active1 & 0x100000000000L) != 0L)
                    return 3;
                return -1;
            default :
                return -1;
            }
    }
    private final int jjStartNfa_0(int pos, long active0, long active1)
    {
        return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_0(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_0(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_0()
    {
        switch(curChar)
            {
            case 45:
                return jjMoveStringLiteralDfa1_0(0x100000000000L);
            default :
                return jjMoveNfa_0(4, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_0(long active1)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_0(0, 0L, active1);
            return 1;
        }
        switch(curChar)
            {
            case 45:
                return jjMoveStringLiteralDfa2_0(active1, 0x100000000000L);
            default :
                break;
            }
        return jjStartNfa_0(0, 0L, active1);
    }
    private final int jjMoveStringLiteralDfa2_0(long old1, long active1)
    {
        if (((active1 &= old1)) == 0L)
            return jjStartNfa_0(0, 0L, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_0(1, 0L, active1);
            return 2;
        }
        switch(curChar)
            {
            case 62:
                if ((active1 & 0x100000000000L) != 0L)
                    return jjStopAtPos(2, 108);
                break;
            default :
                break;
            }
        return jjStartNfa_0(1, 0L, active1);
    }
    private final int jjMoveNfa_0(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 5;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 4:
                                    if ((0xffffdfffffffffffL & l) != 0L)
                                        {
                                            if (kind > 106)
                                                kind = 106;
                                            jjCheckNAddTwoStates(0, 1);
                                        }
                                    else if (curChar == 45)
                                        jjstateSet[jjnewStateCnt++] = 2;
                                    if (curChar == 45)
                                        jjCheckNAdd(0);
                                    break;
                                case 2:
                                    if ((0xffffdfffffffffffL & l) != 0L)
                                        {
                                            if (kind > 106)
                                                kind = 106;
                                            jjCheckNAddTwoStates(0, 1);
                                        }
                                    else if (curChar == 45)
                                        jjstateSet[jjnewStateCnt++] = 3;
                                    break;
                                case 0:
                                    if ((0xffffdfffffffffffL & l) == 0L)
                                        break;
                                    if (kind > 106)
                                        kind = 106;
                                    jjCheckNAddTwoStates(0, 1);
                                    break;
                                case 1:
                                    if (curChar == 45)
                                        jjCheckNAdd(0);
                                    break;
                                case 3:
                                    if ((0xbfffffffffffffffL & l) != 0L && kind > 107)
                                        kind = 107;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 4:
                                case 0:
                                    if (kind > 106)
                                        kind = 106;
                                    jjCheckNAddTwoStates(0, 1);
                                    break;
                                case 2:
                                    if (kind > 106)
                                        kind = 106;
                                    jjCheckNAddTwoStates(0, 1);
                                    break;
                                case 3:
                                    if (kind > 107)
                                        kind = 107;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 4:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 106)
                                        kind = 106;
                                    jjCheckNAddTwoStates(0, 1);
                                    break;
                                case 2:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 106)
                                        kind = 106;
                                    jjCheckNAddTwoStates(0, 1);
                                    break;
                                case 3:
                                    if (jjCanMove_0(hiByte, i1, i2, l1, l2) && kind > 107)
                                        kind = 107;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 5 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_11(int pos, long active0, long active1)
    {
        switch (pos)
            {
            case 0:
                if ((active0 & 0x50000000000000L) != 0L)
                    {
                        jjmatchedKind = 55;
                        return 0;
                    }
                return -1;
            case 1:
                if ((active0 & 0x50000000000000L) != 0L)
                    {
                        jjmatchedKind = 55;
                        jjmatchedPos = 1;
                        return 0;
                    }
                return -1;
            case 2:
                if ((active0 & 0x40000000000000L) != 0L)
                    return 0;
                if ((active0 & 0x10000000000000L) != 0L)
                    {
                        jjmatchedKind = 55;
                        jjmatchedPos = 2;
                        return 0;
                    }
                return -1;
            case 3:
                if ((active0 & 0x10000000000000L) != 0L)
                    {
                        jjmatchedKind = 55;
                        jjmatchedPos = 3;
                        return 0;
                    }
                return -1;
            case 4:
                if ((active0 & 0x10000000000000L) != 0L)
                    return 0;
                return -1;
            default :
                return -1;
            }
    }
    private final int jjStartNfa_11(int pos, long active0, long active1)
    {
        return jjMoveNfa_11(jjStopStringLiteralDfa_11(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_11(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_11(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_11()
    {
        switch(curChar)
            {
            case 35:
                return jjMoveStringLiteralDfa1_11(0x20000000000000L);
            case 37:
                return jjStopAtPos(0, 86);
            case 62:
                return jjStopAtPos(0, 56);
            case 65:
                return jjMoveStringLiteralDfa1_11(0x40000000000000L);
            case 69:
                return jjMoveStringLiteralDfa1_11(0x10000000000000L);
            default :
                return jjMoveNfa_11(2, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_11(long active0)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_11(0, active0, 0L);
            return 1;
        }
        switch(curChar)
            {
            case 77:
                return jjMoveStringLiteralDfa2_11(active0, 0x10000000000000L);
            case 78:
                return jjMoveStringLiteralDfa2_11(active0, 0x40000000000000L);
            case 80:
                return jjMoveStringLiteralDfa2_11(active0, 0x20000000000000L);
            default :
                break;
            }
        return jjStartNfa_11(0, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa2_11(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_11(0, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_11(1, active0, 0L);
            return 2;
        }
        switch(curChar)
            {
            case 67:
                return jjMoveStringLiteralDfa3_11(active0, 0x20000000000000L);
            case 80:
                return jjMoveStringLiteralDfa3_11(active0, 0x10000000000000L);
            case 89:
                if ((active0 & 0x40000000000000L) != 0L)
                    return jjStartNfaWithStates_11(2, 54, 0);
                break;
            default :
                break;
            }
        return jjStartNfa_11(1, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa3_11(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_11(1, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_11(2, active0, 0L);
            return 3;
        }
        switch(curChar)
            {
            case 68:
                return jjMoveStringLiteralDfa4_11(active0, 0x20000000000000L);
            case 84:
                return jjMoveStringLiteralDfa4_11(active0, 0x10000000000000L);
            default :
                break;
            }
        return jjStartNfa_11(2, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa4_11(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_11(2, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_11(3, active0, 0L);
            return 4;
        }
        switch(curChar)
            {
            case 65:
                return jjMoveStringLiteralDfa5_11(active0, 0x20000000000000L);
            case 89:
                if ((active0 & 0x10000000000000L) != 0L)
                    return jjStartNfaWithStates_11(4, 52, 0);
                break;
            default :
                break;
            }
        return jjStartNfa_11(3, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa5_11(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_11(3, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_11(4, active0, 0L);
            return 5;
        }
        switch(curChar)
            {
            case 84:
                return jjMoveStringLiteralDfa6_11(active0, 0x20000000000000L);
            default :
                break;
            }
        return jjStartNfa_11(4, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa6_11(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_11(4, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_11(5, active0, 0L);
            return 6;
        }
        switch(curChar)
            {
            case 65:
                if ((active0 & 0x20000000000000L) != 0L)
                    return jjStopAtPos(6, 53);
                break;
            default :
                break;
            }
        return jjStartNfa_11(5, active0, 0L);
    }
    private final int jjMoveNfa_11(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 3;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 55)
                                                kind = 55;
                                            jjCheckNAdd(0);
                                        }
                                    else if (curChar == 63)
                                        {
                                            if (kind > 55)
                                                kind = 55;
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 55)
                                                kind = 55;
                                            jjCheckNAdd(1);
                                        }
                                    break;
                                case 0:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 55)
                                        kind = 55;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 55)
                                        kind = 55;
                                    jjCheckNAdd(1);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                case 0:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 55;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 55)
                                        kind = 55;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_5(int pos, long active0, long active1)
    {
        switch (pos)
            {
            default :
                return -1;
            }
    }
    private final int jjStartNfa_5(int pos, long active0, long active1)
    {
        return jjMoveNfa_5(jjStopStringLiteralDfa_5(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_5(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_5(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_5()
    {
        switch(curChar)
            {
            case 38:
                return jjStopAtPos(0, 89);
            case 39:
                return jjStopAtPos(0, 98);
            default :
                return jjMoveNfa_5(0, 0);
            }
    }
    private final int jjMoveNfa_5(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 1;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    if ((0xffffff3fffffffffL & l) == 0L)
                                        break;
                                    kind = 97;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    kind = 97;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 97)
                                        kind = 97;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjMoveStringLiteralDfa0_6()
    {
        return jjMoveNfa_6(1, 0);
    }
    private final int jjMoveNfa_6(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 3;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 1:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 87)
                                                kind = 87;
                                            jjCheckNAdd(0);
                                        }
                                    else if (curChar == 59)
                                        {
                                            if (kind > 88)
                                                kind = 88;
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 88)
                                                kind = 88;
                                            jjCheckNAdd(2);
                                        }
                                    break;
                                case 0:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 87)
                                        kind = 87;
                                    jjCheckNAdd(0);
                                    break;
                                case 2:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 88)
                                        kind = 88;
                                    jjCheckNAdd(2);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 1:
                                case 0:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 87;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 1:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 87)
                                        kind = 87;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_14(int pos, long active0)
    {
        switch (pos)
            {
            case 0:
                if ((active0 & 0x800000000L) != 0L)
                    return 2;
                return -1;
            case 1:
                if ((active0 & 0x800000000L) != 0L)
                    return 4;
                return -1;
            default :
                return -1;
            }
    }
    private final int jjStartNfa_14(int pos, long active0)
    {
        return jjMoveNfa_14(jjStopStringLiteralDfa_14(pos, active0), pos + 1);
    }
    private final int jjStartNfaWithStates_14(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_14(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_14()
    {
        switch(curChar)
            {
            case 93:
                return jjMoveStringLiteralDfa1_14(0x800000000L);
            default :
                return jjMoveNfa_14(6, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_14(long active0)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_14(0, active0);
            return 1;
        }
        switch(curChar)
            {
            case 93:
                return jjMoveStringLiteralDfa2_14(active0, 0x800000000L);
            default :
                break;
            }
        return jjStartNfa_14(0, active0);
    }
    private final int jjMoveStringLiteralDfa2_14(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_14(0, old0); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_14(1, active0);
            return 2;
        }
        switch(curChar)
            {
            case 62:
                if ((active0 & 0x800000000L) != 0L)
                    return jjStopAtPos(2, 35);
                break;
            default :
                break;
            }
        return jjStartNfa_14(1, active0);
    }
    private final int jjMoveNfa_14(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 16;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                    if (kind > 36)
                                        kind = 36;
                                    jjCheckNAddStates(0, 2);
                                    break;
                                case 6:
                                    if ((0xefffffffffffffffL & l) != 0L)
                                        {
                                            if (kind > 36)
                                                kind = 36;
                                            jjCheckNAddStates(0, 2);
                                        }
                                    else if (curChar == 60)
                                        {
                                            if (kind > 36)
                                                kind = 36;
                                            jjCheckNAddTwoStates(7, 8);
                                        }
                                    break;
                                case 0:
                                    if ((0xefffffffffffffffL & l) == 0L)
                                        break;
                                    if (kind > 36)
                                        kind = 36;
                                    jjCheckNAddStates(0, 2);
                                    break;
                                case 4:
                                    if ((0xbfffffffffffffffL & l) == 0L)
                                        break;
                                    if (kind > 36)
                                        kind = 36;
                                    jjCheckNAddStates(0, 2);
                                    break;
                                case 7:
                                    if ((0x800200000000L & l) != 0L)
                                        jjCheckNAdd(8);
                                    break;
                                case 8:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        jjCheckNAddStates(3, 7);
                                    break;
                                case 9:
                                    if ((0xefffffffffffffffL & l) != 0L)
                                        jjCheckNAddStates(8, 11);
                                    break;
                                case 11:
                                    jjCheckNAddStates(8, 11);
                                    break;
                                case 13:
                                    if ((0xbfffffffffffffffL & l) != 0L)
                                        jjCheckNAddStates(8, 11);
                                    break;
                                case 15:
                                    if (curChar == 62 && kind > 37)
                                        kind = 37;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                    if ((0xffffffffdfffffffL & l) != 0L)
                                        {
                                            if (kind > 36)
                                                kind = 36;
                                            jjCheckNAddStates(0, 2);
                                        }
                                    else if (curChar == 93)
                                        jjstateSet[jjnewStateCnt++] = 4;
                                    break;
                                case 6:
                                    if ((0xffffffffdfffffffL & l) != 0L)
                                        {
                                            if (kind > 36)
                                                kind = 36;
                                            jjCheckNAddStates(0, 2);
                                        }
                                    else if (curChar == 93)
                                        jjstateSet[jjnewStateCnt++] = 3;
                                    if (curChar == 93)
                                        jjstateSet[jjnewStateCnt++] = 2;
                                    break;
                                case 0:
                                    if ((0xffffffffdfffffffL & l) == 0L)
                                        break;
                                    if (kind > 36)
                                        kind = 36;
                                    jjCheckNAddStates(0, 2);
                                    break;
                                case 1:
                                    if (curChar == 93)
                                        jjstateSet[jjnewStateCnt++] = 2;
                                    break;
                                case 3:
                                    if (curChar == 93)
                                        jjstateSet[jjnewStateCnt++] = 4;
                                    break;
                                case 4:
                                    if (kind > 36)
                                        kind = 36;
                                    jjCheckNAddStates(0, 2);
                                    break;
                                case 5:
                                    if (curChar == 93)
                                        jjstateSet[jjnewStateCnt++] = 3;
                                    break;
                                case 8:
                                    if ((0xffffffffd7ffffffL & l) != 0L)
                                        jjCheckNAddStates(3, 7);
                                    break;
                                case 9:
                                case 11:
                                    if ((0xffffffffdfffffffL & l) != 0L)
                                        jjCheckNAddStates(8, 11);
                                    break;
                                case 10:
                                    if (curChar == 93)
                                        jjstateSet[jjnewStateCnt++] = 11;
                                    break;
                                case 12:
                                    if (curChar == 93)
                                        jjstateSet[jjnewStateCnt++] = 13;
                                    break;
                                case 13:
                                    jjCheckNAddStates(8, 11);
                                    break;
                                case 14:
                                    if (curChar == 93)
                                        jjstateSet[jjnewStateCnt++] = 12;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 2:
                                case 4:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 36)
                                        kind = 36;
                                    jjCheckNAddStates(0, 2);
                                    break;
                                case 6:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 36)
                                        kind = 36;
                                    jjCheckNAddStates(0, 2);
                                    break;
                                case 8:
                                    if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        jjCheckNAddStates(3, 7);
                                    break;
                                case 9:
                                case 11:
                                case 13:
                                    if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        jjCheckNAddStates(8, 11);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 16 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_15(int pos, long active0)
    {
        switch (pos)
            {
            default :
                return -1;
            }
    }
    private final int jjStartNfa_15(int pos, long active0)
    {
        return jjMoveNfa_15(jjStopStringLiteralDfa_15(pos, active0), pos + 1);
    }
    private final int jjStartNfaWithStates_15(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_15(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_15()
    {
        switch(curChar)
            {
            case 63:
                jjmatchedKind = 34;
                return jjMoveStringLiteralDfa1_15(0x200000000L);
            default :
                return jjMoveNfa_15(6, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_15(long active0)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_15(0, active0);
            return 1;
        }
        switch(curChar)
            {
            case 62:
                if ((active0 & 0x200000000L) != 0L)
                    return jjStopAtPos(1, 33);
                break;
            default :
                break;
            }
        return jjStartNfa_15(0, active0);
    }
    private final int jjMoveNfa_15(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 27;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 6:
                                    if ((0x5ffffffefffff9ffL & l) != 0L)
                                        {
                                            if (kind > 31)
                                                kind = 31;
                                            jjCheckNAdd(25);
                                        }
                                    else if ((0x2000000100000600L & l) != 0L)
                                        {
                                            if (kind > 32)
                                                kind = 32;
                                            jjCheckNAdd(26);
                                        }
                                    break;
                                case 25:
                                    if ((0x5ffffffefffff9ffL & l) == 0L)
                                        break;
                                    kind = 31;
                                    jjCheckNAdd(25);
                                    break;
                                case 26:
                                    if ((0x2000000100000600L & l) == 0L)
                                        break;
                                    kind = 32;
                                    jjCheckNAdd(26);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 6:
                                    if (kind > 31)
                                        kind = 31;
                                    jjCheckNAdd(25);
                                    if (curChar == 115)
                                        jjstateSet[jjnewStateCnt++] = 23;
                                    else if (curChar == 101)
                                        jjstateSet[jjnewStateCnt++] = 13;
                                    else if (curChar == 118)
                                        jjstateSet[jjnewStateCnt++] = 5;
                                    break;
                                case 0:
                                    if (curChar == 110 && kind > 30)
                                        kind = 30;
                                    break;
                                case 1:
                                    if (curChar == 111)
                                        jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                case 2:
                                    if (curChar == 105)
                                        jjstateSet[jjnewStateCnt++] = 1;
                                    break;
                                case 3:
                                    if (curChar == 115)
                                        jjstateSet[jjnewStateCnt++] = 2;
                                    break;
                                case 4:
                                    if (curChar == 114)
                                        jjstateSet[jjnewStateCnt++] = 3;
                                    break;
                                case 5:
                                    if (curChar == 101)
                                        jjstateSet[jjnewStateCnt++] = 4;
                                    break;
                                case 7:
                                    if (curChar == 103 && kind > 30)
                                        kind = 30;
                                    break;
                                case 8:
                                    if (curChar == 110)
                                        jjstateSet[jjnewStateCnt++] = 7;
                                    break;
                                case 9:
                                    if (curChar == 105)
                                        jjstateSet[jjnewStateCnt++] = 8;
                                    break;
                                case 10:
                                    if (curChar == 100)
                                        jjstateSet[jjnewStateCnt++] = 9;
                                    break;
                                case 11:
                                    if (curChar == 111)
                                        jjstateSet[jjnewStateCnt++] = 10;
                                    break;
                                case 12:
                                    if (curChar == 99)
                                        jjstateSet[jjnewStateCnt++] = 11;
                                    break;
                                case 13:
                                    if (curChar == 110)
                                        jjstateSet[jjnewStateCnt++] = 12;
                                    break;
                                case 14:
                                    if (curChar == 101)
                                        jjstateSet[jjnewStateCnt++] = 13;
                                    break;
                                case 15:
                                    if (curChar == 101 && kind > 30)
                                        kind = 30;
                                    break;
                                case 16:
                                    if (curChar == 110)
                                        jjstateSet[jjnewStateCnt++] = 15;
                                    break;
                                case 17:
                                    if (curChar == 111)
                                        jjstateSet[jjnewStateCnt++] = 16;
                                    break;
                                case 18:
                                    if (curChar == 108)
                                        jjstateSet[jjnewStateCnt++] = 17;
                                    break;
                                case 19:
                                    if (curChar == 97)
                                        jjstateSet[jjnewStateCnt++] = 18;
                                    break;
                                case 20:
                                    if (curChar == 100)
                                        jjstateSet[jjnewStateCnt++] = 19;
                                    break;
                                case 21:
                                    if (curChar == 110)
                                        jjstateSet[jjnewStateCnt++] = 20;
                                    break;
                                case 22:
                                    if (curChar == 97)
                                        jjstateSet[jjnewStateCnt++] = 21;
                                    break;
                                case 23:
                                    if (curChar == 116)
                                        jjstateSet[jjnewStateCnt++] = 22;
                                    break;
                                case 24:
                                    if (curChar == 115)
                                        jjstateSet[jjnewStateCnt++] = 23;
                                    break;
                                case 25:
                                    if (kind > 31)
                                        kind = 31;
                                    jjCheckNAdd(25);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 6:
                                case 25:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 31)
                                        kind = 31;
                                    jjCheckNAdd(25);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 27 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_10(int pos, long active0, long active1)
    {
        switch (pos)
            {
            default :
                return -1;
            }
    }
    private final int jjStartNfa_10(int pos, long active0, long active1)
    {
        return jjMoveNfa_10(jjStopStringLiteralDfa_10(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_10(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_10(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_10()
    {
        switch(curChar)
            {
            case 34:
                return jjStopAtPos(0, 99);
            case 39:
                return jjStopAtPos(0, 93);
            case 60:
                return jjStopAtPos(0, 59);
            case 62:
                return jjStopAtPos(0, 60);
            default :
                return jjMoveNfa_10(5, 0);
            }
    }
    private final int jjMoveNfa_10(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 14;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 5:
                                    if ((0xafffff7afffff9ffL & l) != 0L)
                                        {
                                            if (kind > 58)
                                                kind = 58;
                                            jjCheckNAdd(12);
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 58)
                                                kind = 58;
                                            jjCheckNAdd(13);
                                        }
                                    break;
                                case 12:
                                    if ((0xafffff7afffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 58)
                                        kind = 58;
                                    jjCheckNAdd(12);
                                    break;
                                case 13:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 58)
                                        kind = 58;
                                    jjCheckNAdd(13);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 5:
                                    if (kind > 58)
                                        kind = 58;
                                    jjCheckNAdd(12);
                                    if (curChar == 80)
                                        jjstateSet[jjnewStateCnt++] = 10;
                                    else if (curChar == 83)
                                        jjstateSet[jjnewStateCnt++] = 4;
                                    break;
                                case 0:
                                    if (curChar == 77 && kind > 57)
                                        kind = 57;
                                    break;
                                case 1:
                                    if (curChar == 69)
                                        jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                case 2:
                                    if (curChar == 84)
                                        jjstateSet[jjnewStateCnt++] = 1;
                                    break;
                                case 3:
                                    if (curChar == 83)
                                        jjstateSet[jjnewStateCnt++] = 2;
                                    break;
                                case 4:
                                    if (curChar == 89)
                                        jjstateSet[jjnewStateCnt++] = 3;
                                    break;
                                case 6:
                                    if (curChar == 67 && kind > 57)
                                        kind = 57;
                                    break;
                                case 7:
                                    if (curChar == 73)
                                        jjstateSet[jjnewStateCnt++] = 6;
                                    break;
                                case 8:
                                    if (curChar == 76)
                                        jjstateSet[jjnewStateCnt++] = 7;
                                    break;
                                case 9:
                                    if (curChar == 66)
                                        jjstateSet[jjnewStateCnt++] = 8;
                                    break;
                                case 10:
                                    if (curChar == 85)
                                        jjstateSet[jjnewStateCnt++] = 9;
                                    break;
                                case 11:
                                    if (curChar == 80)
                                        jjstateSet[jjnewStateCnt++] = 10;
                                    break;
                                case 12:
                                    if (kind > 58)
                                        kind = 58;
                                    jjCheckNAdd(12);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 5:
                                case 12:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 58)
                                        kind = 58;
                                    jjCheckNAdd(12);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 14 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_13(int pos, long active0)
    {
        switch (pos)
            {
            case 0:
                if ((active0 & 0xf8000000000L) != 0L)
                    {
                        jjmatchedKind = 44;
                        return 0;
                    }
                return -1;
            case 1:
                if ((active0 & 0xf8000000000L) != 0L)
                    {
                        jjmatchedKind = 44;
                        jjmatchedPos = 1;
                        return 0;
                    }
                return -1;
            case 2:
                if ((active0 & 0xf8000000000L) != 0L)
                    {
                        jjmatchedKind = 44;
                        jjmatchedPos = 2;
                        return 0;
                    }
                return -1;
            case 3:
                if ((active0 & 0xf8000000000L) != 0L)
                    {
                        jjmatchedKind = 44;
                        jjmatchedPos = 3;
                        return 0;
                    }
                return -1;
            case 4:
                if ((active0 & 0xf8000000000L) != 0L)
                    {
                        jjmatchedKind = 44;
                        jjmatchedPos = 4;
                        return 0;
                    }
                return -1;
            case 5:
                if ((active0 & 0x8000000000L) != 0L)
                    return 0;
                if ((active0 & 0xf0000000000L) != 0L)
                    {
                        jjmatchedKind = 44;
                        jjmatchedPos = 5;
                        return 0;
                    }
                return -1;
            case 6:
                if ((active0 & 0x70000000000L) != 0L)
                    return 0;
                if ((active0 & 0x80000000000L) != 0L)
                    {
                        jjmatchedKind = 44;
                        jjmatchedPos = 6;
                        return 0;
                    }
                return -1;
            default :
                return -1;
            }
    }
    private final int jjStartNfa_13(int pos, long active0)
    {
        return jjMoveNfa_13(jjStopStringLiteralDfa_13(pos, active0), pos + 1);
    }
    private final int jjStartNfaWithStates_13(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_13(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_13()
    {
        switch(curChar)
            {
            case 62:
                return jjStopAtPos(0, 48);
            case 65:
                return jjMoveStringLiteralDfa1_13(0x10000000000L);
            case 68:
                return jjMoveStringLiteralDfa1_13(0x20000000000L);
            case 69:
                return jjMoveStringLiteralDfa1_13(0x48000000000L);
            case 78:
                return jjMoveStringLiteralDfa1_13(0x80000000000L);
            case 91:
                return jjStopAtPos(0, 47);
            default :
                return jjMoveNfa_13(3, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_13(long active0)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_13(0, active0);
            return 1;
        }
        switch(curChar)
            {
            case 76:
                return jjMoveStringLiteralDfa2_13(active0, 0x40000000000L);
            case 78:
                return jjMoveStringLiteralDfa2_13(active0, 0x8000000000L);
            case 79:
                return jjMoveStringLiteralDfa2_13(active0, 0xa0000000000L);
            case 84:
                return jjMoveStringLiteralDfa2_13(active0, 0x10000000000L);
            default :
                break;
            }
        return jjStartNfa_13(0, active0);
    }
    private final int jjMoveStringLiteralDfa2_13(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_13(0, old0); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_13(1, active0);
            return 2;
        }
        switch(curChar)
            {
            case 67:
                return jjMoveStringLiteralDfa3_13(active0, 0x20000000000L);
            case 69:
                return jjMoveStringLiteralDfa3_13(active0, 0x40000000000L);
            case 84:
                return jjMoveStringLiteralDfa3_13(active0, 0x98000000000L);
            default :
                break;
            }
        return jjStartNfa_13(1, active0);
    }
    private final int jjMoveStringLiteralDfa3_13(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_13(1, old0); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_13(2, active0);
            return 3;
        }
        switch(curChar)
            {
            case 65:
                return jjMoveStringLiteralDfa4_13(active0, 0x80000000000L);
            case 73:
                return jjMoveStringLiteralDfa4_13(active0, 0x8000000000L);
            case 76:
                return jjMoveStringLiteralDfa4_13(active0, 0x10000000000L);
            case 77:
                return jjMoveStringLiteralDfa4_13(active0, 0x40000000000L);
            case 84:
                return jjMoveStringLiteralDfa4_13(active0, 0x20000000000L);
            default :
                break;
            }
        return jjStartNfa_13(2, active0);
    }
    private final int jjMoveStringLiteralDfa4_13(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_13(2, old0); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_13(3, active0);
            return 4;
        }
        switch(curChar)
            {
            case 69:
                return jjMoveStringLiteralDfa5_13(active0, 0x40000000000L);
            case 73:
                return jjMoveStringLiteralDfa5_13(active0, 0x10000000000L);
            case 84:
                return jjMoveStringLiteralDfa5_13(active0, 0x88000000000L);
            case 89:
                return jjMoveStringLiteralDfa5_13(active0, 0x20000000000L);
            default :
                break;
            }
        return jjStartNfa_13(3, active0);
    }
    private final int jjMoveStringLiteralDfa5_13(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_13(3, old0); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_13(4, active0);
            return 5;
        }
        switch(curChar)
            {
            case 73:
                return jjMoveStringLiteralDfa6_13(active0, 0x80000000000L);
            case 78:
                return jjMoveStringLiteralDfa6_13(active0, 0x40000000000L);
            case 80:
                return jjMoveStringLiteralDfa6_13(active0, 0x20000000000L);
            case 83:
                return jjMoveStringLiteralDfa6_13(active0, 0x10000000000L);
            case 89:
                if ((active0 & 0x8000000000L) != 0L)
                    return jjStartNfaWithStates_13(5, 39, 0);
                break;
            default :
                break;
            }
        return jjStartNfa_13(4, active0);
    }
    private final int jjMoveStringLiteralDfa6_13(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_13(4, old0); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_13(5, active0);
            return 6;
        }
        switch(curChar)
            {
            case 69:
                if ((active0 & 0x20000000000L) != 0L)
                    return jjStartNfaWithStates_13(6, 41, 0);
                break;
            case 79:
                return jjMoveStringLiteralDfa7_13(active0, 0x80000000000L);
            case 84:
                if ((active0 & 0x10000000000L) != 0L)
                    return jjStartNfaWithStates_13(6, 40, 0);
                else if ((active0 & 0x40000000000L) != 0L)
                    return jjStartNfaWithStates_13(6, 42, 0);
                break;
            default :
                break;
            }
        return jjStartNfa_13(5, active0);
    }
    private final int jjMoveStringLiteralDfa7_13(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_13(5, old0); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_13(6, active0);
            return 7;
        }
        switch(curChar)
            {
            case 78:
                if ((active0 & 0x80000000000L) != 0L)
                    return jjStartNfaWithStates_13(7, 43, 0);
                break;
            default :
                break;
            }
        return jjStartNfa_13(6, active0);
    }
    private final int jjMoveNfa_13(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 3;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 44)
                                                kind = 44;
                                            jjCheckNAdd(0);
                                        }
                                    else if ((0x3800808600000000L & l) != 0L)
                                        {
                                            if (kind > 46)
                                                kind = 46;
                                            jjCheckNAdd(2);
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 45)
                                                kind = 45;
                                            jjCheckNAdd(1);
                                        }
                                    break;
                                case 0:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 44)
                                        kind = 44;
                                    jjCheckNAdd(0);
                                    break;
                                case 1:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 45)
                                        kind = 45;
                                    jjCheckNAdd(1);
                                    break;
                                case 2:
                                    if ((0x3800808600000000L & l) == 0L)
                                        break;
                                    kind = 46;
                                    jjCheckNAdd(2);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0xffffffffd7ffffffL & l) != 0L)
                                        {
                                            if (kind > 44)
                                                kind = 44;
                                            jjCheckNAdd(0);
                                        }
                                    else if (curChar == 93)
                                        {
                                            if (kind > 46)
                                                kind = 46;
                                            jjCheckNAdd(2);
                                        }
                                    break;
                                case 0:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    kind = 44;
                                    jjCheckNAdd(0);
                                    break;
                                case 2:
                                    if (curChar != 93)
                                        break;
                                    kind = 46;
                                    jjCheckNAdd(2);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 44)
                                        kind = 44;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_20(int pos, long active0, long active1)
    {
        switch (pos)
            {
            default :
                return -1;
            }
    }
    private final int jjStartNfa_20(int pos, long active0, long active1)
    {
        return jjMoveNfa_20(jjStopStringLiteralDfa_20(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_20(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_20(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_20()
    {
        switch(curChar)
            {
            case 38:
                return jjStopAtPos(0, 89);
            case 60:
                jjmatchedKind = 7;
                return jjMoveStringLiteralDfa1_20(0x4700L, 0x20000000000L);
            case 93:
                jjmatchedKind = 6;
                return jjMoveStringLiteralDfa1_20(0x1800L, 0x0L);
            default :
                return jjMoveNfa_20(1, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_20(long active0, long active1)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_20(0, active0, active1);
            return 1;
        }
        switch(curChar)
            {
            case 33:
                if ((active0 & 0x100L) != 0L)
                    {
                        jjmatchedKind = 8;
                        jjmatchedPos = 1;
                    }
                return jjMoveStringLiteralDfa2_20(active0, 0x400L, active1, 0x20000000000L);
            case 60:
                if ((active0 & 0x4000L) != 0L)
                    return jjStopAtPos(1, 14);
                break;
            case 62:
                if ((active0 & 0x1000L) != 0L)
                    return jjStopAtPos(1, 12);
                break;
            case 63:
                if ((active0 & 0x200L) != 0L)
                    return jjStopAtPos(1, 9);
                break;
            case 93:
                return jjMoveStringLiteralDfa2_20(active0, 0x800L, active1, 0L);
            default :
                break;
            }
        return jjStartNfa_20(0, active0, active1);
    }
    private final int jjMoveStringLiteralDfa2_20(long old0, long active0, long old1, long active1)
    {
        if (((active0 &= old0) | (active1 &= old1)) == 0L)
            return jjStartNfa_20(0, old0, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_20(1, active0, active1);
            return 2;
        }
        switch(curChar)
            {
            case 45:
                return jjMoveStringLiteralDfa3_20(active0, 0L, active1, 0x20000000000L);
            case 62:
                if ((active0 & 0x800L) != 0L)
                    return jjStopAtPos(2, 11);
                break;
            case 91:
                return jjMoveStringLiteralDfa3_20(active0, 0x400L, active1, 0L);
            default :
                break;
            }
        return jjStartNfa_20(1, active0, active1);
    }
    private final int jjMoveStringLiteralDfa3_20(long old0, long active0, long old1, long active1)
    {
        if (((active0 &= old0) | (active1 &= old1)) == 0L)
            return jjStartNfa_20(1, old0, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_20(2, active0, active1);
            return 3;
        }
        switch(curChar)
            {
            case 45:
                if ((active1 & 0x20000000000L) != 0L)
                    return jjStopAtPos(3, 105);
                break;
            case 67:
                return jjMoveStringLiteralDfa4_20(active0, 0x400L, active1, 0L);
            default :
                break;
            }
        return jjStartNfa_20(2, active0, active1);
    }
    private final int jjMoveStringLiteralDfa4_20(long old0, long active0, long old1, long active1)
    {
        if (((active0 &= old0) | (active1 &= old1)) == 0L)
            return jjStartNfa_20(2, old0, old1); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_20(3, active0, 0L);
            return 4;
        }
        switch(curChar)
            {
            case 68:
                return jjMoveStringLiteralDfa5_20(active0, 0x400L);
            default :
                break;
            }
        return jjStartNfa_20(3, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa5_20(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_20(3, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_20(4, active0, 0L);
            return 5;
        }
        switch(curChar)
            {
            case 65:
                return jjMoveStringLiteralDfa6_20(active0, 0x400L);
            default :
                break;
            }
        return jjStartNfa_20(4, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa6_20(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_20(4, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_20(5, active0, 0L);
            return 6;
        }
        switch(curChar)
            {
            case 84:
                return jjMoveStringLiteralDfa7_20(active0, 0x400L);
            default :
                break;
            }
        return jjStartNfa_20(5, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa7_20(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_20(5, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_20(6, active0, 0L);
            return 7;
        }
        switch(curChar)
            {
            case 65:
                return jjMoveStringLiteralDfa8_20(active0, 0x400L);
            default :
                break;
            }
        return jjStartNfa_20(6, active0, 0L);
    }
    private final int jjMoveStringLiteralDfa8_20(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_20(6, old0, 0L);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_20(7, active0, 0L);
            return 8;
        }
        switch(curChar)
            {
            case 91:
                if ((active0 & 0x400L) != 0L)
                    return jjStopAtPos(8, 10);
                break;
            default :
                break;
            }
        return jjStartNfa_20(7, active0, 0L);
    }
    private final int jjMoveNfa_20(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 2;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 1:
                                    if ((0xefffffbfffffffffL & l) != 0L)
                                        {
                                            if (kind > 13)
                                                kind = 13;
                                            jjCheckNAdd(0);
                                        }
                                    if (curChar == 62)
                                        {
                                            if (kind > 13)
                                                kind = 13;
                                        }
                                    break;
                                case 0:
                                    if ((0xefffffbfffffffffL & l) == 0L)
                                        break;
                                    if (kind > 13)
                                        kind = 13;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 1:
                                    if ((0xffffffffdfffffffL & l) != 0L)
                                        {
                                            if (kind > 13)
                                                kind = 13;
                                            jjCheckNAdd(0);
                                        }
                                    else if (curChar == 93)
                                        {
                                            if (kind > 13)
                                                kind = 13;
                                        }
                                    break;
                                case 0:
                                    if ((0xffffffffdfffffffL & l) == 0L)
                                        break;
                                    kind = 13;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 1:
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 13)
                                        kind = 13;
                                    jjCheckNAdd(0);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 2 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_17(int pos, long active0)
    {
        switch (pos)
            {
            case 0:
                if ((active0 & 0x400000L) != 0L)
                    {
                        jjmatchedKind = 26;
                        return 5;
                    }
                return -1;
            case 1:
                if ((active0 & 0x400000L) != 0L)
                    {
                        jjmatchedKind = 26;
                        jjmatchedPos = 1;
                        return 5;
                    }
                return -1;
            default :
                return -1;
            }
    }
    private final int jjStartNfa_17(int pos, long active0)
    {
        return jjMoveNfa_17(jjStopStringLiteralDfa_17(pos, active0), pos + 1);
    }
    private final int jjStartNfaWithStates_17(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_17(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_17()
    {
        switch(curChar)
            {
            case 63:
                return jjMoveStringLiteralDfa1_17(0x1000000L);
            case 120:
                return jjMoveStringLiteralDfa1_17(0x400000L);
            default :
                return jjMoveNfa_17(3, 0);
            }
    }
    private final int jjMoveStringLiteralDfa1_17(long active0)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_17(0, active0);
            return 1;
        }
        switch(curChar)
            {
            case 62:
                if ((active0 & 0x1000000L) != 0L)
                    return jjStopAtPos(1, 24);
                break;
            case 109:
                return jjMoveStringLiteralDfa2_17(active0, 0x400000L);
            default :
                break;
            }
        return jjStartNfa_17(0, active0);
    }
    private final int jjMoveStringLiteralDfa2_17(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_17(0, old0); 
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            jjStopStringLiteralDfa_17(1, active0);
            return 2;
        }
        switch(curChar)
            {
            case 108:
                if ((active0 & 0x400000L) != 0L)
                    return jjStartNfaWithStates_17(2, 22, 5);
                break;
            default :
                break;
            }
        return jjStartNfa_17(1, active0);
    }
    private final int jjMoveNfa_17(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 6;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0x7ff7f10fffff9ffL & l) != 0L)
                                        {
                                            if (kind > 26)
                                                kind = 26;
                                            jjCheckNAdd(5);
                                        }
                                    else if ((0x780080e600000000L & l) != 0L)
                                        {
                                            if (kind > 25)
                                                kind = 25;
                                            jjCheckNAdd(4);
                                        }
                                    if ((0x100002600L & l) != 0L)
                                        {
                                            if (kind > 23)
                                                kind = 23;
                                            jjCheckNAdd(0);
                                        }
                                    break;
                                case 0:
                                    if ((0x100002600L & l) == 0L)
                                        break;
                                    if (kind > 23)
                                        kind = 23;
                                    jjCheckNAdd(0);
                                    break;
                                case 4:
                                    if ((0x780080e600000000L & l) == 0L)
                                        break;
                                    kind = 25;
                                    jjCheckNAdd(4);
                                    break;
                                case 5:
                                    if ((0x7ff7f10fffff9ffL & l) == 0L)
                                        break;
                                    if (kind > 26)
                                        kind = 26;
                                    jjCheckNAdd(5);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                    if ((0xffffffffd7ffffffL & l) != 0L)
                                        {
                                            if (kind > 26)
                                                kind = 26;
                                            jjCheckNAdd(5);
                                        }
                                    else if ((0x28000000L & l) != 0L)
                                        {
                                            if (kind > 25)
                                                kind = 25;
                                            jjCheckNAdd(4);
                                        }
                                    if (curChar == 88)
                                        jjstateSet[jjnewStateCnt++] = 2;
                                    break;
                                case 1:
                                    if (curChar == 76 && kind > 25)
                                        kind = 25;
                                    break;
                                case 2:
                                    if (curChar == 77)
                                        jjstateSet[jjnewStateCnt++] = 1;
                                    break;
                                case 4:
                                    if ((0x28000000L & l) == 0L)
                                        break;
                                    kind = 25;
                                    jjCheckNAdd(4);
                                    break;
                                case 5:
                                    if ((0xffffffffd7ffffffL & l) == 0L)
                                        break;
                                    if (kind > 26)
                                        kind = 26;
                                    jjCheckNAdd(5);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 3:
                                case 5:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 26)
                                        kind = 26;
                                    jjCheckNAdd(5);
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 6 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    private final int jjStopStringLiteralDfa_4(int pos, long active0, long active1)
    {
        switch (pos)
            {
            default :
                return -1;
            }
    }
    private final int jjStartNfa_4(int pos, long active0, long active1)
    {
        return jjMoveNfa_4(jjStopStringLiteralDfa_4(pos, active0, active1), pos + 1);
    }
    private final int jjStartNfaWithStates_4(int pos, int kind, int state)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { return pos + 1; }
        return jjMoveNfa_4(state, pos + 1);
    }
    private final int jjMoveStringLiteralDfa0_4()
    {
        switch(curChar)
            {
            case 34:
                return jjStopAtPos(0, 104);
            case 38:
                return jjStopAtPos(0, 89);
            default :
                return jjMoveNfa_4(0, 0);
            }
    }
    private final int jjMoveNfa_4(int startState, int curPos)
    {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 1;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
            {
                if (++jjround == 0x7fffffff)
                    ReInitRounds();
                if (curChar < 64)
                    {
                        long l = 1L << curChar;
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    if ((0xffffffbbffffffffL & l) == 0L)
                                        break;
                                    kind = 103;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else if (curChar < 128)
                    {
                        long l = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    kind = 103;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                else
                    {
                        int hiByte = (int)(curChar >> 8);
                        int i1 = hiByte >> 6;
                        long l1 = 1L << (hiByte & 077);
                        int i2 = (curChar & 0xff) >> 6;
                        long l2 = 1L << (curChar & 077);
                    MatchLoop: do
                        {
                            switch(jjstateSet[--i])
                                {
                                case 0:
                                    if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                                        break;
                                    if (kind > 103)
                                        kind = 103;
                                    jjstateSet[jjnewStateCnt++] = 0;
                                    break;
                                default : break;
                                }
                        } while(i != startsAt);
                    }
                if (kind != 0x7fffffff)
                    {
                        jjmatchedKind = kind;
                        jjmatchedPos = curPos;
                        kind = 0x7fffffff;
                    }
                ++curPos;
                if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
                    return curPos;
                try { curChar = input_stream.readChar(); }
                catch(java.io.IOException e) { return curPos; }
            }
    }
    static final int[] jjnextStates = {
        0, 1, 5, 8, 9, 10, 14, 15, 9, 10, 14, 15, 
    };
    private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
    {
        switch(hiByte)
            {
            case 0:
                return ((jjbitVec2[i2] & l2) != 0L);
            default : 
                if ((jjbitVec0[i1] & l1) != 0L)
                    return true;
                return false;
            }
    }
    public static final String[] jjstrLiteralImages = {
        "", null, null, null, null, null, "\135", "\74", "\74\41", "\74\77", // NOI18N
        "\74\41\133\103\104\101\124\101\133", "\135\135\76", "\135\76", null, "\74\74", null, null, null, null, null, "\75", // NOI18N
        null, "\170\155\154", null, "\77\76", null, null, "\77\76", null, null, null, null, // NOI18N
        null, "\77\76", "\77", "\135\135\76", null, null, null, "\105\116\124\111\124\131", // NOI18N
        "\101\124\124\114\111\123\124", "\104\117\103\124\131\120\105", "\105\114\105\115\105\116\124", // NOI18N
        "\116\117\124\101\124\111\117\116", null, null, null, "\133", "\76", null, null, "\76", "\105\115\120\124\131", // NOI18N
        "\43\120\103\104\101\124\101", "\101\116\131", null, "\76", null, null, "\74", "\76", // NOI18N
        "\111\116\103\114\125\104\105", "\111\107\116\117\122\105", null, null, "\133", null, // NOI18N
        "\43\122\105\121\125\111\122\105\104", "\43\111\115\120\114\111\105\104", "\43\106\111\130\105\104", "\111\104", // NOI18N
        "\103\104\101\124\101", "\111\104\122\105\106", "\111\104\122\105\106\123", // NOI18N
        "\105\116\124\111\124\131", "\105\116\124\111\124\111\105\123", "\116\115\124\117\113\105\116", // NOI18N
        "\116\115\124\117\113\105\116\123", "\116\117\124\101\124\111\117\116", null, "\76", "\120\125\102\114\111\103", // NOI18N
        "\123\131\123\124\105\115", null, null, null, "\45", null, null, "\46", null, null, null, "\47", null, // NOI18N
        "\47", "\47", null, "\47", "\42", null, "\42", "\42", null, "\42", "\74\41\55\55", // NOI18N
        null, null, "\55\55\76", }; // NOI18N
    public static final String[] lexStateNames = {
        "IN_COMMENT", // NOI18N
        "IN_STRING", // NOI18N
        "IN_CHARS", // NOI18N
        "IN_GREF", // NOI18N
        "IN_GREF_STRING", // NOI18N
        "IN_GREF_CHARS", // NOI18N
        "IN_PREF", // NOI18N
        "IN_DOCTYPE", // NOI18N
        "IN_ATTLIST_DECL", // NOI18N
        "IN_COND", // NOI18N
        "IN_NOTATION", // NOI18N
        "IN_ELEMENT", // NOI18N
        "IN_ENTITY_DECL", // NOI18N
        "IN_DECL", // NOI18N
        "IN_CDATA", // NOI18N
        "IN_XML_DECL", // NOI18N
        "IN_PI_CONTENT", // NOI18N
        "IN_PI", // NOI18N
        "IN_TAG_ATTLIST", // NOI18N
        "IN_TAG", // NOI18N
        "DEFAULT", // NOI18N
    };
    public static final int[] jjnewLexState = {
        -1, -1, -1, -1, -1, -1, -1, 19, 13, 17, 14, -1, -1, -1, -1, 18, -1, -1, -1, -1, -1, 20, 15, 16, 20, 
        -1, -1, 20, -1, -1, -1, -1, -1, 20, -1, 20, -1, -1, -1, 12, 8, 7, 11, 10, -1, -1, -1, 9, 20, -1, 
        -1, 20, -1, -1, -1, -1, 20, -1, -1, -1, 20, -1, -1, -1, -1, 20, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        -1, -1, -1, -1, -1, 20, -1, -1, -1, -1, 20, 6, -1, -1, 3, -1, -1, -1, 2, -1, -1, 5, -1, -1, 1, 
        -1, -1, 4, -1, -1, 0, -1, -1, -1, 
    };
    private UCode_CharStream input_stream;
    private final int[] jjrounds = new int[27];
    private final int[] jjstateSet = new int[54];
    StringBuffer image;
    int jjimageLen;
    int lengthOfMatch;
    protected char curChar;
    public XMLSyntaxTokenManager(UCode_CharStream stream)
    {
        if (UCode_CharStream.staticFlag)
            throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer."); // NOI18N
        input_stream = stream;
    }
    public XMLSyntaxTokenManager(UCode_CharStream stream, int lexState)
    {
        this(stream);
        SwitchTo(lexState);
    }
    public void ReInit(UCode_CharStream stream)
    {
        jjmatchedPos = jjnewStateCnt = 0;
        curLexState = defaultLexState;
        input_stream = stream;
        ReInitRounds();
    }
    private final void ReInitRounds()
    {
        int i;
        jjround = 0x80000001;
        for (i = 27; i-- > 0;)
            jjrounds[i] = 0x80000000;
    }
    public void ReInit(UCode_CharStream stream, int lexState)
    {
        ReInit(stream);
        SwitchTo(lexState);
    }
    public void SwitchTo(int lexState)
    {
        if (lexState >= 21 || lexState < 0)
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE); // NOI18N
        else
            curLexState = lexState;
    }

    private final Token jjFillToken()
    {
        Token t = Token.newToken(jjmatchedKind);
        t.kind = jjmatchedKind;
        String im = jjstrLiteralImages[jjmatchedKind];
        t.image = (im == null) ? input_stream.GetImage() : im;
        t.beginLine = input_stream.getBeginLine();
        t.beginColumn = input_stream.getBeginColumn();
        t.endLine = input_stream.getEndLine();
        t.endColumn = input_stream.getEndColumn();
        return t;
    }

    int curLexState = 20;
    int defaultLexState = 20;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;

    public final Token getNextToken() 
    {
        int kind;
        Token specialToken = null;
        Token matchedToken;
        int curPos = 0;

        EOFLoop :
            for (;;)
                {   
                    try   
                        {     
                            curChar = input_stream.BeginToken();
                        }     
                    catch(java.io.IOException e)
                        {        
                            jjmatchedKind = 0;
                            matchedToken = jjFillToken();
                            return matchedToken;
                        }
                    image = null;
                    jjimageLen = 0;

                    switch(curLexState)
                        {
                        case 0:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_0();
                            break;
                        case 1:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_1();
                            break;
                        case 2:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_2();
                            break;
                        case 3:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_3();
                            break;
                        case 4:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_4();
                            break;
                        case 5:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_5();
                            break;
                        case 6:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_6();
                            break;
                        case 7:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_7();
                            break;
                        case 8:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_8();
                            break;
                        case 9:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_9();
                            break;
                        case 10:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_10();
                            break;
                        case 11:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_11();
                            break;
                        case 12:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_12();
                            break;
                        case 13:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_13();
                            break;
                        case 14:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_14();
                            break;
                        case 15:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_15();
                            break;
                        case 16:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_16();
                            break;
                        case 17:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_17();
                            break;
                        case 18:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_18();
                            break;
                        case 19:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_19();
                            break;
                        case 20:
                            jjmatchedKind = 0x7fffffff;
                            jjmatchedPos = 0;
                            curPos = jjMoveStringLiteralDfa0_20();
                            break;
                        }
                    if (jjmatchedKind != 0x7fffffff)
                        {
                            if (jjmatchedPos + 1 < curPos)
                                input_stream.backup(curPos - jjmatchedPos - 1);
                            matchedToken = jjFillToken();
                            TokenLexicalActions(matchedToken);
                            if (jjnewLexState[jjmatchedKind] != -1)
                                curLexState = jjnewLexState[jjmatchedKind];
                            return matchedToken;
                        }
                    int error_line = input_stream.getEndLine();
                    int error_column = input_stream.getEndColumn();
                    String error_after = null;
                    boolean EOFSeen = false;
                    try { input_stream.readChar(); input_stream.backup(1); }
                    catch (java.io.IOException e1) {
                        EOFSeen = true;
                        error_after = curPos <= 1 ? "" : input_stream.GetImage(); // NOI18N
                        if (curChar == '\n' || curChar == '\r') {
                            error_line++;
                            error_column = 0;
                        }
                        else
                            error_column++;
                    }
                    if (!EOFSeen) {
                        input_stream.backup(1);
                        error_after = curPos <= 1 ? "" : input_stream.GetImage(); // NOI18N
                    }
                    throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
                }
    }

    final void TokenLexicalActions(Token matchedToken)
    {
        switch(jjmatchedKind)
            {
            case 86 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[86]);
                else
                    image.append(jjstrLiteralImages[86]);
                pushState(getState());
                break;
            case 88 :
                if (image == null)
                    image = new StringBuffer(new String(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1))));
                else
                    image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                setState(popState());
                break;
            case 89 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[89]);
                else
                    image.append(jjstrLiteralImages[89]);
                pushState(getState());
                break;
            case 91 :
                if (image == null)
                    image = new StringBuffer(new String(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1))));
                else
                    image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                setState(popState());
                break;
            case 92 :
                if (image == null)
                    image = new StringBuffer(new String(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1))));
                else
                    image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                setState(popState());
                break;
            case 93 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[93]);
                else
                    image.append(jjstrLiteralImages[93]);
                pushState(getState());
                break;
            case 95 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[95]);
                else
                    image.append(jjstrLiteralImages[95]);
                setState(popState());
                break;
            case 96 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[96]);
                else
                    image.append(jjstrLiteralImages[96]);
                pushState(getState());
                break;
            case 98 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[98]);
                else
                    image.append(jjstrLiteralImages[98]);
                setState(popState());
                break;
            case 99 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[99]);
                else
                    image.append(jjstrLiteralImages[99]);
                pushState(getState());
                break;
            case 101 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[101]);
                else
                    image.append(jjstrLiteralImages[101]);
                setState(popState());
                break;
            case 102 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[102]);
                else
                    image.append(jjstrLiteralImages[102]);
                pushState(getState());
                break;
            case 104 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[104]);
                else
                    image.append(jjstrLiteralImages[104]);
                setState(popState());
                break;
            case 105 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[105]);
                else
                    image.append(jjstrLiteralImages[105]);
                pushState(getState());
                break;
            case 108 :
                if (image == null)
                    image = new StringBuffer(jjstrLiteralImages[108]);
                else
                    image.append(jjstrLiteralImages[108]);
                setState(popState());
                break;
            default : 
                break;
            }
    }
}
