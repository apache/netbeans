/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser;

/**
 *
 * @author marekfukala
 */
public class Util {

    public static final String[] TOKENIZER_STATE_NAMES = new String[]{
        "DATA",
        "RCDATA",
        "SCRIPT_DATA",
        "RAWTEXT",
        "SCRIPT_DATA_ESCAPED",
        "ATTRIBUTE_VALUE_DOUBLE_QUOTED",
        "ATTRIBUTE_VALUE_SINGLE_QUOTED",
        "ATTRIBUTE_VALUE_UNQUOTED",
        "PLAINTEXT",
        "TAG_OPEN",
        "CLOSE_TAG_OPEN",
        "TAG_NAME",
        "BEFORE_ATTRIBUTE_NAME",
        "ATTRIBUTE_NAME",
        "AFTER_ATTRIBUTE_NAME",
        "BEFORE_ATTRIBUTE_VALUE",
        "AFTER_ATTRIBUTE_VALUE_QUOTED",
        "BOGUS_COMMENT",
        "MARKUP_DECLARATION_OPEN",
        "DOCTYPE",
        "BEFORE_DOCTYPE_NAME",
        "DOCTYPE_NAME",
        "AFTER_DOCTYPE_NAME",
        "BEFORE_DOCTYPE_PUBLIC_IDENTIFIER",
        "DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED",
        "DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED",
        "AFTER_DOCTYPE_PUBLIC_IDENTIFIER",
        "BEFORE_DOCTYPE_SYSTEM_IDENTIFIER",
        "DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED",
        "DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED",
        "AFTER_DOCTYPE_SYSTEM_IDENTIFIER",
        "BOGUS_DOCTYPE",
        "COMMENT_START",
        "COMMENT_START_DASH",
        "COMMENT",
        "COMMENT_END_DASH",
        "COMMENT_END",
        "COMMENT_END_SPACE",
        "COMMENT_END_BANG",
        "NON_DATA_END_TAG_NAME",
        "MARKUP_DECLARATION_HYPHEN",
        "MARKUP_DECLARATION_OCTYPE",
        "DOCTYPE_UBLIC",
        "DOCTYPE_YSTEM",
        "AFTER_DOCTYPE_PUBLIC_KEYWORD",
        "BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS",
        "AFTER_DOCTYPE_SYSTEM_KEYWORD",
        "CONSUME_CHARACTER_REFERENCE",
        "CONSUME_NCR",
        "CHARACTER_REFERENCE_TAIL",
        "HEX_NCR_LOOP",
        "DECIMAL_NRC_LOOP",
        "HANDLE_NCR_VALUE",
        "HANDLE_NCR_VALUE_RECONSUME",
        "CHARACTER_REFERENCE_HILO_LOOKUP",
        "SELF_CLOSING_START_TAG",
        "CDATA_START",
        "CDATA_SECTION",
        "CDATA_RSQB",
        "CDATA_RSQB_RSQB",
        "SCRIPT_DATA_LESS_THAN_SIGN",
        "SCRIPT_DATA_ESCAPE_START",
        "SCRIPT_DATA_ESCAPE_START_DASH",
        "SCRIPT_DATA_ESCAPED_DASH",
        "SCRIPT_DATA_ESCAPED_DASH_DASH",
        "BOGUS_COMMENT_HYPHEN",
        "RAWTEXT_RCDATA_LESS_THAN_SIGN",
        "SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN",
        "SCRIPT_DATA_DOUBLE_ESCAPE_START",
        "SCRIPT_DATA_DOUBLE_ESCAPED",
        "SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN",
        "SCRIPT_DATA_DOUBLE_ESCAPED_DASH",
        "SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH",
        "SCRIPT_DATA_DOUBLE_ESCAPE_END"
    }; //NOI18N
    
}
