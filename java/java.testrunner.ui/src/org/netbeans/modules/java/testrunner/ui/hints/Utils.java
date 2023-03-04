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
package org.netbeans.modules.java.testrunner.ui.hints;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.plugin.CommonTestUtilProvider;
import org.netbeans.modules.gsf.testrunner.plugin.GuiUtilsProvider;
import static org.netbeans.modules.java.testrunner.CommonTestUtil.findSourceGroupOwner;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class Utils {

    private static final String TEST_CLASS_SUFFIX = "Test"; //NOI18N
    private static List<String> testingFrameworks;
    private static Object[] locations;

    public static String getLocationText(Object location) {
	String text = location instanceof SourceGroup
		? ((SourceGroup) location).getDisplayName()
		: location instanceof FileObject
		? FileUtil.getFileDisplayName((FileObject) location)
		: location.toString();
	return text;
    }

    public static Map<Object, List<String>> getValidCombinations(CompilationInfo info, String methodName) {
	populateTestingFrameworks();
	if(testingFrameworks.isEmpty()) {
	    return null;
	}
	populateLocations(info.getFileObject());
	Map<Object, List<String>> validCombinations = new HashMap<Object, List<String>>();
	for (Object location : locations) {
	    String targetFolderPath = Utils.getTargetFolderPath(location);
	    List<String> framework2Add = new ArrayList<String>();
	    for (String framework : testingFrameworks) {
		String preffiledName = Utils.getPreffiledName(info, framework);
		preffiledName = preffiledName.replace(".", "/").concat(".java"); //NOI18N
		String path = targetFolderPath.concat("/").concat(preffiledName);
		File f = new File(path);
		FileObject fo = FileUtil.toFileObject(f);
		if(methodName == null) {
		    if (fo == null) {
			framework2Add.add(framework);
		    }
		} else {
		    try {
			String testMethodName = getTestMethodName(methodName);
			if (fo != null && !fo.asText().replace("\n", "").trim().contains(testMethodName.concat("("))) { //NOI18N
			    framework2Add.add(framework);
			}
		    } catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		    }
		}
	    }
	    if (!framework2Add.isEmpty()) {
		validCombinations.put(location, framework2Add);
	    }
	}
	testingFrameworks.clear();
	locations = null;
	return validCombinations;
    }

    private static String getTestMethodName(String methodName) {
	return "test" + capitalizeFirstLetter(methodName); //NOI18N
    }

    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.length() <= 0) {
            return str;
        }

        char chars[] = str.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    private static void populateTestingFrameworks() {
	testingFrameworks = new ArrayList<String>();
	Collection<? extends Lookup.Item<TestCreatorProvider>> testCreatorProviders = Lookup.getDefault().lookupResult(TestCreatorProvider.class).allItems();
	for (Lookup.Item<TestCreatorProvider> provider : testCreatorProviders) {
	    testingFrameworks.add(provider.getDisplayName());
	}
    }

    private static void populateLocations(FileObject activeFO) {
	Collection<? extends CommonTestUtilProvider> testUtilProviders = Lookup.getDefault().lookupAll(CommonTestUtilProvider.class);
	for (CommonTestUtilProvider provider : testUtilProviders) {
	    locations = provider.getTestTargets(activeFO);
	    break;
	}
	if (locations != null && locations.length == 0) {
            SourceGroup sourceGroupOwner = findSourceGroupOwner(activeFO);
            if (sourceGroupOwner != null) {
                // get URLs of target SourceGroup's roots
                locations = UnitTestForSourceQuery.findUnitTests(sourceGroupOwner.getRootFolder());
            }
        }
    }

    private static String getPreffiledName(CompilationInfo info, String selectedFramework) {
	FileObject fileObj = info.getFileObject();
	ClassPath cp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE);
	String className = cp.getResourceName(fileObj, '.', false);
	return className + getTestingFrameworkSuffix(selectedFramework) + TEST_CLASS_SUFFIX;
    }

    private static String getTestingFrameworkSuffix(String selectedFramework) {
	if (selectedFramework == null) {
	    return "";
	}
	String testngFramework = "";
	Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
	for (GuiUtilsProvider provider : providers) {
	    testngFramework = provider.getTestngFramework();
	    break;
	}
	return selectedFramework.equals(testngFramework) ? "NG" : ""; //NOI18N
    }

    private static String getTargetFolderPath(Object selectedLocation) {
	if (selectedLocation == null) {
	    return null;
	}

	if (selectedLocation instanceof SourceGroup) {
	    return ((SourceGroup) selectedLocation).getRootFolder().getPath();
	}
        if (selectedLocation instanceof URL) { // test root folder is not created yet, so return path of "Test Packages" folder
	    return ((URL) selectedLocation).getPath();
	}
	assert selectedLocation instanceof FileObject;      //root folder
	return ((FileObject) selectedLocation).getPath();
    }

}
