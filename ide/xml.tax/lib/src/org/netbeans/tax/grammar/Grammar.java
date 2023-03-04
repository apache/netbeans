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
package org.netbeans.tax.grammar;

import org.netbeans.tax.TreeNode;
import org.netbeans.tax.TreeElement;
import org.netbeans.tax.TreeAttribute;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public interface Grammar {

    /** */
    //      public AbstractElemnentDecl[] definedElements ();

    /** */
    //      public AbstractAttributeDecl[] definedAttributes ();

    /** */
    //      public AbstractEntityDecl[] definedEntities ();

    /** */
    //      public AbstractNotationDecl[] definedNotations ();


    /** Create validator for grammar. */
    public Validator getValidator ();
    
    
    /** */
    public boolean isNamespaceAware ();
    
    
    /** Listen to grammar changes. */
    //    public void addPropertyChangeListener (PropertyChangeListener listener);
    
    /** */
    //    public void removePropertyChangeListener (PropertyChangeListener listener);
    
}
