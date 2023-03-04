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
