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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

        return names.toArray(new String[names.size()]);
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

        return types.toArray(new Class[types.size()]);
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
            Logger.global.log(Level.INFO, "", ex);
        }
        return null;
    }

    public boolean isRPCEncoded() {
        WSDLModel wsdlModel = getXamWsdlModel();
        return Util.isRPCEncoded(wsdlModel);
    }
}
