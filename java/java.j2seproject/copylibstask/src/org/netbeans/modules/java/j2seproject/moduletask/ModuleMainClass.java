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
package org.netbeans.modules.java.j2seproject.moduletask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.modules.java.j2seproject.moduletask.classfile.Attribute;
import org.netbeans.modules.java.j2seproject.moduletask.classfile.ClassFile;
import org.netbeans.modules.java.j2seproject.moduletask.classfile.ConstantPool;

/**
 *
 * @author Tomas Zezula
 */
public final class ModuleMainClass extends Task {
    private static final String ATTR_MODULE_MAIN_CLZ = "ModuleMainClass";   //NOI18N
    private String mainClass;
    private File moduleInfo;
    private boolean failOnError;

    public ModuleMainClass() {
        failOnError = true;
    }

    public String getMainclass() {
        return mainClass;
    }

    public void setMainclass(final String mainClass) {
        this.mainClass = mainClass;
    }

    public File getModuleinfo() {
        return moduleInfo;
    }

    public void setModuleinfo(final File moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public boolean isFailonerror() {
        return failOnError;
    }

    public void setFailonerror(final boolean value) {
        this.failOnError = value;
    }

    @Override
    public void execute() throws BuildException {
        if (mainClass == null) {
            final String msg = "MainClass must be set.";             //NOI18N
            if (failOnError) {
                throw new BuildException(msg);
            } else {
                getProject().log(msg, Project.MSG_WARN);
                return;
            }
        }
        if (moduleInfo == null) {
            final String msg = "ModuleInfo must be set.";   //NOI18N
            if (failOnError) {
                throw new BuildException(msg);
            } else {
                getProject().log(msg, Project.MSG_WARN);
                return;
            }
        }
        if (!moduleInfo.canRead()) {
            final String msg = "MainClass must be readable.";   //NOI18N
            if (failOnError) {
                throw new BuildException(msg);
            } else {
                getProject().log(msg, Project.MSG_WARN);
                return;
            }
        }
        if (!moduleInfo.canWrite()) {
            final String msg = "MainClass must be writable.";   //NOI18N
            if (failOnError) {
                throw new BuildException(msg);
            } else {
                getProject().log(msg, Project.MSG_WARN);
                return;
            }
        }
        try {
            ClassFile cf = null;
            try (InputStream in = Files.newInputStream(moduleInfo.toPath())) {
                cf = new ClassFile(in);
                final ConstantPool cp = cf.getConstantPool();
                final int attrNameIndex = cp.add(new ConstantPool.CPUtf8(cp, ATTR_MODULE_MAIN_CLZ));
                final int classNameIndex = cp.add(new ConstantPool.CPUtf8(cp, internalName(mainClass)));
                final int classIndex = cp.add(new ConstantPool.CPClass(cp, classNameIndex));
                final byte[] data = new byte[2];
                data[0] = (byte) (classIndex >>> 8);
                data[1] = (byte) classIndex;
                final Attribute[] attrs = cf.getAttributes();
                int toDelete = -1;
                for (int i = 0; i < attrs.length; i++) {
                    if (attrs[i].getNameIndex() == attrNameIndex) {
                        toDelete = i;
                        break;
                    }
                }
                if (toDelete != -1) {
                    cf.removeAttribute(toDelete);
                }
                cf.addAttribute(new Attribute(attrNameIndex, data));
            }
            if (cf != null) {
                try (OutputStream out = Files.newOutputStream(moduleInfo.toPath())) {
                    cf.write(out);
                }
            }
        } catch (IOException | IllegalArgumentException ioe) {
            if (failOnError) {
                throw new BuildException(ioe);
            } else {
                getProject().log(ioe.getMessage(), Project.MSG_WARN);
            }
        }
    }

    private static String internalName(final String externalName) {
        return externalName.replace('.', '/');  //NOI18N
    }
}
