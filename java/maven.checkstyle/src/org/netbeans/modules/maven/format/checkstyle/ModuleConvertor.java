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

package org.netbeans.modules.maven.format.checkstyle;

import org.codehaus.plexus.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.openide.util.Exceptions;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author mkleint
 */
public class ModuleConvertor {

private static String PROP_SPACE_BEFORE_DO_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeDoLeftBrace";
private static String PROP_SPACE_BEFORE_STATIC_INIT_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeStaticInitLeftBrace";
private static String PROP_SPACE_BEFORE_IF_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeIfParen";
private static String PROP_SPACE_BEFORE_TRY_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeTryLeftBrace";
private static String PROP_SPACE_BEFORE_ANN_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeAnnotationParen";
private static String PROP_SPACE_BEFORE_SYNC_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeSynchronizedParen";
private static String PROP_SPACE_BEFORE_ARR_INIT_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeArrayInitLeftBrace";
private static String PROP_SPACE_BEFORE_METHOD_DECL_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeMethodDeclParen";
private static String PROP_SPACE_WITHIN_ARR_BRACKETS = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinArrayInitBrackets";
private static String PROP_SPACE_BEFORE_SYNC_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeSynchronizedLeftBrace";
private static String PROP_SPACE_BEFORE_FINALLY = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeFinally";
private static String PROP_SPACE_BEFORE_METHOD_DECL_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeMethodDeclLeftBrace";
private static String PROP_SPACE_WITHIN_BRACES = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinBraces";
private static String PROP_SPACE_BEFORE_FINALLY_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeFinallyLeftBrace";
private static String PROP_SPACE_BEFORE_METHOD_CALL_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeMethodCallParen";
private static String PROP_SPACE_BEFORE_IF_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeIfLeftBrace";
private static String PROP_SPACE_BEFORE_ELSE_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeElseLeftBrace";
private static String PROP_SPACE_BEFORE_SWITCH_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeSwitchParen";
private static String PROP_SPACE_BEFORE_WHILE = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeWhile";
private static String PROP_SPACE_BEFORE_CLASS_DECL_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeClassDeclLeftBrace";
private static String PROP_SPACE_BEFORE_CATCH_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeCatchLeftBrace";
private static String PROP_SPACE_BEFORE_CATCH_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeCatchParen";
private static String PROP_SPACE_BEFORE_SWITCH_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeSwitchLeftBrace";
private static String PROP_SPACE_BEFORE_CATCH = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeCatch";
private static String PROP_SPACE_BEFORE_ELSE = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeElse";
private static String PROP_NEWLINE_MODIFIERS = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.placeNewLineAfterModifiers";
private static String PROP_NEWLINE_WHILE = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.placeWhileOnNewLine";

    public Properties convert(InputStream checkstyleStream) {
        Properties props = new Properties();
        SAXBuilder bldr = new SAXBuilder();
        bldr.setValidation(false);
        bldr.setEntityResolver(new EntityResolver() {
           @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
               InputSource is = null;
               if ("-//Puppy Crawl//DTD Check Configuration 1.0//EN".equals(publicId)) { //NOI18N
                    is = new InputSource(ModuleConvertor.class.getResourceAsStream("config/configuration_1_0.dtd"));
               }
               if ("-//Puppy Crawl//DTD Check Configuration 1.1//EN".equals(publicId)) {//NOI18N
                    is = new InputSource(ModuleConvertor.class.getResourceAsStream("config/configuration_1_1.dtd"));
               }
               if ("-//Puppy Crawl//DTD Check Configuration 1.2//EN".equals(publicId)) {//NOI18N
                    is = new InputSource(ModuleConvertor.class.getResourceAsStream("config/configuration_1_2.dtd"));
               }
               if ("-//Puppy Crawl//DTD Check Configuration 1.3//EN".equals(publicId)) {//NOI18N
                    is = new InputSource(ModuleConvertor.class.getResourceAsStream("config/configuration_1_3.dtd"));
               }
               if (is != null) {
                    is.setPublicId(publicId);
                    is.setSystemId(systemId);
               } else {
                   Logger.getLogger(ModuleConvertor.class.getName()).log(Level.WARNING, "unknown entity publicid={0} systemId={1}", new Object[] {publicId, systemId});
               }
               return is;
            }
        });
        try (checkstyleStream) {
            Document doc = bldr.build(checkstyleStream);
            Element root = doc.getRootElement();
            processModule(root, "", props);
            if (props.size() > 0) {
                props.put("org-netbeans-modules-editor-indent.CodeStyle.usedProfile", "project");
            }
        } catch (JDOMException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return props;

    }

