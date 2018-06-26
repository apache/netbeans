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

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.actions.OpenAction;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.web.core.WebExecSupport;
import org.openide.util.Exceptions;

/** The node representation of <code>JspDataObject</code> for internet files.
*
* @author Petr Jiricka
*/
public class JspNode extends DataNode {

    private static final String EXECUTION_SET_NAME = "Execution"; // NOI18N
    private static final String SHEETNAME_TEXT_PROPERTIES = "textProperties"; // NOI18N

    private static final String ICON_JSP = "org/netbeans/modules/web/core/resources/jsp16.gif"; // NOI18N
    private static final String ICON_TAG = "org/netbeans/modules/web/core/resources/tag16.gif"; // NOI18N
    private static final String ICON_JSP_XML = "org/netbeans/modules/web/core/resources/jsp-xml16.gif"; // NOI18N
    private static final String ICON_JSP_FRAGMENT = "org/netbeans/modules/web/core/resources/jsp-fragment16.gif"; // NOI18N
    
    public static final String PROP_FILE_ENCODING = "encoding"; //NOI18N
    public static final String PROP_REQUEST_PARAMS   = "requestparams"; // NOI18N
    
    /** Create a node for the internet data object using the default children.
    * @param jdo the data object to represent
    */
    public JspNode (JspDataObject jdo) {
        super(jdo, Children.LEAF);
        initialize();
    }

    private void initialize () {
        setIconBaseWithExtension(getIconBase());
        setDefaultAction (SystemAction.get (OpenAction.class));

        if (isTagFile())
                setShortDescription (NbBundle.getMessage(JspNode.class, "LBL_tagNodeShortDesc")); //NOI18N
        else
                setShortDescription (NbBundle.getMessage(JspNode.class, "LBL_jspNodeShortDesc")); //NOI18N
    }

    private String getExtension(){
        return getDataObject().getPrimaryFile().getExt();
    }
    
    private boolean isTagFile(){
        String ext = getExtension();
        return (ext.equals(JspLoader.TAGF_FILE_EXTENSION) 
            || ext.equals(JspLoader.TAGX_FILE_EXTENSION)
            || ext.equals(JspLoader.TAG_FILE_EXTENSION));
    }
    
    @Override
    public DataObject getDataObject() {
        return super.getDataObject();
    }
    
    /** Create the property sheet.
    * Subclasses may want to override this and add additional properties.
    * @return the sheet
    */
    @Override
    protected Sheet createSheet () {
        Sheet.Set ps;

        Sheet sheet = super.createSheet();

        if (!isTagFile()){
            ps = new Sheet.Set ();
            ps.setName(EXECUTION_SET_NAME);
            ps.setDisplayName(NbBundle.getBundle(JspNode.class).getString("PROP_executionSetName")); //NOI18N
            ps.setShortDescription(NbBundle.getBundle(JspNode.class).getString("HINT_executionSetName")); //NOI18N

            ps.put(new PropertySupport.ReadWrite (
                       PROP_REQUEST_PARAMS,
                       String.class,
                       NbBundle.getBundle(JspNode.class).getString("PROP_requestParams"), //NOI18N
                       NbBundle.getBundle(JspNode.class).getString("HINT_requestParams") //NOI18N
                   ) {
                       public Object getValue() {
                           return getRequestParams(((MultiDataObject)getDataObject()).getPrimaryEntry());
                       }
                       public void setValue (Object val) throws InvocationTargetException {
                           if (val instanceof String) {
                               try {
                                   setRequestParams(((MultiDataObject)getDataObject()).getPrimaryEntry(), (String)val);
                               } catch(IOException e) {
                                   throw new InvocationTargetException (e);
                               }
                           }
                           else {
                               throw new IllegalArgumentException();
                           }
                       }
                   }
                  );
                  sheet.put(ps);
        }
        // remove the params property
        //ps.remove(ExecSupport.PROP_FILE_PARAMS);
        // remove the debugger type property
        //ps.remove(ExecSupport.PROP_DEBUGGER_TYPE);


        // text sheet
        ps = new Sheet.Set();
        ps.setName(SHEETNAME_TEXT_PROPERTIES);
        ps.setDisplayName(NbBundle.getBundle(JspNode.class).getString("PROP_textfileSetName")); // NOI18N
        ps.setShortDescription(NbBundle.getBundle(JspNode.class).getString("HINT_textfileSetName")); // NOI18N
        sheet.put(ps);
        
           ps.put(new PropertySupport.ReadOnly(
                   PROP_FILE_ENCODING,
                   String.class,
                   NbBundle.getBundle(JspNode.class).getString("PROP_fileEncoding"), //NOI18N
                   NbBundle.getBundle(JspNode.class).getString("HINT_fileEncoding") //NOI18N
                   ) {
               public Object getValue() {
                   return ((JspDataObject)getDataObject()).getFileEncoding();
               }
           }
           );
        
        return sheet;
    }

    static final void wrapThrowable(Throwable outer, Throwable inner, String message) {
        outer.initCause(inner);
        Exceptions.attachMessage(outer, message);
    }

    /** Set request parameters for a given entry.
    * @param entry the entry
    * @param args array of arguments
    * @exception IOException if arguments cannot be set
    */
    static void setRequestParams(MultiDataObject.Entry entry, String params) throws IOException {
        StringBuffer newParams=new StringBuffer();
        String s=null;
        if (params!=null){
            for (int i=0;i<params.length();i++) {
                char ch = params.charAt(i);
                if ((int)ch!=13 && (int)ch!=10) newParams.append(ch);
            }
            s=newParams.toString();
            if (s.length()==0) s=null;
        } 
        WebExecSupport.setQueryString(entry.getFile (), s);
    }

    /** Get the request parameters associated with a given entry.
    * @param entry the entry
    * @return the arguments, or an empty string if no arguments are specified
    */
    static String getRequestParams(MultiDataObject.Entry entry) {
        return WebExecSupport.getQueryString(entry.getFile ());
    }

    /** Get the icon base.
    * This should be complete resource path to an icon,
    * e.g. <code>some/path/someIcon.png</code>. Subclasses may override this.
    * @return the icon base
    * @see #getIcons
    */
    protected String getIconBase() {
        String ext = getDataObject().getPrimaryFile().getExt();
        
        if (ext.equals(JspLoader.TAGF_FILE_EXTENSION) 
            || ext.equals(JspLoader.TAGX_FILE_EXTENSION)
            || ext.equals(JspLoader.TAG_FILE_EXTENSION))
                return ICON_TAG;
        if (ext.equals(JspLoader.JSF_EXTENSION )
            || ext.equals(JspLoader.JSPF_EXTENSION))
                return ICON_JSP_FRAGMENT;
        if (ext.equals(JspLoader.JSPX_EXTENSION))
                return ICON_JSP_XML;
        return ICON_JSP;
    }

}

