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
package org.netbeans.modules.css.editor.csl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.CssDeclarationContext;
import org.netbeans.modules.css.editor.ParsingErrorsFilter;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.ErrorsProvider;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.lib.api.properties.Token;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NotImplementedException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides extended errors from semantic css analysis.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = ErrorsProvider.class)
public class CssAnalyser implements ErrorsProvider {

    private static final String UNKNOWN_PROPERTY_BUNDLE_KEY = "unknown_property";//NOI18N
    private static final String UNKNOWN_PROPERTY_ERROR_KEY_DELIMITER = "/";//NOI18N
    private static final String UNKNOWN_PROPERTY_ERROR_KEY = "unknown_property" + UNKNOWN_PROPERTY_ERROR_KEY_DELIMITER;//NOI18N
    private static final String INVALID_PROPERTY_VALUE = "invalid_property_value";//NOI18N

    @Override
    public List<? extends FilterableError> getExtendedDiagnostics(CssParserResult parserResult) {
        final Node node = parserResult.getParseTree();
        final Snapshot snapshot = parserResult.getSnapshot();
        final FileObject file = snapshot.getSource().getFileObject();

        List<FilterableError> errors = new ArrayList<>();
        NodeVisitor<List<FilterableError>> visitor = new NodeVisitor<List<FilterableError>>(errors) {

            @Override
            public boolean visit(Node node) {
                if (node.type() == NodeType.propertyDeclaration) {
                    //do not check declarations in fontFace and counterStyle

                    //structure: declarations/declaration/propertyDeclaration
                    Node parent = node.parent().parent();
                    if (parent.type() == NodeType.declarations) {
                        switch (parent.parent().type()) {
                            case fontFace:
                            case counterStyle:
                                return false;
                        }
                    }
                    SemanticAnalyzerResult analyzeDeclaration = CssModuleSupport.analyzeDeclaration(node);
                    switch (analyzeDeclaration.getType()) {
                        case ERRONEOUS:
                            throw new NotImplementedException(); //TODO
                        case VALID:
                            return false;
                        case UNKNOWN:
                            break;
                    }

                    //check if the declaration contains a generated code, if so do not do any checks
                    if (Css3Utils.containsGeneratedCode(node.image())) {
                        return false;
                    }

                    CssDeclarationContext ctx = new CssDeclarationContext(node);

                    Node propertyNode = ctx.getProperty();
                    Node valueNode = ctx.getPropertyValue();

                    if (propertyNode != null) {
                        String propertyName = ctx.getPropertyNameImage();

                        //check non css 2.1 compatible properties and ignore them
                        //values are not checked as well
                        if (isNonCss21CompatibleDeclarationPropertyName(propertyName)) {
                            return false;
                        }

                        //check for vendor specific properies - ignore them
                        PropertyDefinition property = Properties.getPropertyDefinition(propertyName);
                        if (!Css3Utils.containsGeneratedCode(propertyName) && !Css3Utils.isVendorSpecificProperty(propertyName) && property == null) {
                            //unknown property - report
                            String msg = NbBundle.getMessage(CssAnalyser.class, UNKNOWN_PROPERTY_BUNDLE_KEY, propertyName);
                            String key = UNKNOWN_PROPERTY_ERROR_KEY + propertyName;

                            FileObject file = snapshot.getSource().getFileObject();
                            FilterableError error = CssErrorFactory.createError(key,
                                    msg,
                                    msg,
                                    file,
                                    propertyNode.from(),
                                    propertyNode.to(),
                                    false /* not line error */,
                                    Severity.WARNING,
                                    ParsingErrorsFilter.getEnableFilterAction(file, key),
                                    ParsingErrorsFilter.getDisableFilterAction(file, key));

                            if (error != null) {
                                getResult().add(error);
                            }
                        }

                        //check value
                        if (valueNode != null && property != null) {
                            if (NodeUtil.containsError(valueNode)) {
                                return false; //no semantic checks if there's a parsing error
                            }
                            String valueImage = ctx.getPropertyValueImage();

                            //do not check values which contains generated code
                            //we are no able to identify the templating semantic
                            if (!Css3Utils.containsGeneratedCode(valueImage)
                                    //TODO add support for checking value of vendor specific properties, not it is disabled.
                                    && !Css3Utils.isVendorSpecificPropertyValue(file, valueImage)) {
                                ResolvedProperty pv = new ResolvedProperty(file, property, valueImage);
                                if (!pv.isResolved()) {
                                    if (!ctx.containsIEBS9Hack() && !ctx.containsIEStarHack()) {
                                        String errorMsg = null;

                                        //error in property 
                                        List<Token> unresolved = pv.getUnresolvedTokens();
                                        if (unresolved.isEmpty()) {
                                            return false;
                                        }
                                        Token unexpectedToken = unresolved.iterator().next();

                                        if (isNonCss21CompatiblePropertyValue(unexpectedToken.toString())) {
                                            return false;
                                        }

                                        CharSequence unexpectedTokenImg = unexpectedToken.image();
                                        if (Css3Utils.isVendorSpecificPropertyValue(file, unexpectedTokenImg)) {
                                            //the unexpected token is a vendor property value, ignore
                                            return false;
                                        }

                                        if (errorMsg == null) {
                                            errorMsg = NbBundle.getMessage(CssAnalyser.class, INVALID_PROPERTY_VALUE, unexpectedToken.image().toString());
                                        }

                                        FilterableError error = makeError(valueNode.from(),
                                                valueNode.to(),
                                                snapshot,
                                                INVALID_PROPERTY_VALUE,
                                                errorMsg,
                                                errorMsg,
                                                false /* not line error */,
                                                Severity.WARNING);
                                        if (error != null) {
                                            getResult().add(error);
                                        }
                                    }
                                }
                            }
                        }

                    }

                }

                return false;
            }

        };

        visitor.visitChildren(node);

        return errors;
    }

