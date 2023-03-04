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

package org.netbeans.modules.web.core.syntax.completion;

import org.netbeans.modules.web.core.syntax.completion.api.JspCompletionItem;
import java.util.*;
import java.beans.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import javax.swing.ImageIcon;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.modules.web.core.syntax.completion.JavaJspCompletionProvider.CompletionQueryDelegatedToJava;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.openide.util.ImageUtilities;


/** Support for code completion of default JSP tags.
 *
 * @author  pjiricka
 * @author  Marek Fukala
 */
public class AttrSupports {
    
    private static final Logger logger = Logger.getLogger(AttrSupports.class.getName());
    
    public static class ScopeSupport extends AttributeValueSupport.Default {
        
        public ScopeSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("application");    // NOI18N
            list.add("page");           // NOI18N
            list.add("request");        // NOI18N
            list.add("session");        // NOI18N
            return list;
        }
        
    }

    public static class BodyContentSupport extends AttributeValueSupport.Default {

        public BodyContentSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }

        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("scriptless");    // NOI18N
            list.add("tagdependent");           // NOI18N
            list.add("empty");        // NOI18N
            return list;
        }

    }
    
    public static class RootVersionSupport extends AttributeValueSupport.Default {
        
        public RootVersionSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("1.2");    // NOI18N
            list.add("2.0");           // NOI18N
            return list;
        }
        
    }
    
    public static class PluginTypeSupport extends AttributeValueSupport.Default {
        
        public PluginTypeSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("bean");    // NOI18N
            list.add("applet");           // NOI18N
            return list;
        }
        
    }
    
    public static class VariableScopeSupport extends AttributeValueSupport.Default {
        
        public VariableScopeSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("AT_BEGIN");    // NOI18N
            list.add("AT_END");           // NOI18N
            list.add("NESTED");        // NOI18N
            return list;
        }
        
    }
    
    public static class YesNoTrueFalseSupport extends AttributeValueSupport.Default {
        
        public YesNoTrueFalseSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("false");    // NOI18N
            list.add("no");           // NOI18N
            list.add("true");        // NOI18N
            list.add("yes");        // NOI18N
            return list;
        }
        
    }
    
    /**
     * Provides code completion for a class name context
     */
    @Deprecated
    public static class ClassNameSupport extends AttributeValueSupport.Default {
        
        public ClassNameSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return new ArrayList();
        }
        
        protected String getFakedClassBody(String prefix){
            return "class Foo extends " + prefix; //NOI18N
        }
        
        /** Returns the complete result that contains elements from getCompletionItems.   */
        @Override
        public void result(JspCompletionQuery.CompletionResultSet result, JTextComponent component, int offset,
                JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            
            String fakedClassBody = getFakedClassBody(valuePart);
            int shiftedOffset = fakedClassBody.length();
            
            logger.fine("JSP CC: delegating CC query to java file:\n" //NOI18N
                            + fakedClassBody.substring(0, shiftedOffset)
                            + "|" + fakedClassBody.substring(shiftedOffset) + "\n"); //NOI18N
            
            CompletionQueryDelegatedToJava delegate = new CompletionQueryDelegatedToJava(
                    offset, shiftedOffset, CompletionProvider.COMPLETION_QUERY_TYPE);
            
            delegate.create(component.getDocument(), fakedClassBody);
            List<? extends CompletionItem> items =  delegate.getCompletionItems();
            result.addAllItems(items);
            result.setAnchorOffset(offset - (valuePart.length() - valuePart.lastIndexOf('.')) + 1);
        }
   
    }
    
    /**
     * Provides code completion for a comma-separated list of imports context
     */
    @Deprecated
    public static class PackageListSupport extends ClassNameSupport {
        
        public PackageListSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override protected String getFakedClassBody(String prefix){
            int commaPos = prefix.lastIndexOf(",");
            
            if (commaPos > -1){
                prefix = prefix.substring(commaPos + 1);
            }
            
            return "import " + prefix; //NOI18N
        }
    }
    
    public static class GetSetPropertyName extends AttributeValueSupport.Default {
        
        public GetSetPropertyName(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            PageInfo.BeanData[] beanData = sup.getBeanData();
            if(beanData != null) {
                for (int i = 0; i < beanData.length; i++) {
                    list.add(beanData[i].getId());
                }
            }
            return list;
        }
        
    }
    
    
    public abstract static class GetSetPropertyProperty extends AttributeValueSupport.Default {
        
        public GetSetPropertyProperty(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item, boolean setter) {
            ArrayList<String> list = new ArrayList<String>();
            String namePropertyValue = (String)item.getAttributes().get("name");    // NOI18N
            if (namePropertyValue != null) {
                String className = null;
                PageInfo.BeanData[] beanData = sup.getBeanData();
                for (int i = 0; i < beanData.length; i++) {
                    if (beanData[i] == null || beanData[i].getId() == null)
                        continue;
                    
                    if (beanData[i].getId().equals(namePropertyValue)) {
                        className = beanData[i].getClassName();
                        break;
                    }
                }
                
                if (className != null) {
                    ClassLoader cld = null;
                    try {
                        FileObject fileObject = NbEditorUtilities.getDataObject( sup.getDocument()).getPrimaryFile();
                        cld = JspUtils.getModuleClassLoader(fileObject);
                        Class beanClass = Class.forName(className, false, cld);
                        Introspector.flushFromCaches(beanClass);
                        BeanInfo benInfo = Introspector.getBeanInfo(beanClass);
                        PropertyDescriptor[] properties = benInfo.getPropertyDescriptors();
                        for (int j = 0; j < properties.length; j++) {
                            if (setter && (properties[j].getWriteMethod() != null))
                                list.add(properties[j].getName());
                            if (!setter && (properties[j].getReadMethod() != null) && !properties[j].getName().equals("class")) //NOI18N
                                list.add(properties[j].getName());
                        }
                    } catch (ClassNotFoundException e) {
                        //do nothing
                    } catch (IntrospectionException e) {
                        //do nothing
                    } finally {
                        // avoids JAR locking
                        if (cld instanceof Closeable) {
                            try {
                                ((Closeable) cld).close();
                            } catch (IOException ex) {
                                Logger.getLogger(AttrSupports.class.getName()).log(Level.INFO, null, ex);
                            }
                        }
                    }
                }
            }
            return list;
        }
    }
    
    public static class GetPropertyProperty extends GetSetPropertyProperty {
        
        public GetPropertyProperty() {
            super(true, "jsp:getProperty", "property");     // NOI18N
        }
        
        @Override
        public List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return possibleValues(sup, item, false);
        }
        
    }
    
    public static class SetPropertyProperty extends GetSetPropertyProperty {
        
        public SetPropertyProperty() {
            super(true, "jsp:setProperty", "property"); // NOI18N
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            List<String> list = possibleValues(sup, item, true);
            list.add(0, "*");  // NOI18N
            return list;
        }
        
    }
    
    public static class TaglibURI extends AttributeValueSupport.Default {
        
        public TaglibURI() {
            super(false, "taglib", "uri");      // NOI18N
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            List<String> list = new ArrayList<String>();
            Map map = sup.getTagLibraryMappings();
            if (map != null) {
                Iterator iterator = map.keySet().iterator();
                while(iterator.hasNext()) {
                    String s = (String)iterator.next();
                    list.add(s);
                }
            }
            // sort alphabetically
            Collections.sort(list);
            return list;
        }
        
    }
    
    public static class TaglibTagdir extends AttributeValueSupport.Default {
        
        public TaglibTagdir() {
            super(false, "taglib", "tagdir");      // NOI18N
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            List<String> l = new ArrayList<String>();
            FileObject orig = sup.getFileObject();
            FileObject documentBase = JspUtils.guessWebModuleRoot(orig);
            if (documentBase != null) {
                FileObject webInfTags = JspUtils.findRelativeFileObject(documentBase, "WEB-INF/tags");
                if (webInfTags != null) {
                    // WEB-INF/tags itself
                    if (isValidTagDir(webInfTags)) {
                        l.add(JspUtils.findRelativeContextPath(documentBase, webInfTags));
                    }
                    // subfolders of WEB-INF/tags
                    Enumeration en = webInfTags.getFolders(true);
                    while (en.hasMoreElements()) {
                        FileObject subF = (FileObject)en.nextElement();
                        if (isValidTagDir(subF)) {
                            l.add(JspUtils.findRelativeContextPath(documentBase, subF));
                        }
                    }
                }
            }
            // sort alphabetically
            Collections.sort(l);
            return l;
        }
        
        private boolean isValidTagDir(FileObject subF) {
            // must contain at least one file
            return subF.getChildren(false).hasMoreElements();
        }
        
    }
    
    
    /** Support for code completing of package and class. */
    public static class FilenameSupport extends AttributeValueSupport.Default {
        static final ImageIcon PACKAGE_ICON =
                ImageUtilities.loadImageIcon("org/openide/loaders/defaultFolder.gif", false); // NOI18N
      
        public FilenameSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        /** Returns the complete result that contains elements from getCompletionItems.   */
        @Override
        public void result(JspCompletionQuery.CompletionResultSet result, JTextComponent component, int offset,
                JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            String path = "";   // NOI18N
            String fileNamePart = valuePart;
            int lastSlash = valuePart.lastIndexOf('/');
            if (lastSlash == 0) {
                path = "/"; // NOI18N
                fileNamePart = valuePart.substring(1);
            } else if (lastSlash > 0) { // not a leading slash?
                path = valuePart.substring(0, lastSlash);
                fileNamePart = (lastSlash == valuePart.length())? "": valuePart.substring(lastSlash+1);    // NOI18N
            }
            
            int anchor = offset - valuePart.length() + lastSlash + 1;  // works even with -1
            
            try {
                FileObject orig = sup.getFileObject();
                FileObject documentBase = JspUtils.guessWebModuleRoot(orig);
                // need to normalize fileNamePart with respect to orig
                String ctxPath = JspUtils.resolveRelativeURL("/"+orig.getPath(), path);  // NOI18N
                //is this absolute path?
                if (path.startsWith("/") && documentBase != null) {
                    ctxPath = documentBase.getPath() + path;
                } else {
                    ctxPath = ctxPath.substring(1);
                }

                FileSystem fs = orig.getFileSystem();
                
                FileObject folder = fs.findResource(ctxPath);
                if (folder != null) {
                    //add all accessible files from current context
                    result.addAllItems(files(anchor, folder, fileNamePart, sup));
                    
                    //add go up in the directories structure item
                    if (!folder.equals(documentBase) && !path.startsWith("/") // NOI18N
                            && (path.length() == 0 || (path.lastIndexOf("../")+3 == path.length()))){ // NOI18N
                        result.addItem(JspCompletionItem.createGoUpFileCompletionItem(anchor, java.awt.Color.BLUE, PACKAGE_ICON)); // NOI18N
                    }
                }
            } catch (FileStateInvalidException ex) {
                // unreachable FS - disable completion
            } catch (IllegalArgumentException ex) {
                // resolving failed
            }
            
            result.setAnchorOffset(anchor);
        }
        
        private List<JspCompletionItem> files(int offset, FileObject folder, String prefix, JspSyntaxSupport sup) {
            ArrayList<JspCompletionItem> res = new ArrayList<JspCompletionItem>();
            TreeMap<String, JspCompletionItem> resFolders = new TreeMap<String, JspCompletionItem>();
            TreeMap<String, JspCompletionItem> resFiles = new TreeMap<String, JspCompletionItem>();
            
            Enumeration<? extends FileObject> files = folder.getChildren(false);
            while (files.hasMoreElements()) {
                FileObject file = files.nextElement();
                String fname = file.getNameExt();
                if (fname.startsWith(prefix) && !"cvs".equalsIgnoreCase(fname)) {
                    
                    if (file.isFolder())
                        resFolders.put(file.getNameExt(), JspCompletionItem.createFileCompletionItem(file.getNameExt() + "/", offset, java.awt.Color.BLUE, PACKAGE_ICON));
                    else{
                        java.awt.Image icon = JspUtils.getIcon(file);
                        if (icon != null)
                            resFiles.put(file.getNameExt(), JspCompletionItem.createFileCompletionItem(file.getNameExt(), offset, java.awt.Color.BLACK, new javax.swing.ImageIcon(icon)));
                        else
                            resFiles.put(file.getNameExt(), JspCompletionItem.createFileCompletionItem(file.getNameExt(), offset, java.awt.Color.BLACK, null));
                    }
                }
            }
            res.addAll(resFolders.values());
            res.addAll(resFiles.values());
            
            return res;
        }
        
    }
    
    public static class TrueFalseSupport extends AttributeValueSupport.Default {
        
        public TrueFalseSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("false");   // NOI18N
            list.add("true");    // NOI18N
            return list;
        }
        
    }
    
    public static class PageLanguage extends AttributeValueSupport.Default {
        
        public PageLanguage() {
            super(false, "page", "language");    // NOI18N
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("java");    // NOI18N
            return list;
        }
        
    }
    
    public static class EncodingSupport extends AttributeValueSupport.Default {
        
        public EncodingSupport(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        @Override
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            ArrayList<String> list = new ArrayList<String>();
            Iterator<String> iter = java.nio.charset.Charset.availableCharsets().keySet().iterator();
            
            while (iter.hasNext())
                list.add(iter.next());
            
            return list;
        }
        
    }
    
}
