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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.lib2;

/**
 * This class contains settings default values copied over from SettingsDefaults and ExtSettingsDefaults.
 * It exists merely to allow editor infrastructure to use these constants for
 * backwards compatibility reasons without having to depend on editor.deprecated.pre61settings module.
 */
public final class DocumentPreferencesDefaults {

    private DocumentPreferencesDefaults() {
        // no-op
    }

    // -----------------------------------------------------------------------
    // --- from SettingsDefaults
    // -----------------------------------------------------------------------
    
    public static final int defaultTabSize = 8;
    public static final int defaultSpacesPerTab = 4;
    public static final int defaultShiftWidth = 4; // usually
    // not used as there's a Evaluator for shift width

    public static final int defaultTextLimitWidth = 80;

    public static final Acceptor defaultIdentifierAcceptor = AcceptorFactory.LETTER_DIGIT;
    public static final Acceptor defaultWhitespaceAcceptor = AcceptorFactory.WHITESPACE;

    public static final int defaultTextLeftMarginWidth = 2;

    public static final int defaultReadBufferSize = 16384;
    public static final int defaultWriteBufferSize = 16384;
    public static final int defaultReadMarkDistance = 180;
    public static final int defaultMarkDistance = 100;
    public static final int defaultMaxMarkDistance = 150;
    public static final int defaultMinMarkDistance = 50;
    public static final int defaultSyntaxUpdateBatchSize = defaultMarkDistance * 7;
    public static final int defaultLineBatchSize = 2;

    public static final boolean defaultExpandTabs = true;

    public static final Acceptor defaultAbbrevExpandAcceptor = AcceptorFactory.WHITESPACE;
    public static final Acceptor defaultAbbrevAddTypedCharAcceptor = AcceptorFactory.NL;
    public static final Acceptor defaultAbbrevResetAcceptor = AcceptorFactory.NON_JAVA_IDENTIFIER;
    
    public static final boolean defaultHomeKeyColumnOne = false;
    public static final boolean defaultWordMoveNewlineStop = true;
    public static final boolean defaultFindHighlightSearch = true;
    public static final boolean defaultFindIncSearch = true;
    public static final boolean defaultFindBackwardSearch = false;
    public static final boolean defaultFindWrapSearch = true;
    public static final boolean defaultFindMatchCase = false;
    public static final boolean defaultFindWholeWords = false;
    public static final boolean defaultFindRegExp = false;
    public static final int defaultFindHistorySize = 30;
    public static final int defaultWordMatchSearchLen = Integer.MAX_VALUE;
    public static final boolean defaultWordMatchWrapSearch = true;
    public static final boolean defaultWordMatchMatchOneChar = true;
    public static final boolean defaultWordMatchMatchCase = false;
    public static final boolean defaultWordMatchSmartCase = false;
}
