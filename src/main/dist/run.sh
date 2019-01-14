#!/usr/bin/env bash
#
# load external database ids for PhenoGen
#
. /etc/profile
APPNAME=PhenoGenPipeline
APPDIR=/home/rgddata/pipelines/$APPNAME
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`

cd $APPDIR
java -Dspring.config=$APPDIR/../properties/default_db.xml \
    -Dlog4j.configuration=file://$APPDIR/properties/log4j.properties \
    -jar lib/$APPNAME.jar "$@" > run.log 2>&1

mailx -s "[$SERVER] PhenoGen Pipeline Run" rgd.developers@mcw.edu < run.log
