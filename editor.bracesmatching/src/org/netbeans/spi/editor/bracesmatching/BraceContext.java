/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.spi.editor.bracesmatching;

import javax.swing.text.Position;

/**
 * Provides context information for a single brace. The brace may be preceded or followed
 * by a semantically connected text, such as `if' statement or `while' statement in a do-while
 * loop in Java. Such text may be displayed as a context for the brace highlight. The marked text
 * should include the brace sign itself.
 * <p/>
 * It may be necessary to provide a completely unrelated context, such is in the example
 * of if-else statement. The 'else' does not provide enough information itself as it is 
 * just a negation of earlier condition. Such related piece of text can be reported as 'related' 
 * BraceContext instance. Note that <i>related</b> instances may be chained - see the example below.
 * <p/>
 * The infrastructure uses the provided information to present the context to the user. If 
 * a 'related' positions are present the infrastructure will attempt to present the source
 * on the 'context' and 'related' positions in the source text order (e.g. displays portion of the document);
 * contents between 'context' and 'related' positions may be suppressed.
 * <p/>
 * An example of context and related areas for Java (PHP) if-elseif-else, with the origin t
 * <code><pre>
 *  //  vvvvvvvvvvvvvvvvv -- related
 *      if (condition1) {
 *  //  vvvvvvvvvvvvvvvvvvvvvvvvvvv -- related
 *      } else if (elsecondition) {
 *      ...
 *      } else {
 *  //    ^^^^^^ -- context
 *      ...
 *      }
 * //   ^ -- origin/caret location
 * </pre></code>
 * @author sdedic
 */
public final class BraceContext {
    /**
     * @return Starting position of the context text content
     */
    public Position getStart() {
        return start;
    }

    /**
     * @return Ending position of the context text content
     */
    public Position getEnd() {
        return end;
    }

    /**
     * @return the related range, or {@code null} for none.
     */
    public BraceContext getRelated() {
        return related;
    }

    /**
     * Creates a new BraceContext related to this one.
     * 
     * @param start start of the contextual text
     * @param end end of the text 
     * @return the new instance of BraceContext
     */
    public BraceContext createRelated(Position start, Position end) {
        return new BraceContext(start, end, this);
    }
    
    /**
     * Creates a new context area for the brace.
     * @param start start of the text
     * @param end end of the text
     * @return a new instance of BraceContext
     */
    public static BraceContext create(Position start, Position end) {
        return new BraceContext(start, end, null);
    }
    
    /**
     * Start of the related text
     */
    private final Position start;
    
    /**
     * End of the related text
     */
    private final Position end;
    
    /**
     * The related area(s)
     */
    private final BraceContext related;

    private BraceContext(Position openContextStart, Position closeContextEnd,
            BraceContext related) {
        this.start = openContextStart;
        this.end = closeContextEnd;
        this.related = related;
    }
}
