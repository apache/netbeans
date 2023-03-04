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
package org.netbeans.modules.java.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.source.CodeStyle;
import static org.netbeans.api.java.source.CodeStyle.*;

/**
 *
 * @author phrebejk
 */
public class FmtOptions {

    public static final String expandTabToSpaces = SimpleValueNames.EXPAND_TABS;
    public static final String tabSize = SimpleValueNames.TAB_SIZE;
    public static final String spacesPerTab = SimpleValueNames.SPACES_PER_TAB;
    public static final String indentSize = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final String continuationIndentSize = "continuationIndentSize"; //NOI18N
    public static final String labelIndent = "labelIndent"; //NOI18N
    public static final String absoluteLabelIndent = "absoluteLabelIndent"; //NOI18N
    public static final String indentTopLevelClassMembers = "indentTopLevelClassMembers"; //NOI18N
    public static final String indentCasesFromSwitch = "indentCasesFromSwitch"; //NOI18N
    public static final String rightMargin = SimpleValueNames.TEXT_LIMIT_WIDTH;
    
    public static final String addLeadingStarInComment = "addLeadingStarInComment"; //NOI18N

    public static final String preferLongerNames = "preferLongerNames"; //NOI18N
    public static final String fieldNamePrefix = "fieldNamePrefix"; //NOI18N
    public static final String fieldNameSuffix = "fieldNameSuffix"; //NOI18N
    public static final String staticFieldNamePrefix = "staticFieldNamePrefix"; //NOI18N
    public static final String staticFieldNameSuffix = "staticFieldNameSuffix"; //NOI18N
    public static final String parameterNamePrefix = "parameterNamePrefix"; //NOI18N
    public static final String parameterNameSuffix = "parameterNameSuffix"; //NOI18N
    public static final String localVarNamePrefix = "localVarNamePrefix"; //NOI18N
    public static final String localVarNameSuffix = "localVarNameSuffix"; //NOI18N
    public static final String qualifyFieldAccess = "qualifyFieldAccess"; //NOI18N
    public static final String useIsForBooleanGetters = "useIsForBooleanGetters"; //NOI18N
    public static final String addOverrideAnnotation = "addOverrideAnnotation"; //NOI18N
    public static final String makeLocalVarsFinal = "makeLocalVarsFinal"; //NOI18N
    public static final String makeParametersFinal = "makeParametersFinal"; //NOI18N
    public static final String classMembersOrder = "classMembersOrder"; //NOI18N
    public static final String sortMembersByVisibility = "sortMembersByVisibility"; //NOI18N
    public static final String visibilityOrder = "visibilityOrder"; //NOI18N
    public static final String keepGettersAndSettersTogether = "keepGettersAndSettersTogether"; //NOI18N
    public static final String sortMembersInGroups = "sortMembersInGroups"; //NOI18N
    public static final String classMemberInsertionPoint = "classMemberInsertionPoint"; //NOI18N
    /**
     * @since 2.3
     */
    public static final String sortUsesDependencies = "sortUsesDependencies"; // NO18N
    
    public static final String moduleDeclBracePlacement = "moduleDeclBracePlacement"; //NOI18N
    public static final String classDeclBracePlacement = "classDeclBracePlacement"; //NOI18N
    public static final String methodDeclBracePlacement = "methodDeclBracePlacement"; //NOI18N
    public static final String otherBracePlacement = "otherBracePlacement"; //NOI18N
    public static final String specialElseIf = "specialElseIf"; //NOI18N
    public static final String redundantIfBraces = "redundantIfBraces"; //NOI18N
    public static final String redundantForBraces = "redundantForBraces"; //NOI18N
    public static final String redundantWhileBraces = "redundantWhileBraces"; //NOI18N
    public static final String redundantDoWhileBraces = "redundantDoWhileBraces"; //NOI18N
    public static final String alignMultilineExports = "alignMultilineExports"; //NOI18N
    public static final String alignMultilineOpens = "alignMultilineOpens"; //NOI18N
    public static final String alignMultilineProvides = "alignMultilineProvides"; //NOI18N
    public static final String alignMultilineMethodParams = "alignMultilineMethodParams"; //NOI18N
    public static final String alignMultilineLambdaParams = "alignMultilineLambdaParams"; //NOI18N
    public static final String alignMultilineCallArgs = "alignMultilineCallArgs"; //NOI18N
    public static final String alignMultilineAnnotationArgs = "alignMultilineAnnotationArgs"; //NOI18N
    public static final String alignMultilineImplements = "alignMultilineImplements"; //NOI18N
    public static final String alignMultilineThrows = "alignMultilineThrows"; //NOI18N
    public static final String alignMultilineParenthesized = "alignMultilineParenthesized"; //NOI18N
    public static final String alignMultilineBinaryOp = "alignMultilineBinaryOp"; //NOI18N
    public static final String alignMultilineTernaryOp = "alignMultilineTernaryOp"; //NOI18N
    public static final String alignMultilineAssignment = "alignMultilineAssignment"; //NOI18N
    public static final String alignMultilineTryResources = "alignMultilineTryResources"; //NOI18N
    public static final String alignMultilineDisjunctiveCatchTypes = "alignMultilineDisjunctiveCatchTypes"; //NOI18N
    public static final String alignMultilineFor = "alignMultilineFor"; //NOI18N
    public static final String alignMultilineArrayInit = "alignMultilineArrayInit"; //NOI18N
    public static final String placeElseOnNewLine = "placeElseOnNewLine"; //NOI18N
    public static final String placeWhileOnNewLine = "placeWhileOnNewLine"; //NOI18N
    public static final String placeCatchOnNewLine = "placeCatchOnNewLine"; //NOI18N
    public static final String placeFinallyOnNewLine = "placeFinallyOnNewLine"; //NOI18N
    public static final String placeNewLineAfterModifiers = "placeNewLineAfterModifiers"; //NOI18N
    