    private void checkRules(String modulePath, Map<String, String> moduleProps, Properties props) {
        if ("/Checker/TreeWalker".equals(modulePath)) {
            processTreeWalker(moduleProps, props);
        } else if ("/Checker/TreeWalker/LeftCurly".equals(modulePath)) {
            processLeftBrace(moduleProps, props);
        } else if ("/Checker/TreeWalker/RightCurly".equals(modulePath)) {
            processRightBrace(moduleProps, props);
        } else if ("/Checker/TreeWalker/LineLength".equals(modulePath)) {
            processLineLength(moduleProps, props);
        } else if ("/Checker/TreeWalker/NeedBraces".equals(modulePath)) {
            processNeedBraces(moduleProps, props);
        } else if ("/Checker/TreeWalker/WhitespaceAfter".equals(modulePath)) {
            processWhiteSpaceAfter(moduleProps, props);
        } else if ("/Checker/TreeWalker/WhitespaceAround".equals(modulePath)) {
            processWhiteSpaceAround(moduleProps, props);
        } else if ("/Checker/TreeWalker/ParenPad".equals(modulePath)) {
            processParenPad(moduleProps, props);
        } else if ("/Checker/TreeWalker/TabCharacter".equals(modulePath)) {
            processTabCharacter(moduleProps, props);
        }

    }
    private static String PROP_BRACE_CLASS = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.classDeclBracePlacement";
    private static String PROP_BRACE_METHOD = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.methodDeclBracePlacement";
    private static String PROP_BRACE_OTHER = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.otherBracePlacement";
    private static String PROP_BRACE_ELSEIF_HANDLING = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.specialElseIf";
    private void processLeftBrace(Map<String, String> moduleProps, Properties props) {
        String option = getPropertyValue(moduleProps, "option", "eol");
        List<Token> tokens = getApplicableTokens(moduleProps, "tokens");
        String nbValue = "nl".equals(option) ? BracePlacement.NEW_LINE.name() : BracePlacement.SAME_LINE.name();
        int count = 0;
        if (tokens.contains(Token.CLASS_DEF)) {
            props.setProperty(PROP_BRACE_CLASS, nbValue);
            count++;
        }
        if (tokens.contains(Token.METHOD_DEF)) {
            props.setProperty(PROP_BRACE_METHOD, nbValue);
            count++;
        }
        if (tokens.size() > count) {
            props.setProperty(PROP_BRACE_OTHER, nbValue);
        }
    }

    private static String PROP_NEWLINE_ELSE = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.placeElseOnNewLine";
    private static String PROP_NEWLINE_FINALLY = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.placeFinallyOnNewLine";
    private static String PROP_NEWLINE_CATCH = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.placeCatchOnNewLine";
    private void processRightBrace(Map<String, String> moduleProps, Properties props) {
        String option = getPropertyValue(moduleProps, "option", "same");
        List<Token> tokens = getApplicableTokens(moduleProps, "tokens");
        String newLine = "alone".equals(option) ? "true" : "false";
        if (tokens.contains(Token.LITERAL_ELSE)) {
            props.setProperty(PROP_NEWLINE_ELSE, newLine);
        }
        if (tokens.contains(Token.LITERAL_CATCH)) {
            props.setProperty(PROP_NEWLINE_CATCH, "true");
        }
        if (tokens.contains(Token.LITERAL_FINALLY)) {
            props.setProperty(PROP_NEWLINE_FINALLY, "true");
        }
    }