    private static FilterableError makeError(int astFrom, int astTo, Snapshot snapshot, String key, String displayName, String description, boolean lineError, Severity severity) {
        assert astFrom <= astTo;

        return CssErrorFactory.createError(key,
                displayName,
                description,
                snapshot.getSource().getFileObject(),
                astFrom,
                astTo,
                lineError,
                severity);

    }

    public static boolean isConfigurableError(String errorKey) {
        //unknown property errors can be suppressed, so far
        return isUnknownPropertyError(errorKey);
    }

    public static boolean isUnknownPropertyError(String errorKey) {
        return errorKey.startsWith(UNKNOWN_PROPERTY_ERROR_KEY);
    }

    public static String getUnknownPropertyName(String unknownPropertyErrorKey) {
        assert unknownPropertyErrorKey.startsWith(UNKNOWN_PROPERTY_ERROR_KEY);
        int index = unknownPropertyErrorKey.indexOf(UNKNOWN_PROPERTY_ERROR_KEY_DELIMITER);//NOI18N
        return unknownPropertyErrorKey.substring(index + 1);
    }

    //this is only a temporary hack for being able to filter out the css 2.1 errors for
    //commonly used properties not defined in the specification
    private static boolean isNonCss21CompatibleDeclarationPropertyName(String propertyName) {
        return NON_CSS21_DECLARATION_PROPERTY_NAMES.contains(propertyName);
    }

    private static boolean isNonCss21CompatiblePropertyValue(String propertyValue) {
        return NON_CSS21_DECLARATION_PROPERTY_VALUES.contains(propertyValue);
    }

    private static final Collection<String> NON_CSS21_DECLARATION_PROPERTY_NAMES = Arrays.asList(
            new String[]{"opacity", "resize", "text-overflow", "text-shadow", "filter"}); //NOI18N

    private static final Collection<String> NON_CSS21_DECLARATION_PROPERTY_VALUES = Arrays.asList(
            new String[]{"expression"}); //NOI18N

}