    public static final String wrapProvidesWithKeyword = "wrapProvidesWithKeyword"; //NOI18N
    public static final String wrapProvidesWithList = "wrapProvidesWithList"; //NOI18N
    public static final String wrapExportsToKeyword = "wrapExportsToKeyword"; //NOI18N
    public static final String wrapExportsToList = "wrapExportsToList"; //NOI18N
    public static final String wrapOpensToKeyword = "wrapOpensToKeyword"; //NOI18N
    public static final String wrapOpensToList = "wrapOpensToList"; //NOI18N
    public static final String wrapExtendsImplementsKeyword = "wrapExtendsImplementsKeyword"; //NOI18N
    public static final String wrapExtendsImplementsList = "wrapExtendsImplementsList"; //NOI18N
    public static final String wrapMethodParams = "wrapMethodParams"; //NOI18N
    public static final String wrapLambdaParams = "wrapLambdaParams"; //NOI18N
    public static final String wrapLambdaArrow = "wrapLambdaArrow"; //NOI18N
    public static final String wrapAfterLambdaArrow = "wrapAfterLambdaArrow"; //NOI18N
    public static final String wrapThrowsKeyword = "wrapThrowsKeyword"; //NOI18N
    public static final String wrapThrowsList = "wrapThrowsList"; //NOI18N
    public static final String wrapMethodCallArgs = "wrapMethodCallArgs"; //NOI18N
    public static final String wrapAnnotationArgs = "wrapAnnotationArgs"; //NOI18N
    public static final String wrapChainedMethodCalls = "wrapChainedMethodCalls"; //NOI18N
    public static final String wrapAfterDotInChainedMethodCalls = "wrapAfterDotInChainedMethodCalls"; //NOI18N
    public static final String wrapArrayInit = "wrapArrayInit"; //NOI18N
    public static final String wrapTryResources = "wrapTryResources"; //NOI18N
    public static final String wrapDisjunctiveCatchTypes = "wrapDisjunctiveCatchTypes"; //NOI18N
    public static final String wrapAfterDisjunctiveCatchBar = "wrapAfterDisjunctiveCatchBar"; //NOI18N
    public static final String wrapFor = "wrapFor"; //NOI18N
    public static final String wrapForStatement = "wrapForStatement"; //NOI18N
    public static final String wrapIfStatement = "wrapIfStatement"; //NOI18N
    public static final String wrapWhileStatement = "wrapWhileStatement"; //NOI18N
    public static final String wrapDoWhileStatement = "wrapDoWhileStatement"; //NOI18N
    public static final String wrapCaseStatements = "wrapCaseStatements"; //NOI18N
    public static final String wrapAssert = "wrapAssert"; //NOI18N
    public static final String wrapEnumConstants = "wrapEnumConstants"; //NOI18N
    public static final String wrapAnnotations = "wrapAnnotations"; //NOI18N
    public static final String wrapBinaryOps = "wrapBinaryOps"; //NOI18N
    public static final String wrapAfterBinaryOps = "wrapAfterBinaryOps"; //NOI18N
    public static final String wrapTernaryOps = "wrapTernaryOps"; //NOI18N
    public static final String wrapAfterTernaryOps = "wrapAfterTernaryOps"; //NOI18N
    public static final String wrapAssignOps = "wrapAssignOps"; //NOI18N
    public static final String wrapAfterAssignOps = "wrapAfterAssignOps"; //NOI18N
    
