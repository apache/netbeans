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

package org.netbeans.modules.php.editor.api;

import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.elements.IndexQueryImpl;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.PhpElementVisitor;

/**
 * @author Radek Matous
 */
public final class ElementQueryFactory {

    private ElementQueryFactory() {
    }

    public static ElementQuery.Index createIndexQuery(final QuerySupport querySupport)  {
        return IndexQueryImpl.create(querySupport);
    }

    public static ElementQuery.Index getIndexQuery(final ParserResult parseResult)  {
        return (parseResult instanceof PHPParseResult) ? getIndexQuery((PHPParseResult) parseResult)
                : createIndexQuery(QuerySupportFactory.get(parseResult));
    }

    /**
     * shared, cached by model.
     */
    public static ElementQuery.Index getIndexQuery(final PHPParseResult parseResult)  {
        return IndexQueryImpl.getModelInstance(parseResult);
    }
    public static ElementQuery.File createFileQuery(final PHPParseResult parseResult)  {
        return PhpElementVisitor.createElementQuery(parseResult);
    }
}
