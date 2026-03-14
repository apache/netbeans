/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.blade.editor;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.php.blade.editor.lexer.BladeTokenId;
import static org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.BLADE_ECHO_DELIMITOR;

/**
 * known issues 
 * currently blade tag comment not wokring inside htmtl tags <div>{{ $x }}</div>
 * 
 * 
 * @author bhaidu
 */
public class BladeCommentHandler extends CommentHandler.DefaultCommentHandler {

    public static final String COMMENT_START_DELIMITER = "{{--"; //NOI18N
    public static final String COMMENT_END_DELIMITER = "--}}"; //NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(BladeCommentHandler.class.getName());

    @Override
    public String getCommentStartDelimiter() {
        return COMMENT_START_DELIMITER;
    }

    @Override
    public String getCommentEndDelimiter() {
        return COMMENT_END_DELIMITER;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public int[] getAdjustedBlocks(final Document doc, int from, int to) {
        final int[] bounds = new int[]{from, to};

        Runnable task = new Runnable() {
            @Override
            @SuppressWarnings("rawtypes")
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<?> ts = th.tokenSequence();
                ts.move(from);
                ts.moveNext();

                Token<?> token = ts.token();

                if (token != null && token.id() instanceof BladeTokenId) {
                    //handle uncomment
                    switch ((BladeTokenId) token.id()) {
                        case BLADE_COMMENT_START -> {
                            bounds[0] = ts.offset();

                            while (ts.moveNext()) {
                                if (ts.token().id() == BladeTokenId.BLADE_COMMENT_END) {
                                    bounds[1] = ts.offset() + ts.token().length();
                                    break;
                                }
                            }
                        }
                        case BLADE_DIRECTIVE, BLADE_ECHO_DELIMITOR -> {
                            bounds[0] = ts.offset();

                            //looking for directive arguments bounds
                            if (ts.moveNext() && ts.token().id() == BladeTokenId.PHP_BLADE_EXPRESSION) {
                                bounds[1] =  ts.offset() + ts.token().length();
                            }
                            
                            try {
                                //manually inserting the delimiters
                                doc.insertString(bounds[0], COMMENT_START_DELIMITER, null);
                                doc.insertString(Math.max(bounds[1], to) + COMMENT_END_DELIMITER.length(), COMMENT_END_DELIMITER, null);
                                bounds[0] = 0;
                                bounds[1] = 0;
                            } catch (BadLocationException ex) {
                                LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested()); // NOI18N
                            }
                        }

                    }

                }

            }
        };

        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).runAtomic(task);
        } else {
            task.run();
        }

        return new int[]{bounds[0], bounds[1]};
    }
}
