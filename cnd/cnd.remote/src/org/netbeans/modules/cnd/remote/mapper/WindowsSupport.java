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
package org.netbeans.modules.cnd.remote.mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.util.Exceptions;

/**
 *
 */
public class WindowsSupport {
    /*package*/ static final String NFS_NETWORK_PROVIDER_NAME = "NFS Network";//NOI18N

    /**
     *
     * @param execEnv
     * @param otherExecEnv
     * @param providerName provider name ("NFS Network", "Microsoft Windows Network"), if not specified will return *all* drivers
     * @return
     */
    /*package*/static  Map<String, String> findMappings(ExecutionEnvironment execEnv,
            ExecutionEnvironment otherExecEnv, String providerName, boolean localToRemoteMap) {
        try {
            //trying to workaround #227719 - StringIndexOutOfBoundsException: String index out of range: -14

            ProcessBuilder pb = new ProcessBuilder("wmic", "/locale:ms_409","netuse", "list", "full");//NOI18N
            File file = File.createTempFile("wmic_output" , "" + System.currentTimeMillis());//NOI18N
            pb.redirectOutput(file);
            Process exec = ProcessUtils.ignoreProcessError(pb.start()); // out is redirectrd => no ProcessUtils.execute (otherwise unnecessary thread)
            try {
                int waitFor = exec.waitFor();
                if (waitFor != 0) {
                    if (providerName == null) {
                        return findMappingsNetUse(execEnv, otherExecEnv, localToRemoteMap);
                    }
                    return Collections.<String, String>emptyMap();
                }
            } catch (InterruptedException ex) {
                RemoteUtil.LOGGER.log(Level.FINE, null, ex);
                return Collections.<String, String>emptyMap();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("Unicode")));//NOI18N
            List<String> netOutput= new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                netOutput.add(line);
            }
            reader.close();
            Map<String, String> mappings = parseWmicNetUseOutput(otherExecEnv, netOutput, providerName, localToRemoteMap);
            if (mappings.isEmpty()) {
                return findMappingsNetUse(execEnv, otherExecEnv, localToRemoteMap);
            }
            return mappings;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return findMappingsNetUse(execEnv, otherExecEnv, localToRemoteMap);
    }

