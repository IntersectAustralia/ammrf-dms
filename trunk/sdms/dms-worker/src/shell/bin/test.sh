#!/bin/bash

function copy_template {
  FROM_NAME=$1
  TO_DIR=$2
  TO_NAME=$TO_DIR/`basename "$FROM_NAME"`
  echo "Copying $FROM_NAME to $TO_NAME"
  while read line ; do
      while [[ "$line" =~ (\${[a-zA-Z_][a-zA-Z_0-9]*}) ]] ; do
          LHS=${BASH_REMATCH[1]}
          RHS=$(eval echo "\"$LHS\"")
          ## echo $LHS "->" $RHS 
          line=${line//$LHS/$RHS}
      done
      echo $line
  done < "$FROM_NAME" > "$TO_NAME"
}

function copy_other {
  FROM_NAME=$1
  TO_DIR=$2
  TO_NAME=$TO_DIR/`basename "$FROM_NAME"`
  cp "$FROM_NAME" "$TO_NAME"
}

function template_vars {
  VAL=$1
  export DMS_HOME=$VAL/dms.home
  export WORKER_ROOT=$VAL/files-root
  export USER_HOME="$HOME"
}

function copy_resources {
  mkdir $DIR/tmp/dms.home
  mkdir $DIR/tmp/dms.home/keys
  mkdir -p $DIR/tmp/files-root/test/t2
  mkdir $DIR/tmp/spring-extra
  template_vars $DIR/tmp
  copy_template $DIR/dms_home/worker.properties $DIR/tmp/dms.home
  copy_template $DIR/dms_home/tunnel.properties $DIR/tmp/dms.home
  copy_other $DIR/dms_home/keys/privTunnelApplet.der $DIR/tmp/dms.home/keys
  copy_other $DIR/dms_home/keys/privTunnelWorker.der $DIR/tmp/dms.home/keys
  copy_other $DIR/dms_home/keys/pubTunnelWorker.der $DIR/tmp/dms.home/keys
  copy_other $DIR/dms_home/spring-extra/applicationContext-extra.xml $DIR/tmp/spring-extra
  copy_other $DIR/dms_home/log4j.properties $DIR/tmp/dms.home
  copy_other $DIR/dms_home/id_rsa $DIR/tmp/dms.home
  copy_other $DIR/dms_home/lib-extra/servlet-api-2.5.jar $DIR/tmp/dms-worker-*/lib
}

function usage {
  echo "$0 [-i] -s <url> -d <url>
Sample test program to check copy files
-i : reinstall packaged software into bin directory.
-s : source URL (a directory or file)
-c : command
"
}
#
# Command line args
#
i

while getopts "is:d:c:p:" options; do
  case $options in
    i) INSTALL=install;;
    s) SOURCE_URL=$OPTARG;;
    d) DEST_URL=$OPTARG;;
    c) COMMAND=$OPTARG;;
    p) PARAM=$OPTARG;;
    *) usage
       exit 1;; 
  esac
done


DIR=`dirname $0`/..
CDIR=`pwd`
DIR=`cd "$DIR"; pwd`
cd $CDIR
echo WORKING DIR = $DIR
if [ -n "$INSTALL" ]; then
    rm -rf $DIR/tmp
fi
if [ ! -d $DIR/tmp ]; then
    if [ ! -d $DIR/../../target ]; then
        echo Please run mvn package first for dms-worker project
        exit 1
    fi
    mkdir $DIR/tmp
    unzip $DIR/../../target/dms-worker-*-with-dependencies.zip -d $DIR/tmp
    copy_resources
fi
CP="$DIR/tmp/spring-extra"
for jarf in $DIR/tmp/dms-worker-*/lib/*; do CP="$jarf:$CP"; done
java -cp $CP \
     -Ddms.config.home="$DIR/tmp/dms.home" \
     -Dlog4j.configuration="file:$DIR/tmp/dms.home/log4j.properties" \
     au.org.intersect.dms.wn.main.ConnectionIntegrationTester $COMMAND $SOURCE_URL $PARAM
