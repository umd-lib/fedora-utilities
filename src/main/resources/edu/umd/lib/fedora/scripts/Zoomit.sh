#!/bin/bash

filename=$1
extension="${filename##*.}"
baseName="${filename%.*}"

echo fileName $filename
echo extension $extension
echo baseName $baseName

dataFile="/apps/fedora/server/utilities/batch/data/$filename"

echo dataFile $dataFile

if [ -f "$dataFile" ]
  then
    logFile="/apps/fedora/server/utilities/batch/logs/$baseName-zoom.log"
    fileStats="/apps/fedora/server/utilities/batch/ingest/$baseName-fileStats.txt"
    newPids="/apps/fedora/server/utilities/batch/ingest/$baseName-newPids.txt"
    errorFile="/apps/fedora/server/utilities/batch/error/$baseName-error.txt"

    cp $dataFile ref/PrangeBatch.xls

    export ANT_HOME="/apps/fedora/apache-ant-1.8.4"
    nice /apps/fedora/apache-ant-1.8.4/bin/ant -DfileName=$filename run > $logFile

    errorCount=`grep Error $logFile | wc -l`

    if [ "$errorCount" -gt 0 ]
      then
        grep Error $logFile > $errorFile
        mv $dataFile error/

      else
        mv fileStats.txt $fileStats
        mv newPids.txt $newPids
        mv $dataFile ingest/

    fi

fi
