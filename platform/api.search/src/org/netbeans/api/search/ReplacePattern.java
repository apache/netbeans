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
package org.netbeans.api.search;

/**
 * Pattern describes the replace conditions
 * ReplacePattern is a wrap class for replace expression.
 * @since 1.8
 */
public final class ReplacePattern {

    private String replaceExpression;
    private boolean preserveCase;
    
    private ReplacePattern(String replaceExpression, boolean preserveCase) {
        this.replaceExpression = replaceExpression;
        this.preserveCase = preserveCase;
    }

    /**
     * Creates a new ReplacePattern in accordance with given parameters
     *
     * @param replaceExpression non-null String of a replace expression
     * @param preserveCase if true, the case of original text will be preserved
     */
    public static ReplacePattern create(String replaceExpression, boolean preserveCase) {
        return new ReplacePattern(replaceExpression, preserveCase);
    }
    
    public String getReplaceExpression() {
        return replaceExpression;
    }

    public boolean isPreserveCase() {
        return preserveCase;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReplacePattern)) {
            return false;
        }
        ReplacePattern sp = (ReplacePattern) obj;
        return (this.replaceExpression.equals(sp.getReplaceExpression())
                && this.preserveCase == sp.isPreserveCase());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + (this.preserveCase ? 1 : 0);
        result = 37 * result + this.replaceExpression.hashCode();
        return result;
    }

    /**
     * Create new instance with "replace expression" set to passed value, and
     * other values copied from this instance.
     *
     */
    public ReplacePattern changeReplaceExpression(String expression) {
        if ((expression == null && this.replaceExpression == null)
                || (expression != null
                && expression.equals(this.replaceExpression))) {
            return this;
        } else {
            return ReplacePattern.create(expression, preserveCase);
        }
    }

    /**
     * Create new instance with "preserve case" set to passed value, and other
     * values copied from this instance.
     *
     */
    public ReplacePattern changePreserveCase(boolean preserveCase) {
        if (this.preserveCase == preserveCase) {
            return this;
        } else {
            return ReplacePattern.create(replaceExpression, preserveCase);
        }
    }
    
    String toCanonicalString() {
        char p = isPreserveCase()? 'P' : 'p';
        return "" + p + "-" + getReplaceExpression(); //NOI18N
    }

    static ReplacePattern parsePattern(String canonicalString) {
        //format p-replaceWith
        if (canonicalString == null
                || Character.toUpperCase(canonicalString.charAt(0)) != 'P'
                || canonicalString.charAt(1) != '-') {
            return null;
        }
        
        boolean preserveCase = Character.isUpperCase(canonicalString.charAt(0));
        String replaceWith = canonicalString.substring(2);
        return new ReplacePattern(replaceWith, preserveCase);
    }

}
