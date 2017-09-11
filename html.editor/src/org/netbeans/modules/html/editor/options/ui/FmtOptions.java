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
package org.netbeans.modules.html.editor.options.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 *
 */
public class FmtOptions {

    private static final Logger LOGGER = Logger.getLogger(FmtOptions.class.getName());

    public static final String expandTabToSpaces = SimpleValueNames.EXPAND_TABS;
    public static final String tabSize = SimpleValueNames.TAB_SIZE;
    public static final String spacesPerTab = SimpleValueNames.SPACES_PER_TAB;
    public static final String indentSize = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final String continuationIndentSize = "continuationIndentSize"; //NOI18N
    public static final String itemsInArrayDeclarationIndentSize = "itemsInArrayDeclarationIndentSize"; //NOI18N
    public static final String reformatComments = "reformatComments"; //NOI18N
    public static final String indentHtml = "indentHtml"; //NOI18N
    public static final String rightMargin = SimpleValueNames.TEXT_LIMIT_WIDTH;
    public static final String initialIndent = "init.indent"; //NOI18N

    public static final String classDeclBracePlacement = "classDeclBracePlacement"; //NOI18N
    public static final String methodDeclBracePlacement = "methodDeclBracePlacement"; //NOI18N
    public static final String ifBracePlacement = "ifBracePlacement"; //NOI18N
    public static final String forBracePlacement = "forBracePlacement"; //NOI18N
    public static final String whileBracePlacement = "whileBracePlacement"; //NOI18N
    public static final String switchBracePlacement = "switchBracePlacement"; //NOI18N
    public static final String catchBracePlacement = "catchBracePlacement"; //NOI18N
    public static final String useTraitBodyBracePlacement = "useTraitBodyBracePlacement"; //NOI18N
    public static final String otherBracePlacement = "otherBracePlacement"; //NOI18N

    public static final String blankLinesBeforeNamespace = "blankLinesBeforeNamespace"; //NOI18N
    public static final String blankLinesAfterNamespace = "blankLinesAfterNamespace"; //NOI18N
    public static final String blankLinesBeforeUse = "blankLinesBeforeUse"; //NOI18N
    public static final String blankLinesBeforeUseTrait = "blankLinesBeforeUseTrait"; //NOI18N
    public static final String blankLinesAfterUse = "blankLinesAfterUse"; //NOI18N
    public static final String blankLinesBeforeClass = "blankLinesBeforeClass"; //NOI18N
    public static final String blankLinesBeforeClassEnd = "blankLinesBeforeClassEnd"; //NOI18N
    public static final String blankLinesAfterClass = "blankLinesAfterClass"; //NOI18N
    public static final String blankLinesAfterClassHeader = "blankLinesAfterClassHeader"; //NOI18N
    public static final String blankLinesBeforeFields = "blankLinesBeforeField"; //NOI18N
    public static final String blankLinesBetweenFields = "blankLinesBetweenField"; //NOI18N
    public static final String blankLinesAfterFields = "blankLinesAfterField"; //NOI18N
    public static final String blankLinesGroupFieldsWithoutDoc = "blankLinesGroupFieldsWithoutDoc"; //NOI18N
    public static final String blankLinesBeforeFunction = "blankLinesBeforeFunction"; //NOI18N
    public static final String blankLinesAfterFunction = "blankLinesAfterFunction"; //NOI18N
    public static final String blankLinesBeforeFunctionEnd = "blankLinesBeforeFunctionEnd"; //NOI18N
    public static final String blankLinesAfterOpenPHPTag = "blankLinesAfterOpenPHPTag"; //NOI18N
    public static final String blankLinesAfterOpenPHPTagInHTML = "blankLinesAfterOpenPHPTagInHTML"; //NOI18N
    public static final String blankLinesBeforeClosePHPTag = "blankLinesBeforeClosePHPTag"; //NOI18N

    public static final String spaceBeforeWhile = "spaceBeforeWhile"; //NOI18N
    public static final String spaceBeforeElse = "spaceBeforeElse"; //NOI18N
    public static final String spaceBeforeCatch = "spaceBeforeCatch"; //NOI18N
    public static final String spaceBeforeFinally = "spaceBeforeFinally"; //NOI18N
    public static final String spaceBeforeMethodDeclParen = "spaceBeforeMethodDeclParen"; //NOI18N
    public static final String spaceBeforeMethodCallParen = "spaceBeforeMethodCallParen"; //NOI18N
    public static final String spaceBeforeIfParen = "spaceBeforeIfParen"; //NOI18N
    public static final String spaceBeforeForParen = "spaceBeforeForParen"; //NOI18N
    public static final String spaceBeforeWhileParen = "spaceBeforeWhileParen"; //NOI18N
    public static final String spaceBeforeCatchParen = "spaceBeforeCatchParen"; //NOI18N
    public static final String spaceBeforeSwitchParen = "spaceBeforeSwitchParen"; //NOI18N
    public static final String spaceBeforeWithParen = "spaceBeforeWithParen"; //NOI18N

