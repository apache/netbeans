/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.lexer.demo.handcoded.link;

import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.AbstractLanguage;

public class LinkLanguage extends AbstractLanguage {

    /** Lazily initialized singleton instance of this language. */
    private static LinkLanguage INSTANCE;

    /** @return singleton instance of this language. */
    public static synchronized LinkLanguage get() {
        if (INSTANCE == null)
            INSTANCE = new LinkLanguage();

        return INSTANCE;
    }

    public static final int TEXT_INT = 1;
    public static final int HTTP_URI_INT = 2;
    public static final int FTP_URI_INT = 3;
    public static final int URI_INT = 4;


    public static final TokenId FTP_URI = new TokenId("ftp-uri", FTP_URI_INT, new String[]{"uri"}); // FTP absolute URI
    public static final TokenId HTTP_URI = new TokenId("http-uri", HTTP_URI_INT, new String[]{"uri"}); // HTTP absolute URI
    public static final TokenId TEXT = new TokenId("text", TEXT_INT); // Either whole line of text or a part of a line if a link is present on it
    public static final TokenId URI = new TokenId("uri", URI_INT, new String[]{"uri"}); // Other URI type

    LinkLanguage() {
    }

    public Lexer createLexer() {
        return new LinkLexer();
    }

}
