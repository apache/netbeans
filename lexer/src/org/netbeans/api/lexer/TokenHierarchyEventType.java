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

package org.netbeans.api.lexer;

/**
 * Token hierarchy event type determines the reason
 * why token hierarchy modification described by {@link TokenHierarchyEvent}
 * happened.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public enum TokenHierarchyEventType {

    /**
     * Modification (insert/remove) of the characters
     * in the underlying character sequence was performed.
     */
    MODIFICATION,

    /**
     * Explicit relexing of a part of the token hierarchy
     * without any text modification.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     * <br/>
     * This is not actively used yet (no API support yet).
     */
    RELEX,

    /**
     * Complete rebuild of the token hierarchy.
     * <br/>
     * This may be necessary because of any changes
     * in input attributes that influence the lexing.
     * <br/>
     * Only the removed tokens will be notified.
     * There will be no added tokens because they will be created lazily when asked by clients.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    REBUILD,

    /**
     * Token hierarchy became inactive (while being active before) or vice versa.
     * <br/>
     * Current activity state can be determined by {@link TokenHierarchy#isActive()}.
     * <br/>
     * A maintainer of the given mutable input source may decide to activate/deactivate
     * token hierarchy by using {@link org.netbeans.spi.lexer.TokenHierarchyControl#setActive(boolean)}.
     * For example if a Swing docuemnt is not showing and it has not been edited for a long time
     * its token hierarchy may be deactivated to save memory. Once the hierarchy
     * gets deactivated the clients should drop all the functionality depending
     * on the tokens (for example not provide a token-dependent syntax highlighting).
     * <br/>
     * Only the removed tokens will be notified in case the hierarchy becomes inactive.
     * <br/>
     * There will be no added tokens notified in case the the hierarchy becomes active because
     * the tokens will be created lazily when asked by clients.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source. Only the initial (automatic) activation
     * of the mutable token hierarchy will happen under the read lock of the client
     * asking for <code>TokenHierarchy.tokenSequence()</code> or a similar method
     * that leads to automatic activation.
     */
    ACTIVITY,
        
    /**
     * Custom language embedding was created by
     * {@link TokenSequence#createEmbedding(Language,int,int)}.
     * <br/>
     * The {@link TokenHierarchyEvent#tokenChange()} contains the token
     * where the embedding was created and the embedded change
     * {@link TokenChange#embeddedChange(int)} that describes the added
     * embedded language.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    EMBEDDING_CREATED,
    
    /**
     * Custom language embedding was removed by
     * {@link TokenSequence#removeEmbedding(Language)}.
     * <br/>
     * The {@link TokenHierarchyEvent#tokenChange()} contains the token
     * where the embedding was created and the embedded change
     * {@link TokenChange#embeddedChange(int)} that describes the added
     * embedded language.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    EMBEDDING_REMOVED,
    
    /**
     * Notification that result of
     * {@link TokenHierarchy#languagePaths()} has changed.
     * <br/>
     * This change may be notified under both read and write lock
     * of the corresponding input source.
     */
    LANGUAGE_PATHS;

}
