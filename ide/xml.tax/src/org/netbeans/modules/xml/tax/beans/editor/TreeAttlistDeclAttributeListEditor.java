/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.tax.beans.editor;

import java.awt.Component;
import java.beans.PropertyEditorSupport;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeAttlistDeclAttributeListEditor extends PropertyEditorSupport {

    //
    // init
    //

    /** Creates new TreeAttlistDeclAttributeListEditor */
    public TreeAttlistDeclAttributeListEditor () {
    }


    //
    // itself
    //

    /**
     */
    @Override
    public void setAsText (String text) throws IllegalArgumentException {
      // can not be set as text
    }

    /**
     */
    @Override
    public boolean supportsCustomEditor () {
        return true;
    }

    /**
     */
    @Override
    public Component getCustomEditor () {
        TreeAttlistDeclAttributeListCustomizer comp = new TreeAttlistDeclAttributeListCustomizer();
        comp.setObject (getValue());

        return comp;
    }

    /**
     */
    @Override
    public boolean isPaintable () {
      return false;
    }

    /**
     */
    @Override
    public String getAsText () {
        return Util.THIS.getString ("NAME_pe_attributes");
    }

}
