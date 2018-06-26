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
package org.netbeans.modules.php.latte.codetemplates;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.latte.completion.LatteCompletionContext;
import org.netbeans.modules.php.latte.completion.LatteCompletionContextFinder;
import org.netbeans.modules.php.latte.parser.LatteParserResult;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class LatteCodeTemplateFilter extends UserTask implements CodeTemplateFilter {
    private static final Logger LOGGER = Logger.getLogger(LatteCodeTemplateFilter.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(LatteCodeTemplateFilter.class);
    private volatile boolean accept = true;
    private final int offset;
    private final Future<Future<Void>> future;

    private LatteCodeTemplateFilter(final JTextComponent component, int offset) {
        this.offset = offset;
        future = RP.submit(new Callable<Future<Void>>() {

            @Override
            public Future<Void> call() throws Exception {
                try {
                    return parseDocument(component.getDocument());
                } catch (ParseException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
                return null;
            }
        });
    }

    private Future<Void> parseDocument(final Document document) throws ParseException {
        return ParserManager.parseWhenScanFinished(Collections.singleton(Source.create(document)), this);
    }

    @Override
    public boolean accept(CodeTemplate template) {
        try {
            future.get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        return accept;
    }

    @Override
    public void run(ResultIterator resultIterator) throws Exception {
        assert resultIterator != null;
        Parser.Result parserResult = resultIterator.getParserResult();
        if (parserResult instanceof LatteParserResult) {
            LatteCompletionContext context = LatteCompletionContextFinder.find((LatteParserResult) parserResult, offset);
            accept = (LatteCompletionContext.MACRO.equals(context) || LatteCompletionContext.ALL.equals(context));
        }
    }

    public static final class Factory implements CodeTemplateFilter.Factory {

        @Override
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new LatteCodeTemplateFilter(component, offset);
        }

    }

}
