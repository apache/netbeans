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
package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.annotation.PhpAnnotations;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocStaticAccessType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Petr Pisl
 */
public class PHPDocCommentParser {

    private static final Object LINE_PARSERS_LOCK = new Object();

    //@GuardedBy("LINE_PARSERS_LOCK")
    private static final List<AnnotationLineParser> LINE_PARSERS = new CopyOnWriteArrayList<>(PhpAnnotations.getLineParsers());
    static {
        PhpAnnotations.addLineParsersListener(new LineParsersListener());
    }

    private static class LineParsersListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            synchronized (LINE_PARSERS_LOCK) {
                LINE_PARSERS.clear();
                LINE_PARSERS.addAll(PhpAnnotations.getLineParsers());
            }
        }

    }

    private static final Pattern pattern = Pattern.compile("[\r\n][ \\t]*[*]?[ \\t]*"); // NOI18N

    /**
     * Tags that define something of a type
     */
    private static final List<AnnotationParsedLine> PHP_DOC_VAR_TYPE_TAGS = new ArrayList<>();
    static {
        PHP_DOC_VAR_TYPE_TAGS.add(PHPDocTag.Type.PARAM);
        PHP_DOC_VAR_TYPE_TAGS.add(PHPDocTag.Type.PROPERTY);
        PHP_DOC_VAR_TYPE_TAGS.add(PHPDocTag.Type.GLOBAL);
        PHP_DOC_VAR_TYPE_TAGS.add(PHPDocTag.Type.PROPERTY_READ);
        PHP_DOC_VAR_TYPE_TAGS.add(PHPDocTag.Type.PROPERTY_WRITE);
    }

    public PHPDocCommentParser() {
    }

    /**
     *
     * @param startOffset this is offset of the comment in the document. It's used
     * for creating ASTNodes.
     * @param endOffset
     * @param comment
     * @return
     */
    public PHPDocBlock parse(final int startOffset, final int endOffset, final String comment) {
        assert startOffset <= endOffset;
        List<PHPDocTag> tags = new ArrayList<>(); // list of tags
        String blockDescription = "";   // description of the block
        if (comment == null || comment.length() == 0) {
            // issue #142644
            return new PHPDocBlock(startOffset, endOffset, blockDescription, tags);
        }

        Matcher matcher = pattern.matcher(comment);
        int index = 0;
        String line;               // one line of the blog
        String description = "";        // temporary holder for description of block description or tag
        AnnotationParsedLine lastTag = null;
        int lastStartIndex = 0;
        int lastEndIndex = comment.length();

        while (matcher.find()) {
            line = comment.substring(index, matcher.start()).trim();
            if (index == 0) { // remove * from the first line
                line = removeStarAndTrim(line);
            }
            AnnotationParsedLine tagType = findTagOnLine(line);
            if (tagType != null) { // is a tag defined on the line
                if (lastTag == null) { // is it the first tag in the block
                    blockDescription = description.length() > 0 && description.charAt(description.length() - 1) == '\n'
                            ? description.substring(0, description.length() - 1)
                            : description;  // save the block description
                } else { // create last recognized tag
                    PHPDocTag tag = createTag(
                            startOffset + 3 + lastStartIndex,
                            startOffset + 3 + lastEndIndex,
                            lastTag,
                            description.substring(0, description.length() - 1),
                            comment,
                            startOffset + 3);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
                lastTag = tagType;  // remember the recognized tag
                lastStartIndex = index;
                description = "";
                int from = tagType.getName().length() + 1;
                if (from >= 0 && from <= line.length()) {
                    line = line.substring(from); // and the first line of description of the tag
                }
            }
            index = matcher.end();
            lastEndIndex = matcher.start();
            description = description + line + "\n";
        }
        // last line
        if (index == 0) {  // there is only one line comment
            line = removeStarAndTrim(comment);
        } else {
            line = comment.substring(index).trim();
        }
        AnnotationParsedLine tagType = findTagOnLine(line);
        if (tagType != null) {  // is defined a tag on the last line
            if (lastTag == null) {
                blockDescription = description.trim();
            } else {
                PHPDocTag tag = createTag(
                        startOffset + 3 + lastStartIndex,
                        startOffset + 3 + lastEndIndex,
                        lastTag,
                        description.substring(0, description.length() - 1),
                        comment,
                        startOffset + 3);
                if (tag != null) {
                    tags.add(tag);
                }
            }
            int endOffsetOfTag = tagType.getName().length() + 1;
            if (endOffsetOfTag <= line.length()) {
                line = line.substring(endOffsetOfTag).trim();
                PHPDocTag tag = createTag(startOffset + 3 + index, startOffset + 3 + comment.length(), tagType, line, comment, startOffset + 3);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        } else {
            if (lastTag == null) {  // thre is not defined a tag before the last line
                blockDescription = description + line;
            } else {
                description = description + line;
                String tagDescription = description.length() > 0 && description.charAt(description.length() - 1) == '\n'
                        ? description.substring(0, description.length() - 1)
                        : description;
                PHPDocTag tag = createTag(
                        startOffset + 3 + lastStartIndex,
                        startOffset + 3 + lastEndIndex,
                        lastTag,
                        tagDescription,
                        comment,
                        startOffset + 3);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        }
        return new PHPDocBlock(Math.min(startOffset + 3, endOffset), endOffset, blockDescription, tags);
    }

    private PHPDocTag createTag(int start, int end, AnnotationParsedLine type, String description, String originalComment, int originalCommentStart) {
        final Map<OffsetRange, String> types = type.getTypes();
        if (types.isEmpty()) {
            List<PHPDocTypeNode> docTypes = findTypes(description, start, originalComment, originalCommentStart, type);
            if (PHP_DOC_VAR_TYPE_TAGS.contains(type)) {
                String variable = getVaribleName(description);
                PHPDocNode varibaleNode = null;
                if (variable != null) {
                    int startOfVariable = findStartOfDocNode(originalComment, originalCommentStart, variable, start);
                    if (startOfVariable != -1) {
                        varibaleNode = new PHPDocNode(startOfVariable, startOfVariable + variable.length(), variable);
                    }
                } else if (type.equals(PHPDocTag.Type.PARAM)) {
                    varibaleNode = new PHPDocNode(start, start, ""); //NOI18N
                }
                if (varibaleNode != null) {
                    return new PHPDocVarTypeTag(start, end, type, description, docTypes, varibaleNode);
                }
                return null;
            } else if (type.equals(PHPDocTag.Type.METHOD)) {
                String name = getMethodName(description);
                if (name != null) {
                    boolean isStatic = description.trim().startsWith("static"); // NOI18N
                    int startOfVariable = findStartOfDocNode(originalComment, originalCommentStart, name, start);
                    if (startOfVariable != -1) {
                        PHPDocNode methodNode = new PHPDocNode(startOfVariable, startOfVariable + name.length(), name);
                        int startOfDescription = findStartOfDocNode(originalComment, originalCommentStart, description, start);
                        if (startOfDescription != -1) {
                            List<PHPDocVarTypeTag> params = findMethodParams(description, startOfDescription);
                            return new PHPDocMethodTag(start, end, type, docTypes, methodNode, params, description, isStatic);
                        }
                    }
                }
                return null;
            } else if (type.equals(PHPDocTag.Type.RETURN) || type.equals(PHPDocTag.Type.VAR) || type.equals(PHPDocTag.Type.MIXIN)) {
                return new PHPDocTypeTag(start, end, type, description, docTypes);
            }
            return new PHPDocTag(start, end, type, description);
        } else {
            return new PHPDocTypeTag(start, end, type, type.getDescription(), resolveTypes(types, start + (type.startsWithAnnotation() ? 1 : 0)));
        }
    }

    private List<PHPDocTypeNode> resolveTypes(final Map<OffsetRange, String> types, final int lineStart) {
        final List<PHPDocTypeNode> result = new ArrayList<>();
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            result.add(new PHPDocTypeNode(lineStart + entry.getKey().getStart(), lineStart + entry.getKey().getEnd(), entry.getValue(), false));
        }
        return result;
    }

    private List<PHPDocTypeNode> findTypes(String description, int startDescription, String originalComment, int originalCommentStart) {
        return findTypes(description, startDescription, originalComment, originalCommentStart, PHPDocTypeTag.Type.PARAM);
    }

    private List<PHPDocTypeNode> findTypes(String description, int startDescription, String originalComment, int originalCommentStart, AnnotationParsedLine tagType) {
        if (StringUtils.isEmpty(description)) {
            return Collections.emptyList();
        }

        List<PHPDocTypeNode> result = new ArrayList<>();
        int startPosition = startDescription;
        for (String stype : getTypes(description, tagType)) {
            stype = removeHTMLTags(stype);
            stype = sanitizeShapes(stype);
            stype = sanitizeBraces(stype);
            int startDocNode = findStartOfDocNode(originalComment, originalCommentStart, stype, startPosition);
            if (startDocNode == -1) {
                continue;
            }
            // move start position to find the position of the same class name
            // e.g. (X&Y)|(X&Z)
            startPosition = startDocNode + stype.length();
            int index = stype.indexOf("::");    //NOI18N
            boolean isArray = (stype.indexOf('[') > 0 && stype.indexOf(']') > 0);
            if (isArray) {
                stype = stype.substring(0, stype.indexOf('[')).trim();
            }
            PHPDocTypeNode docType;
            if (index == -1) {
                docType = new PHPDocTypeNode(startDocNode, startDocNode + stype.length(), stype, isArray);
            } else {
                String className = stype.substring(0, index);
                String constantName = stype.substring(index + 2);
                PHPDocNode classNameNode = new PHPDocNode(startDocNode, startDocNode + className.length(), className);
                PHPDocNode constantNode = new PHPDocNode(startDocNode + className.length() + 2, startDocNode + stype.length(), constantName);
                docType = new PHPDocStaticAccessType(startDocNode, startDocNode + stype.length(), stype, classNameNode, constantNode);
            }
            result.add(docType);
        }
        return result;
    }

    private List<String> getTypes(String description, AnnotationParsedLine tagType) {
        String[] tokens = description.trim().split("[ ]+"); //NOI18N
        if (isMethodTag(tagType) && tokens.length > 0 && tokens[0].equals(Type.STATIC)) {
            tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
        }
        ArrayList<String> types = new ArrayList<>();
        if (tokens.length > 0 && (isReturnTag(tagType) || !tokens[0].startsWith("$"))) { //NOI18N
            if (findParameterStartPosition(tokens[0]) != -1) {
                // e.g. @method voidReturn((X&Y)|Z $param)
                types.add(Type.VOID);
            } else if (tokens[0].indexOf('|') > -1 || tokens[0].indexOf('&') > -1) {
                String[] ttokens = tokens[0].split("[|&]"); //NOI18N
                for (String ttoken : ttokens) {
                    types.add(ttoken.trim());
                }
            } else if (tokens[0].indexOf('(') > 0) {
                // e.g. @method getSomething(int $i)
                // NOTE: add void type but it is not shown as a return type in doc popup
                // because it doesn't exist
                types.add(Type.VOID);
            } else {
                types.add(tokens[0].trim());
            }
        }

        return types;
    }

    private String getVaribleName(String description) {
        String[] tokens = description.trim().split("[ \n\t]+"); //NOI18N
        String variable = null;

        if (tokens.length > 0 && tokens[0].length() > 0 && tokens[0].charAt(0) == '$') {
            variable = tokens[0].trim();
        } else if ((tokens.length > 1) && (tokens[1].charAt(0) == '$')) {
            variable = tokens[1].trim();
        }
        return variable;
    }

    private String getMethodName(String description) {
        String name = null;
        int index = findParameterStartPosition(description);
        if (index > 0) {
            name = description.substring(0, index);
            index = name.lastIndexOf(' ');
            if (index >= 0) { // e.g. " methodName" has whitespace at 0
                name = name.substring(index + 1);
            }
        } else {
            // probably defined without () after the name
            // then we expect that the name is after the first space
            String desc = description.trim();
            if (desc.startsWith("static ")) { // NOI18N
                desc = desc.substring("static ".length()); // NOI18N
            }
            String[] tokens = desc.trim().split("[ \n\t]+"); //NOI18N
            if (tokens.length > 1) {
                name = tokens[1];
            }
        }
        return name;
    }

    private List<PHPDocVarTypeTag> findMethodParams(String description, int startOfDescription) {
        List<PHPDocVarTypeTag> result = new ArrayList<>();
        int position = startOfDescription;
        ParametersExtractor parametersExtractor = ParametersExtractorImpl.create();
        String parameters = parametersExtractor.extract(description);
        position += parametersExtractor.getPosition();
        if (parameters.length() > 0) {
            String[] tokens = parameters.split("[,]+"); //NOI18N
            String paramName;
            for (String token : tokens) {
                paramName = getVaribleName(token.trim());
                if (paramName != null) {
                    int startOfParamName = findStartOfDocNode(description, startOfDescription, paramName, position);
                    if (startOfParamName != -1) {
                        PHPDocNode paramNameNode = new PHPDocNode(startOfParamName, startOfParamName + paramName.length(), paramName);
                        List<PHPDocTypeNode> types = token.trim().indexOf(' ') > -1
                                ? findTypes(token, position, description, startOfDescription)
                                : Collections.EMPTY_LIST;
                        result.add(new PHPDocVarTypeTag(position, startOfParamName + paramName.length(), PHPDocTag.Type.PARAM, token, types, paramNameNode));
                    }
                }
                position = position + token.length() + 1;
            }
        }
        return result;
    }

    private String removeHTMLTags(String text) {
        String value = text;
        int startTagIndex = value.indexOf('<');
        if (startTagIndex > -1) {
            value = value.substring(0, startTagIndex).trim();
        }
        return value;
    }

    /**
     * Remove `{'key': type}`.
     *
     * e.g. {@code array{'foo': int}}, {@code object{'foo': int, "bar": string}}
     *
     * @see https://phpstan.org/writing-php-code/phpdoc-types#array-shapes
     * @see https://phpstan.org/writing-php-code/phpdoc-types#object-shapes
     *
     * @param type the type
     * @return the sanitized type
     */
    private String sanitizeShapes(String type) {
        String sanitizedType = type;
        int startIndex = sanitizedType.indexOf("{"); // NOI18N
        if (startIndex > -1) {
            sanitizedType = sanitizedType.substring(0, startIndex).trim();
        }
        return sanitizedType;
    }

    private String sanitizeBraces(String type) {
        String sanitizedType = type;
        if (sanitizedType.startsWith("(")) { // NOI18N
            sanitizedType = sanitizedType.substring(1).trim();
        } else if (sanitizedType.endsWith(")")) { // NOI18N
            sanitizedType = sanitizedType.substring(0, sanitizedType.length() - 1);
        }
        return sanitizedType;
    }

    /**
     * Find the start position of the specified string in the comment.
     *
     * @param originalComment the comment
     * @param originalStart the offset of the original comment
     * @param what the target string
     * @param from the start offset
     * @return the start position of the specified string if it is found,
     * otherwise -1.
     */
    private int findStartOfDocNode(String originalComment, int originalStart, String what, int from) {
        int pos = originalComment.indexOf(what, from - originalStart);
        return pos == -1 ? pos : originalStart + pos;
    }

    private String removeStarAndTrim(String text) {
        text = text.trim();
        if (text.length() > 0 && text.charAt(0) == '*') {
            text = text.substring(1).trim();
        }
        return text;
    }

    private AnnotationParsedLine findTagOnLine(String line) {
        AnnotationParsedLine result = null;
        if (line.length() > 0 && line.charAt(0) == '@') {
            String[] tokens = line.trim().split("[ \t]+");
            if (tokens.length > 0) {
                final String name = tokens[0].substring(1);
                String tag = name.toUpperCase();
                if (tag.indexOf('-') > -1) {
                    tag = tag.replace('-', '_');
                }
                try {
                    result = PHPDocTag.Type.valueOf(tag);
                } catch (IllegalArgumentException iae) {
                    // we are not able to thread such tag
                    result = fetchCustomAnnotationLine(line.substring(1));
                    if (result == null) {
                        result = new UnknownAnnotationLine(name, composeDescription(tokens));
                    }
                }
            }
        } else if (line.contains("@")) {
            result = fetchCustomAnnotationLine(line);
        }
        return result;
    }

    private static String composeDescription(String[] tokens) {
        assert tokens.length > 0;
        List<String> tokenList = new ArrayList<>(Arrays.asList(tokens));
        tokenList.remove(0); // remove annotation name
        return StringUtils.implode(tokenList, " ");
    }

    private AnnotationParsedLine fetchCustomAnnotationLine(final String line) {
        AnnotationParsedLine result = null;
        for (AnnotationLineParser annotationLineParser : LINE_PARSERS) {
            AnnotationParsedLine parsedLine = annotationLineParser.parse(line);
            if (parsedLine != null) {
                result = parsedLine;
                break;
            }
        }
        return result;
    }

    private static boolean isReturnTag(AnnotationParsedLine type) {
        return PHPDocTypeTag.Type.RETURN == type;
    }

    private static boolean isMethodTag(AnnotationParsedLine type) {
        return PHPDocTypeTag.Type.METHOD == type;
    }

    private static int findParameterStartPosition(String description) {
        // e.g. static (X&Y)|Z method((X&Y)|Z $param) someting...
        // return type may have a dnf type i.e. it has "("
        // so, also check the char just before "("
        char previousChar = ' ';
        for (int i = 0; i < description.length(); i++) {
            switch (description.charAt(i)) {
                case '(':
                    if (previousChar != '|' && previousChar != '&' && previousChar != ' ') {
                        return i;
                    }
                    break;
                default:
                    break;
            }
            previousChar = description.charAt(i);
        }
        return -1;
    }

    private static final class ParametersExtractorImpl implements ParametersExtractor {

        private int position = 0;
        private String parameters = "";
        private String subDescription = "";
        private boolean hasParameters = false;
        private int bracketBalance = 0;
        private int paramsStart = 0;
        private int paramsEnd = 0;

        public static ParametersExtractor create() {
            return new ParametersExtractorImpl();
        }

        private ParametersExtractorImpl() {
        }

        @Override
        public String extract(String description) {
            int index = findParameterStartPosition(description);
            int possibleParamIndex = description.indexOf('$');
            if (index > -1 && possibleParamIndex > -1) {
                position += index;
                subDescription = description.substring(index);
                processSubDescription();
            }
            return parameters;
        }

        private void processSubDescription() {
            for (int i = 0; i < subDescription.length(); i++) {
                findMatchingBraces(i);
                if (!parameters.isEmpty()) {
                    break;
                }
            }
        }

        private void findMatchingBraces(int i) {
            char ch = subDescription.charAt(i);
            if (ch == '(') {
                processLeftBrace(i);
            } else if (ch == ')') {
                processRightBrace(i);
            } else if (Character.isWhitespace(ch)) {
                return;
            } else {
                processNonWhiteCharacter();
            }
            checkParameters();
        }

        private void processLeftBrace(int i) {
            bracketBalance++;
            if (bracketBalance == 1) {
                paramsStart = i + 1;
            }
        }

        private void processRightBrace(int i) {
            bracketBalance--;
            if (bracketBalance == 0) {
                paramsEnd = i;
            }
        }

        private void processNonWhiteCharacter() {
            if (bracketBalance == 0) {
                hasParameters = false;
            } else {
                hasParameters = true;
            }
        }

        private void checkParameters() {
            if (hasParameters && bracketBalance == 0) {
                parameters = subDescription.substring(paramsStart, paramsEnd);
                position += paramsStart;
            }
        }

        @Override
        public int getPosition() {
            return position;
        }

    }

    private interface ParametersExtractor {

        /**
         * Extracts part of parameters from magic method tag description.
         *
         * @param description Line of magic method tag description.
         * @return Extracted parameters part.
         */
        String extract(String description);

        /**
         * Returns start position of parameters part from magic method tag description line.
         *
         * @return Start position of parameters part.
         */
        int getPosition();

    }

}
