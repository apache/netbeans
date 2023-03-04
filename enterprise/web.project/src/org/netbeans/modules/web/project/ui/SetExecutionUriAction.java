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

package org.netbeans.modules.web.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.WebAppMetadataHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
/** 
*
* @author Milan Kuchtiak
* @author ads
*/
public final class SetExecutionUriAction extends NodeAction {
    
    private static final String IS_SERVLET_FILE = 
                "org.netbeans.modules.web.IsServletFile";            // NOI18N
    public static final String ATTR_EXECUTION_URI = "execution.uri"; //NOI18N
    // Added as  fix for IZ#171708.
    private static final MarkerClass MARKER = new MarkerClass();
    
    /**
     * Creates and starts a thread for generating documentation
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        if ((activatedNodes != null) && (activatedNodes.length == 1)) {
            if (activatedNodes[0] != null) {
                DataObject data = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
                if (data != null) {
                    FileObject servletFo = data.getPrimaryFile();
                    WebModule webModule = WebModule.getWebModule(servletFo);
                    String[] urlPatterns = getServletMappings(webModule, servletFo);
                    if (urlPatterns!=null && urlPatterns.length>0) {
                        String oldUri = (String)servletFo.getAttribute(ATTR_EXECUTION_URI);
                        ServletUriPanel uriPanel = new ServletUriPanel(urlPatterns,oldUri,false);
                        DialogDescriptor desc = new DialogDescriptor(uriPanel,
                            NbBundle.getMessage (SetExecutionUriAction.class, "TTL_setServletExecutionUri"));
                        Object res = DialogDisplayer.getDefault().notify(desc);
                        if (res.equals(NotifyDescriptor.YES_OPTION)) {
                            try {
                                servletFo.setAttribute(ATTR_EXECUTION_URI,uriPanel.getServletUri());
                            } catch (java.io.IOException ex){
                                // ignore
                            }
                        }
                    } else {
                        String mes = NbBundle.getMessage (
                                SetExecutionUriAction.class, "TXT_missingServletMappings",servletFo.getName()); //NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    }
                }
            }
        }
    }
    
    @Override
    protected boolean enable (Node[] activatedNodes) {
        if ((activatedNodes != null) && (activatedNodes.length == 1)) {
            if (activatedNodes[0] != null) {
                DataObject data = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
                if (data != null) {
                    FileObject javaClass = data.getPrimaryFile();
                    WebModule webModule = WebModule.getWebModule(javaClass);
                    if ( servletFilesScanning( webModule, javaClass ) ){
                        return false;
                    }
                    String mimetype = javaClass.getMIMEType();
                    if ( !"text/x-java".equals(mimetype) ){     // NOI18N
                        return false;
                    }
                    Boolean servletAttr = (Boolean)javaClass.getAttribute(IS_SERVLET_FILE);
                    if (!Boolean.TRUE.equals(servletAttr)) {
                        boolean isServletFile = isServletFile(webModule, 
                                javaClass, false );
                        if (isServletFile) {
                            try {
                                javaClass.setAttribute(IS_SERVLET_FILE, Boolean.TRUE); 
                            } catch (java.io.IOException ex) {
                                //we tried
                            }
                        }
                        servletAttr = isServletFile;
                    }
                    return Boolean.TRUE.equals(servletAttr);
                }
            }
        }
        return false;
    }

    /**
     * Help context where to find more about the action.
     * @return the help context for this action
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("org.netbeans.modules.web.project.ui.SetExecutionUriAction");
    }

    /**
     * Human presentable name of the action. This should be presented as an item in a menu.
     * @return the name of the action
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(SetExecutionUriAction.class, "LBL_serveltExecutionUriAction");
    }
    
    /**
     * The action's icon location.
     * @return the action's icon location
     */
    @Override
    protected String iconResource () {
        return "org/netbeans/modules/web/project/ui/resources/servletUri.gif"; // NOI18N
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    // Fix for IZ#170419 - Invoking Run took 29110 ms.
    public static boolean isScanInProgress( WebModule webModule, 
            FileObject fileObject, final ServletScanObserver observer )
    {
        Project project = FileOwnerQuery.getOwner( fileObject );
        ProjectWebModule prjWebModule = null;
        if ( project != null ){
             prjWebModule = project.getLookup().lookup( 
                    ProjectWebModule.class);
        }
        boolean isScan = project!= null;
        if ( isScan ){
                isScan = servletFilesScanning(webModule, fileObject);
                if ( !isScan ){
                    return false;
                }
                final ProjectWebModule source = prjWebModule;
                PropertyChangeListener listener = new PropertyChangeListener() {
                    
                    @Override
                    public void propertyChange( PropertyChangeEvent event ) {
                        String name = event.getPropertyName();
                        if ( ProjectWebModule.LOOKUP_ITEM.equals(name) &&
                                event.getNewValue()!= null && 
                                    event.getNewValue()!= MARKER )
                        {
                            MarkerClass marker = source.getLookup().lookup(
                                    MarkerClass.class);
                            if ( marker!= null && marker!= MARKER && 
                                    observer!= null )
                            {
                                observer.scanFinished();
                                source.removePropertyChangeListener(this);
                            }
                        }
                    }
                };
                prjWebModule.addPropertyChangeListener( listener );
                isScan = servletFilesScanning(webModule, fileObject);
                if ( !isScan ){
                    prjWebModule.removePropertyChangeListener(listener);
                }
            }
            else {
                isScan = false;
            }
        return isScan;
    }
    
    public static String[] getServletMappings(WebModule webModule, 
            FileObject javaClass) 
    {
        // Fix for IZ#170419 - Invoking Run took 29110 ms.
        assert checkScanFinished( webModule , javaClass );
        if (webModule == null) {
            return null;
        }
        
        ClassPath classPath = ClassPath.getClassPath (javaClass, ClassPath.SOURCE);
        if (classPath == null) {
            return null;
        }
        String className = classPath.getResourceName(javaClass,'.',false);

        try {
            List<ServletInfo> servlets =
                    WebAppMetadataHelper.getServlets(webModule.getMetadataModel());
            List<String> mappingList = new ArrayList<String>();
            for (ServletInfo si : servlets) {
                if (className.equals(si.getServletClass())) {
                    mappingList.addAll(si.getUrlPatterns());
                }
            }
            String[] mappings = new String[mappingList.size()];
            mappingList.toArray(mappings);
            return mappings;
        } catch (java.io.IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private static boolean checkScanFinished(WebModule webModule, FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project != null ){
            final ProjectWebModule prjWebModule = project.getLookup().lookup( 
                    ProjectWebModule.class);
            if (prjWebModule != null) {
                MarkerClass marker = prjWebModule.getLookup().lookup(
                        MarkerClass.class );
                return marker!=null && marker!=MARKER;
            }
        }
        return true;
    }

    /**
     * Method check if initial servlet scanning has been started.
     * It's done via setting special mark for project ( actually 
     * for  ProjectWebModule ).
     * If this mark is present initial scanning is either in progress
     * or finished.
     * <code>myMarker</code> is set up if scanning was started.
     * Any other instance of MarkerClass signals that scanning 
     * is already finished.
     * 
     * Fix for IZ#171708 - AWT thread blocked for 15766 ms. (project not usable after opening - fresh userdir)
     */
    private static boolean servletFilesScanning( final WebModule webModule , 
            final FileObject fileObject ) 
    {
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project != null ){
            final ProjectWebModule prjWebModule = project.getLookup().lookup( 
                    ProjectWebModule.class);
            if (prjWebModule != null) {
                MarkerClass marker = prjWebModule.getLookup().lookup(
                        MarkerClass.class );
                if ( marker == null ){
                    Runnable runnable = new Runnable(){
                        @Override
                        public void run() {
                            isServletFile(webModule, fileObject , true );
                            prjWebModule.removeCookie( MARKER);
                            prjWebModule.addCookie( new MarkerClass() );
                        }
                    };
                    if ( prjWebModule.getLookup().lookup( MarkerClass.class ) == null ){
                        /* Double check . It's not good but not fatal.
                         * In the worst case we will start several initial scanning.
                         */
                        RequestProcessor.getDefault().post(runnable);
                        prjWebModule.addCookie( MARKER );
                     }
                    return true;
                }
                else if ( marker == MARKER ){
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }

    /*
     * Modified as fix for IZ#171708. 
     */
    private static boolean isServletFile(WebModule webModule, FileObject javaClass,
            boolean initialScan) 
    {
        if (webModule == null ) {
            return false;
        }
        
        ClassPath classPath = ClassPath.getClassPath (javaClass, ClassPath.SOURCE);
        if (classPath == null) {
            return false;
        }
        String className = classPath.getResourceName(javaClass,'.',false);
        if (className == null) {
            return false;
        }
        
        try {
            MetadataModel<WebAppMetadata> metadataModel = webModule
                    .getMetadataModel();
            boolean result = false;
            if ( initialScan || metadataModel.isReady()) {
                List<ServletInfo> servlets = WebAppMetadataHelper
                        .getServlets(metadataModel);
                List<String> servletClasses = new ArrayList<String>( servlets.size() );
                for (ServletInfo si : servlets) {
                    if (className.equals(si.getServletClass())) {
                        result =  true;
                    }
                    else {
                        servletClasses.add( si.getServletClass() );
                    }
                }
                setServletClasses( servletClasses,  javaClass , initialScan);
            }
            return result;
        } catch (java.io.IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
    
    /*
     * Created as  fix for IZ#171708 - AWT thread blocked for 15766 ms. (project not usable after opening - fresh userdir)
     */
    private static void setServletClasses( final List<String> servletClasses, 
            final FileObject orig, boolean initial )
    {
        if ( initial ){
            JavaSource javaSource = JavaSource.forFileObject( orig );
            if ( javaSource == null) {
                return;
            }
            try {
            javaSource.runUserActionTask( new Task<CompilationController>(){
                @Override
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    for( String servletClass : servletClasses){
                        TypeElement typeElem = controller.getElements().
                            getTypeElement( servletClass);
                        if ( typeElem == null ){
                            continue;
                        }
                        ElementHandle<TypeElement> handle = 
                            ElementHandle.create( typeElem );
                        FileObject fileObject = SourceUtils.getFile( handle, 
                                controller.getClasspathInfo());
                        if ( fileObject != null && !Boolean.TRUE.equals(
                                fileObject.getAttribute(IS_SERVLET_FILE)))
                        {
                            fileObject.setAttribute(IS_SERVLET_FILE, Boolean.TRUE); 
                        }
                    }
                }
            }, true);
            }
            catch(IOException e ){
                Exceptions.printStackTrace(e);
            }
        }
        else {
            Runnable runnable = new Runnable() {
                
                @Override
                public void run() {
                    setServletClasses(servletClasses, orig, true);
                }
            };
            RequestProcessor.getDefault().post(runnable);
        }
    }

    /*
     * Created as  fix for IZ#171708 = AWT thread blocked for 15766 ms. (project not usable after opening - fresh userdir) 
     */
    private static class MarkerClass {
    }
}