    public static final String blankLinesInDeclarations = "blankLinesInDeclarations"; //NOI18N
    public static final String blankLinesInCode = "blankLinesInCode"; //NOI18N
    public static final String blankLinesAfterModuleHeader = "blankLinesAfterModuleHeader"; //NOI18N
    public static final String blankLinesBeforeModuleClosingBrace = "blankLinesBeforeModuleClosingBrace"; //NOI18N
    public static final String blankLinesBeforeModuleDirectives = "blankLinesBeforeModuleDirectives"; //NOI18N
    public static final String blankLinesAfterModuleDirectives = "blankLinesAfterModuleDirectives"; //NOI18N
    public static final String blankLinesBeforePackage = "blankLinesBeforePackage"; //NOI18N
    public static final String blankLinesAfterPackage = "blankLinesAfterPackage"; //NOI18N
    public static final String blankLinesBeforeImports = "blankLinesBeforeImports"; //NOI18N
    public static final String blankLinesAfterImports = "blankLinesAfterImports"; //NOI18N
    public static final String blankLinesBeforeClass = "blankLinesBeforeClass"; //NOI18N
    public static final String blankLinesAfterClass = "blankLinesAfterClass"; //NOI18N
    public static final String blankLinesAfterClassHeader = "blankLinesAfterClassHeader"; //NOI18N
    public static final String blankLinesAfterAnonymousClassHeader = "blankLinesAfterAnonymousClassHeader"; //NOI18N
    public static final String blankLinesAfterEnumHeader = "blankLinesAfterEnumHeader"; //NOI18N
    public static final String blankLinesBeforeClassClosingBrace = "blankLinesBeforeClassClosingBrace"; //NOI18N
    public static final String blankLinesBeforeAnonymousClosingBrace = "blankLinesBeforeAnonymousClassClosingBrace"; //NOI18N
    public static final String blankLinesBeforeEnumClosingBrace = "blankLinesBeforeEnumClosingBrace"; //NOI18N
    public static final String blankLinesBeforeFields = "blankLinesBeforeFields"; //NOI18N
    public static final String blankLinesAfterFields = "blankLinesAfterFields"; //NOI18N
    public static final String blankLinesBeforeMethods = "blankLinesBeforeMethods"; //NOI18N
    public static final String blankLinesAfterMethods = "blankLinesAfterMethods"; //NOI18N
    
