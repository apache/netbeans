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

package org.netbeans.modules.s2banttask;

import java.io.File;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.modules.schema2beans.Schema2Beans;

import org.netbeans.modules.schema2beansdev.GenBeans;

/**
 * @author Cliff Draper
 * @date 2003/04/02
 * This class is an ant task which front ends
 * org.netbeans.modules.schema2beansdev.GenBeans.  It creates a Config
 * object and fills it in with data from the build script.
 * @deprecated Use {@link Schema2Beans} instead.
 */
@Deprecated
public class Schema2BeansAntTask extends S2bConfigDelegator {
    public Schema2BeansAntTask() {
        super(new GenBeans.Config());
    }
    
    /**
     * We change a few defaults from the normal GenBeans:
     *   -auto
     *   -checkUpToDate
     *   -nogenerateTimeStamp
     */
    public void init() {
        setAuto(true);
        setCheckUpToDate(true);
        setGenerateTimeStamp(false);
    }
    
    public void execute() throws BuildException {
        if (_S2bConfig == null) {
            throw new BuildException("Invoked for the second time!");
        }
        try {
            GenBeans.doIt(getConfig());
        } catch (java.io.IOException e) {
            throw new BuildException(e);
        } catch (org.netbeans.modules.schema2beans.Schema2BeansException e) {
            throw new BuildException(e);
        }
        _S2bConfig = null;
    }

    protected GenBeans.Config getConfig() {
        return (GenBeans.Config) _S2bConfig;
    }

    public void setOutputType(String type) {
        if ("basebean".equalsIgnoreCase(type))
            getConfig().setOutputType(GenBeans.Config.OUTPUT_TRADITIONAL_BASEBEAN);
        else if ("javabeans".equalsIgnoreCase(type))
            getConfig().setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        else
            throw new RuntimeException("Incorrect argument to outputType.  It must be 'javabeans' or 'basebean'.");
    }

    public void setSchema(File type) {
        setFilename(type);
    }

    public void setPackage(String value) {
        setPackagePath(value);
    }

    public void setWriteBeanGraph(File bg) {
        setWriteBeanGraphFile(bg);
    }

    public void setReadBeanGraph(File f) {
        addReadBeanGraphFiles(f);
    }
 
    public void setValidate(boolean v) {
        setGenerateValidate(v);
    }

    public void setComments(boolean value) {
        setProcessComments(value);
    }

    public void setDocType(boolean value) {
        setProcessDocType(value);
    }

    public void setDelegator(boolean value) {
        setGenerateDelegator(value);
    }

    public void setAttrProp(boolean value) {
        setAttributesAsProperties(value);
    }

    public void setCommonInterface(String commonBeanName) {
        setGenerateCommonInterface(commonBeanName);
    }

    public void setPropertyEvents(boolean value) {
        setGeneratePropertyEvents(value);
    }

    public void setMin(boolean value) {
        setMinFeatures(value);
    }
 
    public void setPremium(boolean value) {
        if (value) {
            getConfig().buyPremium();
        }
    }

    public void setStrict(boolean value) {
        if (value) {
            getConfig().useStrict();
        }
    }

    /**
     * Add finders for those in this comman separated list.
     * @param exprList example: on /source-element find class-element by full-name,on /source-element/class-element find method-element by name,on /source-element/class-element/method-element find java-doc by name
     */
    public void setFinder(String exprList) {
        StringTokenizer st = new StringTokenizer(exprList, ",");
        while (st.hasMoreTokens()) {
            String expr = st.nextToken().trim();
            if (expr.equals(""))
                continue;
            addFinder(expr);
        }
    }
}
