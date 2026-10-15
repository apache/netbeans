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
package org.netbeans.modules.php.editor.completion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.index.PHPDOCTagElement;
import org.netbeans.modules.php.editor.index.PredefinedSymbolElement;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.PHPDocCommentParser;
import org.netbeans.modules.php.editor.parser.annotation.LinkParsedLine;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
@NbBundle.Messages("PHPDocNotFound=PHPDoc not found")
final class DocRenderer {
    private static final String TD_STYLE = "style=\"text-aling:left; border-width: 0px;padding: 1px;padding:3px;\" ";  //NOI18N
    private static final String TD_STYLE_MAX_WIDTH = "style=\"text-aling:left; border-width: 0px;padding: 1px;padding:3px;width:80%;\" ";  //NOI18N
    private static final String TABLE_STYLE = "style=\"border: 0px; width: 100%;\""; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(PHPCodeCompletion.class.getName());

    private DocRenderer() {
    }

    static Documentation document(ParserResult info, ElementHandle element) {
        if (element instanceof PHPDOCTagElement) {
            PHPDOCTagElement pHPDOCTagElement = (PHPDOCTagElement) element;
            String doc = pHPDOCTagElement.getDoc();
            return Documentation.create(doc == null ? Bundle.PHPDocNotFound() : doc);
        }

        if (element instanceof PredefinedSymbolElement) {
            PredefinedSymbolElement predefinedSymbolElement = (PredefinedSymbolElement) element;
            String doc = predefinedSymbolElement.getDoc();
            return Documentation.create(doc == null ? Bundle.PHPDocNotFound() : doc);
        }

        if (element instanceof PhpElement) {
            return documentIndexedElement((PhpElement) element);
        }

        if (element instanceof TypeMemberElement) {
            // XXX can pass through here?
            TypeMemberElement indexedClassMember = (TypeMemberElement) element;
            return documentIndexedElement(indexedClassMember);
        }

        return null;
    }