    public static final String spaceBeforeWhile = "spaceBeforeWhile"; //NOI18N
    public static final String spaceBeforeElse = "spaceBeforeElse"; //NOI18N
    public static final String spaceBeforeCatch = "spaceBeforeCatch"; //NOI18N
    public static final String spaceBeforeFinally = "spaceBeforeFinally"; //NOI18N
    public static final String spaceBeforeMethodDeclParen = "spaceBeforeMethodDeclParen"; //NOI18N
    public static final String spaceBeforeMethodCallParen = "spaceBeforeMethodCallParen"; //NOI18N
    public static final String spaceBeforeIfParen = "spaceBeforeIfParen"; //NOI18N
    public static final String spaceBeforeForParen = "spaceBeforeForParen"; //NOI18N
    public static final String spaceBeforeWhileParen = "spaceBeforeWhileParen"; //NOI18N
    public static final String spaceBeforeTryParen = "spaceBeforeTryParen"; //NOI18N
    public static final String spaceBeforeCatchParen = "spaceBeforeCatchParen"; //NOI18N
    public static final String spaceBeforeSwitchParen = "spaceBeforeSwitchParen"; //NOI18N
    public static final String spaceBeforeSynchronizedParen = "spaceBeforeSynchronizedParen"; //NOI18N
    public static final String spaceBeforeAnnotationParen = "spaceBeforeAnnotationParen"; //NOI18N    
    public static final String spaceAroundUnaryOps = "spaceAroundUnaryOps"; //NOI18N
    public static final String spaceAroundBinaryOps = "spaceAroundBinaryOps"; //NOI18N
    public static final String spaceAroundTernaryOps = "spaceAroundTernaryOps"; //NOI18N
    public static final String spaceAroundAssignOps = "spaceAroundAssignOps"; //NOI18N
    public static final String spaceAroundAnnotationValueAssignOps = "spaceAroundAnnotationValueAssignOps"; //NOI18N
    public static final String spaceAroundLambdaArrow = "spaceAroundLambdaArrow"; //NOI18N
    public static final String spaceAroundMethodReferenceDoubleColon = "spaceAroundMethodReferenceDoubleColon"; //NOI18N
    public static final String spaceBeforeModuleDeclLeftBrace = "spaceBeforeModuleDeclLeftBrace"; //NOI18N
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
    public static final String spaceBeforeSynchronizedLeftBrace = "spaceBeforeSynchronizedLeftBrace"; //NOI18N
    public static final String spaceBeforeStaticInitLeftBrace = "spaceBeforeStaticInitLeftBrace"; //NOI18N
    public static final String spaceBeforeArrayInitLeftBrace = "spaceBeforeArrayInitLeftBrace"; //NOI18N
    public static final String spaceWithinParens = "spaceWithinParens"; //NOI18N
    public static final String spaceWithinMethodDeclParens = "spaceWithinMethodDeclParens"; //NOI18N
    public static final String spaceWithinLambdaParens = "spaceWithinLambdaParens"; //NOI18N
    public static final String parensAroundSingularLambdaParam = "parensAroundSingularLambdaParam"; //NOI18N
    public static final String spaceWithinMethodCallParens = "spaceWithinMethodCallParens"; //NOI18N
    public static final String spaceWithinIfParens = "spaceWithinIfParens"; //NOI18N
    public static final String spaceWithinForParens = "spaceWithinForParens"; //NOI18N
    public static final String spaceWithinWhileParens = "spaceWithinWhileParens"; //NOI18N
    public static final String spaceWithinSwitchParens = "spaceWithinSwitchParens"; //NOI18N
    public static final String spaceWithinTryParens = "spaceWithinTryParens"; //NOI18N
    public static final String spaceWithinCatchParens = "spaceWithinCatchParens"; //NOI18N
    public static final String spaceWithinSynchronizedParens = "spaceWithinSynchronizedParens"; //NOI18N
    public static final String spaceWithinTypeCastParens = "spaceWithinTypeCastParens"; //NOI18N
    public static final String spaceWithinAnnotationParens = "spaceWithinAnnotationParens"; //NOI18N
    public static final String spaceWithinBraces = "spaceWithinBraces"; //NOI18N
    public static final String spaceWithinArrayInitBrackets = "spaceWithinArrayInitBrackets"; //NOI18N
    public static final String spaceWithinArrayIndexBrackets = "spaceWithinArrayIndexBrackets"; //NOI18N
    public static final String spaceBeforeComma = "spaceBeforeComma"; //NOI18N
    public static final String spaceAfterComma = "spaceAfterComma"; //NOI18N
    public static final String spaceBeforeSemi = "spaceBeforeSemi"; //NOI18N
    public static final String spaceAfterSemi = "spaceAfterSemi"; //NOI18N
    public static final String spaceBeforeColon = "spaceBeforeColon"; //NOI18N
    public static final String spaceAfterColon = "spaceAfterColon"; //NOI18N
    public static final String spaceAfterTypeCast = "spaceAfterTypeCast"; //NOI18N
    
    public static final String useSingleClassImport = "useSingleClassImport"; //NOI18N
    public static final String usePackageImport = "usePackageImport"; //NOI18N
    public static final String useFQNs = "useFQNs"; //NOI18N
    public static final String importInnerClasses = "importInnerClasses"; //NOI18N
    public static final String preferStaticImports = "preferStaticImports"; //NOI18N
    public static final String allowConvertToStarImport = "allowConvertToStarImport"; //NOI18N
    public static final String countForUsingStarImport = "countForUsingStarImport"; //NOI18N
    public static final String allowConvertToStaticStarImport = "allowConvertToStaticStarImport"; //NOI18N
    public static final String countForUsingStaticStarImport = "countForUsingStaticStarImport"; //NOI18N
    public static final String packagesForStarImport = "packagesForStarImport"; //NOI18N
    public static final String separateStaticImports = "separateStaticImports"; //NOI18N
    public static final String importGroupsOrder = "importGroupsOrder"; //NOI18N
    public static final String separateImportGroups = "separateImportGroups"; //NOI18N
    
    public static final String enableCommentFormatting = "enableCommentFormatting"; //NOI18N
    public static final String enableBlockCommentFormatting = "enableBlockCommentFormatting"; //NOI18N
    public static final String wrapCommentText = "wrapCommentText"; //NOI18N
    public static final String wrapOneLineComment = "wrapOneLineComment"; //NOI18N
    public static final String preserveNewLinesInComments = "preserveNewLinesInComments"; //NOI18N
    public static final String blankLineAfterJavadocDescription = "blankLineAfterJavadocDescription"; //NOI18N
    public static final String blankLineAfterJavadocParameterDescriptions = "blankLineAfterJavadocParameterDescriptions"; //NOI18N
    public static final String blankLineAfterJavadocReturnTag = "blankLineAfterJavadocReturnTag"; //NOI18N
    public static final String generateParagraphTagOnBlankLines = "generateParagraphTagOnBlankLines"; //NOI18N
    public static final String alignJavadocParameterDescriptions = "alignJavadocParameterDescriptions"; //NOI18N
    public static final String alignJavadocReturnDescription = "alignJavadocReturnDescription"; //NOI18N
    public static final String alignJavadocExceptionDescriptions = "alignJavadocExceptionDescriptions"; //NOI18N

