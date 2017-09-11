/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssHelpResolver {

    private static CssHelpResolver INSTANCE = new CssHelpResolver(
            "org/netbeans/modules/css/resources/css_property_help"); //NOI18N
    
    private static Pattern NAV_BAR = Pattern.compile("<div\\s+class\\s*=\\s*\"navbar\"",  //NOI18N
            Pattern.MULTILINE);
    
    private Pattern END_SEARCH = Pattern.compile("<[hH][1-5]>|<a\\s+name=\"propdef-",  //NOI18N
            Pattern.MULTILINE);  //NOI18N
    
    private Pattern PROP_DEF_PATTERN = Pattern.compile( 
            "<div\\s+class\\s*=\\s*\"propdef\"" );              // NOI18N
    
    private static final String HELP_LOCATION = "docs/css21-spec.zip"; //NOI18N
    private static URL HELP_ZIP_URL;
    private WeakHashMap<String, String> pages_cache = new WeakHashMap<>();

    public static CssHelpResolver instance() {
        return INSTANCE;
    }

    private CssHelpResolver(String sourcePath) {
        parseSource(sourcePath);
    }
    private Map<String, PropertyDescriptor> properties;

    public String getPropertyHelp(String propertyName) {
        URL helpURL = getPropertyHelpURL(propertyName);
        if(helpURL == null) {
            return null;
        } else {
            return getHelpText(helpURL);
        }
    }

    private String getHelpText(URL url) {
        if (url == null) {
            return null;
        }

        //strip off the anchor url part
        String path = url.getPath();

        //try to load from cache
        String file_content = pages_cache.get(path);
        if (file_content == null) {
            try {
                ByteArrayOutputStream baos;
                try (InputStream is = url.openStream()) {
                    byte buffer[] = new byte[1000];
                    baos = new ByteArrayOutputStream();
                    int count = 0;
                    do {
                        count = is.read(buffer);
                        if (count > 0) {
                            baos.write(buffer, 0, count);
                        }
                    } while (count > 0);
                }
                file_content = baos.toString();
                baos.close();
            } catch (java.io.IOException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Cannot read css help file.", e); //NOI18N
            }

            pages_cache.put(path, file_content);
        }

        //strip off the "anchor part"
        String anchor = url.getRef();

        Pattern start_search = Pattern.compile("^.*<a\\s+name=\"" + anchor + "\".*$", 
                Pattern.MULTILINE); //NOI18N
        Matcher matcher = start_search.matcher(file_content);
//        int anchor_index = file_content.indexOf("<a name=\"" + anchor + "\"");
        //find line beginning
        int start_index = 0;
        int begin_end_search_from = 0;
        if (matcher.find()) {
            start_index = matcher.start();
            begin_end_search_from = matcher.end();
        }

        start_index = findSectionStart( start_index, file_content );

//        for (start_index = anchor_index; start_index > 0; start_index--) {
//            char ch = file_content.charAt(start_index);
//            if (ch == '\n') {
//                break;
//            }
//        }

        //find end of the "anchor part"
        int end_index = file_content.length();

        matcher = END_SEARCH.matcher(file_content.subSequence(begin_end_search_from,
                file_content.length()));
        if (matcher.find()) {
            end_index = matcher.start() + begin_end_search_from ;
        }

        String anchor_part = file_content.substring(start_index, end_index);
        
        matcher = NAV_BAR.matcher( anchor_part );
        if( matcher.find()){
            anchor_part = anchor_part.substring( 0 , matcher.start() );
        }

        int firstLineEnd = anchor_part.indexOf("\n");  //NOI18N
        if (firstLineEnd > 0) {
            String firstLine = anchor_part.substring(0, firstLineEnd);
            firstLine = firstLine.replaceAll("<strong>'", "<strong style=\"font-size: large\">");   //NOI18N
            firstLine = firstLine.replaceAll("'</strong>", "</strong>"); //NOI18N
            anchor_part = firstLine + anchor_part.substring(firstLineEnd + 1);
        }
        return anchor_part;

    }

    //needed for resolving links
    public URL getPropertyHelpURL(String propertyName) {
        String hzurl = CssHelpResolver.getHelpZIPURLasString();
        if(hzurl == null) {
            return null;
        }
        PropertyDescriptor pd = getPD(propertyName);
        if (pd == null) {
            return null;
        } else {
            try {
                return new URL(hzurl + pd.helpLink);
            } catch (MalformedURLException ex) {
                Logger.getAnonymousLogger().log(Level.WARNING, 
                        "Error creating URL for property " + propertyName, ex); //NOI18N
                return null;
            }
        }
    }

//    public URL getPropertyValueHelp(String propertyName, String propertyValueName) {
//        PropertyDescriptor pd = getPD(propertyName);
//        if (pd != null) {
//            String valueHelpLink = pd.values.get(propertyValueName);
//            if (valueHelpLink == null) {
//                Logger.getAnonymousLogger().warning("No such value " + propertyValueName + " for property " + propertyName);
//            } else {
//                try {
//                    return new URL(valueHelpLink);
//                } catch (MalformedURLException ex) {
//                    Logger.getAnonymousLogger().log(Level.WARNING, "Error creating URL for property value " + propertyValueName + " (property " + propertyName + ")", ex);
//                }
//            }
//        }
//        return null;
//    }
    private PropertyDescriptor getPD(String propertyName) {
        return properties.get(propertyName.toLowerCase());
    }
    
    private int findSectionStart( int startIndex, String fileContent ) {
        int index = startIndex;
        for( ; index>=0 ; index--){
            char ch = fileContent.charAt(index);
            if ( ch == '<'){
                char next = fileContent.charAt( index +1 );
                if ( (next == 'h' || next=='H' ) && Character.isDigit( 
                        fileContent.charAt(index+2)))
                {
                    // header tag ?
                    StringBuilder builder = new StringBuilder();
                    builder.append( ch );
                    builder.append( next );
                    builder.append("\\d");      // NOI18N
                    builder.append('>');
                    
                    Pattern pattern = Pattern.compile(builder.toString());
                    Matcher matcher = pattern.matcher( fileContent );
                    if ( matcher.find( index )){
                        break;
                    }
                }
            }
        }
        if ( index >0 ){
            String beggining = fileContent.substring( index , startIndex );
            Matcher matcher = PROP_DEF_PATTERN.matcher( beggining);
            int matchIndex = 0;
            while ( matcher.find()){
                matchIndex = matcher.start();
            }
            return index+matchIndex;
        }
        else {
            return startIndex;
        }
    }

    private void parseSource(String sourcePath) {
        ResourceBundle bundle = NbBundle.getBundle(sourcePath);

        properties = new HashMap<>();

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            //the bundle key is the property link; the property name is extracted from the link
            String helpLink = keys.nextElement();

            int propertyNameIdx = helpLink.indexOf('-');
            String propertyName = helpLink.substring(propertyNameIdx + 1);

            String value = bundle.getString(helpLink);

            //parse the value - delimiter is semicolon
            StringTokenizer st = new StringTokenizer(value, ";"); //NOI18N
            Map<String, String> valueToLink = new HashMap<>();
            while (st.hasMoreTokens()) {
                String val = st.nextToken();
                int propertyValueIdx = helpLink.indexOf('-');
                String valueName = helpLink.substring(propertyValueIdx + 1);
                valueToLink.put(valueName, val);
            }

            PropertyDescriptor pd = new PropertyDescriptor(propertyName, helpLink, valueToLink);
            properties.put(propertyName, pd);

        }

    }

    public static synchronized String getHelpZIPURLasString() {
        return getHelpZIPURL() == null ? null : getHelpZIPURL().toString();
    }

    public static synchronized URL getHelpZIPURL() {
        if (HELP_ZIP_URL == null) {
            File f = InstalledFileLocator.getDefault().locate(HELP_LOCATION, null, false); //NoI18N
            if (f != null) {
                try {
                    URL urll = f.toURI().toURL(); //toURI should escape the illegal characters like spaces
                    HELP_ZIP_URL = FileUtil.getArchiveRoot(urll);
                } catch (java.net.MalformedURLException e) {
                    ErrorManager.getDefault().notify(e);
                }
            } else {
		Logger.getLogger(CssHelpResolver.class.getSimpleName())
                        .info("Cannot locate the css documentation file " + HELP_LOCATION ); //NOI18N
	    }
        }

        return HELP_ZIP_URL;
    }

    private static class PropertyDescriptor {

        String propertyName;
        String helpLink;
        Map<String, String> values;

        private PropertyDescriptor(String propertyName, String helpLink, Map<String, String> values) {
            this.propertyName = propertyName;
            this.helpLink = helpLink;
            this.values = values;
        }
    }
}
