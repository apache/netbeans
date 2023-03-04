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
