#!/usr/bin/env bash
#
# load external database ids for PhenoGen
#
. /etc/profile
APPNAME="phenogen-pipeline"
APPDIR=/home/rgddata/pipelines/$APPNAME
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`
EMAIL_LIST=mtutaj@mcw.edu
if [ "$SERVER" = "REED" ]; then
  EMAIL_LIST=rgd.devops@mcw.edu
fi

cd $APPDIR
java -Dspring.config=$APPDIR/../properties/default_db2.xml \
    -Dlog4j.configuration=file://$APPDIR/properties/log4j.properties \
    -jar lib/$APPNAME.jar "$@" > run.log 2>&1

mailx -s "[$SERVER] PhenoGen Pipeline Run" $EMAIL_LIST < $APPDIR/logs/summary.log
