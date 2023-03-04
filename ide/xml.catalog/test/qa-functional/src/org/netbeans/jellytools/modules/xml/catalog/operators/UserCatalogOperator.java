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
/*
 * UserCatalogOperator.java
 *
 * Created on September 19, 2006, 2:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.jellytools.modules.xml.catalog.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author jindra
 */
public class UserCatalogOperator extends NbDialogOperator {
    
    private JTextFieldOperator _txtPublic;
    private JTextFieldOperator _txtUri;
    private JButtonOperator _btBrowse;
    private JRadioButtonOperator _rbPublic;
    
    /** Creates a new instance of UserCatalogOperator */
    public UserCatalogOperator() {
        super("Register");
        
    }
    
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }
    
    public JRadioButtonOperator rbPublic() {
        if (_rbPublic==null) {
            _rbPublic = new JRadioButtonOperator(this, "Public ID:");
        }
        return _rbPublic;
    }

    public JTextFieldOperator txtPublic() {
        if (_txtPublic==null) {
            _txtPublic = new JTextFieldOperator(this, "");
        }
        return _txtPublic;
    }

    public JTextFieldOperator txtUri() {
        if (_txtUri==null) {
            _txtUri = new JTextFieldOperator(this, 2);
        }
        return _txtUri;
    }
    
}
