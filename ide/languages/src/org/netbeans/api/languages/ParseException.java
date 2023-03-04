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

package org.netbeans.api.languages;


/**
 * Parse Exception.
 *
 * @author Jan Jancura
 */
public class ParseException extends Exception {

    private ASTNode node;

    
    /**
     * Creates a new instance of ParseException.
     */
    public ParseException () {}
    
    /**
     * Creates a new instance of ParseException with given error message.
     * 
     * @param text a text of message
     */
    public ParseException (String text) {
        super (text);
    }
    
    /**
     * Creates a new instance of ParseException encapsulating some other Exception.
     * 
     * @param ex an internal exception
     */
    public ParseException (Exception ex) {
        super (ex);
        if (ex instanceof ParseException)
            node = ((ParseException) ex).getASTNode ();
    }
    
    /**
     * Creates a new instance of ParseException with given error message and AST tree.
     * 
     * @param text a text of message
     * @param root a root of AST tree
     */
    public ParseException (String text, ASTNode root) {
        super (text);
        this.node = root;
    }
    
    /**
     * Creates a new instance of ParseException with given exception and AST tree.
     * 
     * @param ex an internal exception
     * @param root a root of AST tree
     */
    public ParseException (Exception ex, ASTNode root) {
        super (ex);
        this.node = root;
    }

    /**
     * Returns root of AST tree.
     * 
     * @return a root of AST tree
     */
    public ASTNode getASTNode () {
        return node;
    }
}

