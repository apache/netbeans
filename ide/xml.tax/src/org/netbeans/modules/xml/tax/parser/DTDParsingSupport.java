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
import org.netbeans.tax.TreeDTD;
import org.netbeans.tax.TreeDocumentRoot;

/**
 *
 * @author  Petr Kuzel
 * @version 0.9
 */
public class DTDParsingSupport extends ParsingSupport {

    /**
     * Parse DataObject returning TreeDTD or null (on parse failure).
     */
    public TreeDocumentRoot parse(InputSource in) throws IOException, TreeException {

        TreeStreamSource treeBuilder = new TreeStreamSource(TreeDTD.class, in, null);
        TreeDTD tree = (TreeDTD)treeBuilder.getBuilder().buildDocument();
        return tree;                
    }    
        
}
