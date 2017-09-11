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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.html.editor.javadoc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Pisl
 */
public class HelpManager {
    
    private static HelpManager manager = null;
    
    private Hashtable helpMap;
    private long lastChange;
    private String helpZipURL;
    private URL lastURL;
    
    /**  HelpManager a new instance of HelpManager */
    private HelpManager() {
        helpMap = null;
        lastChange = 0;
        helpZipURL = null;
        lastURL = null;
    }
    
    static public HelpManager getDefault(){
        if (manager == null){
            manager = new HelpManager();
        }
        return manager;
    }
    
    private void init(){
        if (helpMap != null)
            return;
        // This part of the code is for the easy way how to define config file.
        String help = "";
        try{
            //File file = InstalledFileLocator.getDefault().locate("docs/HtmlHelp.xml", null, false);
            /*File file = new File ("/space/cvs/trunk/HtmlHelp.xml");
            if (file != null && lastChange != file.lastModified()){
                System.out.println("Config file was changed");
                helpMap = null;
                lastChange = file.lastModified();
            }*/
            if (helpMap == null){
                SAXHelpHandler handler;
                try (InputStream in = HelpManager.class.getClassLoader()
                     .getResourceAsStream("org/netbeans/modules/html/editor/resources/HtmlHelp.xml")) {
                    if (in == null){
                        helpMap = new Hashtable();
                        return;
                    }
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    handler = new SAXHelpHandler();
                    java.util.Date start = new java.util.Date();
                    parser.parse(in, handler);
                }
                
                //parser.parse(file, handler);
                
                //System.out.println("Parsing config file takes " + (end.getTime() - start.getTime()));
                help = handler.getHelpFile();
                if (help == null || help.equals("")){
                    help = null;
                    helpMap = new Hashtable();
                    return;
                }
                
                helpMap = handler.getMap();
                
                File f = InstalledFileLocator.getDefault().locate(help, null, false); //NoI18N
                if (f != null){
                    try {
                        URL urll = f.toURL();
                        urll = FileUtil.getArchiveRoot(urll);
                        helpZipURL = new URI(urll.getProtocol(), urll.getFile(), urll.getRef()).toString();
                    } catch (java.net.MalformedURLException e){
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                        helpMap = new Hashtable();
                        return;
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | URISyntaxException e){
            e.printStackTrace();
            ErrorManager.getDefault().log(e.toString());
        }
    }
    
    public URL getRelativeHelpToLast(String link){
        return getRelativeURL(lastURL, link);
    }
    
    public URL getRelativeURL(URL baseurl, String link){
        String url = baseurl.toString();
        int index;
        if (link.trim().charAt(0) == '#'){
            index = url.indexOf('#');
            if (index > -1)
                url = url.substring(0,url.indexOf('#'));
            url = url + link;
        } else {
            index = 0;
            url = url.substring(0, url.lastIndexOf('/'));
            while ((index = link.indexOf("../", index)) > -1){      //NOI18N
                url = url.substring(0, url.lastIndexOf('/'));
                link = link.substring(index+3);
            }
            url = url + "/" + link; // NOI18N
        }
        URL newURL = null;
        try{
            newURL = new URL(url);
        } catch (java.net.MalformedURLException e){
            ErrorManager.getDefault().log(e.toString());
            return null;
        }
        return newURL;
    }
    
    public String getHelp(String key){
        if (key == null) return null;
        return getHelp(findHelpItem(key));
    }
    
    public String getHelp(TagHelpItem helpItem){
        URL url = getHelpURL(helpItem);
        if (url == null)
            return null;
        
        lastURL = url;
        String help = getHelpText(url);
        int offset = 0;
        //String head = null;
        if (help != null){
            //head = getHead(help);
            if (helpItem.getStartText() != null){
                offset = help.indexOf(helpItem.getStartText());
                if (offset > 0){
                    offset = offset + helpItem.getStartTextOffset();
                    help = help.substring(offset);
                }
            }
            if (helpItem.getEndText() != null){
                offset = help.indexOf(helpItem.getEndText());
                if (offset > 0 ) {
                    offset = offset + helpItem.getEndTextOffset();
                    help = help.substring(0, offset);
                }
            }
        } else {
            help = "";
        }
        if (helpItem.getTextBefore() != null)
            help = helpItem.getTextBefore() + help;
        if (helpItem.getTextAfter() != null)
            help = help + helpItem.getTextAfter();
        //if (help.length() > 0){
        //    help = head + help + "</body></html>";
        //}
        return help;
    }
    
    /*private String getHead(String help){
        String head = null;
        int index = help.indexOf ("</head>");
        if (index > 0){
            head = help.substring(0, index);
            head = head + "</head><body>";
        }
        return head;
    }*/
    public String getHelpText(URL url){
        if (url == null )
            return null;
        try{
            ByteArrayOutputStream baos;
            try (InputStream is = url.openStream()) {
                byte buffer[] = new byte[1000];
                baos = new ByteArrayOutputStream();
                int count = 0;
                do {
                    count = is.read(buffer);
                    if (count > 0) baos.write(buffer, 0, count);
                } while (count > 0);
            }
            String text = baos.toString();
            baos.close();
            return text;
        } catch (java.io.IOException e){
            e.printStackTrace();
            return null;
        }
    }
    
    public URL getHelpURL(String key){
        return getHelpURL(findHelpItem(key));
    }
    
    public URL getHelpURLForLink(String link) {
        URL url = null;
        
        if(link != null){
            String surl = helpZipURL + link;
            try{
                url = new URL(surl);
            } catch (java.net.MalformedURLException e){
                ErrorManager.getDefault().log(e.toString());
                return null;
            }
        }
        
        return url;
    }
    
    public URL getHelpURL(TagHelpItem helpItem){
        URL url = null;
        
        if(helpItem != null){
            String surl = helpZipURL + helpItem.getFile();
            try{
                url = new URL(surl);
            } catch (java.net.MalformedURLException e){
                ErrorManager.getDefault().log(e.toString());
                return null;
            }
        }
        
        return url;
    }
    
    
    public TagHelpItem findHelpItem(String key){
        if (key == null) return null;
        init();
        Object o = helpMap.get(key.toUpperCase(Locale.ENGLISH));
        if (o != null){
            TagHelpItem helpItem = (TagHelpItem)o;
            
            if (helpItem != null)
                while (helpItem != null && helpItem.getIdentical() != null){
                helpItem = (TagHelpItem)helpMap.get(helpItem.getIdentical().toUpperCase());
                }
            
            return helpItem;
        }
        return null;
    }
    
    public String getHelpText(URL url, String anchor) {
        String pattern = "<a name=\"" + anchor + "\"";
        String text = getHelpText(url);
        BufferedReader br = new BufferedReader(new StringReader(text));
        String line = null;
        StringBuffer textAfterAnchor = null;
        int prestack = 0;
        try {
            while((line = br.readLine()) != null) {
                if(line.indexOf(pattern) != -1) {
                    //found the anchor -> cut off everything before
                    textAfterAnchor = new StringBuffer();
                    textAfterAnchor.append(line.substring(line.indexOf(pattern)));
                } else if(textAfterAnchor != null) {
                    //missing <pre> tag hack
                    if(line.indexOf("<pre") != -1) prestack++;
                    if(line.indexOf("</pre") != -1) prestack--;
                    
                    textAfterAnchor.append(line+"\n");
                }
            }
        }catch(IOException ioe ) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            
        }
        return textAfterAnchor == null ? null : "<html><body>" + (prestack < 0 ? "<pre>" : "") + textAfterAnchor.toString();
    }
        
    public String getAnchorText(URL url) {
        String link = url.toExternalForm();
        if(link.indexOf('#') != -1) return link.substring(link.indexOf('#') + 1);
        else return null;
    }
    
}
