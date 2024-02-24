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
package org.netbeans.modules.websvc.saas.codegen.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nam
 */
public class SoapClientOperationInfo {

    private WsdlSaasMethod method;
    private String categoryName;
    private String serviceName;
    private String portName;
    private String operationName;
    private String wsdlUrl;
    private Project project;
    private WsdlData webServiceData;
    private WSService service;
    private WSOperation operation;
    private WSPort port;
    private List<ParameterInfo> headerParams = Collections.emptyList();

    public SoapClientOperationInfo(WsdlSaasMethod m, Project project) {
        this.method = m;
        this.categoryName = m.getSaas().getParentGroup().getName();
        this.serviceName = m.getSaas().getDefaultServiceName();
        this.project = project;

        method.getSaas().toStateReady(true);

        this.webServiceData = method.getSaas().getWsdlData();
        this.portName = method.getWsdlPort().getName();
        this.operationName = method.getWsdlOperation().getName();
        this.wsdlUrl = method.getSaas().getUrl();
        this.service = method.getSaas().getWsdlModel();
        this.port = method.getWsdlPort();
        this.operation = method.getWsdlOperation();
    }

    public Project getProject() {
        return project;
    }

    public WsdlSaasMethod getMethod() {
        return method;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPortName() {
        return portName;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getWsdlURL() {
        return wsdlUrl;
    }

    public String getWsdlLocation() {
        initWsdlModelInfo();
        return webServiceData.getWsdlFile();
    }

    public void initWsdlModelInfo() {
    }

    public static WSOperation findOperationByName(WSPort port, String name) {
        for (Object o : port.getOperations()) {
            if (name.equals(((WSOperation) o).getName())) {
                return ((WSOperation) o);
            }
        }
        return null;
    }

    public WSPort getPort() {
        initWsdlModelInfo();
        return port;
    }

    public WSOperation getOperation() {
        initWsdlModelInfo();
        return operation;
    }

    public WSService getService() {
        initWsdlModelInfo();
        return service;
    }

    //TODO maybe parse SEI class (using Retouche) for @WebParam.Mode annotation
    public List<WSParameter> getOutputParameters() {
        ArrayList<WSParameter> params = new ArrayList<WSParameter>();
        for (WSParameter p : getOperation().getParameters()) {
            if (p.isHolder()) {
                params.add(p);
            }
        }
        return params;
    }

    public static String getParamType(WSParameter param) {
        if (param.isHolder()) {
            String outputType = param.getTypeName();
            int iLT = outputType.indexOf('<');
            int iGT = outputType.indexOf('>');
            if (iLT > 0 || iGT > 0) {
                outputType = outputType.substring(iLT + 1, iGT).trim();
            }
            return outputType;
        } else {
            return param.getTypeName();
        }
    }

    //TODO maybe parse SEI class (using Retouche) for @WebParam.Mode annotation
    public String getOutputType() {
        String outputType = getOperation().getReturnTypeName();
        if (Constants.VOID.equals(outputType)) {
            for (WSParameter p : getOperation().getParameters()) {
                if (p.isHolder()) {
                    outputType = getParamType(p);
                    break;
                }
            }
        }
        return outputType;
    }

    //TODO maybe parse SEI class (using Retouche) for @WebParam.Mode annotation
    public String[] getInputParameterNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (WSParameter p : getOperation().getParameters()) {
            if (!p.isHolder()) {
                names.add(p.getName());
            }
        }

        return names.toArray(new String[0]);
    }

    //TODO maybe parse SEI class (using Retouche) for @WebParam.Mode annotation
    public Class[] getInputParameterTypes() {
        ArrayList<Class> types = new ArrayList<Class>();

        for (WSParameter p : getOperation().getParameters()) {
            if (!p.isHolder()) {
                int repeatCount = 0;
                Class type = null;

                // This is a hack to wait for the complex type to become
                // available. We will give up after 120 seconds.
                synchronized (this) {
                    try {
                        while (repeatCount < 60) {
                            type = getType(project, p.getTypeName());

                            if (type != null) {
                                break;
                            }

                            repeatCount++;
                            this.wait(2000);
                        }
                    } catch (InterruptedException ex) {
                    }
                }

                // RESOLVE:
                // Need to failure gracefully by displaying an error dialog.
                // For now, set it to Object.class.
                if (type == null) {
                    type = Object.class;
                }

                types.add(type);
            }
        }

        return types.toArray(new Class[0]);
    }

    public Class getType(Project project, String typeName) {
        return Util.getType(project, typeName);
    }

    public boolean needsSoapHandler() {
        return getSoapHeaderParameters().size() > 0;
    }

    public List<ParameterInfo> getSoapHeaderParameters() {
        return headerParams;
    }

    public WSDLModel getXamWsdlModel() {
        try {
            FileObject wsdlFO = FileUtil.toFileObject(new File(webServiceData.getWsdlFile()));
            ;
            return WSDLModelFactory.getDefault().getModel(Utilities.createModelSource(wsdlFO, true));
        } catch (CatalogModelException ex) {
            Logger.getGlobal().log(Level.INFO, "", ex);
        }
        return null;
    }

    public boolean isRPCEncoded() {
        WSDLModel wsdlModel = getXamWsdlModel();
        return Util.isRPCEncoded(wsdlModel);
    }
}
