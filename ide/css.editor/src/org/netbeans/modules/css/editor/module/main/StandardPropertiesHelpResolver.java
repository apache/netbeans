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
package org.netbeans.modules.css.editor.module.main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.css.editor.URLRetriever;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author mfukala@netbeans.org
 */
public class StandardPropertiesHelpResolver extends HelpResolver {

    //workaround for exceptions where the property name doesn't correspond 
    //to the anchor name in the help file
    //TODO possibly use some non class hardcoded mechanism - a property file
    private static final Map<String, String> propertyNamesTranslationTable = 
            new HashMap<>();
    static {
    }
    
    private static final Logger LOGGER = Logger.getLogger(HelpResolver.class.getName());
    private static final String SPEC_ARCHIVE_NAME = "docs/css3-spec.zip"; //NOI18N
    private static String SPEC_ARCHIVE_INTERNAL_URL;
    private static final String W3C_SPEC_URL_PREFIX = "http://www.w3.org/TR/"; //NOI18N
    private static final String MODULE_ARCHIVE_PATH = "www.w3.org/TR/"; //NOI18N
    private static final String INDEX_HTML_FILE_NAME = "index.html"; //NOI18N

    private static final String NO_HELP_MSG = NbBundle.getMessage(StandardPropertiesHelpResolver.class, "completion-help-no-documentation-found");

    @Override
    public String getHelp(FileObject context, PropertyDefinition property) {
        CssModule cssModule = property.getCssModule();
        if (cssModule == null) {
            return null;
        }
        String moduleDocBase = cssModule.getSpecificationURL();
        if (moduleDocBase == null) {
            return null;
        }
        
        if("http://www.w3.org/TR/CSS2".equals(moduleDocBase)) { //NOI18N
            //css2 help is treated by the legacy help resolver
            return null;
        }

        if (moduleDocBase.startsWith(W3C_SPEC_URL_PREFIX)) {
            String moduleFolderName = moduleDocBase.substring(W3C_SPEC_URL_PREFIX.length());
            StringBuilder propertyUrl = new StringBuilder();
            propertyUrl.append(getSpecURL());
            propertyUrl.append(MODULE_ARCHIVE_PATH);
            propertyUrl.append(moduleFolderName);
            propertyUrl.append('/');
            propertyUrl.append(INDEX_HTML_FILE_NAME);
            propertyUrl.append('#');
            propertyUrl.append(property.getName());
            try {
                URL propertyHelpURL = new URL(propertyUrl.toString());
                String urlContent = URLRetriever.getURLContentAndCache(propertyHelpURL);
                
                assert urlContent != null : "null " + propertyHelpURL;
                
                //1. find the anchor

                //there are some exceptions where the anchors are defined under different
                //ids than the property names
                String modifiedPropertyName = propertyNamesTranslationTable.get(property.getName());
                String propertyName = modifiedPropertyName != null ? modifiedPropertyName : property.getName();
                
                //following forms of anchors are supported:
                //<dfn id="property"> 
                //<dfn id="property0"> ... sometimes the property is referred with a number suffix
                
                String elementName = "dfn";
                
                String patternImg = String.format("(?s)<%s[^>]*id=(['\"])propdef-%s\\d?\\1[^>]*>", elementName, propertyName);
                
                Pattern pattern = Pattern.compile(patternImg); //DOTALL mode
                Matcher matcher = pattern.matcher(urlContent);
                if (!matcher.find(0)){
                    patternImg = String.format("(?s)<%s[^>]*id=['\"]?\\w*-??propdef-%s\\d?['\"]?[^>]*>", elementName, propertyName);
                    pattern = Pattern.compile(patternImg); //DOTALL mode
                    matcher = pattern.matcher(urlContent);
                }
                if (!matcher.find(0)){
                    patternImg = String.format("(?s)<%s[^>]*id=['\"]?\\w*-??%s\\d?['\"]?>", elementName, propertyName);
                    pattern = Pattern.compile(patternImg); //DOTALL mode
                    matcher = pattern.matcher(urlContent);
                }
                if (!matcher.find(0)){
                    patternImg = String.format("(?s)<%s[^>]*id=['\"]?\\w*-??%s\\d?['\"]?[^>]*>", elementName, propertyName);
                    pattern = Pattern.compile(patternImg); //DOTALL mode
                    matcher = pattern.matcher(urlContent);
                }
                
                
                //2. go backward and find h3 or h2 section start
                if (matcher.find(0)) {
                    int sectionStart = -1;
                    int from = matcher.start();

                    int state = 0;
                    loop:
                    for (int i = from; i > 0; i--) {
                        char c = urlContent.charAt(i);
                        switch (state) {
                            case 0:
                                if (c == '2' || c == '3' || c == '4') {
                                    state = 1;
                                }
                                break;
                            case 1:
                                if (c == 'h') {
                                    state = 2;
                                } else {
                                    state = 0;
                                }
                                break;
                            case 2:
                                if (c == '<') {
                                    //found <h2 or <h3
                                    sectionStart = i;
                                    break loop;
                                } else {
                                    state = 0;
                                }
                                break;
                        }
                    }

                    //3.go forward and find next section start (h2 or h3)
                    //note: the section end can be limited by different heading
                    //level than was the opening heading!
                    if (sectionStart >= 0) {
                        //find next section
                        Pattern sectionEndFinder = Pattern.compile("(?s)<h[234]"); //NOI18N
                        Matcher findSectionEnd = sectionEndFinder.matcher(urlContent.subSequence(from, urlContent.length()));
                        if (findSectionEnd.find()) {
                            String help = urlContent.substring(sectionStart, from + findSectionEnd.start());
                            help = "<base href=\"" + propertyHelpURL.toExternalForm() +"\">" + help;
                            return help;
                        }
                    }

                } else {
                    //no pattern found, likely a bit different source
                    LOGGER.warning(String.format("No property anchor section pattern found for property '%s'", propertyUrl)); //NOI18N
                    return NO_HELP_MSG;
                }
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        return NO_HELP_MSG;
    }

    @Override
    public URL resolveLink(FileObject context, PropertyDefinition property, String link) {
        return null;
    }

    @Override
    public int getPriority() {
        return 500;
    }

    private synchronized String getSpecURL() {
        if(SPEC_ARCHIVE_INTERNAL_URL == null) {
            SPEC_ARCHIVE_INTERNAL_URL = createSpecURL();
        }
        return SPEC_ARCHIVE_INTERNAL_URL;
    }

    private String createSpecURL() {
        File file = InstalledFileLocator.getDefault().locate(SPEC_ARCHIVE_NAME, "org.netbeans.modules.css.editor", false); //NoI18N
        if (file != null) {
            try {
                URL urll = Utilities.toURI(file).toURL(); //toURI should escape the illegal characters like spaces
                assert FileUtil.isArchiveFile(urll);
                return FileUtil.getArchiveRoot(urll).toExternalForm();
            } catch (java.net.MalformedURLException e) {
                //should not happen
                LOGGER.log(Level.SEVERE, String.format("Error obtaining archive root URL for file '%s'", file.getAbsolutePath()), e); //NOI18N
            }
        } else {
            LOGGER.warning(String.format("Cannot locate the css documentation file '%s'.", SPEC_ARCHIVE_NAME)); //NOI18N
        }
        return null;
    }
}
