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
package org.netbeans.tax;

//  import org.netbeans.tax.grammar.Grammar; // will be ...

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
abstract class AbstractTreeDocument extends TreeParentNode {

    /** -- can be null. */
    //      private Grammar softGrammar; // will be ...


    //
    // init
    //

    /**
     * Creates new AbstractTreeDocument.
     */
    protected AbstractTreeDocument () {
        super ();
    }

    /** Creates new AbstractTreeDocument -- copy constructor. */
    protected AbstractTreeDocument (AbstractTreeDocument abstractDocument, boolean deep) {
        super (abstractDocument, deep);
        
        //  	this.softGrammar = abstractDocument.softGrammar; // will be ...
    }
    
    
    //
    // grammar // will be ...
    //
    
    //      /** Set soft grammar. Soft grammar is not directly used in document but is used for validation only.
    //       *  There are some document formats which are opened but has basic structure, e.g. ant, xsl.
    //       * @param grammar soft grammar
    //       */
    //      public void setSoftGrammar (Grammar grammar) {
    //  	softGrammar = grammar;
    //      }
    
    //      /** */
    //      public Grammar getSoftGrammar () {
    //  	return softGrammar;
    //      }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     *
     */
    protected abstract class ChildListContentManager extends TreeParentNode.ChildListContentManager {
        
    } // end: class ChildListContentManager
    
}
