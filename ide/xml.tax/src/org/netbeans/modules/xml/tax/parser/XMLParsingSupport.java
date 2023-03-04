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
package org.netbeans.modules.xml.tax.parser;

import java.io.IOException;

import org.xml.sax.InputSource;

import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeDocument;
import org.netbeans.tax.TreeDocumentRoot;

import org.netbeans.modules.xml.tax.parser.ParsingSupport;

/**
 *
 *
 * @author  Petr Kuzel
 * @version
 */
public class XMLParsingSupport extends ParsingSupport {

    /**
     * Parse XML document and return TreeDocument instance ot null.
     */
    public TreeDocumentRoot parse(InputSource in) throws IOException, TreeException {
//        if (url == null)
//	    url = getPrimaryFile().getURL();
        TreeStreamSource treeBuilder = new TreeStreamSource (TreeDocument.class, in, null);
        return (TreeDocument)treeBuilder.getBuilder().buildDocument();
        
    }
}