    public static CodeStyleProducer codeStyleProducer;
    
    static final String CODE_STYLE_PROFILE = "CodeStyle"; // NOI18N
    static final String DEFAULT_PROFILE = "default"; // NOI18N
    static final String PROJECT_PROFILE = "project"; // NOI18N
    static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    static final String usedProfile = "usedProfile"; // NOI18N
    
    private static final String JAVA = "text/x-java"; //NOI18N
    
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
    
//    public static boolean getGlobalExpandTabToSpaces() {
//        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
//        return prefs.getBoolean(SimpleValueNames.EXPAND_TABS, getDefaultAsBoolean(expandTabToSpaces));
//    }
//
//    public static int getGlobalTabSize() {
//        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
//        return prefs.getInt(SimpleValueNames.TAB_SIZE, getDefaultAsInt(tabSize));
//    }
//
//    public static int getGlobalSpacesPerTab() {
//        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
//        return prefs.getInt(SimpleValueNames.SPACES_PER_TAB, getDefaultAsInt(spacesPerTab));
//    }
//
//    public static int getGlobalIndentSize() {
//        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
//        return prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);
//    }
//
//    public static int getGlobalRightMargin() {
//        Preferences prefs = MimeLookup.getLookup(JAVA).lookup(Preferences.class);
//        return prefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, getDefaultAsInt(rightMargin));
//    }
    