    public static final String spaceAroundUnaryOps = "spaceAroundUnaryOps"; //NOI18N
    public static final String spaceAroundBinaryOps = "spaceAroundBinaryOps"; //NOI18N
    public static final String spaceAroundTernaryOps = "spaceAroundTernaryOps"; //NOI18N
    public static final String spaceAroundStringConcatOps = "spaceAroundStringConcatOps"; //NOI18N
    public static final String spaceAroundAssignOps = "spaceAroundAssignOps"; //NOI18N
    public static final String spaceAroundKeyValueOps = "spaceAroundKeyValueOps"; //NOI18N
    public static final String spaceAroundObjectOps = "spaceAroundObjectOps"; //NOI18N
    public static final String spaceBeforeClassDeclLeftBrace = "spaceBeforeClassDeclLeftBrace"; //NOI18N
    public static final String spaceBeforeMethodDeclLeftBrace = "spaceBeforeMethodDeclLeftBrace"; //NOI18N
    public static final String spaceBeforeIfLeftBrace = "spaceBeforeIfLeftBrace"; //NOI18N
    public static final String spaceBeforeElseLeftBrace = "spaceBeforeElseLeftBrace"; //NOI18N
    public static final String spaceBeforeWhileLeftBrace = "spaceBeforeWhileLeftBrace"; //NOI18N
    public static final String spaceBeforeForLeftBrace = "spaceBeforeForLeftBrace"; //NOI18N
    public static final String spaceBeforeDoLeftBrace = "spaceBeforeDoLeftBrace"; //NOI18N
    public static final String spaceBeforeSwitchLeftBrace = "spaceBeforeSwitchLeftBrace"; //NOI18N
    public static final String spaceBeforeTryLeftBrace = "spaceBeforeTryLeftBrace"; //NOI18N
    public static final String spaceBeforeCatchLeftBrace = "spaceBeforeCatchLeftBrace"; //NOI18N
    public static final String spaceBeforeFinallyLeftBrace = "spaceBeforeFinallyLeftBrace"; //NOI18N
    public static final String spaceBeforeWithLeftBrace = "spaceBeforeWithLeftBrace"; //NOI18N
//    public static final String spaceBeforeSynchronizedLeftBrace = "spaceBeforeSynchronizedLeftBrace"; //NOI18N
//    public static final String spaceBeforeStaticInitLeftBrace = "spaceBeforeStaticInitLeftBrace"; //NOI18N
//    public static final String spaceBeforeArrayInitLeftBrace = "spaceBeforeArrayInitLeftBrace"; //NOI18N
    public static final String spaceWithinParens = "spaceWithinParens"; //NOI18N
    public static final String spaceWithinArrayDeclParens = "spaceWithinArrayDeclParens"; //NOI18N
    public static final String spaceWithinMethodDeclParens = "spaceWithinMethodDeclParens"; //NOI18N
    public static final String spaceWithinMethodCallParens = "spaceWithinMethodCallParens"; //NOI18N
    public static final String spaceWithinIfParens = "spaceWithinIfParens"; //NOI18N
    public static final String spaceWithinForParens = "spaceWithinForParens"; //NOI18N
    public static final String spaceWithinWhileParens = "spaceWithinWhileParens"; //NOI18N
    public static final String spaceWithinSwitchParens = "spaceWithinSwitchParens"; //NOI18N
    public static final String spaceWithinCatchParens = "spaceWithinCatchParens"; //NOI18N
    public static final String spaceWithinWithParens = "spaceWithinWithParens"; //NOI18N
//    public static final String spaceWithinSynchronizedParens = "spaceWithinSynchronizedParens"; //NOI18N
    public static final String spaceWithinTypeCastParens = "spaceWithinTypeCastParens"; //NOI18N
//    public static final String spaceWithinAnnotationParens = "spaceWithinAnnotationParens"; //NOI18N
    public static final String spaceWithinBraces = "spaceWithinBraces"; //NOI18N
    public static final String spaceWithinArrayBrackets = "spaceWithinArrayBrackets"; //NOI18N
    public static final String spaceBeforeComma = "spaceBeforeComma"; //NOI18N
    public static final String spaceAfterComma = "spaceAfterComma"; //NOI18N
    public static final String spaceBeforeSemi = "spaceBeforeSemi"; //NOI18N
    public static final String spaceAfterSemi = "spaceAfterSemi"; //NOI18N
    public static final String spaceBeforeColon = "spaceBeforeColon"; //NOI18N
    public static final String spaceAfterColon = "spaceAfterColon"; //NOI18N

