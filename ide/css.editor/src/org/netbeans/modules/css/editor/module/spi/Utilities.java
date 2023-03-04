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
package org.netbeans.modules.css.editor.module.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.csl.CssElement;
import org.netbeans.modules.css.editor.csl.CssPropertyElement;
import org.netbeans.modules.css.editor.module.PropertiesReader;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.api.properties.GrammarElement;
import org.netbeans.modules.css.lib.api.properties.PropertyCategory;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 * @author mfukala@netbeans.org
 */
public class Utilities {

    private Utilities() {
    }
    
    /**
     * Name of the meta property defining property category. 
     * See description.txt in the o.n.m.css.editor.module.main.properties
     */
    public static final String CATEGORY_META_PROPERTY_NAME = "$category"; //NOI18N
    
    /**
     * Creates a generic mark occurrences node visitor for given node types. 
     * Only elements of the same type (from the given types list) and the same image are marked.
     */
    public static <T extends Set<OffsetRange>> NodeVisitor<T> createMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result, NodeType... nodeTypesToMatch) {

        final Snapshot snapshot = context.getSnapshot();

        int astCaretOffset = snapshot.getEmbeddedOffset(context.getCaretOffset());
        if (astCaretOffset == -1) {
            return null;
        }


        final Node current = NodeUtil.findNonTokenNodeAtOffset(context.getParseTreeRoot(), astCaretOffset);
        if (current == null) {
            //this may happen if the offset falls to the area outside the selectors rule node.
            //(for example when the stylesheet starts or ends with whitespaces or comment and
            //and the offset falls there).
            //In such case root node (with null parent) is returned from NodeUtil.findNodeAtOffset() 
            return null;
        }

        Set types = EnumSet.copyOf(Arrays.asList(nodeTypesToMatch));
        if(!types.contains(current.type())) {
            return null;
        }
        
        final CharSequence selectedNamespacePrefixImage = current.image();

        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                if (node.type() == current.type()
                        && CharSequenceUtilities.textEquals(selectedNamespacePrefixImage, node.image())) {
                    OffsetRange documentNodeRange = Css3Utils.getDocumentOffsetRange(node, snapshot);
                    getResult().add(Css3Utils.getValidOrNONEOffsetRange(documentNodeRange));
                }
                return false;
            }
        };
    }

      public static  List<CompletionProposal> createRAWCompletionProposals(Collection<String> props, ElementKind kind, int anchor) {
          return createRAWCompletionProposals(props, kind, anchor, null);
      }
      
      public static  List<CompletionProposal> createRAWCompletionProposals(Collection<String> props, ElementKind kind, int anchor, String addPrefix) {
        List<CompletionProposal> proposals = new ArrayList<>(props.size());
        for (String value : props) {
            if(addPrefix != null) {
                value = addPrefix + value;
            }
            CssElement handle = new CssElement(value);
            CompletionProposal proposal = CssCompletionItem.createRAWCompletionItem(handle, value, kind, anchor, false);
            proposals.add(proposal);
        }
        return proposals;
    }
 
    public static List<CompletionProposal> wrapProperties(Collection<PropertyDefinition> props, int anchor) {
        return wrapProperties(props, anchor, 0);
    }
    public static List<CompletionProposal> wrapProperties(Collection<PropertyDefinition> props, int anchor, int stripLen) {
        Set<String> names = new HashSet<>();
        List<CompletionProposal> proposals = new ArrayList<>(props.size());
        for (PropertyDefinition p : props) {
            String propName = p.getName();
            //filter out non-public properties
            if (!GrammarElement.isArtificialElementName(propName)) {
                if(names.add(propName)) { //do not list same name more times
                    CssElement handle = new CssPropertyElement(p);
                    String insertPrefix = stripLen == 0
                            ? propName
                            : propName.substring(stripLen);
                    CompletionProposal proposal = CssCompletionItem.createPropertyCompletionItem(handle, p, insertPrefix, anchor, false);
                    proposals.add(proposal);
                }
            } 
        }
        return proposals;
    } 
    
    /**
     * Utility method which creates a map of property name to PropertyDefinition for 
     * a properties file defining the css properties
     * 
     * the syntax of the file:
     * 
     * propertyName=valueGrammar
     * 
     * Example:
     * 
     * outline-style=auto | <border-style>
     * 
     * @param sourcePath - an absolute path to the resource properties file relative to the module base
     */
    public static Map<String, PropertyDefinition> parsePropertyDefinitionFile(String sourcePath, CssModule module) {
        Map<String, PropertyDefinition> properties = new HashMap<>();
        
        //why not use NbBundle.getBundle()? - we need the items in the natural source order
        Collection<Pair<String, String>> parseBundle = PropertiesReader.parseBundle(sourcePath);

        PropertyCategory category = PropertyCategory.DEFAULT;
        for(Pair<String, String> pair : parseBundle) {
            String name = pair.first();
            String value = pair.second();
            
            if(name.startsWith("$")) {
                //property category
                if(CATEGORY_META_PROPERTY_NAME.equalsIgnoreCase(name)) {
                    try {
                        category = PropertyCategory.valueOf(value.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        Logger.getAnonymousLogger().log(Level.INFO, 
                                String.format("Unknown property category name %s in %s properties definition file. Served by %s css module.", value, sourcePath, module.getSpecificationURL()),
                                e);
                    }
                } else {
                    //unknown meta property
                    Logger.getAnonymousLogger().log(Level.INFO, null, 
                            new IllegalArgumentException(String.format("Unknown meta property %s in %s properties definition file. Served by %s css module.", name, sourcePath, module.getSpecificationURL())));
                }
                
            } else {
                //parse bundle key - there might be more properties separated by semicolons
                StringTokenizer nameTokenizer = new StringTokenizer(name, ";"); //NOI18N

                while (nameTokenizer.hasMoreTokens()) {
                    String parsed_name = nameTokenizer.nextToken().trim();
                    PropertyDefinition prop = new PropertyDefinition(parsed_name, value, category, module);
                    properties.put(parsed_name, prop);
                }
            }

        }

        return properties;
    }

    /**
     * Resolves whether the given property value contains a vendor specific code.
     * 
     * Each property value can be divided into parts/tokens typically by whitespaces.
     * Any of these tokens may contain a vendor specific code.
     * 
     * This method resolves whether the given property value token contains a vendor
     * specific code. It tests if the property token starts with any of the registered
     * browser specific prefixes.
     * 
     * Example: background-image: -webkit-linear-gradient(top, #0088cc, #0055cc);
     * 
     * The method will return true for the "-webkit-linear-gradient(top, #0088cc, #0055cc)" token.
     * 
     * @param file context file
     * @param value the value of the resolved property
     * @since 1.38
     */
    public static boolean isVendorSpecificPropertyValueToken(FileObject file, CharSequence value) {
        return Css3Utils.isVendorSpecificPropertyValue(file, value);
    }
    
    /**
     * @since 1.46
     * @param proposals
     * @param prefix
     * @param ignoreCase
     * @return 
     */
    public static List<CompletionProposal> filterCompletionProposals(List<CompletionProposal> proposals, CharSequence prefix, boolean ignoreCase) {
        List<CompletionProposal> filtered = new ArrayList<>();
        for(CompletionProposal proposal : proposals) {
            if(LexerUtils.startsWith(proposal.getInsertPrefix(), prefix, ignoreCase, false)) {
                filtered.add(proposal);
            }
        }
        return filtered;
    }
}
