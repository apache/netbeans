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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.CustomSectionNodePanel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.ReferencesNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;


/**
 * @author Peter Williams
 */
public abstract class NamedBeanGroupNode extends BaseSectionNode implements BeanResolver, DescriptorReader {

    public static final String STANDARD_SERVLET_NAME = Servlet.SERVLET_NAME; // e.g. "ServletName"
    public static final String STANDARD_EJB_NAME = Ejb.EJB_NAME; // e.g. "EjbName"
    public static final String STANDARD_EJB_REF_NAME = EjbRef.EJB_REF_NAME; // e.g. "EjbRefName"
    public static final String STANDARD_RES_REF_NAME = ResourceRef.RES_REF_NAME; // e.g. "ResourceRefName"
    public static final String STANDARD_RESOURCE_ENV_REF_NAME = ResourceEnvRef.RESOURCE_ENV_REF_NAME; // e.g. "ResourceEnvRefName"
    public static final String STANDARD_SERVICE_REF_NAME = ServiceRef.SERVICE_REF_NAME; // e.g. "ServiceRefName"
    public static final String STANDARD_ROLE_NAME = SecurityRoleMapping.ROLE_NAME; // e.g. "RoleName"
    public static final String STANDARD_PORTCOMPONENT_NAME = WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME; // e.g. "WebserviceDescriptionName"
    public static final String STANDARD_WEBSERVICE_DESC_NAME = WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME; // e.g. "WebserviceDescriptionName"
    public static final String STANDARD_MSGDEST_NAME = MessageDestination.MESSAGE_DESTINATION_NAME; // e.g. "MessageDestination"
    public static final String STANDARD_MSGDEST_REF_NAME = MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME; // e.g. "MessageDestinationRef"
    public static final String STANDARD_PORTCOMPONENT_REF_NAME = "PortComponentRef"; // NOI18N
    
    // Prefixes for default bean names when new beans are created by the user.
    protected static final String PFX_ROLE = "role"; // NOI18N
    protected static final String PFX_SERVLET = "servlet"; // NOI18N
    protected static final String PFX_EJB = "ejb"; // NOI18N
    protected static final String PFX_SERVICE = "service"; // NOI18N
    protected static final String PFX_ENDPOINT = "endpoint"; // NOI18N
    protected static final String PFX_DESTINATION = "destination"; // NOI18N
    protected static final String PFX_EJB_REF = "ejb_ref"; // NOI18N
    protected static final String PFX_RESOURCE_ENV_REF = "resource_env_ref"; // NOI18N
    protected static final String PFX_RESOURCE_REF = "resource_ref"; // NOI18N
    protected static final String PFX_DESTINATION_REF = "destination_ref"; // NOI18N
    protected static final String PFX_SERVICE_REF = "service_ref"; // NOI18N
    
    private static RequestProcessor processor = new RequestProcessor("SunDDNodeBuilder", 1);
    
    protected CommonDDBean commonDD;
    private String beanNameProperty;
    private Class beanClass;
    private AddBeanAction addBeanAction;
    
    private volatile boolean doCheck = false;
    private volatile boolean checking = false;

    private AtomicInteger newBeanId = new AtomicInteger(1);
    
