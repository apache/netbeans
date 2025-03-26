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

package org.netbeans.modules.parsing.api;


/**
 * UserTask allows control process of parsing of {@link Source}
 * containing blocks of embedded languages, and do some computations based on 
 * all (or some) parser {@link org.netbeans.modules.parsing.spi.Parser.Result}s. 
 * It is useful when you need to implement code completion based on results of more
 * embedded languages, or if you want to implement refactoring of some 
 * blocks of code embedded in some other blocks of other code, etc...
 *
 * @author Jan Jancura
 */
public abstract class UserTask extends Task implements ResultProcessor {

}




