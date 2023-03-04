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

package org.netbeans.modules.pdf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JMenuItem;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.XMLDataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Permits a special kind of .xml file to be used for PDF links.
 * After this processor is registered, any .xml file which matches
 * the specified DTD (it must declare a <code>&lt;!DOCTYPE&gt;</code>)
 * will provide an instance of a {@link JMenuItem}.
 * This menu item will be named according to the XML file's display
 * name (which may be controlled via localized filenames from a
 * bundle as elsewhere).
 * Selecting it will try to show the mentioned PDF file.
 * The PDF file may be referred to as an absolute file name,
 * or as a localized path within the IDE installation,
 * or (in the future) as an arbitrary URL.
 * The XML file is suitable for direct inclusion in a menu
 * bar folder, for example <samp>..../system/Menu/Help/</samp>.
 *
 * @author Jesse Glick
 * @author  Marian Petras
 * @see org.openide.loaders.XMLDataObject.Processor
 */
public class LinkProcessor implements InstanceCookie,
                                      XMLDataObject.Processor,
                                      ActionListener {

    /** Public ID of catalog. */
    public static final String PUBLIC_ID
            = "-//NetBeans//DTD PDF Document Menu Link 1.0//EN";        //NOI18N
    /** */
    public static final String PUBLIC_WWW
            = "http://www.netbeans.org/dtds/pdf_link-1_0.dtd";          //NOI18N

    /** <code>XMLDataObject</code> this processor is linked to. */
    private XMLDataObject xmlDataObject;
    
    
    /* JST: Replaced with registration in xml layer.
    * Initilializes <code>LinkProcessor</code>. *
    public static void init () {
        // Registering of catalog is in xml layer, see org/netbeans/modules/utilities/Layer.xml.
        
        XMLDataObject.Info xmlInfo = new XMLDataObject.Info ();
        
        xmlInfo.setIconBase("/org/netbeans/modules/pdf/PDFDataIcon"); // NOI18N
        xmlInfo.addProcessorClass(LinkProcessor.class);
        XMLDataObject.registerInfo(PUBLIC_ID, xmlInfo);
    }
     */

    /* Implements interface <code>XMLDataObject.Processor</code>. */
    /**
     * Attaches this processor to specified XML data object.
     *
     * @param  xmlDataObject  XML data object to which attach this processor
     */
    public void attachTo(XMLDataObject xmlDataObject) {
        this.xmlDataObject = xmlDataObject;
    }

    /* Implements interface <code>InstanceCookie</code>. */
    /**
     * @return <code>JMenuItem</code> class
     */
    public Class instanceClass() throws IOException, ClassNotFoundException {
        return JMenuItem.class;
    }

    /* Implements interface <code>InstanceCookie</code>. */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        /*
        Image icon = Utilities.loadImage(
                "org/netbeans/modules/pdf/PDFDataIcon.gif");           //NOI18N
        try {
            FileObject file = xmlDataObject.getPrimaryFile();
            FileSystem.Status fsStatus = file.getFileSystem().getStatus();
            icon = fsStatus.annotateIcon(icon,
                                         BeanInfo.ICON_COLOR_16x16,
                                         xmlDataObject.files());
        } catch (FileStateInvalidException fsie) {
            // OK, so we use the default icon
        } */
            
        String name = xmlDataObject.getNodeDelegate().getDisplayName();
        
        JMenuItem menuItem = new JMenuItem(/*new ImageIcon(icon)*/);
        Mnemonics.setLocalizedText(menuItem, name);
        menuItem.addActionListener(this);
        
        return menuItem;
    }
    
    /* Implements interface <code>InstanceCookie</code>. */
    /**
     * @return  name of the <code>xmlDataObject</code>
     */
    public String instanceName() {
        return xmlDataObject.getName();
    }

    /**
     * Retrieves the name of a file describing the XML data object
     *
     * @return  as much precious path to the file as possible
     */
    private String getXMLFileName() {
        FileObject fileObject = xmlDataObject.getPrimaryFile();
        return FileUtil.getFileDisplayName(fileObject);
    }

    /**
     * Notifies the user that the XML file is broken.
     */
    private void notifyXMLFileBroken() {
        String msg = NbBundle.getMessage(LinkProcessor.class,
                                         "EXC_file_not_matching_DTD",   //NOI18N
                                         getXMLFileName());
        ErrorManager.getDefault().log(ErrorManager.USER, msg);
    }
    
    /**
     * Notifies the user about some problem with the XML file.
     *
     * @param  msgKey  resource bundle key for the message
     * @param  urlSpec  url that caused the problem
     * @param  isError  type of the message - use <code>true</code> for
     *                  an error message, <code>false</code> for
     *                  an information message
     */
    private void notifyBadFileSpec(String msgKey,
                                   String urlSpec,
                                   boolean isError) {
        String msg = NbBundle.getMessage(LinkProcessor.class,
                                         msgKey,
                                         getXMLFileName(),
                                         urlSpec);
        ErrorManager.getDefault().log(isError ? ErrorManager.WARNING
                                              : ErrorManager.USER,
                                      msg);
    }
    
    /**
     */
    private void notifyFileDoesNotExist(String path) {
        String msg = NbBundle.getMessage(LinkProcessor.class,
                                         "MSG_File_does_not_exist",     //NOI18N
                                         path);
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                msg, NotifyDescriptor.WARNING_MESSAGE));
    }
    
    /**
     * Grabs a file from a specification in an element of an XML file.
     *
     * @param  innerElement  element containing specification of a PDF file
     * @return  file corresponding to the specification,
     *          or <code>null</code> if the specification was illegal
     *          or unsupported
     */
    private File grabFile(Element innerElement) {
        String linkType = innerElement.getTagName();
        
        /* handle element "file": */
        if (linkType.equals("file")) {                                  //NOI18N
            if (!innerElement.hasAttribute("path")) {                   //NOI18N
                notifyXMLFileBroken();
                return null;
            }
            return new File(innerElement.getAttribute("path"));         //NOI18N
            
        /* handle element "idefile": */
        } else if (linkType.equals("idefile")) {                        //NOI18N
            if (!innerElement.hasAttribute("base")) {                   //NOI18N
                notifyXMLFileBroken();
                return null;
            }
            String base = innerElement.getAttribute("base");            //NOI18N
            String path = base.replace('.', '/') + ".pdf";              //NOI18N
            File file = InstalledFileLocator.getDefault()
                        .locate(path, null, true);
            if (file == null) {
                notifyFileDoesNotExist(path);
                return null;
            }
            return file;
            
        /* handle element "url": */
        } else if (linkType.equals("url")) {                            //NOI18N
            if (!innerElement.hasAttribute("name")) {                   //NOI18N
                notifyXMLFileBroken();
                return null;
            }
            String urlSpec = innerElement.getAttribute("name");         //NOI18N
            URL url;
            try {
                url = new URL(urlSpec);
            } catch (MalformedURLException ex) {
                notifyBadFileSpec(
                        "MSG_Cannot_open_malformed_URL",                //NOI18N
                        urlSpec,
                        true);
                return null;
            }
            if (!url.getProtocol().equals("file")) {                    //NOI18N
                notifyBadFileSpec(
                        "MSG_Cannot_open_unsupported_URL",              //NOI18N
                        urlSpec,
                        false);
            }
            try {
                return new File(new URI("file://" + url.getPath()));    //NOI18N
            } catch (URISyntaxException ex1) {
                ErrorManager.getDefault().notify(ex1);
                return null;
            } catch (IllegalArgumentException ex2) {
                ErrorManager.getDefault().notify(ex2);
                return null;
            }
            
        } else {
            notifyXMLFileBroken();
            return null;
        }
    }
    
    /* Implements interface <code>ActionListener</code>. */
    /**
     * Performs an action. Retrieves a PDF data object from the specified
     * XML data object and opens it.
     */
    public void actionPerformed(ActionEvent evt) {
        try {
            
            /* Grab the element containing the link: */
            Element innerElement;
            Document document = xmlDataObject.getDocument();
            Element pdfLinkElement = document.getDocumentElement();
            NodeList nodeList = pdfLinkElement.getChildNodes();
            int count = nodeList.getLength();
            Node node = null;
            for (int i = 0; i < count; i++) {
                Node nextNode = nodeList.item(i);
                if (nextNode.getNodeType() == Node.ELEMENT_NODE) {
                    if (node == null) {
                        node = nextNode;
                    } else {
                        /* there should be just one element */
                        notifyXMLFileBroken();
                        return;
                    }
                }
            }
            if (node == null) {
                /* there should be exactly one element within 'pdfLink' */
                notifyXMLFileBroken();
                return;
            }
            innerElement = (Element) node;
            
            /* Retrieve the PDF file: */
            File file = grabFile(innerElement);
            
            /* Try to open the file in an external viewer: */
            if (file != null) {
                try {
                    // [PENDING] in-process PDF viewer support
                    new PDFOpenSupport(file).open();
                    return;
                } catch (IllegalArgumentException ex) {
                    notifyFileDoesNotExist(file.getPath());
                }
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
}