    public static final String placeElseOnNewLine = "placeElseOnNewLine"; //NOI18N
    public static final String placeWhileOnNewLine = "placeWhileOnNewLine"; //NOI18N
    public static final String placeCatchOnNewLine = "placeCatchOnNewLine"; //NOI18N
    public static final String placeNewLineAfterModifiers = "placeNewLineAfterModifiers"; //NOI18N
    public static final String alignMultilineMethodParams = "alignMultilineMethodParams"; //NOI18N
    public static final String alignMultilineCallArgs = "alignMultilineCallArgs"; //NOI18N
    public static final String alignMultilineImplements = "alignMultilineImplements"; //NOI18N
    public static final String alignMultilineParenthesized = "alignMultilineParenthesized"; //NOI18N
    public static final String alignMultilineBinaryOp = "alignMultilineBinaryOp"; //NOI18N
    public static final String alignMultilineTernaryOp = "alignMultilineTernaryOp"; //NOI18N
    public static final String alignMultilineAssignment = "alignMultilineAssignment"; //NOI18N
    public static final String alignMultilineFor = "alignMultilineFor"; //NOI18N
    public static final String alignMultilineArrayInit = "alignMultilineArrayInit"; //NOI18N

    public static final String groupAlignmentAssignment = "groupAlignmentAssignment"; //NOI18N
    public static final String groupAlignmentArrayInit = "groupAlignmentArrayInit"; //NOI18N

    public static final String wrapStatement = "wrapStatement"; //NOI18N
    public static final String wrapVariables = "wrapVariables"; //NOI18N
    public static final String wrapMethodParams = "wrapMethodParams"; //NOI18N
    public static final String wrapMethodCallArgs = "wrapMethodCallArgs"; //NOI18N
    public static final String wrapChainedMethodCalls = "wrapChainedMethodCalls"; //NOI18N
    public static final String wrapAfterDotInChainedMethodCalls = "wrapAfterDotInChainedMethodCalls"; //NOI18N
    public static final String wrapArrayInit = "wrapArrayInit"; //NOI18N
    public static final String wrapArrayInitItems = "wrapArrayInitItems"; //NOI18N
    public static final String wrapFor = "wrapFor"; //NOI18N
    public static final String wrapForStatement = "wrapForStatement"; //NOI18N
    public static final String wrapIfStatement = "wrapIfStatement"; //NOI18N
    public static final String wrapWhileStatement = "wrapWhileStatement"; //NOI18N
    public static final String wrapDoWhileStatement = "wrapDoWhileStatement"; //NOI18N
    public static final String wrapWithStatement = "wrapWithStatement"; //NOI18N
    public static final String wrapBinaryOps = "wrapBinaryOps"; //NOI18N
    public static final String wrapAfterBinaryOps = "wrapAfterBinaryOps"; //NOI18N
    public static final String wrapTernaryOps = "wrapTernaryOps"; //NOI18N
    public static final String wrapAfterTernaryOps = "wrapAfterTernaryOps"; //NOI18N
    public static final String wrapAssignOps = "wrapAssignOps"; //NOI18N
    public static final String wrapBlockBraces = "wrapBlockBraces";  //NOI18N
    public static final String wrapStatementsOnTheLine = "wrapStateMentsOnTheLine"; // NOI18N
    public static final String wrapObjects = "wrapObjects"; // NOI18N
    public static final String wrapProperties = "wrapProperties"; // NOI18N

    public static final String preferFullyQualifiedNames = "preferFullyQualifiedNames"; //NOI18N
    public static final String preferMultipleUseStatementsCombined = "preferMultipleUseStatementsCombined"; //NOI18N
    public static final String startUseWithNamespaceSeparator = "startUseWithNamespaceSeparator"; //NOI18N

//    public static CodeStyleProducer codeStyleProducer;

    private FmtOptions() {}

    public static int getDefaultAsInt(String key) {
        return Integer.parseInt(defaults.get(key));
    }

    public static boolean getDefaultAsBoolean(String key) {
        return Boolean.parseBoolean(defaults.get(key));
    }

    public static String getDefaultAsString(String key) {
        return defaults.get(key);
    }

