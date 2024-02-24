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
package org.netbeans.modules.websvc.saas.codegen.java.support;

import com.sun.source.tree.ClassTree;
import java.io.IOException;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;

/**
 * Copy of j2ee/utilities Util class
 *  
 * TODO: Should move some of the methods into o.n.m.w.r.support.Utils class
 * since that's the package used for sharing all the utility classes.
 * 
 */
public class JavaUtil {

    private static final String JAXB_LIB = "jaxb";     //NOI18N
    
    public static final Modifier[] PUBLIC = new Modifier[] { Modifier.PUBLIC };
    
    public static final Modifier[] PRIVATE = new Modifier[] { Modifier.PRIVATE };
    
    public static final Modifier[] PROTECTED = new Modifier[] { Modifier.PROTECTED };
    
    public static final Modifier[] PRIVATE_STATIC = new Modifier[] {
        Modifier.PRIVATE, Modifier.STATIC };
    
    public static final Modifier[] PUBLIC_STATIC = new Modifier[] {
        Modifier.PUBLIC, Modifier.STATIC
    };
   
    public static final Modifier[] PUBLIC_STATIC_FINAL = new Modifier[] {
        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL
    };

    public static boolean isJava(DataObject d) {
        if (d != null && "java".equals(d.getPrimaryFile().getExt())) //NOI18N
        {
            return true;
        }
        return false;
    }

    /**
     * Returns the SourceGroup of the passesd project which contains the
     * fully-qualified class name.
     */
    public static SourceGroup getClassSourceGroup(Project project, String fqClassName) {
        String classFile = fqClassName.replace('.', '/') + "." + Constants.JAVA_EXT; // NOI18N
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);

        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject classFO = sourceGroup.getRootFolder().getFileObject(classFile);
            if (classFO != null) {
                return sourceGroup;
            }
        }
        return null;
    }

    public static SourceGroup[] getSourceGroups(Project project) {
        SourceGroup[] sourceGroups = null;

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] docRoot = sources.getSourceGroups(Util.TYPE_DOC_ROOT);
        SourceGroup[] srcRoots = SourceGroupSupport.getJavaSourceGroups(project);

        if (docRoot != null && srcRoots != null) {
            sourceGroups = new SourceGroup[docRoot.length + srcRoots.length];
            System.arraycopy(docRoot, 0, sourceGroups, 0, docRoot.length);
            System.arraycopy(srcRoots, 0, sourceGroups, docRoot.length, srcRoots.length);
        }

        if (sourceGroups == null || sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }
        return sourceGroups;
    }

    public static boolean isProjectOpened(Project p) {
        // Workaround: OpenProjects.getDefault() can be null
        // when called from ProjectOpenedHook.projectOpened() upon IDE startup
        if (OpenProjects.getDefault() == null) {
            return true;
        }
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(p)) {
                return true;
            }
        }
        return false;
    }

    public static void addClientJars(SaasBean bean, Project p,
            FileObject target) throws IOException {
        if (bean instanceof RestClientSaasBean) {
            if (p == null || bean == null ||
                    ((RestClientSaasBean) bean).getMethod() == null) {
                throw new IllegalArgumentException(
                        "Cannot create JAXB classes, since project|bean is null.");
            }
            LibrariesHelper.addClientJars(p, target, ((RestClientSaasBean) bean).getMethod().getSaas());
        }
    }

    @Deprecated
    public static void addJaxbLib(Project p) throws IOException {
    }

    public static void addImportsToSource(JavaSource source, List<String> imports) throws IOException {
        for (final String imp : imports) {
            ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);
                    JavaSourceHelper.addImports(copy, new String[]{imp});
                }
            });
            result.commit();
        }
    }

    public static void addInputParamField(JavaSource source,
            final ParameterInfo p, final String[] annotations, final Object[] annotationAttrs) throws IOException {
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree modifiedTree = JavaSourceHelper.addField(copy,
                        initial,
                        PRIVATE,
                        annotations, annotationAttrs,
                        Util.getParameterName(p, true, true, true),
                        p.getTypeName(),
                        Util.getParamValue(p));
                copy.rewrite(initial, modifiedTree);
            }
        });
        result.commit();
    }

    public static void addInputParamFields(JavaSource source,
            final List<ParameterInfo> params) throws IOException {
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                JavaSourceHelper.addFields(copy, Util.getParamNames(params),
                        Util.getParamTypeNames(params), Util.getParamValues(params));
            }
        });
        result.commit();
    }

    public static void addInputParamFields(JavaSource source,
            final List<ParameterInfo> params,
            final javax.lang.model.element.Modifier[] modifier) throws IOException {
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                List<ParameterInfo> addList = new ArrayList<ParameterInfo>();
                for (ParameterInfo p : params) {
                    if (JavaSourceHelper.getField(copy, Util.getParameterName(p, true, true, true)) == null) {
                        addList.add(p);
                    }
                }
                JavaSourceHelper.addFields(copy, Util.getParamNames(addList),
                        Util.getParamTypeNames(addList), Util.getParamValues(addList), modifier);
            }
        });
        result.commit();
    }
    
    public static Class getType(Project project, String typeName) {
        List<ClassPath> classPaths = SourceGroupSupport.gerClassPath(project);
        for (ClassPath cp : classPaths) {
            try {
                Class ret = Util.getPrimitiveType(typeName);
                if (ret != null) {
                    return ret;
                }
                ClassLoader cl = cp.getClassLoader(true);
                ret = Util.getGenericRawType(typeName, cl);
                if (ret != null) {
                    return ret;
                }
                if (cl != null) {
                    return cl.loadClass(typeName);
                }
            } catch (ClassNotFoundException ex) {
                //Logger.global.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }
    
    public static SoapClientJavaOperationInfo[] toJaxwsOperationInfos(WsdlSaasMethod m, 
            Project project) {
        List<SoapClientJavaOperationInfo> infos = new ArrayList<SoapClientJavaOperationInfo>();
        infos.add(new SoapClientJavaOperationInfo(m, project));
        
        return infos.toArray(new SoapClientJavaOperationInfo[0]);
    }
}
