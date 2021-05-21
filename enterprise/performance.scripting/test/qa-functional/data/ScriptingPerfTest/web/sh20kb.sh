#!/bin/bash
# itest bash script
# name : test-installer-creator.sh
# desc : Standalone installer test core script
# parameters which has to be exported from server:
#       ITEST_BUILD_NAME
#       ITEST_BUILD_VERSION
#       ITEST_CREATOR_PATH
#       ITEST_CREATOR_SUBDIRS
#       ITEST_CREATOR_FILES


# ****************************
# PREPARING OF THE ENVIRONMENT
# ****************************
echo "System environment preparing ..."

cd ~

declare ITEST_DIR=$(pwd)/itest
declare ITEST_ARCHIVE=installer/test/qa-functional/itest.zip
declare ITEST_SLEEP=10000
declare SYSTEM_TEMP=/tmp

cd $ITEST_DIR

if [ "$(test -f config && echo "OK")" == "" ]; then
    echo "config file is missing or it is corrupted"
    exit 1
fi

# The path where the JDK which will be used is installed
declare TEMP_BUFFER=$(cat config | grep "itest.jdk")
declare ITEST_JDK=${TEMP_BUFFER:10}
# The version of the JDK on tha ITEST_JDK path -> it's important only for standalone and asbundle package
declare TEMP_BUFFER=$(cat config | grep "itest.reqjdkver")
declare ITEST_REQJDKVER=${TEMP_BUFFER:16}
# The path where all builds of nb/jdkbundles/asbundles are situated (on unix stations it is "/net/phoenix/space/builds/netbeans"
declare TEMP_BUFFER=$(cat config | grep "itest.nbsourcepath")
declare ITEST_NBSOURCEPATH=${TEMP_BUFFER:19}
# Path to the CVS command which is used for checkouting of the actual versions of the test's scripts
declare TEMP_BUFFER=$(cat config | grep "itest.cvs")
declare ITEST_CVS=${TEMP_BUFFER:10}
# Path to the SCP command which is used for copying of the test report to the beetle
declare TEMP_BUFFER=$(cat config | grep "itest.scp")
declare ITEST_SCP=${TEMP_BUFFER:10}

# checking the existence of the global variables
if [ "${ITEST_DIR}" == "" ]; then
    echo "ITEST_DIR parameter missing"
    exit 1
elif [ "${ITEST_JDK}" == "" ]; then
    echo "ITEST_JDK parameter missing"
    exit 1
elif [ "${ITEST_REQJDKVER}" == "" ]; then
    echo "ITEST_REQJDKVER parameter missing"
    exit 1
elif [ "${ITEST_NBSOURCEPATH}" == "" ]; then
    echo "ITEST_NBSOURCEPATH parameter missing"
    exit 1
elif [ "${ITEST_CVS}" == "" ]; then
    echo "ITEST_CVS parameter missing"
    exit 1
elif [ "${ITEST_SCP}" == "" ]; then
    echo "ITEST_SCP parameter missing"
    exit 1
fi

if [ "$(test -f config && echo "OK")" != "" ]; then
    mv ./config $SYSTEM_TEMP/config
    declare MOVE=yes
fi

rm -rf *
rm -rf ~/InstallShield

if [ "$MOVE" == "yes" ]; then
    mv $SYSTEM_TEMP/config ./config
fi
            
$ITEST_CVS -d :pserver:qatester:user@server.domain:/cvs checkout $ITEST_ARCHIVE &>/dev/null
            
unzip -d ./ $ITEST_ARCHIVE &>/dev/null
rm -rf installer
            
if [ "$(test -f config && echo "OK")" == "" ]; then
    mv ./config.new ./config
fi

if [ "$(test -f config && echo "OK")" == "" ]; then
    echo "config file is missing or it is corrupted"
    exit 1
fi

# ***********************
# UPDATING THE TEST FILES
# ***********************
echo "Updating the test files ..."

# Additional settings
export ITEST_TEMP=$ITEST_DIR/temp
export ITEST_LOGS=$ITEST_DIR/logs
export ITEST_BIN=$ITEST_DIR/bin
export ITEST_JTOOL=$ITEST_DIR/jtool

# Inicialization of the local variables
declare CVS_SCRIPTS=installer/test/qa-functional/scripts/actions
declare LOCAL_SCRIPTS=$ITEST_DIR/scripts

# Checkouting for the actual version

cd $ITEST_DIR
    
$ITEST_CVS -d :pserver:qatester:user@server.domain:/cvs checkout $CVS_SCRIPTS &>/dev/null