    // Private section ---------------------------------------------------------

    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N

    
    public enum BracePlacement {
        SAME_LINE,
        NEW_LINE,
	NEW_LINE_INDENTED,
        PRESERVE_EXISTING
    }

    public enum WrapStyle {
        WRAP_ALWAYS,
        WRAP_IF_LONG,
        WRAP_NEVER
    }
    
//    //opening brace styles
    public static final String OBRACE_NEWLINE = BracePlacement.NEW_LINE.name();
    public static final String OBRACE_SAMELINE = BracePlacement.SAME_LINE.name();
    public static final String OBRACE_PRESERVE = BracePlacement.PRESERVE_EXISTING.name();
    public static final String OBRACE_NEWLINE_INDENTED = BracePlacement.NEW_LINE_INDENTED.name();

    public static final String WRAP_ALWAYS = WrapStyle.WRAP_ALWAYS.name();
    public static final String WRAP_IF_LONG = WrapStyle.WRAP_IF_LONG.name();
    public static final String WRAP_NEVER = WrapStyle.WRAP_NEVER.name();

    private static Map<String,String> defaults;

    static {
        createDefaults();
    }

    private static void createDefaults() {
        String defaultValues[][] = {
            { expandTabToSpaces, TRUE}, //NOI18N
            { tabSize, "8"}, //NOI18N
            { indentSize, "4"}, //NOI18N
            { continuationIndentSize, "8"}, //NOI18N
            { itemsInArrayDeclarationIndentSize, "4"}, // NOI18N
            { reformatComments, FALSE }, //NOI18N
            { indentHtml, TRUE }, //NOI18N
            { rightMargin, "80"}, //NOI18N
            { initialIndent, "0"}, //NOI18N

	    { classDeclBracePlacement, OBRACE_SAMELINE },
	    { methodDeclBracePlacement, OBRACE_SAMELINE },
	    { ifBracePlacement, OBRACE_SAMELINE },
	    { forBracePlacement, OBRACE_SAMELINE },
	    { whileBracePlacement, OBRACE_SAMELINE },
	    { switchBracePlacement, OBRACE_SAMELINE },
	    { catchBracePlacement, OBRACE_SAMELINE },
            { useTraitBodyBracePlacement, OBRACE_SAMELINE },
	    { otherBracePlacement, OBRACE_SAMELINE },

            { blankLinesBeforeNamespace, "1"}, //NOI18N
            { blankLinesAfterNamespace, "1"}, //NOI18N
            { blankLinesBeforeUse, "1"}, //NOI18N
            { blankLinesBeforeUseTrait, "1"}, //NOI18N
            { blankLinesAfterUse, "1"}, //NOI18N
            { blankLinesBeforeClass, "1"}, //NOI18N
            { blankLinesAfterClass, "1"}, //NOI18N
            { blankLinesAfterClassHeader, "0"}, //NOI18N
            { blankLinesBeforeClassEnd, "0"}, //NOI18N
            { blankLinesBeforeFields, "1"}, //NOI18N
	    { blankLinesGroupFieldsWithoutDoc, TRUE}, //NOI18N
	    { blankLinesBetweenFields, "1"}, //NOI18N
            { blankLinesAfterFields, "1"}, //NOI18N
            { blankLinesBeforeFunction, "1"}, //NOI18N
            { blankLinesAfterFunction, "1"}, //NOI18N
            { blankLinesBeforeFunctionEnd, "0"}, //NOI18N
	    { blankLinesAfterOpenPHPTag, "1"}, //NOI18N
	    { blankLinesAfterOpenPHPTagInHTML, "0"}, //NOI18N
	    { blankLinesBeforeClosePHPTag, "0"}, //NOI18N

            { spaceBeforeWhile, TRUE},
            { spaceBeforeElse, TRUE},
            { spaceBeforeCatch, TRUE},
            { spaceBeforeFinally, TRUE},
            { spaceBeforeMethodDeclParen, FALSE},
            { spaceBeforeMethodCallParen, FALSE},
            { spaceBeforeIfParen, TRUE},
            { spaceBeforeForParen, TRUE},
            { spaceBeforeWhileParen, TRUE},
            { spaceBeforeCatchParen, TRUE},
            { spaceBeforeSwitchParen, TRUE},
            { spaceBeforeWithParen, TRUE},
            { spaceAroundUnaryOps, FALSE},
            { spaceAroundBinaryOps, TRUE},
            { spaceAroundTernaryOps, TRUE},
	    { spaceAroundStringConcatOps, TRUE},
	    { spaceAroundKeyValueOps, TRUE},
            { spaceAroundAssignOps, TRUE},
	    { spaceAroundObjectOps, FALSE},
            { spaceBeforeClassDeclLeftBrace, TRUE},
            { spaceBeforeMethodDeclLeftBrace, TRUE},
            { spaceBeforeIfLeftBrace, TRUE},
            { spaceBeforeElseLeftBrace, TRUE},
            { spaceBeforeWhileLeftBrace, TRUE},
            { spaceBeforeForLeftBrace, TRUE},
            { spaceBeforeDoLeftBrace, TRUE},
            { spaceBeforeSwitchLeftBrace, TRUE},
            { spaceBeforeTryLeftBrace, TRUE},
            { spaceBeforeCatchLeftBrace, TRUE},
            { spaceBeforeFinallyLeftBrace, TRUE},
            { spaceBeforeWithLeftBrace, TRUE},
//            { spaceBeforeSynchronizedLeftBrace, TRUE},
//            { spaceBeforeStaticInitLeftBrace, TRUE},
//            { spaceBeforeArrayInitLeftBrace, FALSE},
            { spaceWithinParens, FALSE},
	    { spaceWithinArrayDeclParens, FALSE},
            { spaceWithinMethodDeclParens, FALSE},
            { spaceWithinMethodCallParens, FALSE},
            { spaceWithinIfParens, FALSE},
            { spaceWithinForParens, FALSE},
            { spaceWithinWhileParens, FALSE},
            { spaceWithinSwitchParens, FALSE},
            { spaceWithinCatchParens, FALSE},
            { spaceWithinWithParens, FALSE},
//            { spaceWithinSynchronizedParens, FALSE},
            { spaceWithinTypeCastParens, FALSE},
//            { spaceWithinAnnotationParens, FALSE},
            { spaceWithinBraces, FALSE},
            { spaceWithinArrayBrackets, FALSE},
            { spaceBeforeComma, FALSE},
            { spaceAfterComma, TRUE},
            { spaceBeforeSemi, FALSE},
            { spaceAfterSemi, TRUE},
            { spaceBeforeColon, FALSE},
            { spaceAfterColon, TRUE},

	    { alignMultilineMethodParams, FALSE}, //NOI18N
            { alignMultilineCallArgs, FALSE}, //NOI18N
            { alignMultilineImplements, FALSE}, //NOI18N
            { alignMultilineParenthesized, FALSE}, //NOI18N
            { alignMultilineBinaryOp, FALSE}, //NOI18N
            { alignMultilineTernaryOp, FALSE}, //NOI18N
            { alignMultilineAssignment, FALSE}, //NOI18N
            { alignMultilineFor, FALSE}, //NOI18N
            { alignMultilineArrayInit, FALSE}, //NOI18N
            { placeElseOnNewLine, FALSE}, //NOI18N
            { placeWhileOnNewLine, FALSE}, //NOI18N
            { placeCatchOnNewLine, FALSE}, //NOI18N
            { placeNewLineAfterModifiers, FALSE}, //NOI18N

            {groupAlignmentArrayInit, FALSE},
            {groupAlignmentAssignment, FALSE},

	    { wrapStatement, WRAP_ALWAYS}, //NOI18N
            { wrapVariables, WRAP_NEVER}, //NOI18N
            { wrapMethodParams, WRAP_NEVER}, //NOI18N
            { wrapMethodCallArgs, WRAP_NEVER}, //NOI18N
            { wrapChainedMethodCalls, WRAP_NEVER}, //NOI18N
            { wrapAfterDotInChainedMethodCalls, TRUE}, //NOI18N
            { wrapArrayInit, WRAP_NEVER}, //NOI18N
            { wrapArrayInitItems, WRAP_NEVER}, //NOI18N
            { wrapFor, WRAP_NEVER}, //NOI18N
            { wrapForStatement, WRAP_ALWAYS}, //NOI18N
            { wrapIfStatement, WRAP_ALWAYS}, //NOI18N
            { wrapWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapDoWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapWithStatement, WRAP_ALWAYS}, //NOI18N
            { wrapBinaryOps, WRAP_NEVER}, //NOI18N
            { wrapAfterBinaryOps, FALSE}, //NOI18N
            { wrapTernaryOps, WRAP_NEVER},
            { wrapAfterTernaryOps, FALSE}, //NOI18N
            { wrapAssignOps, WRAP_NEVER},
            { wrapBlockBraces, TRUE},
            { wrapStatementsOnTheLine, TRUE},
            { wrapObjects, WRAP_NEVER},
            { wrapProperties, WRAP_NEVER},

            { preferFullyQualifiedNames, FALSE},
            { preferMultipleUseStatementsCombined, FALSE},
            { startUseWithNamespaceSeparator, FALSE}
        };

        defaults = new HashMap<>();

        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }

