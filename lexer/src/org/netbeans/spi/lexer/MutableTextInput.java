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

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;

/**
 * Mutable attributed character sequence allowing to listen for changes in its text.
 *
 * <p>
 * The input can temporarily be made inactive which leads to dropping
 * of all the present tokens for the input until the input becomes
 * active again.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class MutableTextInput<I> {

    private TokenHierarchyControl<I> thc;

    /**
     * Get the language suitable for lexing of this input.
     *
     * @return language language by which the text of this input should be lexed.
     *  <br/>
     *  This method is only checked upon creation of token hierarchy.
     *  <br/>
     *  If this method returns null the token hierarchy cannot be created
     *  and will be returned as null upon asking until this method will return
     *  non-null value.
     */
    protected abstract Language<?> language();

    /**
     * Get the character sequence provided and maintained by this input.
     */
    protected abstract CharSequence text();
    
    /**
     * Get lexer-specific information about this input
     * or null if there is no specific information.
     * <br>
     * The attributes are typically retrieved by the lexer.
     */
    protected abstract InputAttributes inputAttributes();
    
    /**
     * Get object that logically provides the text
     * for this mutable text input.
     * <br/>
     * For example it may be a swing text document instance
     * {@link javax.swing.text.Document} in case the result of {@link #text()}
     * is the content of the document.
     *
     * @return non-null mutable input source.
     */
    protected abstract I inputSource();

    /**
     * Called by infrastructure to check if the underlying input source is currently
     * read-locked by the current thread.
     * <br/>
     * This method is only called when turning on the logger for token hierarchy updates:
     * <code>-J-Dorg.netbeans.lib.lexer.TokenHierarchyUpdate.level=FINE</code>
     * <br/>
     * It is expected that if write-lock means read-lock too i.e. if {@link #isWriteLocked()}
     * returns true that this method will also return true automatically.
     * <br/>
     * The following operations require read-locking:
     * <ul>
     *   <li>Creation and using of token sequence {@link org.netbeans.api.lexer.TokenHierarchy#tokenSequence()}.</li>
     *   <li>Creation of token sequence list {@link org.netbeans.api.lexer.TokenHierarchy#tokenSequenceList(LanguagePath,int,int)}.</li>
     * </ul>
     * 
     * @return true if the underlying input source is read-locked by the current thread
     * (or if unsure e.g. if there is a read-lock present but it's not possible
     *  to determine whether it was this or other thread that performed the locking).
     *  <br/>
     *  Returning false means that the input source is surely unlocked which will be
     *  reported as a serious error by the infrastructure.
     */
    protected abstract boolean isReadLocked();
    
    /**
     * Called by infrastructure to check if the underlying input source is currently
     * write-locked by the current thread.
     * <br/>
     * This method is only called when turning on the logger for token hierarchy updates:
     * <code>-J-Dorg.netbeans.lib.lexer.TokenHierarchyUpdate.level=FINE</code>
     * <br/>
     * The following operations require write-locking:
     * <ul>
     *   <li>Text modification {@link org.netbeans.spi.lexer.TokenHierarchyControl#textModified(int,int,CharSequence,int)}</li>
     *   <li>Creation of custom embedding by {@link org.netbeans.api.lexer.TokenSequence#createEmbedding(Language,int,int)}.</li>
     *   <li>Removal of custom embedding by {@link org.netbeans.api.lexer.TokenSequence#removeEmbedding(Language)}.</li>
     * </ul>
     * 
     * @return true if the underlying input source is write-locked by the current thread
     * (or if unsure).
     *  Returning false means that the input source is surely unlocked which will be
     *  reported as a serious error by the infrastructure.
     */
    protected abstract boolean isWriteLocked();

    /**
     * Get token hierarchy control for this mutable text input.
     * <br>
     * Each mutable text input can hold it in a specific way
     * e.g. swing document can use
     * <code>getProperty(TokenHierarchyControl.class)</code>.
     */
    public final TokenHierarchyControl<I> tokenHierarchyControl() {
        synchronized (this) {
            if (thc == null) {
                thc = new TokenHierarchyControl<I>(this);
            }
            return thc;
        }
    }

}
