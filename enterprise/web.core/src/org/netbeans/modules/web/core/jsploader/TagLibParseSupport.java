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

package org.netbeans.modules.web.core.jsploader;

import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.web.core.api.ErrorInfo;
import org.netbeans.modules.web.core.jsploader.api.TagLibParseCookie;
import org.netbeans.modules.web.core.spi.ErrorAnnotation;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.JspOpenInfo;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.modules.web.core.api.JspColoringData;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.spi.ErrorAnnotationFactory;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/** Support for parsing JSP pages and tag files and cooperation between the parser 
 * and the editor.
 * Parsing is context-aware, which means that a web module and the associated 
 * environment (libraries) is needed to provide proper tag library coloring and completion and other webmodule-dependent
 * features. The support tries to do its best to get good parse results even for
 * pages and tag files for which the web module context is not known, but 
 * in this case nothing can be guaranteed.
 *
 * @author Petr Jiricka
 * @version 
 */
public class TagLibParseSupport implements org.openide.nodes.Node.Cookie, TagLibParseCookie {

    //allow max 10 requests to run in parallel & have one RP for all taglib parsings
    private static final RequestProcessor REQUEST_PROCESSOR = new RequestProcessor("background jsp parsing", 10); // NOI18N;

    private final FileObject jspFile;
    
    // request processing stuff
    private boolean documentDirty;
    private RequestProcessor.Task parsingTask = null;

//    private static final int WAIT_FOR_EDITOR_TIMEOUT = 15 * 1000; //15 seconds

    /** Holds a reference to the JSP coloring data. */
    private WeakReference<JspColoringData> jspColoringDataRef;
    
    /** Holds a time-based cache of the JspOpenInfo structure. */
    private TimeReference jspOpenInfoRef;
    
    /** Holds the last parse result: JspParserAPI.ParseResult (whether successful or not).
     * The editor should hold a strong reference to this object. That way, if the editor window
     * is closed, memory is reclaimed, but important data is kept when it is needed.
     */
    private SoftReference<JspParserAPI.ParseResult> parseResultRef;

    /** Holds the last successful parse result: JspParserAPI.ParseResult.
     * The editor should hold a strong reference to this object. That way, if the editor window
     * is closed, memory is reclaimed, but important data is kept when it is needed.
     */
    private SoftReference<JspParserAPI.ParseResult> parseResultSuccessfulRef;
    
    private final Object parseResultLock = new Object();
    private final Object openInfoLock = new Object();
    
    /** Holds a strong reference to the parsing 'successful' data during an editor 
     * pane is opened for a JSP corresponding to this support. 
     */
//    private Object parseResultSuccessfulRefStrongReference = null;

    //this field is used to try to catch the situation when someone calls the parser
    //before editor support is initialized - causing #49300
    private boolean wasAnEditorPaneChangeEvent = false;
    
    private boolean parsingTaskCancelled = false;
    
    /** Whether parser task was started (at least once) or not. */
    private boolean parserStarted = false;
    
    /** Holds reference for annotation errors
     */
    private ErrorAnnotation annotations;
    /** Creates new TagLibParseSupport 
     * @param jspFile the resource to parse
     */
    public TagLibParseSupport(FileObject jspFile) {
        this.jspFile = jspFile;
        annotations = ErrorAnnotationFactory.Query.create(jspFile);
    }

    /** Gets the tag library data relevant for the editor. */
    public JspColoringData getJSPColoringData() {
        // #120530 - do not start parsing
        return getJSPColoringData(false);
    }
    
    private WebModule getWebModule(FileObject fo){
        WebModule wm = WebModule.getWebModule(fo);
        if (wm != null){
            FileObject wmRoot = wm.getDocumentBase();
            if (wmRoot != null && (fo == wmRoot || FileUtil.isParentOf(wmRoot, fo))) {
                return wm;
            }
        }
        return null;
    }
    
    JspColoringData getJSPColoringData(boolean prepare) {
        if (jspColoringDataRef != null) {
            Object o = jspColoringDataRef.get();
            if (o != null)
                return (JspColoringData)o;
        }
        JspColoringData jcd = new JspColoringData(this);
        jspColoringDataRef = new WeakReference<>(jcd);
        if (prepare) {
            prepare();
        }
        return jcd;
    }

    /** Sets the dirty flag - if the document was modified after last parsing. */
    public void setDocumentDirty(boolean b) {
        documentDirty = b;
    }

    /** Tests the documentDirty flag. */
    public boolean isDocumentDirty() {
        return documentDirty;
    }