    protected static Map<String, String> getDefaults() {
	return defaults;
    }

     // Support section ---------------------------------------------------------

    public static class CategorySupport implements ActionListener, DocumentListener, PreviewProvider, PreferencesCustomizer {

//        public static final String OPTION_ID = "org.netbeans.modules.javascript2.editor.formatter.FormatingOptions.ID";
//
//        private static final int LOAD = 0;
//        private static final int STORE = 1;
//        private static final int ADD_LISTENERS = 2;
//
//        private static final ComboItem  bracePlacement[] = new ComboItem[] {
//                new ComboItem( OBRACE_NEWLINE, "LBL_bp_NEWLINE" ), // NOI18N
//		new ComboItem( OBRACE_NEWLINE_INDENTED, "LBL_bp_NEWLINE_INDENTED" ), // NOI18N
//                new ComboItem( OBRACE_SAMELINE, "LBL_bp_SAMELINE" ), // NOI18N
//                new ComboItem( OBRACE_PRESERVE, "LBL_bp_PRESERVE" ), // NOI18N
//            };
//
//	private static final ComboItem  wrap[] = new ComboItem[] {
//                new ComboItem( WrapStyle.WRAP_ALWAYS.name(), "LBL_wrp_WRAP_ALWAYS" ), // NOI18N
//                new ComboItem( WrapStyle.WRAP_IF_LONG.name(), "LBL_wrp_WRAP_IF_LONG" ), // NOI18N
//                new ComboItem( WrapStyle.WRAP_NEVER.name(), "LBL_wrp_WRAP_NEVER" ) // NOI18N
//            };

