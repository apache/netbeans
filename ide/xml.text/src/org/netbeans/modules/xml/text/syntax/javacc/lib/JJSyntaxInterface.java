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
package org.netbeans.modules.xml.text.syntax.javacc.lib;

/**
 * Intergace provided by JavaCC grammars bridges.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface JJSyntaxInterface extends JJConstants {


    /** Initialize parser to initial state. */
    public void init(CharStream in);

    /** Initialize parser to particular state. */
    public void init(CharStream in, int state);

    /** Move parser to next state. */
    public void next();

    //query methods

    /** @return ID of the last recognized token. */
    public int getID();
    
    /** @return length of the last token. */
    public int getLength();
    
    /** @return token string representation. */
    public String getImage();
    
    //state persistence methods 
    
    /** @return last state */
    public int getState();
    
    /** Set last state. */
    public void setState(int state);
    
    /** @return last substates*/
    public int[] getStateInfo();
    
    /** Set last substates. */
    public void setStateInfo(int[] states);

}
