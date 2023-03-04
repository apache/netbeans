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
package org.netbeans.tax.io;

import java.io.StringWriter;

import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeNode;

/**
 * Converts any arbitrary node and its subnodes to their String representation.
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class XMLStringResult extends TreeStreamResult {

    //
    // init
    //

    /** Creates new XMLStringResult. */
    private XMLStringResult (StringWriter stringWriter) {
        super (stringWriter);
    }


    //
    // static utils
    //

    /**
     * @param node to be ddeply converted to its String representation.
     */
    public static final String toString (TreeNode node) throws TreeException {
        StringWriter stringWriter = new StringWriter ();
        XMLStringResult result = new XMLStringResult (stringWriter);
        TreeStreamResult.TreeStreamWriter writer =
        (TreeStreamResult.TreeStreamWriter)result.getWriter (null);
        
        writer.writeNode (node);
        return stringWriter.toString ();
    }
    
}
