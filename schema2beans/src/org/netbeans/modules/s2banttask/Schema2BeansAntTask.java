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