    private static String PROP_SPACE_WITHIN_CATCH_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinCatchParens";
    private static String PROP_SPACE_WITHIN_IF_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinIfParens";
    private static String PROP_SPACE_WITHIN_METHOD_CALL_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinMethodCallParens";
    private static String PROP_SPACE_WITHIN_SYNC_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinSynchronizedParens";
    private static String PROP_SPACE_WITHIN_FOR_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinForParens";
    private static String PROP_SPACE_WITHIN_SWITCH_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinSwitchParens";
    private static String PROP_SPACE_WITHIN_WHILE_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinWhileParens";
    private static String PROP_SPACE_WITHIN_CAST_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinTypeCastParens";
    private static String PROP_SPACE_WITHIN_ANN_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinAnnotationParens";
    private static String PROP_SPACE_WITHIN_METHOD_DECL_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinMethodDeclParens";

    private void processParenPad(Map<String, String> moduleProps, Properties props) {
        String option = getPropertyValue(moduleProps, "option", "nospace");
        String space = "space".equals(option) ? "true" : "false";
        List<Token> tokens = getApplicableTokens(moduleProps, "tokens");
        if (tokens.contains(Token.METHOD_CALL)) {
            props.setProperty(PROP_SPACE_WITHIN_METHOD_CALL_PAREN, space);
        }
        if (tokens.contains(Token.LPAREN) || tokens.contains(Token.RPAREN)) {
            props.setProperty(PROP_SPACE_WITHIN_ANN_PAREN, space);
            props.setProperty(PROP_SPACE_WITHIN_CAST_PAREN, space);
            props.setProperty(PROP_SPACE_WITHIN_CATCH_PAREN, space);
            props.setProperty(PROP_SPACE_WITHIN_FOR_PAREN, space);
            props.setProperty(PROP_SPACE_WITHIN_IF_PAREN, space);
            props.setProperty(PROP_SPACE_WITHIN_METHOD_DECL_PAREN, space);
            props.setProperty(PROP_SPACE_WITHIN_SWITCH_PAREN, space);
            props.setProperty(PROP_SPACE_WITHIN_SYNC_PAREN, space);
            props.setProperty(PROP_SPACE_WITHIN_WHILE_PAREN, space);
        }
    }


    private static String PROP_SPACE_AROUND_BINARY_OPS = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceAroundBinaryOps";
    private static String PROP_SPACE_AROUND_TERNARY_OPS = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceAroundTernaryOps";
    private static String PROP_SPACE_AROUND_ASSIGN_OPS = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceAroundAssignOps";
    private static String PROP_SPACE_AROUND_UNARY_OPS = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceAroundUnaryOps";
    private static String PROP_SPACE_BEFORE_FOR_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeForParen";
    private static String PROP_SPACE_BEFORE_FOR_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeForLeftBrace";
    private static String PROP_SPACE_BEFORE_WHILE_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeWhileParen";
    private static String PROP_SPACE_BEFORE_WHILE_LEFT = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceBeforeWhileLeftBrace";
    private static String PROP_SPACE_WITHIN_PAREN = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceWithinParens";

