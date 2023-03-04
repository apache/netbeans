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

package org.netbeans.modules.editor.lib2;

/** Mostly used acceptors
*
* @author Miloslav Metelka
* @version 1.00
*/
public class AcceptorFactory {

    public static final Acceptor TRUE = new Fixed(true);

    public static final Acceptor FALSE = new Fixed(false);

    public static final Acceptor NL = new Char('\n');

    public static final Acceptor SPACE_NL = new TwoChar(' ', '\n');

    public static final Acceptor WHITESPACE
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isWhitespace(ch);
          }
          public @Override String toString() {
            return "o.n.m.e.lib2.AcceptorFactory.WHITESPACE"; //NOI18N
          }
      };

    public static final Acceptor LETTER_DIGIT
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isLetterOrDigit(ch);
          }
          public @Override String toString() {
            return "o.n.m.e.lib2.AcceptorFactory.LETTER_DIGIT"; //NOI18N
          }
      };

    public static final Acceptor UNICODE_IDENTIFIER
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isUnicodeIdentifierPart(ch);
          }
          public @Override String toString() {
            return "o.n.m.e.lib2.AcceptorFactory.UNICODE_IDENTIFIER"; //NOI18N
          }
      };

    public static final Acceptor JAVA_IDENTIFIER
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isJavaIdentifierPart(ch);
          }
          public @Override String toString() {
            return "o.n.m.e.lib2.AcceptorFactory.JAVA_IDENTIFIER"; //NOI18N
          }
      };

    public static final Acceptor NON_JAVA_IDENTIFIER
    = new Acceptor() {
          public final boolean accept(char ch) {
              return !Character.isJavaIdentifierPart(ch);
          }
          public @Override String toString() {
            return "o.n.m.e.lib2.AcceptorFactory.NON_JAVA_IDENTIFIER"; //NOI18N
          }
      };

    private static final class Fixed implements Acceptor {
        private boolean state;

        public Fixed(boolean state) {
            this.state = state;
        }

        public final boolean accept(char ch) {
                  return state;
        }
        
        public @Override String toString() {
            return "o.n.m.e.lib2.AcceptorFactory.Fixed@" + Integer.toHexString(System.identityHashCode(this)) + " : state = " + state; //NOI18N
        }
    }

    private static final class Char implements Acceptor {
        private char hit;

        public Char(char hit) {
            this.hit = hit;
        }

        public final boolean accept(char ch) {
                  return ch == hit;
        }
        
        public @Override String toString() {
            return "o.n.m.e.lib2.AcceptorFactory.Char@" + Integer.toHexString(System.identityHashCode(this)) + " : hit = " + hit; //NOI18N
        }
    }

    private static final class TwoChar implements Acceptor {
        private char hit1, hit2;

        public TwoChar(char hit1, char hit2) {
            this.hit1 = hit1;
            this.hit2 = hit2;
        }

        public final boolean accept(char ch) {
                  return ch == hit1 || ch == hit2;
        }
        
        public @Override String toString() {
            return "o.n.m.e.lib2.AcceptorFactory.TwoChar@" + Integer.toHexString(System.identityHashCode(this)) + " : hit1 = " + hit1 + ", hit2 = " + hit2; //NOI18N
        }
    }
}