    /** Starts the parsing if the this class is 'dirty' and status != STATUS_NOT
    * and parsing is not running yet.
      @return parsing task so caller may listen on its completion.
    */
    public Task autoParse() {
        //do not parse if it is not necessary
        //this is the BaseJspEditorSupport optimalization since the autoParse causes the webmodule
        //to be reparsed even if it has already been reparsed.
        if(isDocumentDirty() || !isParserStarted()) {
            return parseObject(Thread.MIN_PRIORITY);
        } else {
            return REQUEST_PROCESSOR.post(new Runnable() {
                public void run() {
                    //do nothing, just a dummy task
                }
            });
        }
    }

    /** Method that instructs the implementation of the source element
    * to prepare the element. It is non blocking method that returns
    * task that can be used to control if the operation finished or not.
    *
    * @return task to control the preparation of the elemement
    */
    public Task prepare() {
        return parseObject(Thread.MAX_PRIORITY - 1);
    }

    private Task parseObject(int priority) {
        //reset the state so the next parsing will run normally
        parsingTaskCancelled = false;
        
        //debug #49300: print out current stacktrace when the editor support is not initialized yet
        if(!wasAnEditorPaneChangeEvent) 
            Exceptions.attachLocalizedMessage(new IllegalStateException(),
                  "The TagLibParseSupport.parseObject() is called before editor support is created!"); //NOI18N
        
        synchronized (parseResultLock) {
            RequestProcessor.Task t = parsingTask;

            if (t != null) {
                t.setPriority(Math.max(t.getPriority(), priority));
                return t;
            }
            setParserStarted();

            setDocumentDirty(false);
            t = REQUEST_PROCESSOR.post(new ParsingRunnable(), 0, priority);
            parsingTask = t;
            return parsingTask;
        }
    }
    
    //used for notifying the parsing thread (to start the parsing)
    void setEditorOpened(boolean editorOpened) {
        //mark that the an editor pane open event was fired
        wasAnEditorPaneChangeEvent = true;
        
        synchronized (parseResultLock) {
            if(!editorOpened) {
                //clean the stronref to the parsing data when the editor is closed
//                parseResultSuccessfulRefStrongReference = null;
            }
        }
        
    }
  
    void cancelParsingTask() {
        if(parsingTask !=  null) {
            //there is schedulled or running parsing task -> cancel it!
            boolean removed = parsingTask.cancel();
            parsingTask = null;
            jspColoringDataRef = null;
        }
        
        parsingTaskCancelled = true;
    }
    
    public JspParserAPI.JspOpenInfo getCachedOpenInfo(boolean preferCurrent, boolean useEditor) {
        synchronized (openInfoLock) {
            if (preferCurrent)
                jspOpenInfoRef = null;
            long timestamp = jspFile.lastModified().getTime();
            if (jspOpenInfoRef == null) {
                jspOpenInfoRef = new TimeReference();
            }
            JspParserAPI.JspOpenInfo info = (JspParserAPI.JspOpenInfo)jspOpenInfoRef.get(timestamp);
            if (info == null) {
                info = JspParserFactory.getJspParser().getJspOpenInfo(jspFile, getWebModule(jspFile), useEditor);
                jspOpenInfoRef.put(info, timestamp);
            }
            return info;
        }
    }
    
    public OpenInfo getOpenInfo(boolean preferCurrent, boolean useEditor) {
        JspOpenInfo delegate = getCachedOpenInfo(preferCurrent, useEditor);
        return OpenInfo.create(delegate.isXmlSyntax(), delegate.getEncoding());
    }
    
    public JspParserAPI.ParseResult getCachedParseResult(boolean successfulOnly, boolean preferCurrent) {
        return getCachedParseResult(successfulOnly, preferCurrent, false);
    }
    
    /** Returns a cached parse information about the page.
     * @param successfulOnly if true, and the page has been parsed successfully in the past, returns
     *  the result of this successful parse. Otherwise returns null.
     *  If set to false, never returns null.
     * @param needCurrent if true, attempts to return the result corresponding to the page exactly at this moment<br>
     *   If both parameters are true, and the page is currently successfully parsable, then returns this result, If it is
     *   unparsable, returns null.
     * @return the result of parsing this page
     */
    public JspParserAPI.ParseResult getCachedParseResult(boolean successfulOnly, boolean preferCurrent, boolean forceParse) {
        boolean needToParse = forceParse;
        
         if (preferCurrent && isDocumentDirty()) {
            // need to get an up to date copy
            needToParse = true;
        }
        if (parseResultRef == null) {
            // no information available
            needToParse = true;
        }
        
        JspParserAPI.ParseResult ret = null;
        SoftReference<JspParserAPI.ParseResult> myRef = successfulOnly ? parseResultSuccessfulRef : parseResultRef;
        if (myRef != null) {
            ret = myRef.get();
        }
        
        if ((ret == null) && (!successfulOnly)) {
            // to comply with the Javadoc regarding not returning null
            needToParse = true;
        }
        
        if (needToParse) {
            RequestProcessor.Task t = prepare(); // having the reference is important 
                                                 // so the SoftReference does not get garbage collected
            t.waitFinished();
            myRef = successfulOnly ? parseResultSuccessfulRef : parseResultRef;
            if (myRef != null) {
                ret = (JspParserAPI.ParseResult)myRef.get();
            }
        }
        return ret;
    }
    
