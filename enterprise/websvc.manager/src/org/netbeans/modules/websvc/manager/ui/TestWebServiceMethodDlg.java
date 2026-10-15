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
package org.netbeans.modules.websvc.manager.ui;


import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.event.TableModelEvent;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaType;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor;
import org.netbeans.modules.websvc.saas.util.TypeUtil;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RowModel;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Dialog that tests JAX-WS client methods
 *
 * @author  David Botterill
 */
public class TestWebServiceMethodDlg extends JPanel implements ActionListener, MethodTaskListener {

    private Dialog dialog;
    private DialogDescriptor dlg = null;
    private final String okString = NbBundle.getMessage(this.getClass(), "CLOSE");
    /**
     * The runtimeClassLoader should be used when running the web service client.  This classloader
     * only includes the necessary runtime jars for JAX-RPC to run.  The classloader does NOT have a
     * parent to delegate to.  I did this because of Xerces classloader clashes with other netbeans
     * modules.
     * -David Botterill 4/21/2004
     */
    private URLClassLoader runtimeClassLoader;
    private final DefaultMutableTreeNode parameterRootNode = new DefaultMutableTreeNode();
    private DefaultMutableTreeNode resultRootNode = new DefaultMutableTreeNode();
    private final WsdlSaas wsData;
    private final WSPort port;
    private final JavaMethod method;
    private MethodTask methodTask;

    /** Creates new form TestWebServiceMethodDlg */
    public TestWebServiceMethodDlg(WsdlSaasMethod saasMethod) {
        method = saasMethod.getJavaMethod();
        wsData = saasMethod.getSaas();
        port = saasMethod.getWsdlPort();
        //assert wsData.getWsdlData().getJaxWsDescriptor() != null;

        initComponents();
        myInitComponents();

        this.lblTitle.setText(NbBundle.getMessage(this.getClass(), "TEST_WEBSVC_LABEL") + " " /*+ method.getName()*/);
    }

