#!/usr/bin/env bash
#
# pipeline wrapper script calling gradle-generated script
#
# load external database ids for PhenoGen
#
. /etc/profile
APPNAME=PhenoGenPipeline
APPDIR=/home/rgddata/pipelines/$APPNAME
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`

cd $APPDIR
DB_OPTS="-Dspring.config=$APPDIR/../properties/default_db.xml"
LOG4J_OPTS="-Dlog4j.configuration=file://$APPDIR/properties/log4j.properties"
declare -x "PHENO_GEN_PIPELINE_OPTS=$DB_OPTS $LOG4J_OPTS"
bin/$APPNAME "$@" 2>&1 > run.log

mailx -s "[$SERVER] PhenoGen Pipeline Run" rgd.developers@mcw.edu < run.log
