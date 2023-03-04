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

import org.netbeans.editor.*;

/**
 * Maps token ID returned by jjgrammer bridge to JJTokenID.
 *
 * @author  Petr Kuzel
 * @version
 */
public interface JJMapperInterface extends JJConstants {


    /** @return token for particular ID. */
    public JJTokenID createToken(int id);

    /** @return token guessed for particular state. */
    public JJTokenID guessToken(String token, int state, boolean lastBuffer);

    /**
     * Called if  createToken(int id) return isError() token.
     * @return supposed token for particular id and state. 
     */
    public JJTokenID supposedToken(String token, int state, int id);
    
}
