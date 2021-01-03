/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.source;

import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.python.source.ui.FmtOptions;

import org.openide.filesystems.FileObject;
import static org.netbeans.modules.python.source.ui.FmtOptions.*;

/** 
 *  XXX make sure the getters get the defaults from somewhere
 *  XXX add support for profiles
 *  XXX get the preferences node from somewhere else in odrer to be able not to
 *      use the getters and to be able to write to it.
 * 
 */
public final class CodeStyle {
    static {
        FmtOptions.codeStyleProducer = new Producer();
    }
    private Preferences preferences;

    private CodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }

//    /**
//     * Gets <code>CodeStyle</code> for files in the given project.
//     *
//     * <p>Please see the other two <code>getDefault</code> methods as they are
//     * the preferred way of getting <code>CodeStyle</code>.
//     *
//     * @param project The project to get the <code>CodeStyle</code> for.
//     * @return The current code style that would be used by documents opened
//     *   from files belonging to the <code>project</code>.
//     *
//     * @deprecated Please use {@link #getDefault(javax.swing.text.Document)}
//     *   or {@link #getDefault(org.openide.filesystems.FileObject)} respectively.
//     */
//    @Deprecated
//    public static CodeStyle getDefault(Project project) {
//        return getDefault(project.getProjectDirectory());
//    }
    /**
     * Gets <code>CodeStyle</code> for the given file. If you have a document
     * instance you should use the {@link #getDefault(javax.swing.text.Document)}
     * method.
     * 
     * @param file The file to get the <code>CodeStyle</code> for.
     * @return The current code style that would be used by a document if the
     *   <code>file</code> were opened in the editor.
     *
     * @since 0.39
     */
    public synchronized static CodeStyle getDefault(FileObject file) {
        Preferences prefs = CodeStylePreferences.get(file).getPreferences();
        return FmtOptions.codeStyleProducer.create(prefs);
    }

    /**
     * Gets <code>CodeStyle</code> for the given document. This is the preferred
     * method of getting <code>CodeStyle</code>. If you don't have a document
     * you can use {@link #getDefault(org.openide.filesystems.FileObject)} method instead.
     *
     * @param doc The document to get the <code>CodeStyle</code> for.
     * @return The current code style used by a document. This is the code style that
     *   will be used when formatting the document or generating new code.
     * 
     * @since 0.39
     */
    public synchronized static CodeStyle getDefault(Document doc) {
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        return FmtOptions.codeStyleProducer.create(prefs);
    }

    // General tabs and indents ------------------------------------------------
    public boolean expandTabToSpaces() {
//        System.out.println("~~~ expand-tabs=" + preferences.get(SimpleValueNames.EXPAND_TABS, null));
        return preferences.getBoolean(expandTabToSpaces, getDefaultAsBoolean(expandTabToSpaces));
    }

    public int getTabSize() {
//        System.out.println("~~~ tab-size=" + preferences.get(SimpleValueNames.TAB_SIZE, null));
        return preferences.getInt(tabSize, getDefaultAsInt(tabSize));
    }

    public int getIndentSize() {
//        System.out.println("~~~ indent-shift-width=" + preferences.get(SimpleValueNames.INDENT_SHIFT_WIDTH, null));
        int indentLevel = preferences.getInt(indentSize, getDefaultAsInt(indentSize));

        if (indentLevel <= 0) {
//            System.out.println("~~~ expand-tabs=" + preferences.get(SimpleValueNames.EXPAND_TABS, null));
            boolean expandTabs = preferences.getBoolean(expandTabToSpaces, getDefaultAsBoolean(expandTabToSpaces));
            if (expandTabs) {
//                System.out.println("~~~ spaces-per-tab=" + preferences.get(SimpleValueNames.SPACES_PER_TAB, null));
                indentLevel = preferences.getInt(spacesPerTab, getDefaultAsInt(spacesPerTab));
            } else {
//                System.out.println("~~~ tab-size=" + preferences.get(SimpleValueNames.TAB_SIZE, null));
                indentLevel = preferences.getInt(tabSize, getDefaultAsInt(tabSize));
            }
        }

        return indentLevel;
    }

    public int getContinuationIndentSize() {
        return preferences.getInt(continuationIndentSize, getDefaultAsInt(continuationIndentSize));
    }

    public int getLabelIndent() {
        return preferences.getInt(labelIndent, getDefaultAsInt(labelIndent));
    }

    public boolean absoluteLabelIndent() {
        return preferences.getBoolean(absoluteLabelIndent, getDefaultAsBoolean(absoluteLabelIndent));
    }

    public boolean indentTopLevelClassMembers() {
        return preferences.getBoolean(indentTopLevelClassMembers, getDefaultAsBoolean(indentTopLevelClassMembers));
    }

    public boolean indentCasesFromSwitch() {
        return preferences.getBoolean(indentCasesFromSwitch, getDefaultAsBoolean(indentCasesFromSwitch));
    }

    public int getRightMargin() {
        return preferences.getInt(rightMargin, getDefaultAsInt(rightMargin));
    }

    /*
    public boolean addLeadingStarInComment() {
    return preferences.getBoolean(addLeadingStarInComment, getDefaultAsBoolean(addLeadingStarInComment));
    }

    // Code generation ---------------------------------------------------------
    
    public boolean preferLongerNames() {
    return preferences.getBoolean(preferLongerNames, getDefaultAsBoolean(preferLongerNames));
    }

    public String getFieldNamePrefix() {
    return preferences.get(fieldNamePrefix, getDefaultAsString(fieldNamePrefix));
    }

    public String getFieldNameSuffix() {
    return preferences.get(fieldNameSuffix, getDefaultAsString(fieldNameSuffix));
    }

    public String getStaticFieldNamePrefix() {
    return preferences.get(staticFieldNamePrefix, getDefaultAsString(staticFieldNamePrefix));
    }

    public String getStaticFieldNameSuffix() {
    return preferences.get(staticFieldNameSuffix, getDefaultAsString(staticFieldNameSuffix));
    }

    public String getParameterNamePrefix() {
    return preferences.get(parameterNamePrefix, getDefaultAsString(parameterNamePrefix));
    }

    public String getParameterNameSuffix() {
    return preferences.get(parameterNameSuffix, getDefaultAsString(parameterNameSuffix));
    }

    public String getLocalVarNamePrefix() {
    return preferences.get(localVarNamePrefix, getDefaultAsString(localVarNamePrefix));
    }

    public String getLocalVarNameSuffix() {
    return preferences.get(localVarNameSuffix, getDefaultAsString(localVarNameSuffix));
    }

    public boolean qualifyFieldAccess() {
    return preferences.getBoolean(qualifyFieldAccess, getDefaultAsBoolean(qualifyFieldAccess));
    }

    public boolean useIsForBooleanGetters() {
    return preferences.getBoolean(useIsForBooleanGetters, getDefaultAsBoolean(useIsForBooleanGetters));
    }

    public boolean addOverrideAnnotation() {
    return preferences.getBoolean(addOverrideAnnotation, getDefaultAsBoolean(addOverrideAnnotation));
    }

    public boolean makeLocalVarsFinal() {
    return preferences.getBoolean(makeLocalVarsFinal, getDefaultAsBoolean(makeLocalVarsFinal));
    }

    // Alignment ----------------------------------------------------
    
    public boolean alignMultilineMethodParams() {
    return preferences.getBoolean(alignMultilineMethodParams, getDefaultAsBoolean(alignMultilineMethodParams));
    }

    public boolean alignMultilineCallArgs() {
    return preferences.getBoolean(alignMultilineCallArgs, getDefaultAsBoolean(alignMultilineCallArgs));
    }

    public boolean alignMultilineAnnotationArgs() {
    return preferences.getBoolean(alignMultilineAnnotationArgs, getDefaultAsBoolean(alignMultilineAnnotationArgs));
    }

    public boolean alignMultilineImplements() {
    return preferences.getBoolean(alignMultilineImplements, getDefaultAsBoolean(alignMultilineImplements));
    }

    public boolean alignMultilineThrows() {
    return preferences.getBoolean(alignMultilineThrows, getDefaultAsBoolean(alignMultilineThrows));
    }

    public boolean alignMultilineParenthesized() {
    return preferences.getBoolean(alignMultilineParenthesized, getDefaultAsBoolean(alignMultilineParenthesized));
    }

    public boolean alignMultilineBinaryOp() {
    return preferences.getBoolean(alignMultilineBinaryOp, getDefaultAsBoolean(alignMultilineBinaryOp));
    }

    public boolean alignMultilineTernaryOp() {
    return preferences.getBoolean(alignMultilineTernaryOp, getDefaultAsBoolean(alignMultilineTernaryOp));
    }

    public boolean alignMultilineAssignment() {
    return preferences.getBoolean(alignMultilineAssignment, getDefaultAsBoolean(alignMultilineAssignment));
    }

    public boolean alignMultilineFor() {
    return preferences.getBoolean(alignMultilineFor, getDefaultAsBoolean(alignMultilineFor));
    }

    public boolean alignMultilineArrayInit() {
    return preferences.getBoolean(alignMultilineArrayInit, getDefaultAsBoolean(alignMultilineArrayInit));
    }

    public boolean placeElseOnNewLine() {
    return preferences.getBoolean(placeElseOnNewLine, getDefaultAsBoolean(placeElseOnNewLine));
    }

    public boolean placeWhileOnNewLine() {
    return preferences.getBoolean(placeWhileOnNewLine, getDefaultAsBoolean(placeWhileOnNewLine));
    }

    public boolean placeCatchOnNewLine() {
    return preferences.getBoolean(placeCatchOnNewLine, getDefaultAsBoolean(placeCatchOnNewLine));
    }

    public boolean placeFinallyOnNewLine() {
    return preferences.getBoolean(placeFinallyOnNewLine, getDefaultAsBoolean(placeFinallyOnNewLine));
    }
    
    public boolean placeNewLineAfterModifiers() {
    return preferences.getBoolean(placeNewLineAfterModifiers, getDefaultAsBoolean(placeNewLineAfterModifiers));
    }

    // Wrapping ----------------------------------------------------------------
    
    public WrapStyle wrapExtendsImplementsKeyword() {
    String wrap = preferences.get(wrapExtendsImplementsKeyword, getDefaultAsString(wrapExtendsImplementsKeyword));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapExtendsImplementsList() {
    String wrap = preferences.get(wrapExtendsImplementsList, getDefaultAsString(wrapExtendsImplementsList));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapMethodParams() {
    String wrap = preferences.get(wrapMethodParams, getDefaultAsString(wrapMethodParams));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapThrowsKeyword() {
    String wrap = preferences.get(wrapThrowsKeyword, getDefaultAsString(wrapThrowsKeyword));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapThrowsList() {
    String wrap = preferences.get(wrapThrowsList, getDefaultAsString(wrapThrowsList));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapMethodCallArgs() {
    String wrap = preferences.get(wrapMethodCallArgs, getDefaultAsString(wrapMethodCallArgs));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapAnnotationArgs() {
    String wrap = preferences.get(wrapAnnotationArgs, getDefaultAsString(wrapAnnotationArgs));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapChainedMethodCalls() {
    String wrap = preferences.get(wrapChainedMethodCalls, getDefaultAsString(wrapChainedMethodCalls));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapArrayInit() {
    String wrap = preferences.get(wrapArrayInit, getDefaultAsString(wrapArrayInit));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapFor() {
    String wrap = preferences.get(wrapFor, getDefaultAsString(wrapFor));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapForStatement() {
    String wrap = preferences.get(wrapForStatement, getDefaultAsString(wrapForStatement));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapIfStatement() {
    String wrap = preferences.get(wrapIfStatement, getDefaultAsString(wrapIfStatement));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapWhileStatement() {
    String wrap = preferences.get(wrapWhileStatement, getDefaultAsString(wrapWhileStatement));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapDoWhileStatement() {
    String wrap = preferences.get(wrapDoWhileStatement, getDefaultAsString(wrapDoWhileStatement));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapAssert() {
    String wrap = preferences.get(wrapAssert, getDefaultAsString(wrapAssert));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapEnumConstants() {
    String wrap = preferences.get(wrapEnumConstants, getDefaultAsString(wrapEnumConstants));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapAnnotations() {
    String wrap = preferences.get(wrapAnnotations, getDefaultAsString(wrapAnnotations));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapBinaryOps() {
    String wrap = preferences.get(wrapBinaryOps, getDefaultAsString(wrapBinaryOps));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapTernaryOps() {
    String wrap = preferences.get(wrapTernaryOps, getDefaultAsString(wrapTernaryOps));
    return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapAssignOps() {
    String wrap = preferences.get(wrapAssignOps, getDefaultAsString(wrapAssignOps));
    return WrapStyle.valueOf(wrap);
    }

    // Blank lines -------------------------------------------------------------
    
    public int getBlankLinesBeforePackage() {
    return preferences.getInt(blankLinesBeforePackage, getDefaultAsInt(blankLinesBeforePackage));
    }

    public int getBlankLinesAfterPackage() {
    return preferences.getInt(blankLinesAfterPackage, getDefaultAsInt(blankLinesAfterPackage));
    }

    public int getBlankLinesBeforeImports() {
    return preferences.getInt(blankLinesBeforeImports, getDefaultAsInt(blankLinesBeforeImports));
    }

    public int getBlankLinesAfterImports() {
    return preferences.getInt(blankLinesAfterImports, getDefaultAsInt(blankLinesAfterImports));
    }

    public int getBlankLinesBeforeClass() {
    return preferences.getInt(blankLinesBeforeClass, getDefaultAsInt(blankLinesBeforeClass));
    }

    public int getBlankLinesAfterClass() {
    return preferences.getInt(blankLinesAfterClass, getDefaultAsInt(blankLinesAfterClass));
    }

    public int getBlankLinesAfterClassHeader() {
    return preferences.getInt(blankLinesAfterClassHeader, getDefaultAsInt(blankLinesAfterClassHeader));
    }

    public int getBlankLinesBeforeFields() {
    return preferences.getInt(blankLinesBeforeFields, getDefaultAsInt(blankLinesBeforeFields));
    }

    public int getBlankLinesAfterFields() {
    return preferences.getInt(blankLinesAfterFields, getDefaultAsInt(blankLinesAfterFields));
    }

    public int getBlankLinesBeforeMethods() {
    return preferences.getInt(blankLinesBeforeMethods, getDefaultAsInt(blankLinesBeforeMethods));
    }

    public int getBlankLinesAfterMethods() {
    return preferences.getInt(blankLinesAfterMethods, getDefaultAsInt(blankLinesAfterMethods));
    }

    // Spaces ------------------------------------------------------------------
    
    public boolean spaceBeforeWhile() {
    return preferences.getBoolean(spaceBeforeWhile, getDefaultAsBoolean(spaceBeforeWhile));
    }

    public boolean spaceBeforeElse() {
    return preferences.getBoolean(spaceBeforeElse, getDefaultAsBoolean(spaceBeforeElse));
    }

    public boolean spaceBeforeCatch() {
    return preferences.getBoolean(spaceBeforeCatch, getDefaultAsBoolean(spaceBeforeCatch));
    }

    public boolean spaceBeforeFinally() {
    return preferences.getBoolean(spaceBeforeFinally, getDefaultAsBoolean(spaceBeforeFinally));
    }

    public boolean spaceBeforeMethodDeclParen() {
    return preferences.getBoolean(spaceBeforeMethodDeclParen, getDefaultAsBoolean(spaceBeforeMethodDeclParen));
    }

    public boolean spaceBeforeMethodCallParen() {
    return preferences.getBoolean(spaceBeforeMethodCallParen, getDefaultAsBoolean(spaceBeforeMethodCallParen));
    }

    public boolean spaceBeforeIfParen() {
    return preferences.getBoolean(spaceBeforeIfParen, getDefaultAsBoolean(spaceBeforeIfParen));
    }

    public boolean spaceBeforeForParen() {
    return preferences.getBoolean(spaceBeforeForParen, getDefaultAsBoolean(spaceBeforeForParen));
    }

    public boolean spaceBeforeWhileParen() {
    return preferences.getBoolean(spaceBeforeWhileParen, getDefaultAsBoolean(spaceBeforeWhileParen));
    }

    public boolean spaceBeforeCatchParen() {
    return preferences.getBoolean(spaceBeforeCatchParen, getDefaultAsBoolean(spaceBeforeCatchParen));
    }

    public boolean spaceBeforeSwitchParen() {
    return preferences.getBoolean(spaceBeforeSwitchParen, getDefaultAsBoolean(spaceBeforeSwitchParen));
    }

    public boolean spaceBeforeSynchronizedParen() {
    return preferences.getBoolean(spaceBeforeSynchronizedParen, getDefaultAsBoolean(spaceBeforeSynchronizedParen));
    }

    public boolean spaceBeforeAnnotationParen() {
    return preferences.getBoolean(spaceBeforeAnnotationParen, getDefaultAsBoolean(spaceBeforeAnnotationParen));
    }

    public boolean spaceAroundUnaryOps() {
    return preferences.getBoolean(spaceAroundUnaryOps, getDefaultAsBoolean(spaceAroundUnaryOps));
    }

    public boolean spaceAroundBinaryOps() {
    return preferences.getBoolean(spaceAroundBinaryOps, getDefaultAsBoolean(spaceAroundBinaryOps));
    }

    public boolean spaceAroundTernaryOps() {
    return preferences.getBoolean(spaceAroundTernaryOps, getDefaultAsBoolean(spaceAroundTernaryOps));
    }

    public boolean spaceAroundAssignOps() {
    return preferences.getBoolean(spaceAroundAssignOps, getDefaultAsBoolean(spaceAroundAssignOps));
    }

    public boolean spaceBeforeClassDeclLeftBrace() {
    return preferences.getBoolean(spaceBeforeClassDeclLeftBrace, getDefaultAsBoolean(spaceBeforeClassDeclLeftBrace));
    }

    public boolean spaceBeforeMethodDeclLeftBrace() {
    return preferences.getBoolean(spaceBeforeMethodDeclLeftBrace, getDefaultAsBoolean(spaceBeforeMethodDeclLeftBrace));
    }

    public boolean spaceBeforeIfLeftBrace() {
    return preferences.getBoolean(spaceBeforeIfLeftBrace, getDefaultAsBoolean(spaceBeforeIfLeftBrace));
    }

    public boolean spaceBeforeElseLeftBrace() {
    return preferences.getBoolean(spaceBeforeElseLeftBrace, getDefaultAsBoolean(spaceBeforeElseLeftBrace));
    }

    public boolean spaceBeforeWhileLeftBrace() {
    return preferences.getBoolean(spaceBeforeWhileLeftBrace, getDefaultAsBoolean(spaceBeforeWhileLeftBrace));
    }

    public boolean spaceBeforeForLeftBrace() {
    return preferences.getBoolean(spaceBeforeForLeftBrace, getDefaultAsBoolean(spaceBeforeForLeftBrace));
    }

    public boolean spaceBeforeDoLeftBrace() {
    return preferences.getBoolean(spaceBeforeDoLeftBrace, getDefaultAsBoolean(spaceBeforeDoLeftBrace));
    }

    public boolean spaceBeforeSwitchLeftBrace() {
    return preferences.getBoolean(spaceBeforeSwitchLeftBrace, getDefaultAsBoolean(spaceBeforeSwitchLeftBrace));
    }

    public boolean spaceBeforeTryLeftBrace() {
    return preferences.getBoolean(spaceBeforeTryLeftBrace, getDefaultAsBoolean(spaceBeforeTryLeftBrace));
    }

    public boolean spaceBeforeCatchLeftBrace() {
    return preferences.getBoolean(spaceBeforeCatchLeftBrace, getDefaultAsBoolean(spaceBeforeCatchLeftBrace));
    }

    public boolean spaceBeforeFinallyLeftBrace() {
    return preferences.getBoolean(spaceBeforeFinallyLeftBrace, getDefaultAsBoolean(spaceBeforeFinallyLeftBrace));
    }

    public boolean spaceBeforeSynchronizedLeftBrace() {
    return preferences.getBoolean(spaceBeforeSynchronizedLeftBrace, getDefaultAsBoolean(spaceBeforeSynchronizedLeftBrace));
    }

    public boolean spaceBeforeStaticInitLeftBrace() {
    return preferences.getBoolean(spaceBeforeStaticInitLeftBrace, getDefaultAsBoolean(spaceBeforeStaticInitLeftBrace));
    }

    public boolean spaceBeforeArrayInitLeftBrace() {
    return preferences.getBoolean(spaceBeforeArrayInitLeftBrace, getDefaultAsBoolean(spaceBeforeArrayInitLeftBrace));
    }

    public boolean spaceWithinParens() {
    return preferences.getBoolean(spaceWithinParens, getDefaultAsBoolean(spaceWithinParens));
    }

    public boolean spaceWithinMethodDeclParens() {
    return preferences.getBoolean(spaceWithinMethodDeclParens, getDefaultAsBoolean(spaceWithinMethodDeclParens));
    }

    public boolean spaceWithinMethodCallParens() {
    return preferences.getBoolean(spaceWithinMethodCallParens, getDefaultAsBoolean(spaceWithinMethodCallParens));
    }

    public boolean spaceWithinIfParens() {
    return preferences.getBoolean(spaceWithinIfParens, getDefaultAsBoolean(spaceWithinIfParens));
    }

    public boolean spaceWithinForParens() {
    return preferences.getBoolean(spaceWithinForParens, getDefaultAsBoolean(spaceWithinForParens));
    }

    public boolean spaceWithinWhileParens() {
    return preferences.getBoolean(spaceWithinWhileParens, getDefaultAsBoolean(spaceWithinWhileParens));
    }

    public boolean spaceWithinSwitchParens() {
    return preferences.getBoolean(spaceWithinSwitchParens, getDefaultAsBoolean(spaceWithinSwitchParens));
    }

    public boolean spaceWithinCatchParens() {
    return preferences.getBoolean(spaceWithinCatchParens, getDefaultAsBoolean(spaceWithinCatchParens));
    }

    public boolean spaceWithinSynchronizedParens() {
    return preferences.getBoolean(spaceWithinSynchronizedParens, getDefaultAsBoolean(spaceWithinSynchronizedParens));
    }

    public boolean spaceWithinTypeCastParens() {
    return preferences.getBoolean(spaceWithinTypeCastParens, getDefaultAsBoolean(spaceWithinTypeCastParens));
    }

    public boolean spaceWithinAnnotationParens() {
    return preferences.getBoolean(spaceWithinAnnotationParens, getDefaultAsBoolean(spaceWithinAnnotationParens));
    }

    public boolean spaceWithinBraces() {
    return preferences.getBoolean(spaceWithinBraces, getDefaultAsBoolean(spaceWithinBraces));
    }

    public boolean spaceWithinArrayInitBrackets() {
    return preferences.getBoolean(spaceWithinArrayInitBrackets, getDefaultAsBoolean(spaceWithinArrayInitBrackets));
    }

    public boolean spaceBeforeComma() {
    return preferences.getBoolean(spaceBeforeComma, getDefaultAsBoolean(spaceBeforeComma));
    }

    public boolean spaceAfterComma() {
    return preferences.getBoolean(spaceAfterComma, getDefaultAsBoolean(spaceAfterComma));
    }

    public boolean spaceBeforeSemi() {
    return preferences.getBoolean(spaceBeforeSemi, getDefaultAsBoolean(spaceBeforeSemi));
    }

    public boolean spaceAfterSemi() {
    return preferences.getBoolean(spaceAfterSemi, getDefaultAsBoolean(spaceAfterSemi));
    }

    public boolean spaceBeforeColon() {
    return preferences.getBoolean(spaceBeforeColon, getDefaultAsBoolean(spaceBeforeColon));
    }

    public boolean spaceAfterColon() {
    return preferences.getBoolean(spaceAfterColon, getDefaultAsBoolean(spaceAfterColon));
    }

    public boolean spaceAfterTypeCast() {
    return preferences.getBoolean(spaceAfterTypeCast, getDefaultAsBoolean(spaceAfterTypeCast));
    }

     */
    // Spaces -----------------------------------------------------------------
    public boolean addSpaceAroundOperators() {
        return preferences.getBoolean(addSpaceAroundOperators, getDefaultAsBoolean(addSpaceAroundOperators));
    }

    public boolean removeSpaceInsideParens() {
        return preferences.getBoolean(removeSpaceInParens, getDefaultAsBoolean(removeSpaceInParens));
    }

    public boolean addSpaceAfterComma() {
        return preferences.getBoolean(addSpaceAfterComma, getDefaultAsBoolean(addSpaceAfterComma));
    }

    public boolean removeSpaceBeforeSep() {
        return preferences.getBoolean(removeSpaceBeforeSep, getDefaultAsBoolean(removeSpaceBeforeSep));
    }

    public boolean removeSpaceInParamAssign() {
        return preferences.getBoolean(removeSpaceInParamAssign, getDefaultAsBoolean(removeSpaceInParamAssign));
    }

    public boolean collapseSpaces() {
        return preferences.getBoolean(collapseSpaces, getDefaultAsBoolean(collapseSpaces));
    }

    // Imports -----------------------------------------------------------------
    public boolean formatImports() {
        return preferences.getBoolean(formatImports, getDefaultAsBoolean(formatImports));
    }

    public boolean oneImportPerLine() {
        return preferences.getBoolean(oneImportPerLine, getDefaultAsBoolean(oneImportPerLine));
    }

    public boolean removeDuplicates() {
        return preferences.getBoolean(removeDuplicates, getDefaultAsBoolean(removeDuplicates));
    }

    public boolean systemLibsFirst() {
        return preferences.getBoolean(systemLibsFirst, getDefaultAsBoolean(systemLibsFirst));
    }

    public boolean preferSymbolImports() {
        return preferences.getBoolean(preferSymbolImports, getDefaultAsBoolean(preferSymbolImports));
    }

    public boolean sortImports() {
        return preferences.getBoolean(sortImports, getDefaultAsBoolean(sortImports));
    }

    public boolean separateFromImps() {
        return preferences.getBoolean(separateFromImps, getDefaultAsBoolean(separateFromImps));
    }

    public ImportCleanupStyle cleanupImports() {
        String cleanup = preferences.get(cleanupUnusedImports, getDefaultAsString(cleanupUnusedImports));
        return ImportCleanupStyle.valueOf(cleanup);
    }

    public String[] getPackagesForStarImport() {
        return null;
    }

    // Nested classes ----------------------------------------------------------
    public enum WrapStyle {
        WRAP_ALWAYS,
        WRAP_IF_LONG,
        WRAP_NEVER
    }

    public enum ImportCleanupStyle {
        LEAVE_ALONE,
        COMMENT_OUT,
        DELETE
    }

    // Communication with non public packages ----------------------------------
    private static class Producer implements FmtOptions.CodeStyleProducer {
        @Override
        public CodeStyle create(Preferences preferences) {
            return new CodeStyle(preferences);
        }
    }
}
