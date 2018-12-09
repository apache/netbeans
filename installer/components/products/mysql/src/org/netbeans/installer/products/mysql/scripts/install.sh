#!/bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU General Public
# License Version 2 only ("GPL") or the Common Development and Distribution
# License("CDDL") (collectively, the "License"). You may not use this file except in
# compliance with the License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
# License for the specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header Notice in
# each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
# designates this particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that accompanied this code.
# If applicable, add the following below the License Header, with the fields enclosed
# by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# Contributor(s):
# 
# The Original Software is NetBeans. The Initial Developer of the Original Software
# is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
# Rights Reserved.
# 
# If you wish your version of this file to be governed by only the CDDL or only the
# GPL Version 2, indicate your decision by adding "[Contributor] elects to include
# this software in this distribution under the [CDDL or GPL Version 2] license." If
# you do not indicate a single choice of license, a recipient has the option to
# distribute your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above. However, if you
# add GPL Version 2 code and therefore, elected the GPL Version 2 license, then the
# option applies only if the new code is made subject to such option by the copyright
# holder.
# 

set -x
ISROOT="$1"
SYSTEM_TYPE="$2"
PASSWORD="$3"

INSTALLDIR=`pwd`

cp ./support-files/my-"$SYSTEM_TYPE".cnf ./my.cnf


do_query() {
    tmpFile=./query.tmp
    echo "$1" > $tmpFile
    if [ 1 -eq $ISROOT ] ; then
        if [ -n "$PASSWORD" ] ; then
            ./bin/mysql --defaults-file=./my.cnf --password="$PASSWORD" <$tmpFile
	else
            ./bin/mysql --defaults-file=./my.cnf <$tmpFile
        fi
    else
        if [ -n "$PASSWORD" ] ; then
            ./bin/mysql --defaults-file=./my.cnf --user=root --password="$PASSWORD" <$tmpFile
        else
            ./bin/mysql --defaults-file=./my.cnf --user=root <$tmpFile
	fi
    fi
    code=$?
    rm $tmpFile
    return $code
}
escape() {
	echo "$1" | sed -e "s/\//\\\\\//g"
}


remove_remote_root() {
    do_query "DELETE FROM mysql.user WHERE User='root' AND Host!='localhost';"
    if [ $? -eq 0 ] ; then
	echo " ... Success!"
    else
	echo " ... Failed!"
    fi
}

remove_anonymous() {
    if [ -n "$REMOVE_ANONYMOUS" ] ; then 
        do_query "DELETE FROM mysql.user WHERE User='';"
        echo "Result : $?"
        do_query "FLUSH PRIVILEGES;"
        echo "Result : $?"
    fi
}

#Modify my.cnf with settings
#PORT_NUMBER, SKIP_NETWORKING, REMOVE_ANONYMOUS, MODIFY_SECURITY should be passed via env variables
if [ -n "$PORT_NUMBER" ] ; then
    sed  -e "s/3306/$PORT_NUMBER/g" ./my.cnf > ./my.cnf.tmp && mv ./my.cnf.tmp ./my.cnf
fi

if [ -n "$SKIP_NETWORKING" ] ; then
    sed -e "s/#skip-networking/skip-networking/g" ./my.cnf > ./my.cnf.tmp && mv ./my.cnf.tmp ./my.cnf
fi

#Enable using InnoDB
sed -e "s/#innodb_/innodb_/g" ./my.cnf > ./my.cnf.tmp && mv ./my.cnf.tmp ./my.cnf

#Update mysql directory
DEFAULT_MYSQL_DIR=/usr/local/mysql
sed -e "s/`escape $DEFAULT_MYSQL_DIR`/`escape $INSTALLDIR`/g" ./my.cnf > ./my.cnf.tmp && mv ./my.cnf.tmp ./my.cnf

