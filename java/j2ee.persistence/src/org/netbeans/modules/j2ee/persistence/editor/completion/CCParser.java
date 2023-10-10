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
package org.netbeans.modules.j2ee.persistence.editor.completion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 * Builds an annotations tree containg NN name and attribs map. Supports nested annotations.
 *
 * @author Marek Fukala
 */

public class CCParser {
    
    //parser states
    private static final int INIT = 0;
    private static final int NN = 1; //@
    private static final int ERROR = 2;
    private static final int NNNAME = 3; //@Table
    private static final int INNN = 4; //@Table(
    private static final int ATTRNAME = 5; //@Table(name
    private static final int EQ = 6; //@Table(name=
    private static final int ATTRVALUE = 7; //@Table(name="hello" || @Table(name=@
    
    private final CompilationController controller;

    CCParser(CompilationController controller) {
        this.controller = controller;
    }
    
    public CC parseAnnotation(int offset) {
        int nnStart = findAnnotationStart(offset);
        if(nnStart == -1) {
            return null;
        } else {
            return parseAnnotationOnOffset(nnStart);
        }
    }
    
    /** very simple annotations parser */
    private CC parseAnnotationOnOffset(int offset) {
            int state = INIT;
            TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            ts.moveStart();
            if (ts.move(offset) == 0) {
                if(!ts.moveNext()) {
                        ts.movePrevious();
                    }
            }
            Token<JavaTokenId> titk = ts.token();
            JavaTokenId id = titk.id();
            
            assert id == JavaTokenId.AT;
            
            int nnstart = ts.offset();
            int nnend;
            String nnName = null;
            String currAttrName = null;
            String currAttrValue = null;
            int currAttrStartOffset = -1;
            boolean currAttrQuated = false;
            int lparopened = 0;
            
            List<NNAttr> attrs = new ArrayList<>(5);
            //helper var
            int eqOffset = -1;
            
            do {
                id = titk.id();
                //ignore whitespaces
                if(id == JavaTokenId.WHITESPACE || id==JavaTokenId.LINE_COMMENT || id==JavaTokenId.BLOCK_COMMENT || id==JavaTokenId.JAVADOC_COMMENT) {
                    if(!ts.moveNext()) {
                        break;
                    }
                    titk = ts.token();
                    continue;
                }
                
                switch(state) {
                    case INIT:
                        switch(id) {
                            case AT:
                                state = NN;
                                break;
                            default:
                                state = ERROR;
                        }
                        break;
                    case NN:
                        switch(id) {
                            case IDENTIFIER:
                                state = NNNAME;
                                nnName = titk.text().toString();
                                break;
                            default:
                                state = ERROR;
                        }
                        break;
                    case NNNAME:
                        switch(id) {
                            case LPAREN:
                                state = INNN;
                                break;
                            case DOT:
                            case IDENTIFIER:
                                //add the token image to the NN name
                                nnName += titk.text().toString();
                                break;
                            default:
                                //we are in NN name, but no parenthesis came
                                //this mean either error or annotation without parenthesis like @Id
                                nnend = nnstart + "@".length() + nnName.length();
                                CC newNN = new CC(nnName, attrs, nnstart, nnend);
                                return newNN;
                        }
                        break;
                    case INNN:
                        switch(id) {
                            case IDENTIFIER:
                                currAttrName = titk.text().toString();
                                state = ATTRNAME;
                                break;
                                //case JavaTokenContext.RPAREN_ID:
                            case COMMA:
                                //just consume, still in INNN
                                break;
                            case STRING_LITERAL:
                                if (attrs.isEmpty()) {
                                    state = EQ;
                                    currAttrStartOffset = currAttrStartOffset<0 ? ts.offset() : currAttrStartOffset;
                                    currAttrQuated = true;//currently is used in cc and we support qq for one literal only, may need to be revieved later for "combined" cases
                                    currAttrValue = Utils.unquote(titk.text().toString());
                                    break;
                                }
                            default:
                                //we reached end of the annotation, or error
                                state = ERROR;
                                break;
                        }
                        break;
                    case ATTRNAME:
                        switch(id) {
                            case EQ:
                                state = EQ;
                                currAttrValue = "";
                                currAttrStartOffset = -1;
                                currAttrQuated = false;
                                eqOffset = ts.offset();
                                break;
                            default:
                                state = ERROR;
                        }
                        break;
                    case EQ:
                        switch(id) {
                            case STRING_LITERAL:
                                currAttrStartOffset = currAttrStartOffset<0 ? ts.offset() : currAttrStartOffset;
                                currAttrQuated = true;//currently is used in cc and we support qq for one literal only, may need to be revieved later for "combined" cases
                                currAttrValue += Utils.unquote(titk.text().toString());
                                break;
                            case DOT:
                            case IDENTIFIER:
                                //need to collect data, do not switch to INNN here
                                //multidot identifier can be expected, and it may be summ with literals and with parensis
                                currAttrStartOffset = currAttrStartOffset<0 ? ts.offset() : currAttrStartOffset;
                                currAttrValue += titk.text().toString();
                                break;
                            case PLUS:
                                currAttrStartOffset = currAttrStartOffset<0 ? ts.offset() : currAttrStartOffset;
                                currAttrValue += titk.text().toString();
                                break;
                            case LPAREN:
                                lparopened++;
                                currAttrStartOffset = currAttrStartOffset<0 ? ts.offset() : currAttrStartOffset;
                                currAttrValue += titk.text().toString();
                                break;
                            case AT:
                                //nested annotation
                                CC nestedNN = parseAnnotationOnOffset(ts.offset());
                                attrs.add(new NNAttr(currAttrName, nestedNN, ts.offset(), false));
                                state = INNN;
                                //I need to skip what was parsed in the nested annotation in this parser
                                if (ts.move(nestedNN.getEndOffset()) == 0) {
                                    if(!ts.moveNext()) {
                                                        ts.movePrevious();
                                                    }
                                }
                                titk = ts.token();
                                continue; //next loop
                            case RPAREN:
                                lparopened--;
                                if(lparopened<0){
                                    state = INNN;
                                    attrs.add(new NNAttr(currAttrName, currAttrValue, currAttrStartOffset, currAttrQuated));
                                    ts.movePrevious();
                                    break;
                                }
                            case COMMA:
                                    state = INNN;
                                    attrs.add(new NNAttr(currAttrName, currAttrValue, currAttrStartOffset, currAttrQuated));
                                    break;
                            default:
                                //ERROR => recover
                                //set the start offset of the value to the offset of the equator + 1
                                attrs.add(new NNAttr(currAttrName, "", eqOffset + 1, false));
                                state = INNN;
                                break;
                        }
                }
                
                if(state == ERROR) {
                    //return what we parser so far to be error recovery as much as possible
                    nnend = ts.offset() + titk.text().toString().length();
                    CC newNN = new CC(nnName, attrs, nnstart, nnend);
                    return newNN;
                }
                if(!ts.moveNext()) {
                    break;
                }
                titk = ts.token();//get next token
                
            } while(titk != null);
            

        
        return null;
    }
    
    
    private int  findAnnotationStart(int offset) {
        
        if(offset>0){//0 can't contain any '@' before
            
            int parentCount = -100;

            TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            if (ts.move(offset) == 0 || !ts.moveNext()) {
                ts.movePrevious();
            }
            int len = offset - ts.offset();
            if (len > 0 && (ts.token().id() == JavaTokenId.IDENTIFIER ||
                    ts.token().id().primaryCategory().startsWith("keyword") || //NOI18N
                    ts.token().id().primaryCategory().startsWith("string") || //NOI18N
                    ts.token().id().primaryCategory().equals("literal")) //NOI18N
                    && ts.token().length() >= len) { //TODO: Use isKeyword(...) when available
            }
            Token<JavaTokenId> titk = ts.token();
            while(titk != null) {
                    JavaTokenId id = titk.id();

                if(id == JavaTokenId.RPAREN) {
                    if(parentCount == -100) {
                        parentCount = 0;
                    }
                    parentCount++;
                } else if(id == JavaTokenId.LPAREN) {
                    if(parentCount == -100) {
                        parentCount = 0;
                    }
                    parentCount--;
                } else if(id == JavaTokenId.AT) {
                    if(parentCount == -1 || parentCount == -100) { //needed if offset is not within annotation content
                        return ts.offset();
                    }
                }
                if (!ts.movePrevious()) {
                    break;
                }
                titk = ts.token();
            }
        }
        
        return -1;
    }
    
