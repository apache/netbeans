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
package org.netbeans.tax.decl.parser;

import java.io.*;

import org.netbeans.tax.*;

/**
 * A simple kind of reader with some enhanced methods
 *  suitable for parsing purposes.
 */
public class ParserReader extends PushbackReader {

    /** */
    //      private TreeElementDecl elementDecl;


    //
    // init
    //

    public ParserReader (String source) {
        super (new StringReader (source), 20);

        //          this.elementDecl = elementDecl;
    }


    //
    // itself
    //

    //      /**
    //       */
    //      public final TreeElementDecl getRoot () {
    //          return elementDecl;
    //      }
    
    /** Trim out starting whitespaces. */
    public ParserReader trim () {
        int ch;
        while ( true ) {
            // read until non WS or EOF
            try {
                ch = read ();
                if (ch == -1 || ! Character.isWhitespace ((char)ch))
                    break;
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
        
        try {
            if (ch != -1) unread (ch);
        } catch (IOException ex) {
            ex.printStackTrace ();
        }
        
        return this;
    }
    
    /** SE: Move on if true with otherwise push back.
     * @return true if prefix is at the beginig of the stream
     */
    public boolean startsWith (String prefix) {
        char buf[] = new char[prefix.length ()];
        try {
            read (buf);
            //                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("startsWith(" + prefix + " got " + new String(buf)); // NOI18N
            boolean ret = new String (buf).equals (prefix);
            if (ret) return true;
        } catch (IOException ex) {
            ex.printStackTrace ();
            return false;
        }
        
        try {
            unread (buf);
        } catch (IOException ex) {
            ex.printStackTrace ();
        }
        return false;
    }
    
    /** @return next character or -1 */
    public int peek () {
        try {
            int ch = read ();
            unread (ch);
            return ch;
        } catch (IOException ex) {
            return -1;
        }
    }
    
    /** @return whitespace or "()?+*" separated token or "". */ // NOI18N
    public String getToken () {
        StringBuffer sb = new StringBuffer ();
        
        int ch = -1;
        
        trim ();
        
        boolean reading = true; //is a char in reading buffer
        int len = 0;
        
        try {
            readChars:
                while (reading) {  //read until token recognized
                    ch = read ();
                    if ( ch == -1 || Character.isWhitespace ((char)ch) )
                        break;
                    switch (ch) {
                        //do not eat interesting chars
                        case ')': case '(': case '?': case '+': case '*':
                            break readChars;
                            
                            //these are tokens alone
                        case ',': case '|':
                            if (len == 0) {
                                reading = false;  //  finnish, no unread
                            } else {
                                break readChars;  // finnish current token
                            }
                    }
                    
                    sb.append ((char)ch);
                    len++;
                }
                if (ch != -1 && reading) unread (ch);
        } catch (IOException ex) {
            // return most of recognized
        }
        
        String toret = sb.toString ();
        //            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Token: " + toret); // NOI18N
        return toret;
    }
}
