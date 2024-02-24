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

package org.netbeans.modules.web.core.syntax;

import java.io.File;
import java.util.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.servlet.jsp.tagext.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.web.core.syntax.completion.api.JspCompletionItem;
import org.netbeans.modules.web.core.syntax.deprecated.ELTokenContext;
import org.netbeans.modules.web.core.syntax.deprecated.JspDirectiveTokenContext;
import org.netbeans.modules.web.core.syntax.deprecated.JspMultiTokenContext;
import org.netbeans.modules.web.core.syntax.deprecated.JspTagTokenContext;
import org.netbeans.modules.web.core.api.JspContextInfo;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.JspOpenInfo;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Petr Jiricka, Petr Nejedly
 * @author Marek.Fukala@Sun.COM
 */
public class JspSyntaxSupport extends ExtSyntaxSupport implements FileChangeListener {

    /** ErrorManager shared by whole module (package) for logging */
    static final Logger err =
            Logger.getLogger("org.netbeans.modules.web.jspsyntax"); // NOI18N

    /* Constants for various contexts in the text from the point of
    view of JSP completion.*/

    /** Completion context for JSP tags (standard or custom) */
    public static final int TAG_COMPLETION_CONTEXT = 1;
    /** Completion context for JSP end tags (standard or custom) */
    public static final int ENDTAG_COMPLETION_CONTEXT = 2;
    /** Completion context for JSP directives */
    public static final int DIRECTIVE_COMPLETION_CONTEXT = 3;
    /** Completion context for JSP comments */
    public static final int COMMENT_COMPLETION_CONTEXT = 4;
    /** Completion context for other JSP text - such as body of custom tags
     * with TAG_DEPENDENT body content. */
    public static final int TEXT_COMPLETION_CONTEXT = 5;
    /** Completion context for the content language */
    public static final int CONTENTL_COMPLETION_CONTEXT = 6;
    /** Completion context for the scripting language */
    public static final int SCRIPTINGL_COMPLETION_CONTEXT = 7;
    /** Completion context for error */
    public static final int ERROR_COMPLETION_CONTEXT = 8;
    /** Completion context for expression language */
    public static final int EL_COMPLETION_CONTEXT = 9;



    private static final String STANDARD_JSP_PREFIX = "jsp";    // NOI18N
    /** Data for completion: TreeMap for standard JSP tags
     * (tag name, array of attributes). */
    private static TagInfo[] standardJspTagDatas;

    private static TagInfo[] standardTagTagDatas;
    /** Data for completion, when the jsp page is in XML syntax
     **/
    private static TagInfo[] xmlJspTagDatas;

    /** Data for completion, when the tag file is in XML syntax
     **/
    private static TagInfo[] xmlTagFileTagDatas;

    /** Data for completion: TreeMap for JSP directives
     * (directive name, array of attributes). */
    private static TagInfo[] directiveJspData;
    private static TagInfo[] directiveTagFileData;

    /** Mapping the URI of tag library -> URL where the help files are.
     */
    private static HashMap helpMap = null;

    private static final TokenID[] JSP_BRACKET_SKIP_TOKENS = new TokenID[] {
        JavaTokenContext.LINE_COMMENT,
        JavaTokenContext.BLOCK_COMMENT,
        JavaTokenContext.CHAR_LITERAL,
        JavaTokenContext.STRING_LITERAL,
        JspTagTokenContext.ATTR_VALUE,
        JspTagTokenContext.COMMENT
    };

    private FileObject fobj;

    /** Special bracket finder is used when caret is in JSP context */
    private boolean useCustomBracketFinder = true;

    /** Creates new JspSyntaxSupport */
    public static synchronized JspSyntaxSupport get(Document doc) {
        JspSyntaxSupport sup = (JspSyntaxSupport)doc.getProperty(JspSyntaxSupport.class);
        if(sup == null) {
            sup = new JspSyntaxSupport((BaseDocument)doc);
            doc.putProperty(JspSyntaxSupport.class, sup);
            }
        return sup;
    }

    public JspSyntaxSupport(BaseDocument doc) {
        super(doc);
        fobj = null;
        if (doc != null){
            initFileObject();
        }

    }

    private void initFileObject() {
        DataObject dobj = NbEditorUtilities.getDataObject(getDocument());
            if(dobj != null)  {
                fobj = NbEditorUtilities.getDataObject(getDocument()).getPrimaryFile();
                fobj.addFileChangeListener(WeakListeners.create(FileChangeListener.class, this, fobj));
            }
    }

    public boolean isXmlSyntax(){
        JspContextInfo jspCO = JspContextInfo.getContextInfo(fobj);
        if(jspCO == null) {
            return false;
        }
        JspOpenInfo jspOpenInfo = jspCO.getCachedOpenInfo(fobj, false);
        if(jspOpenInfo == null) {
            return false;
        }
        return jspOpenInfo.isXmlSyntax();
    }

    protected JspParserAPI.ParseResult getParseResult() {
        JspParserAPI.ParseResult result = JspUtils.getCachedParseResult(fobj, true, false);
        if (result == null) {
            result = JspUtils.getCachedParseResult(fobj, false, false);
        }
        return result;
    }

    /** Returns a map of prefix -> URI that maps tag libraries on prefixes.
     * For the XML syntax this mapping may only be approximate.
     */
    protected Map getPrefixMapper() {
        // PENDING - must also take xmlPrefixMapper into account
        JspParserAPI.ParseResult result = getParseResult();
        Map prefixMapper = null;
        if (result != null && result.getPageInfo() != null) {
            //if (result.isParsingSuccess()) {
            // PENDING - can we somehow get incomplete parsed information ?
            if (!result.getPageInfo().getXMLPrefixMapper().isEmpty()) {
                prefixMapper = result.getPageInfo().getApproxXmlPrefixMapper();
                if (prefixMapper.isEmpty()){
                    prefixMapper = result.getPageInfo().getXMLPrefixMapper();
                }
                prefixMapper.putAll(result.getPageInfo().getJspPrefixMapper());

            } else {
                prefixMapper = result.getPageInfo().getJspPrefixMapper();
            }
            //}
        }
        return prefixMapper;
    }

    private Map getTagLibraries(boolean requiresFresh) {
        //refresh tag libraries mappings - this call causes the WebAppParseSupport to refresh taglibs mapping
        getTagLibraryMappings();
        //if requiresFresh force the parser to update the parse information for the file
        JspParserAPI.ParseResult result = JspUtils.getCachedParseResult(fobj, false, requiresFresh, requiresFresh);
        if (result != null) {
            PageInfo pi = result.getPageInfo();
            if(pi == null) {
                //report error but do not break the entire CC
                err.log(Level.INFO, null, new NullPointerException("PageInfo obtained from JspParserAPI.ParseResult is null"));
                return null;
            } else {
                return pi.getTagLibraries();
            }
        }

        return null; //an error
    }

    private TagInfo[] getSortedTagInfos(TagInfo[] tinfos) {
        Arrays.sort(tinfos, (TagInfo ti1, TagInfo ti2) -> {
            String tname1 = (ti1.getDisplayName() == null ? ti1.getTagName() : ti1.getDisplayName());
            String tname2 = (ti2.getDisplayName() == null ? ti2.getTagName() : ti2.getDisplayName());
            if(tname1 == null || tname2 == null) return 0;
            return tname1.compareTo(tname2);
        });
        return tinfos;
    }

    private TagLibraryInfo getTagLibrary(String prefix, boolean requiresFresh) {
        Map mapper = getPrefixMapper();
        if (mapper != null) {
            Object uri = mapper.get(prefix);
            if (uri != null) {
                Map taglibs = getTagLibraries(requiresFresh);
                if (taglibs != null) {
                    return (TagLibraryInfo)taglibs.get(uri);
                }
            }
        }
        return null;
    }

    @Override
    protected SyntaxSupport createSyntaxSupport(Class syntaxSupportClass) {
        SyntaxSupport support = super.createSyntaxSupport(syntaxSupportClass);
        if (support != null)
            return support;

        EditorKit kit;
        // try the content language support
        kit = CloneableEditorSupport.getEditorKit("text/html"); //NOI18N
        if (kit instanceof BaseKit) {
            support = ((BaseKit)kit).createSyntaxSupport(getDocument());
            if (support != null)
                return support;
        }
        // try the scripting language support
        kit = CloneableEditorSupport.getEditorKit("text/x-java"); //NOI18N
        if (kit instanceof BaseKit) {
            support = ((BaseKit)kit).createSyntaxSupport(getDocument());
            if (support != null)
                return support;
        }
        return null;
    }