    private static Documentation documentIndexedElement(final PhpElement indexedElement) {
        PhpDocumentation phpDocumentation = PhpDocumentation.NONE;
        final CCDocHtmlFormatter locationHeader = new CCDocHtmlFormatter();
        CCDocHtmlFormatter header = new CCDocHtmlFormatter();
        final String location = getLocation(indexedElement);
        final ElementQuery elementQuery = indexedElement.getElementQuery();
        if (location != null) {
            locationHeader.appendHtml(String.format("<div align=\"right\"><font size=-1>%s</font></div>", location));  //NOI18N
        }
        if (canBeProcessed(indexedElement)) {
            phpDocumentation = getPhpDocumentation(indexedElement, header);
            if (phpDocumentation == PhpDocumentation.NONE) {
                if (indexedElement instanceof MethodElement) {
                    ElementFilter forName = ElementFilter.forName(NameKind.exact(indexedElement.getName()));
                    ElementQuery.Index index = elementQuery.getQueryScope().isIndexScope() ? (Index) elementQuery
                            : ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(indexedElement.getFileObject()));
                    final Set<TypeElement> inheritedTypes = index.getInheritedTypes(((MethodElement) indexedElement).getType());
                    for (Iterator<TypeElement> typeIt = inheritedTypes.iterator(); phpDocumentation == PhpDocumentation.NONE && typeIt.hasNext();) {
                        final Set<MethodElement> inheritedMethods = forName.filter(index.getDeclaredMethods(typeIt.next()));
                        for (Iterator<MethodElement> methodIt = inheritedMethods.iterator(); phpDocumentation == PhpDocumentation.NONE && methodIt.hasNext();) {
                            header = new CCDocHtmlFormatter();
                            phpDocumentation = getPhpDocumentation(methodIt.next(), header);
                        }
                    }
                }
            }
        }
        return phpDocumentation.createDocumentation(locationHeader);
    }

    private static boolean canBeProcessed(PhpElement indexedElement) {
        return indexedElement != null && indexedElement.getOffset() > -1 && indexedElement.getFileObject() != null;
    }

    private static PhpDocumentation getPhpDocumentation(final PhpElement indexedElement, final CCDocHtmlFormatter header) {
        PhpDocumentation result = PhpDocumentation.NONE;
        if (canBeProcessed(indexedElement)) {
            FileObject nextFo = indexedElement.getFileObject();
            try {
                Source source = Source.create(nextFo);
                if (source != null) {
                    ASTNodeFinder nodeFinder = new ASTNodeFinder(indexedElement);
                    ParserManager.parse(Collections.singleton(source), nodeFinder);
                    PHPDocExtractor phpDocExtractor = new PHPDocExtractor(header, indexedElement, nodeFinder.getNode(), nodeFinder.getPhpDocBlock());
                    result = phpDocExtractor.getPhpDocumentation();
                }
            } catch (ParseException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return result;
    }

    @NbBundle.Messages("PHPPlatform=PHP Platform")
    private static String getLocation(PhpElement indexedElement) {
        String location = null;
        if (indexedElement.isPlatform()) {
            location = Bundle.PHPPlatform();
        } else {
            FileObject fobj = indexedElement.getFileObject();
            if (fobj != null) {
                Project project = FileOwnerQuery.getOwner(fobj);
                if (project != null) {
                    // find the appropriate source root
                    Sources sources = ProjectUtils.getSources(project);
                    // TODO the PHPSOURCE constatnt has to be published in the project api
                    for (SourceGroup group : sources.getSourceGroups("PHPSOURCE")) {
                        //NOI18N
                        String relativePath = FileUtil.getRelativePath(group.getRootFolder(), fobj);
                        if (relativePath != null) {
                            location = relativePath;
                            break;
                        }
                    }
                    if (location == null) {
                        // just to be sure, that the relative location was resolved
                        location = fobj.getPath();
                    }
                } else {
                    location = indexedElement.getFilenameUrl();
                }
            }
        }
        return location;
    }

    static final class PHPDocExtractor {
        // http://manual.phpdoc.org/HTMLSmartyConverter/HandS/phpDocumentor/tutorial_phpDocumentor.howto.pkg.html#basics.desc
        // + table (table, tr, th, td)

        private static final Pattern KEEP_TAGS_PATTERN
                = Pattern.compile("<(?!(/|b|code|br|i|kbd|li|ol|p|pre|samp|ul|var|table|tr|th|td)(\\b|\\s))", Pattern.CASE_INSENSITIVE); // NOI18N
        private static final Pattern REPLACE_NEWLINE_PATTERN = Pattern.compile("(\r?\n){2,}"); // NOI18N
        // #183594
        private static final Pattern LIST_PATTERN = Pattern.compile("(\r?\n)(?=([-+#o]\\s|\\d\\.?\\s))"); // NOI18N
        private static final Pattern DESC_HEADER_PATTERN = Pattern.compile("(\r?\n)*(.*?((\r?\n){2,}|\\.\\s*\r?\n)|.*)", Pattern.DOTALL); // NOI18N
        private static final Pattern INLINE_INHERITDOC_PATTERN = Pattern.compile("\\{@inheritdoc *\\}", Pattern.CASE_INSENSITIVE); // NOI18N
        private static final ArrayList<String> LINK_TAGS = new ArrayList<>();
        // see: https://pear.php.net/package/PhpDocumentor/docs/latest/phpDocumentor/tutorial_tags.inlineinheritdoc.pkg.html
        // phpdocumentor separates a doc comment into a header and description.
        // only description is replaced.
        // e.g.
        // /**
        //  * Header.
        //  * Description.
        //  */
        // /** {@inheritdoc} */ -> /** Description. */
        private static final boolean INHERITDOC_FOR_PHPDOCUMENTOR = Boolean.getBoolean("nb.php.editor.inheritDocForPhpdocumentor"); // NOI18N
        static volatile boolean UNIT_TEST_INHERITDOC_FOR_PHPDOCUMENTER = false; // for unit tests

        static {
            LINK_TAGS.add("@link"); // NOI18N
            LINK_TAGS.add("@see"); // NOI18N
            LINK_TAGS.add("@use"); // NOI18N
        }
        private final CCDocHtmlFormatter header;
        private final StringBuilder phpDoc = new StringBuilder();
        private final PhpElement indexedElement;
        private final List<String> links = new ArrayList<>();
        private final ASTNode node;
        @NullAllowed
        private PHPDocBlock phpDocBlock;

        public PHPDocExtractor(CCDocHtmlFormatter header, PhpElement indexedElement, ASTNode node, @NullAllowed PHPDocBlock phpDocBlock) {
            this.header = header;
            this.indexedElement = indexedElement;
            this.node = node;
            this.phpDocBlock = phpDocBlock;
        }

        public PhpDocumentation getPhpDocumentation() {
            extract();
            return PhpDocumentation.Factory.create(header, phpDoc, links);
        }

        public void cancel() {
        }

        private void extract() {
            if (node == null) {
                return;
            }
            extractHeader();
            extractPHPDoc();
        }

        private void extractHeader() {
            if (node instanceof FunctionDeclaration) {
                doFunctionDeclaration((FunctionDeclaration) node);
            } else {
                header.name(indexedElement.getKind(), true);
                header.appendText(indexedElement.getName());
                header.name(indexedElement.getKind(), false);
                String value = null;
                if (indexedElement instanceof ConstantElement) {
                    ConstantElement constant = (ConstantElement) indexedElement;
                    value = constant.getValue();
                } else if (indexedElement instanceof TypeConstantElement) {
                    TypeConstantElement constant = (TypeConstantElement) indexedElement;
                    value = constant.getValue();
                } else if (indexedElement instanceof EnumCaseElement) {
                    EnumCaseElement enumCase = (EnumCaseElement) indexedElement;
                    if (enumCase.isBacked()) {
                        value = enumCase.getValue();
                    }
                }
                if (value != null) {
                    header.appendText(" = "); //NOI18N
                    header.appendText(value);
                }
            }
            header.appendHtml("<br/><br/>"); //NOI18N
        }

        private void doFunctionDeclaration(FunctionDeclaration functionDeclaration) {
            String fname = CodeUtils.extractFunctionName(functionDeclaration);
            header.appendHtml("<font size=\"+1\">"); //NOI18N
            header.name(ElementKind.METHOD, true);
            header.appendText(fname);
            header.name(ElementKind.METHOD, false);
            header.appendHtml("</font>"); //NOI18N

            header.parameters(true);
            header.appendText("("); //NOI18N
            int paramCount = functionDeclaration.getFormalParameters().size();

            for (int i = 0; i < paramCount; i++) {
                FormalParameter param = functionDeclaration.getFormalParameters().get(i);
                if (param.getParameterType() != null) {
                    Identifier paramId = CodeUtils.extractUnqualifiedIdentifier(param.getParameterType());
                    if (paramId != null) {
                        header.type(true);
                        if (param.isNullableType()) {
                            header.appendText(CodeUtils.NULLABLE_TYPE_PREFIX);
                        }
                        header.appendText(paramId.getName() + " "); //NOI18N
                        header.type(false);
                    }
                }

                header.appendText(CodeUtils.getParamDisplayName(param));

                if (param.isOptional()) {
                    header.type(true);
                    header.appendText("="); // NOI18N

                    if (param.getDefaultValue() instanceof Scalar) {
                        Scalar scalar = (Scalar) param.getDefaultValue();
                        header.appendText(scalar.getStringValue());
                    }

                    header.type(false);
                }

                if (i + 1 < paramCount) {
                    header.appendText(", "); //NOI18N
                }
            }

            header.appendText(")"); // NOI18N
            header.parameters(false);
        }

        private void extractPHPDoc() {
            if (node instanceof PHPDocTag) {
                if (node instanceof PHPDocMethodTag) {
                    extractPHPDoc((PHPDocMethodTag) node);
                } else {
                    if (node instanceof PHPDocVarTypeTag) {
                        PHPDocVarTypeTag varTypeTag = (PHPDocVarTypeTag) node;
                        // e.g. @property (A&B)|C $property something
                        String[] values = CodeUtils.WHITE_SPACES_PATTERN.split(varTypeTag.getValue().trim(), 2);
                        String type = ""; // NOI18N
                        if (!values[0].startsWith("$")) { // NOI18N
                            type = putTypeSeparatorBetweenWhitespaces(values[0]);
                        }
                        phpDoc.append(processPhpDoc(String.format("%s<br /><table><tr><th align=\"left\">Type:</th><td>%s</td></tr></table>", // NOI18N
                                varTypeTag.getDocumentation(),
                                type)));
                    } else {
                        phpDoc.append(processPhpDoc(((PHPDocTag) node).getDocumentation()));
                    }
                }
            } else {
                extractPHPDocBlock();
            }
        }

        private void extractPHPDoc(PHPDocMethodTag methodTag) {
            StringBuilder params = new StringBuilder();
            StringBuilder returnValue = new StringBuilder();
            String description = methodTag.getDocumentation();

            if (description != null && description.length() > 0) {
                description = processPhpDoc(description);
            }

            if (methodTag.getParameters() != null && !methodTag.getParameters().isEmpty()) {
                for (PHPDocVarTypeTag tag : methodTag.getParameters()) {
                    params.append(composeParameterLine(tag));
                }
            }
            returnValue.append(composeTypesAndDescription(putTypeSeparatorBetweenWhitespaces(methodTag.getReturnType()), null));
            phpDoc.append(composeFunctionDoc(description, params.toString(), returnValue.toString(), null));
        }

        private void extractPHPDocBlock() {
            List<PHPDocVarTypeTag> params = new ArrayList<>();
            List<PHPDocTypeTag> returns = new ArrayList<>();
            StringBuilder others = new StringBuilder();

            // class, interface or method
            List<PhpElement> inheritedElements = getInheritedElements();
            List<PHPDocBlock> inheritedComments = getInheritedComments(inheritedElements);

            String description = phpDocBlock == null ? null : phpDocBlock.getDescription();
            description = composeDescription(description, inheritedComments);

            List<PHPDocTag> tags = phpDocBlock == null ? Collections.emptyList() : phpDocBlock.getTags();
            for (PHPDocTag tag : tags) {
                AnnotationParsedLine kind = tag.getKind();
                if (kind.equals(PHPDocTag.Type.PARAM)) {
                    PHPDocVarTypeTag paramTag = (PHPDocVarTypeTag) tag;
                    params.add(paramTag);
                } else if (kind.equals(PHPDocTag.Type.RETURN)) {
                    PHPDocTypeTag returnTag = (PHPDocTypeTag) tag;
                    returns.add(returnTag);
                } else if (kind.equals(PHPDocTag.Type.VAR)) {
                    PHPDocTypeTag typeTag = (PHPDocTypeTag) tag;
                    others.append(composeTypesAndDescription(getType(typeTag), typeTag.getDocumentation()));
                } else if (kind.equals(PHPDocTag.Type.DEPRECATED)) {
                    String oline = String.format("<tr><th align=\"left\">%s</th><td>%s</td></tr>%n", //NOI18N
                            processPhpDoc(tag.getKind().getName()), processPhpDoc(tag.getDocumentation(), "")); //NOI18N
                    others.append(oline);
                } else if (kind instanceof LinkParsedLine) {
                    links.add(kind.getDescription());
                } else {
                    String tagDescription = kind instanceof PHPDocTag.Type
                            ? tag.getValue() // kind description is empty
                            : kind.getDescription();
                    String oline = String.format("<tr><th align=\"left\">%s</th><td>%s</td></tr>%n", //NOI18N
                            processPhpDoc(tag.getKind().getName()), processPhpDoc(tagDescription, "")); //NOI18N
                    others.append(oline);
                }
            }

            // field without phpdoc or with but without @var tag
            if ((phpDocBlock == null || !tagInTagsList(tags, PHPDocTag.Type.VAR))
                    && indexedElement instanceof FieldElement) {
                FieldElement fieldElement = (FieldElement) indexedElement;
                String declaredType = putTypeSeparatorBetweenWhitespaces(fieldElement.getDeclaredType());
                others.append(composeTypesAndDescription(declaredType, "")); // NOI18N
            }

            phpDoc.append(composeFunctionDoc(processDescription(
                    processPhpDoc(description, "")), //NOI18N
                    composeParamTags(params, inheritedComments),
                    composeReturnTags(returns, inheritedComments),
                    others.toString()));
        }

        private String getType(PHPDocTypeTag typeTag) {
            // e.g. @var (A&B)|C description
            String[] split = CodeUtils.WHITE_SPACES_PATTERN.split(typeTag.getValue().trim(), 2);
            return putTypeSeparatorBetweenWhitespaces(split[0]);
        }

        private boolean tagInTagsList(List<PHPDocTag> tags, AnnotationParsedLine tagKind) {
            boolean hasVarTag = false;

            for (PHPDocTag tag : tags) {
                AnnotationParsedLine kind = tag.getKind();
                if (kind.equals(tagKind)) {
                    hasVarTag = true;
                    break;
                }
            }

            return hasVarTag;
        }

        protected String processDescription(String text) {
            StringBuilder result = new StringBuilder();
            int lastIndex = 0;
            int index = text.indexOf('{', 0);
            while (index > -1 && text.length() > (index + 1)) {
                result.append(text.substring(lastIndex, index));
                lastIndex = index;
                char charAt = text.charAt(index + 2);
                if (charAt == 'l' || charAt == 's' || charAt == 'u') {
                    int endIndex = text.indexOf(' ', index);
                    if (endIndex > -1) {
                        String tag = text.substring(index + 1, endIndex).trim();
                        if (LINK_TAGS.contains(tag)) {
                            index = endIndex + 1;
                            endIndex = text.indexOf('}', index);
                            if (endIndex > -1) {
                                String link = text.substring(index, endIndex).trim();
                                result.append(String.format("<a href=\"%s\">%s</a>", link, link));
                                lastIndex = endIndex + 1;
                            }
                        }
                    }
                }

                index = text.indexOf('{', index + 1);
            }
            if (lastIndex > -1) {
                result.append(text.substring(lastIndex));
            }
            return result.toString();
        }

        @NbBundle.Messages({
            "Parameters=Parameters:",
            "ReturnValue=Returns:"
        })
        private String composeFunctionDoc(String description, String parameters, String returnValue, String others) {
            StringBuilder value = new StringBuilder();

            value.append(description);
            value.append("<br />\n"); //NOI18N

            if (parameters.length() > 0) {
                value.append("<h3>"); //NOI18N
                value.append(Bundle.Parameters());
                value.append("</h3>\n<table cellspacing=0 " + TABLE_STYLE + ">\n").append(parameters).append("</table>\n"); //NOI18N
            }

            if (returnValue.length() > 0) {
                value.append("<h3>"); //NOI18N
                value.append(Bundle.ReturnValue());
                value.append("</h3>\n<table>\n"); //NOI18N
                value.append(returnValue);
                value.append("</table>");
            }

            if (others != null && others.length() > 0) {
                value.append("<table>\n").append(others).append("</table>\n"); //NOI18N
            }
            return value.toString();
        }

        private String composeParameterLine(PHPDocVarTypeTag param) {
            return composeParameterLine(param, param.getDocumentation());
        }

        private String composeParameterLine(PHPDocVarTypeTag param, String documentation) {
            return composeParameterLine(composeParamType(param), param.getVariable().getValue(), documentation);
        }

        private String composeParameterLine(@NullAllowed String type, String variableValue, String documentation) {
            String typeString = type == null ? "" : putTypeSeparatorBetweenWhitespaces(type); // NOI18N
            String pline = String.format("<tr><td>&nbsp;</td><td valign=\"top\" %s><nobr>%s</nobr></td><td valign=\"top\" %s><nobr><b>%s</b></nobr></td><td valign=\"top\" %s>%s</td></tr>%n", //NOI18N
                    TD_STYLE,
                    typeString,
                    TD_STYLE,
                    variableValue,
                    TD_STYLE_MAX_WIDTH,
                    documentation == null ? "&nbsp" : processPhpDoc(documentation)); // NOI18N
            return pline;
        }

        @NbBundle.Messages({
            "Type=Type",
            "Description=Description"
        })
        private String composeTypesAndDescription(String type, String description) {
            StringBuilder returnValue = new StringBuilder();
            if (StringUtils.hasText(type)) {
                returnValue.append(String.format("<tr><th align=\"left\">%s:</th><td>%s</td></tr>", //NOI18N
                        Bundle.Type(), type));
            }

            if (description != null && description.length() > 0) {
                returnValue.append(String.format("<tr><th align=\"left\" valign=\"top\">%s:</th><td>%s</td></tr>", //NOI18N
                        Bundle.Description(), processPhpDoc(description)));
            }
            return returnValue.toString();
        }

        /**
         * Create types separated with "|" or "&".
         *
         * @param types types
         * @return types separated with "|" or "&"
         */
        private String composeType(Collection<TypeResolver> types, Type.Kind typeKind) {
            StringBuilder sb = new StringBuilder();
            for (TypeResolver type : types) {
                if (type.isResolved()) {
                    QualifiedName typeName = type.getTypeName(true);
                    if (typeName != null) {
                        if (sb.length() > 0) {
                                if (typeKind == Type.Kind.INTERSECTION) {
                                    sb.append(" ").append(typeKind.getSign()).append(" "); // NOI18N
                                } else {
                                    //GH-5355: If a function returns multiple types
                                    //and doesn't have a declared return type,
                                    //it's always a union type.
                                    sb.append(" ").append(Type.Kind.UNION.getSign()).append(" "); // NOI18N
                                }
                        }
                        if (type.isNullableType()) {
                            sb.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                        }
                        String tName = typeName.toString();
                        if (tName.startsWith("\\")) { // NOI18N
                            if (VariousUtils.isSpecialClassName(tName.substring(1))) {
                                tName = tName.substring(1);
                            }
                        }
                        sb.append(tName);
                    }
                }
            }
            return sb.toString();
        }

        private String composeParamType(PHPDocVarTypeTag param) {
            // (X&Y)|Z $param
            String[] split = CodeUtils.WHITE_SPACES_PATTERN.split(param.getValue().trim(), 2);
            if (!split[0].startsWith("$")) { // NOI18N
                return split[0];
            }
            return ""; // NOI18N
        }

        // because of unit tests
        static String processPhpDoc(String phpDoc) {
            return processPhpDoc(phpDoc, Bundle.PHPDocNotFound());
        }

        static String processPhpDoc(String phpDoc, String defaultText) {
            String result = defaultText;
            if (StringUtils.hasText(phpDoc)) {
                String notags = KEEP_TAGS_PATTERN.matcher(phpDoc).replaceAll("&lt;"); // NOI18N
                notags = REPLACE_NEWLINE_PATTERN.matcher(notags).replaceAll("<br><br>"); // NOI18N
                result = LIST_PATTERN.matcher(notags).replaceAll("<br>&nbsp;&nbsp;&nbsp;&nbsp;"); // NOI18N
            }
            return result;
        }

        private String composeDescription(String description, List<PHPDocBlock> comments) {
            String ret = description;
            if (ret != null) {
                for (PHPDocBlock comment : comments) {
                    ret = replaceInheritdocForDescription(ret, comment.getDescription());
                    if (!hasInlineInheritdoc(ret)) {
                        break;
                    }
                }
            }
            return ret;
        }

        @CheckForNull
        private String replaceInheritdocForDescription(@NullAllowed String description, @NullAllowed String parentDescription) {
            if (description == null) {
                return parentDescription;
            }
            if (hasInlineInheritdoc(description)) {
                if (parentDescription != null && !parentDescription.trim().isEmpty()) {
                    if (INLINE_INHERITDOC_PATTERN.matcher(description.trim()).matches()) {
                        return parentDescription;
                    }
                    String inheritdoc = parentDescription;
                    if (INHERITDOC_FOR_PHPDOCUMENTOR || UNIT_TEST_INHERITDOC_FOR_PHPDOCUMENTER) {
                        inheritdoc = removeDescriptionHeader(parentDescription);
                    }
                    return replaceInlineInheritdoc(description, inheritdoc);
                }
            }
            return description;
        }

        private String composeParamTags(List<PHPDocVarTypeTag> paramTags, List<PHPDocBlock> inheritedComments) {
            StringBuilder params = new StringBuilder();
            // add also missing params
            if (indexedElement instanceof BaseFunctionElement) {
                BaseFunctionElement functionElement = (BaseFunctionElement) indexedElement;
                List<ParameterElement> parameters = functionElement.getParameters();
                for (ParameterElement parameter : parameters) {
                    PHPDocVarTypeTag param = null;
                    String name = parameter.getName();
                    for (PHPDocVarTypeTag paramTag : paramTags) {
                        String value = paramTag.getVariable().getValue();
                        if (name.equals(value)) {
                            param = paramTag;
                        }
                    }
                    // use fallback params
                    if (param == null
                            && indexedElement instanceof MethodElement) {
                        for (PHPDocBlock inheritedComment : inheritedComments) {
                            List<PHPDocTag> tags = inheritedComment.getTags();
                            for (PHPDocTag tag : tags) {
                                AnnotationParsedLine kind = tag.getKind();
                                if (kind.equals(PHPDocTag.Type.PARAM)) {
                                    PHPDocVarTypeTag t = (PHPDocVarTypeTag) tag;
                                    String value = t.getVariable().getValue();
                                    if (name.equals(value)) {
                                        param = t;
                                        break;
                                    }
                                }
                            }
                            if (param != null) {
                                break;
                            }
                        }
                    }
                    // append line
                    if (param != null) {
                        String paramDescription = composeParamTagDescription(param, inheritedComments);
                        String paramLine = composeParameterLine(param, paramDescription);
                        params.append(paramLine);
                    } else {
                        // use actual parameter types
                        String paramLine = composeParameterLine(parameter.getDeclaredType(), name, ""); // NOI18N
                        params.append(paramLine);
                    }
                }
            } else {
                for (PHPDocVarTypeTag paramTag : paramTags) {
                    String paramLine = composeParameterLine(paramTag);
                    params.append(paramLine);
                }
            }
            return params.toString();
        }

        private String composeParamTagDescription(PHPDocVarTypeTag tag, List<PHPDocBlock> phpDocBlocks) {
            String documentation = tag.getDocumentation();
            for (PHPDocBlock docBlock : phpDocBlocks) {
                documentation = composeParamTagDescription(documentation, tag, docBlock);
            }
            return documentation;
        }

        private String composeParamTagDescription(String documentation, PHPDocVarTypeTag varTypeTag, PHPDocBlock phpDocBlock) {
            String ret = documentation;
            if (ret != null && hasInlineInheritdoc(ret)) {
                List<PHPDocTag> tags = phpDocBlock.getTags();
                for (PHPDocTag tag : tags) {
                    AnnotationParsedLine kind = tag.getKind();
                    if (kind.equals(PHPDocTag.Type.PARAM)) {
                        PHPDocVarTypeTag inheritedTag = (PHPDocVarTypeTag) tag;
                        PHPDocNode variable = varTypeTag.getVariable();
                        PHPDocNode inheritedVariable = inheritedTag.getVariable();
                        if (variable != null && inheritedVariable != null) {
                            if (variable.getValue().equals(inheritedVariable.getValue())) {
                                String inheritedDocumentation = inheritedTag.getDocumentation();
                                if (inheritedDocumentation != null && !inheritedDocumentation.trim().isEmpty()) {
                                    return replaceInlineInheritdoc(ret, inheritedTag.getDocumentation());
                                }
                            }
                        }
                    }
                }
            }
            return ret;
        }

        private String composeReturnTags(List<PHPDocTypeTag> returnTags, List<PHPDocBlock> inheritedComments) {
            StringBuilder returnValue = new StringBuilder();
            // if a return tag is missing, use fallback(parent) one
            List<PHPDocTypeTag> fallbacks = new ArrayList<>(returnTags);
            if (fallbacks.isEmpty()) {
                for (PHPDocBlock inheritedComment : inheritedComments) {
                    List<PHPDocTag> tags = inheritedComment.getTags();
                    for (PHPDocTag tag : tags) {
                        if (tag.getKind().equals(PHPDocTag.Type.RETURN)) {
                            fallbacks.add((PHPDocTypeTag) tag);
                        }
                    }
                    if (!fallbacks.isEmpty()) {
                        break;
                    }
                }
            }
            for (PHPDocTypeTag fallback : fallbacks) {
                returnValue.append(composeTypesAndDescription(getType(fallback), fallback.getDocumentation()));
            }

            if (fallbacks.isEmpty()) {
                // no phpdoc
                if (indexedElement instanceof BaseFunctionElement) {
                    BaseFunctionElement functionElement = (BaseFunctionElement) indexedElement;
                    // currently, fully qualified type names are shown
                    String returnType = putTypeSeparatorBetweenWhitespaces(functionElement.getDeclaredReturnType());
                    if (StringUtils.hasText(returnType)) {
                        returnValue.append(composeTypesAndDescription(returnType, "")); // NOI18N
                    } else {
                        // GH-5355
                        returnValue.append(composeTypesAndDescription(composeType(functionElement.getReturnTypes(), getTypeKind(functionElement)), "")); // NOI18N
                    }
                }
            }
            return returnValue.toString();
        }

        private Type.Kind getTypeKind(BaseFunctionElement element) {
            Type.Kind kind = Type.Kind.NORMAL;
            if (element.isReturnIntersectionType()) {
                kind = Type.Kind.INTERSECTION;
            } else if (element.isReturnUnionType()) {
                kind = Type.Kind.UNION;
            }
            return kind;
        }

        private List<PhpElement> getInheritedElements() {
            if (!needInheritedElements()) {
                return Collections.emptyList();
            }

            List<PhpElement> inheritedElements = new ArrayList<>();
            if (indexedElement instanceof MethodElement) {
                MethodElement methodElement = (MethodElement) indexedElement;
                inheritedElements.addAll(getAllOverriddenMethods(methodElement));
            } else if (indexedElement instanceof ClassElement) {
                ClassElement classElement = (ClassElement) indexedElement;
                inheritedElements.addAll(getAllInheritedClasses(classElement));
            } else if (indexedElement instanceof InterfaceElement) {
                InterfaceElement interfaceElement = (InterfaceElement) indexedElement;
                inheritedElements.addAll(getAllInheritedInterfaces(interfaceElement));
            } else if (indexedElement instanceof FieldElement) {
                FieldElement fieldElement = (FieldElement) indexedElement;
                inheritedElements.addAll(getAllOverriddenFields(fieldElement));
            } else if (indexedElement instanceof TypeConstantElement) {
                TypeConstantElement constElement = (TypeConstantElement) indexedElement;
                inheritedElements.addAll(getAllOverriddenConstants(constElement));
            }
            return inheritedElements;
        }

        private boolean needInheritedElements() {
            if (phpDocBlock == null) {
                return true;
            }
            String description = phpDocBlock.getDescription();
            if (hasInlineInheritdoc(description)) {
                return true;
            }

            if (indexedElement instanceof MethodElement) {
                List<PHPDocTag> tags = phpDocBlock.getTags();
                Set<String> params = new HashSet<>(tags.size());
                for (PHPDocTag tag : tags) {
                    AnnotationParsedLine kind = tag.getKind();
                    if (kind.equals(PHPDocTag.Type.PARAM)) {
                        PHPDocVarTypeTag t = (PHPDocVarTypeTag) tag;
                        params.add(t.getVariable().getValue());
                        if (hasInlineInheritdoc(tag.getDocumentation())) {
                            return true;
                        }
                    }
                }
                MethodElement method = (MethodElement) indexedElement;
                for (ParameterElement parameter : method.getParameters()) {
                    if (!params.contains(parameter.getName())) {
                        return true;
                    }
                }
            }
            return false;
        }

        private List<PHPDocBlock> getInheritedComments(List<PhpElement> elements) {
            List<PHPDocBlock> inheritedComments = new ArrayList<>();
            for (PhpElement element : elements) {
                PHPDocBlock docBlock = getPhpDocBlock(element);
                if (docBlock != null) {
                    if (phpDocBlock == null || isOnlyInheritdoc(phpDocBlock)) {
                        phpDocBlock = docBlock;
                    } else {
                        inheritedComments.add(docBlock);
                    }
                }
            }
            return inheritedComments;
        }

        private boolean isOnlyInheritdoc(PHPDocBlock phpDocBlock) {
            return INLINE_INHERITDOC_PATTERN.matcher(phpDocBlock.getDescription().trim()).matches()
                    && phpDocBlock.getTags().isEmpty();
        }

        @CheckForNull
        static String replaceInlineInheritdoc(@NullAllowed String description, @NullAllowed String inheritdoc) {
             if (description == null && inheritdoc != null) {
                return inheritdoc;
            }
            if (description == null || inheritdoc == null) {
                return description;
            }
            if (inheritdoc.trim().isEmpty()) {
                return description;
            }
            // #270415 escape "$" and "\"
            return INLINE_INHERITDOC_PATTERN.matcher(description).replaceAll(Matcher.quoteReplacement(inheritdoc));
        }

        static boolean hasInlineInheritdoc(String description) {
            return description == null ? false : INLINE_INHERITDOC_PATTERN.matcher(description).find();
        }

        static String removeDescriptionHeader(String description) {
            return description == null ? null : DESC_HEADER_PATTERN.matcher(description).replaceFirst(""); // NOI18N
        }

        private static Index getIndex(PhpElement phpElement) {
            final ElementQuery elementQuery = phpElement.getElementQuery();
            return elementQuery.getQueryScope().isIndexScope()
                    ? (Index) elementQuery
                    : ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(phpElement.getFileObject()));
        }

        /**
         * Get all inherited classes recursively.
         *
         * @param typeElement the TypeElement
         * @return All inherited types
         */
        private static List<TypeElement> getAllInheritedClasses(TypeElement typeElement) {
            List<TypeElement> types = new ArrayList<>();
            getInheritedClasses(typeElement, types);
            return types;
        }

        private static void getInheritedClasses(TypeElement typeElement, List<TypeElement> types) {
            Set<ClassElement> inheritedClasses = getInheritedClasses(typeElement);
            types.addAll(inheritedClasses);
            for (ClassElement inheritedClasse : inheritedClasses) {
                getInheritedClasses(inheritedClasse, types);
            }
        }

        private static Set<ClassElement> getInheritedClasses(TypeElement typeElement) {
            Index index = getIndex(typeElement);
            return index.getDirectInheritedClasses(typeElement);
        }

        /**
         * Get all inherited interfaces recursively.
         *
         * @param typeElement the TypeElement
         * @return All inherited types
         */
        private static List<TypeElement> getAllInheritedInterfaces(TypeElement typeElement) {
            List<TypeElement> types = new ArrayList<>();
            getInheritedInterfaces(typeElement, types);
            return types;
        }

        private static void getInheritedInterfaces(TypeElement typeElement, List<TypeElement> types) {
            Set<InterfaceElement> inheritedInterfaces = getInheritedInterfaces(typeElement);
            types.addAll(inheritedInterfaces);
            for (InterfaceElement inheritedInterface : inheritedInterfaces) {
                getInheritedClasses(inheritedInterface, types);
            }
        }

        private static Set<InterfaceElement> getInheritedInterfaces(TypeElement typeElement) {
            Index index = getIndex(typeElement);
            return index.getDirectInheritedInterfaces(typeElement);
        }

        private static List<MethodElement> getAllOverriddenMethods(MethodElement method) {
            List<MethodElement> methods = new ArrayList<>();
            getOverriddenMethods(method, methods);
            return methods;
        }

        private static void getOverriddenMethods(MethodElement method, List<MethodElement> methods) {
            Set<MethodElement> overriddenMethods = getOverriddenMethods(method);
            methods.addAll(overriddenMethods);
            for (MethodElement overriddenMethod : overriddenMethods) {
                getOverriddenMethods(overriddenMethod, methods);
            }
        }

        private static Set<MethodElement> getOverriddenMethods(MethodElement method) {
            ElementFilter methodNameFilter = ElementFilter.forName(NameKind.exact(method.getName()));
            return methodNameFilter.filter(getInheritedMethods(method));
        }

        private static Set<MethodElement> getInheritedMethods(MethodElement method) {
            Index index = getIndex(method);
            TypeElement type = method.getType();
            if (type == null) {
                return Collections.emptySet();
            }
            return index.getInheritedMethods(type);
        }

        private static List<FieldElement> getAllOverriddenFields(FieldElement field) {
            List<FieldElement> fields = new ArrayList<>();
            getOverriddenFields(field, fields);
            return fields;
        }

        private static void getOverriddenFields(FieldElement field, List<FieldElement> fields) {
            Set<FieldElement> overriddenFields = getOverriddenFields(field);
            fields.addAll(overriddenFields);
            for (FieldElement overriddenField : overriddenFields) {
                getOverriddenFields(overriddenField, fields);
            }
        }

        private static Set<FieldElement> getOverriddenFields(FieldElement field) {
            ElementFilter fieldNameFilter = ElementFilter.forName(NameKind.exact(field.getName()));
            return fieldNameFilter.filter(getInheritedFields(field));
        }

        private static Set<FieldElement> getInheritedFields(FieldElement field) {
            Index index = getIndex(field);
            TypeElement type = field.getType();
            if (type == null) {
                return Collections.emptySet();
            }
            return index.getInheritedFields(type);
        }

        private static List<TypeConstantElement> getAllOverriddenConstants(TypeConstantElement constant) {
            List<TypeConstantElement> constants = new ArrayList<>();
            getOverriddenConstants(constant, constants);
            return constants;
        }

        private static void getOverriddenConstants(TypeConstantElement constant, List<TypeConstantElement> constants) {
            if (constant.isMagic()) {
                // e.g. A::class
                // prevent NPE in getIndex()
                return;
            }
            Set<TypeConstantElement> overriddenConstants = getOverriddenConstants(constant);
            constants.addAll(overriddenConstants);
            for (TypeConstantElement overriddenConstant : overriddenConstants) {
                getOverriddenConstants(overriddenConstant, constants);
            }
        }

        private static Set<TypeConstantElement> getOverriddenConstants(TypeConstantElement constant) {
            ElementFilter constantNameFilter = ElementFilter.forName(NameKind.exact(constant.getName()));
            return constantNameFilter.filter(getInheritedConstants(constant));
        }

        private static Set<TypeConstantElement> getInheritedConstants(TypeConstantElement constant) {
            Index index = getIndex(constant);
            TypeElement type = constant.getType();
            if (type == null) {
                return Collections.emptySet();
            }
            return index.getInheritedTypeConstants(type);
        }

        @CheckForNull
        private PHPDocBlock getPhpDocBlock(final PhpElement phpElement) {
            if (!canBeProcessed(phpElement)) {
                return null;
            }
            FileObject fileObject = phpElement.getFileObject();
            BaseDocument document = GsfUtilities.getDocument(fileObject, true);
            if (document != null) {
                document.readLock();
                try {
                    int offset = phpElement.getOffset();
                    TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(document, offset);
                    if (ts != null) {
                        ts.move(offset);
                        if (ts.movePrevious()) {
                            List<PHPTokenId> lookfor = Arrays.asList(
                                    PHPTokenId.PHPDOC_COMMENT,
                                    PHPTokenId.PHP_CURLY_OPEN,
                                    PHPTokenId.PHP_CURLY_CLOSE,
                                    PHPTokenId.PHP_SEMICOLON
                            );
                            Token<? extends PHPTokenId> token = LexUtilities.findPreviousToken(ts, lookfor);
                            if (token != null && token.id() == PHPTokenId.PHPDOC_COMMENT) {
                                PHPDocCommentParser phpDocCommentParser = new PHPDocCommentParser();
                                return phpDocCommentParser.parse(
                                        ts.offset() - 3, // - /**
                                        ts.offset() + token.length(),
                                        token.text().toString()
                                );
                            }
                        }
                    }
                } finally {
                    document.readUnlock();
                }
            }
            return null;
        }

        private static String putTypeSeparatorBetweenWhitespaces(@NullAllowed String type) {
            if (type == null) {
                return null;
            }
            return type.replace("&", " & ").replace("|", " | "); // NOI18N
        }
    }

    private static final class ASTNodeFinder extends UserTask {

        private final PhpElement indexedElement;
        private ASTNode node;
        private PHPDocBlock phpDocBlock;

        public ASTNodeFinder(PhpElement indexedElement) {
            this.indexedElement = indexedElement;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            ParserResult presult = (ParserResult) resultIterator.getParserResult();
            if (presult != null) {
                Program program = Utils.getRoot(presult);
                if (program != null) {
                    node = Utils.getNodeAtOffset(program, indexedElement.getOffset());
                    if (node == null) { // issue #118222
                        LOGGER.log(
                                Level.WARNING,
                                "Could not find AST node for element {0} defined in {1}",
                                new Object[]{indexedElement.getName(), indexedElement.getFilenameUrl()});
                        return;
                    }
                    if (!(node instanceof PHPDocTag)) {
                        Comment comment = Utils.getCommentForNode(program, node);
                        if (comment instanceof PHPDocBlock) {
                            phpDocBlock = (PHPDocBlock) comment;
                        }
                    }
                }
            }
        }

        @CheckForNull
        public ASTNode getNode() {
            return node;
        }

        @CheckForNull
        public PHPDocBlock getPhpDocBlock() {
            return phpDocBlock;
        }
    }

    private interface PhpDocumentation {
        PhpDocumentation NONE = new PhpDocumentation() {

            @Override
            public Documentation createDocumentation(CCDocHtmlFormatter locationHeader) {
                return Documentation.create(String.format("%s%s", locationHeader.getText(), Bundle.PHPDocNotFound())); //NOI18N
            }
        };

        Documentation createDocumentation(CCDocHtmlFormatter locationHeader);

        static final class Factory {

            static PhpDocumentation create(CCDocHtmlFormatter header, StringBuilder body, List<String> links) {
                URL url = null;
                if (links.size() > 0) {
                    try {
                        url = new URL(links.get(0));
                    } catch (MalformedURLException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    }
                    if (links.size() > 1) {
                        attachLinks(body, links);
                    }
                }
                String description = String.format("%s%s", header.getText(), body.length() == 0 ? Bundle.PHPDocNotFound() : body); //NOI18N
                return new PhpDocumentationImpl(description, url);
            }

            @NbBundle.Messages("OnlineDocs=Online Documentation")
            private static void attachLinks(StringBuilder body, List<String> links) {
                assert links.size() > 1 : links.size();
                body.append("<h3>"); //NOI18N
                body.append(Bundle.OnlineDocs());
                body.append("</h3>\n"); //NOI18N
                for (String link : links) {
                    String line = String.format("<a href=\"%s\">%s</a><br>%n", link, link); //NOI18N
                    body.append(line);
                }
            }

        }

        static final class PhpDocumentationImpl implements PhpDocumentation {
            private final String description;
            private final URL url;

            private PhpDocumentationImpl(String description, URL url) {
                this.description = description;
                this.url = url;
            }

            @Override
            public Documentation createDocumentation(CCDocHtmlFormatter locationHeader) {
                assert  locationHeader != null;
                return Documentation.create(String.format("%s%s", locationHeader.getText(), description), url); //NOI18N
            }

        }
    }

}
