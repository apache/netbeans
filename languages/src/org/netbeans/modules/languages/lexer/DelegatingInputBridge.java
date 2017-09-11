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

package org.netbeans.modules.languages.lexer;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.languages.CharInput;
import org.netbeans.modules.languages.lexer.SLexer.TokenProperties;
import org.netbeans.modules.languages.lexer.SLexer.Vojta;
import org.netbeans.modules.languages.parser.Pattern;


class DelegatingInputBridge extends CharInput {

    private InputBridge     input;
    private Pattern         start;
    private Pattern         end;
    private int             tokenType;
    private List<Vojta>     embeddings = new ArrayList<Vojta> ();

    DelegatingInputBridge (
        InputBridge         input, 
        Pattern             start,
        Pattern             end,
        int                 tokenType
    ) {
        this.input =        input;
        this.start =        start;
        this.end   =        end;
        this.tokenType =    tokenType;
    }

    public char read () {
        readEmbeddings ();
        return input.read ();
    }

    public void setIndex (int index) {
        input.setIndex (index);
    }

    public int getIndex () {
        return input.getIndex ();
    }

    public char next () {
        readEmbeddings ();
        return input.next ();
    }

    public boolean eof () {
        readEmbeddings ();
        return input.eof ();
    }

    public String getString (int from, int to) {
        return input.getString (from, to);
    }

    public String toString () {
        return input.toString ();
    }
    
    public List<Vojta> getEmbeddings () {
        List<Vojta> e = embeddings;
        embeddings = new ArrayList<Vojta> ();
        return e;
    }
    
    private void readEmbeddings () {
        int startIndex = input.getIndex ();
        if (!input.eof () && start.next (input) != null) {
            int startSkipLength = input.getIndex () - startIndex;
            int endSkipLength = input.getIndex ();
            while (!input.eof () && end.next (input) == null) {
                input.read ();
                endSkipLength = input.getIndex ();
            }
            endSkipLength = input.getIndex () - endSkipLength;
            embeddings.add (
                new Vojta (
                    tokenType,
                    startIndex,
                    input.getIndex (),
                    new TokenProperties (
                        SLexer.INJECTED_CODE,
                        startSkipLength,
                        endSkipLength
                    )
                )
            );
        }
    }
}


