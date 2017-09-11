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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Provides extra properties of a token.
 * <br/>
 * Normally each token has an extra instance of the property provider:
 * <pre>
 * final class MyTokenPropertyProvider implements TokenPropertyProvider {
 *
 *     private final Object value;
 *
 *     TokenPropProvider(Object value) {
 *         this.value = value;
 *     }
 *      
 *     public Object getValue (Token token, Object key) {
 *         if ("type".equals(key))
 *             return value;
 *         return null;
 *     }
 *
 * }
 * </pre>
 * <br/>
 * However multiple flyweight instances of the provider may be used to save memory
 * if there are just several values for a property.
 * <br/>
 * Example of two instances of a provider for boolean property "key":
 * <pre>
 * final class MyTokenPropertyProvider implements TokenPropertyProvider {
 *
 *     static final MyTokenPropertyProvider TRUE = new MyTokenPropertyProvider(Boolean.TRUE);
 *
 *     static final MyTokenPropertyProvider FALSE = new MyTokenPropertyProvider(Boolean.FALSE);
 * 
 *     private final Boolean value;
 *
 *     private MyTokenPropertyProvider(Boolean value) {
 *         this.value = value;
 *     }
 *
 *     public Object getValue(Token&lt;T&gt; token, Object key) {
 *         if ("key".equals(key)) {
 *             return value;
 *         }
 *         return null;
 *     }
 *
 * }
 * </pre>
 * <br/>
 * A special kind of token <code>PropertyToken</code> allows to carry token properties.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenPropertyProvider<T extends TokenId> {
    
    /**
     * Get value of a token property.
     *
     * @param token non-null token for which the property is being retrieved.
     *  It might be useful if the property would be computed dynamically.
     * @param key non-null key for which the value should be retrieved.
     * @return value of the property or null if there is no value for the given key.
     */
    Object getValue(Token<T> token, Object key);

}