    private void processWhiteSpaceAround(Map<String, String> moduleProps, Properties props) {
        List<Token> tokens = getApplicableTokens(moduleProps, "tokens");
        props.setProperty(PROP_SPACE_AROUND_ASSIGN_OPS, new Boolean (tokens.contains(Token.ASSIGN)
                                                                  || tokens.contains(Token.BAND_ASSIGN)
                                                                  || tokens.contains(Token.BOR_ASSIGN)
                                                                  || tokens.contains(Token.BXOR_ASSIGN)
                                                                  || tokens.contains(Token.BSR_ASSIGN)
                                                                  || tokens.contains(Token.DIV_ASSIGN)
                                                                  || tokens.contains(Token.MINUS_ASSIGN)
                                                                  || tokens.contains(Token.SR_ASSIGN)
                                                                  || tokens.contains(Token.MINUS_ASSIGN)
                                                                  || tokens.contains(Token.PLUS_ASSIGN)
                                                                  || tokens.contains(Token.STAR_ASSIGN)).toString());

//where does this get set?        props.setProperty(PROP_SPACE_AROUND_UNARY_OPS, new Boolean ().toString());

        props.setProperty(PROP_SPACE_AROUND_BINARY_OPS, new Boolean (tokens.contains(Token.AND)
                                                                  || tokens.contains(Token.BOR)
                                                                  || tokens.contains(Token.BSR)
                                                                  || tokens.contains(Token.BXOR)
                                                                  || tokens.contains(Token.DIV)
                                                                  || tokens.contains(Token.EQUAL)
                                                                  || tokens.contains(Token.NOT_EQUAL)
                                                                  || tokens.contains(Token.GE)
                                                                  || tokens.contains(Token.GT)
                                                                  || tokens.contains(Token.LE)
                                                                  || tokens.contains(Token.LT)
                                                                  || tokens.contains(Token.LAND)
                                                                  || tokens.contains(Token.LOR)
                                                                  || tokens.contains(Token.MINUS)
                                                                  || tokens.contains(Token.MOD)
                                                                  || tokens.contains(Token.PLUS)
                                                                  || tokens.contains(Token.SL)
                                                                  || tokens.contains(Token.SR)).toString());

        props.setProperty(PROP_SPACE_AROUND_TERNARY_OPS, new Boolean (tokens.contains(Token.QUESTION)).toString());
        props.setProperty(PROP_SPACE_BEFORE_FOR_LEFT, new Boolean (tokens.contains(Token.LITERAL_FOR)).toString());
        props.setProperty(PROP_SPACE_BEFORE_FOR_PAREN, new Boolean (tokens.contains(Token.LITERAL_FOR)).toString());
        props.setProperty(PROP_SPACE_BEFORE_WHILE_LEFT, new Boolean (tokens.contains(Token.LITERAL_WHILE)).toString());
        //#221067 no LPAREN and RPAREN inside withespacearound - http://checkstyle.sourceforge.net/config_whitespace.html
        //props.setProperty(PROP_SPACE_WITHIN_PAREN, new Boolean (tokens.contains(Token.LPAREN) || tokens.contains(Token.RPAREN)).toString());

    }


    private static String PROP_SPACE_AFTER_SEMI = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceAfterSemi";
    private static String PROP_SPACE_AFTER_CAST = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceAfterTypeCast";
    private static String PROP_SPACE_AFTER_COMMA = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceAfterComma";
    private static String PROP_SPACE_AFTER_COLON = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.spaceAfterColon";
    private void processWhiteSpaceAfter(Map<String, String> moduleProps, Properties props) {
        List<Token> tokens = getApplicableTokens(moduleProps, "tokens");
        props.setProperty(PROP_SPACE_AFTER_COMMA, new Boolean (tokens.contains(Token.COMMA)).toString());
        props.setProperty(PROP_SPACE_AFTER_CAST, new Boolean (tokens.contains(Token.TYPECAST)).toString());
        props.setProperty(PROP_SPACE_AFTER_SEMI, new Boolean (tokens.contains(Token.SEMI)).toString());
    }

    private static String PROP_EXPAND_TABS = "org-netbeans-modules-editor-indent.CodeStyle.project.expand-tabs";
    private void processTabCharacter(Map<String, String> moduleProps, Properties props) {
        props.setProperty(PROP_EXPAND_TABS, Boolean.TRUE.toString());
    }

    private static String PROP_LINE_MAX = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.text-limit-width";
    private void processLineLength(Map<String, String> moduleProps, Properties props) {
        String option = getPropertyValue(moduleProps, "max", "80");
        props.setProperty(PROP_LINE_MAX, option);
    }