    public NamedBeanGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD,
            String beanNameProperty, Class beanClass, String header, String iconBase,
            ASDDVersion version) {
        super(sectionNodeView, new NamedChildren(), commonDD, version, header, iconBase);
        
        this.commonDD = commonDD;
        this.beanNameProperty = beanNameProperty;
        this.beanClass = beanClass;
        
        setExpanded(true);
    }
    
    /** Expected to be called from derived class constructor, if needed.
     */
    protected void enableAddAction(String addActionTitle) {
        addBeanAction = new AddBeanAction(addActionTitle);
    }
    
    protected abstract SectionNode createNode(DDBinding binding);
    
    protected abstract CommonDDBean [] getBeansFromModel();
    
    protected abstract CommonDDBean addNewBean();

    protected abstract CommonDDBean addBean(CommonDDBean newBean);
    
    protected abstract void removeBean(CommonDDBean bean);
    
    /** This method determines whether a given sun DD event source is one this
     *  group should listen to.  Generally this means the source is equivalent
     *  to the sun DD object owned by this group.
     * 
     *  Override this when event source matching is not this simple (See
     *  EjbGroupNode.java).
     */
    protected boolean isEventSource(Object source) {
        return source == commonDD || 
                (source instanceof RootInterface && commonDD instanceof RootInterface &&
                ((RootInterface) commonDD).isEventSource((RootInterface) source));
    }
    
    @Override
    public SectionNodeInnerPanel createInnerPanel() {
        SectionNodeView sectionNodeView = getSectionNodeView();
        BoxPanel boxPanel = new BoxPanel(sectionNodeView) {
            @Override
            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
//                System.out.println(NamedBeanGroupNode.this.getClass().getSimpleName() + ".BoxPanel.dataModelPropertyChange: " + 
//                        source + ", " + propertyName + ", " + oldValue + ", " + newValue);
                // Check for matching bean first, then check for matching event source.
                if(newValue == null && beanClass.isInstance(oldValue) || 
                        oldValue == null && beanClass.isInstance(newValue)) {
                    if(isEventSource(source)) {
                        checkChildren((CommonDDBean) newValue);
                    }
                }
            }
            
            @Override
            public void refreshView() {
                checkChildren(null);
            }
        };
        boxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        populateBoxPanel(boxPanel);
        return boxPanel;
    }
    
    @Override
    public SectionNodePanel getSectionNodePanel() {
        SectionNodePanel nodePanel = super.getSectionNodePanel();
        if(addBeanAction != null && nodePanel.getHeaderButtons() == null) {
            nodePanel.setHeaderActions(new Action [] { addBeanAction });
        }
        return nodePanel;
    }
    
    @Override
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        SectionNodeInnerPanel innerPanel = super.createNodeInnerPanel();
        return innerPanel;
    }
    
    @Override
    protected SectionNodePanel createSectionNodePanel() {
        return new CustomSectionNodePanel(this);
    }    
    
    public void checkChildren(final CommonDDBean focusBean) {
//        System.out.println(this.getClass().getSimpleName() + ".checkChildren( " + focusBean + " )");
        processor.post(new Runnable() {
            public void run() {
                // Compute dataset
                final SortedSet<DDBinding> bindingDataSet = computeBindingSet();
                
                // Notify AWT for UI update.
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        doCheck = true;
                        if (setChecking(true)) {
                            try {
                                while (doCheck) {
                                    doCheck = false;
                                    check(focusBean, bindingDataSet);
                                }
                            } finally {
                                setChecking(false);
                            }
                        }
                    }
                });
            }
        });
    }
    
    // !PW FIXME was private, change back soon
    protected synchronized boolean setChecking(boolean value) {
        if (value) {
            if (checking) {
                return false;
            } else {
                checking = true;
                return true;
            }
        } else {
            checking = false;
            return true;
        }
    }

    /** This method actually updates the display nodes in the UI.  It should not
     *  do any slow calculation and should only be called on AWT thread.
     */
    protected void check(final CommonDDBean focusBean, SortedSet<DDBinding> bindingDataSet) {
        assert SwingUtilities.isEventDispatchThread();
        
        Map<Object, Node> nodeMap = new HashMap<Object, Node>();
        Children children = getChildren();
        Node[] nodes = children.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            nodeMap.put(((SectionNode) node).getKey(), node);
            
            if(node instanceof NamedBeanNode) {
                NamedBeanNode nbn = (NamedBeanNode) node;
                DDBinding nodeBinding = nbn.getBinding();
                
                if(!nodeBinding.isVirtual()) {
                    // Nonvirtual nodes should alread be handled... 
                    // (barring obscure race conditions at least.)
                    continue;
                }
                
                CommonDDBean nodeSunBean = nodeBinding.getSunBean();
                
                // Attempt to normalize sunbeans for virtual nodes.
                // *** N^2 algorithm - find a better way...
                for(DDBinding candidate: bindingDataSet) {
                    CommonDDBean candidateSunBean = candidate.getSunBean();
                    if(nodeSunBean == candidateSunBean) {
                        break; 
                    }

                    if(!candidate.isVirtual()) {
                        continue;
                    }
                    
                    if(Utils.strEquivalent(nodeBinding.getBeanName(), candidate.getBeanName())) {
                        DDBinding replacement = candidate.rebind(nodeSunBean);
                        bindingDataSet.remove(candidate);
                        bindingDataSet.add(replacement);
                        break;
                    }
                }
            }
        }
       
        // !PW Optimization - How to match virtual servlets from prior pass with virtual servlets from this pass?
        // Currently their keys will always be created new.  Can we look them up?
        
        SectionNode focusNode = null;
        boolean dirty = nodes.length != bindingDataSet.size();
        List<Node> newNodeList = new ArrayList<Node>(bindingDataSet.size());
        
        int index = 0;
        Iterator<DDBinding> setIter = bindingDataSet.iterator();
        while(setIter.hasNext()) {
            DDBinding binding = setIter.next();
            SectionNode node = (SectionNode) nodeMap.get(binding.getSunBean());
            // if the node is null (didn't exist before) or the node has different binding, then (re)create it.
            if(node == null || (node instanceof NamedBeanNode && !binding.equals(((NamedBeanNode) node).getBinding()))) {
                node = createNode(binding);
                dirty = true;
            }
            newNodeList.add(node);
            if(!dirty) {
                dirty = ((SectionNode) nodes[index]).getKey() != node.getKey();
            }
            if(binding.getSunBean() == focusBean) {
                focusNode = node;
            }
            index++;
        }
        
        if (dirty) {
            Node [] newNodes = newNodeList.toArray(new Node[0]);
            children.remove(nodes);
            children.add(newNodes);
            populateBoxPanel();
        }
        
        if(focusBean != null && focusNode != null) {
            final SectionNodePanel nodePanel = focusNode.getSectionNodePanel();
            nodePanel.open();
            nodePanel.scroll();

            // XXX hack to work around focus bug on reference panel.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    nodePanel.setActive(true);
                }
            });
        }
    }
    
    protected SortedSet<DDBinding> computeBindingSet() {
        CommonDDBean [] sunBeans = getBeansFromModel();
        Map<String, Object> stdBeanPropertyMap = readDescriptor();
        Map<String, Object> annotationPropertyMap = readAnnotations();
        
        return computeBindingSet(sunBeans, stdBeanPropertyMap, annotationPropertyMap);
    }
    
    @SuppressWarnings("unchecked")
    protected SortedSet<DDBinding> computeBindingSet(CommonDDBean [] sunBeans, 
            Map<String, Object> stdBeanPropertyMap, Map<String, Object> annotationPropertyMap) {
        SortedSet<DDBinding> bindingDataSet = new TreeSet<DDBinding>();

        // Match up like names
        if(sunBeans != null) {
            for(CommonDDBean sunBean: sunBeans) {
                String beanName = getBeanName(sunBean);
                beanName = (beanName != null) ? beanName.trim() : beanName;

                Map<String, Object> stdBeanProperties = null;
                if(stdBeanPropertyMap != null) {
                    Object value = stdBeanPropertyMap.get(beanName);
                    if(value != null) {
                        if(value instanceof Map<?, ?>) {
                            stdBeanProperties = (Map<String, Object>) value;
                            stdBeanPropertyMap.remove(beanName);
                        }
                    }
                }

                Map<String, Object> annotationProperties = null;
                if(annotationPropertyMap != null) {
                    Object value = annotationPropertyMap.get(beanName);
                    if(value != null) {
                        if(value instanceof Map<?, ?>) {
                            annotationProperties = (Map<String, Object>) value;
                            annotationPropertyMap.remove(beanName);
                        }
                    }
                }

                DDBinding binding = new DDBinding(this, sunBean, stdBeanProperties, annotationProperties, false);
                bindingDataSet.add(binding);
            }
        }
        
        // Add dummy entries for all unmatched standard servlets (unmatched sun servlets were added previous step)
        if(stdBeanPropertyMap != null) {
            Set<Map.Entry<String, Object>> entrySet = stdBeanPropertyMap.entrySet();
            for(Map.Entry<String, Object> entry: entrySet) {
                String beanName = entry.getKey();
                Object value = entry.getValue();
                if(value != null) {
                    if(value instanceof Map<?, ?>) {
                        Map<String, Object> stdBeanProperties = (Map<String, Object>) value;
                        CommonDDBean newSunBean = createBean();
                        setBeanName(newSunBean, beanName);

                        Map<String, Object> annotationProperties = null;
                        if(annotationPropertyMap != null) {
                            value = annotationPropertyMap.get(beanName);
                            if(value != null) {
                                if(value instanceof Map<?, ?>) {
                                    annotationProperties = (Map<String, Object>) value;
                                    annotationPropertyMap.remove(beanName);
                                }
                            }
                        }

                        DDBinding binding = new DDBinding(this, newSunBean, stdBeanProperties, annotationProperties, true);
                        bindingDataSet.add(binding);
                    }
                }
            }
        }

        // Add dummy entries for all unmatched standard servlets (unmatched sun servlets were added previous step)
        if(annotationPropertyMap != null) {
            Set<Map.Entry<String, Object>> entrySet = annotationPropertyMap.entrySet();
            for(Map.Entry<String, Object> entry: entrySet) {
                String beanName = entry.getKey();
                Object value = entry.getValue();
                if(value != null) {
                    if(value instanceof Map<?, ?>) {
                        Map<String, Object> annotationProperties = (Map<String, Object>) value;
                        CommonDDBean newSunBean = createBean();
                        setBeanName(newSunBean, beanName);

                        DDBinding binding = new DDBinding(this, newSunBean, null, annotationProperties, true);
                        bindingDataSet.add(binding);
                    }
                }
            }
        }
        
        // !PW FIXME Mix annotations into previous calculations if we had them (none for servlet, but what about @RunAs?)
        // Possibly only consider annotations on bound servlets?  Then we can look at specific servlet class for annotations
        // using <servlet-class> field in standard descriptor.
        
        return bindingDataSet;
    }
    
    protected <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        MetadataModel<T> metadataModel = null;
        SectionNodeView view = getSectionNodeView();
        XmlMultiViewDataObject dObj = view.getDataObject();
        GlassfishConfiguration config = GlassfishConfiguration.getConfiguration(
                FileUtil.toFile(dObj.getPrimaryFile()));
        if(config != null) {
            metadataModel = config.getMetadataModel(type);
        }
        return metadataModel;
    }

    protected org.netbeans.modules.j2ee.dd.api.common.RootInterface getStandardRootDD() {
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = null;
        SectionNodeView view = getSectionNodeView();
        XmlMultiViewDataObject dObj = view.getDataObject();
        GlassfishConfiguration config = GlassfishConfiguration.getConfiguration(
                FileUtil.toFile(dObj.getPrimaryFile()));
        if(config != null) {
            stdRootDD = config.getStandardRootDD();
        }
        return stdRootDD;
    }

    protected org.netbeans.modules.j2ee.dd.api.common.RootInterface getWebServicesRootDD() {
        org.netbeans.modules.j2ee.dd.api.common.RootInterface wsRootDD = null;
        SectionNodeView view = getSectionNodeView();
        XmlMultiViewDataObject dObj = view.getDataObject();
        GlassfishConfiguration config = GlassfishConfiguration.getConfiguration(
                FileUtil.toFile(dObj.getPrimaryFile()));
        if(config != null) {
            wsRootDD = config.getWebServicesRootDD();
        }
        return wsRootDD;
    }
    
    public String getNewBeanId(String prefix) {
        String newId;
        int count = 0;

        // Create unique ID, but not too many tries (avoid infinite or long loops).
        do {
            newId = prefix + newBeanId.getAndIncrement();
        } while(getChildren().findChild(newId) != null && ++count < 100);
        
        return newId;
    }
    
    // ------------------------------------------------------------------------
    // DescriptorReader implementation
    // ------------------------------------------------------------------------
    public Map<String, Object> readDescriptor() {
        CommonBeanReader reader = getModelReader();
        return reader != null ? reader.readDescriptor(getStandardRootDD()) : null;
    }

    public Map<String, Object> readAnnotations() {
        Map<String, Object> result = null;
        CommonBeanReader reader = getModelReader();
        if(reader != null) {
            SectionNodeView view = getSectionNodeView();
            XmlMultiViewDataObject dObj = view.getDataObject();
            result = reader.readAnnotations(dObj);
        }
        return result;
    }
    
    protected CommonBeanReader getModelReader() {
        return null;
    }
    
    /** Determines the name of the parent node, if any.  For example, determines the
     *  name of an ejb that an resource-ref or other named reference is embedded in.
     */
    protected String getParentNodeName() {
        String parentName = null;
        Node parentNode = getParentNode();
        // Hack to bypass the references group node, but then this entire method is a hack.
        if(parentNode instanceof ReferencesNode) {
            parentNode = parentNode.getParentNode();
        }
        if(parentNode instanceof NamedBeanNode) {
            DDBinding binding = ((NamedBeanNode) parentNode).getBinding();
            if(binding != null) {
                parentName = binding.isBound() ? binding.getBindingName() : binding.getBeanName();
                if(parentName == null || parentName.length() == 0) {
                    parentName = null;
                }
            }
        }
        return parentName;
    }
    
    protected NamedBeanGroupNode getParentGroupNode() {
        NamedBeanGroupNode parentGroupNode = null;
        Node parentNode = getParentNode();
        // Hack to bypass the references group node, but then this entire method is a hack.
        if(parentNode instanceof ReferencesNode) {
            parentNode = parentNode.getParentNode();
        }
        if(parentNode instanceof NamedBeanNode) {
            parentNode = parentNode.getParentNode();
            if(parentNode instanceof NamedBeanGroupNode) {
                parentGroupNode = (NamedBeanGroupNode) parentNode;
            }
        }
        return parentGroupNode;
    }
    
    /** -----------------------------------------------------------------------
     * AddBeanAction implementation
     */
    public final class AddBeanAction extends AbstractAction {
        
        public AddBeanAction(String actionText) {
            super(actionText);
//            char mnem = NbBundle.getMessage(PortInfoGroupNode.class, "MNE_Add" + resourceBase).charAt(0);
//            putValue(MNEMONIC_KEY, Integer.valueOf(mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            SectionNodeView view = getSectionNodeView();
            if(view instanceof DDSectionNodeView) {
                XmlMultiViewDataObject dObj = ((DDSectionNodeView) view).getDataObject();
                if(dObj instanceof SunDescriptorDataObject) {
                    Node parent = getParentNode();
                    if(parent instanceof ReferencesNode) {
                        parent = ((ReferencesNode) parent).getParentNode();
                    }
                    if(parent instanceof NamedBeanNode) { // ejb parent node
                        ((NamedBeanNode) parent).addVirtualBean();
                    }
                    addNewBean();
                    
                    SunDescriptorDataObject sunDO = (SunDescriptorDataObject) dObj;
                    sunDO.modelUpdatedFromUI();
                    // sunDO.setChangedFromUI(true);
                }
            }
        }
    }
    
    public static class NamedChildren extends Children.SortedMap<DDBinding> implements Comparator<Node> {

        public NamedChildren() {
            setComparator(this);
        }

        @Override
        public Node findChild(String name) {
            Node result = null;
            Node[] list = getNodes();

            if (list.length > 0 && name != null) {
                Node key = new AbstractNode(Children.LEAF);
                key.setName(name);
                int index = Arrays.binarySearch(list, key, this);
                if(index >= 0) {
                    result = list[index];
                }
            }
            
            return result;
        }
        
        @Override
        public boolean add(Node[] arr) {
            java.util.Map<DDBinding, Node> nodeMap = arrayToMap(arr);
            this.putAll(nodeMap);
            return true;
        }

        @Override
        public boolean remove(Node[] arr) {
            java.util.Map<DDBinding, Node> nodeMap = arrayToMap(arr);
            this.removeAll(nodeMap.keySet());
            return true;
        }

        private java.util.Map<DDBinding, Node> arrayToMap(Node [] arr) {
            java.util.Map<DDBinding, Node> nodeMap = new HashMap<DDBinding, Node>();
            for(Node n: arr) {
                if(n instanceof NamedBeanNode) {
                    NamedBeanNode node = (NamedBeanNode) n;
                    nodeMap.put(node.getBinding(), node);
                }
            }
            return nodeMap;
        }

        public int compare(Node n1, Node n2) {
            return Utils.strCompareTo(n1.getName(), n2.getName());
        }
    }
}