    // Flag, whether there is already an error in the jsp page. 
    private boolean hasError = false;

    private synchronized boolean isParserStarted() {
        return parserStarted;
    }

    private synchronized void setParserStarted() {
        this.parserStarted = true;
    }
    
    private class ParsingRunnable implements Runnable {
        
        /** Holds the result of parsing. Need to hold it here
         * to make sure that we have a strong reference and the SoftReference
         * does not get garbage collected.
         */
        JspParserAPI.ParseResult locResult = null;
        
        public ParsingRunnable () {
        }
        
        public void run() {
            //test whether the parsing task has been cancelled -
            //someone called EditorCookie.close() during the parsing was waiting
            //on openedLock
            if(!parsingTaskCancelled && getWebModule(jspFile) != null) {
                JspParserAPI parser = JspParserFactory.getJspParser();
                // assert parser != null;
                if (parser == null) {
                    throw new InternalError();
                }
                
                getJSPColoringData(false).parsingStarted();
                
                locResult = parser.analyzePage(jspFile, getWebModule(jspFile), JspParserAPI.ERROR_IGNORE);
                assert locResult != null;
                
                synchronized (TagLibParseSupport.this.parseResultLock) {
                    parseResultRef = new SoftReference<>(locResult);
                    if (locResult.isParsingSuccess()) {
                        parseResultSuccessfulRef = new SoftReference<>(locResult);
                        //hold a reference to the parsing data until last editor pane is closed
                        //motivation: the editor doesn't always hold a strogref to this object
                        //so the SoftRef is sometime cleaned even if there is an editor pane opened.
//                        parseResultSuccessfulRefStrongReference = locResult;
                        //set icon withouth errors
                        if (hasError){
                            //remove all errors
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    annotations.annotate(new ErrorInfo[] {});
                                }
                            });
                            hasError = false;
                        }
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                List<ErrorInfo> errors = new ArrayList<>(locResult.getErrors().length);
                                for (int i = 0; i < locResult.getErrors().length; i ++){
                                    JspParserAPI.ErrorDescriptor err = locResult.getErrors()[i];
                                    if (err != null && checkError(err)) {
                                        errors.add(new ErrorInfo(translate(err.getErrorMessage()),
                                                err.getLine(),
                                                err.getColumn(),
                                                ErrorInfo.JSP_ERROR));
                                    }
                                }
                                annotations.annotate(errors.toArray(new ErrorInfo[]{}));
                                
                                // set icon with error.
                                if (!hasError && !errors.isEmpty()){
                                    hasError = true;
                                }
                                
                            }
                        });
                    }
                    PageInfo pageInfo = locResult.getPageInfo();
                    
                    // if failure do nothing
                    parsingTask = null;
                    
                    if (pageInfo == null) return;
                    Map<String, String> prefixMapper = null;
                    if (pageInfo.getXMLPrefixMapper().size() > 0) {
                        prefixMapper = pageInfo.getApproxXmlPrefixMapper();
                        if (prefixMapper.size() == 0){
                            prefixMapper = pageInfo.getXMLPrefixMapper();
                        }
                        prefixMapper.putAll(pageInfo.getJspPrefixMapper());
                    }
                    else {
                        prefixMapper = pageInfo.getJspPrefixMapper();
                    }
                    getJSPColoringData(false).applyParsedData(pageInfo.getTagLibraries(), prefixMapper, 
                              pageInfo.isELIgnored(), getCachedOpenInfo(false, false).isXmlSyntax(), 
                              locResult.isParsingSuccess());
                }
            }
            
        }
        
        private boolean checkError(JspParserAPI.ErrorDescriptor err) {
            if(err.getErrorMessage() == null) {
                Logger.getGlobal().log(Level.INFO, null, 
                        new IllegalStateException("Invalid JspParserAPI.ErrorDescription from jsp parser - null error message: " + err.toString()));
                return false;
            }
            return true;
        }
        
        private String translate (String text){
            return text.replace("&lt;", "<").replace("&gt;", ">");
        }

    }
}
