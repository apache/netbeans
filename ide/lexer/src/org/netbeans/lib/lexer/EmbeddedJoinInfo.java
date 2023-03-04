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

package org.netbeans.lib.lexer;

import org.netbeans.api.lexer.TokenId;

/**
 * Class that wraps a each embedded token list contained in join token list.
 * 
 * @author Miloslav Metelka
 */

public final class EmbeddedJoinInfo<T extends TokenId> {
    
    public EmbeddedJoinInfo(JoinTokenList<T> joinTokenList, int rawJoinTokenIndex, int rawTokenListIndex) {
        assert (joinTokenList != null);
        this.joinTokenList = joinTokenList;
        this.rawJoinTokenIndex = rawJoinTokenIndex;
        this.rawTokenListIndex = rawTokenListIndex;
    }
    
    /**
     * Reference to join token list can be conveniently used by ETL which hosts 
     * In fact this is the only field through which the join token list base instance
     * is referenced.
     */
    public final JoinTokenList<T> joinTokenList; // 12 bytes (8-super + 4)

    /**
     * Index in terms of join token list
     * that corresponds to first token of wrapped ETL.
     * <br/>
     * A join token is counted into an ETL that contains its last part.
     * For example: ETL1, ETL2, ETL3 with JT starting in ETL1 and ending in ETL3
     * then rJTI will be: ETL1:0 ETL2:0 
     * <br/>
     * The index must be gap-preprocessed.
     */
    private int rawJoinTokenIndex; // 16 bytes

    /**
     * Index of related ETL in a join token list (base).
     * <br/>
     * The index must be gap-preprocessed.
     */
    private int rawTokenListIndex; // 20 bytes

    /**
     * In case this ETL holds a PartToken at (tokenCount - 1) index then
     * this fields determines a number of ETLs to go forward to reach
     * a last part of a join token (being a first token
     * of that ETL).
     * For example if this is an ETL1 holding EJI1 having jTLPS == 2 then
     * it means that the ETL1.token(ETL1.tokenCount - 1) is a partToken1
     * and partToken1.joinToken() starts in ETL1 and it spans whole ETL2
     * and some initial portion of ETL3.
     * <br/>
     * If partToken1.joinToken() join index is e.g. == 1 then
     * ETL2.EJI2.joinTokenIndex == 1 and ETL3.EJI3.joinTokenIndex == 1.
     * The join token is counted into its last ETL.
     * 
     * <p>
     * Zero value means that there is no join token at the end of ETL
     * (last token of ETL ends with the last character of the ETL).
     * </p>
     */
    private int joinTokenLastPartShift; // 24 bytes

    public int joinTokenIndex() {
        return joinTokenList.joinTokenIndex(rawJoinTokenIndex);
    }

    public int getRawJoinTokenIndex() {
        return rawJoinTokenIndex;
    }

    public void setRawJoinTokenIndex(int rawJoinTokenIndex) {
        this.rawJoinTokenIndex = rawJoinTokenIndex;
    }

    public int tokenListIndex() {
        return joinTokenList.tokenListIndex(rawTokenListIndex);
    }

    public int getRawTokenListIndex() {
        return rawTokenListIndex;
    }

    public void setRawTokenListIndex(int rawTokenListIndex) {
        this.rawTokenListIndex = rawTokenListIndex;
    }

    public void setJoinTokenLastPartShift(int joinTokenLastPartShift) {
        this.joinTokenLastPartShift = joinTokenLastPartShift;
    }

    public int joinTokenLastPartShift() {
        return joinTokenLastPartShift;
    }

    public StringBuilder dumpInfo(StringBuilder sb, EmbeddedTokenList<?,?> etl) {
        if (sb == null)
            sb = new StringBuilder(70);
        sb.append("<").append(joinTokenIndex()).append("(R").append(rawJoinTokenIndex).append("),"); // NOI18N
        if (etl != null) {
            sb.append(joinTokenIndex() + etl.joinTokenCount());
        } else {
            sb.append("?");
        }
        sb.append(">, tli=").append(tokenListIndex()).append("(R").append(rawTokenListIndex);
        sb.append("), lps=").append(joinTokenLastPartShift());
        return sb;
    }

    @Override
    public String toString() {
        return dumpInfo(null, null).toString();
    }

}
