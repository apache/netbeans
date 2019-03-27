#!/bin/sh
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
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
