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
                        URL urll = f.toURI().toURL();
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