    private static String PROP_GENERATE_BRACE_DO = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.redundantDoWhileBraces";
    private static String PROP_GENERATE_BRACE_FOR = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.redundantForBraces";
    private static String PROP_GENERATE_BRACE_IF = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.redundantIfBraces";
    private static String PROP_GENERATE_BRACE_WHILE = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.redundantWhileBraces";
    private void processNeedBraces(Map<String, String> moduleProps, Properties props) {
        List<Token> tokens = getApplicableTokens(moduleProps, "tokens");
        props.setProperty(PROP_GENERATE_BRACE_DO, tokens.contains(Token.LITERAL_DO) ? BracesGenerationStyle.GENERATE.name() : BracesGenerationStyle.LEAVE_ALONE.name());
        props.setProperty(PROP_GENERATE_BRACE_FOR, tokens.contains(Token.LITERAL_FOR) ? BracesGenerationStyle.GENERATE.name() : BracesGenerationStyle.LEAVE_ALONE.name());
        props.setProperty(PROP_GENERATE_BRACE_IF, (tokens.contains(Token.LITERAL_IF) || tokens.contains(Token.LITERAL_ELSE)) ? BracesGenerationStyle.GENERATE.name() : BracesGenerationStyle.LEAVE_ALONE.name());
        props.setProperty(PROP_GENERATE_BRACE_WHILE, tokens.contains(Token.LITERAL_WHILE) ? BracesGenerationStyle.GENERATE.name() : BracesGenerationStyle.LEAVE_ALONE.name());
    }

    private static String PROP_TABSIZE = "org-netbeans-modules-editor-indent.text.x-java.CodeStyle.project.tab-size";
    private void processTreeWalker(Map<String, String> moduleProps, Properties props) {
        String option = getPropertyValue(moduleProps, "tabWidth", "8");
        props.setProperty(PROP_TABSIZE, option);
    }

    private List<Token> getApplicableTokens(Map<String, String> moduleProps, String string) {
        String tokens = moduleProps.get(string);
        if (tokens == null) {
            return Arrays.asList(Token.values());
        }
        String[] split = StringUtils.split(tokens, ",");
        List<Token> toRet = new ArrayList<Token>();
        for (String val : split) {
            try {
                toRet.add(Token.valueOf(val.trim()));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return toRet;
    }

    private String getPropertyValue(Map<String, String> props, String key, String defaultValue) {
        String option = props.get(key);
        option = option != null ? option : defaultValue;
        return option;
    }

    private void processModule(Element module, String path, Properties props) {
        if (!"module".equals(module.getName())) {
            return;
        }
        String moduleName = module.getAttributeValue("name");
        @SuppressWarnings("unchecked")
        List<Element> propElements = module.getChildren("property");
        Map<String, String> moduleProps = new HashMap<String, String>();
        for (Element prp : propElements) {
            String name = prp.getAttributeValue("name");
            String value = prp.getAttributeValue("value");
            assert name != null && value != null;
            moduleProps.put(name, value);
        }
        String modulePath = path + "/" + moduleName;
        checkRules(modulePath, moduleProps, props);
        //now check child modules..
        @SuppressWarnings("unchecked")
        List<Element> childs = module.getChildren("module");
        for (Element child : childs) {
            processModule(child, modulePath, props);
        }
    }

    private enum Token {

        CLASS_DEF, CTOR_DEF, INTERFACE_DEF, LITERAL_CATCH, LITERAL_DO, LITERAL_ELSE,
        LITERAL_FINALLY, LITERAL_FOR, LITERAL_IF, LITERAL_SWITCH, LITERAL_SYNCHRONIZED,
        LITERAL_TRY, LITERAL_WHILE, METHOD_DEF, COMMA, TYPECAST, SEMI,
        CTOR_CALL, SUPER_CTOR_CALL,
        ASSIGN, BAND, BAND_ASSIGN, BOR, BOR_ASSIGN, BSR, BSR_ASSIGN, BXOR, BXOR_ASSIGN,
        COLON, DIV, DIV_ASSIGN, EQUAL, GE, GT, LAND, LCURLY, LE, LITERAL_ASSERT, LITERAL_RETURN,
        LOR, LT, MINUS, MINUS_ASSIGN, MOD, MOD_ASSIGN, NOT_EQUAL, PLUS, PLUS_ASSIGN, QUESTION, RCURLY,
        SL, SLIST, SL_ASSIGN, SR, SR_ASSIGN, STAR, STAR_ASSIGN,  GENERIC_START, GENERIC_END, AND,
        TYPE_EXTENSION_AND, WILDCARD_TYPE, METHOD_CALL, LPAREN, RPAREN;
    }

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
}