    public static boolean isInteger(String optionID) {
        String value = defaults.get(optionID);
        
        try {
            Integer.parseInt(value);
            return true;            
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
    
    // Private section ---------------------------------------------------------
    
    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N
    
    private static final String WRAP_ALWAYS  = WrapStyle.WRAP_ALWAYS.name();
    private static final String WRAP_IF_LONG  = WrapStyle.WRAP_IF_LONG.name();
    private static final String WRAP_NEVER  = WrapStyle.WRAP_NEVER.name();
    
    private static final String BP_NEW_LINE = BracePlacement.NEW_LINE.name();
    private static final String BP_NEW_LINE_HALF_INDENTED = BracePlacement.NEW_LINE_HALF_INDENTED.name();
    private static final String BP_NEW_LINE_INDENTED = BracePlacement.NEW_LINE_INDENTED.name();
    private static final String BP_SAME_LINE = BracePlacement.SAME_LINE.name(); 
    
    private static final String BGS_ELIMINATE = BracesGenerationStyle.ELIMINATE.name(); 
    private static final String BGS_LEAVE_ALONE = BracesGenerationStyle.LEAVE_ALONE.name(); 
    private static final String BGS_GENERATE = BracesGenerationStyle.GENERATE.name();
    
    private static final String IP_CARET = InsertionPoint.CARET_LOCATION.name();
    private static final String IP_FIRST = InsertionPoint.FIRST_IN_CATEGORY.name();
    private static final String IP_LAST = InsertionPoint.LAST_IN_CATEGORY.name();
    
    private static Map<String,String> defaults;
    
    static {
        createDefaults();
    }
    
    private static void createDefaults() {
        String defaultValues[][] = {
            { expandTabToSpaces, TRUE}, //NOI18N
            { tabSize, "4"}, //NOI18N
            { spacesPerTab, "4"}, //NOI18N
            { indentSize, "4"}, //NOI18N
            { continuationIndentSize, "8"}, //NOI18N
            { labelIndent, "0"}, //NOI18N
            { absoluteLabelIndent, FALSE}, //NOI18N
            { indentTopLevelClassMembers, TRUE}, //NOI18N
            { indentCasesFromSwitch, TRUE}, //NOI18N
            { rightMargin, "80"}, //NOI18N
            { addLeadingStarInComment, TRUE}, //NOI18N

            { preferLongerNames, TRUE}, //NOI18N
            { fieldNamePrefix, ""}, //NOI18N // XXX null
            { fieldNameSuffix, ""}, //NOI18N // XXX null
            { staticFieldNamePrefix, ""}, //NOI18N // XXX null
            { staticFieldNameSuffix, ""}, //NOI18N // XXX null
            { parameterNamePrefix, ""}, //NOI18N // XXX null
            { parameterNameSuffix, ""}, //NOI18N // XXX null
            { localVarNamePrefix, ""}, //NOI18N // XXX null
            { localVarNameSuffix, ""}, //NOI18N // XXX null
            { qualifyFieldAccess, FALSE}, //NOI18N // XXX
            { useIsForBooleanGetters, TRUE}, //NOI18N
            { addOverrideAnnotation, TRUE}, //NOI18N
            { makeLocalVarsFinal, FALSE}, //NOI18N
            { makeParametersFinal, FALSE}, //NOI18N
            { classMembersOrder, "STATIC FIELD;STATIC_INIT;STATIC METHOD;FIELD;INSTANCE_INIT;CONSTRUCTOR;METHOD;STATIC CLASS;CLASS"}, //NOI18N
            { sortMembersByVisibility, FALSE}, //NOI18N
            { visibilityOrder, "PUBLIC;PRIVATE;PROTECTED;DEFAULT"}, //NOI18N
            { keepGettersAndSettersTogether, FALSE}, //NOI18N
            { sortMembersInGroups, FALSE}, //NOI18N
            { sortUsesDependencies, TRUE}, //NOI18N
            { classMemberInsertionPoint, IP_CARET},

            { moduleDeclBracePlacement, BP_SAME_LINE}, //NOI18N
            { classDeclBracePlacement, BP_SAME_LINE}, //NOI18N
            { methodDeclBracePlacement, BP_SAME_LINE}, //NOI18N
            { otherBracePlacement, BP_SAME_LINE}, //NOI18N
            { specialElseIf, TRUE}, //NOI18N
            { redundantIfBraces, BGS_GENERATE}, //NOI18N
            { redundantForBraces, BGS_GENERATE}, //NOI18N
            { redundantWhileBraces, BGS_GENERATE}, //NOI18N
            { redundantDoWhileBraces, BGS_GENERATE}, //NOI18N
            { alignMultilineExports, FALSE}, //NOI18N
            { alignMultilineOpens, FALSE}, //NOI18N
            { alignMultilineProvides, FALSE}, //NOI18N
            { alignMultilineMethodParams, FALSE}, //NOI18N
            { alignMultilineLambdaParams, FALSE}, //NOI18N
            { alignMultilineCallArgs, FALSE}, //NOI18N
            { alignMultilineAnnotationArgs, FALSE}, //NOI18N
            { alignMultilineImplements, FALSE}, //NOI18N
            { alignMultilineThrows, FALSE}, //NOI18N
            { alignMultilineParenthesized, FALSE}, //NOI18N
            { alignMultilineBinaryOp, FALSE}, //NOI18N
            { alignMultilineTernaryOp, FALSE}, //NOI18N
            { alignMultilineAssignment, FALSE}, //NOI18N
            { alignMultilineTryResources, FALSE}, //NOI18N
            { alignMultilineDisjunctiveCatchTypes, FALSE}, //NOI18N
            { alignMultilineFor, FALSE}, //NOI18N
            { alignMultilineArrayInit, FALSE}, //NOI18N
            { placeElseOnNewLine, FALSE}, //NOI18N 
            { placeWhileOnNewLine, FALSE}, //NOI18N
            { placeCatchOnNewLine, FALSE}, //NOI18N 
            { placeFinallyOnNewLine, FALSE}, //NOI18N 
            { placeNewLineAfterModifiers, FALSE}, //NOI18N

            { wrapProvidesWithKeyword, WRAP_NEVER}, //NOI18N
            { wrapProvidesWithList, WRAP_NEVER}, //NOI18N
            { wrapExportsToKeyword, WRAP_NEVER}, //NOI18N
            { wrapExportsToList, WRAP_NEVER}, //NOI18N
            { wrapOpensToKeyword, WRAP_NEVER}, //NOI18N
            { wrapOpensToList, WRAP_NEVER}, //NOI18N
            { wrapExtendsImplementsKeyword, WRAP_NEVER}, //NOI18N
            { wrapExtendsImplementsList, WRAP_NEVER}, //NOI18N
            { wrapMethodParams, WRAP_NEVER}, //NOI18N
            { wrapLambdaParams, WRAP_NEVER}, //NOI18N
            { wrapLambdaArrow, WRAP_NEVER}, //NOI18N
            { wrapAfterLambdaArrow, FALSE}, //NOI18N
            { wrapThrowsKeyword, WRAP_NEVER}, //NOI18N
            { wrapThrowsList, WRAP_NEVER}, //NOI18N
            { wrapMethodCallArgs, WRAP_NEVER}, //NOI18N
            { wrapAnnotationArgs, WRAP_NEVER}, //NOI18N
            { wrapChainedMethodCalls, WRAP_NEVER}, //NOI18N
            { wrapAfterDotInChainedMethodCalls, TRUE}, //NOI18N
            { wrapArrayInit, WRAP_NEVER}, //NOI18N
            { wrapTryResources, WRAP_NEVER}, //NOI18N
            { wrapDisjunctiveCatchTypes, WRAP_NEVER}, //NOI18N
            { wrapAfterDisjunctiveCatchBar, FALSE}, //NOI18N
            { wrapFor, WRAP_NEVER}, //NOI18N
            { wrapForStatement, WRAP_ALWAYS}, //NOI18N
            { wrapIfStatement, WRAP_ALWAYS}, //NOI18N
            { wrapWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapDoWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapCaseStatements, WRAP_ALWAYS}, //NOI18N
            { wrapAssert, WRAP_NEVER}, //NOI18N
            { wrapEnumConstants, WRAP_NEVER}, //NOI18N
            { wrapAnnotations, WRAP_ALWAYS}, //NOI18N
            { wrapBinaryOps, WRAP_NEVER}, //NOI18N
            { wrapAfterBinaryOps, FALSE}, //NOI18N
            { wrapTernaryOps, WRAP_NEVER}, //NOI18N
            { wrapAfterTernaryOps, FALSE}, //NOI18N
            { wrapAssignOps, WRAP_NEVER}, //NOI18N
            { wrapAfterAssignOps, FALSE}, //NOI18N

            { blankLinesInDeclarations, "1"}, //NOI18N
            { blankLinesInCode, "1"}, //NOI18N
            { blankLinesAfterModuleHeader, "0"}, //NOI18N 
            { blankLinesBeforeModuleClosingBrace, "0"}, //NOI18N 
            { blankLinesBeforeModuleDirectives, "0"}, //NOI18N 
            { blankLinesAfterModuleDirectives, "0"}, //NOI18N 
            { blankLinesBeforePackage, "0"}, //NOI18N
            { blankLinesAfterPackage, "1"}, //NOI18N
            { blankLinesBeforeImports, "1"}, //NOI18N 
            { blankLinesAfterImports, "1"}, //NOI18N
            { blankLinesBeforeClass, "1"}, //NOI18N 
            { blankLinesAfterClass, "0"}, //NOI18N
            { blankLinesAfterClassHeader, "1"}, //NOI18N 
            { blankLinesAfterAnonymousClassHeader, "0"}, //NOI18N 
            { blankLinesAfterEnumHeader, "0"}, //NOI18N 
            { blankLinesBeforeClassClosingBrace, "0"}, //NOI18N 
            { blankLinesBeforeAnonymousClosingBrace, "0"}, //NOI18N 
            { blankLinesBeforeEnumClosingBrace, "0"}, //NOI18N 
            { blankLinesBeforeFields, "0"}, //NOI18N 
            { blankLinesAfterFields, "0"}, //NOI18N
            { blankLinesBeforeMethods, "1"}, //NOI18N
            { blankLinesAfterMethods, "0"}, //NOI18N

            { spaceBeforeWhile, TRUE}, //NOI18N // XXX
            { spaceBeforeElse, TRUE}, //NOI18N // XXX
            { spaceBeforeCatch, TRUE}, //NOI18N // XXX
            { spaceBeforeFinally, TRUE}, //NOI18N // XXX
            { spaceBeforeMethodDeclParen, FALSE}, //NOI18N
            { spaceBeforeMethodCallParen, FALSE}, //NOI18N
            { spaceBeforeIfParen, TRUE}, //NOI18N
            { spaceBeforeForParen, TRUE}, //NOI18N
            { spaceBeforeWhileParen, TRUE}, //NOI18N
            { spaceBeforeTryParen, TRUE}, //NOI18N
            { spaceBeforeCatchParen, TRUE}, //NOI18N
            { spaceBeforeSwitchParen, TRUE}, //NOI18N
            { spaceBeforeSynchronizedParen, TRUE}, //NOI18N
            { spaceBeforeAnnotationParen, FALSE}, //NOI18N    
            { spaceAroundUnaryOps, FALSE}, //NOI18N
            { spaceAroundBinaryOps, TRUE}, //NOI18N
            { spaceAroundTernaryOps, TRUE}, //NOI18N
            { spaceAroundAssignOps, TRUE}, //NOI18N
            { spaceAroundAnnotationValueAssignOps, TRUE}, //NOI18N
            { spaceAroundLambdaArrow, TRUE}, //NOI18N
            { spaceAroundMethodReferenceDoubleColon, FALSE}, //NOI18N
            { spaceBeforeModuleDeclLeftBrace, TRUE}, //NOI18N
            { spaceBeforeClassDeclLeftBrace, TRUE}, //NOI18N
            { spaceBeforeMethodDeclLeftBrace, TRUE}, //NOI18N
            { spaceBeforeIfLeftBrace, TRUE}, //NOI18N
            { spaceBeforeElseLeftBrace, TRUE}, //NOI18N
            { spaceBeforeWhileLeftBrace, TRUE}, //NOI18N
            { spaceBeforeForLeftBrace, TRUE}, //NOI18N
            { spaceBeforeDoLeftBrace, TRUE}, //NOI18N
            { spaceBeforeSwitchLeftBrace, TRUE}, //NOI18N
            { spaceBeforeTryLeftBrace, TRUE}, //NOI18N
            { spaceBeforeCatchLeftBrace, TRUE}, //NOI18N
            { spaceBeforeFinallyLeftBrace, TRUE}, //NOI18N
            { spaceBeforeSynchronizedLeftBrace, TRUE}, //NOI18N
            { spaceBeforeStaticInitLeftBrace, TRUE}, //NOI18N
            { spaceBeforeArrayInitLeftBrace, FALSE}, //NOI18N
            { spaceWithinParens, FALSE}, //NOI18N
            { spaceWithinMethodDeclParens, FALSE}, //NOI18N
            { spaceWithinLambdaParens, FALSE}, //NOI18N
            { parensAroundSingularLambdaParam, FALSE}, //NOI18N
            { spaceWithinMethodCallParens, FALSE}, //NOI18N
            { spaceWithinIfParens, FALSE}, //NOI18N
            { spaceWithinForParens, FALSE}, //NOI18N
            { spaceWithinWhileParens, FALSE}, //NOI18N
            { spaceWithinSwitchParens, FALSE}, //NOI18N
            { spaceWithinTryParens, FALSE}, //NOI18N
            { spaceWithinCatchParens, FALSE}, //NOI18N
            { spaceWithinSynchronizedParens, FALSE}, //NOI18N
            { spaceWithinTypeCastParens, FALSE}, //NOI18N
            { spaceWithinAnnotationParens, FALSE}, //NOI18N
            { spaceWithinBraces, FALSE}, //NOI18N
            { spaceWithinArrayInitBrackets, FALSE}, //NOI18N
            { spaceWithinArrayIndexBrackets, FALSE}, //NOI18N
            { spaceBeforeComma, FALSE}, //NOI18N
            { spaceAfterComma, TRUE}, //NOI18N
            { spaceBeforeSemi, FALSE}, //NOI18N
            { spaceAfterSemi, TRUE}, //NOI18N
            { spaceBeforeColon, TRUE}, //NOI18N
            { spaceAfterColon, TRUE}, //NOI18N
            { spaceAfterTypeCast, TRUE}, //NOI18N

            { useSingleClassImport, TRUE}, //NOI18N
            { usePackageImport, FALSE}, //NOI18N
            { useFQNs, FALSE}, //NOI18N
            { importInnerClasses, FALSE}, //NOI18N
            { preferStaticImports, FALSE}, //NOI18N
            { allowConvertToStarImport, FALSE}, //NOI18N
            { countForUsingStarImport, "5"}, //NOI18N
            { allowConvertToStaticStarImport, FALSE}, //NOI18N
            { countForUsingStaticStarImport, "3"}, //NOI18N
            { packagesForStarImport, ""}, //NOI18N
            { separateStaticImports, FALSE}, //NOI18N
            { importGroupsOrder, ""}, //NOI18N
            { separateImportGroups, TRUE}, //NOI18N
            
            { enableCommentFormatting, TRUE}, //NOI18N
            { enableBlockCommentFormatting, FALSE}, //NOI18N
            { wrapCommentText, TRUE}, //NOI18N
            { wrapOneLineComment, TRUE}, //NOI18N
            { preserveNewLinesInComments, FALSE}, //NOI18N
            { blankLineAfterJavadocDescription, TRUE}, //NOI18N
            { blankLineAfterJavadocParameterDescriptions, FALSE}, //NOI18N
            { blankLineAfterJavadocReturnTag, FALSE}, //NOI18N
            { generateParagraphTagOnBlankLines, FALSE}, //NOI18N
            { alignJavadocParameterDescriptions, FALSE}, //NOI18N
            { alignJavadocReturnDescription, FALSE}, //NOI18N
            { alignJavadocExceptionDescriptions, FALSE}, //NOI18N                        
        };
        
        defaults = new HashMap<String,String>();
        
        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }
 
    public static interface CodeStyleProducer {
        
        public CodeStyle create( Preferences preferences );    
    }
}
