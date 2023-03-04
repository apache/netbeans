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

package org.netbeans.modules.web.debug.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.openide.filesystems.*;
import org.openide.text.*;
import org.openide.cookies.*;

import org.netbeans.modules.web.api.webmodule.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.JSPServletFinder;

import org.netbeans.api.project.*;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;



/**
 *
 * @author Martin Grebac
 */
public class Utils {
    
    /** Logger for web.debug module. */
    private static Logger err = Logger.getLogger("org.netbeans.modules.web.debug");   // NOI18N
    private static final boolean LOG = err.isLoggable(Level.FINE);
    
    private static final Map<String, Reference<FileObject>> documentBaseByContextPath = new HashMap<String, Reference<FileObject>>();
    private static final Set<String> relativePathsWithContext = new HashSet<String>();
    
    public static Logger getEM () {
        return err;
    }
    
    public static void log (String msg) {
        if(LOG) {
            err.log(Level.FINE, msg);
        }
    }
    
    public static FileObject getFileObjectFromUrl(String url) {
        
        FileObject fo = null;
        
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            //noop
        }

        return fo;
    }
    
    public static boolean isJsp(FileObject fo) {
        return fo != null && "text/x-jsp".equals(fo.getMIMEType());   //NOI18N
    }
    
    public static boolean isJsp(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        return isJsp(fo);
    }

    public static boolean isTag(FileObject fo) {
        return fo != null && "text/x-tag".equals(fo.getMIMEType());   //NOI18N
    }
    
    public static boolean isTag(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        return isTag(fo);
    }

    public static String getTargetServerID(FileObject fo) {
        if (fo != null) {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null) {
                J2eeModuleProvider mp = (J2eeModuleProvider)p.getLookup().lookup(J2eeModuleProvider.class);
                if (mp != null) {
                    String serverID = mp.getServerID();
                    return serverID;
                }
            }
        }
        return null;
    }
    
    public static String getJspName(String url) {

        FileObject fo = getFileObjectFromUrl(url);
        if (fo != null) {
            return fo.getNameExt();
        }
        return (url == null) ? null : url.toString();
    }
    
    public static String getJspPath(String url) {       
        FileObject fo = getFileObjectFromUrl(url);
        if (fo == null) {
            return null;
        }
        
        String jspRelativePath = url;
        WebModule wm = WebModule.getWebModule (fo);
        if (wm != null) {
            FileObject documentBase = wm.getDocumentBase();
            if (documentBase != null) {
                jspRelativePath = FileUtil.getRelativePath(wm.getDocumentBase(), fo);
            } else { // doc base is invalid
                return null;
            }
        }
        JSPServletFinder finder = JSPServletFinder.findJSPServletFinder (fo);
        if (finder != null) {
            String translated = finder.getServletSourcePath(jspRelativePath);
            if (translated != null) {
                return translated;
            }
        }
        return jspRelativePath;
    }
    
    public static String getServletClass(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        if (fo == null) {
            return null;
        }
        JSPServletFinder finder = JSPServletFinder.findJSPServletFinder (fo);
        WebModule wm = WebModule.getWebModule (fo);
        if (wm == null)
            return null;

        FileObject docBase = wm.getDocumentBase();
        String jspRelativePath;
        if (docBase != null) {
            jspRelativePath = FileUtil.getRelativePath(docBase, fo);
            if (jspRelativePath == null) {
                // fo is not under docBase
                return null;
            }
        } else {
            // ?
            jspRelativePath = fo.getPath();
        }
        String contextPath = wm.getContextPath();

        String servletPath = finder.getServletResourcePath(jspRelativePath);      
        if (servletPath == null) // we don't have class name, so assume we are debugging tomcat or appsrv
                servletPath = JspNameUtil.getServletResourcePath(finder, contextPath, jspRelativePath);
        if (servletPath != null) {
            servletPath = servletPath.substring(0, servletPath.length()-5); // length of ".java"
            servletPath = servletPath.replace('/', '.'); //NOI18N
        }
        Utils.log("servlet class: " + servletPath);
        return servletPath;
    }

    public static String getClassFilter(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        if (fo == null) {
            return null;
        }
        JSPServletFinder finder = JSPServletFinder.findJSPServletFinder (fo);
        
        String filter = getServletClass(url);
        if (filter != null) {
            // get package only
            int lastDot = filter.lastIndexOf('.');
            if (lastDot > 0) {
                filter = filter.substring(0, lastDot) + ".*"; //NOI18N
                String basePackageName = finder.getServletBasePackageName();
                if (basePackageName == null) {
                    basePackageName = "org.apache.jsp"; // NOI18N
                }
                if (filter.startsWith(basePackageName)) 
                    filter = basePackageName + ".*"; // NOI18N
            }
        }
        return filter;
    }
    
