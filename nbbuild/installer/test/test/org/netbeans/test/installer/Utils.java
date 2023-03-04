/**
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
package org.netbeans.test.installer;

import java.awt.event.KeyEvent;
import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import junit.framework.TestCase;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
//import org.netbeans.junit.NbTestCase;

/**
 *

 */
public class Utils {

    public static final long MAX_EXECUTION_TIME = 30000000;
    public static final long MAX_INSTALATION_WAIT = 60000000;
    public static final int DELAY = 50;
    public static final String NEWLINE_REGEXP = "(?:\n\r|\r\n|\n|\r)";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Pattern PATTERN = Pattern.compile("(20[0-9]{10})");
    public static final String NB_DIR_NAME = "NetBeans";
    public static final String GF2_DIR_NAME = "GlassFish2";
    public static final String GF2_PRELUDE_DIR_NAME = "gf-prelude";
    //public static final String GF3_DIR_NAME = "GlassFish3";
    public static final String TOMCAT_DIR_NAME = "Tomcat";
    public static final String NEXT_BUTTON_LABEL = "Next >";
    public static final String FINISH_BUTTON_LABEL = "Finish";
    public static final String INSTALL_BUTTON_LABEL = "Install";
    public static final String UNINSTALL_BUTTON_LABEL = "Uninstall";
    public static final String MAIN_FRAME_TITLE = "Netbeans IDE";
    public static final String OK = "OK";

