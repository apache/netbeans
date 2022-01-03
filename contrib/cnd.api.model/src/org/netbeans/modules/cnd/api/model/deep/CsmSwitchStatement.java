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

package org.netbeans.modules.cnd.api.model.deep;

import org.netbeans.modules.cnd.api.model.CsmScope;
import java.util.List;

/**
 * Represents switch statement;
 * getCodeBlock().getStatements() returns the list of the statements;
 *
 * TODO: perhaps it isn't worth to subclass CsmCompoundStatement and we'd better
 * add a separate member getStatements().
 *
 * TODO: perhaps we should provide some higher level of service
 * for determining the groups of statements for each case
 *
 */
public interface CsmSwitchStatement extends CsmStatement, CsmScope  {

    /** gets switch condition */
    CsmCondition getCondition();
    
    /** gets swithc body */
    CsmStatement getBody();
    
}