rm -rf $LOCAL_SCRIPTS
                    
mv $ITEST_DIR/$CVS_SCRIPTS $LOCAL_SCRIPTS

rm -rf $ITEST_DIR/installer
                            
chmod +x $LOCAL_SCRIPTS/*

# *********************************
# POST UPDATING ENVIRONMENT SETTING
# *********************************
echo "Post updating environment setting ..."

# Directories existence check
if [ "$(test -d $ITEST_TEMP && echo "OK")" == "" ]; then
    mkdir $ITEST_TEMP
fi
    
if [ "$(test -d $ITEST_LOGS && echo "OK")" == "" ]; then
    mkdir $ITEST_LOGS
fi

# Post-checkout configuration of the environment
cd ${LOCAL_SCRIPTS}

# testing of the parameters exported from server
if [ "${ITEST_BUILD_NAME}" == "" ]; then
    echo "ITEST_BUILD_NAME parameter missing"
    exit 1
elif [ "${ITEST_BUILD_VERSION}" == "" ]; then
    echo "ITEST_BUILD_VERSION parameter missing"
    exit 1
elif [ "${ITEST_CREATOR_PATH}" == "" ]; then                                                                                                                                                                  
    echo "ITEST_CREATOR_PATH parameter missing"                                                                                                                                                             
    exit 1                                                                                                                                                                                                
elif [ "${ITEST_CREATOR_SUBDIRS}" == "" ]; then                                                                                                                                                                
    echo "ITEST_CREATOR_SUBDIRS parameter missing"                                                                                                                                                             
    exit 1                                                                                                                                                                                                
elif [ "${ITEST_CREATOR_FILES}" == "" ]; then                                                                                                                                                             
    echo "ITEST_CREATOR_FILES parameter missing"                                                                                                                                                          
    exit 1 
fi


# finding out of the platform
case $(uname) in
    CYGWIN_NT-5.1) declare ITEST_PLATFORM_IDENTIFIER=win
                   ;;
    CYGWIN_NT-5.0) declare ITEST_PLATFORM_IDENTIFIER=win
                   ;;
    Linux) declare ITEST_PLATFORM_IDENTIFIER=linux
           ;;
    SunOS) if [ "$(uname -p | grep "i386")" != "" ]; then
                declare ITEST_PLATFORM_IDENTIFIER=x86
           else
                declare ITEST_PLATFORM_IDENTIFIER=sparc
           fi
           ;;
    *) echo "UNKNOWN PLATFORM"
       exit 1
       ;;
esac

# ********************
# STARTING OF THE TEST
# ********************
echo "Test started ..."
echo "***"
echo ""

declare ITEST_PLATFORM_DETAIL=$(uname -a)
declare ITEST_TESTED_FILE=${ITEST_NBSOURCEPATH}$ITEST_CREATOR_PATH$(ls ${ITEST_NBSOURCEPATH}$ITEST_CREATOR_PATH | grep "${ITEST_PLATFORM_IDENTIFIER}" | grep netbeans)
declare ITEST_TESTED_CREATOR_FILE=${ITEST_NBSOURCEPATH}$ITEST_CREATOR_PATH$(ls ${ITEST_NBSOURCEPATH}$ITEST_CREATOR_PATH | grep "${ITEST_PLATFORM_IDENTIFIER}" | grep CreatorPack)
declare ITEST_JAVA=${ITEST_JDK}/jre/bin/java
declare ITEST_JAR=${ITEST_JDK}/bin/jar
declare ITEST_TEST_NAME="NetBeans IDE Standalone + Creator Pack Installer Test"
declare ITEST_CORE_LIB=${ITEST_JTOOL}/itest-core.jar
declare ITEST_JEMMY_LIB=${ITEST_JTOOL}/lib/jemmy.jar
declare ITEST_CANCEL_LIB=${ITEST_JTOOL}/itest-action-cancel.jar
declare ITEST_INSTALLERCREATOR_LIB=${ITEST_JTOOL}/itest-action-installercreator.jar
declare ITEST_IDE_JARFILE=${ITEST_TEMP}/standalone.jar
declare ITEST_CREATOR_JARFILE=${ITEST_TEMP}/creator.jar

# report header
echo "Testname : "$ITEST_TEST_NAME
echo ""
echo "Platform : "$ITEST_PLATFORM_DETAIL
echo "Tested IDE file : "$ITEST_TESTED_FILE
echo "Tested Creator file : "$ITEST_TESTED_CREATOR_FILE
echo "Date : "$(date)
echo ""
echo "Results :"

# runing the action script "action-bintojar.sh"
# TESTSTEP 1
# **********
echo -n "Teststep 1 - Converting the IDE binary file to the JAR....."

sleep 1
    
export ACTION_BINTOJAR_BINSOURCE=${ITEST_TESTED_FILE}
export ACTION_BINTOJAR_JARTARGET=${ITEST_IDE_JARFILE}
export ACTION_BINTOJAR_JAR=${ITEST_JAR}
export ACTION_BINTOJAR_TEMPDIR=${ITEST_TEMP}
export ACTION_BINTOJAR_PLATFORM=${ITEST_PLATFORM_IDENTIFIER}

./action-fail.sh
./action-bintojar.sh &>/dev/null

if [ "$?" = "0" ]; then
    echo PASS
else
    echo FAIL
fi

# runing the action script "action-bintojar.sh"                                                                                                                                                             
# TESTSTEP 2                                                                                                                                                                                               
# **********                                                                                                                                                                                                
echo -n "Teststep 2 - Converting the Creator binary file to the JAR....."                                                                                                                                       
                                                                                                                                                                                                            
sleep 1                                                                                                                                                                                                     
                                            
export ACTION_BINTOJAR_BINSOURCE=${ITEST_TESTED_CREATOR_FILE}                                                                                                                                                                
export ACTION_BINTOJAR_JARTARGET=${ITEST_CREATOR_JARFILE}
                                                                                                                                                                                                            
./action-fail.sh                                                                                                                                                                                            
./action-bintojar.sh &>/dev/null                                                                                                                                                                            
                                                                                                                                                                                                            
if [ "$?" = "0" ]; then                                                                                                                                                                                     
    echo PASS                                                                                                                                                                                               
else                                                                                                                                                                                                        
    echo FAIL                                                                                                                                                                                               
fi

# install the IDE
# TESTSTEP 3
# **********
echo "Teststep 3 - IDE Installer test"
echo ""
echo "==>"
echo ""

sleep 1

declare INSTALLER_TEST_INSTALLDIR=${ITEST_TEMP}/nb-standalone-$RANDOM
                            
if [ "${ITEST_PLATFORM_IDENTIFIER}" != "win" ]; then
    declare INSTALLER_TEST_NWRITEDIR=${ITEST_TEMP}/nwritedir

    rm -rf ${INSTALLER_TEST_NWRITEDIR}
    mkdir ${INSTALLER_TEST_NWRITEDIR}
    chmod 000 ${INSTALLER_TEST_NWRITEDIR}
else
    declare INSTALLER_TEST_NWRITEDIR=${ITEST_NBSOURCEPATH}
fi

if [ "${ITEST_PLATFORM_IDENTIFIER}" == "win" ]; then
    declare INSTALL_CLASSPATH=$(cygpath -wp ${ITEST_IDE_JARFILE}:${ITEST_CORE_LIB}:${ITEST_JEMMY_LIB})
    declare INSTALL_DIR=$(cygpath -wp ${INSTALLER_TEST_INSTALLDIR})
    declare INSTALL_JDKHOME=$(cygpath -wp ${ITEST_JDK})
    declare INSTALL_NWRITEDIR=$(cygpath -wp ${INSTALLER_TEST_NWRITEDIR})
else
    declare INSTALL_CLASSPATH=${ITEST_IDE_JARFILE}:${ITEST_CORE_LIB}:${ITEST_JEMMY_LIB}
    declare INSTALL_DIR=${INSTALLER_TEST_INSTALLDIR}
    declare INSTALL_JDKHOME=${ITEST_JDK}
    declare INSTALL_NWRITEDIR=${INSTALLER_TEST_NWRITEDIR}
fi

$ITEST_JAVA -cp $INSTALL_CLASSPATH org.netbeans.itest.Run -action installersa -log console -sleep $ITEST_SLEEP -name "netbeans standalone installer test" -installersa.nbpath $INSTALL_DIR -installersa.jdkpath $INSTALL_JDKHOME -installersa.nwritedir $INSTALL_NWRITEDIR -installersa.buildname "$ITEST_BUILD_NAME" -installersa.buildversion "$ITEST_BUILD_VERSION"

if [ "${ITEST_PLATFORM_IDENTIFIER}" != "win" ]; then
    chmod 777 ${INSTALLER_TEST_NWRITEDIR}
    rm -rf ${INSTALLER_TEST_NWRITEDIR}
fi

echo ""
echo "==>"
echo ""

# packing installed netbeans into zip archive
# *******************************************
echo "Packing installed netbeans..."

declare INSTALLER_ARCHIVE=$ITEST_TEMP/nb.zip

zip -r $INSTALLER_ARCHIVE $INSTALLER_TEST_INSTALLDIR &>/dev/null

echo ""

# running the uninstaller test                                                                                                                                                                              
# TESTSTEP 4                                                                                                                                                                                                
# **********                                                                                                                                                                                                
echo "Teststep 4 - NetBeans IDE Uninstaller test"                                                                                                                                                                    
echo ""                                                                                                                                                                                                     
echo "==>"                                                                                                                                                                                                  
echo ""                                                                                                                                                                                                     
                                                                                                                                                                                                            
sleep 1                                                                                                                                                                                                     
                                                                                                                                                                                                            
declare UNINSTALLER_JAR=${INSTALLER_TEST_INSTALLDIR}/_uninst/uninstall.jar                                                                                                                                  
                                                                                                                                                                                                            
if [ "${ITEST_PLATFORM_IDENTIFIER}" == "win" ]; then                                                                                                                                                        
    declare UNINSTALL_CLASSPATH=$(cygpath -wp ${UNINSTALLER_JAR}:${ITEST_CORE_LIB}:${ITEST_JEMMY_LIB})                                                                                                      
else                                                                                                                                                                                                        
    declare UNINSTALL_CLASSPATH=${UNINSTALLER_JAR}:${ITEST_CORE_LIB}:${ITEST_JEMMY_LIB}                                                                                                                     
fi                                                                                                                                                                                                          
                                                                                                                                                                                                                    
$ITEST_JAVA -cp $UNINSTALL_CLASSPATH org.netbeans.itest.Run -action uninstaller -log console -sleep $ITEST_SLEEP -name "netbeans uninstaller test" -uninstaller.buildname "$ITEST_BUILD_NAME" -uninstaller.buildversion "$INSTALL_BUILD_VERSION"
                                                                                                                                                                                                                    
echo ""                                                                                                                                                                                                     
echo "==>"
echo ""

# unpacking netbeans
# ******************
echo "Unpacking netbeans..."

unzip $INSTALLER_ARCHIVE -d / &>/dev/null

echo ""

# install the creator pack                                                                                                                                                                                
# TESTSTEP 5                                                                                                                                                                                           
# **********                                                                                                                                                                                                
echo "Teststep 5 - Creator Installer test"                                                                                                                                                                          
echo ""                                                                                                                                                                                                     
echo "==>"                                                                                                                                                                                                  
echo ""                                                                                                                                                                                                     
                                                                                                                                                                                                            
sleep 1                                                                                                                                                                                                     
                                                
if [ "${ITEST_PLATFORM_IDENTIFIER}" == "win" ]; then                                                                                                  
    declare INSTALL_CREATOR_CLASSPATH=$(cygpath -wp ${ITEST_CREATOR_JARFILE}:${ITEST_CORE_LIB}:${ITEST_JEMMY_LIB})                                          
else                                                                                                                                            
    declare INSTALL_CREATOR_CLASSPATH=${ITEST_CREATOR_JARFILE}:${ITEST_CORE_LIB}:${ITEST_JEMMY_LIB}                                                         
fi
                                                                                                                                                            
$ITEST_JAVA -cp $INSTALL_CREATOR_CLASSPATH org.netbeans.itest.Run -action installercreator -log console -sleep $ITEST_SLEEP -name "netbeans standalone+creator pack installer test" -Installercreator.NetBeansPath "$INSTALL_DIR" -Installercreator.NetBeansVersion "$ITEST_BUILD_VERSION" -Installercreator.CreatorPackDir "rave2.0" -Installercreator.CreatorPackSubdirList "$ITEST_CREATOR_SUBDIRS" -Installercreator.CreatorPackFileList "$ITEST_CREATOR_FILES"
                                                                                                                                                                                                                                                        
echo ""                                                                                                                                                                                                     
echo "==>"                                                                                                                                                                                                  
echo ""  

# ***************************
# CLEANING UP THE ENVIRONMENT
# ***************************
echo ""
echo "***"
echo "Test finished ..."
echo "System environment cleaning ..."

cd $ITEST_DIR

if [ "$(test -f config && echo "OK")" != "" ]; then
    mv ./config $SYSTEM_TEMP/config
    declare MOVE=yes
fi

rm -rf *
rm -rf ~/InstallShield

if [ "$MOVE" == "yes" ]; then
    mv $SYSTEM_TEMP/config ./config
fi

# ****
# DONE
# ****
echo "Done"

pkill -u tester &>/dev/null

exit 0