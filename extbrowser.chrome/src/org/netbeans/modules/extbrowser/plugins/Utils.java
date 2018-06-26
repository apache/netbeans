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
package org.netbeans.modules.extbrowser.plugins;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.Platform;
import com.sun.jna.PointerType;
import com.sun.jna.TypeMapper;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * @author ads
 *
 */
public final class Utils {
    
    private static final String APPDATA_CMD = "cmd /c echo %AppData%"; // NOI18N
    
    public static final Charset UTF_8 = Charset.forName("UTF-8");     // NOI18N
    
    private Utils(){
    }
    
    public static String[] getUserPaths(String... paths) {
        String[] result = new String[paths.length];
        String appRoot = getUserHome();

        if (appRoot == null) {
            return null;
        }
        for (int i = 0; i < paths.length; i++) {
            result[i] = appRoot + paths[i];
        }

        return result;
    }
    
    public static String unquote( String string ){
        if ( string.isEmpty() ){
            return string;
        }
        if ( string.charAt(0) == '"'){
            string = string.substring( 1 );
        }
        if ( string.charAt(string.length() -1) == '"'){
            string = string.substring( 0, string.length() -1);
        }
        return string;
    }
    
    public static JSONObject readFile( File file ){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader( 
                    new FileInputStream(file), UTF_8)); 
            return (JSONObject)JSONValue.parseWithException(reader);
        } catch (ParseException ex) {
            Logger.getLogger( Utils.class.getCanonicalName()).log(Level.FINE,
                    "cannot parse JSON file "+file , ex );
        } catch ( IOException e ){
            Logger.getLogger( Utils.class.getCanonicalName()).log(Level.FINE,
                    null , e );
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                    Logger.getLogger(Utils.class.
                            getCanonicalName()).log(Level.FINE, null, ex);
                }
            }
        }
        return null;
    }

    public static boolean compareChecksum( File zipFile, File checksumFile) {
        if (zipFile == null) {
            return true;
        }
        
        String checkSumName = checksumFile.getName();
        
        if (checksumFile.exists() && checksumFile.isFile()) {
            ZipFile extensionZip = null;
            try {
                extensionZip = new ZipFile(zipFile);
                ZipEntry entry = extensionZip.getEntry(checkSumName);
                
                if (entry != null) {                    
                    BufferedInputStream profileInput  = new BufferedInputStream(
                            new FileInputStream(checksumFile));
                    InputStream xpiInput = extensionZip.getInputStream(entry);
                    
                    try {
                        int red;
                        
                        do {
                            red = profileInput.read();
                            if ( red != xpiInput.read() ){
                                return false;
                            }
                        } 
                        while (red>=0);
                        
                        return true;
                    } 
                    finally {
                        profileInput.close();
                        xpiInput.close();
                    }
                    
                }
            } 
            catch (IOException ex) {
                Logger.getLogger(Utils.class.getCanonicalName()).
                    log(Level.SEVERE, null , ex); 
            } 
            finally {
                if (extensionZip != null) {
                    try {
                        extensionZip.close();
                    }
                    catch (IOException ex) {
                        Logger.getLogger(Utils.class.
                                getCanonicalName()).log(
                                        Level.SEVERE, 
                                            "Error closing zip file", ex);  // NOI18N
                    }
                }
            }
        }
        
        return false;
    }
    
    public static void rmDir(File folder) {
        if (!folder.exists()) {
            return;
        }
        
        File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                rmDir(child);
            }
        }
        folder.delete();
    }
    
    public static String readZip( File zipFile , String requiredName ){
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                if ( fileName.equals(requiredName)){
                    return readStream(zip.getInputStream(entry));
                }
            }
        }
        catch( IOException ex ){
            Logger.getLogger(Utils.class.getCanonicalName()).
                log(Level.INFO, null , ex);
        }
        finally {
            try {
                if ( zip!= null ){
                    zip.close();
                }
            }
            catch( IOException ex ){
                Logger.getLogger(Utils.class.getCanonicalName()).
                    log(Level.INFO, null , ex);
            }
        }      
        return null;
    }
    
    public static String readStream( InputStream inputStream )
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader( 
                inputStream ,UTF_8));
        return read( reader );
    }
    
    private static String read( BufferedReader reader ) throws IOException {
        String line;
        StringBuilder builder = new StringBuilder();
        do {
            line = reader.readLine();
            if (line != null) {
                builder.append(line);
            }
        }
        while (line != null);
        return builder.toString();
    }

    public static Document parseZipXml( File zipFile , String requiredName , 
            DocumentBuilder builder) throws SAXException
    {
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                if ( fileName.equals(requiredName)){
                    return builder.parse(zip.getInputStream(entry));
                }
            }
        }
        catch( IOException ex ){
            Logger.getLogger(Utils.class.getCanonicalName()).
                log(Level.INFO, null , ex);
        }
        finally {
            try {
                if ( zip!= null ){
                    zip.close();
                }
            }
            catch( IOException ex ){
                Logger.getLogger(Utils.class.getCanonicalName()).
                    log(Level.INFO, null , ex);
            }
        }      
        return null;
    }

    public static void extractFiles(File zipFile, File destDir) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        try {
            destDir.mkdirs();
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                
                if (entry.isDirectory()) {
                    File newFolder = new File(destDir, fileName);
                    newFolder.mkdirs();
                } 
                else {
                    File file = new File(destDir, fileName);
                    file.getParentFile().mkdirs();
                    if (file.exists() && file.isDirectory()) {
                        throw new IOException("Cannot write normal file " +
                        		"to existing directory with the same path");  // NOI18N
                    }
                    
                    BufferedOutputStream output = new BufferedOutputStream(
                            new FileOutputStream(file));
                    InputStream input = zip.getInputStream(entry);
                    
                    try {
                        final byte[] buffer = new byte[4096];
                        int len;
                        while ((len = input.read(buffer)) >= 0) {
                            output.write(buffer, 0, len);
                        }
                    } 
                    finally {
                        output.close();
                        input.close();
                    }
                }
            }
        } 
        finally {
            zip.close();
        }
    }
    
    public static List<Integer> getVersionParts(String version) {
        List<Integer> result = new ArrayList<Integer>();
        
        StringTokenizer tokens = new StringTokenizer(version, ".");
        while (tokens.hasMoreTokens()) {
            String nextToken = tokens.nextToken();
            try {
                if (nextToken.contains("a")) {
                    int index = nextToken.indexOf("a");

                    String first = nextToken.substring(0, index);
                    String second = nextToken.substring(index + 1, 
                            nextToken.length());

                    // version xxbyy is greater than any version xx-1 without a beta
                    // but less than version xx without a beta
                    result.add(Integer.parseInt(first) - 1);
                    result.add(-1);
                    result.add(Integer.parseInt(second));
                } else if (nextToken.contains("b")) {
                    int index = nextToken.indexOf("b");

                    String first = nextToken.substring(0, index);
                    String second = nextToken.substring(index + 1, 
                            nextToken.length());

                    // version xxbyy is greater than any version xx-1 without a beta
                    // but less than version xx without a beta
                    result.add(Integer.parseInt(first) - 1);
                    result.add(Integer.parseInt(second));
                } else {
                    result.add(Integer.parseInt(nextToken));
                }
            } catch (NumberFormatException ex) {
                // skip values that are not numbers
            }
        }
        
        return result;
    }

    private static String getUserHome() {
        String userHome = System.getProperty("user.home"); // NOI18N

        if (!Utilities.isWindows()) {
            return userHome;
        } 
        else {
            /*BufferedReader br = null;
            try {
                Process process = Runtime.getRuntime().exec(APPDATA_CMD);
                process.waitFor();

                InputStream input = process.getInputStream();
                br = new BufferedReader(new InputStreamReader(input));

                while (br.ready()) {
                    String nextLine = br.readLine();

                    if (nextLine.trim().length() == 0) continue;

                    File f = new File(nextLine.trim());
                    if (f.exists() && f.isDirectory()) {
                        return f.getAbsolutePath();
                    }
                }
            }
            catch (Exception ex) {
                Logger.getLogger(Utils.class.getCanonicalName()).
                    log(Level.INFO, "Unable to run process: " + APPDATA_CMD, ex );      // NOI18N
            }
            finally {
                if (br != null) {
                    try {
                        br.close();
                    }
                    catch (IOException ex) {
                        Logger.getLogger(Utils.class.getCanonicalName()).
                            log(Level.INFO, 
                                    "Unable close process input stream reader " ,       // NOI18N 
                                        ex );      
                    }
                }
            }*/

            //return userHome + File.separator + "Application Data";  // NOI18N
            return System.getenv("AppData");                          // NOI18N
        }
    }

    // inspired by: http://stackoverflow.com/a/586917/1381125
    public static synchronized String getLOCALAPPDATAonWinXP() {
        assert Platform.isWindows() : "cannot call getLOCALAPPDATAonWinXP on non-Windows OS"; // NOI18N
        if (Platform.isWindows()) {
            char[] pszPath = new char[Shell32.MAX_PATH];
            try {
                if (Shell32_INSTANCE == null) {
                    Shell32_INSTANCE = (Shell32) Native.loadLibrary("shell32",
                            Shell32.class, OPTIONS);
                }
                int hResult = Shell32_INSTANCE.SHGetFolderPath(null, Shell32.CSIDL_LOCAL_APPDATA,
                        null, Shell32.SHGFP_TYPE_CURRENT, pszPath);
                if (Shell32.S_OK == hResult) {
                    String path = new String(pszPath);
                    int len = path.indexOf('\0');
                    path = path.substring(0, len);
                    return path;
                }
            } catch (Throwable t){
                // ignore
            }
        }
        return null;
    }

    private static Shell32 Shell32_INSTANCE;
    
    private static Map<String, Object> OPTIONS = new HashMap<String, Object>();
    static {
        OPTIONS.put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
        OPTIONS.put(Library.OPTION_FUNCTION_MAPPER,
                W32APIFunctionMapper.UNICODE);
    }

    static class HANDLE extends PointerType implements NativeMapped {
    }

    static class HWND extends HANDLE {
    }

    static interface Shell32 extends Library {

        public static final int MAX_PATH = 260;
        public static final int CSIDL_LOCAL_APPDATA = 0x001c;
        public static final int SHGFP_TYPE_CURRENT = 0;
        public static final int SHGFP_TYPE_DEFAULT = 1;
        public static final int S_OK = 0;

        /**
         * see http://msdn.microsoft.com/en-us/library/bb762181(VS.85).aspx
         * 
         * HRESULT SHGetFolderPath( HWND hwndOwner, int nFolder, HANDLE hToken,
         * DWORD dwFlags, LPTSTR pszPath);
         */
        public int SHGetFolderPath(HWND hwndOwner, int nFolder, HANDLE hToken,
                int dwFlags, char[] pszPath);

    }

    
    
}
