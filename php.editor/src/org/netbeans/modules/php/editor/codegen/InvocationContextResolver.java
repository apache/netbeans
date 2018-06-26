/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
import org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;

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
                return lastNode instanceof ClassDeclaration;
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
