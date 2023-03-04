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
package org.netbeans.modules.junit;

import org.netbeans.modules.junit.api.JUnitSettings;
import org.netbeans.modules.junit.api.JUnitTestUtil;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.Location;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 * Default IT plugin.
 */
public final class DefaultITPlugin extends JUnitPlugin {

    @Override
    protected Location getTestLocation(Location sourceLocation) {
        FileObject fileObj = sourceLocation.getFileObject();
        ClassPath srcCp;

        if ((srcCp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE)) == null) {
            return null;
        }

        String baseResName = srcCp.getResourceName(fileObj, '/', false);
        if(baseResName == null) {
            return null;
        }
        String testResName = !fileObj.isFolder()
                             ? getTestResName(baseResName, fileObj.getExt())
                             : getSuiteResName(baseResName);
        assert testResName != null;
	Location oppositeLocation = getOppositeLocation(sourceLocation, srcCp, testResName, true);
        return oppositeLocation;
    }



    @Override
    protected Location getTestedLocation(Location testLocation) {
        FileObject fileObj = testLocation.getFileObject();
        ClassPath srcCp;

        if (fileObj.isFolder()
               || ((srcCp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE)) == null)) {
            return null;
        }

        String baseResName = srcCp.getResourceName(fileObj, '/', false);
        if (baseResName == null) {
            return null;     //if the selectedFO is not within the classpath
        }
        String srcResName = getSrcResName(baseResName, fileObj.getExt());
        if (srcResName == null) {
            return null;     //if the selectedFO is not a test class (by name)
        }

        return getOppositeLocation(testLocation,
                                   srcCp,
                                   srcResName,
                                   false);
    }

    private static String getTestResName(String baseResName, String ext) {
        StringBuilder buf
                = new StringBuilder(baseResName.length() + ext.length() + 10);
	buf.append(baseResName).append("IT"); //NOI18N
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }

    /**
     */
    private static String getSuiteResName(String baseResName) {
        if (baseResName.length() == 0) {
            return JUnitSettings.getDefault().getRootSuiteClassName();
        }

        final String suiteSuffix = "Suite";                             //NOI18N

        String lastNamePart
                = baseResName.substring(baseResName.lastIndexOf('/') + 1);

        StringBuilder buf = new StringBuilder(baseResName.length()
                                              + lastNamePart.length()
                                              + suiteSuffix.length()
                                              + 6);
        buf.append(baseResName).append('/');
        buf.append(Character.toUpperCase(lastNamePart.charAt(0)))
           .append(lastNamePart.substring(1));
        buf.append(suiteSuffix);
        buf.append(".java");                                            //NOI18N

        return buf.toString();
    }

    /**
     */
    private static String getSrcResName(String testResName, String ext) {
        if (!testResName.endsWith("IT")) {  //NOI18N
            return null;
        }

        StringBuilder buf
                = new StringBuilder(testResName.length() + ext.length());
	buf.append(testResName.substring(0, testResName.length() - 2)); //NOI18N
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }

    private static Location getOppositeLocation(
	    final Location sourceLocation,
	    final ClassPath fileObjCp,
	    final String oppoResourceName,
	    final boolean sourceToTest) {
	FileObject fileObj = sourceLocation.getFileObject();
	FileObject fileObjRoot;

	if ((fileObjRoot = fileObjCp.findOwnerRoot(fileObj)) == null) {
	    return null;
	}

	URL[] oppoRootsURLs = sourceToTest
		? UnitTestForSourceQuery.findUnitTests(fileObjRoot)
		: UnitTestForSourceQuery.findSources(fileObjRoot);
	//if (sourceToTest && (oppoRootsURLs.length == 0)) {
	//    PENDING - offer creation of new unit tests root
	//}
	if ((oppoRootsURLs == null) || (oppoRootsURLs.length == 0)) {
	    return null;
	}

	ClassPath oppoRootsClassPath = ClassPathSupport
		.createClassPath(oppoRootsURLs);
	final List<FileObject> oppoFiles = oppoRootsClassPath
		.findAllResources(oppoResourceName);
	if (oppoFiles.isEmpty()) {
	    //if (sourceToTest) {
	    //    PENDING - offer creation of new test class
	    //}
	    return null;
	}

	return new Location(oppoFiles.get(0)/*, null*/);
    }

    @Override
    protected FileObject[] createTests(FileObject[] filesToTest, FileObject targetRoot, Map<CreateTestParam, Object> params) {
	Project project = FileOwnerQuery.getOwner(filesToTest[0]);
        if (project != null) {
            JUnitPlugin plugin = JUnitTestUtil.getPluginForProject(project);
            if (plugin instanceof DefaultPlugin) {
                params.put(CommonPlugin.CreateTestParam.INC_GENERATE_INTEGRATION_TEST, Boolean.TRUE);
		return ((DefaultPlugin) plugin).createTests(filesToTest, targetRoot, params);
	    }
	}
	return new FileObject[0];
    }
}
