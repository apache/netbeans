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

package org.netbeans.modules.testng;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.java.classpath.ClassPath;
import static org.netbeans.api.java.classpath.ClassPath.SOURCE;
import static org.netbeans.api.java.project.JavaProjectConstants.SOURCES_TYPE_JAVA;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.CreateTestParam;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.Location;
import org.netbeans.modules.testng.TestabilityResult.SkippedClass;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Default TestNG plugin.
 *
 * @author  Marian Petras
 */
public final class DefaultPlugin extends TestNGPlugin {
    
    /** name of FreeMarker template property - generate {@code &#64;BeforeClass} method? */
    private static final String templatePropBeforeClass = "classSetUp"; //NOI18N
    /** name of FreeMarker template property - generate {@code &#64;AfterClass} method? */
    private static final String templatePropAfterClass = "classTearDown";//NOI18N
    /** name of FreeMarker template property - generate {@code &#64;Before} method? */
    private static final String templatePropBefore = "methodSetUp";     //NOI18N
    /** name of FreeMarker template property - generate {@code &#64;After} method? */
    private static final String templatePropAfter = "methodTearDown";   //NOI18N
    /** name of FreeMarker template property - generate in-method source code hints? */
    private static final String templatePropCodeHints = "sourceCodeHint";   //NOI18N
    /** name of FreeMarker template property - generate hints - method placeholders? */
    private static final String templatePropMethodPH = "testMethodsPlaceholder"; //NOI18N
    /** name of FreeMarker template property - use Java annotations? */
    private static final String templatePropUseAnnotations = "useAnnotations"; //NOI18N
    /** name of FreeMarker template property - list of class names */
    private static final String templatePropClassNames = "classNames";  //NOI18N
    /**
     * name of FreeMarker template property - list of class names,
     * each with a suffix <code>&quot;.class&quot;</code>
     */
    private static final String templatePropClasses = "classes";        //NOI18N
    private static final String NGPrefix = "NG";        //NOI18N
    
    
    /**
     * 
     */
    @Override
    protected boolean canCreateTests(FileObject... fileObjects) {
        if (fileObjects.length == 0) {
            return false;
        }

        final FileObject firstFile = fileObjects[0];
        final SourceGroup sourceGroup = findSourceGroup(firstFile);
        if (sourceGroup == null) {
            return false;
        }
        final FileObject rootFolder = sourceGroup.getRootFolder();
        if (UnitTestForSourceQuery.findUnitTests(rootFolder).length == 0) {
            return false;
        }

        /*
         * Now we know that source folder of the first file has a corresponding
         * test folder (possible non-existent).
         */
        if (fileObjects.length == 1) {
            /* ... if there is just one file selected, it is all we need: */
            return true;
        }

        /*
         * ...for multiple files, we just check that all the selected files
         * have the same root folder:
         */
        for (int i = 1; i < fileObjects.length; i++) {
            FileObject fileObj = fileObjects[i];
            if (!FileUtil.isParentOf(rootFolder, fileObj)
                    || !sourceGroup.contains(fileObj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds a Java source group the given file belongs to.
     * 
     * @param  file  {@code FileObject} to find a {@code SourceGroup} for
     * @return  the found {@code SourceGroup}, or {@code null} if the given
     *          file does not belong to any Java source group
     */
    private static SourceGroup findSourceGroup(FileObject file) {
        final Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }

        Sources src = ProjectUtils.getSources(project);
        SourceGroup[] srcGrps = src.getSourceGroups(SOURCES_TYPE_JAVA);
        for (SourceGroup srcGrp : srcGrps) {
            FileObject rootFolder = srcGrp.getRootFolder();
            if (((file == rootFolder) || FileUtil.isParentOf(rootFolder, file)) 
                    && srcGrp.contains(file)) {
                return srcGrp;
            }
        }
        return null;
    }

    /**
     *
     */
    protected Location getTestLocation(Location sourceLocation) {
        FileObject fileObj = sourceLocation.getFileObject();
        ClassPath srcCp;
        
        if ((srcCp = ClassPath.getClassPath(fileObj, SOURCE)) == null) {
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
        
        return getOppositeLocation(sourceLocation,
                                   srcCp,
                                   testResName,
                                   true);
    }
    
    /**
     *
     */
    protected Location getTestedLocation(Location testLocation) {
        FileObject fileObj = testLocation.getFileObject();
        ClassPath srcCp;
        
        if (fileObj.isFolder()
               || ((srcCp = ClassPath.getClassPath(fileObj, SOURCE)) == null)) {
            return null;
        }
        
        String baseResName = srcCp.getResourceName(fileObj, '/', false);
        if (baseResName == null) {
            return null;
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
    
    /**
     *
     */
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
        
//        final ElementHandle elementHandle = sourceLocation.getElementHandle();
//        if (elementHandle == null) {
            return new Location(oppoFiles.get(0)/*, null*/);
//        }
        
//        /* Build SOURCE classpath: */
//        ClassPath[] srcCpDelegates = new ClassPath[2];
//        if (sourceToTest) {
//            srcCpDelegates[0] = fileObjCp;
//            srcCpDelegates[1] = oppoRootsClassPath;
//        } else {
//            srcCpDelegates[0] = oppoRootsClassPath;
//            srcCpDelegates[1] = fileObjCp;
//        }
//        ClassPath srcClassPath
//                = ClassPathSupport.createProxyClassPath(srcCpDelegates);
//        
//        /* Build COMPILE classpath: */
//        FileObject[] oppoRoots = oppoRootsClassPath.getRoots();
//        ClassPath[] compCpDelegates = new ClassPath[oppoRoots.length + 1];
//        int delegateIndex = 0;
//        if (sourceToTest) {
//            compCpDelegates[delegateIndex++]
//                    = ClassPath.getClassPath(fileObjRoot, COMPILE);
//        }
//        for (FileObject oppoRoot : oppoRoots) {
//            compCpDelegates[delegateIndex++]
//                    = ClassPath.getClassPath(oppoRoot, COMPILE);
//        }
//        if (!sourceToTest) {
//            compCpDelegates[delegateIndex++]
//                    = ClassPath.getClassPath(fileObjRoot, COMPILE);
//        }
//        ClassPath compClassPath
//                = ClassPathSupport.createProxyClassPath(compCpDelegates);
//        
//        /* Obtain the BOOT classpath: */
//        ClassPath bootClassPath = ClassPath.getClassPath(fileObj, BOOT);
//        
//        ClasspathInfo cpInfo = ClasspathInfo.create(bootClassPath,
//                                                    compClassPath,
//                                                    srcClassPath);
//        List<FileObject> files = new ArrayList<FileObject>(oppoFiles.size() + 1);
//        files.add(fileObj);
//        files.addAll(oppoFiles);
//        JavaSource javaSource = JavaSource.create(cpInfo, files);
//        
//        try {
//            MatchFinder matchFinder = new MatchFinder(sourceLocation,
//                                                      oppoFiles,
//                                                      sourceToTest);
//            javaSource.runUserActionTask(matchFinder, true);
//            return matchFinder.getResult();
//        } catch (IOException ex) {
//            Logger.getLogger("global").log(Level.SEVERE, null, ex);     //NOI18N
//            return null;
//        }
    }
    

    /**
     */
    private static String getTestResName(String baseResName, String ext) {
        StringBuilder buf
                = new StringBuilder(baseResName.length() + ext.length() + 10);
        buf.append(baseResName).append(NGPrefix + "Test");                         //NOI18N
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }
    
    /**
     */
    private static String getSuiteResName(String baseResName) {
        if (baseResName.length() == 0) {
            return TestNGSettings.getDefault().getRootSuiteClassName();
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
        if (!testResName.endsWith(NGPrefix + "Test")) {                            //NOI18N
            return null;
        }
        
        StringBuilder buf
                = new StringBuilder(testResName.length() + ext.length());
        buf.append(testResName.substring(0, testResName.length() - 6));
        if (ext.length() != 0) {
            buf.append('.').append(ext);
        }
        return buf.toString();
    }
    
    /**
     */
    private static String getTestClassName(String baseClassName) {
        return baseClassName + NGPrefix + "Test";                                  //NOI18N
    }
    
    /**
     */
    private static String getSourceClassName(String testClassName) {
        final String suffix = "Test";                                   //NOI18N
        final int suffixLen = suffix.length();
        
        return ((testClassName.length() > suffixLen)
                    && testClassName.endsWith(suffix))
               ? testClassName.substring(0, testClassName.length() - suffixLen)
               : null;
    }
    
    /**
     */
    private static String getTestMethodName(String baseMethodName) {
        final String prefix = "test";                                   //NOI18N
        final int prefixLen = prefix.length();
        
        StringBuffer buf = new StringBuffer(prefixLen
                                            + baseMethodName.length());
        buf.append(prefix).append(baseMethodName);
        buf.setCharAt(prefixLen,
                      Character.toUpperCase(baseMethodName.charAt(0)));
        return buf.toString();
    }
    
    /**
     */
    private static String getSourceMethodName(String testMethodName) {
        final String prefix = "test";                                   //NOI18N
        final int prefixLen = prefix.length();
        
        return ((testMethodName.length() > prefixLen)
                    && testMethodName.startsWith(prefix))
               ? new StringBuilder(testMethodName.length() - prefixLen)
                        .append(Character.toLowerCase(testMethodName.charAt(prefixLen)))
                        .append(testMethodName.substring(prefixLen + 1))
                        .toString()
               : null;
    }
    
    /**
     * Creates test classes for given source classes.
     * 
     * @param filesToTest  source files for which test classes should be
     *                      created
     * @param targetRoot   root folder of the target source root
     * @param params  parameters of creating test class
     * @return created test files
     */
    @Messages({"MSG_StatusBar_CreateTest_Begin=Creating tests ...", 
         "# {0} - skipped class name",
        "# {1} - reason for skipping",
        "MSG_skipped_class=Class {0} was skipped because it was {1}.",
        "# {0} - reason for skipping the classes",
        "MSG_skipped_classes=Some classes were skipped because they were {0}.",
        "MSG_No_test_created=No tests were created because no testable class was found.",
        "PROP_testng_testClassTemplate=Templates/UnitTests/EmptyTestNGTest.java",
        "PROP_testng_testSuiteTemplate=Templates/UnitTests/TestNGSuite.xml"})
    @Override
    protected FileObject[] createTests(
                                final FileObject[] filesToTest,
                                final FileObject targetRoot,
                                final Map<CreateTestParam, Object> params) {
        //XXX: not documented that in case that if filesToTest is <null>,
        //the target root param works as a target folder
        
        ProgressIndicator progress = new ProgressIndicator();
        progress.show();

        String msg = Bundle.MSG_StatusBar_CreateTest_Begin();   //NOI18N
        progress.displayStatusText(msg);

        final TestCreator testCreator = new TestCreator(params);
        
        CreationResults results;
        try {
            final String templateId;
            final String suiteTemplateId;
            boolean forTestSuite = (filesToTest != null)
                    && (filesToTest.length != 0)
                    && ((filesToTest.length > 1) || !filesToTest[0].isData());
            templateId = Bundle.PROP_testng_testClassTemplate();
            suiteTemplateId = forTestSuite
                    ? Bundle.PROP_testng_testSuiteTemplate()
                    : null;
            DataObject doTestTempl = (templateId != null)
                    ? loadTestTemplate(templateId)
                    : null;
            if (doTestTempl == null) {
                return null;
            }
            DataObject doSuiteTempl = (suiteTemplateId != null)
                                      ? loadTestTemplate(suiteTemplateId)
                                      : null;
            if (forTestSuite && (doSuiteTempl == null)) {
                return null;
            }
            
            Map<String, Boolean> templateParams = createTemplateParams(params);
            setAnnotationsSupport(targetRoot, templateParams);

            if ((filesToTest == null) || (filesToTest.length == 0)) {
                //XXX: Not documented that filesToTest may be <null>
                
                addTemplateParamEntry(params, CreateTestParam.INC_CODE_HINT,
                                      templateParams, templatePropMethodPH);

                String testClassName = (String) params.get(CreateTestParam.CLASS_NAME);
                assert testClassName != null;
                results = new CreationResults(1);
                DataObject testDataObj = createEmptyTest(targetRoot,
                                                         testClassName,
                                                         testCreator,
                                                         templateParams,
                                                         doTestTempl);
                if (testDataObj != null) {
                    results.addCreated(testDataObj);
                }
                
            } else {
                ClassPath testClassPath = ClassPathSupport.createClassPath(
                                                new FileObject[] {targetRoot});
                if (!forTestSuite) {
                    String testClassName = (String) params.get(CreateTestParam.CLASS_NAME);
                    if (testClassName == null) {
                        String srcClassName
                                = ClassPath.getClassPath(filesToTest[0], SOURCE)
                                  .getResourceName(filesToTest[0], '.', false);
                        testClassName = getTestClassName(srcClassName);
                    }
                    try {
                        results = createSingleTest(
                                filesToTest[0],
                                testClassName,
                                testCreator,
                                templateParams,
                                doTestTempl,
                                testClassPath,
                                TestabilityResult.NO_TESTEABLE_METHODS.getReasonValue(),
                                null,              //parent suite
                                progress);
                    } catch (CreationError ex) {
                        ErrorManager.getDefault().notify(ex);
                        results = new CreationResults(1);
                    }
                } else {
                    results = new CreationResults();

                    // go through all nodes
                    for (FileObject fileToTest : filesToTest) {
                        try {
                            results.combine(createTests(fileToTest,
                                                        testCreator,
                                                        templateParams,
                                                        doTestTempl,
                                                        doSuiteTempl,
                                                        testClassPath,
                                                        null,
                                                        progress));
                        } catch (CreationError e) {
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                }
            }
        } finally {
            progress.hide();
        }

        final Set<SkippedClass> skipped = results.getSkipped();
        final Set<DataObject> created = results.getCreated();
        if (!skipped.isEmpty()) {
            // something was skipped
            String message;
            if (skipped.size() == 1) {
                // one class? report it
                SkippedClass skippedClass = skipped.iterator().next();
                message = Bundle.MSG_skipped_class(skippedClass.clsName, strReason(skippedClass.reason, "COMMA", "AND")); //NOI18N
            } else {
                // more classes, report a general error
                // combine the results
                TestabilityResult reason = TestabilityResult.OK;
                for (SkippedClass sc : skipped) {
                    reason = TestabilityResult.combine(reason, sc.reason);
                }

                message = Bundle.MSG_skipped_classes(strReason(reason, "COMMA", "OR")); //NOI18N
            }
            TestUtil.notifyUser(message, NotifyDescriptor.INFORMATION_MESSAGE);

        }
        
        if (created.isEmpty()) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    TestUtil.notifyUser(
                            Bundle.MSG_No_test_created(),
                            NotifyDescriptor.INFORMATION_MESSAGE);
                }
            });
        }
        
        FileObject[] createdFiles;
        if (created.isEmpty()) {
            createdFiles = new FileObject[0];
        } else {
            createdFiles = new FileObject[created.size()];
            int i = 0;
            for (DataObject dObj : created) {
                createdFiles[i++] = dObj.getPrimaryFile();
            }
        }
        return createdFiles;
    }

    /**
     * Create a map of FreeMaker template parameters from a map
     * of {@code CreateTestParam}s.
     */
    public static final Map<String, Boolean> createTemplateParams(
                                          Map<CreateTestParam, Object> params) {
        Map<String,Boolean> result = new HashMap<String,Boolean>(7);

        addTemplateParamEntry(params, CreateTestParam.INC_CLASS_SETUP,
                              result, templatePropBeforeClass);
        addTemplateParamEntry(params, CreateTestParam.INC_CLASS_TEAR_DOWN,
                              result, templatePropAfterClass);
        addTemplateParamEntry(params, CreateTestParam.INC_SETUP,
                              result, templatePropBefore);
        addTemplateParamEntry(params, CreateTestParam.INC_TEAR_DOWN,
                              result, templatePropAfter);
        addTemplateParamEntry(params, CreateTestParam.INC_CODE_HINT,
                              result, templatePropCodeHints);

        return result;
    }

    private static void addTemplateParamEntry(Map<CreateTestParam, Object> srcParams,
                                              CreateTestParam srcParamKey,
                                              Map<String, Boolean> templParams,
                                              String templParamKey) {
        Object value = srcParams.get(srcParamKey);
        if (value instanceof Boolean) {
            templParams.put(templParamKey, Boolean.class.cast(value));
        }
    }
    
    /**
     * Creates a new test class.
     * 
     * @param  targetRoot     <!-- //PENDING -->
     * @param  testClassName  <!-- //PENDING -->
     * @param  testCreator  {@code TestCreator} to be used for filling
     *                      the test class template
     * @param  templateDataObj  {@code DataObject} representing
     *                          the test file template
     * @return  the created test, or {@code null} if no test was created
     */
    private DataObject createEmptyTest(FileObject targetRoot,
                                       String testClassName,
                                       TestCreator testCreator,
                                       final Map<String, ? extends Object> templateParams,
                                       DataObject templateDataObj) {
        if (testClassName == null) {
            throw new IllegalArgumentException("testClassName = null"); //NOI18N
        }
        
        DataObject testDataObj = null;
        try {
            DataFolder targetFolderDataObj = DataFolder.findFolder(targetRoot);
            testDataObj = templateDataObj.createFromTemplate(
                                        targetFolderDataObj,
                                        testClassName,
                                        templateParams);

            /* fill in setup etc. according to dialog settings */
            testCreator.createEmptyTest(testDataObj.getPrimaryFile());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return testDataObj;
    }
    
    /**
     *
     */
    private static CreationResults createSingleTest(
                FileObject sourceFile,
                String requestedTestClassName,
                final TestCreator testCreator,
                final Map<String, ? extends Object> templateParams,
                DataObject templateDataObj,
                ClassPath testClassPath,
                long skipTestabilityResultMask,
                List<String> parentSuite,
                ProgressIndicator progress) throws CreationError {
        
        List<SkippedClass> nonTestable;
        List<ElementHandle<TypeElement>> testable;
        try {
            JavaSource javaSource = JavaSource.forFileObject(sourceFile);
            //issue 161598
            if (javaSource == null)
                return CreationResults.EMPTY;
            if (skipTestabilityResultMask != 0) {
                nonTestable = new ArrayList<SkippedClass>();
                testable = TopClassFinder.findTestableTopClasses(javaSource,
                                                                 testCreator,
                                                                 nonTestable,
                                                                 skipTestabilityResultMask);
            } else {
                nonTestable = Collections.<SkippedClass>emptyList();
                testable = TopClassFinder.findTopClasses(javaSource);
            }
        } catch (IOException ex) {
            throw new CreationError(ex);
        }
        
        CreationResults result = new CreationResults(4);
        if (!nonTestable.isEmpty()) {
            result.addSkipped(nonTestable);
        }
        if (!testable.isEmpty()) {
            /* used only if (requestedTestClassName != null): */
            boolean mainClassProcessed = false;

            try {
                for (ElementHandle<TypeElement> clsToTest : testable) {
                    String testClassName;
                    String srcClassNameFull = clsToTest.getQualifiedName();
                    if ((requestedTestClassName != null)
                            && !mainClassProcessed
                            && TestUtil.getSimpleName(srcClassNameFull)
                                   .equals(sourceFile.getName())) {
                        testClassName = requestedTestClassName;
                        mainClassProcessed = true;
                    } else {
                        testClassName = TestUtil.getTestClassName(srcClassNameFull.concat(NGPrefix));
                    }
                    String testResourceName = testClassName.replace('.', '/');

                    /* find or create the test class DataObject: */
                    DataObject testDataObj = null;
                    FileObject testFile = testClassPath.findResource(
                                            testResourceName + ".java");//NOI18N
                    boolean isNew = (testFile == null);
                    if (testFile == null) {
                        testDataObj = createTestClass(testClassPath,
                                                      testResourceName,
                                                      templateDataObj,
                                                      templateParams);
                        testFile = testDataObj.getPrimaryFile();
                    }
                    
                    testCreator.createSimpleTest(clsToTest, testFile, isNew);
                    if (testDataObj == null) {
                        testDataObj = DataObject.find(testFile);
                    }
                    save(testDataObj);
                    
                    result.addCreated(testDataObj);
                    // add the test class to the parent's suite
                    if (parentSuite != null) {
                        parentSuite.add(testClassName);
                    }
                }
            } catch (IOException ex) {       //incl. DataObjectNotFoundException
                throw new CreationError(ex);
            }
        }
        
        return result;
    }
    
    /**
     *
     */
    private static CreationResults createTests(
                final FileObject srcFileObj,
                final TestCreator testCreator,
                final Map<String, ? extends Object> templateParams,
                DataObject doTestT,
                DataObject doSuiteT,
                final ClassPath testClassPath,
                List<String> parentSuite,
                ProgressIndicator progress) throws CreationError {

        CreationResults results;
        if (srcFileObj.isFolder()) {
            results = new CreationResults();

            List<String> mySuite = new LinkedList<String>();
            
            progress.setMessage(getScanningMsg(srcFileObj.getName()));

            for (FileObject childFileObj : srcFileObj.getChildren()) {
                if (progress.isCanceled()) {
                    results.setAbborted();
                    break;
                }
                if (!VisibilityQuery.getDefault().isVisible(childFileObj)) {
                    continue;
                }
                results.combine(createTests(childFileObj,
                                            testCreator,
                                            templateParams,
                                            doTestT,
                                            doSuiteT,
                                            testClassPath,
                                            mySuite,
                                            progress));
                if (results.isAbborted()) {
                    break;
                }
            }

            // if everything went ok, and the option is enabled,
            // create a suite for the folder .
            if (!results.isAbborted()
//                    && !mySuite.isEmpty()
                    && TestNGSettings.getDefault().isGenerateSuiteClasses()) {
                createSuiteTest(srcFileObj,
                                (String) null,
                                testCreator,
                                templateParams,
                                doSuiteT,
                                testClassPath,
                                mySuite,
                                parentSuite,
                                progress);
            }
        } else if (srcFileObj.isData() && TestUtil.isJavaFile(srcFileObj)) {
            results = createSingleTest(srcFileObj,
                                       (String) null, //use the default clsName
                                       testCreator,
                                       templateParams,
                                       doTestT,
                                       testClassPath,
                                       TestabilityResult.NO_TESTEABLE_METHODS.getReasonValue(),
                                       parentSuite,
                                       progress);
        } else {
            results = CreationResults.EMPTY;
        }
        return results;
    }

    /**
     *
     */
    private static DataObject createSuiteTest(
            FileObject folder,
            String suiteName,
            final TestCreator testCreator,
            final Map<String, ? extends Object> templateParams,
            DataObject templateDataObj,
            ClassPath testClassPath,
            List<String> classesToInclude,
            List<String> parentSuite,
            ProgressIndicator progress) throws CreationError {

        // find correct package name
        ClassPath cp = ClassPath.getClassPath(folder, SOURCE);
        assert cp != null : "SOURCE classpath was not found for " + folder; //NOI18N
        if (cp == null) {
            return null;
        }

        String pkg = cp.getResourceName(folder, '/', false);
        if (pkg.length() > 0) {
            // #176958 : append the rootFolderName in the fullSuiteName, so that we can decide in which Classpath root folder 
            //           the new Suite file will be generated, when createTestClass(...) is called later on
            String rootFolderName = ""; //NOI18N
            if (cp.getRoots().length > 1) {
                FileObject rootOwnerFO = cp.findOwnerRoot(folder);
                if (rootOwnerFO != null) {
                    rootFolderName = rootOwnerFO.getName() + '/';
                }
            }
            String dotPkg = pkg.replace('/', '.');
            String fullSuiteName = rootFolderName.concat((suiteName != null)
                    ? pkg + '/' + suiteName
                    : TestUtil.convertPackage2SuiteName(pkg));

//            String classNames = makeListOfClasses(classesToInclude, null);
//            String classes = makeListOfClasses(classesToInclude, ".class"); //NOI18N

            final Map<String, Object> suiteTemplParams = new HashMap<String, Object>(templateParams);
//            suiteTemplParams.put(templatePropClassNames, classNames);
//            suiteTemplParams.put(templatePropClasses, classes);
            String projectName = ProjectUtils.getInformation(FileOwnerQuery.getOwner(folder)).getName();
            suiteTemplParams.put("suiteName", projectName);
            suiteTemplParams.put("testName", dotPkg.concat(" suite"));
            suiteTemplParams.put("pkg", dotPkg);

            try {
                /*
                 * find or create the test class DataObject:
                 */
                DataObject testDataObj = null;
                FileObject testFile = testClassPath.findResource(
                        fullSuiteName + ".xml");       //NOI18N
                boolean isNew = (testFile == null);
                if (testFile == null) {
                    testDataObj = createTestClass(testClassPath,
                            fullSuiteName,
                            templateDataObj,
                            suiteTemplParams);
                    testFile = testDataObj.getPrimaryFile();
                }
                
                try {
                    if (testDataObj == null) {
                        testDataObj = DataObject.find(testFile);
                    }
                    save(testDataObj);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                    return null;
                }
                return testDataObj;
            } catch (IOException ioe) {
                throw new CreationError(ioe);
            }
        }
        return null;
    }

    /**
     *
     */
    public DataObject createSuiteTest(
                                final FileObject targetRootFolder,
                                final FileObject targetFolder,
                                final String suiteName,
                                final Map<CreateTestParam, Object> params) {
        final Map<String, Boolean> templateParams = createTemplateParams(params);
        setAnnotationsSupport(targetFolder, templateParams);
        TestCreator testCreator = new TestCreator(params);
        final ClasspathInfo cpInfo = ClasspathInfo.create(targetRootFolder);
        List<String> testClassNames = TestUtil.getJavaFileNames(targetFolder,
                                                                cpInfo);
        
        final String templateId = Bundle.PROP_testng_testSuiteTemplate();
        
        final DataObject doSuiteTempl
                = loadTestTemplate(templateId);
        if (doSuiteTempl == null) {
            return null;
        }
        
        DataObject suiteDataObj;
        try {
            return createSuiteTest(targetFolder,
                                   suiteName,
                                   testCreator,
                                   templateParams,
                                   doSuiteTempl,
                                   cpInfo.getClassPath(PathKind.SOURCE),
                                   new LinkedList<String>(testClassNames),
                                   null,            //parent suite
                                   null);           //progress indicator
        } catch (CreationError ex) {
            return null;
        }
    }
    
    /**
     */
    private static DataObject createTestClass(
            ClassPath cp,
            String testClassName,
            DataObject templateDataObj,
            final Map<String, ? extends Object> templateParams)
                                        throws DataObjectNotFoundException,
                                               IOException {
        
        FileObject root = cp.getRoots()[0];
        // #176958 : decide in which Classpath root folder the new Suite file will be generated
        String rootFolderName = ""; //NOI18N
        if (cp.getRoots().length > 1) {
            int indexFirst = testClassName.indexOf('/');
            int indexLast = testClassName.lastIndexOf('/');
            assert indexFirst != indexLast :  // this should not happen
                    "ClassPath=" + cp + "\n" + "testClassName=" + testClassName;                //NOI18N
            rootFolderName = testClassName.substring(0, indexFirst);
            testClassName = testClassName.substring(indexLast + 1);
            for (int i = 0; i < cp.getRoots().length; i++) {
                FileObject rootFO = cp.getRoots()[i];
                if (rootFO.getPath().endsWith(rootFolderName)) {
                    root = rootFO;
                }
            }
        }
        int index = testClassName.lastIndexOf('/');
        String pkg = index > -1 ? testClassName.substring(0, index)
                                : "";                                   //NOI18N
        String clazz = index > -1 ? testClassName.substring(index+1)
                                  : testClassName;

        // create package if it does not exist
        if (pkg.length() > 0) {
            root = FileUtil.createFolder(root, pkg);        //IOException
        }
        // instantiate template into the package
        return templateDataObj.createFromTemplate(          //IOException
                    DataFolder.findFolder(root),
                    clazz,
                    templateParams);
    }

    /**
     * Determines whether annotations should be used in test classes in the
     * given folder when generating the given type of TestNG tests.
     * If annotations are supported, adds this information to the map of
     * template parameters.
     * 
     * @param  testFolder  target folder for generated test classes
     * @param  templateParams  map of template params to store
     *                         the information to
     * @return  {@code true} if it was detected that annotations are supported;
     *          {@code false} otherwise
     */
    private static boolean setAnnotationsSupport(
                                        FileObject testFolder,
                                        Map<String, Boolean> templateParams) {
        if (!testFolder.isFolder()) {
            throw new IllegalArgumentException("not a folder");         //NOI18N
        }

        templateParams.put(templatePropUseAnnotations, true);
        return true;
    }

    /**
     *
     */
    private static void save(DataObject dataObj) throws IOException {
        Savable sc = dataObj.getLookup().lookup(Savable.class);
        if (null != sc) {
            sc.save();
        }
    }
        
    /**
     * Loads a test template.
     * If the template loading fails, displays an error message.
     *
     * @param  templateID  bundle key identifying the template type
     * @return  loaded template, or <code>null</code> if the template
     *          could not be loaded
     */
    private static DataObject loadTestTemplate(String templateID) {
        // get the Test class template
        String path = templateID;
        try {
            FileObject fo = FileUtil.getConfigFile(path);
            if (fo == null) {
                noTemplateMessage(path);
                return null;
            }
            return DataObject.find(fo);
        }
        catch (DataObjectNotFoundException e) {
            noTemplateMessage(path);
            return null;
        }
    }
        
    /**
     *
     */
    @Messages({"# {0} - template file","MSG_template_not_found=Template file {0} was not found. Check the TestNG templates in the Template manager."})
    private static void noTemplateMessage(String temp) {
        String msg = Bundle.MSG_template_not_found(temp);     //NOI18N
        NotifyDescriptor descr = new NotifyDescriptor.Message(
                msg,
                NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(descr);
    }

    /**
     * A helper method to create the reason string from a result
     * and two message bundle keys that indicate the separators to be used instead
     * of "," and " and " in a connected reason like: 
     * "abstract, package-private and without testable methods".
     * <p>
     * The values of the keys are expected to be framed by two extra characters
     * (e.g. as in " and "), which are stripped off. These characters serve to 
     * preserve the spaces in the properties file.
     *
     * @param reason the TestabilityResult to represent
     * @param commaKey bundle key for the connective to be used instead of ", "
     * @param andKey   bundle key for the connective to be used instead of "and"
     * @return String composed of the reasons contained in
     *         <code>reason</code> separated by the values of commaKey and
     *         andKey.
     */
    @Messages({"COMMA=, ","OR= or ","AND= and "})
    private static String strReason(TestabilityResult reason, String commaKey, String andKey) {
        String strComma = Bundle.COMMA();
        String strAnd = andKey.equals("OR") ? Bundle.OR() : Bundle.AND();
        String strReason = reason.getReason( // string representation of the reasons
                        strComma, strAnd);

        return strReason;

    }

    /**
     *
     */
    @NbBundle.Messages({"# {0} - class name", "FMT_generator_status_creating=Creating: {0} ..."})
    private static String getCreatingMsg(String className) {
        return Bundle.FMT_generator_status_creating(className);
    }

    /**
     *
     */
    @NbBundle.Messages({"# {0} - source folder", "FMT_generator_status_scanning=Scanning: {0} ..."})
    private static String getScanningMsg(String sourceName) {
        return Bundle.FMT_generator_status_scanning(sourceName);
    }

    /**
     *
     */
    @NbBundle.Messages({"# {0} - source folder", "FMT_generator_status_ignoring=Ignoring: {0} ..."})
    private static String getIgnoringMsg(String sourceName, String reason) {
        return Bundle.FMT_generator_status_ignoring(sourceName);
    }

    
    /**
     * Error thrown by failed test creation.
     */
    @SuppressWarnings("serial")
    private static final class CreationError extends Exception {
        CreationError() {};
        CreationError(Throwable cause) {
            super(cause);
        }
    }
    
    /**
     * Utility class representing the results of a test creation
     * process. It gatheres all tests (as DataObject) created and all
     * classes (as JavaClasses) for which no test was created.
     */
    static final class CreationResults {
        static final CreationResults EMPTY = new CreationResults();

        Set<DataObject> created; // Set< createdTest : DataObject >
        Set<SkippedClass> skipped;
        boolean abborted = false;

        CreationResults() { this(20);}

        CreationResults(int expectedSize) {
            created = new HashSet<DataObject>(expectedSize * 2, 0.5f);
            skipped = new HashSet<SkippedClass>(expectedSize * 2, 0.5f);
        }

        void setAbborted() {
            abborted = true;
        }

        /**
         * Returns true if the process of creation was abborted. The
         * result contains the results gathered so far.
         */
        boolean isAbborted() {
            return abborted;
        }

        /**
         * Adds a new entry to the set of created tests.
         * @return true if it was added, false if it was present before
         */
        boolean addCreated(DataObject test) {
            return created.add(test);
        }

        /**
         */
        boolean addSkipped(SkippedClass skippedClass) {
            return skipped.add(skippedClass);
        }
        
        /**
         */
        void addSkipped(Collection<SkippedClass> skippedClasses) {
            if (!skippedClasses.isEmpty()) {
                skipped.addAll(skippedClasses);
            }
        }

        /**
         * Returns a set of classes that were skipped in the process.
         * @return Set<SkippedClass>
         */
        Set<SkippedClass> getSkipped() {
            return skipped;
        }

        /**
         * Returns a set of test data objects created.
         * @return Set<DataObject>
         */
        Set<DataObject> getCreated() {
            return created;
        }

        /**
         * Combines two results into one. If any of the results is an
         * abborted result, the combination is also abborted. The
         * collections of created and skipped classes are unified.
         * @param rhs the other CreationResult to combine into this
         */
        void combine(CreationResults rhs) {
            if (rhs.abborted) {
                this.abborted = true;
            }

            this.created.addAll(rhs.created);
            this.skipped.addAll(rhs.skipped);
        }

    }
    
}
