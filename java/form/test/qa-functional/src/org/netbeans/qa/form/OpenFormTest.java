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
package org.netbeans.qa.form;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import junit.framework.*;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.FrameOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.*;

/**
 *
 * @author Marek G.
 * 
 * 
 */
 
public class OpenFormTest extends JellyTestCase {
    
    public OpenFormTest(java.lang.String testName) {
        super(testName);
    }
    
      public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(OpenFormTest.class).addTest(
                "testOpenForm"
                ).gui(true).enableModules(".*").clusters(".*"));
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    private List getJavaFormList() {
        String dataDir = getDataDir().getAbsolutePath();
        String fileSeparator =  System.getProperty("file.separator");
        
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        
        List list = new ArrayList();
        
        try {
            //System.out.println("PATH : " + dataDir + fileSeparator + "FormList.dat");
            fileReader = new FileReader(dataDir + fileSeparator + "FormList.dat");
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                list.add(line);
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex){
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedReader!= null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return list;
    }
    
    public void testOpenForm() {
        
        System.out.println("XXXXXXXXXXXXX");
        System.out.println("getDataDir          = " + getDataDir());
        System.out.println("getWorkDirPath      = " + getWorkDirPath());
        
        System.out.println("XXXXXXXXXXXXX");
        String dataDir = getDataDir().getAbsolutePath();
        String workdirpath = getWorkDirPath();
        String formPath = null;
        String fileSeparator = System.getProperty("file.separator");
        String lineSeparator = System.getProperty("line.separator");
        String xtest_sketchpad   = System.getProperty("xtest.sketchpad");
        
        List list = getJavaFormList();
        //        String prePath = "/space/cvs-netbeans/form/src";
        String prePath = workdirpath.substring(0,workdirpath.indexOf(fileSeparator + "testOpenForm"));
        
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        
        try {
            fileReader = new FileReader(dataDir + fileSeparator + "OpenForm.template");
            bufferedReader = new BufferedReader(fileReader);
            fileWriter = new FileWriter(workdirpath + fileSeparator +  "OpenForm.html");
            bufferedWriter = new BufferedWriter(fileWriter);
                        
            String line = null;
            while (!(line = bufferedReader.readLine()).equals("<PUT CONTENT HERE>")) {
                //System.out.println("LINE : " + line + " : ");
                bufferedWriter.write(line + lineSeparator);                
            }
            
            int lastindex         = 0;
            String module         = null;
            String directory      = null;
            String filename       = null;
            String filenamenoext  = null;
            String fullPath       = null;
            ActionNoBlock actionNoBlock;
            JFileChooserOperator jFileChooserOperator;
            FormDesignerOperator formDesignerOperator = null;
            FrameOperator frameOperator = null;
            for (ListIterator listIterator = list.listIterator(); listIterator.hasNext(); ) {
                /** read items from list formPath: EventCustomEditor.form, FormCustomEditor.form,
                 * FormCustomEditorAdvanced.form, MethodPicker.form, ParametersPicker.form, PropertyPicker.form, CustomCodeEditor.form, StringArrayCustomEditor.form, ConnectionPanel1.form, ConnectionPanel2.form, ConnectionPanel3.form]
                 **/
                formPath = (String) listIterator.next();
                
                fullPath = new StringBuffer(prePath).append(fileSeparator).append(formPath).toString();
                System.out.println("fullpath: " + fullPath);
                lastindex = fullPath.lastIndexOf(fileSeparator);
     
                //                module = formPath.substring(0, formPath.indexOf(fileSeparator));
                directory = dataDir + fileSeparator + "OpenForm"; //fullPath.substring(0,lastindex);
                filename  = fullPath.substring(lastindex + 1);               
                filenamenoext = filename.substring(0, filename.lastIndexOf("."));               
                actionNoBlock = new ActionNoBlock("File|Open File...", null);
                actionNoBlock.perform();
                jFileChooserOperator = new JFileChooserOperator();
                jFileChooserOperator.setCurrentDirectory(new File(directory));
                jFileChooserOperator.selectFile(filename);
                jFileChooserOperator.approve();
                formDesignerOperator = new FormDesignerOperator(filenamenoext);
                
                
                formDesignerOperator.btPreviewForm().push();
                frameOperator = new FrameOperator("Design Preview [" + filenamenoext +"]");
                
                
                // org.netbeans.jemmy.util.PNGEncoder.captureScreen(formDesignerOperator.fakePane().getSource(), workdirpath + fileSeparator + filenamenoext + ".png");
                new EventTool().waitNoEvent(1000);
                System.out.println("xtest_sketchpad: " + xtest_sketchpad);
                copy(new File(dataDir + fileSeparator + "OpenForm"+ fileSeparator + filenamenoext + ".png"), new File(xtest_sketchpad + fileSeparator + filenamenoext + ".png"));
                
                org.netbeans.jemmy.util.PNGEncoder.captureScreen(frameOperator.getSource(), workdirpath + fileSeparator + filenamenoext + ".png");
                
                frameOperator.close();
                formDesignerOperator.close();
                
                bufferedWriter.write("<TABLE width=\"98%\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\">");
                bufferedWriter.write("<TR bgcolor=\"#A6CAF0\" align=\"center\">");
                bufferedWriter.write("<TD ALIGN=\"LEFT\" colspan=\"2\"> " + "<B>" + filenamenoext + "</B></TD>");
                bufferedWriter.write("</TR>");
                bufferedWriter.write("<TR bgcolor=\"#A6CAF0\" align=\"center\">");
                bufferedWriter.write("<TD ALIGN=\"LEFT\" >" + "<B>" + " Current testing " + "</B></TD>");
                bufferedWriter.write("<TD ALIGN=\"LEFT\" >" + "<B>" + " <FONT color=\"#EEEE0E\">Golden file</FONT> " + "</B></TD>");
                bufferedWriter.write("</TR>");
                bufferedWriter.write("<TR>");
                bufferedWriter.write("<TD ALIGN=\"CENTER\"><IMG BORDER=\"2\" TITLE=\"" + filenamenoext + "\" SRC=\"" + filenamenoext + ".png" + "\"></IMG></TD>");
                bufferedWriter.write("<TD bgcolor=\"#EEEE0E\" ALIGN=\"CENTER\"><IMG BORDER=\"2\" TITLE=\"" + filenamenoext + "\" SRC=\"" + "../../sketch-pad" + fileSeparator + filenamenoext + ".png" + "\"></IMG></TD>");
                bufferedWriter.write("</TR>");
                bufferedWriter.write("</TABLE>");
                bufferedWriter.write("<HR>");
                
            }
            
            while ((line = bufferedReader.readLine()) != null){
                //                System.out.println("XLINE : " + line + " : ");
                bufferedWriter.write(line + lineSeparator);
            }
            
        } catch (FileNotFoundException ex) {
        } catch (IOException ex){
        } finally {
            try {
                if (bufferedReader!= null) {
                    bufferedReader.close();
                }
                if (bufferedWriter!= null) {
                    bufferedWriter.close();
                }
            } catch (IOException ex) {
            }
        }
        
        assertTrue("Look at the work directory for particular form's screenshots", true);
    }
    
    private void copy(File source, File destination) throws IOException {
        FileChannel input = null, output = null;
        try {
            input = new FileInputStream(source).getChannel();
            output = new FileOutputStream(destination).getChannel();
            
            long size = input.size();
            MappedByteBuffer buffer = input.map(FileChannel.MapMode.READ_ONLY, 0, size);
            
            output.write(buffer);
            
        } finally {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
        }
    }
    
}