    /** This method decides what kind of completion (html, java, jsp-tag, ...) should be opened
     * or whether the completion window should be closed if it is opened.
     */
    @Override
    public int checkCompletion(JTextComponent target, String typedText, boolean visible ) {

        char first = typedText.charAt(0); //get typed char

        TokenItem item; //get token on the cursor
        try{
            item = getItemAtOrBefore(target.getCaret().getDot());
        }catch(BadLocationException e) {
            return COMPLETION_HIDE;
        }
        if (item == null) return COMPLETION_HIDE;

        TokenContextPath tcp = item.getTokenContextPath();

        if(tcp.contains(JavaTokenContext.contextPath)) {
            return COMPLETION_CANCEL; //the JavaCompletionProvider handles this
        }

        //JSP tag or directive
        if(tcp.contains(JspTagTokenContext.contextPath)) {
            //need to distinguish tag/end_tag/directive - search back throught the token chain for <%@ , </ or < tokens
            TokenItem tracking = item;

            //the maxBacktrace number says how far back we are willing to look for
            //the start tag tokens <%, </ and < (simply the JSP tag or directive beginning).
            //There may happen a situation when user starts to write a tag that the a large
            //part of the document behind the cursor is recognized as a jsp tag.
            //In such a case typing somewhere in the end of this 'badly' recognized block
            //would slow down the typing rapidy due to the backtracking oven a long token chain.
            //
            //So we suppose that tag has less than 20 attributes (and one attribute-value pair consumes 4 tokens)
            int maxBacktrace = 20 * 4;

            do {
                //test whether the token is not an error token
                if(tracking.getTokenID() == JspTagTokenContext.ERROR) return COMPLETION_HIDE;

                String image = tracking.getImage();
                //System.out.println("tracking: " + tracking);

                //this is a case when use types %> which is recognized as a JspTagToken, but before that there are java tokens
                if(image.equals("%>")) return COMPLETION_HIDE;

                if(tracking.getImage().startsWith("<%")) {
                    //we are in a directive
                    if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "DIRECTIVE_COMPLETION_CONTEXT");   // NOI18N

                    //open completion also in such a case: <%=|
                    if( !visible && first == '=' && tracking.getImage().equals("<%")) return COMPLETION_POPUP;

                    if( !visible && first == '%' || first == '@' || first == ' ' ) return COMPLETION_POPUP;
                    if( visible && first == '=' || first == '>' ) return COMPLETION_HIDE;
                    return visible ? COMPLETION_POST_REFRESH : COMPLETION_CANCEL;
                }
                if(tracking.getImage().equals("<")) {
                    //we are in a tag
                    if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "TAG_COMPLETION_CONTEXT");   // NOI18N
                    if( !visible && first == ' ' || first == ':' ) return COMPLETION_POPUP;
                    if( visible && first == '>' ) return COMPLETION_HIDE;
                    return visible ? COMPLETION_POST_REFRESH : COMPLETION_CANCEL;
                }
                if(tracking.getImage().equals("</")) {
                    //we are in an end tag
                    if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "ENDTAG_COMPLETION_CONTEXT");   // NOI18N
                    if( visible && first == '>' ) return COMPLETION_HIDE;
                    return visible ? COMPLETION_POST_REFRESH : COMPLETION_CANCEL;
                }
                //test whether we are still in the tag context
                if(!tracking.getTokenContextPath().contains(JspTagTokenContext.contextPath)) {
                    if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "We are out of jsp tag without finding any tag start token!");
                    break;
                }

                if(tracking.getImage().equals(">")) {
                    try {
                        //check if the cursor is behind an open tag
                        SyntaxElement se = getElementChain(tracking.getOffset());
                        if(se instanceof SyntaxElement.Tag) {
                            return COMPLETION_POPUP;
                        }
                    }catch(BadLocationException e) {
                        //do nothing
                    }
                    return COMPLETION_HIDE;
                }

                //search previous token
                tracking = tracking.getPrevious();

            } while((maxBacktrace-- > 0) && (tracking != null));

        }//eof JSP tag

        if(tcp.contains(ELTokenContext.contextPath)) {
            if(first == '.') {
                return COMPLETION_POPUP;
            }
        }

        return COMPLETION_HIDE;
    }

    /** Returns offset where the next offset after this offset starts. */
    private int getTokenEnd( TokenItem item ) {
        if (item == null)
            return 0; //getDocument().getLength();
        return item.getOffset() + item.getImage().length();
    }

    /** Filters list of strings so only strings starting
     * with a given prefix are returned in the new List. */
    public static final List<Object> filterList(List<? extends Object> toFilter, String prefix) {
        List<Object> newList = new ArrayList<>();
        Object item;
        for (int i = 0; i < toFilter.size(); i++) {
            item = toFilter.get(i);
            String txt;
            if (item instanceof TagInfo)
                txt = ((TagInfo)item).getTagName();
            else if (item instanceof TagAttributeInfo)
                txt = ((TagAttributeInfo)item).getName();
            else
                txt = (String)item;

            if (txt != null && txt.startsWith(prefix)) {
                newList.add(item);
            }
        }
        return newList;
    }

     /** Filters list of strings so only strings starting
     * with a given prefix are returned in the new List. */
    public static final List<String> filterStrings(List<String> toFilter, String prefix) {
        List<String> newList = new ArrayList<>();
        for(String val : toFilter) {
            if(val.startsWith(prefix)) {
                newList.add(val);
            }
        }
        return newList;
    }

    /** Gets all 'jsp prefixes' whose 'string prefix' matches complPrefix as a list of Strings. */
    public final List<Object> getTagPrefixes(String complPrefix) {
        return filterList(getAllTagPrefixes(), complPrefix);
    }

    /** Gets all tags whose 'string prefix' matches complPrefix as a list of Strings.
     * Assumes that complPrefix also includes the 'jsp prefix'.
     */
    public final List getTags(String complPrefix) {
        int colonIndex = complPrefix.indexOf(":");  // NOI18N
        if (colonIndex == -1)
            throw new IllegalArgumentException();
        return getTags(complPrefix.substring(0, colonIndex),
                complPrefix.substring(colonIndex + 1));
    }

    /** Gets all tags whose 'string prefix' matches complPrefix and whose 'jsp prefix'
     * is tagPrefix as a list of Strings.
     * Assumes that complPrefix does not include the 'jsp prefix'.
     */
    public final List getTags(String tagPrefix, String complPrefix) {
        return filterList(getAllTags(tagPrefix, true), complPrefix);
    }

    /** Gets attributes for tag whose prefix + name
     * is tagPrefixName as a list of Strings.
     * The attribute's 'string prefix' must match complPrefix.
     */
    public final List getTagAttributes(String tagPrefixName, String complPrefix) {
        int colonIndex = tagPrefixName.indexOf(":");    // NOI18N
        if (colonIndex == -1)
            throw new IllegalArgumentException();
        return getTagAttributes(tagPrefixName.substring(0, colonIndex),
                tagPrefixName.substring(colonIndex + 1), complPrefix);
    }

    /** Gets attributes for tag whose 'jsp prefix'
     * is tagPrefix and whose tag name is tagName as a list of Strings.
     * The attribute's 'string prefix' must match complPrefix.
     */
    protected final List getTagAttributes(String tagPrefix, String tagName, String complPrefix) {
        return filterList(getAllTagAttributes(tagPrefix, tagName), complPrefix);
    }

    /** Gets all directives whose 'string prefix' matches complPrefix as a list of Strings. */
    public final List getDirectives(String complPrefix) {
        return filterList(getAllDirectives(), complPrefix);
    }

    /** Gets attributes for directive <code>directive</code> as a list of Strings.
     * The attribute's 'string prefix' must match complPrefix.  */
    public final List getDirectiveAttributes(String directive, String complPrefix) {
        return filterList(getAllDirectiveAttributes(directive), complPrefix);
    }

    /**
     *  Returns a list of strings - prefixes available in this support context (JSP file).
     */
    protected List getAllTagPrefixes() {
        List items = new ArrayList();

        // jsp: prefix
        items.add(STANDARD_JSP_PREFIX);

        Map mapper = getPrefixMapper();
        if (mapper != null) {
            // sort it
            TreeSet ts = new TreeSet();
            ts.addAll(mapper.keySet());
            ts.remove("jsp"); // remove jsp prefix when is declared in xml syntax
            items.addAll(ts);
        }
        // prefixes for tag libraries
/*        TagLibParseSupport support = (dobj == null) ?
            null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        if (support != null) {
            // add all prefixes from the support
            TagLibParseSupport.TagLibData[] tagLibData = support.getTagLibEditorData().getTagLibData();
            for (int i = 0; i < tagLibData.length; i++)
                items.add(tagLibData[i].getPrefix());
        }
 */
        return items;
    }

    /** This method merges tagInfos of tags and file tags from a tagLibrary.
     * @param tagLibrary
     * @return array of TagInfo of all tags and tag files from the library
     * or an empty array of if there is not any tag or tagfile defined in the library.
     */
    private TagInfo[] getAllTagInfos(TagLibraryInfo tagLibrary) {

        // if there is not any tag or file tag, then the empty array is returned
        TagInfo[] allTags = new TagInfo[0];

        if (tagLibrary != null) {
            int tagInfosLength = 0;
            int tagAllInfosLength = 0;
            TagInfo[] tagInfos = tagLibrary.getTags();
            TagFileInfo[] tagFileInfos = tagLibrary.getTagFiles();

            if (tagInfos != null) { // it can be null, when the jsp parser finished unexpectedly
                tagInfosLength = tagInfos.length;
                tagAllInfosLength = tagInfosLength;
            }
            if (tagFileInfos != null) { // it can be null, when the jsp parser finished unexpectedly
                tagAllInfosLength += tagFileInfos.length;
            }

            allTags = new TagInfo[tagAllInfosLength];

            if (tagInfos != null) {
                System.arraycopy(tagInfos, 0, allTags, 0, tagInfosLength);
            }
            if (tagFileInfos != null) {
                for (int i = 0; i < tagFileInfos.length; i++) {
                    allTags[tagInfosLength + i] = tagFileInfos[i].getTagInfo();
                }
            }
        }
        return allTags;
    }

    /**  Returns a list of strings - tag names available for a particular prefix.
     */
    protected List getAllTags(String prefix, boolean requiresFresh) {
        List items = new ArrayList();

        // standard JSP tags (jsp:)
        initCompletionData();
        if (STANDARD_JSP_PREFIX.equals(prefix)) {
            TagInfo[] stanTagDatas = getTagInfos();
            items.addAll(Arrays.asList(stanTagDatas));
        }

        TagLibraryInfo info = getTagLibrary(prefix, requiresFresh);
        TagInfo[] tags = null;
        if (info != null) {
            tags = getAllTagInfos(info);
        }
        if (tags != null) {
            assert info != null;
            tags = getSortedTagInfos(tags);
            String url = (String)helpMap.get(info.getURI());
            if (url != null && !url.equals("")){
                for (int i = 0; i < tags.length; i++) {
                    items.add(new TagInfo(tags[i].getTagName(),
                            tags[i].getTagClassName(), tags[i].getBodyContent(),
                            url + tags[i].getTagName() + ".html#tag-start-" + tags[i].getTagName()
                            + "#tag-end-" + tags[i].getTagName(), info,
                            tags[i].getTagExtraInfo(), tags[i].getAttributes(),
                            tags[i].getDisplayName(), tags[i].getSmallIcon(), tags[i].getLargeIcon(),
                            tags[i].getTagVariableInfos(), tags[i].hasDynamicAttributes()));
                }
            } else {
                items.addAll(Arrays.asList(tags));
            }
        }
        return items;
    }

    /** Should be overriden ny subclasses to support JSP 1.1.
     *  Returns a list of strings - attribute names available for a particular prefix and tag name.
     */
    protected List getAllTagAttributes(String prefix, String tag) {
        List items = new ArrayList();

        // attributes for standard JSP tags (jsp:)
        initCompletionData();
        if (STANDARD_JSP_PREFIX.equals(prefix)) {
            TagInfo[] stanTagDatas = getTagInfos();
            for (int i=0; i<stanTagDatas.length; i++) {
                if (stanTagDatas[i].getTagName().equals(tag)) {
                    TagAttributeInfo[] attrs = stanTagDatas[i].getAttributes();
                    items.addAll(Arrays.asList(attrs));
                    break;
                }
            }
        }

        TagLibraryInfo info = getTagLibrary(prefix, true);
        if (info != null) {
            TagInfo tagInfo = info.getTag(tag);
            if (tagInfo == null) {
                TagFileInfo tagFile = info.getTagFile(tag);
                if (tagFile != null) {
                    tagInfo = tagFile.getTagInfo();
                }
            }
            if (tagInfo != null) {
                TagAttributeInfo[] attributes = tagInfo.getAttributes();
                String url = (String)helpMap.get(tagInfo.getTagLibrary().getURI());
                if (url != null && !url.equals("")){
                    for (int i = 0; i < attributes.length; i++) {
                        items.add(new TagAttributeInfo(attributes[i].getName(),
                                attributes[i].isRequired(),
                                url + tagInfo.getTagName() + ".html#attribute-start-" + attributes[i].getName()
                                + "#attribute-end-" + attributes[i].getName(),
                                attributes[i].canBeRequestTime(),
                                attributes[i].isFragment()));
                    }
                } else {
                    items.addAll(Arrays.asList(attributes));
                }
            }
        }
        return items;
    }



    /** Should be overriden ny subclasses to support JSP 1.1. */
    protected List<TagInfo> getAllDirectives() {
        initCompletionData();
        List<TagInfo> items = new ArrayList<>();

        //Is xml syntax? => return nothing.
        if (isXmlSyntax()) return items;

        TagInfo[] directiveData;
        if(NbEditorUtilities.getMimeType(getDocument()).equals(JspUtils.TAG_MIME_TYPE))
            directiveData = directiveTagFileData;
        else {
            directiveData = directiveJspData;

        }
        items.addAll(Arrays.asList(directiveData));
        return items;
    }

    /** Should be overriden ny subclasses to support JSP 1.1. */
    protected List getAllDirectiveAttributes(String directive) {
        initCompletionData();
        List items = new ArrayList();
        //Is xml syntax? => return nothing.
        if (isXmlSyntax()) return items;

        TagInfo[] directiveData;
        if(NbEditorUtilities.getMimeType(getDocument()).equals(JspUtils.TAG_MIME_TYPE))
            directiveData = directiveTagFileData;
        else
            directiveData = directiveJspData;
        for (int i=0; i<directiveData.length; i++) {
            if (directiveData[i].getTagName().equals(directive)) {
                TagAttributeInfo[] attrs = directiveData[i].getAttributes();
                items.addAll(Arrays.asList(attrs));
                break;
            }
        }
        return items;
    }

    public PageInfo.BeanData[] getBeanData() {
        JspParserAPI.ParseResult result = getParseResult();
        if (result != null) {
            PageInfo pageInfo = result.getPageInfo();
            //pageInfo can be null in some cases when the parser cannot parse
            //the webmodule or the page itself
            if(pageInfo != null) {
                return pageInfo.getBeans();
            }
        }
        /*TagLibParseSupport support = (dobj == null) ?
            null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        return support.getTagLibEditorData().getBeanData();*/
        return null;
    }

    public boolean isErrorPage() {
        JspParserAPI.ParseResult result = getParseResult();
        if (result != null) {
            if (result.getPageInfo() != null)
                return result.getPageInfo().isErrorPage();
        }
        /*TagLibParseSupport support = (dobj == null) ?
            null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        return support.getTagLibEditorData().isErrorPage ();*/
        return false;
    }


    /**
     * The mapping of the 'global' tag library URI to the location
     * (resource path) of the TLD associated with that tag library.
     * The location is returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location
     *        of the tld.
     */
    public Map getTagLibraryMappings() {
        if (fobj == null) {
            return null;
        }
        return JspUtils.getTaglibMap(fobj);
    }

    private static void initHelp(){
        if (helpMap == null){
            String url="";
            File f = InstalledFileLocator.getDefault().locate("docs/jstl11-doc.zip", "org.netbeans.modules.web.core.syntax", false); //NoI18N
            if (f != null){
                try {
                    URL urll = f.toURL();
                    urll = FileUtil.getArchiveRoot(urll);
                    url = urll.toString();
                } catch (java.net.MalformedURLException e){
                    err.log(Level.WARNING, null, e);
                    // nothing to do
                }
            }
            helpMap = new HashMap();
            //           The URI from the tld file          URL, where are the files for the library
            helpMap.put("http://java.sun.com/jsp/jstl/core", url + "c/");
            helpMap.put("http://java.sun.com/jstl/core", url + "c/");
            helpMap.put("http://java.sun.com/jstl/core_rt", url + "c_rt/");
            helpMap.put("http://java.sun.com/jsp/jstl/fmt", url + "fmt/");
            helpMap.put("http://java.sun.com/jstl/fmt", url + "fmt/");
            helpMap.put("http://java.sun.com/jstl/fmt_rt", url + "fmt_rt/");
            helpMap.put("http://java.sun.com/jsp/jstl/functions", url + "fn/");
            helpMap.put("http://java.sun.com/jstl/functions", url + "fn/");
            helpMap.put("http://jakarta.apache.org/taglibs/standard/permittedTaglibs", url+"permittedTaglibs/");
            helpMap.put("http://jakarta.apache.org/taglibs/standard/scriptfree", url+ "scriptfree/");
            helpMap.put("http://java.sun.com/jsp/jstl/sql", url + "sql/");
            helpMap.put("http://java.sun.com/jstl/sql", url + "sql/");
            helpMap.put("http://java.sun.com/jstl/sql_rt", url + "sql_rt/");
            helpMap.put("http://java.sun.com/jsp/jstl/xml", url + "x/");
            helpMap.put("http://java.sun.com/jstl/xml", url + "x/");
            helpMap.put("http://java.sun.com/jstl/xml_rt", url + "x_rt/");
            f = InstalledFileLocator.getDefault().locate("docs/jsf12-tlddoc.zip", "org.netbeans.modules.web.core.syntax", false); //NoI18N
            if (f != null){
                try {
                    URL urll = f.toURL();
                    urll = FileUtil.getArchiveRoot(urll);
                    url = urll.toString();
                    helpMap.put("http://java.sun.com/jsf/html", url + "h/");
                    helpMap.put("http://java.sun.com/jsf/core", url + "f/");
                } catch (java.net.MalformedURLException e){
                    err.log(Level.WARNING, null, e);
                    // nothing to do
                }
            }
            f = InstalledFileLocator.getDefault().locate("docs/struts-tags.zip", "org.netbeans.modules.web.core.syntax", false);
            if (f != null){
                try {
                    URL urll = f.toURL();
                    urll = FileUtil.getArchiveRoot(urll);
                    url = urll.toString();
                    helpMap.put("http://jakarta.apache.org/struts/tags-bean", url + "bean/");
                    helpMap.put("http://struts.apache.org/tags-bean", url + "bean/");
                    helpMap.put("/WEB-INF/struts-bean.tld", url + "bean/");
                    helpMap.put("http://jakarta.apache.org/struts/tags-html", url + "html/");
                    helpMap.put("http://struts.apache.org/tags-html", url + "html/");
                    helpMap.put("/WEB-INF/struts-html.tld", url + "html/");
                    helpMap.put("http://jakarta.apache.org/struts/tags-logic", url + "logic/");
                    helpMap.put("http://struts.apache.org/tags-logic", url + "logic/");
                    helpMap.put("/WEB-INF/struts-logic.tld", url + "logic/");
                    helpMap.put("http://jakarta.apache.org/struts/tags-nested", url + "nested/");
                    helpMap.put("http://struts.apache.org/tags-nested", url + "nested/");
                    helpMap.put("/WEB-INF/struts-nested.tld", url + "nested/");
                    helpMap.put("http://jakarta.apache.org/struts/tags-tiles", url + "tiles/");
                    helpMap.put("http://struts.apache.org/tags-tiles", url + "tiles/");
                    helpMap.put("/WEB-INF/struts-tiles.tld", url + "tiles/");
                } catch (java.net.MalformedURLException e){
                    err.log(Level.WARNING, null, e);
                    // nothing to do
                }
            }

        }

    }

    private static void initCompletionData() {
        if (helpMap == null)
            initHelp();
        String url = "";           // NOI18N

         if (directiveJspData == null){
            directiveJspData = new TagInfo[] {
                new TagInfo("include", null, TagInfo.BODY_CONTENT_EMPTY, url+"syntaxref209.html#1003408#8975", null, null,            // NOI18N
                        new TagAttributeInfo[] { new TagAttributeInfo("file", true, url + "syntaxref209.html#16836#10636", false)}),           // NOI18N
                        new TagInfo("page", null, TagInfo.BODY_CONTENT_EMPTY, url+"syntaxref2010.html", null, null,            // NOI18N
                        new TagAttributeInfo[] { new TagAttributeInfo("autoFlush", false, url+"syntaxref2010.html#15673#15675", false),           // NOI18N
                        new TagAttributeInfo("buffer", false, url+"syntaxref2010.html#15671#15673", false),           // NOI18N
                        new TagAttributeInfo("contentType", false, url+"syntaxref2010.html#15683#1001361", false),           // NOI18N
                        new TagAttributeInfo("errorPage", false, url+"syntaxref2010.html#15679#15681", false),              // NOI18N
                        new TagAttributeInfo("extends", false, url+"syntaxref2010.html#15665#16862", false),           // NOI18N
                        new TagAttributeInfo("import", false, url+"syntaxref2010.html#16862#15669", false),           // NOI18N
                        new TagAttributeInfo("info", false, url+"syntaxref2010.html#15677#15679", false),           // NOI18N
                        new TagAttributeInfo("isELIgnored", false, url+"syntaxref2010.html#1011216#18865", false), // NOI18N
                        new TagAttributeInfo("isErrorPage", false, url+"syntaxref2010.html#15681#15683", false), // NOI18N
                        new TagAttributeInfo("isThreadSafe", false, url+"syntaxref2010.html#15675#15677", false), // NOI18N
                        new TagAttributeInfo("language", false, url+"syntaxref2010.html#15663#15665", false), // NOI18N
                        new TagAttributeInfo("pageEncoding", false, url+"syntaxref2010.html#1001361#1011216", false), // NOI18N
                        new TagAttributeInfo("session", false, url+"syntaxref2010.html#15669#15671", false)}), // NOI18N
                        new TagInfo("taglib", null, TagInfo.BODY_CONTENT_EMPTY, url+"syntaxref2012.html#1003416#1002041", null, null,  // NOI18N
                        new TagAttributeInfo[] { new TagAttributeInfo("prefix", true, url+"syntaxref2012.html#1011290#1002041", false), // NOI18N
                        new TagAttributeInfo("uri", false, url+"syntaxref2012.html#10721#1011294", false), // NOI18N
                        new TagAttributeInfo("tagdir", false, url + "syntaxref2012.html#1011294#1011290", false)}) // NOI18N
            };
        }

         if (directiveTagFileData == null){
            directiveTagFileData = new TagInfo[]{
                new TagInfo("attribute", null, TagInfo.BODY_CONTENT_EMPTY, url + "syntaxref208.html", null, null,                        // NOI18N
                        new TagAttributeInfo[] { new TagAttributeInfo("description", false, url + "syntaxref208.html#1004672", false),                      // NOI18N
                        new TagAttributeInfo("fragment", false, url + "syntaxref208.html#1004657#1004666", false),                         // NOI18N
                        new TagAttributeInfo("name", true, url + "syntaxref208.html#1004648#1004655", false),                              // NOI18N
                        new TagAttributeInfo("required", false, url + "syntaxref208.html#1004655#1004657", false),                             // NOI18N
                        new TagAttributeInfo("rtexprvalue", false, url + "syntaxref208.html#1004666#1004669", false),                          // NOI18N
                        new TagAttributeInfo("type", false, url + "syntaxref208.html#1004669#1004672", false)}),                                   // NOI18N
                        directiveJspData[0],
                        new TagInfo("tag", null, TagInfo.BODY_CONTENT_EMPTY, url + "syntaxref2011.html", null, null,                              // NOI18N
                        new TagAttributeInfo[] { new TagAttributeInfo("body-content", false, url + "syntaxref2011.html#1005164#005172", false),                 // NOI18N
                        new TagAttributeInfo("description", false, url + "syntaxref2011.html#1005196#1005198", false),                  // NOI18N
                        new TagAttributeInfo("display-name", false, url + "syntaxref2011.html#1005161#1005164", false),                 // NOI18N
                        new TagAttributeInfo("dynamic-attributes", false, url + "syntaxref2011.html#005172#1005190", false),           // NOI18N
                        new TagAttributeInfo("example", false, url + "syntaxref2011.html#1005198#1005201", false),                      // NOI18N
                        new TagAttributeInfo("import", false, url + "syntaxref2011.html#1005203#1005209", false),                       // NOI18N
                        new TagAttributeInfo("isELIgnored", false, url + "syntaxref2011.html#1005214#1005291#1005291", false),                  // NOI18N
                        //new TagAttributeInfo("isScriptingEnabled", false, url + "syntaxref2011.html#", false),            // NOI18N
                        new TagAttributeInfo("large-icon", false, url + "syntaxref2011.html#1005193#1005196", false),                   // NOI18N
                        new TagAttributeInfo("language", false, url + "syntaxref2011.html#1005201#1005203", false),                     // NOI18N
                        new TagAttributeInfo("pageEncoding", false, url + "syntaxref2011.html#1005209#1005214", false),                  // NOI18N
                        new TagAttributeInfo("small-icon", false, url + "syntaxref2011.html#1005190#1005193", false)}),                     // NOI18N
                        directiveJspData[2],
                        new TagInfo("variable", null, TagInfo.BODY_CONTENT_EMPTY, url + "syntaxref2013.html#15694#1003563", null, null,                             // NOI18N
                        new TagAttributeInfo[] { new TagAttributeInfo("alias", false, url + "syntaxref2013.html#1005914#1005956", false),                          // NOI18N
                        new TagAttributeInfo("declare", false, url + "syntaxref2013.html#1006001#1006019", false),                          // NOI18N
                        new TagAttributeInfo("description", false, url + "syntaxref2013.html#1005991#1003563", false),                      // NOI18N
                        new TagAttributeInfo("name-given", false, url + "syntaxref2013.html#1003561#1005914", false),                       // NOI18N
                        new TagAttributeInfo("scope", false, url + "syntaxref2013.html#1006019#1005991", false),                            // NOI18N
                        new TagAttributeInfo("variable-class", false, url + "syntaxref2013.html#1005956#1006001", false)})                  // NOI18N
            };
        }

        if (standardJspTagDatas == null) {
            final String helpFiles = "docs/syntaxref20.zip"; //NoI18N
            File f = InstalledFileLocator.getDefault().locate(helpFiles, "org.netbeans.modules.web.core.syntax", true); //NoI18N
            if (f != null){
                try {
                    URL urll = f.toURL();
                    urll = FileUtil.getArchiveRoot(urll);
                    url = urll.toString();
                } catch (java.net.MalformedURLException e){
                    err.log(Level.WARNING, null, e);
                    // nothing to do
                }
            }
            standardJspTagDatas = new TagInfo[] {
                new TagInfo("attribute", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2014.html",             // NOI18N
                        null, null, new TagAttributeInfo[] { new TagAttributeInfo("name", true, url + "syntaxref2014.html#1003581#1006483", false),           // NOI18N
                        new TagAttributeInfo("trim", false, url + "syntaxref2014.html#1006483#1003583", false)}),           // NOI18N
                new TagInfo("body", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2015.html#1006731#1003768",             // NOI18N
                        null, null, new TagAttributeInfo[]{}),
                new TagInfo("element", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2016.html#1003696#1003708",             // NOI18N
                        null, null, new TagAttributeInfo[] { new TagAttributeInfo("name", true, url + "syntaxref2016.html#1003706#1003708", false)}),           // NOI18N
                new TagInfo("expression", null, TagInfo.BODY_CONTENT_JSP, url+"syntaxref205.html#1004353#11268",                 // NOI18N
                        null, null, new TagAttributeInfo[] {}),
                new TagInfo("fallback", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2023.html#11583#19029",    // NOI18N
                        null, null, new TagAttributeInfo[] {}),
                new TagInfo("forward", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2018.html#1003349#15708", // NOI18N
                        null, null, new TagAttributeInfo[] { new TagAttributeInfo("page", true, url + "syntaxref2018.html#15704#15708", true)}),       // NOI18N
                new TagInfo("getProperty", null, TagInfo.BODY_CONTENT_EMPTY, url + "syntaxref2019.html#8820#9201",           // NOI18N
                        null, null, new TagAttributeInfo[] { new TagAttributeInfo("name", true, url + "syntaxref2019.html#15748#10919", false),       // NOI18N
                        new TagAttributeInfo("property", true, url + "syntaxref2019.html#10919#19482", false)}), // NOI18N
                new TagInfo("include", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2020.html#8828#9228",              // NOI18N
                        null, null, new TagAttributeInfo[] { new TagAttributeInfo("flush", true, url + "syntaxref2020.html#17145#18376", false),       // NOI18N
                        new TagAttributeInfo("page", true, url + "syntaxref2020.html#10930#17145", true)}),       // NOI18N
                new TagInfo("param", null, TagInfo.BODY_CONTENT_EMPTY, url + "syntaxref2023.html#11538#11583",                // NOI18N
                        null, null, new TagAttributeInfo[] { new TagAttributeInfo("name", true, url + "syntaxref2023.html#11538#11583", false),        // NOI18N
                        new TagAttributeInfo("value", true, url + "syntaxref2023.html#11538#11583", true)}),      // NOI18N
                new TagInfo("params", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2023.html#11538#11583",                 // NOI18N
                        null, null, new TagAttributeInfo[] {}),
                new TagInfo("plugin", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2023.html#1004158#19029",               // NOI18N
                        null, null, new TagAttributeInfo[] { new TagAttributeInfo("align", false, url + "syntaxref2023.html#11516#11518", false),      // NOI18N
                        new TagAttributeInfo("archive", false, url + "syntaxref2023.html#11553#11516", false),    // NOI18N
                        new TagAttributeInfo("code", true, url + "syntaxref2023.html#11514#11515", false),        // NOI18N
                        new TagAttributeInfo("codebase", true, url + "syntaxref2023.html#11515#11547", false),   // NOI18N
                        new TagAttributeInfo("height", false, url + "syntaxref2023.html#11518#11568", false),     // NOI18N
                        new TagAttributeInfo("hspace", false, url + "syntaxref2023.html#11568#11520", false),     // NOI18N
                        new TagAttributeInfo("iepluginurl", false, url + "syntaxref2023.html#11526#11538", false),// NOI18N
                        new TagAttributeInfo("jreversion", false, url + "syntaxref2023.html#11520#11525", false), // NOI18N
                        new TagAttributeInfo("name", false, url + "syntaxref2023.html#11547#11553", false),       // NOI18N
                        new TagAttributeInfo("nspluginurl", false,url + "syntaxref2023.html#11525#11526", false),// NOI18N
                        new TagAttributeInfo("type", true, url + "syntaxref2023.html#10935#11514", false),        // NOI18N
                        new TagAttributeInfo("vspace", false, url + "syntaxref2023.html#11568#11520", false),     // NOI18N
                        new TagAttributeInfo("width", false, url + "syntaxref2023.html#11518#11568", false)}),    // NOI18N
                new TagInfo("setProperty", null, TagInfo.BODY_CONTENT_EMPTY, url + "syntaxref2025.html#8856#9329",           // NOI18N
                        null, null, new TagAttributeInfo[] { new TagAttributeInfo("name", true, url + "syntaxref2025.html#17612#1001786", true),        // NOI18N
                        new TagAttributeInfo("param", false, url + "syntaxref2025.html#9919#20483", false),      // NOI18N
                        new TagAttributeInfo("property", false, url + "syntaxref2025.html#1001786#9329", false),   // NOI18N
                        new TagAttributeInfo("value", false, url + "syntaxref2025.html#20483#9329", true)}),     // NOI18N
                new TagInfo("text", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2026.html",
                        null, null, new TagAttributeInfo[]{}),
                new TagInfo("useBean", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2027.html#8865#9359",     // NOI18N
                        null, null, new TagAttributeInfo[] { new TagAttributeInfo("beanName", false, url + "syntaxref2027.html#15804#9359", false),   // NOI18N
                        new TagAttributeInfo("class", false, url + "syntaxref2027.html#10968#19433", false),      // NOI18N
                        new TagAttributeInfo("id", true, url + "syntaxref2027.html#10964#10966", false),         // NOI18N
                        new TagAttributeInfo("scope", true, url + "syntaxref2027.html#10966#10968", false),      // NOI18N
                        new TagAttributeInfo("type", false, url + "syntaxref2027.html#19433#18019", false)}),     // NOI18N
                new TagInfo("declaration", null, TagInfo.BODY_CONTENT_JSP, url+"syntaxref204.html#10983#10991",                 // NOI18N
                        null, null, new TagAttributeInfo[] {}),
                new TagInfo("scriptlet", null, TagInfo.BODY_CONTENT_JSP, url+"syntaxref206.html#10996#11007",                 // NOI18N
                        null, null, new TagAttributeInfo[] {}),
                new TagInfo("directive.page", null, TagInfo.BODY_CONTENT_EMPTY, directiveJspData[1].getInfoString(),   // NOI18N
                        null, null, directiveJspData[1].getAttributes()),
                new TagInfo("directive.include", null, TagInfo.BODY_CONTENT_EMPTY, directiveJspData[1].getInfoString(),   // NOI18N
                        null, null, directiveJspData[0].getAttributes())
            };

            List<TagInfo> standardTagTagDatasList = new ArrayList<>();
            standardTagTagDatasList.addAll(Arrays.asList(standardJspTagDatas));

            standardTagTagDatasList.add(new TagInfo("doBody", null, TagInfo.BODY_CONTENT_EMPTY, url + "syntaxref2017.html", null, null,            // NOI18N
                    new TagAttributeInfo[] { new TagAttributeInfo("scope", false, url + "syntaxref2017.html#1006246#syntaxref20.html", false),           // NOI18N
                    new TagAttributeInfo("var", false, url + "syntaxref2017.html#1006234#1006240", false),           // NOI18N
                    new TagAttributeInfo("varReader", false, url + "syntaxref2017.html#1006240#1006246", false)}));           // NOI18N

            standardTagTagDatasList.add(new TagInfo("invoke", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2021.html#8837#1003634", null, null,            // NOI18N
                    new TagAttributeInfo[] { new TagAttributeInfo("fragment", true, url + "syntaxref2021.html#1007359#1007361", false),           // NOI18N
                    new TagAttributeInfo("scope", false, url + "syntaxref2021.html#1007373#1003634", false),           // NOI18N
                    new TagAttributeInfo("var", false, url + "syntaxref2021.html#1007361#1007367", false),           // NOI18N
                    new TagAttributeInfo("varReader", false, url + "syntaxref2021.html#1007367#1007373", false)}));           // NOI18N

            standardTagTagDatasList.add(new TagInfo("directive.tag", null, TagInfo.BODY_CONTENT_EMPTY, directiveTagFileData[2].getInfoString(), // NOI18N
                        null, null, directiveTagFileData[2].getAttributes()));
            standardTagTagDatasList.add(new TagInfo("directive.attribute", null, TagInfo.BODY_CONTENT_EMPTY, directiveTagFileData[0].getInfoString(), // NOI18N
                        null, null, directiveTagFileData[0].getAttributes()));
            standardTagTagDatasList.add(new TagInfo("directive.variable", null, TagInfo.BODY_CONTENT_EMPTY, directiveTagFileData[4].getInfoString(), // NOI18N
                        null, null, directiveTagFileData[4].getAttributes()));

            standardTagTagDatas = standardTagTagDatasList.toArray(new TagInfo[]{});

        }

        if (xmlJspTagDatas == null) {
            TagInfo[] commonXMLTagDatas;
            commonXMLTagDatas = new TagInfo[]{
                        new TagInfo("output", null, TagInfo.BODY_CONTENT_JSP, url + "syntaxref2022.html#1004130#1007521",                 // NOI18N
                        null, null, new TagAttributeInfo[] {new TagAttributeInfo("doctype-public", false, "url + syntaxref2022.html#1007534#1007521", false),  // NOI18N
                        new TagAttributeInfo("doctype-root-element", false, "url + syntaxref2022.html#1007528#1007532", false),    // NOI18N
                        new TagAttributeInfo("doctype-system", false, url + "syntaxref2022.html#1007532#1007534", false),  // NOI18N
                        new TagAttributeInfo("omit-xml-declaration", false, url + "syntaxref2022.html#1007525#1007528"   , false)}),    // NOI18N
                        new TagInfo("root", null, TagInfo.BODY_CONTENT_JSP, url+"syntaxref2024.html#1003283#1003311",                         // NOI18N
                        null, null, new TagAttributeInfo[] {new TagAttributeInfo("version", false, url+"syntaxref2024.html#1003299#1003301", false),
                        new TagAttributeInfo("xmlns:jsp", false, url+"syntaxref2024.html#1003297#1003299", false),
                        new TagAttributeInfo("xmlns:x", false, url+"syntaxref2024.html#1003301#1003311", false)})
            };

            List<TagInfo> xmlJspTagDatasList = new ArrayList<>();
            xmlJspTagDatasList.addAll(Arrays.asList(standardJspTagDatas));
            xmlJspTagDatasList.addAll(Arrays.asList(commonXMLTagDatas));
            xmlJspTagDatas = xmlJspTagDatasList.toArray(new TagInfo[]{});

            List<TagInfo> xmlTagFileTagDatasList = new ArrayList<>();
            xmlTagFileTagDatasList.addAll(Arrays.asList(standardTagTagDatas));
            xmlTagFileTagDatasList.addAll(Arrays.asList(commonXMLTagDatas));
            xmlTagFileTagDatas = xmlTagFileTagDatasList.toArray(new TagInfo[]{});

        }


    }

    private TagInfo[] getTagInfos(){
        TagInfo[] rValue;
        if (fobj != null &&
                ("text/x-jsp".equals(fobj.getMIMEType()) ||
                "text/x-tag".equals(fobj.getMIMEType()))){
            if ( isXmlSyntax()){
                if (NbEditorUtilities.getMimeType(getDocument()).equals(JspUtils.TAG_MIME_TYPE))
                    rValue = xmlTagFileTagDatas;
                else
                    rValue = xmlJspTagDatas;
            } else
                if (NbEditorUtilities.getMimeType(getDocument()).equals(JspUtils.TAG_MIME_TYPE))
                    rValue = standardTagTagDatas;
                else
                    rValue = standardJspTagDatas;
        } else
            rValue = new TagInfo[0];
        return rValue;
    }

    @Override
    public String toString() {
        return printJspCompletionInfo();
    }

    /** Debug output of all tags and directives. */
    private String printJspCompletionInfo() {
        StringBuilder output = new StringBuilder();

        output.append("TAGS\n");    // NOI18N
        List tagPrefixes = getTagPrefixes("");  // NOI18N
        for (int i = 0; i < tagPrefixes.size(); i++) {
            String prefix = (String)tagPrefixes.get(i);
            output.append("  ");    // NOI18N
            output.append(prefix);
            output.append("\n");    // NOI18N
            List tags = getTags(prefix, "");    // NOI18N
            for (int j = 0; j < tags.size(); j++) {
                if (tags.get(j) instanceof TagInfo){
                    TagInfo ti = (TagInfo) tags.get(j);
                    output.append("    "); // NOI18N
                    output.append(ti.getTagName());
                    output.append("\n"); // NOI18N
                    TagAttributeInfo[] attributes =  ti.getAttributes();
                    for (int k = 0; k < attributes.length; k++) {
                        output.append("      "); // NOI18N
                        output.append(attributes[k].getName());
                        output.append("\n");// NOI18N
                    }
                } else {
                    String tagName = (String)tags.get(j);
                    output.append("    "); // NOI18N
                    output.append(tagName);
                    output.append("\n"); // NOI18N
                    List attributes = getTagAttributes(prefix, tagName, "");// NOI18N
                    for (int k = 0; k < attributes.size(); k++) {
                        String attribute = (String)attributes.get(k);
                        output.append("      "); // NOI18N
                        output.append(attribute);
                        output.append("\n"); // NOI18N
                    }
                }
            }

        }

        output.append("DIRECTIVES\n");// NOI18N
        List directives = getDirectives("");// NOI18N
        for (int i = 0; i < directives.size(); i++) {
            if (directives.get(i) instanceof TagInfo){
                TagInfo ti = (TagInfo) directives.get(i);
                output.append("    "); // NOI18N
                output.append(ti.getTagName());
                output.append("\n"); // NOI18N
                TagAttributeInfo[] attributes =  ti.getAttributes();
                for (int k = 0; k < attributes.length; k++) {
                    output.append("      "); // NOI18N
                    output.append(attributes[k].getName());
                    output.append("\n");// NOI18N
                }
            } else {
                String directive = (String)directives.get(i);
                output.append("  "); // NOI18N
                output.append(directive);
                output.append("\n");// NOI18N
                List attributes = getDirectiveAttributes(directive, "");// NOI18N
                for (int k = 0; k < attributes.size(); k++) {
                    String attribute = (String)attributes.get(k);
                    output.append("      "); // NOI18N
                    output.append(attribute);
                    output.append("\n"); // NOI18N
                }
            }
        }

        return output.toString();
    }

    /** Returns an item on offset <code>offset</code>
     * This method is largely a workaround for a bug in getTokenChain().
     * If offset falls right between two items, returns one which is just before
     * offset. If <code>offset == 0</code>, retruns null. */
    @Deprecated()
    public TokenItem getItemAtOrBefore(int offset) throws BadLocationException {
        TokenItem backItem = null;
        int chainLength = 100;
        while (backItem == null) {
            if (offset < getDocument().getLength()) {
                backItem = getTokenChain( offset,
                        Math.min(offset + chainLength, getDocument().getLength())/*, false*/ );
            } else {
                // @ end of document
                backItem = getTokenChain(Math.max(offset-50, 0), offset);
            }

            if (chainLength > 1000) {
                break;
            }
            chainLength++;
        }
        if (backItem == null)
            return null;

        // forward to the offset where our token definitely is
        TokenItem item;
        while (true) {
            item = backItem.getNext();
            //System.out.println("looking at item " + item);
            if (item == null) {
                item = backItem;
                break;
            }
            if (item.getOffset() > offset) {
                item = backItem;
                break;
            }
            backItem = item;
        }

        TokenItem adjustedItem = (item.getOffset() == offset) ?
            item.getPrevious() : item;

        return adjustedItem;
    }

    /** Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just after the offset.
     * @param offset offset in document where to search for SyntaxElement
     * @return SyntaxElement surrounding or laying before the offset
     */
    public SyntaxElement getElementChain( int offset ) throws BadLocationException {
        TokenItem item = getItemAtOrBefore(offset);
        if (item == null)
            return null;
        TokenID id = item.getTokenID();

        if (/*id == JspTagTokenContext.COMMENT || */
                id == JspTagTokenContext.ERROR ||
                id == JspTagTokenContext.TEXT ||
                id == JspMultiTokenContext.ERROR ||
                /*id == JspDirectiveTokenContext.COMMENT || */
                id == JspDirectiveTokenContext.ERROR ||
                id == JspDirectiveTokenContext.TEXT
                ) {
            return null;
        }

        //JSP comment
        if(id == JspTagTokenContext.COMMENT || id == JspDirectiveTokenContext.COMMENT) {
            return getCommentChain(item);
        }

        //Expression language handling
        if(item.getTokenContextPath().contains(ELTokenContext.contextPath)) {
            return getELChain(item);
        }

        if (id == JspTagTokenContext.SYMBOL2 || id == JspDirectiveTokenContext.SYMBOL2) {
            if (isScriptStartToken(item)) {
                return getScriptingChain(item);
            }

            if ((getTokenEnd(item) == offset) && isScriptEndToken(item)) {
                item.getNext();
                if (!isTagDirToken(item))
                    return getContentChain(item);
            }
            return null;
        }

        if (id == JspTagTokenContext.TAG ||
                id == JspTagTokenContext.SYMBOL ||
                id == JspTagTokenContext.ATTRIBUTE ||
                id == JspTagTokenContext.ATTR_VALUE ||
                id == JspTagTokenContext.WHITESPACE ||
                id == JspTagTokenContext.EOL ||
                id == JspDirectiveTokenContext.TAG ||
                id == JspDirectiveTokenContext.SYMBOL ||
                id == JspDirectiveTokenContext.ATTRIBUTE ||
                id == JspDirectiveTokenContext.ATTR_VALUE ||
                id == JspDirectiveTokenContext.WHITESPACE ||
                id == JspDirectiveTokenContext.EOL) {
            // may be intetesting: tag, directive,
            // but may also be a comment. Look back for SYMBOL: <, </, <%@
            // or COMMENT
            TokenItem elementStart = item;
            do {
                //System.out.println("backtracking, elementStart = " + elementStart);
                if (elementStart == null)
                    return null;
                if (elementStart.getTokenID() == JspTagTokenContext.SYMBOL
                        || elementStart.getTokenID() == JspDirectiveTokenContext.SYMBOL) {
                    if (elementStart.getImage().equals("<")) {   // NOI18N
                        return getTagOrDirectiveChain(true, elementStart);
                    }
                    if (elementStart.getImage().equals("</")) {   // NOI18N
                        return getEndTagChain(elementStart);
                    }
                    if (elementStart.getImage().equals("<%@")) {   // NOI18N
                        return getTagOrDirectiveChain(false, elementStart);
                    }
                }
                if(elementStart.getTokenID() == JspTagTokenContext.ERROR
                        || elementStart.getTokenID() == JspDirectiveTokenContext.ERROR) {
                    //an error in JSP code
                    return null;
                }
                if (elementStart.getTokenID() == JspTagTokenContext.COMMENT
                        || elementStart.getTokenID() == JspDirectiveTokenContext.COMMENT) {
                    return null;
                }
                elementStart = elementStart.getPrevious();
            }
            while (true);
        }

        // now we are either in the scripting language or in the content language.
        // to determine which one it is, look back for SYMBOL2: <%, <%=, <%!
        // (scripting language) or for any other JspTag token (content language).
        // if nothing found, we are in the content language
        if (isScriptingOrContentToken(item)) {
            TokenItem elementStart = item;
            do {
                if (elementStart.getPrevious() == null) {
                    // we backtracked to the beginning without finding
                    // a distinguishing symbol - we are in the content language
                    return getContentChain(elementStart);
                }
                elementStart = elementStart.getPrevious(); // now non-null
                if (!isScriptingOrContentToken(elementStart)
                        || elementStart.getTokenID() == JspTagTokenContext.COMMENT
                        || elementStart.getTokenID() == JspDirectiveTokenContext.COMMENT
                        || elementStart.getTokenContextPath().contains(ELTokenContext.contextPath)) {
                    // something from JSP
                    if (isScriptStartToken(elementStart)) {
                        return getScriptingChain(elementStart.getNext());
                    } else {
                        return getContentChain(elementStart.getNext());
                    }
                }
            }
            while (true);
        }

        return null;
    }

    /** Returns true if item is a starting symbol for a block in
     * the scripting language. */
    private boolean isScriptStartToken(TokenItem item) {
        if (item == null)
            return false;
        TokenID id = item.getTokenID();
        if (id == JspTagTokenContext.SYMBOL2
                || id == JspDirectiveTokenContext.SYMBOL2) {
            String image = item.getImage();
            if (image.equals("<%") ||   // NOI18N
                    image.equals("<%=") ||  // NOI18N
                    image.equals("<%!"))    // NOI18N
                return true;
        }
        return false;
    }

    /** Returns true if item is an ending symbol for a block in
     * the scripting language. */
    private boolean isScriptEndToken(TokenItem item) {
        if (item == null)
            return false;
        TokenID id = item.getTokenID();
        if (id == JspTagTokenContext.SYMBOL2
                || id == JspDirectiveTokenContext.SYMBOL2) {
            String image = item.getImage();
            if (image.equals("%>")) // NOI18N
                return true;
        }
        return false;
    }

    /** Returns true if item is an item which can be INSIDE
     * a JSP tag or directive (i.e. excuding delimeters). */
    private boolean isInnerTagDirToken(TokenItem item) {
        if (!isTagDirToken(item))
            return false;
        TokenID id = item.getTokenID();
        if (id == JspTagTokenContext.SYMBOL
                || id == JspDirectiveTokenContext.SYMBOL) {
            String image = item.getImage();
            if (image.equals("<") ||    // NOI18N
                    image.equals("</") ||   // NOI18N
                    image.equals("<%@") ||  // NOI18N
                    image.equals("%>") ||   // NOI18N
                    image.equals(">") ||    // NOI18N
                    image.equals("/>"))     // NOI18N
                return false;
        }
        return true;
    }

    /** Returns true if item is an item which can be INSIDE
     * a JSP tag or directive (i.e. excuding delimeters). */
    private boolean isTagDirToken(TokenItem item) {
        if (item == null)
            return false;
        TokenID id = item.getTokenID();
        if (id == null)
            return false;
        if ((id != JspTagTokenContext.TEXT) &&
                (id != JspTagTokenContext.ERROR) &&
                (id != JspTagTokenContext.TAG) &&
                (id != JspTagTokenContext.SYMBOL) &&
                (id != JspTagTokenContext.ATTRIBUTE) &&
                (id != JspTagTokenContext.ATTR_VALUE) &&
                (id != JspTagTokenContext.WHITESPACE) &&
                (id != JspTagTokenContext.EOL) &&
                (id != JspDirectiveTokenContext.TEXT) &&
                (id != JspDirectiveTokenContext.ERROR) &&
                (id != JspDirectiveTokenContext.TAG) &&
                (id != JspDirectiveTokenContext.SYMBOL) &&
                (id != JspDirectiveTokenContext.ATTRIBUTE) &&
                (id != JspDirectiveTokenContext.ATTR_VALUE) &&
                (id != JspDirectiveTokenContext.WHITESPACE) &&
                (id != JspDirectiveTokenContext.EOL )) {
            return false;
        }
        // PENDING - EOL can still be a comment
        return true;
    }

    /** Return true if this item does not belong to JSP syntax
     *  and belongs to one of the syntaxes we delegate to. */
    private boolean isScriptingOrContentToken(TokenItem item) {
        if (item == null)
            return true;
        TokenID id = item.getTokenID();
        if (id == null)
            return true;
        if ((id == JspTagTokenContext.TEXT) ||
                (id == JspTagTokenContext.ERROR) ||
                (id == JspTagTokenContext.TAG) ||
                (id == JspTagTokenContext.SYMBOL) ||
                (id == JspTagTokenContext.ATTRIBUTE) ||
                (id == JspTagTokenContext.ATTR_VALUE) ||
                (id == JspTagTokenContext.SYMBOL2) ||
                (id == JspTagTokenContext.EOL) ||
                (id == JspDirectiveTokenContext.TEXT) ||
                (id == JspDirectiveTokenContext.ERROR) ||
                (id == JspDirectiveTokenContext.TAG) ||
                (id == JspDirectiveTokenContext.SYMBOL) ||
                (id == JspDirectiveTokenContext.ATTRIBUTE) ||
                (id == JspDirectiveTokenContext.ATTR_VALUE) ||
                (id == JspDirectiveTokenContext.SYMBOL2) ||
                (id == JspDirectiveTokenContext.EOL) ||
                (id == JspMultiTokenContext.ERROR))
            return false;
        return true;
    }

    public boolean isValueBeginning(String text) {
        if (text.trim().endsWith("\"\""))   // NOI18N
            return false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c != ' ') &&
                    (c != '=') &&
                    (c != '"'))
                return false;
        }
        return true;
    }

    // ------- METHODS FOR CONSTRUCTING SEMANTICALLY LIKNKED CHAINS OF TOKENS ------

    private SyntaxElement getCommentChain(TokenItem token) {
        //we are somewhere in a JSP comment - need to find its start and end
        //backtrace for start
        TokenItem start = null;
        TokenItem search = token;
        do {
            if(search != null && search.getImage().startsWith("<%--")) { //NOI18N
                start = search;
                break;
            }
            search = search.getPrevious();
        } while(search != null);

        if(start == null) {
            //didn't find a comment start - strange???
            return null;
        }

        //find comment end
        TokenItem end = null;
        TokenItem prevNonNullToken = token;
        search = token;
        do {
            if(search != null && search.getImage().endsWith("--%>")) { //NOI18N
                end = search;
                break;
            }
            prevNonNullToken = search;
            search = search.getNext();
        } while(search != null);
        if(end == null) {
            //comment to the end of the file - may happed
            end = prevNonNullToken; //last non-null token
        }

        return new SyntaxElement.Comment(this, start.getOffset(), getTokenEnd(end));
    }

    private SyntaxElement getELChain(TokenItem token) {
        //we are somewhere in an expression language - need to find its start and end
        //backtrace for start
        TokenItem scan = token;
        TokenItem start = null;
        do {
            start = scan;
            scan = scan.getPrevious();
        } while(scan != null && scan.getTokenContextPath().contains(ELTokenContext.contextPath));

        //find comment end
        TokenItem end = null;
        scan = token;
        do {
            end = scan;
            scan = scan.getNext();
        } while(scan != null && scan.getTokenContextPath().contains(ELTokenContext.contextPath));

        return new SyntaxElement.ExpressionLanguage(this, start.getOffset(), getTokenEnd(end));
    }


    /** Gets an element representing a tag or directive starting with token item firstToken. */
    private SyntaxElement getTagOrDirectiveChain(boolean tag, TokenItem firstToken) {
        //suppose we are in a tag or directive => we do not have to distinguish tokenIDs -
        //it is sufficient to work with numeric ids (solves directive/tag tokenID problem)
        TokenItem item = firstToken.getNext();
        String name = getWholeWord(item, JspTagTokenContext.TAG);
        while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID ||
                item.getTokenID().getNumericID() == JspTagTokenContext.WHITESPACE_ID )) {
            item = item.getNext();
        }
        TreeMap attributes = new TreeMap();
        while (isInnerTagDirToken(item)) {
            // collect the attributes
            if (item.getTokenID().getNumericID() == JspTagTokenContext.ATTRIBUTE_ID) {
                String attributeName = getWholeWord(item, JspTagTokenContext.ATTRIBUTE);
                // forward to the next non-ATTRIBUTE token
                while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.ATTRIBUTE_ID)) {
                    item = item.getNext();
                }
                // find the value
                while ((item != null) &&
                        (item.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID) &&
                        (isValueBeginning(item.getImage()))) {
                    item = item.getNext();
                }
                StringBuilder value = new StringBuilder();
                while ((item != null)
                        && ((item.getTokenID().getNumericID() == JspTagTokenContext.ATTR_VALUE_ID)
                        || (item.getTokenID().getNumericID() == JspTagTokenContext.EOL_ID))) {
                    value.append(item.getImage());
                    item = item.getNext();
                    // request time values
                    if ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL2_ID)) {
                        // scripting language - something like request time value of a JSP tag
                        while (!isScriptEndToken(item)) {
                            if (item == null)
                                break;
                            else {
                                value.append(item.getImage());
                                item = item.getNext();
                            }
                        }
                        // now it's a script end token
                        if (item != null) {
                            value.append(item.getImage());
                            item = item.getNext();
                        }
                    }

                    //expression language - just put its content into attribute value
                    if((item != null) && (item.getTokenContextPath().contains(ELTokenContext.contextPath))) {
                        //go over all EL tokens and add them to the attribute value
                        while(item.getTokenContextPath().contains(ELTokenContext.contextPath)) {
                            item = item.getNext();
                            if(item == null) break;
                            value.append(item.getImage());
                        }
                    }

                }
                String vString = value.toString();
                // cut off the beginning and ending quotes
                if (vString.startsWith("\""))   // NOI18N
                    vString = vString.substring(1);
                if (vString.endsWith("\""))     // NOI18N
                    vString = vString.substring(0, vString.length() - 1);
                attributes.put(attributeName, vString);
                continue;
            }
            if (item.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL2_ID) {
                // scripting language - something like request time value of a JSP tag
                while (!isScriptEndToken(item)) {
                    if (item == null)
                        break;
                    else
                        item = item.getNext();
                }
                // now it's a script end token
                if (item != null)
                    item = item.getNext();
                continue;
            }
            // a token I am not interested in
            item = item.getNext();
        }
        if (tag) {
            boolean endslash= false;
            if (item != null)
                endslash = (item.getImage().equals("/>"));   // NOI18N

            return new SyntaxElement.Tag(this, firstToken.getOffset(),
                    (item != null)? getTokenEnd(item): getDocument().getLength(),
                    name, attributes, endslash);
        } else {
            return new SyntaxElement.Directive(this, firstToken.getOffset(),
                    (item != null)? getTokenEnd(item): getDocument().getLength(),
                    name, attributes);
        }
    }

    private SyntaxElement getEndTagChain(TokenItem firstToken) {
        TokenItem item = firstToken.getNext();
        String name = getWholeWord(item, JspTagTokenContext.TAG);
        while ((item != null) && (item.getTokenID() == JspTagTokenContext.TAG)) {
            item = item.getNext();
        }
        while (isInnerTagDirToken(item)) {
            item = item.getNext();
        }
        return new SyntaxElement.EndTag(this, firstToken.getOffset(),
                getTokenEnd(item), name);
    }

    private String getWholeWord(TokenItem firstToken, TokenID requestedTokenID) {
        StringBuilder sb = new StringBuilder();
        while ((firstToken != null) && (firstToken.getTokenID().getNumericID() == requestedTokenID.getNumericID())) {
            sb.append(firstToken.getImage());
            firstToken = firstToken.getNext();
        }
        return sb.toString().trim();
    }

    /** Returns an element of scripting language starting with firstToken.
     * If forstToken is null, returns element representing end of the document.
     */
    private SyntaxElement getScriptingChain(TokenItem firstToken) {
        if (firstToken == null) {
            return new SyntaxElement.ScriptingL(this,
                    getDocument().getLength(), getDocument().getLength());
        }
        TokenItem item = firstToken;
        do {
            TokenItem nextItem = item.getNext();
            if (nextItem == null) {
                return new SyntaxElement.ScriptingL(this,
                        firstToken.getOffset(), getDocument().getLength());
            }
            if (!isScriptingOrContentToken(nextItem))
                return new SyntaxElement.ScriptingL(this,
                        firstToken.getOffset(), getTokenEnd(nextItem));
            item = nextItem;
        }
        while (true);
    }

    /** Returns an element of content language starting with firstToken.
     * If forstToken is null, returns element representing end of the document.
     */
    private SyntaxElement getContentChain(TokenItem firstToken) {
        if (firstToken == null) {
            return new SyntaxElement.ContentL(this,
                    getDocument().getLength(), getDocument().getLength());
        }
        TokenItem item = firstToken;
        do {
            TokenItem nextItem = item.getNext();
            if (nextItem == null) {
                return new SyntaxElement.ContentL(this,
                        firstToken.getOffset(), getDocument().getLength());
            }
            if (!isScriptingOrContentToken(nextItem)
                    || nextItem.getTokenID() == JspTagTokenContext.COMMENT
                    || nextItem.getTokenID() == JspDirectiveTokenContext.COMMENT
                    || nextItem.getTokenContextPath().contains(ELTokenContext.contextPath))
                return new SyntaxElement.ContentL(this,
                        firstToken.getOffset(), getTokenEnd(item));
            item = nextItem;
        }
        while (true);
    }

    /** The way how to get previous SyntaxElement in document. It is not intended
     * for direct usage, and thus is not public. Usually, it is called from
     * SyntaxElement's method getPrevious()
     */
    SyntaxElement getPreviousElement( int offset ) throws BadLocationException {
        if (offset == 0)
            return null;
        SyntaxElement elem = null;
        offset--;
        do {
            elem = getElementChain( offset);
            if (elem == null){
                TokenItem ti = getItemAtOrBefore(offset);
                if (ti == null)
                    return null;
                offset = ti.getOffset() -1 ;
            }
        } while (elem == null && offset >= 0);
        return elem;
    }

    SyntaxElement getNextElement(int offset) throws BadLocationException {
        int doclen = getDocument().getLength();
        if(offset >= doclen)
            return null;

        SyntaxElement elem = null;
        offset++;
        do {
            elem = getElementChain(offset);
            if(elem == null) {
                TokenItem ti = getItemAtOrBefore(offset);
                if(ti == null)
                    return null;
                offset = getTokenEnd(ti) + 1;
            }
        } while(elem == null && offset < doclen);

        return elem;
    }

    public List<JspCompletionItem> getPossibleEndTags(int offset, int anchor, String pattern) throws BadLocationException {
        return getPossibleEndTags(offset, anchor, pattern, false); //return all end tags
    }

    public List<JspCompletionItem> getPossibleEndTags(int offset, int anchor, String pattern, boolean firstOnly) throws BadLocationException {
        SyntaxElement elem = getElementChain( offset );

        Stack<String> stack = new Stack<>();
        List<JspCompletionItem> result = new ArrayList<>();
        Set<String> found = new HashSet<>();

        if( elem != null ) {
            elem = elem.getPrevious();  // we need smtg. before our </
        } else {    // End of Document
            if( offset > 0 ) {
                elem = getElementChain( offset-1 );
            } else { // beginning of document too, not much we can do on empty doc
                return result;
            }
        }


        for( ; elem != null; elem = elem.getPrevious() ) {

            if( elem instanceof SyntaxElement.EndTag ) {
                stack.push( ((SyntaxElement.EndTag)elem).getName() );
            } else if( elem instanceof SyntaxElement.Tag ) {
                SyntaxElement.Tag tag = (SyntaxElement.Tag)elem;

                if (tag.isClosed())
                    continue;

                String image = tag.getName();
                String prefix = image.substring(0, image.indexOf(':'));
                String name = image.substring(image.indexOf(':')+1);
                TagInfo ti = null;

                TagLibraryInfo tli = getTagLibrary(prefix, true);
                if (tli != null) {
                    ti = tli.getTag(name);
                    if (ti == null) {
                        // try to find the info among tag files
                        TagFileInfo tagFileInfo = tli.getTagFile(name);
                        if (tagFileInfo != null) {
                            ti = tagFileInfo.getTagInfo();
                        }
                    }
                }

                if (STANDARD_JSP_PREFIX.equals(prefix)) {
                    initCompletionData();
                    TagInfo[] stanTagDatas = getTagInfos();
                    for (int i=0; i<stanTagDatas.length; i++) {
                        if (stanTagDatas[i].getTagName().equals(name)) {
                            ti = stanTagDatas[i];
                            break;
                        }
                    }
                }

                if (ti == null) continue; // Unknown tag - ignore

                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes - can close this tag
                    if( image.startsWith( pattern ) && !found.contains( image ) ) {    // add only new items
                        found.add( image );

                        if (ti.getBodyContent().equalsIgnoreCase(TagInfo.BODY_CONTENT_EMPTY))
                            continue;

                        result.add(JspCompletionItem.createTag("/"+image, anchor, ti ) );  // NOI18N

                        if(firstOnly) break; //return only the first found not-finished start token
                    }
                    // if( ! tag.hasOptionalEnd() ) break;  // If this tag have required EndTag, we can't go higher until completing this tag
                } else {                        // not empty - we match content of stack
                    if( stack.peek().equals( image ) ) { // match - close this branch of document tree
                        stack.pop();
                    } // else if( ! tag.hasOptionalEnd() ) break; // we reached error in document structure, give up
                }

                // this is error - end of empty tag
                if (ti.getBodyContent().equalsIgnoreCase(TagInfo.BODY_CONTENT_EMPTY))
                    continue;

                // if( tag.isEmpty() ) continue; // ignore empty Tags - they are like start and imediate end

            }
        }

        return result;
    }

    public CompletionItem getAutocompletedEndTag(int offset) {
        try {
            SyntaxElement elem = getElementChain( offset - 1);
            if(elem instanceof SyntaxElement.Tag) {
                String tagName = ((SyntaxElement.Tag)elem).getName();
                return HtmlCompletionItem.createAutocompleteEndTag(tagName, offset);
            }
        }catch(BadLocationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return null;
    }

    public FileObject getFileObject() {
        return fobj;
    }

    /** Get the bracket finder that will search for the matching bracket
     * or null if the bracket character doesn't belong to bracket
     * characters.
     *
     * Customized finder recognizes also '<' and '>' as bracket chars. It is set to be used
     * in findMatchingBlock.
     */
    @Override
    protected ExtSyntaxSupport.BracketFinder getMatchingBracketFinder(char bracketChar) {
        if (useCustomBracketFinder) {
            JspSyntaxSupport.BracketFinder bf = new JspSyntaxSupport.BracketFinder(bracketChar);
            return bf.isValid()? bf: null;
        } else{
            return super.getMatchingBracketFinder(bracketChar);
        }
    }

    /** Find matching bracket or more generally block
     * that matches with the current position.
     * @param offset position of the starting bracket
     * @param simple whether the search should skip comment and possibly other areas.
     *  This can be useful when the speed is critical, because the simple
     *  search is faster.
     * @return array of integers containing starting and ending position
     *  of the block in the document. Null is returned if there's
     *  no matching block.
     */
    @Override
    public int[] findMatchingBlock(int offset, boolean simpleSearch)
            throws BadLocationException {

        int [] r_value = null;

        TokenItem token = getItemAtOrBefore((offset<getDocument().getLength())?offset+1:offset);
        if (token != null){
            //do we need to match jsp comment?
            if (token.getTokenID() == JspTagTokenContext.COMMENT) {
                return findMatchingJspComment(token, offset);
            }
            // Is it matching of scriptlet delimiters?
            if (token.getTokenContextPath().contains(JspTagTokenContext.contextPath)
                    && token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL2_ID){
                return findMatchingScripletDelimiter(token);
            }
            // Try to match the tags.
            if (token.getTokenContextPath().contains(JspTagTokenContext.contextPath)){
                return findMatchingTag(token);
            } else {
                if (isScriptingOrContentToken(token)) {
                    useCustomBracketFinder = false;
                } else {
                    useCustomBracketFinder = true;
                }
                r_value =  super.findMatchingBlock(offset, simpleSearch);
            }
        }


        return r_value;
    }

    private int[] findMatchingJspComment(TokenItem token, int offset) {
        String tokenImage = token.getImage();
        if(tokenImage.startsWith("<%--") && (offset < (token.getOffset()) + "<%--".length())) { //NOI18N
            //start html token - we need to find the end token of the html comment
            while(token != null) {
                if((token.getTokenID() == JspTagTokenContext.COMMENT)
                        || (token.getTokenID() == JspTagTokenContext.EOL)) {
                    if(token.getImage().endsWith("--%>")) { //NOI18N
                        //found end token
                        int start = token.getOffset() + token.getImage().length() - "--%>".length(); //NOI18N
                        int end = token.getOffset() + token.getImage().length();
                        return new int[] {start, end};
                    }
                } else break;
                token = token.getNext();
            }
        } else if(tokenImage.endsWith("--%>") && (offset >= (token.getOffset()) + token.getImage().length() - "--%>".length())) { //NOI18N
            //end html token - we need to find the start token of the html comment
            while(token != null) {
                if((token.getTokenID() == JspTagTokenContext.COMMENT)
                        || (token.getTokenID() == JspTagTokenContext.EOL)) {
                    if(token.getImage().startsWith("<%--")) { //NOI18N
                        //found end token
                        int start = token.getOffset();
                        int end = token.getOffset() + "<%--".length(); //NOI18N
                        return new int[] {start, end};
                    }
                } else break;
                token = token.getPrevious();
            }
        }
        return null;
    }

    private int [] findMatchingScripletDelimiter(TokenItem token){
        if (token.getImage().charAt(0) == '<'){
            do{
                token = token.getNext();
            } while (token != null
                    && !(token.getTokenContextPath().contains(JspTagTokenContext.contextPath)
                    &&token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL2_ID));
        } else {
            do{
                token = token.getPrevious();
            } while (token != null
                    && !(token.getTokenContextPath().contains(JspTagTokenContext.contextPath)
                    &&token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL2_ID));
        }
        if (token != null){
            return new int [] {token.getOffset(), token.getOffset() + token.getImage().length()};
        }
        return null;
    }

    private int[] findMatchingTag(TokenItem inputToken){
        // TODO - replanning to the other thread. Now it's in awt thread
        // if the curret is after jsp tag ( after the char '>' ), ship inside the tag
        TokenItem token = inputToken;
        if (token != null && token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID
                && token.getImage().charAt(token.getImage().length()-1) == '>')
            token = token.getPrevious();
        boolean isInside = false;  // flag, whether the curret is somewhere in a jsp tag
        if (token != null
                && ((token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID && token.getImage().trim().length() > 0)
                || (token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID && token.getImage().charAt(0)=='<'))){
            if (token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID) { // the starting of the jsp tag
                // we are somewhere at beginning of the jsp tag. Find out the token with the jsp tag.
                while (token !=null
                && !(token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID
                && token.getImage().trim().length() > 0)) {
                    token = token.getNext();    // move at the jsp tag
                }
            }
            isInside = true; // the curret is somewhere in '<jsptag' or '</jsptag'
        } else {
            // find out whether the curret is inside a jsp tag
            if (token != null
                    && !(token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID && token.getImage().trim().length() > 0)){
                token = token.getPrevious();
                //try to find the beginning of the tag.
                while (token!=null
                        && !(token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID
                        && token.getImage().trim().length() > 0)  // this is hack, because a whitspaces are returned are with TAG_ID
                        && !(token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID
                        && token.getImage().charAt(token.getImage().length()-1) == '>')) {
                    token = token.getPrevious();
                }
                if (token!=null && token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID) {
                    isInside = true;
                }
            }
        }
        // Now we have the begining of the tag and we can start with the finding opposit tag.
        if (token != null && token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID && isInside){

            int start; // possition where the matched tag starts
            int end;   // possition where the matched tag ends
            int poss = 0; // how many the same tags is inside the mathed tag
            boolean singleTag = false;
            String tag = token.getImage().trim();

            while (token != null && token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL_ID) {
                token = token.getPrevious();
            }
            if (token == null)
                return null;
            if ((token.getImage().length() == 2) && token.getImage().charAt(1) == '/'){
                while ( token != null){
                    // see issue #229149
                    if (token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID) {
                        if ("/>".equals(token.getImage())) { //NOI18N
                            singleTag = true;
                        } else if ("<".equals(token.getImage())) { //NOI18N
                            singleTag = false;
                        }
                    }
                    if (token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID) {
                        if (token.getImage().trim().equals(tag) && !singleTag){
                            while (token != null && token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL_ID) {
                                token = token.getPrevious();
                            }
                            if (token != null) {
                                if (token.getImage().length() == 1){
                                    if (poss == 0){
                                        start = token.getOffset();
                                        token = token.getNext();
                                        end = token.getOffset()+token.getImage().length();

                                        // add everything up to ending > or /> into the selection
                                        TokenItem next = token.getNext();
                                        while (next != null &&
                                                !((next.getTokenID() == JspTagTokenContext.SYMBOL && next.getImage().endsWith(">"))
                                                || next.getTokenID() == JspTagTokenContext.EOL)) {
                                            end += next.getImage().length();
                                            next = next.getNext();
                                        }
                                        if (next != null && next.getTokenID() != JspTagTokenContext.EOL) {
                                            end += next.getImage().length();
                                        }

                                        return new int[] {start, end};
                                    } else {
                                        poss++;
                                    }
                                }
                                if (token.getImage().length() == 2){
                                    poss--;
                                }
                            }

                        }
                    }
                    token = token.getPrevious();
                }

            } else{
                if ((token.getImage().length() == 1) && token.getImage().charAt(0) == '<'){
                    poss = 1;
                    TokenItem hToken;
                    while ( token != null){
                        if (token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID) {
                            if (token.getImage().trim().equals(tag)){
                                hToken = token;
                                while (token != null && token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL_ID) {
                                    token = token.getPrevious();
                                }
                                if (token != null) {
                                    if (token.getImage().length() == 2){
                                        if (poss == 0){
                                            start = token.getOffset();
                                            end = hToken.getOffset()+hToken.getImage().length()+1;
                                            token = token.getNext();

                                            while (token != null && (token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL_ID
                                                    || token.getImage().charAt(0)!='>')){
                                                token = token.getNext();
                                            }
                                            if (token != null)
                                                end = token.getOffset()+1;
                                            return new int[] {start, end};
                                        } else {
                                            poss++;
                                        }
                                    }
                                    if (token.getImage().length() == 1){
                                        // see issue #229149
                                        while (token != null) {
                                            if (token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID) {
                                                if ("/>".equals(token.getImage())) {        //NOI18N
                                                    break;
                                                } else if (">".equals(token.getImage())) {  //NOI18N
                                                    poss--;
                                                    break;
                                                }
                                            }
                                            token = token.getNext();
                                        }
                                    }
                                }
                                token = hToken;
                            }
                        }
                        token = token.getNext();
                    }
                }
            }
        }
        return null;
    }

    /** Finds out whether the given tagTokenItem is a part of a singleton tag (e.g. <div style=""/>).
     * @tagTokenItem a token item whithin a tag
     * @return true is the token is a part of singleton tag
     */
    public boolean isSingletonTag(TokenItem tagTokenItem) {
        TokenItem ti = tagTokenItem;
        while(ti != null) {
            if(ti.getTokenID() == JspTagTokenContext.SYMBOL){
                if("/>".equals(ti.getImage())) { // NOI18N
                    //it is a singleton tag => do not match
                    return true;
                }
                if(">".equals(ti.getImage())) return false; // NOI18N
            }
            //break the loop on TEXT or on another open tag symbol
            //(just to prevent long loop in case the tag is not closed)
            if(ti.getTokenID() == JspTagTokenContext.TEXT) break;

            ti = ti.getNext();
        }
        return false;
    }

    /** Get the array of token IDs that should be skipped when
     * searching for matching bracket. It usually includes comments
     * and character and string constants. Returns empty array by default.
     */
    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    protected TokenID[] getBracketSkipTokens() {
        return JSP_BRACKET_SKIP_TOKENS;
    }

    public static TokenSequence tokenSequence(TokenHierarchy hi, Language language, int offset) {
        TokenSequence ts = hi.tokenSequence(language);
        if(ts == null) {
            //Html language is not top level one
            ts = hi.tokenSequence();
	    if(ts == null) {
		return null; //seems to might happen during j2ee ergonomics lazy load
	    }
            ts.move(offset);
            if(!ts.moveNext() && !ts.movePrevious()) {
                return null; //no token found
            } else {
                ts = ts.embedded(language);
            }
        }
        return ts;
    }


    /** Finder for the matching bracket. It gets the original bracket char
     * and searches for the appropriate matching bracket character.
     */
    public class BracketFinder extends ExtSyntaxSupport.BracketFinder {

        BracketFinder(char c) {
            super(c);
        }

        /** Check whether the bracketChar really contains
         * the bracket character. If so assign the matchChar
         * and moveCount variables.
         */
        @Override
        protected boolean updateStatus() {
            if (super.updateStatus())
                return true;
            boolean valid = true;
            switch (bracketChar) {
                case '<':
                    matchChar = '>';
                    moveCount = +1;
                    break;
                case '>':
                    matchChar = '<';
                    moveCount = -1;
                    break;
                default:
                    valid = false;
            }
            return valid;
        }

        boolean isValid() {
            return (moveCount != 0);
        }

    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        //refresh fileobject
        initFileObject();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
}