#Set basedir and datadir in support-files/mysql.server and my.cnf files
awk '{ print $i ; if($i=="[mysqld]") { print a ; print b }}' a="basedir = $INSTALLDIR" b="datadir = $INSTALLDIR/data" < ./my.cnf > my.cnf.tmp && mv ./my.cnf.tmp ./my.cnf
instdir=`escape "$INSTALLDIR"`

cp ./support-files/mysql.server ./support-files/mysql.server.tmp
sed -e "s/^basedir=/basedir=$instdir/g" ./support-files/mysql.server > ./support-files/mysql.server.tmp
mv ./support-files/mysql.server.tmp ./support-files/mysql.server

#https://bugs.launchpad.net/bugs/251656
if [ 0 -eq $ISROOT ] && 
   [ -n "`grep lsb-base-logging.sh /lib/lsb/init-functions 2>/dev/null`" ] &&
   [ -n "`grep usplash_write /etc/lsb-base-logging.sh 2>/dev/null`" ] && 
   [ -n "`usplash_write SUCCESS ok > /dev/null | grep \"open: Permission denied\"`"] ; then
    echo "... disabling lsb init-functions since it uses usplash_write which is not supported under user"
    cp ./support-files/mysql.server ./support-files/mysql.server.tmp
    sed -e "s/\/lib\/lsb\/init-functions/\/lib\/lsb\/init-functions-disabled/g" ./support-files/mysql.server > ./support-files/mysql.server.tmp
    mv ./support-files/mysql.server.tmp ./support-files/mysql.server
fi

if [ 1 -eq $ISROOT ] ; then

    groupadd mysql

    if [ 0 -eq $? ] ; then
       echo "... OK"
    elif [ 9 -eq $? ] ; then
       echo "... group mysql already exist"
    else
       echo "errorcode : $?"
    fi

    useradd -g mysql mysql 

    if [ 0 -eq $? ] ; then
       echo "... OK"
    elif [ 9 -eq $? ] ; then
       echo "... group mysql already exist"
    else
       echo "errorcode : $?"
    fi

    chown -R mysql .

    if [ 0 -eq $? ] ; then
       echo "... OK"
    else
       echo "errorcode : $?"
    fi


    chgrp -R mysql .
    if [ 0 -eq $? ] ; then
       echo "... OK"
    else
       echo "errorcode : $?"
    fi

    chmod -R g+w ./data
    if [ 0 -eq $? ] ; then
       echo "... OK"
    else
       echo "errorcode : $?"
    fi
fi

if [ 1 -eq $ISROOT ] ; then
    ./scripts/mysql_install_db --no-defaults --defaults-file="$INSTALLDIR"/my.cnf --user=mysql 
    echo "Result : $?"
else 
    ./scripts/mysql_install_db --no-defaults --defaults-file="$INSTALLDIR"/my.cnf
    echo "Result : $?"
fi

if [ 1 -eq $ISROOT ] ; then

    chown -R root  .
    if [ 0 -eq $? ] ; then
       echo "... OK"
    else
       echo "errorcode : $?"
    fi

    chown -R mysql data

    if [ 0 -eq $? ] ; then
       echo "... OK"
    else
       echo "errorcode : $?"
    fi
fi

if [ 1 -eq $ISROOT ] ; then
    ./bin/mysqld_safe --defaults-file="$INSTALLDIR"/my.cnf --user=mysql &
else 
    ./bin/mysqld_safe --defaults-file="$INSTALLDIR"/my.cnf &
fi

sleep 3
sleep 2

if [ -n "$PASSWORD" ] ; then
    ./bin/mysqladmin --defaults-file=./my.cnf -u root password "$PASSWORD"
    echo "Result : $?"
    ./bin/mysqladmin --defaults-file=./my.cnf -u root -h `hostname` password "$PASSWORD"
    echo "Result : $?"
fi

if [ -n "$MODIFY_SECURITY" ] ; then
    remove_anonymous
    remove_remote_root
fi