/**
     *
     * AccessMask=1179785
     * Comment=
     * ConnectionState=Disconnected
     * ConnectionType=Current Connection
     * Description=RESOURCE CONNECTED - VirtualBox Shared Folders
     * DisplayType=Share
     * InstallDate=
     * LocalName=G:
     * Name=\\vboxsrv\shared (G:)
     * Persistent=FALSE
     * ProviderName=VirtualBox Shared Folders
     * RemoteName=\\vboxsrv\shared
     * RemotePath=\\vboxsrv\shared
     * ResourceType=Disk
     * Status=Unavailable
     * UserName=
     *
     * @param hostName
     * @param output
     * @return
     * @throws IOException
     */
    /* package */ static Map<String, String> parseWmicNetUseOutput(ExecutionEnvironment execEnv,
        List<String> output, String providerName, boolean localToRemoteMap) throws IOException {
        return parseWmicNetUseOutput(execEnv.getHost(), output, providerName, true, localToRemoteMap);
    }

    /**
     * Returns local -> remote map
     * @param host
     * @param output
     * @param providerName
     * @return
     * @throws IOException
     */
    //for test purpose only
    static Map<String, String> parseWmicNetUseOutput(String host,
        List<String> output, String providerName, boolean isUnixStyle, boolean localToRemoteMap) throws IOException {
        List<NetUseStructure> elements = new ArrayList<>();
        NetUseStructure currentElement = null;
        //started from AccessMask
        for (String line : output) {
            //==
            String[] values=line.split("=");//NOI18N
            if (values == null || values.length != 2) {
                continue;
            }
            String name = values[0];
            String value = values[1];
            switch (name) {
                case "AccessMask"://NOI18N
                    //put previous if initialized
                    if (currentElement != null) {
                        elements.add(currentElement);
                    }
                    currentElement = new NetUseStructure();
                    break;
                case "LocalName"://NOI18N
                    if (currentElement == null) {
                        break;
                    }
                    currentElement.localDiskName = value;
                    break;
                case "RemotePath"://NOI18N
                    if (currentElement == null) {
                        break;
                    }
                    currentElement.setRemotePath(value);
                    break;
                case "ConnectionState"://NOI18N
                    if (currentElement == null) {
                        break;
                    }
                    currentElement.isConnected = !"Disconnected".equals(value);//NOI18N
                    break;
                case "ProviderName"://NOI18N
                    if (currentElement == null) {
                        break;
                    }
                    currentElement.isNFS = NFS_NETWORK_PROVIDER_NAME.equals(value);
                    currentElement.providerName = value;
                    break;

            }
        }
        //put the last one
        if (currentElement != null) {
            elements.add(currentElement);
        }
        Map<String, String> result = new HashMap<>();
        for (NetUseStructure element : elements) {
            if ( host.equals(element.host) && (element.isConnected || element.isNFS) && (providerName == null ||
                    providerName.equals(element.providerName))) {
                if (localToRemoteMap) {
                    result.put(element.localDiskName, isUnixStyle ? element.remoteFolder.replace('\\', '/') : element.remoteFolder); // NOI18N
                } else {
                    result.put(isUnixStyle ? element.remoteFolder.replace('\\', '/') : element.remoteFolder, element.localDiskName); // NOI18N
                }
            }
        }
        return result;
    }

    /**
     *
     * @param execEnv
     * @param otherExecEnv
     * @param localToRemoteMap use true if you wan to see L:->/export as a result map
     * @return
     */
    private static Map<String, String> findMappingsNetUse(ExecutionEnvironment execEnv, ExecutionEnvironment otherExecEnv, boolean localToRemoteMap) {
        try {
            //trying to workaround #227719 - StringIndexOutOfBoundsException: String index out of range: -14
            ProcessBuilder pb = new ProcessBuilder("net", "use");//NOI18N
            File file = File.createTempFile("netuse_output" , "" + System.currentTimeMillis());//NOI18N
            pb.redirectOutput(file);
            Process exec = ProcessUtils.ignoreProcessError(pb.start()); // out is redirectrd => no ProcessUtils.execute (otherwise unnecessary thread)
            try {
                int waitFor = exec.waitFor();
                if (waitFor != 0) {
                    return Collections.<String, String>emptyMap();
                }
            } catch (InterruptedException ex) {
                RemoteUtil.LOGGER.log(Level.FINE, null, ex);
                return Collections.<String, String>emptyMap();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("Unicode")));//NOI18N
            List<String> netOutput= new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                netOutput.add(line);
            }
            reader.close();
            //we will set LANG and LC_ALL env variables

            Map<String, String> mappings = parseNetUseOutput(otherExecEnv.getHost(), netOutput, localToRemoteMap);
            return mappings;
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
        return Collections.<String, String>emptyMap();
    }