        private final String previewText;
//        private String forcedOptions[][];

//        private boolean changed = false;
//        private boolean loaded = false;
        private final String id;
        protected final JPanel panel;
//        private final List<JComponent> components = new LinkedList<>();
        private JEditorPane previewPane;

        private final Preferences preferences;
        private final Preferences previewPrefs;

        private final String mimeType;

        protected CategorySupport(String mimeType, Preferences preferences, String id, JPanel panel, String previewText, String[]... forcedOptions) {
            this.mimeType = mimeType;
            this.preferences = preferences;
            this.id = id;
            this.panel = panel;
            this.previewText = previewText != null ? previewText : NbBundle.getMessage(FmtOptions.class, "SAMPLE_Default"); //NOI18N

            // Scan the panel for its components
//            scan(panel, components);

            // Initialize the preview preferences
            Preferences forcedPrefs = new PreviewPreferences();
            for (String[] option : forcedOptions) {
                forcedPrefs.put( option[0], option[1]);
            }
            this.previewPrefs = new ProxyPreferences(preferences, forcedPrefs);

            // Load and hook up all the components
//            loadFrom(preferences);
//            addListeners();
        }

        protected void addListeners() {
//            scan(ADD_LISTENERS, null);
        }

        protected void loadFrom(Preferences preferences) {
//            loaded = true;
//            scan(LOAD, preferences);
//            loaded = false;
        }
//
//        public void applyChanges() {
//            storeTo(preferences);
//        }
//
        protected void storeTo(Preferences p) {
//            scan(STORE, p);
        }

        public void notifyChanged() {
//            if (loaded)
//                return;
            storeTo(preferences);
            refreshPreview();
        }

        // ActionListener implementation ---------------------------------------

        @Override
        public void actionPerformed(ActionEvent e) {
            notifyChanged();
        }

        // DocumentListener implementation -------------------------------------

        @Override
        public void insertUpdate(DocumentEvent e) {
            notifyChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            notifyChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            notifyChanged();
        }

        // PreviewProvider methods -----------------------------------------------------

