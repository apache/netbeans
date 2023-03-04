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


package org.netbeans.modules.i18n;


import javax.swing.text.Position;


/**
 * Object representing found hard coded string in internationalized document.
 * @author  Peter Zavadsky
 */
public class HardCodedString extends Object {

    /** Actual text representing hard coded string. */
    private String text;

    /** Start position of hard coded string. */
    private Position startPosition;

    /** End position of hard coded string. */
    private Position endPosition;


    /** Creates new <code>HardCodedString</code>. */
    public HardCodedString(String text, Position startPosition, Position endPosition) {
        this.text = text;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    

    /** Getter for hard coded value. Text without double quotes. */
    public String getText() {
        return text;
    }

    /** Getter for start position.  */
    public Position getStartPosition() {
        return startPosition;
    }

    /** Getter for end position. */
    public Position getEndPosition() {
        return endPosition;
    }
    
    /** Gets length of hard coded string double quotes included. */
    public int getLength() {
        return endPosition.getOffset() - startPosition.getOffset();
    }
}