//    public static String getCompoundClassFilter(String url) {
//
//        FileObject wmfo = getJspFileObjectFromUrl(url);
//        if (wmfo == null) {
//            return null;
//        }
//        WebModule wm = WebModule.getWebModule(wmfo);
//        Enumeration files = wm.getDocumentBase().getChildren(true);
//                        
//        String filter = null; //NOI18N
//        while (files.hasMoreElements()) {
//            FileObject fo = (FileObject)files.nextElement();
//            if (!fo.isFolder() && "text/x-jsp".equals(fo.getMIMEType()) && (fo != null)) {
//                String jspRelPath = FileUtil.getRelativePath(wm.getDocumentBase(), fo);
//                JSPServletFinder finder = JSPServletFinder.findJSPServletFinder(fo);
//                String servletPath = finder.getServletResourcePath(jspRelPath);
//                if ((servletPath != null) && !servletPath.equals("")) {                
//                    servletPath = servletPath.substring(0, servletPath.length()-5); // length of ".java"
//                    servletPath = servletPath.substring(0, servletPath.lastIndexOf('/')); // get package only
//                    if (filter == null) {
//                        filter = servletPath;
//                    } else {
//                        if (!(servletPath.startsWith(filter))) {
//                            while (!servletPath.startsWith(filter)) {
//                                filter = filter.substring(0, filter.lastIndexOf('/'));
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        filter = filter.replace('/', '.') + "."; //NOI18N
//        Utils.getEM().log("compound filter: " + filter);
//        return filter;
//    }

    public static String getContextPath(String url) {
        FileObject wmfo = getFileObjectFromUrl(url);
        if (wmfo == null) {
            return null;
        }
        WebModule wm = WebModule.getWebModule(wmfo);        
        if (wm != null) {
            String conextPath = wm.getContextPath();
            FileObject documentBase = wm.getDocumentBase();
            if (documentBase != null) {
                synchronized (documentBaseByContextPath) {
                    documentBaseByContextPath.put(conextPath, new WeakReference<FileObject>(documentBase));
                }
                String relativePath = FileUtil.getRelativePath(documentBase, wmfo);
                if (relativePath != null) {
                    synchronized (relativePathsWithContext) {
                        relativePathsWithContext.add(relativePath);
                    }
                }
            }
            return conextPath;
        }
        return null;   
    }
    
    public static boolean hasContext(String relativePath) {
        synchronized (relativePathsWithContext) {
            return relativePathsWithContext.contains(relativePath);
        }
    }
    
    public static FileObject getDocumentBaseForContextPath(String contextPath) {
        FileObject fo = null;
        Reference<FileObject> foRef;
        synchronized (documentBaseByContextPath) {
            foRef = documentBaseByContextPath.get(contextPath);
        }
        if (foRef != null) {
            fo = foRef.get();
        }
        return fo;
    }
        
    public static String getJavaIdentifier(StyledDocument doc, JEditorPane ep, int offset) {        
        String t = null;
        if ( (ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
            t = ep.getSelectedText();
        }
        if (t != null) {
            return t;
        }
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        try {
            javax.swing.text.Element lineElem = 
                org.openide.text.NbDocument.findLineRootElement(doc).
                getElement(line);
            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 && 
                (Character.isJavaIdentifierPart (
                    t.charAt (identStart - 1)
                ) ||
                (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen && Character.isJavaIdentifierPart(t.charAt(identEnd))) {
                identEnd++;
            }
            if (identStart == identEnd) {
                return null;
            }
            return t.substring (identStart, identEnd);
        } catch (javax.swing.text.BadLocationException e) {
            return null;
        }
    }    

    public static boolean isScriptlet(StyledDocument doc, JEditorPane ep, int offset) {
        String t;
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        try {
            while (line > 0) {
                javax.swing.text.Element lineElem = 
                    org.openide.text.NbDocument.findLineRootElement(doc).getElement(line);
                if (lineElem == null) {
                    continue;
                }
                int lineStartOffset = lineElem.getStartOffset();
                int lineLen = lineElem.getEndOffset() - lineStartOffset;
                t = doc.getText (lineStartOffset, lineLen);
                if ((t != null) && (t.length() > 1)) {
                    int identStart;
                    if (line == NbDocument.findLineNumber(doc, offset)) {
                        identStart = col;
                    } else {
                        identStart = lineLen-1;
                    }
                    while (identStart > 0) {
                        if ((t.charAt(identStart) == '%') && (t.charAt(identStart-1) == '<')) {
                            return true;
                        }
                        if ((t.charAt(identStart) == '>') && (t.charAt(identStart-1) == '%')) {
                            return false;
                        }                    
                        identStart--;
                    }
                }
                line--;
            }
        } catch (javax.swing.text.BadLocationException e) {
        }
        return false;
    }        
    
    public static String getELIdentifier(StyledDocument doc, JEditorPane ep, int offset) {
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        )   t = ep.getSelectedText ();
        if (t != null) {
            if ((t.startsWith("$")) && (t.endsWith("}"))) {
                return t;
            } else {
                return null;
            }
        }
        
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        try {
            javax.swing.text.Element lineElem = 
                org.openide.text.NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 && (t.charAt(identStart) != '$')) {
                identStart--;
            }
            if ((identStart > 0) && (t.charAt(identStart) == '$') && (t.charAt(identStart-1) == '\\')) {
                return null;
            }
            int identEnd = col;
            while ((identEnd < lineLen) && identEnd > 0 && identEnd <= t.length() && (t.charAt(identEnd-1) != '}'))  {
                identEnd++;
            }
            if (identStart == identEnd) {
                return null;
            }
            String outp = t.substring(identStart, identEnd);
            if ((outp.startsWith("$")) && (outp.endsWith("}"))) {
                return outp;
            } else {            
                return null;
            }
        } catch (javax.swing.text.BadLocationException e) {
            return null;
        }
    }
    
    public static String getJavaIdentifier () {
        JEditorPane ep = EditorContextDispatcher.getDefault().getCurrentEditor();
        if (ep == null) {
            return null;
        }
        return getJavaIdentifier (
            (StyledDocument) ep.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }

//    public static String getELIdentifier () {
//        EditorCookie e = getCurrentEditorCookie ();
//        if (e == null) {
//            return null;
//        }
//        JEditorPane ep = getCurrentEditor (e);
//        if (ep == null) {
//            return null;
//        }
//        return getELIdentifier (
//            e.getDocument (),
//            ep,
//            ep.getCaret ().getDot ()
//        );
//    }

    public static boolean isScriptlet() {
        JEditorPane ep = EditorContextDispatcher.getDefault().getCurrentEditor();
        if (ep == null) {
            return false;
        }
        return isScriptlet(
            (StyledDocument) ep.getDocument (),
            ep,
            ep.getCaret ().getDot ()
        );
    }
  
    
    
}
