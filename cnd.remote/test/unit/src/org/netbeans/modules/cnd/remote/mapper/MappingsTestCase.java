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
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.openide.util.Exceptions;

/**
 *
 */
public class MappingsTestCase extends RemoteTestBase {


    @ForAllEnvironments
    public void testAnalyzer() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        HostMappingsAnalyzer ham = new HostMappingsAnalyzer(execEnv); //sg155630@elif
        final Map<String, String> mappings = ham.getMappings();
        assert mappings != null;
        String sortedMappings = toSortedString(mappings);        
        String referenceMappings = getReferenceMappingSortedString();
        if (referenceMappings != null && !referenceMappings.isEmpty()) {
            System.err.printf("\nMappings for %s differ.\nReference mappins are:%s\nActual mappings are:%s\n",
                    execEnv, referenceMappings, sortedMappings);
            if (!referenceMappings.equals(sortedMappings)) {
                assertTrue("Mappings differ", false);
            }
        } else {
            System.err.printf("Mappings for %s:%s\n", execEnv, sortedMappings);
        }
    }
    
    public void testWindowsNFSHostAnalyzer() {
        try {
            Map<String, String> mapping = new HashMap<>();
            getMappingsImpl("localhost", "serverOne", mapping, WindowsSupport.NFS_NETWORK_PROVIDER_NAME, true);
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                assert "P:".equals(entry.getKey()) && "/pub".equals(entry.getValue());
            }
            mapping.clear();
            getMappingsImpl("localhost", "sErvEr_22_", mapping, WindowsSupport.NFS_NETWORK_PROVIDER_NAME, true);            
            assert mapping.isEmpty();
            
            mapping.clear();;
            getMappingsImpl("localhost", "serverTwo", mapping, WindowsSupport.NFS_NETWORK_PROVIDER_NAME, true);            
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                System.out.println("NFS: LocalPath:" + entry.getKey() + " RemotePath:" + entry.getValue());
                assert "D:".equals(entry.getKey()) && "/Тестовая папка".equals(entry.getValue());
            }            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    public void testWindowsSambaHostAnalyzer() {
        try {
            Map<String, String> mapping = new HashMap<>();
            getMappingsImpl("localhost", "serverOne", mapping, null, false);
            assert mapping.size () == 1;
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                assert "P:".equals(entry.getValue()) && "/pub".equals(entry.getKey());
            }
            mapping.clear();            
            getMappingsImpl("localhost", "sErvEr_22_", mapping, null, false);
            assert mapping.size() == 1;
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                assert "Y:".equals(entry.getValue()) && "/long name".equals(entry.getKey());
            }
            mapping.clear();
            getMappingsImpl("localhost", "name.domen.domen2.zone", mapping, null, false);
            assert mapping.size () == 1;
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                assert "Z:".equals(entry.getValue()) && "/sg155630".equals(entry.getKey());
            }            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }    
    
    private void getMappingsImpl(String firstHost, String secondHost, Map<String, String> mappingsFirst2Second, String providerName,boolean localToRemoteMap) throws IOException{
        // all maps are host network name -> host local name
        Map<String, String> firstNetworkNames2Inner = WindowsSupport.parseWmicNetUseOutput(secondHost, getWmicOutput(), providerName, true, localToRemoteMap);
        mappingsFirst2Second.putAll(firstNetworkNames2Inner);        
    }    

    private String getReferenceMappingSortedString() throws Exception {
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String mspec = NativeExecutionTestSupport.getMspec(getTestExecutionEnvironment());
            String section = "remote." + mspec + ".pathMappings";
        SortedSet<String> sortedKeySet = new TreeSet<>(rcFile.getKeys(section));
        if (sortedKeySet.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String key : sortedKeySet) {
            String value = rcFile.get(section, key);
            key = expandMacro(key);
            value = expandMacro(value);
            sb.append("\n\t").append(key).append('=').append(value);
        }
        return sb.toString();
    }

    private String expandMacro(String text) throws Exception {
        String localHostName = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal()).getHostname();
        return text.replace("${LOCALHOST}", localHostName);
    }

    private String toSortedString(Map<String, String> mappings) {
        TreeMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.putAll(mappings);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            sb.append("\n\t").append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }
    
    
    private static List<String> getList(String string) {
        final List<String> result = new LinkedList<>();
        final BufferedReader br = new BufferedReader(new StringReader(string));

        try {
            String line;            
                while ((line = br.readLine()) != null) {
                    result.add(line);
                }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }

        return result;        
    }
    
    private static List<String> getWmicOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("AccessMask=1179785\n");
        sb.append("Comment=\n");
        sb.append("ConnectionState=Connected\n");
        sb.append("ConnectionType=Current Connection\n");
        sb.append("Description=NFS Network\n");
        sb.append("DisplayType=Share\n");
        sb.append("InstallDate=\n");
        sb.append("LocalName=P:\n");
        sb.append("Name=\\\\serverOne\\pub (P:)\n");
        sb.append("Persistent=FALSE\n");
        sb.append("ProviderName=NFS Network\n");
        sb.append("RemoteName=\\\\serverOne\\pub\n");
        sb.append("RemotePath=\\\\serverOne\\pub\n");
        sb.append("ResourceType=Disk\n");
        sb.append("Status=Available\n");
        sb.append("UserName=\n");
        sb.append("\n");
        sb.append("AccessMask=1179785\n");
        sb.append("Comment=\n");
        sb.append("ConnectionState=Connected\n");
        sb.append("ConnectionType=Current Connection\n");
        sb.append("Description=NFS Network\n");
        sb.append("DisplayType=Share\n");
        sb.append("InstallDate=\n");
        sb.append("LocalName=D:\n");
        sb.append("Name=\\\\serverTwo\\Тестовая папка (D:)\n");
        sb.append("Persistent=FALSE\n");
        sb.append("ProviderName=NFS Network\n");
        sb.append("RemoteName=\\\\serverTwo\\Тестовая папка\n");
        sb.append("RemotePath=\\\\serverTwo\\Тестовая папка\n");
        sb.append("ResourceType=Disk\n");
        sb.append("Status=Available\n");
        sb.append("UserName=\n");
        sb.append("\n");        
        sb.append("AccessMask=1179785\n");
        sb.append("Comment=\n");
        sb.append("ConnectionState=Connected\n");
        sb.append("ConnectionType=Current Connection\n");
        sb.append("Description=RESOURCE CONNECTED - VirtualBox Shared Folders\n");
        sb.append("DisplayType=Share\n");
        sb.append("InstallDate=\n");
        sb.append("LocalName=Y:\n");
        sb.append("Name=\\\\sErvEr_22_\\long name (Y:)\n");
        sb.append("Persistent=FALSE\n");
        sb.append("ProviderName=VirtualBox Shared Folders\n");
        sb.append("RemoteName=\\\\sErvEr_22_\\long name\n");
        sb.append("RemotePath=\\\\sErvEr_22_\\long name\n");
        sb.append("ResourceType=Disk\n");
        sb.append("Status=Available\n");
        sb.append("UserName=\n");        
        sb.append("\n");
        sb.append("AccessMask=1179785\n");
        sb.append("Comment=\n");
        sb.append("ConnectionState=Connected\n");
        sb.append("ConnectionType=Current Connection\n");
        sb.append("Description=RESOURCE CONNECTED - VirtualBox Shared Folders\n");
        sb.append("DisplayType=Share\n");
        sb.append("InstallDate=\n");
        sb.append("LocalName=Z:\n");
        sb.append("Name=\\\\name.domen.domen2.zone\\sg155630 (Z:)\n");
        sb.append("Persistent=FALSE\n");
        sb.append("ProviderName=VirtualBox Shared Folders\n");
        sb.append("RemoteName=\\\\name.domen.domen2.zone\\sg155630\n");
        sb.append("RemotePath=\\\\name.domen.domen2.zone\\sg155630\n");
        sb.append("ResourceType=Disk\n");
        sb.append("Status=Available\n");
        sb.append("UserName=\n"); 
        return getList(sb.toString());
    }
    
    public void testHostMappingProviderWindows_English_WMIC () throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("AccessMask=1179785\n");
        sb.append("Comment=\n");
        sb.append("ConnectionState=Connected\n");
        sb.append("ConnectionType=Current Connection\n");
        sb.append("Description=RESOURCE CONNECTED - VirtualBox Shared Folders\n");
        sb.append("DisplayType=Share\n");
        sb.append("InstallDate=\n");
        sb.append("LocalName=P:\n");
        sb.append("Name=\\\\serverOne\\pub (P:)\n");
        sb.append("Persistent=FALSE\n");
        sb.append("ProviderName=VirtualBox Shared Folders\n");
        sb.append("RemoteName=\\\\serverOne\\pub\n");
        sb.append("RemotePath=\\\\serverOne\\pub\n");
        sb.append("ResourceType=Disk\n");
        sb.append("Status=Available\n");
        sb.append("UserName=\n");
        sb.append("\n");
        sb.append("AccessMask=1179785\n");
        sb.append("Comment=\n");
        sb.append("ConnectionState=Connected\n");
        sb.append("ConnectionType=Current Connection\n");
        sb.append("Description=RESOURCE CONNECTED - VirtualBox Shared Folders\n");
        sb.append("DisplayType=Share\n");
        sb.append("InstallDate=\n");
        sb.append("LocalName=Y:\n");
        sb.append("Name=\\\\sErvEr_22_\\long name (Y:)\n");
        sb.append("Persistent=FALSE\n");
        sb.append("ProviderName=VirtualBox Shared Folders\n");
        sb.append("RemoteName=\\\\sErvEr_22_\\long name\n");
        sb.append("RemotePath=\\\\sErvEr_22_\\long name\n");
        sb.append("ResourceType=Disk\n");
        sb.append("Status=Available\n");
        sb.append("UserName=\n");        
        sb.append("\n");
        sb.append("AccessMask=1179785\n");
        sb.append("Comment=\n");
        sb.append("ConnectionState=Connected\n");
        sb.append("ConnectionType=Current Connection\n");
        sb.append("Description=RESOURCE CONNECTED - VirtualBox Shared Folders\n");
        sb.append("DisplayType=Share\n");
        sb.append("InstallDate=\n");
        sb.append("LocalName=Z:\n");
        sb.append("Name=\\\\name.domen.domen2.zone\\sg155630 (Z:)\n");
        sb.append("Persistent=FALSE\n");
        sb.append("ProviderName=VirtualBox Shared Folders\n");
        sb.append("RemoteName=\\\\name.domen.domen2.zone\\sg155630\n");
        sb.append("RemotePath=\\\\name.domen.domen2.zone\\sg155630\n");
        sb.append("ResourceType=Disk\n");
        sb.append("Status=Available\n");
        sb.append("UserName=\n");                
        Map<String, String> map;
        map = WindowsSupport.parseWmicNetUseOutput("serverOne", getList(sb.toString()), null, true, false);
        assert map != null && map.size() == 1 && "P:".equals(map.get("/pub"));

        map = WindowsSupport.parseWmicNetUseOutput("sErvEr_22_", getList(sb.toString()), null, true, false);
        assert map != null && map.size() == 1 && "Y:".equals(map.get("/long name"));

        map = WindowsSupport.parseWmicNetUseOutput("name.domen.domen2.zone", getList(sb.toString()), null,true, false);
        assert map != null && map.size() == 1 && "Z:".equals(map.get("/sg155630"));        
    }

    public void testHostMappingProviderWindows_English() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("New connections will not be remembered.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("Status       Local     Remote                               Network\n");
        sb.append("\n");
        sb.append("-------------------------------------------------------------------------------\n");
        sb.append("OK           P:        \\\\serverOne\\pub                     Microsoft Windows Network\n");
        sb.append("Disconnected Y:        \\\\sErvEr_22_\\long name              Microsoft Windows Network\n");
        sb.append("OK           Z:        \\\\name.domen.domen2.zone\\sg155630   Microsoft Windows Network\n");
        sb.append("The command completed successfully.\n");
        Map<String, String> map;
        map = WindowsSupport.parseNetUseOutput("serverOne", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "P:".equals(map.get("/pub"));

        map = WindowsSupport.parseNetUseOutput("sErvEr_22_", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "Y:".equals(map.get("/long name"));

        map = WindowsSupport.parseNetUseOutput("name.domen.domen2.zone", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "Z:".equals(map.get("/sg155630"));
    }

    public void testHostMappingProviderWindows_English2() throws Exception {
        // Bug #214622
        StringBuilder sb = new StringBuilder();
        sb.append("New connections will be remembered.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("Status       Local     Remote                    Network\n");
        sb.append("\n");
        sb.append("-------------------------------------------------------------------------------\n");
        sb.append("Disconnected K:        \\\\amkar.russia.sun.com\\home\n");
        sb.append("                                                Microsoft Windows Network\n");
        sb.append("             X:        \\\\vboxsvr\\cnd-main        VirtualBox Shared Folders\n");
        sb.append("             Z:        \\\\vboxsvr\\exchange        VirtualBox Shared Folders\n");
        sb.append("The command completed successfully.\n");
        Map<String, String> map;
        map = WindowsSupport.parseNetUseOutput("amkar.russia.sun.com", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "K:".equals(map.get("/home"));

        map = WindowsSupport.parseNetUseOutput("vboxsvr", getList(sb.toString()), false);
        assert map != null && map.size() == 2;
    }
    
    public void testHostMappingProviderWindows_English3() throws Exception {
        // Bug #213365
        StringBuilder sb = new StringBuilder();
        sb.append("New connections will be remembered.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("Status       Local     Remote                    Network\n");
        sb.append("\n");
        sb.append("-------------------------------------------------------------------------------\n");
        sb.append("Disconnected K:        \\\\amkar.rus.sun.com\\home\n");
        sb.append("                                                Microsoft Windows Network\n");
        sb.append("             X:        \\\\vboxsvr\\cnd-main        VirtualBox Shared Folders\n");
        sb.append("             Z:        \\\\vboxsvr\\exchange        VirtualBox Shared Folders\n");
        sb.append("The command completed successfully.\n");
        Map<String, String> map;
        map = WindowsSupport.parseNetUseOutput("amkar.rus.sun.com", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "K:".equals(map.get("/home"));

        map = WindowsSupport.parseNetUseOutput("vboxsvr", getList(sb.toString()), false);
        assert map != null && map.size() == 2 && "X:".equals(map.get("/cnd-main")) && "Z:".equals(map.get("/exchange"));
    }

    public void testHostMappingProviderWindows_Russian() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Новые подключения не будут запомнены.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("Состояние    Локальный Удаленный                            Сеть\n");
        sb.append("\n");
        sb.append("-------------------------------------------------------------------------------\n");
        sb.append("OK           P:        \\\\serverOne\\pub                     Microsoft Windows Network\n");
        sb.append("Нет доступа  Y:        \\\\sErvEr_22_\\long name              Microsoft Windows Network\n");
        sb.append("OK           Z:        \\\\name.domen.domen2.zone\\sg155630   Microsoft Windows Network\n");
        sb.append("Команда выполнена успешно.\n");
        Map<String, String> map;
        map = WindowsSupport.parseNetUseOutput("serverOne", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "P:".equals(map.get("/pub"));

        map = WindowsSupport.parseNetUseOutput("sErvEr_22_", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "Y:".equals(map.get("/long name"));

        map = WindowsSupport.parseNetUseOutput("name.domen.domen2.zone", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "Z:".equals(map.get("/sg155630"));
        System.out.println("Everything is OK for Russian output");
    }

    public void testHostMappingProviderWindows_German() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Neue Verbindungen werden gespeichert.\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("Status           Lokal     Remote                               Netzwerk\n");
        sb.append("\n");
        sb.append("-------------------------------------------------------------------------------\n");
        sb.append("OK               P:        \\\\serverOne\\pub                     Microsoft Windows-Netzwerk\n");
        sb.append("Nicht verfügbar  Y:        \\\\sErvEr_22_\\long name              Microsoft Windows-Netzwerk\n");
        sb.append("OK               Z:        \\\\name.domen.domen2.zone\\sg155630   Microsoft Windows-Netzwerk\n");
        sb.append("Der Befehl wurde erfolgreich ausgefÃ¼hrt.\n");
        Map<String, String> map;
        map = WindowsSupport.parseNetUseOutput("serverOne", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "P:".equals(map.get("/pub"));

        map = WindowsSupport.parseNetUseOutput("sErvEr_22_", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "Y:".equals(map.get("/long name"));

        map = WindowsSupport.parseNetUseOutput("name.domen.domen2.zone", getList(sb.toString()), false);
        assert map != null && map.size() == 1 && "Z:".equals(map.get("/sg155630"));
    }

    public void testHostMappingProviderWindows_Unexpected() throws Exception {
        // Test for error processing.
        // In the case net use prints something unexpected no exceptions should be thrown

        // 1. No header
        String netUseOutput_1 =
                "-------------------------------------------------------------------\n" +
                "OK       P:     \\\\server_1\\pub      Microsoft Windows-Netzwerk\n";
        WindowsSupport.parseNetUseOutput("server_1", getList(netUseOutput_1), false);

        // 2. No "-----------------" line
        String netUseOutput_2 =
                "Status   Local  Remote               Network\n" +
                "OK       P:     \\\\server_1\\pub      Microsoft Windows-Netzwerk\n";
        WindowsSupport.parseNetUseOutput("server_1", getList(netUseOutput_2), false);

        // 3. short host
        String netUseOutput_3 =
                "\n" +
                "Status   Local  Remote               Network\n" +
                "-------------------------------------------------------------------\n" +
                "         P:     xx                     Microsoft Windows-Netzwerk\n";
        WindowsSupport.parseNetUseOutput("server_1", getList(netUseOutput_3), false);

        // 4. server without "\\"
        String netUseOutput_4 =
                "\n" +
                "Status   Local  Remote               Network\n" +
                "-------------------------------------------------------------------\n" +
                "OK       P:     server_1_pub           Microsoft Windows-Netzwerk\n";
        WindowsSupport.parseNetUseOutput("server_1", getList(netUseOutput_4), false);

        // 5. empty line
        WindowsSupport.parseNetUseOutput("server_1", getList(""), false);

        // 6. just some crap
        String netUseOutput_6 = "qwe\nasd\n---------------\nzxc\n123\n456\n\n\n";
        WindowsSupport.parseNetUseOutput("server_1", getList(netUseOutput_6), false);
    }

//    public void testHostMappingProviderSamba() throws Exception {
//        Map<String, String> map;
//        map = HostMappingProviderSamba.parseOutput(new StringReader(getConfigFile().toString()));
//        assert map != null && map.size() == 1 && "/export/pub".equals(map.get("pub"));
//    }
//    
    
    private static StringBuilder getConfigFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("[global]\n");
        sb.append("\n");
        sb.append("      workgroup = staff\n");
        sb.append("      server string = Eaglet-SR Samba Server\n");
        sb.append("      log file = /var/adm/samba_log.%m\n");
        sb.append("      security = SHARE\n");
        sb.append("\n");
        sb.append("[pub]\n");
        sb.append("\n");
        sb.append("      comment = pub\n");
        sb.append("      path = /export/pub\n");
        sb.append("      force user = tester\n");
        sb.append("      force group = other\n");
        sb.append("      read only = No\n");
        sb.append("      guest ok = No\n");
        return sb;
    }

    public void testSimpleConfigParser() {
        SimpleConfigParser p = new SimpleConfigParser();
        StringBuilder sb = getConfigFile();
        sb.insert(0, "orphan=orphanValue\n");
        p.parse(new StringReader(sb.toString()));
        assert p.getSections().contains("global");
        assert p.getOrphanAttributes().get("orphan").equals("orphanValue");
        assert p.getAttributes("pub").get("path").equals("/export/pub");
    }

//    public void testMappingsValidation() {
//        if (canTestRemote()) {
//            RemotePathMap.validateMapping(getHKey(), rpath, "/net/endif/export");
//        }
//    }

    // we need this since some methods are without @ForAllEnvironments
    public MappingsTestCase(String testName) {
        super(testName);
    }

    public MappingsTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(MappingsTestCase.class);
    }

}
