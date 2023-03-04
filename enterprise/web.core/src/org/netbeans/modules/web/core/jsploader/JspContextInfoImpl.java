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

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import javax.swing.text.Document;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.core.api.JspColoringData;
import org.netbeans.modules.web.core.api.JspContextInfo;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.loaders.DataObject;

public class JspContextInfoImpl extends JspContextInfo {
    
    public JspContextInfoImpl() {
    }
    
    private static TagLibParseSupport getTagLibParseSupport(FileObject fo) {
        TagLibParseSupport tlps = null;
        if (fo != null && fo.isValid()){
            try {
                tlps = (TagLibParseSupport)DataObject.find(fo).getCookie(TagLibParseSupport.class);
            }
            catch (org.openide.loaders.DataObjectNotFoundException e){
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        return tlps;
    }
    
    public URLClassLoader getModuleClassLoader(FileObject fo) {
        return JspParserFactory.getJspParser().getModuleClassLoader(WebModule.getWebModule (fo));
    }
    
    /** Returns the taglib map as returned by the parser, taking data from the editor as parameters.
     * Returns null in case of a failure (exception, no web module, no parser etc.)
     */
    public Map getTaglibMap(FileObject fo) {
        try {
            JspParserAPI parser = JspParserFactory.getJspParser();
            if (parser == null) {
                Logger.getLogger("global").log(Level.INFO, null, new NullPointerException());
            }
            else {
                WebModule webModule = WebModule.getWebModule(fo);
                if (webModule != null) {
                    return parser.getTaglibMap(webModule);
                }
            }
        }
        catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return null;
    }
    
    /** This method returns an image, which is displayed for the FileObject in the explorer.
     * It is used to display objects in editor (e.g. in code completion).
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file. 
     */
    public java.awt.Image getIcon(FileObject fo) {
        java.awt.Image icon = null;
        try {
            icon = DataObject.find(fo).getNodeDelegate().getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        }
        catch(org.openide.loaders.DataObjectNotFoundException e) {
            e.printStackTrace(System.out);
        }
        return icon;
    }
    
   
    public JspParserAPI.ParseResult getCachedParseResult (FileObject fo, boolean successfulOnly, boolean preferCurrent, boolean forceParse) {
        TagLibParseSupport sup = getTagLibParseSupport (fo);
        if (sup != null) {
            return sup.getCachedParseResult (successfulOnly, preferCurrent, forceParse);
        }
        return null;
    }
    
    public JspParserAPI.ParseResult getCachedParseResult (FileObject fo, boolean successfulOnly, boolean preferCurrent) {
        return getCachedParseResult(fo, successfulOnly, preferCurrent, false);
    }
    
    public JspColoringData getJSPColoringData (FileObject fo) {
        TagLibParseSupport sup = getTagLibParseSupport (fo);
        if (sup != null) {
            return sup.getJSPColoringData ();
        }
        return null;
    }
    
    public org.netbeans.modules.web.jsps.parserapi.JspParserAPI.JspOpenInfo getCachedOpenInfo(FileObject fo, boolean preferCurrent) {
        TagLibParseSupport sup = getTagLibParseSupport (fo);
        if (sup != null) {
            return sup.getCachedOpenInfo(preferCurrent, true);
        }
        return null;
    }
    
    public FileObject guessWebModuleRoot (FileObject fo) {
        WebModule wm =  WebModule.getWebModule (fo);
        if (wm != null)
            return wm.getDocumentBase ();
        return null;
    }
}