/**
     * Parses "net use" Windows command output.
     * Here is an example of the output (note that "\\" means "\")
     *
     * ----- output example start -----
     *      New connections will not be remembered.
     *
     *
     *      Status       Local     Remote                               Network
     *
     *      -------------------------------------------------------------------------------
     *      OK           P:        \\\\serverOne\\pub                     Microsoft Windows Network
     *      Disconnected Y:        \\\\sErvEr_22_\\long name              Microsoft Windows Network
     *      OK           Z:        \\\\name.domen.domen2.zone\\username   Microsoft Windows Network
     *      The command completed successfully.
     *
     * ----- output example end -----
     *
     * @param hostName
     * @param outputReader
     * @return
     * @throws java.io.IOException
     */
    @SuppressWarnings("empty-statement")
    /* package */ static Map<String, String> parseNetUseOutput(String hostName, List<String> output, boolean localToRemoteMap) throws IOException {
        String line;
        // firtst, find the "---------" line and remember "Status  Local  Remote Network" one
        String lastNonEmptyLine = null;
        Iterator<String> iterator = output.iterator();
        while (iterator.hasNext()) {
            line = iterator.next();
            if (line == null || line.contains("----------------")) {//NOI18N
                break;
            }
            if (line.length() > 0) {
                lastNonEmptyLine = line;
            }
        }
        // we found "----";
        if (lastNonEmptyLine == null) {
            return Collections.<String, String>emptyMap();
        }

        // lastNonEmptyLine should contain "Status  Local  Remote Network" - probably localized
        String[] words = lastNonEmptyLine.split("[ \t]+"); // NOI18N
        if (words.length < 4) {
            return Collections.<String, String>emptyMap();
        }

        int nLocal = lastNonEmptyLine.indexOf(words[1]); // "Local"
        int nRemote = lastNonEmptyLine.indexOf(words[2]); // "Remote"
        int nNetwork = lastNonEmptyLine.indexOf(words[3]); // "Network"
        // neither of nLocal, nRemote and nNetwork can be negative - no check need
        List<NetUseStructure> elements = new ArrayList<>();
        NetUseStructure currentElement = null;
        //status can be empty
        while (iterator.hasNext()) {
            line = iterator.next();
        //for( line = reader.readLine(); line != null; line = reader.readLine() ) {  //NOI18N
            if (line.indexOf(':') != -1) {
                if (currentElement != null) {
                    elements.add(currentElement);
                }
                currentElement = new NetUseStructure();
                currentElement.localDiskName = line.substring(nLocal, nRemote -1).trim(); // something like X:
                String remote = line.substring(nRemote).trim(); // something like \\hostname\foldername
                if (remote.length() > nNetwork - 1 - nRemote && nNetwork - 1 - nRemote> 0) {
                    if (remote.charAt(nNetwork - 2 - nRemote) == ' ') {
                        remote = remote.substring(0, nNetwork - 1 - nRemote).trim();

                    }
                }
                currentElement.setRemotePath(remote);
                currentElement.isConnected = true;

                //do not see any reasons to use lower case here
            }
        }
        //add last one if any
        if (currentElement != null) {
            elements.add(currentElement);
        }

        Map<String, String> result = new HashMap<>();
        for (NetUseStructure element : elements) {
            if ( hostName.equals(element.host) && (element.isConnected || element.isNFS)) {
                if (localToRemoteMap) {
                    result.put(element.localDiskName, element.remoteFolder.replace('\\', '/')); // NOI18N
                } else {
                    result.put(element.remoteFolder.replace('\\', '/'), element.localDiskName); // NOI18N
                }
            }
        }
        return result;
    }



    /*protected*/
    static class NetUseStructure {

        String localDiskName;
        String host;
        String remoteFolder;
        boolean isConnected;
        boolean isNFS = false;
        String providerName;

        NetUseStructure() {
            isNFS = false;
        }

        void setRemotePath(String remote) {
            if (remote.length() > 2) {
                final String remotePath = remote.substring(2);
                //  System.out.println("remote=" + remotePath);
                int indexOf = remotePath.indexOf("\\"); //NOI18N
                //NOI18N
                //System.out.println("indexOf=" + indexOf);
                if (indexOf <= 0) {
                    host = null;
                    remoteFolder = null;
                    isConnected = false;
                    return;
                }
                host = remotePath.substring(0, indexOf);
                remoteFolder = remotePath.substring(indexOf);
            }
        }

        private String getRemoteFolder(ExecutionEnvironment execEnv) {
            return execEnv == null ? remoteFolder : CndPathUtilities.naturalizeSlashes(FileSystemProvider.getFileSystem(execEnv), remoteFolder);
        }
    }
}
