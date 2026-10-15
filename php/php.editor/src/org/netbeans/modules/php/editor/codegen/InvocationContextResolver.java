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
package org.netbeans.modules.php.editor.codegen;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class InvocationContextResolver {

    private static final Logger LOGGER = Logger.getLogger(InvocationContextResolver.class.getName());
    private List<ASTNode> nodes = new LinkedList<>();
    private final JTextComponent component;

    public static InvocationContextResolver create(final JTextComponent component) {
        InvocationContextResolver invocationContextResolver = new InvocationContextResolver(component);
        invocationContextResolver.resolveNodesUnderCarret();
        return invocationContextResolver;
    }

    private InvocationContextResolver(final JTextComponent component) {
        this.component = component;
    }

    private void resolveNodesUnderCarret() {
        try {
            ParserManager.parse(Collections.singleton(Source.create(component.getDocument())), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ParserResult info = (ParserResult) resultIterator.getParserResult();
                    if (info != null) {
                        int caretOffset = component.getCaretPosition();
                        nodes = NavUtils.underCaret(info, caretOffset);
                    }
                }
            });
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Error during resolving nodes under carret (codegens): {0}", ex.getMessage());
        }
    }

    public boolean isExactlyIn(InvocationContext context) {
        boolean result = false;
        if (!nodes.isEmpty()) {
            ASTNode lastNode = nodes.get(nodes.size() - 1);
            if (lastNode instanceof Block) {
                lastNode = nodes.get(nodes.size() - 2);
            }
            result = context.isExactlyIn(lastNode);
        }
        return result;
    }

    public enum InvocationContext {
        CLASS {
            @Override
            boolean isExactlyIn(ASTNode lastNode) {
                return lastNode instanceof ClassDeclaration
                        || lastNode instanceof TraitDeclaration
                        || lastNode instanceof InterfaceDeclaration
                        || lastNode instanceof EnumDeclaration
                        || (lastNode instanceof ClassInstanceCreation && ((ClassInstanceCreation) lastNode).isAnonymous());
            }
        },

        METHOD {
            @Override
            boolean isExactlyIn(ASTNode lastNode) {
                return lastNode instanceof MethodDeclaration;
            }
        },

        FUNCTION {
            @Override
            boolean isExactlyIn(ASTNode lastNode) {
                return lastNode instanceof FunctionDeclaration;
            }
        },

        NAMESPACE {
            @Override
            boolean isExactlyIn(ASTNode lastNode) {
                return lastNode instanceof NamespaceDeclaration;
            }
        },

        PROGRAM {
            @Override
            boolean isExactlyIn(ASTNode lastNode) {
                return lastNode instanceof Program;
            }
        },

        EMPTY_STATEMENT {
            @Override
            boolean isExactlyIn(ASTNode lastNode) {
                return lastNode instanceof EmptyStatement;
            }
        };

        abstract boolean isExactlyIn(ASTNode lastNode);
    }

}
