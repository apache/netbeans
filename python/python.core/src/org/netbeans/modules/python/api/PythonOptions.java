package org.netbeans.modules.python.api;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

public class PythonOptions {
    public static String PYTHON_COMMAND = "python.command";
    public static String PYTHON_NATIVE = "python.native" ;
    public static String PYTHON_CONSOLE = "python.console";
    public static String PROMPT_FOR_ARGS = "prompt.for.args";
    public static String PYTHON_DEFAULT = "python.default";
    // debugger's options
    public static String DBG_LISTENING_PORT = "dbg.listening.port";
    public static String STOP_AT_FIRST_LINE = "dbg.stopatfirstline";
    // debugger's shell coloring options
    public static String DBGSHELL_FONT = "dbg.dbgshell.font";
    private static Font  DBGSHELL_DEFAULT_FONT = new Font ("Courier",Font.PLAIN,12) ;
    public static String DBGSHELL_BACKGROUND_COLOR = "dbg.dbgshell.background.color";
    private static Color DBGSHELL_BACKGROUND_COLOR_DEFAULT = Color.WHITE ;
    public static String DBGSHELL_INFO_COLOR = "dbg.dbgshell.messages.color";
    private static Color DBGSHELL_INFO_COLOR_DEFAULT = Color.BLACK ;
    public static String DBGSHELL_HEADER_COLOR = "dbg.dbgshell.header.color";
    private static Color DBGSHELL_HEADER_COLOR_DEFAULT = Color.BLUE ;
    public static String DBGSHELL_WARNING_COLOR = "dbg.dbgshell.warning.color";
    private static Color DBGSHELL_WARNING_COLOR_DEFAULT = Color.PINK ;
    public static String DBGSHELL_ERROR_COLOR = "dbg.dbgshell.error.color";
    private static Color DBGSHELL_ERROR_COLOR_DEFAULT = Color.RED ;

    private Preferences pref;
    private String defaultPython = "";
    private static PythonOptions instance;

    
    public static PythonOptions getInstance(){
        if(instance == null)
            instance = new PythonOptions();
        return instance;
    }
    
    private PythonOptions(){
        pref = NbPreferences.forModule(PythonOptions.class);
        String temp = System.getProperty("netbeans.dirs");
        StringTokenizer token = new StringTokenizer(temp, File.pathSeparator);
        String pattern = "nbPython";
        boolean found = false;
        while (!found && token.hasMoreTokens() ){
            String tempToken = token.nextToken();
            if(tempToken.contains(pattern) || tempToken.contains(pattern.toLowerCase()) ){
                defaultPython = tempToken + File.separator + "jython-2.7.0" // XXX: Hardcoded? 
                        + File.separator + "bin" + File.separator + "jython";
                if (System.getProperty("os.name").toLowerCase().contains("windows")){
                    defaultPython += ".bat";
                    // FileZilla 152322 : check that the parent directory exist
                    // before creating the jythonbatfile to avoid IOException in netbeans log
                    File check = new File(defaultPython) ;
                    File parentDir = check.getParentFile() ;
                    if ( parentDir.isDirectory() )
                      createJythonBatFile(defaultPython);
                }
                    
                found = true;
            }
        }
    }

    public String getPythonCommand(){
        return pref.get(PYTHON_COMMAND, defaultPython);
    }
    
    public void setPythonCommand(String command){
        pref.put(PYTHON_COMMAND, command);
    }
    public String getPythonDefault(){
        return pref.get(PYTHON_DEFAULT, defaultPython);
    }
    
    public void setPythonDefault(String command){
        pref.put(PYTHON_DEFAULT, command);
    }
    public boolean getNative(){
        return pref.getBoolean(PYTHON_NATIVE, false);
    }
    
    public void setNative(boolean value){
        pref.putBoolean(PYTHON_NATIVE, value);
    }

    public String getPythonConsole(){
        return pref.get(PYTHON_CONSOLE, defaultPython);
    }

    public void setPythonConsole(String console){
        pref.put(PYTHON_CONSOLE, console);
    }
    public boolean getPromptForArgs(){
        return pref.getBoolean(PROMPT_FOR_ARGS, false);
    }
    public void setPromptForArgs(boolean value){
        pref.putBoolean(PROMPT_FOR_ARGS, value);
    }

    // debugger options
    public void setPythonDebuggingPort( int dbgPortStart ) {
        pref.putInt(DBG_LISTENING_PORT, dbgPortStart);
    }

    public int getPythonDebuggingPort(){
        return pref.getInt(DBG_LISTENING_PORT, 49152) ;
    }

    public void setStopAtFirstLine( boolean stopAtFirstLine ){
        pref.putBoolean(STOP_AT_FIRST_LINE, stopAtFirstLine);
    }

