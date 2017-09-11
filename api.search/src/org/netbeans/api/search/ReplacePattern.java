/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