    public static String getInstaller( TestData data )
    {
      //      File sourceBandle = new File(data.getInstallerFileName());

      String sAddr = data.getInstallerURL( );
      String sFileName = sAddr.substring( sAddr.lastIndexOf( "/" ) + 1 );
      if( sFileName.equals( "" ) )
        sFileName = "installer" + "." + data.getPlatformExt( );
      data.SetExecutableName( sFileName );

      File destBundle = new File( data.getTestWorkDir( ) + File.separator + sFileName );

      if( !destBundle.exists( ) )
      {
        InputStream in = null;
        FileOutputStream out = null;

        try {
            URL sourceBandeleURL = new URL(sAddr);
            Proxy proxy = null;

            proxy = data.getProxy();

            in = sourceBandeleURL.openConnection(proxy).getInputStream();
//            in = new FileInputStream(sourceBandle);
            out = new FileOutputStream(destBundle);

            byte[] buffer = new byte[ 1 << 20 ]; // Just 1Mb
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            data.getLogger().log(Level.SEVERE, "Can not get bundle", ex);
            return toString(ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    data.getLogger().log(Level.SEVERE, "Can not get bundle", ex);
                    return toString(ex);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    data.getLogger().log(Level.SEVERE, "Can not get bundle", ex);
                    return toString(ex);
                }
            }
        }
      }
      data.setInstallerFile(destBundle);
      return OK;
    }

    @SuppressWarnings("empty-statement")
    public static String extractBundle(TestData data) {
        int errorLevel = 0;

        try {
            java.lang.String command = null;
            command = data.getInstallerFile().getCanonicalPath();

            java.lang.ProcessBuilder builder;
            java.lang.Process process;

            if (0 != data.getPlatformName().compareTo("windows")) {
                if (data.getPlatformName().contains("linux") || data.getPlatformName().contains("solaris")) {
                    builder = new java.lang.ProcessBuilder("chmod", "+x", command);
                    process = builder.start();

                    long runningTime;
                    for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
                        try {
                            errorLevel = process.exitValue();
                            break;
                        } catch (IllegalThreadStateException e) {
                            ; // do nothing - the process is still running
                        }
                        wait(data, 1);
                    }

                    if (runningTime >= MAX_EXECUTION_TIME) {
                        process.destroy();
                        data.getLogger().log(Level.SEVERE, "Timeout. Chmod process destroyed");
                        return "Timeout. Chmod process destroyed";
                    } else if (errorLevel != 0) {
                        return "ErrorLevel=>" + errorLevel;
                    }
                }
            }

            String pathToExtract = data.getTestWorkDir() + java.io.File.separator + "bundle";

            builder = new java.lang.ProcessBuilder(command, "--verbose", "--userdir", data.getTestWorkDir().getCanonicalPath(), "--extract", pathToExtract, "--output", data.getTestWorkDir().getCanonicalPath() + File.separator + "inst_" + data.getInstallerType() + ".log");
            process = builder.start();

            long runningTime;
            for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
                try {
                    errorLevel = process.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    ; // do nothing - the process is still running
                }
                wait(data, 1);
            }

            if (runningTime >= MAX_EXECUTION_TIME) {
                process.destroy();
                data.getLogger().log(Level.SEVERE, "Timeout. Installer extract process destroyed");
                return "Timeout. Installer extract process destroyed";
            } else if (errorLevel == 0) {
                data.setBundleFile(new File(pathToExtract + java.io.File.separator + "bundle.jar"));
                data.getInstallerFile().deleteOnExit();
                return OK;
            } else {
                return "ErrorLevel=>" + errorLevel;
            }
        } catch (IOException ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
            return toString(ex);
        }
    }

    @SuppressWarnings("empty-statement")
    public static String extractUninstallerJar(TestData data)
    {
        data.setUninstallerFile(
            new File(
                data.GetNetBeansInstallPath( )
                + File.separator
                + "uninstall." + data.getPlatformExt( )
              )
          );
        int errorLevel = 0;

        try {
            java.lang.String command = null;
            command = data.getUninstallerFile( ).getCanonicalPath();

            java.lang.ProcessBuilder builder;
            java.lang.Process process;

            if (0 != data.getPlatformName().compareTo("windows")) {
                if (data.getPlatformName().contains("linux") || data.getPlatformName().contains("solaris")) {
                    builder = new java.lang.ProcessBuilder("chmod", "+x", command);
                    process = builder.start();

                    long runningTime;
                    for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
                        try {
                            errorLevel = process.exitValue();
                            break;
                        } catch (IllegalThreadStateException e) {
                            ; // do nothing - the process is still running
                        }
                        wait(data, 1);
                    }

                    if (runningTime >= MAX_EXECUTION_TIME) {
                        process.destroy();
                        data.getLogger().log(Level.SEVERE, "Timeout. Chmod process destroyed");
                        return "Timeout. Chmod process destroyed";
                    } else if (errorLevel != 0) {
                        return "ErrorLevel=>" + errorLevel;
                    }
                }
            }

            String pathToExtract = data.getTestWorkDir() + java.io.File.separator + "uninstall";

            builder = new java.lang.ProcessBuilder(command, "--verbose", "--userdir", data.getTestWorkDir().getCanonicalPath(), "--extract", pathToExtract, "--output", data.getTestWorkDir().getCanonicalPath() + File.separator + "uninst_" + data.getInstallerType() + ".log");
            process = builder.start();

            long runningTime;
            for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
                try {
                    errorLevel = process.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    ; // do nothing - the process is still running
                }
                wait(data, 1);
            }

            if (runningTime >= MAX_EXECUTION_TIME) {
                process.destroy();
                data.getLogger().log(Level.SEVERE, "Timeout. Uninstaller extract process destroyed");
                return "Timeout. Uninstaller extract process destroyed";
            } else if (errorLevel == 0) {
                data.setUninstallerBundleFile(new File(pathToExtract + java.io.File.separator + "uninstall.jar"));
                return OK;
            } else {
                return "ErrorLevel=>" + errorLevel;
            }
        } catch (IOException ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
            return toString(ex);
        }
    }

    public static String loadClasses(TestData data) {
        try {

            data.setClassLoader(new URLClassLoader(new URL[]{data.getBundleFile().toURI().toURL()}, data.getClass().getClassLoader()));
            data.setInstallerMainClass(Class.forName(data.getInstallerMainClassName(), true, data.getClassLoader()));
        } catch (Exception ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
            return toString(ex);
        }
        return OK;
    }

    public static void phaseOnePOne( TestData data, String installerType )
    {
        try {
            String wd = System.getenv("WORKSPACE");
            TestCase.assertNotNull(wd);
            data.setWorkDir(new File(wd));
            data.CreateInstallPaths( );
        } catch (IOException ex) {
            TestCase.fail("Can not get WorkDir");
        }

        data.setInstallerType(installerType);

        System.setProperty("nbi.dont.use.system.exit", "true");
        System.setProperty("nbi.utils.log.to.console", "false");
        System.setProperty("servicetag.allow.register", "false");
        System.setProperty("show.uninstallation.survey", "false");
        System.setProperty("user.home", data.getWorkDirCanonicalPath());
        
        if (Boolean.valueOf(System.getProperty("test.use.build.number"))) {
            TestCase.assertNotNull("Determine build number", Utils.determineBuildNumber(data));
        }
        //data.setBuildNumber(null);
    }

    public static void phaseOnePTwo(TestData data) {
        TestCase.assertEquals(
            "Get installer",
            Utils.OK,
            Utils.getInstaller( data )
          );
        TestCase.assertEquals("Extract bundle", Utils.OK, Utils.extractBundle(data));
        TestCase.assertEquals("Load class", Utils.OK, Utils.loadClasses(data));
        TestCase.assertEquals("Run main method", Utils.OK, Utils.runInstaller(data));
    }

    public static String runInstaller(final TestData data) {
        (new Runnable() {

            public void run() {
                try {
                    data.getInstallerMainClass().getMethod("main", java.lang.String[].class).invoke(null, (java.lang.Object) (new String[]{}));
                } catch (Exception ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        }).run();
        return OK;
    }

    public static String runUninstaller(final TestData data) {
        (new Runnable() {

            public void run() {
                try {
                    //dirty hack --ignore-lock
                    data.getUninstallerMainClass().getMethod("main", java.lang.String[].class).invoke(null, (java.lang.Object) (new String[]{"--force-uninstall", "--ignore-lock", "--verbose", "--output ./uninst.log"}));
                } catch (Exception ex) {
                    data.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        }).run();
        return OK;
    }

    public static String loadEngineClasses(TestData data) {
        try {
            data.setEngineClassLoader(new URLClassLoader(new URL[]{data.getUninstallerBundleFile().toURI().toURL()}, data.getClass().getClassLoader()));
            data.setUninstallerMainClass(Class.forName(data.getUninstallerMainClassName(), true, data.getEngineClassLoader()));
        } catch (Exception ex) {
            data.getLogger().log(Level.SEVERE, null, ex);
            return toString(ex);
        }
        return OK;
    }

    public static void stepWelcome() {
        new JButtonOperator(new JFrameOperator(MAIN_FRAME_TITLE), NEXT_BUTTON_LABEL).push();
    }

    public static void stepLicense() {
        JFrameOperator installerMain = new JFrameOperator(MAIN_FRAME_TITLE);

        new JCheckBoxOperator(installerMain, "I accept").push();
        new JButtonOperator(installerMain, NEXT_BUTTON_LABEL).push();
    }

    public static void stepSetDir(
        TestData data,
        String label,
        String dir
      )
    {
      JFrameOperator installerMain = new JFrameOperator( MAIN_FRAME_TITLE );

      if( null == dir )
      {
        String sDefaultPath = new JTextFieldOperator( ( JTextField )( new JLabelOperator( installerMain, label ).getLabelFor( ) ) ).getText( );
        // Set default path to data
        data.SetDefaultPath( sDefaultPath );
      }
      else
      {
        try
        {
          new JTextFieldOperator( ( JTextField )( new JLabelOperator( installerMain, label ).getLabelFor( ) ) ).setText( ( new File( dir ) ).getCanonicalPath( ) );
        }
        catch( IOException ex )
        {
          ex.printStackTrace( );
        }
      }

      new JButtonOperator( installerMain, NEXT_BUTTON_LABEL ).push( );
    }

    public static void stepChooseComponet( String name, TestData data )
    {
        JFrameOperator installerMain = new JFrameOperator(MAIN_FRAME_TITLE);

        new JButtonOperator(installerMain, "Customize...").push();
        JDialogOperator customizeInstallation = new JDialogOperator("Customize Installation");
        JListOperator featureList = new JListOperator(customizeInstallation);
        featureList.selectItem(name);

        featureList.pressKey(KeyEvent.VK_SPACE);

        // Check prelude
        data.m_bPreludePresents = ( -1 != featureList.findItemIndex( "Prelude" ) );

        new JButtonOperator(customizeInstallation, "OK").push();
    }

    public static void CheckPrelude( TestData data )
    {
        JFrameOperator installerMain = new JFrameOperator(MAIN_FRAME_TITLE);

        new JButtonOperator(installerMain, "Customize...").push();
        JDialogOperator customizeInstallation = new JDialogOperator("Customize Installation");
        JListOperator featureList = new JListOperator(customizeInstallation);

        // Check prelude
        data.m_bPreludePresents = ( -1 != featureList.findItemIndex( "Prelude" ) );

        new JButtonOperator(customizeInstallation, "OK").push();
    }

    public static void stepInstall(TestData data) {
        JFrameOperator installerMain = new JFrameOperator(MAIN_FRAME_TITLE);

        new JButtonOperator(installerMain, INSTALL_BUTTON_LABEL).push();
        watchProgressBar(data, installerMain, "Installing");
    }

    public static void stepUninstall(TestData data) {
        JFrameOperator uninstallMain = new JFrameOperator(MAIN_FRAME_TITLE);
        new JButtonOperator(uninstallMain, UNINSTALL_BUTTON_LABEL).push();
        watchProgressBar(data, uninstallMain, "Uninstalling");
    }

    public static void stepFinish() {
        new JButtonOperator(new JFrameOperator(MAIN_FRAME_TITLE), FINISH_BUTTON_LABEL).push();
    }

    public static void phaseOne(TestData data, String installerType) {
        phaseOnePOne(data, installerType);
        data.setInstallerURL(constructURL(data));
        phaseOnePTwo(data);
    }

    /*
    public static void phaseFourFive(TestData data)
    {
        Utils.waitSecond(data, 5);

        TestCase.assertEquals("Installer Finshed", 0, (Integer) System.getProperties().get("nbi.exit.code"));

        TestCase.assertEquals("NetBeans dir created", OK, Utils.dirExist(NB_DIR_NAME));

        TestCase.assertEquals("Extract uninstaller jar", OK, Utils.extractUninstallerJar(data));
        TestCase.assertEquals("Load engine classes", OK, Utils.loadEngineClasses(data));
        TestCase.assertEquals("Run uninstaller main class", OK, Utils.runUninstaller(data));

        Utils.stepUninstall(data);

        Utils.stepFinish();

        Utils.waitSecond(data, 5);

        TestCase.assertEquals("Uninstaller Finshed", 0, (Integer) System.getProperties().get("nbi.exit.code"));

        TestCase.assertFalse("NetBeans dir deleted", Utils.dirExist(NB_DIR_NAME).equals(OK));
        TestCase.assertFalse("Tomcat dir deleted", Utils.dirExist(TOMCAT_DIR_NAME).equals(OK));
        TestCase.assertFalse("GlassFish2 dir deleted", Utils.dirExist(GF2_DIR_NAME).equals(OK));
        TestCase.assertFalse("GlassFish3 dir deleted", Utils.dirExist(GF3_DIR_NAME).equals(OK));
    }
    */

    public static void phaseFour(TestData data) {
        //Installation
        //Utils.stepInstall(data);

        //finish
        //Utils.stepFinish();

        Utils.waitSecond(data, 5);

        TestCase.assertEquals("Installer Finshed", 0, (Integer) System.getProperties().get("nbi.exit.code"));

        TestCase.assertEquals(
            "NetBeans dir created",
            OK,
            Utils.dirExist( data.GetNetBeansInstallPath( ) )
          );
        
    }
    
    public static void phaseFive( TestData data )
    {
      TestCase.assertEquals(
          "Extract uninstaller jar",
          OK,
          Utils.extractUninstallerJar( data )
        );
      TestCase.assertEquals("Load engine classes", OK, Utils.loadEngineClasses(data));
      TestCase.assertEquals("Run uninstaller main class", OK, Utils.runUninstaller(data));

      Utils.stepUninstall(data);

      Utils.stepFinish();

      Utils.waitSecond(data, 5);

      TestCase.assertEquals("Uninstaller Finshed", 0, (Integer) System.getProperties().get("nbi.exit.code"));

      TestCase.assertFalse(
          "NetBeans dir deleted",
          Utils.dirExist( data.GetNetBeansInstallPath( ) ).equals( OK )
        );
      TestCase.assertFalse(
          "GlassFish2 dir deleted",
          Utils.dirExist( data.GetApplicationServerInstallPath( ) ).equals( OK )
        );
      if( data.m_bPreludePresents )
      {
        TestCase.assertFalse(
            "Prelude dir deleted",
            Utils.dirExist( data.GetApplicationServerPreludeInstallPath( ) ).equals( OK )
        );
      }
      TestCase.assertFalse(
          "Tomcat dir deleted",
          Utils.dirExist( data.GetTomcatInstallPath( ) ).equals( OK )
        );
      //TestCase.assertFalse("GlassFish3 dir deleted", Utils.dirExist(GF3_DIR_NAME, data).equals(OK));
    }
    
    public static void waitSecond(TestData data, int sec) {
        wait(data, 1000 * sec);
    }

    public static void wait(TestData data, int time) {
        try {
            java.lang.Thread.sleep(time);
        } catch (InterruptedException ex) {
            data.getLogger().log(Level.SEVERE, "Interrupted");
        }
    }

    public static String determineBuildNumber(TestData data) {
        data.setBuildNumber(null);

        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                URL downloadPage = new URL(data.getNetbeansDownloadPage() + "/js/build_info.js");
                InputStream in = downloadPage.openConnection(data.getProxy()).getInputStream();
                StringBuilder pageContent = new StringBuilder();

                byte[] buffer = new byte[1024];
                while (in.available() > 0) {
                    int read = in.read(buffer);

                    String readString = new String(buffer, 0, read);
                    for (String string : readString.split(NEWLINE_REGEXP)) {
                        pageContent.append(string).append(LINE_SEPARATOR);
                    }
                    wait(data, 100);
                }
                in.close();

                final Matcher matcher = PATTERN.matcher(pageContent);

                if (matcher.find()) {
                    data.setBuildNumber(matcher.group(1));
                    data.getLogger().log(Level.INFO, "Build number => " + data.getBuildNumber());
                    return OK;
                } else {
                    data.getLogger().log(Level.INFO, "Cannot find build number. Attempt #" + attempt);

                    FileWriter page = new FileWriter(new File(data.getTestWorkDir() + File.separator + "downloadpage" + attempt + ".html"));
                    page.write(pageContent.toString());
                    page.close();
                }
            } catch (Exception ex) {
                data.getLogger().log(Level.SEVERE, "Can not determine latest build. Attempt #" + attempt, ex);
            }
            waitSecond(data, 5);
        }
        return null;
    }

    public static String dirExist( String dirName )
    {
      if( null == dirName )
        return "Directory specified is null.";

      File dir = new File( dirName );
      if( dir.exists( ) && dir.isDirectory( ) )
      {
        return OK;
      }
      else
      {
        return "Directory " + dirName + "does not exist";
      }
    }

    private static String toString(Exception ex) {
        return ex.getClass().getName() + "=>" + ex.getMessage();
    }

    private static void watchProgressBar(TestData data, JFrameOperator frame, String label) {
        new JLabelOperator(frame, label); //dirty hack
        JProgressBarOperator progressBar = new JProgressBarOperator(frame);

        long waitingTime;
        for (waitingTime = 0; waitingTime < Utils.MAX_INSTALATION_WAIT; waitingTime += Utils.DELAY) {
            int val = ((JProgressBar) progressBar.getSource()).getValue();
            if (val >= 100) {
                break;
            }
            Utils.waitSecond(data, 5);
        }

        if (waitingTime >= Utils.MAX_INSTALATION_WAIT) {
            TestCase.fail("Installation timeout");
        }
    }

    public static String constructURL(TestData data) {
        String prefix = System.getProperty("test.installer.url.prefix");
        
        String bundleNamePrefix = System.getProperty("test.installer.bundle.name.prefix");
        //String prefix = (data.getBuildNumber() != null) ? "http://bits.netbeans.org/netbeans/6.0/nightly/latest/bundles/netbeans-6.0-" + data.getBuildNumber() : val;

        String bundleType = data.getInstallerType();
        if (bundleType == null || bundleType.equals("all")) {
            bundleType = "";
        } else {
            bundleType = "-" + bundleType;
        }

        String build_number = (Boolean.valueOf(System.getProperty("test.use.build.number"))) ? "-" + data.getBuildNumber() : "";
        return prefix + "/" + "bundles" +
                "/" + bundleNamePrefix +
                build_number + bundleType + "-" +
                data.getPlatformName() + "." + data.getPlatformExt();
    }

  public static void RunCommitTests( TestData data )
  {
    int iExitCode = -1;
    try
    {
    Runtime rt = Runtime.getRuntime( );
    // Get module path
    // c:\\work\\hg\\main\\php.editor
    String sExec =
        "D:\\Ant\\apache-ant-1.7.1\\bin\\ant.bat"
        + " -f ../all-tests.xml -Dbasedir=."
        + " -Dmodules.list=" + data.GetTestPackage( )
        // + " -Dtest.config=commit"
        + " -Dnetbeans.dest.dir=" + data.getWorkDirCanonicalPath( ) + File.separator + NB_DIR_NAME
        ;
    // Start tests
    System.out.println( "++++" + sExec );
    Process pTesting = rt.exec(
        sExec,
        null,
        new File( "E:\\buffer\\btd\\qa-functional" )
      );

    BufferedReader br = new BufferedReader(
        new InputStreamReader( pTesting.getInputStream( ) )
      );
    BufferedReader bre = new BufferedReader(
        new InputStreamReader( pTesting.getErrorStream( ) )
      );
    while( true )
    {
      String s;
      if( br.ready( ) )
      {
        s = br.readLine( );
        System.out.println( "[out] " + s );
      }
      else
      if( bre.ready( ) )
      {
        s = bre.readLine( );
        System.out.println( "[err] " + s );
      }
      else
      {
        try
        {
          iExitCode = pTesting.exitValue( );
          break;
        }
        catch( IllegalThreadStateException ex )
        {
          try
          {
            Thread.sleep( 50 );
          }
          catch( InterruptedException exx )
          {
            exx.printStackTrace( );
          }
        }
      }
      //pTesting.waitFor( );
        // ToDo
      // Check result
        // ToDo
    }
    }
    catch( IOException ex )
    {
      ex.printStackTrace( );
    }

    TestCase.assertEquals("Commit Test Result", 0, iExitCode );

    return;
  }
}