    private boolean isRPCEncoded(WsdlData wsdlData) {
        File wsdlFile = new File(wsdlData.getWsdlFile());
        try {
            wsdlFile = wsdlFile.getCanonicalFile();
            return wsdlFile != null && JaxWsUtils.isRPCEncoded(wsdlFile.toURI());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    /**
     * This method returns the classloader of the Jar wsdlFile containing the web service for which we are testing the methods.
     * This class loader should be used for the runtime environment when invoking a web service.
     * TODO: determine if the tree components should get the class loader here, store the classloader in the tree nodes, or pass
     * to the tree component constructors.
     *@returns URLClassLoader - the class loader of the Jar wsdlFile for the web service with the methods to test.
     */
    private URLClassLoader getRuntimeClassLoader() {
        if (null == runtimeClassLoader) {

            /**
             * First add the URL to the jar wsdlFile for this web service.
             */
            try {
                WsdlData wsdlData = wsData.getWsdlData();

                boolean isRPCEncoded = isRPCEncoded(wsdlData);

                List<URL> urlList = TypeUtil.buildClasspath(null, !isRPCEncoded);

                WsdlServiceProxyDescriptor descriptor;
                if (isRPCEncoded) {
                    descriptor = wsdlData.getJaxRpcDescriptor();
                } else {
                    descriptor = wsdlData.getJaxWsDescriptor();
                }

                for (WsdlServiceProxyDescriptor.JarEntry entry : descriptor.getJars()) {
                    if (entry.getType().equals(WsdlServiceProxyDescriptor.JarEntry.PROXY_JAR_TYPE)) {
                        File jarFile = new File(descriptor.getXmlDescriptorFile().getParent(), entry.getName());
                        File tmpJarFile = createTempCopy(jarFile);

                        urlList.add(tmpJarFile.toURI().toURL());
                    }
                }

                URL[] urls = urlList.toArray(new URL[0]);
                /**
                 * Delegate to the module's classloader since core/startup/NbInstaller
                 * overrides the JAX-WS 2.0 jars present in JDK 6
                 *
                 * The above it no longer true but I am not sure why, yet.
                 */
                runtimeClassLoader = new URLClassLoader(urls); //this.getClass().getClassLoader());
            } catch (IOException mfu) {
                ErrorManager.getDefault().notify(mfu);
                ErrorManager.getDefault().log(this.getClass().getName() + ":IOException=" + mfu);
                return null;
            }
        }

        return runtimeClassLoader;
    }

    private File createTempCopy(File src) {
        try {
            java.io.File tempFile = Files.createTempFile("proxyjar", "jar").toFile();
            java.nio.channels.FileChannel inChannel = new java.io.FileInputStream(src).getChannel();
            java.nio.channels.FileChannel outChannel = new java.io.FileOutputStream(tempFile).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);

            inChannel.close();
            outChannel.close();
            return tempFile;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
            return null;
        }
    }

    public void displayDialog() {

        dlg = new DialogDescriptor(this, NbBundle.getMessage(this.getClass(), "TEST_WEB_SERVICE_METHOD"),
                false, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, HelpCtx.DEFAULT_HELP, this);
        dlg.setOptions(new Object[]{okButton});
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        /**
         * After the window is opened, set the focus to the Get information button.
         */
        final JPanel thisPanel = this;
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(
                        new Runnable() {

                            @Override
                            public void run() {
                                btnSubmit.requestFocus();
                                thisPanel.getRootPane().setDefaultButton(btnSubmit);
                            }
                        });
            }
        });

        /**
         * Fix for Bug: 6217545
         * Need to know what the normal cursor is so we can reset it when
         * the dialog is closed.
         * - David Botterill 1/14/2005
         *
         */
        normalCursor = dialog.getCursor();
        /**
         * Fix for Bug: 6217545
         * Set the MouseListener for the OK button to a special adapter that will
         * make the cursor look normal ALWAYS when over the OK button.
         * - David Botterill 1/14/2005
         */
        BusyMouseAdapter mouseAdapter = new BusyMouseAdapter(normalCursor);
        okButton.addMouseListener(mouseAdapter);


        dialog.show();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        pnlParameter = new javax.swing.JPanel();
        pnlLabel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblParameters = new javax.swing.JLabel();
        scrollPaneParameter = new javax.swing.JScrollPane();
        btnPanel = new javax.swing.JPanel();
        btnSubmit = new javax.swing.JButton();
        pnlResults = new javax.swing.JPanel();
        lblResults = new javax.swing.JLabel();
        scrollPaneResults = new javax.swing.JScrollPane();

        setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.main.ACC_desc")); // NOI18N
        setPreferredSize(new java.awt.Dimension(600, 450));
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.jsplintPane1.ACC_desc")); // NOI18N

        pnlParameter.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 5, 12));
        pnlParameter.setLayout(new java.awt.BorderLayout());

        pnlLabel.setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.pnlLabel.ACC_desc")); // NOI18N
        pnlLabel.setLayout(new java.awt.GridLayout(2, 0));

        lblTitle.setFont(lblTitle.getFont().deriveFont(lblTitle.getFont().getStyle() | java.awt.Font.BOLD, lblTitle.getFont().getSize()-2));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(lblTitle, org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TEST_WEB_SERVICE_METHOD")); // NOI18N
        lblTitle.setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.lblTitle.ACC_desc")); // NOI18N
        pnlLabel.add(lblTitle);

        lblParameters.setFont(lblParameters.getFont().deriveFont(lblParameters.getFont().getSize()-4f));
        lblParameters.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(lblParameters, org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TEST_WEBSVC_INSTRUCTIONS")); // NOI18N
        lblParameters.setToolTipText(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.jLabel1.ACC_desc")); // NOI18N
        pnlLabel.add(lblParameters);

        pnlParameter.add(pnlLabel, java.awt.BorderLayout.NORTH);
        pnlLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.pnlLabel.ACC_name")); // NOI18N

        scrollPaneParameter.setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.scrollPaneParameter.ACC_desc")); // NOI18N
        pnlParameter.add(scrollPaneParameter, java.awt.BorderLayout.CENTER);
        scrollPaneParameter.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.scrollPaneParameter.ACC_name")); // NOI18N

        btnPanel.setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.btnPanel.ACC_desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnSubmit, org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "BUTTON_SUBMIT")); // NOI18N
        btnSubmit.setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.btnSubmit.ACC_desc")); // NOI18N
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });
        btnPanel.add(btnSubmit);

        pnlParameter.add(btnPanel, java.awt.BorderLayout.SOUTH);
        btnPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.btnPanel.ACC_name")); // NOI18N

        jSplitPane1.setLeftComponent(pnlParameter);
        pnlParameter.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.pnlParameter.ACC_name")); // NOI18N
        pnlParameter.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.pnlParameter.ACC_desc")); // NOI18N

        pnlResults.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 12, 5, 12));
        pnlResults.setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.pnResults.ACC_desc")); // NOI18N
        pnlResults.setLayout(new java.awt.BorderLayout(0, 5));

        lblResults.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblResults.setLabelFor(scrollPaneResults);
        org.openide.awt.Mnemonics.setLocalizedText(lblResults, org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "RESULTS")); // NOI18N
        lblResults.setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.lblResults.ACC_desc")); // NOI18N
        pnlResults.add(lblResults, java.awt.BorderLayout.NORTH);

        scrollPaneResults.setToolTipText(org.openide.util.NbBundle.getBundle(TestWebServiceMethodDlg.class).getString("TestWebServiceMethodDlg.scrollPaneResults.ACC_desc")); // NOI18N
        pnlResults.add(scrollPaneResults, java.awt.BorderLayout.CENTER);
        scrollPaneResults.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.scrollPaneResults.ACC_name")); // NOI18N

        jSplitPane1.setRightComponent(pnlResults);
        pnlResults.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.pnResults.ACC_name")); // NOI18N

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
        jSplitPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.jsplintPane1.ACC_name")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.main.ACC_name")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        invokeMethod();
    }//GEN-LAST:event_btnSubmitActionPerformed
    private void invokeMethod() {
        /**
         *  Steps to call the method.
         *  1. Get the parameter values from the tree
         *  2. Get the client wrapper class
         *  3. Get the method.
         *  4. call the Method with the parameter values
         *  5. Display the return value.
         */

        /**
         * Get the parameter values from the tree.  The parameters will be the children of the root node only. Any children
         * of the parameter nodes are values used to derive the parameter values.  This means only the first children of the root
         * node will be used a parameters.  The logic to "roll-up" a parameter value is left to the TypeCellEditor class.
         */

        /**
         * Use a LinkedList because we care about the order of the parameters.
         */
        LinkedList<Object> paramList = new LinkedList<Object>();
        for(int ii=0; null != this.getParamterRootNode() && ii < this.getParamterRootNode().getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) this.getParamterRootNode().getChildAt(ii);
            TypeNodeData nodeData = (TypeNodeData)childNode.getUserObject();
            Object parameterValue = nodeData.getTypeValue();

            paramList.add(parameterValue);
        }

        /**
         * specify the wrapper client class name for this method.
         */
        String clientClassName;
        if(isRPCEncoded(wsData.getWsdlData())){
            clientClassName = wsData.getWsdlModel().getJavaName() + "_Impl";
        }else{
           clientClassName = wsData.getWsdlModel().getJavaName();
        }

        /**
         * Fix for Bug: 6217545
         * We need to run the method in a separate thread so the user can cancel if the method call
         * locks up.
         * First we need to create the thread, then register for a listener so we can get notified when the method's
         * finished.
         * -David Botterill 1/14/2005
         */
        methodTask = new MethodTask(clientClassName,paramList,this.method,this.getRuntimeClassLoader());

        methodTask.registerListener(this);

        Thread methodThread = new Thread(methodTask);

        methodThread.start();
    }
    
    @Override
    public void methodFinished(final Object inReturnedObject,final LinkedList inParamList) {
        if ( SwingUtilities.isEventDispatchThread()){
            doMethodFinished(inReturnedObject, inParamList);
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                
                @Override
                public void run() {
                    doMethodFinished(inReturnedObject, inParamList);
                }
            });
        }
    }

    private void doMethodFinished(Object inReturnedObject, List inParamList) {
        dialog.setCursor(normalCursor);

        showResults(inReturnedObject);

        /**
         * Fix for Bug#: 5059732
         * Now we need to also set the parameter values in the tree nodes since they may have changed due
         * to the support for pass by reference ("Holders").
         * - David Botterill 8/12/2004
         */

        for(int ii=0; null != this.getParamterRootNode() && ii < this.getParamterRootNode().getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) this.getParamterRootNode().getChildAt(ii);
            TypeNodeData nodeData = (TypeNodeData)childNode.getUserObject();
            nodeData.setTypeValue(inParamList.get(ii));
            /**
             * We really only care about Holder types from here since they are the only type of parameter that
             * can have the value changed by the endpoint service.
             */
            String topNodeType = nodeData.getTypeClass();
            if(ReflectionHelper.isHolder(topNodeType)) {
                ((ParameterTreeNode)childNode).updateChildren();
            }
        }
        /**
         * Update the table since we may have changed some tree node values.
         */
        parameterOutline.tableChanged(new TableModelEvent(parameterOutline.getOutlineModel()));

    }

    private void showResults(Object inResultObject) {
        /**
         * Create a tree of the result object types.
         */
        try {
            resultOutline = loadResultTreeTable(this.method, inResultObject);
            resultOutline.getTableHeader().setReorderingAllowed(false);
            resultOutline.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.resultOutline.ACC_name"));
            resultOutline.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.resultOutline.ACC_desc"));
            addFocusListener(resultOutline);

            lblResults.setLabelFor(resultOutline);

            scrollPaneResults.setViewportView(resultOutline);
        }catch (WebServiceReflectionException ex) {
                        Throwable cause = ex.getCause();
            ErrorManager.getDefault().notify(cause);
            ErrorManager.getDefault().log(this.getClass().getName() + ": WebServiceReflectionException=" + cause);
        }
    }


    private void myInitComponents() {
        okButton.setText(okString);

        /**
         * Now set up the Nodes for the TreeTableView
         */
        if(null == this.method) {
            return;
        }

        try {
            NodeHelper.createInstance(getRuntimeClassLoader());

            parameterOutline = loadParameterTreeTable(this.method);

            // Turn off the reordering
            /**
             * Add it to the correct Panel.
             */

            scrollPaneParameter.setViewportView(parameterOutline);

            /**
             * Set up Accessibility stuff for not UI-Editor stuff.
             *
             */

            okButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.okButton.ACC_name"));
            okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.okButton.ACC_desc"));
            okButton.setMnemonic(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.okButton.ACC_mnemonic").charAt(0));

            parameterOutline.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.parameterOutline.ACC_name"));
            parameterOutline.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TestWebServiceMethodDlg.class, "TestWebServiceMethodDlg.parameterOutline.ACC_desc"));
            lblParameters.setLabelFor(parameterOutline);
            addFocusListener(parameterOutline);
        }catch (WebServiceReflectionException ex) {
                        Throwable cause = ex.getCause();
            ErrorManager.getDefault().notify(cause);
            ErrorManager.getDefault().log(this.getClass().getName() + ": WebServiceReflectionException=" + cause);
        }
    }

    private void addFocusListener(final JTable table) {
        // fixes tab cycle when the table is empty
        table.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent evt) {
                Container cycleRoot = table.getFocusCycleRootAncestor();
                FocusTraversalPolicy policy = table.getFocusTraversalPolicy();
                if (policy == null && cycleRoot != null) {
                    policy = cycleRoot.getFocusTraversalPolicy();
                }

                if (table.getRowCount() == 0 && policy != null) {
                    Component target = policy.getComponentAfter(cycleRoot, table);
                    if (target != null && target == evt.getOppositeComponent()) {
                        target = policy.getComponentBefore(cycleRoot, table);
                    }

                    if (target != null) {
                        target.requestFocusInWindow();
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent evt) {
            }
        });
    }

    private DefaultMutableTreeNode getParamterRootNode() {
        return parameterRootNode;
    }

    private DefaultMutableTreeNode getResultRootNode() {
        return resultRootNode;
    }

    private void setResultRootNode(DefaultMutableTreeNode inNode) {
        resultRootNode = inNode;
    }

    private Outline loadResultTreeTable(JavaMethod inMethod, Object inResultObject) throws WebServiceReflectionException {
        if(null == inMethod) {
            return null;
        }
        JavaType currentType = inMethod.getReturnType();
        String typeName = currentType.getRealName();
        TypeNodeData data = ReflectionHelper.createTypeData(typeName, inResultObject);

        DefaultMutableTreeNode node = NodeHelper.getInstance().createResultNodeFromData(data);

        /**
         * Make sure to create a new result root each time since the user can change the parameters and submit many
         * times.
         */
        this.setResultRootNode(new DefaultMutableTreeNode());
        /**
         *  Add it to the root.
         */
        this.getResultRootNode().add(node);

        DefaultTreeModel treeModel = new DefaultTreeModel(this.getResultRootNode());
        RowModel rowModel = new ResultRowModel();
        OutlineModel outlineModel = DefaultOutlineModel.createOutlineModel(treeModel,
                rowModel, false,NbBundle.getMessage(this.getClass(), 
                "TYPE_COLUMN_NAME"));
        Outline returnOutline = new Outline(outlineModel);
        ResultCellEditor cellEditor = new ResultCellEditor();
        returnOutline.setDefaultEditor(Object.class,cellEditor);
        returnOutline.setRootVisible(false);

        returnOutline.setRenderDataProvider(new TypeDataProvider());

        return returnOutline;
    }

    private Outline loadParameterTreeTable(JavaMethod inMethod) throws WebServiceReflectionException {
        if(null == inMethod) {
            return null;
        }

        List<JavaParameter> parameters = inMethod.getParametersList();
        for (JavaParameter currentParameter : parameters) {
            /**
             * Add all Parameter's to the root tree node.
             */
            JavaType currentType = currentParameter.getType();

            String typeName = currentType.getRealName();
            String typeParamName = currentParameter.getName();

            if (currentParameter.isHolder()) {
                typeName = "javax.xml.ws.Holder<" + typeName + ">"; // NOI18N
            }

            TypeNodeData data = ReflectionHelper.createTypeData(typeName, typeParamName);
            data.setTypeValue(NodeHelper.getInstance().getParameterDefaultValue(data));
            if (currentParameter.isHolder()) {
                if (currentParameter.isIN()) data.setHolderType(TypeNodeData.IN);
                if (currentParameter.isOUT()) data.setHolderType(TypeNodeData.OUT);
                if (currentParameter.isINOUT()) data.setHolderType(TypeNodeData.IN_OUT);
            }

            DefaultMutableTreeNode node = NodeHelper.getInstance().createNodeFromData(data);

            /**
             *  Add it to the root.
             */
            this.getParamterRootNode().add(node);
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(this.getParamterRootNode());
        RowModel rowModel = new TypeRowModel(this.getRuntimeClassLoader());
        OutlineModel outlineModel = DefaultOutlineModel.createOutlineModel(
                treeModel,rowModel, false,NbBundle.getMessage(this.getClass(), 
                "TYPE_COLUMN_NAME"));       // NOI18N
        Outline returnOutline = new Outline(outlineModel);
        TypeCellEditor cellEditor = new TypeCellEditor(getRuntimeClassLoader());
        returnOutline.setDefaultEditor(Object.class,cellEditor);
        returnOutline.setRootVisible(false);
        returnOutline.setRenderDataProvider(new TypeDataProvider());
        /**
         * Fix Bug 5052705.  This setting will cause the cells values to take affect when
         * the focus is lost.  This will remove the requirement of hitting "ENTER" after
         * entering a value in a cell to get the value to take affect.
         */
        returnOutline.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N

        return returnOutline;
    }


    @Override
    public void actionPerformed(ActionEvent evt) {
        String actionCommand = evt.getActionCommand();
        if(actionCommand.equalsIgnoreCase(okString)) {
            okButtonAction(evt);
        }
    }

    private void okButtonAction(ActionEvent evt) {
        /**
         * If the MethodTask is not null, the MethodTask
         * thread may still be running so we need to tell
         * it we've cancelled.
         */
        if(null != methodTask) {
            methodTask.cancel();
        }
        dialog.setCursor(normalCursor);
        dialog.dispose();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lblParameters;
    private javax.swing.JLabel lblResults;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlLabel;
    private javax.swing.JPanel pnlParameter;
    private javax.swing.JPanel pnlResults;
    private javax.swing.JScrollPane scrollPaneParameter;
    private javax.swing.JScrollPane scrollPaneResults;
    // End of variables declaration//GEN-END:variables

    private final JButton okButton = new JButton();
    private Outline parameterOutline;
    private Outline resultOutline;
    private Cursor normalCursor;

    class MethodTask implements Runnable {

        private final String clientClassName;
        private final LinkedList paramList;
        private final JavaMethod javaMethod;
        private final URLClassLoader urlClassLoader;
        private final List<MethodTaskListener> listeners = new ArrayList<>();
        private boolean cancelled=false;


        MethodTask(String inClientClassName, LinkedList inParamList, JavaMethod inJavaMethod,
                URLClassLoader inURLClassLoader) {
            clientClassName = inClientClassName;
            paramList = inParamList;
            javaMethod = inJavaMethod;
            urlClassLoader = inURLClassLoader;
        }

        public void registerListener(MethodTaskListener inListener) {
            if(!listeners.contains(inListener)) {
                listeners.add(inListener);
            }
        }

        private void notifyListeners(Object returnedObject) {
            Iterator<MethodTaskListener> listenerIterator = listeners.iterator();
            while(listenerIterator.hasNext()) {
                MethodTaskListener currentListener = listenerIterator.next();
                currentListener.methodFinished(returnedObject, paramList);
            }
        }

        @Override
        public void run() {
            /**
             * Now invoke the method using the ReflectionHelper.
             */
            Object returnObject;
            try {
                returnObject = ReflectionHelper.callMethodWithParams(clientClassName, paramList, javaMethod,urlClassLoader, wsData.getWsdlData(), port);
            } catch (Exception wsre) {
                if(!cancelled) {
                    Throwable exception = wsre;
                    if (wsre.getCause() instanceof java.lang.reflect.InvocationTargetException) {
                        exception = wsre.getCause();
                    }
                    MethodExceptionDialog errorDialog = new MethodExceptionDialog(exception);
                    /**
                     * Notify the listeners so the cursor will be reset;
                     */
                    notifyListeners(null);
                    errorDialog.showDialog(btnSubmit);
                }
                return;
            }

            notifyListeners(returnObject);
        }

        public void cancel() {
            cancelled=true;
        }
    }

    private static class BusyMouseAdapter extends MouseAdapter {
        private final Cursor normalCursor;

        public BusyMouseAdapter(Cursor inNormalCursor) {
            normalCursor = inNormalCursor;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            e.getComponent().setCursor(normalCursor);
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}
