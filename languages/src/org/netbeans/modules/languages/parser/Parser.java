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

package org.netbeans.modules.languages.parser;

import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.ASTToken;
import java.util.*;
import org.netbeans.api.languages.Language;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Feature.Type;
import org.netbeans.modules.languages.TokenType;


/**
 *
 * @author Jan Jancura
 */
public class Parser {
    
    public static final String DEFAULT_STATE = "DEFAULT";

    private Parser () {
    }
    
    public static Parser create (List<TokenType> rules) {
        return new Parser (rules);
    }
    
    
    private Map<Integer,Pattern>        stateToPattern = new HashMap<Integer,Pattern> ();
    private Map<String,Integer>         nameToState = new HashMap<String,Integer> ();
    private Map<Integer,String>         stateToName = new HashMap<Integer,String> ();
    private List<TokenType>             tokenTypes;
    private int counter = 1;
    {
        nameToState.put (DEFAULT_STATE, -1);
        stateToName.put (-1, DEFAULT_STATE);
    }
    
    
    private Parser (List<TokenType> tokenTypes) {
        this.tokenTypes = tokenTypes;
        Iterator<TokenType> it = tokenTypes.iterator ();
        while (it.hasNext ()) {
            TokenType r = it.next ();
            add (r);
        }
    }
    
    public List<TokenType> getTokenTypes () {
        return tokenTypes;
    }
    
    private void add (TokenType tokenType) {
        if (tokenType.getPattern () == null) return;
        String startState = tokenType.getStartState ();
        if (startState == null) startState = DEFAULT_STATE;
        int state = 0;
        if (nameToState.containsKey (startState))
            state = nameToState.get (startState);
        else {
            state = counter++;
            nameToState.put (startState, state);
            stateToName.put (state, startState);
        }
        Pattern pattern = tokenType.getPattern (); 
        pattern.mark (tokenType.getPriority (), tokenType);
        if (stateToPattern.containsKey (state))
            stateToPattern.put (
                state,
                stateToPattern.get (state).merge (pattern)
            );
        else
            stateToPattern.put (state, pattern);
    }
    
    public ASTToken read (Cookie cookie, CharInput input, Language language) {
        if (input.eof ()) return null;
        int originalIndex = input.getIndex ();
        Pattern pattern = stateToPattern.get (cookie.getState ());
        if (pattern == null) return null;
        TokenType tokenType = (TokenType) pattern.read (input);
        if (tokenType == null) {
            return null;
        }
        Feature tokenProperties = tokenType.getProperties ();
        cookie.setProperties (tokenProperties);
        String endState = tokenType.getEndState ();
        int state = -1;
        if (endState != null)
            state = getState (endState);
        cookie.setState (state);
        ASTToken token = ASTToken.create (
            language,
            tokenType.getTypeID (),
            input.getString (originalIndex, input.getIndex ()),
            originalIndex
        );
        if (tokenProperties != null &&
            tokenProperties.getType ("call") != Type.NOT_SET
        ) {
            input.setIndex(originalIndex);
            Object[] r = (Object[]) tokenProperties.getValue ("call", new Object[] {input});
            if (r == null)
                throw new NullPointerException ("Method " + tokenProperties.getMethodName ("call") + " returns null!\n");
            token = (ASTToken) r [0];
            if (r [1] != null)
                cookie.setState (getState((String) r [1]));
        }
        return token;
    }
    
    public int getState (String stateName) {
        Integer i = nameToState.get (stateName);
        if (i == null)
            throw new IllegalArgumentException ("Unknown lexer state: " + stateName);
        return i.intValue ();
    } 
    
    private Map<String,Pattern> patterns = new HashMap<String,Pattern> ();
    
    
    public String toString () {
        StringBuffer sb = new StringBuffer ();
        Iterator<String> it = patterns.keySet ().iterator ();
        while (it.hasNext ()) {
            String state = it.next ();
            sb.append (state).append (":").append (patterns.get (state));
        }
        return sb.toString ();
    }
    
    
    // innerclasses ............................................................
    
    public interface Cookie {
        public abstract int getState ();
        public abstract void setState (int state);
        public abstract void setProperties (Feature tokenProperties);
    }
}
