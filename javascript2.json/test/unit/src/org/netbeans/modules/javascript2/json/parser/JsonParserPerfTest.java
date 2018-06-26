/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.json.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class JsonParserPerfTest extends NbTestCase {

    public JsonParserPerfTest(@NonNull final String name) {
        super(name);
    }

    public void testLexerPerf() {
        final String data = createData(10_000);
        final ANTLRInputStream in = new ANTLRInputStream(data);
        final JsonLexer lex = new JsonLexer(in);
        final long st = System.nanoTime();
        final long count = lex.getAllTokens().size();
        final long et = System.nanoTime();
        System.out.printf("Lexing %d tokens in %dms.%n", count, (et-st)/1_000_000);
    }

 public void testParserPerf() {
        final String data = createData(10_000);
        final ANTLRInputStream in = new ANTLRInputStream(data);
        final JsonLexer lex = new JsonLexer(in);
        final CommonTokenStream tokens = new CommonTokenStream(lex);
        final JsonParser parser = new JsonParser(tokens);
        final long st = System.nanoTime();
        parser.json();
        final long et = System.nanoTime();
        System.out.printf("Parsing in %dms.%n", (et-st)/1_000_000);
    }



    private String createData(final int pairCount) {
        final StringBuilder result = new StringBuilder();
        result.append("{\n");       //NOI18N
        result.append("\"pairs\":[\n"); //NOI18N
        for (int i=0; i<pairCount; i++) {
            result.append("{\n");   //NOI18N
            result.append("\"key\":"+i+",\n");    //NOI18N
            result.append("\"value\":\""+Integer.toBinaryString(i)+"\"\n");   //NOI18N
            result.append('}');     //NOI18N
            if (i+1 < pairCount) {
                result.append(','); //NOI18N
            }
            result.append('\n');    //NOI18N
        }
        result.append("]\n");       //NOI18N
        result.append("}\n");       //NOI18N
        return result.toString();
    }

}