    public boolean getStopAtFirstLine(){
        return pref.getBoolean(STOP_AT_FIRST_LINE,true) ;
    }


    // debugger shell coloring and fonts options

    private String colorToString( Color color )
    {
    int R = color.getRed();
    int G = color.getGreen();
    int B = color.getBlue();
      return String.format("#%02X%02X%02X", R,G,B) ;
    }


    private Color colorFromString( String strColor )
    {
    int R = Integer.parseInt(strColor.substring(1,3),16) ;
    int G = Integer.parseInt(strColor.substring(3,5),16) ;
    int B = Integer.parseInt(strColor.substring(5,7),16) ;
      return new Color(R,G,B) ;
    }

    private Font stringToFont(String strFont) {
        int offsetSize = strFont.lastIndexOf('-');
        if (offsetSize == -1) {
            return null;
        }
        int offsetStyle = strFont.substring(0, offsetSize).lastIndexOf('-');
        if (offsetStyle == -1) {
            return null;
        }
        String name = strFont.substring(0, offsetStyle);
        int style = Integer.parseInt(strFont.substring(offsetStyle + 1, offsetSize));
        int size = Integer.parseInt(strFont.substring(offsetSize + 1));
        return new Font(name, style, size);
    }

    private String fontToString( Font font ) {
      return String.format("%s-%d-%d" , font.getFontName() , font.getStyle() , font.getSize() ) ;
    }



    public void setDbgShellFont( Font font) {
        pref.put(DBGSHELL_FONT, fontToString(font) )  ;
    }

    public Font getDbgShellFont() {
        return stringToFont( pref.get(DBGSHELL_FONT, fontToString(DBGSHELL_DEFAULT_FONT) ) ) ;
    }

    public Color getDbgShellBackground() {
        return colorFromString( pref.get( DBGSHELL_BACKGROUND_COLOR , colorToString(DBGSHELL_BACKGROUND_COLOR_DEFAULT) ) ) ;
    }

    public void setDbgShellBackground( Color color) {
        pref.put(DBGSHELL_BACKGROUND_COLOR, colorToString( color ) )  ;
    }

    public Color getDbgShellHeaderColor() {

        return colorFromString( pref.get(DBGSHELL_HEADER_COLOR , colorToString(DBGSHELL_HEADER_COLOR_DEFAULT)  ) ) ;
    }

    public void setDbgShellHeaderColor( Color color) {
        pref.put(DBGSHELL_HEADER_COLOR, colorToString( color ) )  ;
    }

    public Color getDbgShellInfoColor() {
        return colorFromString( pref.get(DBGSHELL_INFO_COLOR , colorToString(DBGSHELL_INFO_COLOR_DEFAULT) ) ) ;
    }

    public void setDbgShellInfoColor( Color color) {
        pref.put(DBGSHELL_INFO_COLOR, colorToString( color ) )  ;
    }

    public Color getDbgShellWarningColor() {
        return colorFromString( pref.get(DBGSHELL_WARNING_COLOR , colorToString( DBGSHELL_WARNING_COLOR_DEFAULT ) ) ) ;
    }

    public void setDbgShellWarningColor( Color color) {
        pref.put(DBGSHELL_WARNING_COLOR, colorToString( color ) )  ;
    }

    public Color getDbgShellErrorColor() {
        return colorFromString( pref.get(DBGSHELL_ERROR_COLOR , colorToString(DBGSHELL_ERROR_COLOR_DEFAULT) ) ) ;
    }

    public void setDbgShellErrorColor( Color color) {
        pref.put(DBGSHELL_ERROR_COLOR, colorToString( color ) )  ;
    }



    private void createJythonBatFile(String batFile) {
        File bat = new File(batFile);
        if(!bat.exists()){
            try {
                String battext = "@echo off\nrem This file was generated by nbpython\n\n" + 
                        "set ARGS=\nset JYTHON_HOME=XXXXXX\n"+
                        "set CLASSPATH=%JYTHON_HOME%\\jython.jar;%CLASSPATH%"+
                        ":loop\nif [%1] == [] goto end\n" + 
                        "    set ARGS=%ARGS% %1\n    shift\n    goto loop\n:end\n"+
                        "\"java\" -Dpython.home=\"%JYTHON_HOME%\" -classpath \"%CLASSPATH%\" org.python.util.jython %ARGS%\n";
                String jythonHome = batFile.substring(0, batFile.lastIndexOf("bin") - 1);                
                bat.createNewFile();
                BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bat)));
                br.write(battext.replace("XXXXXX", jythonHome));
                br.flush();
                br.close();
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch(IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
    }
}
