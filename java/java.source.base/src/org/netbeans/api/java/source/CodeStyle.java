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

package org.netbeans.api.java.source;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.text.Document;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.ui.FmtOptions;
import static org.netbeans.modules.java.ui.FmtOptions.*;
import org.openide.filesystems.FileObject;

/** 
 *  XXX make sure the getters get the defaults from somewhere
 *  XXX add support for profiles
 *  XXX get the preferences node from somewhere else in odrer to be able not to
 *      use the getters and to be able to write to it.
 * 
 * @author Dusan Balek
 */
public final class CodeStyle {
    
    static {
        FmtOptions.codeStyleProducer = new Producer();
    }
    
    private Preferences preferences;
    
    private CodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Gets <code>CodeStyle</code> for files in the given project.
     *
     * <p>Please see the other two <code>getDefault</code> methods as they are
     * the preferred way of getting <code>CodeStyle</code>.
     *
     * @param project The project to get the <code>CodeStyle</code> for.
     * @return The current code style that would be used by documents opened
     *   from files belonging to the <code>project</code>.
     *
     * @deprecated Please use {@link #getDefault(javax.swing.text.Document)}
     *   or {@link #getDefault(org.openide.filesystems.FileObject)} respectively.
     */
    @Deprecated
    public static CodeStyle getDefault(Project project) {
        return getDefault(project.getProjectDirectory());
    }
    
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
    public static synchronized CodeStyle getDefault(FileObject file) {
        Preferences prefs = CodeStylePreferences.get(file, JavacParser.MIME_TYPE).getPreferences();
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
    public static synchronized CodeStyle getDefault(Document doc) {
        Preferences prefs = CodeStylePreferences.get(doc, JavacParser.MIME_TYPE).getPreferences();
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

    public boolean makeParametersFinal() {
        return preferences.getBoolean(makeParametersFinal, getDefaultAsBoolean(makeParametersFinal));
    }

    /**
     * Returns an information about the desired grouping of class members.
     * @since 0.96
     */
    public MemberGroups getClassMemberGroups() {
        return new MemberGroups(preferences.get(classMembersOrder, getDefaultAsString(classMembersOrder)),
                preferences.getBoolean(sortMembersByVisibility, getDefaultAsBoolean(sortMembersByVisibility))
                ? preferences.get(visibilityOrder, getDefaultAsString(visibilityOrder)) : null);
    }
    
    /**
     * @since 0.125 
     */
    public boolean keepGettersAndSettersTogether() {
        return preferences.getBoolean(keepGettersAndSettersTogether, getDefaultAsBoolean(keepGettersAndSettersTogether));
    }

    /**
     * @since 0.125 
     */
    public boolean sortMembersInGroupsAlphabetically() {
        return preferences.getBoolean(sortMembersInGroups, getDefaultAsBoolean(sortMembersInGroups));
    }

    /**
     * Determines whether the dependencies between members must be used when sorting.
     * It returns true only if some sorting option is available (default: off) and the dependency
     * inspection is enabled (default: true).
     * <p/>
     * Changing member order without looking for dependencies may result in incorrect code. A field
     * must be declared textually first, and only then it can be referenced by simple name from field
     * initializers and class/instance initializers - see defect #249199.
     * @since 2.3
     */
    public boolean computeMemberDependencies() {
        if (sortMembersInGroupsAlphabetically() || preferences.getBoolean(sortMembersByVisibility, getDefaultAsBoolean(sortMembersByVisibility))) {
            return preferences.getBoolean(sortUsesDependencies, getDefaultAsBoolean(sortUsesDependencies));
        }
        return false;
    }

    /**
     * Returns an information about the desired insertion point of a new class member.
     * @since 0.96
     */
    public InsertionPoint getClassMemberInsertionPoint() {
        String point = preferences.get(classMemberInsertionPoint, getDefaultAsString(classMemberInsertionPoint));
        return InsertionPoint.valueOf(point);
    }
    
    // Alignment and braces ----------------------------------------------------
    
    /**
     * @since 2.23 
     */
    public BracePlacement getModuleDeclBracePlacement() {
        String placement = preferences.get(moduleDeclBracePlacement, getDefaultAsString(moduleDeclBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getClassDeclBracePlacement() {
        String placement = preferences.get(classDeclBracePlacement, getDefaultAsString(classDeclBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getMethodDeclBracePlacement() {
        String placement = preferences.get(methodDeclBracePlacement, getDefaultAsString(methodDeclBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getOtherBracePlacement() {
        String placement = preferences.get(otherBracePlacement, getDefaultAsString(otherBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public boolean specialElseIf() {
        return preferences.getBoolean(specialElseIf, getDefaultAsBoolean(specialElseIf));
    }

    public BracesGenerationStyle redundantIfBraces() {
        String redundant = preferences.get(redundantIfBraces, getDefaultAsString(redundantIfBraces));
        return BracesGenerationStyle.valueOf(redundant);
    }

    public BracesGenerationStyle redundantForBraces() {
        String redundant = preferences.get(redundantForBraces, getDefaultAsString(redundantForBraces));
        return BracesGenerationStyle.valueOf(redundant);
    }

    public BracesGenerationStyle redundantWhileBraces() {
        String redundant = preferences.get(redundantWhileBraces, getDefaultAsString(redundantWhileBraces));
        return BracesGenerationStyle.valueOf(redundant);
    }

    public BracesGenerationStyle redundantDoWhileBraces() {
        String redundant = preferences.get(redundantDoWhileBraces, getDefaultAsString(redundantDoWhileBraces));
        return BracesGenerationStyle.valueOf(redundant);
    }

    /**
     * @since 2.23 
     */
    public boolean alignMultilineExports() {
        return preferences.getBoolean(alignMultilineExports, getDefaultAsBoolean(alignMultilineExports));
    }

    /**
     * @since 2.25 
     */
    public boolean alignMultilineOpens() {
        return preferences.getBoolean(alignMultilineOpens, getDefaultAsBoolean(alignMultilineOpens));
    }

    /**
     * @since 2.25
     */
    public boolean alignMultilineProvides() {
        return preferences.getBoolean(alignMultilineProvides, getDefaultAsBoolean(alignMultilineProvides));
    }

    public boolean alignMultilineMethodParams() {
        return preferences.getBoolean(alignMultilineMethodParams, getDefaultAsBoolean(alignMultilineMethodParams));
    }

    /**
     * @since 0.113
     */
    public boolean alignMultilineLambdaParams() {
        return preferences.getBoolean(alignMultilineLambdaParams, getDefaultAsBoolean(alignMultilineLambdaParams));
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

    /**
     * @since 0.67
     */
    public boolean alignMultilineTryResources() {
        return preferences.getBoolean(alignMultilineTryResources, getDefaultAsBoolean(alignMultilineTryResources));
    }

    public boolean alignMultilineDisjunctiveCatchTypes() {
        return preferences.getBoolean(alignMultilineDisjunctiveCatchTypes, getDefaultAsBoolean(alignMultilineDisjunctiveCatchTypes));
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
    
    /**
     * @since 2.23 
     */
    public WrapStyle wrapProvidesWithKeyword() {
        String wrap = preferences.get(wrapProvidesWithKeyword, getDefaultAsString(wrapProvidesWithKeyword));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 2.25 
     */
    public WrapStyle wrapProvidesWithList() {
        String wrap = preferences.get(wrapProvidesWithList, getDefaultAsString(wrapProvidesWithList));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 2.23 
     */
    public WrapStyle wrapExportsToKeyword() {
        String wrap = preferences.get(wrapExportsToKeyword, getDefaultAsString(wrapExportsToKeyword));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 2.23 
     */
    public WrapStyle wrapExportsToList() {
        String wrap = preferences.get(wrapExportsToList, getDefaultAsString(wrapExportsToList));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 2.25
     */
    public WrapStyle wrapOpensToKeyword() {
        String wrap = preferences.get(wrapOpensToKeyword, getDefaultAsString(wrapOpensToKeyword));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 2.25
     */
    public WrapStyle wrapOpensToList() {
        String wrap = preferences.get(wrapOpensToList, getDefaultAsString(wrapOpensToList));
        return WrapStyle.valueOf(wrap);
    }

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

    /**
     * @since 0.113
     */
    public WrapStyle wrapLambdaParams() {
        String wrap = preferences.get(wrapLambdaParams, getDefaultAsString(wrapLambdaParams));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 0.113
     */
    public WrapStyle wrapLambdaArrow() {
        String wrap = preferences.get(wrapLambdaArrow, getDefaultAsString(wrapLambdaArrow));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 0.113
     */
    public boolean wrapAfterLambdaArrow() {
        return preferences.getBoolean(wrapAfterLambdaArrow, getDefaultAsBoolean(wrapAfterLambdaArrow));
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

    public boolean wrapAfterDotInChainedMethodCalls() {
        return preferences.getBoolean(wrapAfterDotInChainedMethodCalls, getDefaultAsBoolean(wrapAfterDotInChainedMethodCalls));
    }

    public WrapStyle wrapArrayInit() {
        String wrap = preferences.get(wrapArrayInit, getDefaultAsString(wrapArrayInit));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 0.67
     */
    public WrapStyle wrapTryResources() {
        String wrap = preferences.get(wrapTryResources, getDefaultAsString(wrapTryResources));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapDisjunctiveCatchTypes() {
        String wrap = preferences.get(wrapDisjunctiveCatchTypes, getDefaultAsString(wrapDisjunctiveCatchTypes));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 2.18
     */
    public boolean wrapAfterDisjunctiveCatchBar() {
        return preferences.getBoolean(wrapAfterDisjunctiveCatchBar, getDefaultAsBoolean(wrapAfterDisjunctiveCatchBar));
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

    /**
     * @since 0.120
     */
    public WrapStyle wrapCaseStatements() {
        String wrap = preferences.get(wrapCaseStatements, getDefaultAsString(wrapCaseStatements));
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

    public boolean wrapAfterBinaryOps() {
        return preferences.getBoolean(wrapAfterBinaryOps, getDefaultAsBoolean(wrapAfterBinaryOps));
    }

    public WrapStyle wrapTernaryOps() {
        String wrap = preferences.get(wrapTernaryOps, getDefaultAsString(wrapTernaryOps));
        return WrapStyle.valueOf(wrap);
    }

    public boolean wrapAfterTernaryOps() {
        return preferences.getBoolean(wrapAfterTernaryOps, getDefaultAsBoolean(wrapAfterTernaryOps));
    }

    public WrapStyle wrapAssignOps() {
        String wrap = preferences.get(wrapAssignOps, getDefaultAsString(wrapAssignOps));
        return WrapStyle.valueOf(wrap);
    }

    /**
     * @since 0.119
     */    
    public boolean wrapAfterAssignOps() {
        return preferences.getBoolean(wrapAfterAssignOps, getDefaultAsBoolean(wrapAfterAssignOps));
    }

    // Blank lines -------------------------------------------------------------
    
    /**
     * @since 0.118
     */
    public int getMaximumBlankLinesInDeclarations() {
        return preferences.getInt(blankLinesInDeclarations, getDefaultAsInt(blankLinesInDeclarations));
    }

    /**
     * @since 0.118
     */
    public int getMaximumBlankLinesInCode() {
        return preferences.getInt(blankLinesInCode, getDefaultAsInt(blankLinesInCode));
    }

    /**
     * @since 2.23 
     */
    public int getBlankLinesAfterModuleHeader() {
        return preferences.getInt(blankLinesAfterModuleHeader, getDefaultAsInt(blankLinesAfterModuleHeader));
    }

    /**
     * @since 2.23 
     */
    public int getBlankLinesBeforeModuleClosingBrace() {
        return preferences.getInt(blankLinesBeforeModuleClosingBrace, getDefaultAsInt(blankLinesBeforeModuleClosingBrace));
    }

    /**
     * @since 2.23 
     */
    public int getBlankLinesBeforeModuleDirectives() {
        return preferences.getInt(blankLinesBeforeModuleDirectives, getDefaultAsInt(blankLinesBeforeModuleDirectives));
    }

    /**
     * @since 2.23 
     */
    public int getBlankLinesAfterModuleDirectives() {
        return preferences.getInt(blankLinesAfterModuleDirectives, getDefaultAsInt(blankLinesAfterModuleDirectives));
    }

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

    public int getBlankLinesAfterAnonymousClassHeader() {
        return preferences.getInt(blankLinesAfterAnonymousClassHeader, getDefaultAsInt(blankLinesAfterAnonymousClassHeader));
    }

    /**
     * @since 0.140
     */
    public int getBlankLinesAfterEnumHeader() {
        return preferences.getInt(blankLinesAfterEnumHeader, getDefaultAsInt(blankLinesAfterEnumHeader));
    }

    /**
     * @since 0.106
     */
    public int getBlankLinesBeforeClassClosingBrace() {
        return preferences.getInt(blankLinesBeforeClassClosingBrace, getDefaultAsInt(blankLinesBeforeClassClosingBrace));
    }

    /**
     * @since 0.106
     */
    public int getBlankLinesBeforeAnonymousClassClosingBrace() {
        return preferences.getInt(blankLinesBeforeAnonymousClosingBrace, getDefaultAsInt(blankLinesBeforeAnonymousClosingBrace));
    }

    /**
     * @since 0.140
     */
    public int getBlankLinesBeforeEnumClosingBrace() {
        return preferences.getInt(blankLinesBeforeEnumClosingBrace, getDefaultAsInt(blankLinesBeforeEnumClosingBrace));
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

    /**
     * @since 0.67
     */
    public boolean spaceBeforeTryParen() {
        return preferences.getBoolean(spaceBeforeTryParen, getDefaultAsBoolean(spaceBeforeTryParen));
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

    public boolean spaceAroundAnnotationValueAssignOps() {
        return preferences.getBoolean(spaceAroundAnnotationValueAssignOps, getDefaultAsBoolean(spaceAroundAnnotationValueAssignOps));
    }

    /**
     * @since 0.113
     */
    public boolean spaceAroundLambdaArrow() {
        return preferences.getBoolean(spaceAroundLambdaArrow, getDefaultAsBoolean(spaceAroundLambdaArrow));
    }

    /**
     * @since 0.113
     */
    public boolean spaceAroundMethodReferenceDoubleColon() {
        return preferences.getBoolean(spaceAroundMethodReferenceDoubleColon, getDefaultAsBoolean(spaceAroundMethodReferenceDoubleColon));
    }

    /**
     * @since 2.23 
     */
    public boolean spaceBeforeModuleDeclLeftBrace() {
        return preferences.getBoolean(spaceBeforeModuleDeclLeftBrace, getDefaultAsBoolean(spaceBeforeModuleDeclLeftBrace));
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

    /**
     * @since 0.113
     */
    public boolean spaceWithinLambdaParens() {
        return preferences.getBoolean(spaceWithinLambdaParens, getDefaultAsBoolean(spaceWithinLambdaParens));
    }

    /**
     * @since 2.45
     */
    public boolean parensAroundSingularLambdaParam() {
        return preferences.getBoolean(parensAroundSingularLambdaParam, getDefaultAsBoolean(parensAroundSingularLambdaParam));
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

    /**
     * @since 0.67
     */
    public boolean spaceWithinTryParens() {
        return preferences.getBoolean(spaceWithinTryParens, getDefaultAsBoolean(spaceWithinTryParens));
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

    public boolean spaceWithinArrayIndexBrackets() {
        return preferences.getBoolean(spaceWithinArrayIndexBrackets, getDefaultAsBoolean(spaceWithinArrayIndexBrackets));
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

    // Imports -----------------------------------------------------------------

    /**
     * Returns whether to use single class import statements when adding imports.
     * @return <code>true</code> if the single class imports should be added,
     * <code>false</code> if the 'star' import of the entire package should be added
     */
    public boolean useSingleClassImport() {
        return preferences.getBoolean(useSingleClassImport, getDefaultAsBoolean(useSingleClassImport));
    }

    /**
     * Returns whether to use fully qualified class names when generating code.
     * @return <code>true</code> if the fully qualified class name should be generated
     * every time the class is used, <code>false</code> if the import statement
     * and the simple class name should be used instead.
     */
    public boolean useFQNs() {
        return preferences.getBoolean(useFQNs, getDefaultAsBoolean(useFQNs));
    }

    /**
     * Returns whether to create import statements for the inner classes.
     * @since 0.86
     */
    public boolean importInnerClasses() {
        return preferences.getBoolean(importInnerClasses, getDefaultAsBoolean(importInnerClasses));
    }

    /**
     * Returns whether to create static imports for the static class members.
     * @since 0.121
     */
    public boolean preferStaticImports() {
        return preferences.getBoolean(preferStaticImports, getDefaultAsBoolean(preferStaticImports));
    }

    /**
     * Returns the number of classes that have to be imported from a package
     * to convert the single class imports to a 'star' import of the entire package.
     */
    public int countForUsingStarImport() {
        boolean allow = preferences.getBoolean(allowConvertToStarImport, getDefaultAsBoolean(allowConvertToStarImport));
        return allow ? preferences.getInt(countForUsingStarImport, getDefaultAsInt(countForUsingStarImport)) : Integer.MAX_VALUE;
    }

    /**
     * Returns the number of static members that have to be imported from a class
     * to convert the single member static imports to a 'star' import of the entire class.
     */
    public int countForUsingStaticStarImport() {
        boolean allow = preferences.getBoolean(allowConvertToStaticStarImport, getDefaultAsBoolean(allowConvertToStaticStarImport));
        return allow ? preferences.getInt(countForUsingStaticStarImport, getDefaultAsInt(countForUsingStaticStarImport)) : Integer.MAX_VALUE;
    }

    /**
     * Returns the names of packages that should be always imported using the 'star'
     * import statements.
     */
    public String[] getPackagesForStarImport() {
        String pkgs = preferences.get(packagesForStarImport, getDefaultAsString(packagesForStarImport));
        if (pkgs == null || pkgs.length() == 0) {
            return new String[0];
        } else {
            return pkgs.trim().split("\\s*[,;]\\s*"); //NOI18N
        }
    }
    
    /**
     * Returns an information about the desired grouping of import statements.
     * Imported classes are grouped as per their packages. 
     * @since 0.86
     */
    public ImportGroups getImportGroups() {
        return new ImportGroups(preferences.get(importGroupsOrder, getDefaultAsString(importGroupsOrder)));
    }

    /**
     * Returns whether to separate the import groups with blank lines.
     * @since 0.86
     */
    public boolean separateImportGroups() {
        return preferences.getBoolean(separateImportGroups, getDefaultAsBoolean(separateImportGroups));
    }

    // Comments -----------------------------------------------------------------

    public boolean enableBlockCommentFormatting() {
        return preferences.getBoolean(enableCommentFormatting, getDefaultAsBoolean(enableCommentFormatting))
                && preferences.getBoolean(enableBlockCommentFormatting, getDefaultAsBoolean(enableBlockCommentFormatting));
    }

    public boolean enableJavadocFormatting() {
        return preferences.getBoolean(enableCommentFormatting, getDefaultAsBoolean(enableCommentFormatting));
    }

    public boolean wrapCommentText() {
        return preferences.getBoolean(wrapCommentText, getDefaultAsBoolean(wrapCommentText));
    }

    public boolean wrapOneLineComments() {
        return preferences.getBoolean(wrapOneLineComment, getDefaultAsBoolean(wrapOneLineComment));
    }

    public boolean preserveNewLinesInComments() {
        return preferences.getBoolean(preserveNewLinesInComments, getDefaultAsBoolean(preserveNewLinesInComments));
    }

    public boolean blankLineAfterJavadocDescription() {
        return preferences.getBoolean(blankLineAfterJavadocDescription, getDefaultAsBoolean(blankLineAfterJavadocDescription));
    }

    public boolean blankLineAfterJavadocParameterDescriptions() {
        return preferences.getBoolean(blankLineAfterJavadocParameterDescriptions, getDefaultAsBoolean(blankLineAfterJavadocParameterDescriptions));
    }

    public boolean blankLineAfterJavadocReturnTag() {
        return preferences.getBoolean(blankLineAfterJavadocReturnTag, getDefaultAsBoolean(blankLineAfterJavadocReturnTag));
    }

    public boolean generateParagraphTagOnBlankLines() {
        return preferences.getBoolean(generateParagraphTagOnBlankLines, getDefaultAsBoolean(generateParagraphTagOnBlankLines));
    }

    public boolean alignJavadocParameterDescriptions() {
        return preferences.getBoolean(alignJavadocParameterDescriptions, getDefaultAsBoolean(alignJavadocParameterDescriptions));
    }

    public boolean alignJavadocReturnDescription() {
        return preferences.getBoolean(alignJavadocReturnDescription, getDefaultAsBoolean(alignJavadocReturnDescription));
    }

    public boolean alignJavadocExceptionDescriptions() {
        return preferences.getBoolean(alignJavadocExceptionDescriptions, getDefaultAsBoolean(alignJavadocExceptionDescriptions));
    }

    // Nested classes ----------------------------------------------------------

    public enum BracePlacement {
        SAME_LINE,
        NEW_LINE,
        NEW_LINE_HALF_INDENTED,
        NEW_LINE_INDENTED
    }

    public enum BracesGenerationStyle {
        GENERATE,
        LEAVE_ALONE,
        ELIMINATE
    }
    
    public enum WrapStyle {
        WRAP_ALWAYS,
        WRAP_IF_LONG,
        WRAP_NEVER
    }
    
    public enum InsertionPoint {
        LAST_IN_CATEGORY,
        FIRST_IN_CATEGORY,
        ORDERED_IN_CATEGORY,
        CARET_LOCATION
    }
    
    /**
     * Provides an information about the desired grouping of import statements,
     * including group order.
     * @since 0.86
     */
    public static final class ImportGroups {

        private Info[] infos;
        private boolean separateStatic;

        private ImportGroups(String groups) {
            if (groups == null || groups.length() == 0) {
                this.infos = new Info[0];
            } else {
                String[] order = groups.trim().split("\\s*[,;]\\s*"); //NOI18N
                this.infos = new Info[order.length];
                for (int i = 0; i < order.length; i++) {
                    String imp = order[i];
                    Info info = new Info(i);
                    if (imp.startsWith("static ")) { //NOI18N
                        info.isStatic = true;
                        this.separateStatic = true;
                        imp = imp.substring(7);
                    }
                    info.prefix = imp.length() > 0 && !"*".equals(imp) ? imp + '.' : ""; //NOI18N                    
                    this.infos[i] = info;
                }
                Arrays.sort(this.infos, new Comparator<Info>() {
                    @Override
                    public int compare(Info o1, Info o2) {
                        int bal = o2.prefix.length() - o1.prefix.length();
                        return bal == 0 ? o1.prefix.compareTo(o2.prefix) : bal;
                    }
                });
            }
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ImportGroups[\n");
            for (Info i : infos) {
                sb.append("\t").append(i.prefix).append(": ");
                if (i.isStatic) {
                    sb.append("static");
                }
                sb.append("\n");
            }
            sb.append("]");
            return sb.toString();
        }

        /**
         * Returns the group number of the imported element. Imports with the same
         * number form a group. Groups with lower numbers should be positioned
         * higher in the import statement list. Imports within a group should
         * be sorted alphabetically.
         * @param name the imported element's package name
         * @param isStatic is the import static
         * @return the group number
         * @since 0.86
         */
        public int getGroupId(String name, boolean isStatic) {
            for (Info info : infos) {
                if (separateStatic ? info.check(name, isStatic) : info.check(name))
                    return info.groupId;
            }
            return infos.length;
        }

        private static final class Info {

            private int groupId;
            private boolean isStatic;
            private String prefix;
            
            private Info(int id) {
                this.groupId = id;
            }

            private boolean check(String s) {
                return s.startsWith(prefix);
            }

            private boolean check(String s, boolean b) {
                return isStatic == b && check(s);
            }
        }
    }

    /**
     * Provides an information about the desired grouping of class members,
     * including group order.
     * @since 0.96
     */
    public static final class MemberGroups {

        private Info[] infos;

        private MemberGroups(String groups, String visibility) {
            if (groups == null || groups.length() == 0) {
                this.infos = new Info[0];
            } else {
                String[] order = groups.trim().split("\\s*[,;]\\s*"); //NOI18N
                String[] visibilityOrder = visibility != null ? visibility.trim().split("\\s*[,;]\\s*") : new String[1]; //NOI18N
                this.infos = new Info[order.length * visibilityOrder.length];
                for (int i = 0; i < order.length; i++) {
                    String o = order[i];
                    boolean isStatic = false;
                    if (o.startsWith("STATIC ")) { //NOI18N
                        isStatic = true;
                        o = o.substring(7);
                    }
                    ElementKind kind = ElementKind.valueOf(o);
                    for (int j = 0; j < visibilityOrder.length; j++) {
                        int idx = i * visibilityOrder.length + j;
                        String vo = visibilityOrder[j];
                        Info info = new Info(idx);
                        info.ignoreVisibility = vo == null || !"DEFAULT".equals(vo); //NOI18N
                        info.mods = vo != null && !"DEFAULT".equals(vo) ? EnumSet.of(Modifier.valueOf(vo)) : EnumSet.noneOf(Modifier.class); //NOI18N
                        if (isStatic)
                            info.mods.add(Modifier.STATIC);
                        info.kind = kind;
                        this.infos[idx] = info;                        
                    }
                }
            }
        }

        /**
         * Returns the group number of the class member. Elements with the same
         * number form a group. Groups with lower numbers should be positioned
         * higher in the class member list.
         * @param tree the member tree
         * @return the group number
         * @since 0.96
         */
        public int getGroupId(Tree tree) {
            ElementKind kind = ElementKind.OTHER;
            Set<Modifier> modifiers = null;
            switch (tree.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    kind = ElementKind.CLASS;
                    modifiers = ((ClassTree)tree).getModifiers().getFlags();
                    break;
                case METHOD:
                    MethodTree mt = (MethodTree)tree;
                    if (mt.getName().contentEquals("<init>")) { //NOI18N
                        kind = ElementKind.CONSTRUCTOR;
                    } else {
                        kind = ElementKind.METHOD;
                    }
                    modifiers = mt.getModifiers().getFlags();
                    break;
                case VARIABLE:
                    kind = ElementKind.FIELD;
                    modifiers = ((VariableTree)tree).getModifiers().getFlags();
                    break;
                case BLOCK:
                    kind = ((BlockTree)tree).isStatic() ? ElementKind.STATIC_INIT : ElementKind.INSTANCE_INIT;
                    break;
            }
            for (Info info : infos) {
                if (info.check(kind, modifiers))
                    return info.groupId;
            }
            return infos.length;
        }

        /**
         * Returns the group number of the class member. Elements with the same
         * number form a group. Groups with lower numbers should be positioned
         * higher in the class member list.
         * @param element the member element
         * @return the group number
         * @since 0.96
         */
        public int getGroupId(Element element) {
            for (Info info : infos) {
                ElementKind kind = element.getKind();
                if (kind == ElementKind.ANNOTATION_TYPE || kind == ElementKind.ENUM || kind == ElementKind.INSTANCE_INIT)
                    kind = ElementKind.CLASS;
                if (info.check(kind, element.getModifiers()));
                    return info.groupId;
            }
            return infos.length;
        }
        
        /** 
         * Pretty-prints the group info
         * @return 
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("MemberGroups[\n");
            for (Info i : infos) {
                sb.append("\t").append(i.kind.toString()).append(": ");
                sb.append(i.mods);
                if (i.ignoreVisibility) {
                    sb.append(", ignore");
                }
                sb.append("\n");
            }
            sb.append("]");
            return sb.toString();
        }

        private static final class Info {

            private int groupId;
            private boolean ignoreVisibility;
            private Set<Modifier> mods;
            private ElementKind kind;
            
            private Info(int id) {
                this.groupId = id;
            }

            private boolean check(ElementKind kind, Set<Modifier> modifiers) {
                if (this.kind != kind)
                    return false;
                if (modifiers == null || modifiers.isEmpty())
                    return mods.isEmpty();
                if (!modifiers.containsAll(this.mods))
                    return false;
                EnumSet<Modifier> copy = EnumSet.copyOf(modifiers);
                copy.removeAll(this.mods);
                copy.retainAll(ignoreVisibility? EnumSet.of(Modifier.STATIC)
                        : EnumSet.of(Modifier.STATIC, Modifier.PUBLIC, Modifier.PRIVATE, Modifier.PROTECTED)); 
                return copy.isEmpty();
            }
        }
    }

    // Communication with non public packages ----------------------------------
    
    private static class Producer implements FmtOptions.CodeStyleProducer {

        @Override
        public CodeStyle create(Preferences preferences) {
            return new CodeStyle(preferences);
        }
        
    } 
    
}
