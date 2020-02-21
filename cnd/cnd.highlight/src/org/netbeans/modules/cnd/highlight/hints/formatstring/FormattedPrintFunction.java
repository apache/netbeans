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
package org.netbeans.modules.cnd.highlight.hints.formatstring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;

/**
 *
 */
class FormattedPrintFunction {
    private final CsmFile file;
    private final ArrayList<Parameter> parameters;
    private final String formatString;
    private final int offset;
    private static final boolean STRICT_TYPE_CHECKS;
    
    static {
        String doStrictCheck = System.getProperty("printf.check.strict"); //NOI18N
        if (doStrictCheck != null) {
            STRICT_TYPE_CHECKS = Boolean.parseBoolean(doStrictCheck);
        } else {
            STRICT_TYPE_CHECKS = false;
        }
    }
    
    public FormattedPrintFunction(CsmFile file, int offset, String formatString, ArrayList<Parameter> parameters) {
        this.file = file;
        this.offset = offset;
        this.formatString = formatString;
        this.parameters = parameters;
    }

    public List<FormatError> validate() {
        ArrayList<FormatInfo> formatInfoList = processFormatString(formatString, offset);
        List<FormatError> result = new LinkedList<>();
        if (getParametersFromFormat(formatInfoList) != parameters.size()) {
            int line = CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, offset)[0];
            int start = (int) CsmFileInfoQuery.getDefault().getOffset(file, line, 1);
            int end = (int) CsmFileInfoQuery.getDefault().getOffset(file, line+1, 1) - 1;
            result.add(new FormatError(FormatError.FormatErrorType.ARGS, null, null, start, end));
        } else {
            for (int i = 0, limit = formatInfoList.size(), pIndex = 0; i < limit; i++) {
                FormatInfo info = formatInfoList.get(i);
                List<FormatError> list = info.validateFormat();
                if (list.isEmpty() && !info.specifier().equals("%")) {  // NOI18N
                    String wType = null;
                    String pType = null;
                    String type = null;
                    if (pIndex < parameters.size() && info.hasWidthWildcard()) {
                        wType = getParameterType(parameters.get(pIndex).getValue(), parameters.get(pIndex).getOffset(), file);
                        if (wType != null && !wType.equals("int")) {  // NOI18N
                            result.add(new FormatError(FormatError.FormatErrorType.TYPE_WILDCARD, "Width", null, info.startOffset, info.endOffset));  // NOI18N
                        }
                        pIndex++;
                    }
                    if (pIndex < parameters.size() && info.hasPrecisionWildcard()) {
                        pType = getParameterType(parameters.get(pIndex).getValue(), parameters.get(pIndex).getOffset(), file);
                        if (pType != null && !pType.equals("int")) {  // NOI18N
                            result.add(new FormatError(FormatError.FormatErrorType.TYPE_WILDCARD, "Precision", null, info.startOffset, info.endOffset));  // NOI18N
                        }
                        pIndex++;
                    }
                    if (pIndex < parameters.size()) {
                        if (parameters.get(pIndex).resolveType()) {
                            type = getParameterType(parameters.get(pIndex).getValue(), parameters.get(pIndex).getOffset(), file);
                            if (type != null) {
                                String fType = info.getFullType();
                                List<String> validFlags = Utilities.typeToFormat(type);
                                if (!validFlags.isEmpty() && !validFlags.contains(fType)) {
                                    result.add(new FormatError(FormatError.FormatErrorType.TYPE_MISMATCH, type, fType, info.startOffset, info.endOffset));
                                } else if (validFlags.isEmpty() && !fType.equals("p") && STRICT_TYPE_CHECKS) {  // NOI18N
                                    result.add(new FormatError(FormatError.FormatErrorType.TYPE_MISMATCH, type, fType, info.startOffset, info.endOffset));
                                }
                            }
                        }
                    }
                    pIndex++;
                } else if (!list.isEmpty()) {
                    result.addAll(list);
                    pIndex++;
                }
            }
        }
        return result;
    }

    private ArrayList<FormatInfo> processFormatString(String format, int offset) {
        ArrayList<FormatInfo> result = new ArrayList<>();
        FormatInfo info = new FormatInfo();
        ConversionState state = ConversionState.DEFAULT;
        int startOffset = 0;
        int endOffset = 0;
        for (int i = 0, limit = format.length(); i < limit; i++) {
            startOffset++;
            endOffset++;
            if (format.charAt(i) == '%' && state == ConversionState.DEFAULT) {  // NOI18N
                state = ConversionState.START;
                info = new FormatInfo();
                info.setStartOffset(offset+startOffset);
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.charAt(i) == FormatFlag.APOSTROPHE.character()) {
                state = ConversionState.FLAGS;
                info.addFormatFlag(FormatFlag.APOSTROPHE);
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.charAt(i) == FormatFlag.HASH.character()) {
                state = ConversionState.FLAGS;
                info.addFormatFlag(FormatFlag.HASH);
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.charAt(i) == FormatFlag.ZERO.character()) {
                state = ConversionState.FLAGS;
                info.addFormatFlag(FormatFlag.ZERO);
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.substring(i, i+1).matches("-|\\+|\\s")) { // NOI18N
                state = ConversionState.FLAGS;
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS) && format.substring(i, i+1).matches("[0-9]|\\*")) { // NOI18N
                state = ConversionState.WIDTH;
                if (format.charAt(i) == '*') { // NOI18N
                    info.setWidthWildcardFlag(true);
                }
            }  else if (state == ConversionState.WIDTH && Character.isDigit(format.charAt(i))) {
                continue;
            } else if ((state == ConversionState.START || state == ConversionState.FLAGS || state == ConversionState.WIDTH) && format.charAt(i) == '.') {
                state = ConversionState.PRECISION;
            } else if (state == ConversionState.PRECISION && format.substring(i, i+1).matches("[0-9]|\\*")) { // NOI18N
                if (format.charAt(i) == '*') { // NOI18N
                    info.setPrecisionWildcardFlag(true);
                }
            } else if (state != ConversionState.DEFAULT) {
                if ((i+2) < format.length() && format.substring(i, i+2).equals("hh")) { // NOI18N
                    startOffset++;
                    endOffset++;
                    info.setLengthFlag(LengthFlag.hh);
                    i++;
                } else if (format.charAt(i) == 'h') { // NOI18N
                    info.setLengthFlag(LengthFlag.h);
                } else if (format.charAt(i) == 'j') { // NOI18N
                    info.setLengthFlag(LengthFlag.j);
                } else if (format.charAt(i) == 'z') { // NOI18N
                    info.setLengthFlag(LengthFlag.z);
                } else if (format.charAt(i) == 't') { // NOI18N
                    info.setLengthFlag(LengthFlag.t);
                } else if ((i+2) < format.length() && format.substring(i, i+2).equals("ll")) { // NOI18N
                    startOffset++;
                    endOffset++;
                    info.setLengthFlag(LengthFlag.ll);
                    i++;
                } else if (format.charAt(i) == 'l') { // NOI18N
                    info.setLengthFlag(LengthFlag.l);
                } else if (format.charAt(i) == 'L') { // NOI18N
                    info.setLengthFlag(LengthFlag.L);
                } else {
                    info.setSpecifier(String.valueOf(format.charAt(i)));
                    info.setEndOffset(offset+endOffset);
                    result.add(info);
                    state = ConversionState.DEFAULT;
                }
            }
        }
        return result;
    }

    private int getParametersFromFormat(Collection<FormatInfo> info) {
        int result = 0;
        for (FormatInfo i : info) {
            if (!i.specifier().equals("%")) {  // NOI18N
                result++;
            }
            if (i.hasPrecisionWildcard()) {
                result++;
            }
            if (i.hasWidthWildcard()) {
                result++;
            }
        }
        return result;
    }

    private String getParameterType(String value, int offset, CsmFile file) {
        DummyResolvedTypeHandler handler = new DummyResolvedTypeHandler();
        CsmExpressionResolver.resolveType(value
                                         ,file
                                         ,offset
                                         ,null
                                         ,handler);

        if (handler.type != null) {
            CsmClassifier clsf;
            CsmDeclaration.Kind kind;
            CsmClassifier handlerClassifier = null;
            CsmType handlerType = handler.type;
            if ((clsf = handlerType.getClassifier()) != null && (kind = clsf.getKind()) != null && kind.equals(CsmDeclaration.Kind.TYPEDEF)) {
                switch (handlerType.getCanonicalText().toString()) {
                    case "intmax_t":    case "intmax_t*":   // NOI18N
                    case "uintmax_t":   case "uintmax_t*":  // NOI18N
                    case "size_t":      case "size_t*":     // NOI18N
                    case "ptrdiff_t":   case "ptrdiff_t*":  // NOI18N
                    case "wint_t":      case "wint_t*":     // NOI18N
                    case "wchar_t":     case "wchar_t*":    // NOI18N
                        break;
                    default:
                        handlerClassifier = CsmClassifierResolver.getDefault().getTypeClassifier(handlerType, file, offset, true);
                        break;
                }
            } 
            if (handlerClassifier == null) {
                handlerClassifier = handlerType.getClassifier();
            }
            StringBuilder result = new StringBuilder(handlerClassifier.getName().toString().replace("const", "").replace("&", ""));  // NOI18N
            for (int i = 0, limit = Math.max(handlerType.getArrayDepth(), handlerType.getPointerDepth()); i < limit; i++) {
                result.append("*");  // NOI18N
            }
            return result.toString();
        }
        return null;
    }

    private static class DummyResolvedTypeHandler implements CsmExpressionResolver.ResolvedTypeHandler {

        public CsmType type;

        @Override
        public void process(CsmType resolvedType) {
            type = resolvedType;
        }
    }
    
    /*
     * Format specification fields:
     *     %[flags][min field width][precision][length]conversion specifier
     * where:
     *     flags: #,0,-,+, ,'
     *     length: h,hh,l,ll,j,z,t,L
     *     conversion specifier: d,i,o,u,x,X,f,F,e,E,g,G,a,A,c,s,p,n,C,S,%
     */
    private static enum ConversionState {
        DEFAULT,
        START,
        FLAGS,
        WIDTH,
        PRECISION,
        CONVERSION
    }
    
    private static enum FormatFlag {
        APOSTROPHE('\'', 0b110100110011110000000),  // NOI18N
        MINUS('-', 0b111111111111111111111),        // NOI18N
        PLUS('+', 0b111111111111111111111),         // NOI18N
        SPACE(' ', 0b111111111111111111111),        // NOI18N
        HASH('#', 0b001011111111110000000),         // NOI18N
        ZERO('0', 0b111111111111110000000);         // NOI18N
        
        private final char flag;
        private final int mask;
        
        FormatFlag(char flag, int mask) {
            this.flag = flag;
            this.mask = mask;
        }
        
        public char character() {
            return flag;
        }
        
        public int getMask() {
            return mask;
        }
    }
    
    private static enum LengthFlag {
        h("h", 0b111111000000000001000),    // NOI18N
        hh("hh", 0b111111000000000001000),  // NOI18N
        l("l", 0b111111111111111101000),    // NOI18N
        ll("ll", 0b111111111111110001000),  // NOI18N
        j("j", 0b111111000000000001000),    // NOI18N
        z("z", 0b111111000000000001000),    // NOI18N
        t("t", 0b111111000000000001000),    // NOI18N
        L("L", 0b000000111111110000000);    // NOI18N
        
        private final String flag;
        private final int mask;
        
        LengthFlag(String flag, int mask) {
            this.flag = flag;
            this.mask = mask;
        }
        
        @Override
        public String toString() {
            return flag;
        }
        
        public int getMask() {
            return mask;
        }
    }
    
    private static class FormatInfo {
        private static final List<String> conversionCharacters = Arrays.asList("d","i","o","u","x","X","f","F","e","E","g","G","a","A","c","s","p","n","C","S","%"); // NOI18N
        private final List<FormatFlag> formatFlags;
        private LengthFlag lengthFlag;
        private String specifier;
        private boolean hasWidthWildcard = false;
        private boolean hasPrecisionWildcard = false;
        private int startOffset;
        private int endOffset;
        
        public FormatInfo() {
            formatFlags = new LinkedList<>();
        }
        
        public boolean hasWidthWildcard() {
            return hasWidthWildcard;
        }

        public boolean hasPrecisionWildcard() {
            return hasPrecisionWildcard;
        }

        public void setSpecifier(String specifier) {
            this.specifier = specifier;
        }

        public void setLengthFlag(LengthFlag lengthFlag) {
            this.lengthFlag = lengthFlag;
        }
        
        public void addFormatFlag(FormatFlag flag) {
            formatFlags.add(flag);
        }

        public void setWidthWildcardFlag(boolean flag) {
            hasWidthWildcard = flag;
        }

        public void setPrecisionWildcardFlag(boolean flag) {
            hasPrecisionWildcard = flag;
        }
        
        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }
        
        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }
        
        public int startOffset() {
            return startOffset;
        }
        
        public int endOffset() {
            return endOffset;
        }
        
        public String specifier() {
            return specifier;
        }
        
        public String getFullType() {
            StringBuilder result = new StringBuilder();
            if (lengthFlag != null) {
                result.append(lengthFlag);
            }
            result.append(specifier);
            return result.toString();
        }
        
        public List<FormatError> validateFormat() {
            if (!conversionCharacters.contains(specifier)) {
                return Collections.singletonList(new FormatError(FormatError.FormatErrorType.TYPE_NOTEXIST, null, specifier, startOffset, endOffset));
            }
            List<FormatError> result = new LinkedList<>();
            
            // validate format flags
            for (FormatFlag flag : formatFlags) {
                int filter = 0b100000000000000000000;
                for (int i = 0, limit = conversionCharacters.size(); i < limit; i++) {
                    if ((flag.getMask() & filter) == 0 && specifier.equals(conversionCharacters.get(i))) {
                        result.add(new FormatError(FormatError.FormatErrorType.FLAG, String.valueOf(flag.character()), specifier, startOffset, endOffset));
                        break;
                    }
                    filter >>= 1;
                }
            }
            
            // validate length flags
            if (lengthFlag != null) {
                int filter = 0b100000000000000000000;
                for (int i = 0, limit = conversionCharacters.size(); i < limit; i++) {
                    if ((lengthFlag.getMask() & filter) == 0 && specifier.equals(conversionCharacters.get(i))) {
                        result.add(new FormatError(FormatError.FormatErrorType.LENGTH, lengthFlag.toString(), specifier, startOffset, endOffset));
                        break;
                    }
                    filter >>= 1;
                }
            }
            return result;
        }
    }
}