        @Override
        public JComponent getPreviewComponent() {
            if (previewPane == null) {
                previewPane = new JEditorPane();
                previewPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtOptions.class, "AN_Preview")); //NOI18N
                previewPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtOptions.class, "AD_Preview")); //NOI18N
                //previewPane.putClientProperty("HighlightsLayerIncludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.SyntaxHighlighting$"); //NOI18N
                previewPane.setEditorKit(CloneableEditorSupport.getEditorKit(mimeType));
                previewPane.setEditable(false);
            }
            return previewPane;
        }

        @Override
        public void refreshPreview() {
            JEditorPane pane = (JEditorPane) getPreviewComponent();
            try {
                int rm = previewPrefs.getInt(rightMargin, getDefaultAsInt(rightMargin));
                pane.putClientProperty("TextLimitLine", rm); //NOI18N
            }
            catch( NumberFormatException e ) {
                // Ignore it
            }

            Rectangle visibleRectangle = pane.getVisibleRect();
            pane.setText(previewText);
            pane.setIgnoreRepaint(true);

            final Document doc = pane.getDocument();
            if (doc instanceof BaseDocument) {
                final Reformat reformat = Reformat.get(doc);
                reformat.lock();
                try {
                    ((BaseDocument) doc).runAtomic(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                reformat.reformat(0, doc.getLength());
                            } catch (BadLocationException ble) {
                                LOGGER.log(Level.WARNING, null, ble);
                            }
                        }
                    });
                } finally {
                    reformat.unlock();
                }
            } else {
                LOGGER.warning(String.format("Can't format %s; it's not BaseDocument.", doc)); //NOI18N
            }
            pane.setIgnoreRepaint(false);
            pane.scrollRectToVisible(visibleRectangle);
            pane.repaint(100);

        }

        // PreferencesCustomizer implementation --------------------------------

        @Override
        public JComponent getComponent() {
            return panel;
        }

        @Override
        public String getDisplayName() {
            return panel.getName();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        // PreferencesCustomizer.Factory implementation ------------------------

        public static final class Factory implements PreferencesCustomizer.Factory {

            private final String mimeType;
            private final String id;
            private final Class<? extends JPanel> panelClass;
            private final String previewText;
            private final String[][] forcedOptions;

            public Factory(String mimeType, String id, Class<? extends JPanel> panelClass, String previewText, String[]... forcedOptions) {
                this.mimeType = mimeType;
                this.id = id;
                this.panelClass = panelClass;
                this.previewText = previewText;
                this.forcedOptions = forcedOptions;
            }

            @Override
            public PreferencesCustomizer create(Preferences preferences) {
                try {
                    return new CategorySupport(mimeType, preferences, id, panelClass.newInstance(), previewText, forcedOptions);
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.log(Level.WARNING, "Exception during creating formatter customiezer", e);
                    return null;
                }
            }
        } // End of CategorySupport.Factory class

        // Private methods -----------------------------------------------------

//        private void performOperation(int operation, JComponent jc, String optionID, Preferences p) {
//            switch (operation) {
//                case LOAD:
//                    loadData(jc, optionID, p);
//                    break;
//                case STORE:
//                    storeData(jc, optionID, p);
//                    break;
//                case ADD_LISTENERS:
//                    addListener(jc);
//                    break;
//                default:
//                    LOGGER.log(Level.WARNING, "Unknown operation value {0}", operation);
//                    break;
//            }
//        }
//
//        private void scan(int what, Preferences p ) {
//            for (JComponent jc : components) {
//                Object o = jc.getClientProperty(OPTION_ID);
//                if (o instanceof String) {
//                    performOperation(what, jc, (String)o, p);
//                } else if (o instanceof String[]) {
//                    for(String oid : (String[])o) {
//                        performOperation(what, jc, oid, p);
//                    }
//                }
//            }
//        }
//
//        private void scan(Container container, List<JComponent> components) {
//            for (Component c : container.getComponents()) {
//                if (c instanceof JComponent) {
//                    JComponent jc = (JComponent)c;
//                    Object o = jc.getClientProperty(OPTION_ID);
//                    if (o instanceof String || o instanceof String[])
//                        components.add(jc);
//                }
//                if (c instanceof Container)
//                    scan((Container)c, components);
//            }
//        }
//
//        /** Very smart method which tries to set the values in the components correctly
//         */
//        private void loadData( JComponent jc, String optionID, Preferences node ) {
//
//            if ( jc instanceof JTextField ) {
//                JTextField field = (JTextField)jc;
//                field.setText( node.get(optionID, getDefaultAsString(optionID)) );
//            }
//            else if ( jc instanceof JCheckBox ) {
//                JCheckBox checkBox = (JCheckBox)jc;
//                boolean df = getDefaultAsBoolean(optionID);
//                checkBox.setSelected( node.getBoolean(optionID, df));
//            }
//            else if ( jc instanceof JComboBox) {
//                JComboBox cb  = (JComboBox)jc;
//                String value = node.get(optionID, getDefaultAsString(optionID) );
//                ComboBoxModel model = createModel(value);
//                cb.setModel(model);
//                ComboItem item = whichItem(value, model);
//                cb.setSelectedItem(item);
//            }
//
//        }
//
//        private void storeData( JComponent jc, String optionID, Preferences node ) {
//
//            if ( jc instanceof JTextField ) {
//                JTextField field = (JTextField)jc;
//
//                String text = field.getText();
//
//                // XXX test for numbers
//                if ( isInteger(optionID) ) {
//                    try {
//                        Integer.parseInt(text);
//                    } catch (NumberFormatException e) {
//                        return;
//                    }
//                }
//
//                // XXX: watch out, tabSize, spacesPerTab, indentSize and expandTabToSpaces
//                // fall back on getGlopalXXX() values and not getDefaultAsXXX value,
//                // which is why we must not remove them. Proper solution would be to
//                // store formatting preferences to MimeLookup and not use NbPreferences.
//                // The problem currently is that MimeLookup based Preferences do not support subnodes.
//                if (!optionID.equals(tabSize) &&
//                    !optionID.equals(spacesPerTab) && !optionID.equals(indentSize) &&
//                    getDefaultAsString(optionID).equals(text)
//                ) {
//                    node.remove(optionID);
//                } else {
//                    node.put(optionID, text);
//                }
//            }
//            else if ( jc instanceof JCheckBox ) {
//                JCheckBox checkBox = (JCheckBox)jc;
//                if (!optionID.equals(expandTabToSpaces) && getDefaultAsBoolean(optionID) == checkBox.isSelected())
//                    node.remove(optionID);
//                else
//                    node.putBoolean(optionID, checkBox.isSelected());
//            }
//            else if ( jc instanceof JComboBox) {
//                JComboBox cb  = (JComboBox)jc;
//                ComboItem comboItem = ((ComboItem) cb.getSelectedItem());
//                String value = comboItem == null ? getDefaultAsString(optionID) : comboItem.value;
//
//                if (getDefaultAsString(optionID).equals(value))
//                    node.remove(optionID);
//                else
//                    node.put(optionID,value);
//            }
//        }
//
//        private void addListener( JComponent jc ) {
//            if ( jc instanceof JTextField ) {
//                JTextField field = (JTextField)jc;
//                field.addActionListener(this);
//                field.getDocument().addDocumentListener(this);
//            }
//            else if ( jc instanceof JCheckBox ) {
//                JCheckBox checkBox = (JCheckBox)jc;
//                checkBox.addActionListener(this);
//            }
//            else if ( jc instanceof JComboBox) {
//                JComboBox cb  = (JComboBox)jc;
//                cb.addActionListener(this);
//            }
//        }
//
//
//        private ComboBoxModel createModel( String value ) {
//
//            // is it braces placement?
//            for (ComboItem comboItem : bracePlacement) {
//                if ( value.equals( comboItem.value) ) {
//                    return new DefaultComboBoxModel( bracePlacement );
//                }
//            }
//
//	    // is it wrap
//            for (ComboItem comboItem : wrap) {
//                if ( value.equals( comboItem.value) ) {
//                    return new DefaultComboBoxModel( wrap );
//                }
//            }
//
//            return null;
//        }
//
//        private static ComboItem whichItem(String value, ComboBoxModel model) {
//
//            for (int i = 0; i < model.getSize(); i++) {
//                ComboItem item = (ComboItem)model.getElementAt(i);
//                if ( value.equals(item.value)) {
//                    return item;
//                }
//            }
//            return null;
//        }
//
//        private static class ComboItem {
//
//            String value;
//            String displayName;
//
//            public ComboItem(String value, String key) {
//                this.value = value;
//                this.displayName = NbBundle.getMessage(FmtOptions.class, key);
//            }
//
//            @Override
//            public String toString() {
//                return displayName;
//            }
//
//        }
    }

    public static class PreviewPreferences extends AbstractPreferences {

        private Map<String,Object> map = new HashMap<>();

        public PreviewPreferences() {
            super(null, ""); // NOI18N
        }

        @Override
        protected void putSpi(String key, String value) {
            map.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            return (String)map.get(key);
        }

        @Override
        protected void removeSpi(String key) {
            map.remove(key);
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    // read-only, no subnodes
    public static final class ProxyPreferences extends AbstractPreferences {

        private final Preferences[] delegates;

        public ProxyPreferences(Preferences... delegates) {
            super(null, ""); // NOI18N
            this.delegates = delegates;
        }

        @Override
        protected void putSpi(String key, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String getSpi(String key) {
            for(Preferences p : delegates) {
                String value = p.get(key, null);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        @Override
        protected void removeSpi(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            Set<String> keys = new HashSet<>();
            for(Preferences p : delegates) {
                keys.addAll(Arrays.asList(p.keys()));
            }
            return keys.toArray(new String[ keys.size() ]);
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    } // End of ProxyPreferences class

   
    public static boolean isInteger(String optionID) {
        String value = defaults.get(optionID);

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
}