    public static class NNAttr {
        private String name;
        private Object value;
        private int valueOffset;
        private boolean quoted;
        
        NNAttr(String name, Object value, int valueOffset, boolean quoted) {
            this.name = name;
            this.value = value;
            this.valueOffset = valueOffset;
            this.quoted = quoted;
        }
        
        public String getName() {
            return name;
        }
        
        public Object getValue() {
            return value;
        }
        
        public int getValueOffset() {
            return valueOffset;
        }
        
        public boolean isValueQuoted() {
            return quoted;
        }
        
    }
    
    public static class CC {
        
        private String name;
        private List<NNAttr> attributes;
        private int startOffset, endOffset;
        
        public CC(String name, List<NNAttr> attributes, int startOffset, int endOffset) {
            this.name = name;
            this.attributes = attributes;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public String getName() {
            return name;
        }
        
        public List<NNAttr> getAttributesList() {
            return attributes;
        }
        
        public Map<String,Object> getAttributes() {
            HashMap<String,Object> map = new HashMap<>();
            for(NNAttr nnattr : getAttributesList()) {
                map.put(nnattr.getName(), nnattr.getValue());
            }
            return map;
        }
        
        public NNAttr getAttributeForOffset(int offset) {
            NNAttr prevnn = null;
            for(NNAttr nnattr : getAttributesList()) {
                if(nnattr.getValueOffset() >= offset) {
                    break;
                }
                prevnn = nnattr;
            }
            
            if(prevnn == null) {
                return null;
            }
            
            int nnEndOffset = prevnn.getValueOffset() + prevnn.getValue().toString().length() + (prevnn.isValueQuoted() ? 2 : 0);
            if(nnEndOffset >= offset && prevnn.getValueOffset() <= offset) {
                return prevnn;
            } else {
                return null;
            }
        }
        
        public int getStartOffset() {
            return startOffset;
        }
        
        public int getEndOffset() {
            return endOffset;
        }
        
        @Override
        public String toString() {
            //just debug purposes -> no need for superb performance
            String text = "@" + getName() + " [" + getStartOffset() + " - " + getEndOffset() + "](";
            for(NNAttr nnattr : getAttributesList()) {
                String key = nnattr.getName();
                String value = nnattr.getValue().toString();
                text+=key+"="+value+ " (" + nnattr.getValueOffset() + ") ,";
            }
            text = text.substring(0, text.length() -1);
            text+=")";
            return text;
        }
    }
    
    public static class MD{
        private final String methodName;
        private final boolean withQ;
        private final boolean insideParam;
        private int valueOffset;
        private final String value;
        public MD(String methodName, String value, int valueOffset, boolean withQ, boolean insideParam){
            this.methodName = methodName;
            this.withQ = withQ;
            this.insideParam = insideParam;
            this.valueOffset = valueOffset;
            this.value = value;
        }

        /**
         * @return the methodName
         */
        public String getMethodName() {
            return methodName;
        }

        /**
         * @return the withQ
         */
        public boolean isWithQ() {
            return withQ;
        }

        /**
         * @return the insideParam
         */
        public boolean isInsideParam() {
            return insideParam;
        }

        /**
         * @return the valueOffset
         */
        public int getValueOffset() {
            return valueOffset;
        }
        
        public String getValue() {
            return value;
        }
    }
    public static  final String  CREATE_QUERY="createQuery";//NOI18N
    public static  final String  CREATE_NAMEDQUERY="createNamedQuery";//NOI18N

}
