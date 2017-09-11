/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.editor;

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
            return "o.n.e.AcceptorFactory.WHITESPACE"; //NOI18N
          }
      };

    public static final Acceptor LETTER_DIGIT
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isLetterOrDigit(ch);
          }
          public @Override String toString() {
            return "o.n.e.AcceptorFactory.LETTER_DIGIT"; //NOI18N
          }
      };

    public static final Acceptor JAVA_IDENTIFIER
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isJavaIdentifierPart(ch);
          }
          public @Override String toString() {
            return "o.n.e.AcceptorFactory.JAVA_IDENTIFIER"; //NOI18N
          }
      };

    public static final Acceptor NON_JAVA_IDENTIFIER
    = new Acceptor() {
          public final boolean accept(char ch) {
              return !Character.isJavaIdentifierPart(ch);
          }
          public @Override String toString() {
            return "o.n.e.AcceptorFactory.NON_JAVA_IDENTIFIER"; //NOI18N
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
            return "o.n.e.AcceptorFactory.Fixed@" + Integer.toHexString(System.identityHashCode(this)) + " : state = " + state; //NOI18N
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
            return "o.n.e.AcceptorFactory.Char@" + Integer.toHexString(System.identityHashCode(this)) + " : hit = " + hit; //NOI18N
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
            return "o.n.e.AcceptorFactory.TwoChar@" + Integer.toHexString(System.identityHashCode(this)) + " : hit1 = " + hit1 + ", hit2 = " + hit2; //NOI18N
        }
    }


}
