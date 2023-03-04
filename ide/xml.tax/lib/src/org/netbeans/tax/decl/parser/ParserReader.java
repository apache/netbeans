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
