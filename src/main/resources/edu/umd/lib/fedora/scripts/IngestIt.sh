#!/bin/bash

cd /apps/fedora/server/utilities/batch/ingest/
filename=`ls *.xls | sort | head -1`
extension="${filename##*.}"
baseName="${filename%.*}"

dataFile="/apps/fedora/server/utilities/batch/ingest/$filename"

if [ -f "$dataFile" ]
  then
    logFile="/apps/fedora/server/utilities/batch/logs/$baseName-ingest.log"
    fileStats="/apps/fedora/server/utilities/batch/ingest/$baseName-fileStats.txt"
    newPids="/apps/fedora/server/utilities/batch/ingest/$baseName-newPids.txt"
    errorFile="/apps/fedora/server/utilities/batch/error/$baseName-error-ingest.txt"
    pidFile="/apps/fedora/server/utilities/batch/done/$baseName-pids.txt"
    linkURLs="/apps/fedora/server/utilities/batch/done/$baseName-linkURLs.txt"
    reindexPids="/apps/fedora/server/utilities/batch/done/$baseName-reindexPids.txt"
    recordCounts="/apps/fedora/server/utilities/batch/done/$baseName-counts.txt"
    email='phammer@umd.edu'

    # position all of the files for this batch
    cd /apps/fedora/server/utilities/batch
    cp $dataFile ref/Prangebatch2.xls
    cp $fileStats ref/fileStats.txt
    echo pid > ref/newPids.txt
    cat $newPids >> ref/newPids.txt

    echo Records > $recordCounts
    wc -l $newPids >> $recordCounts

    rm -rf foxml
    mkdir foxml
    export ANT_HOME="/apps/fedora/apache-ant-1.8.4"
    nice /apps/fedora/apache-ant-1.8.4/bin/ant load > $logFile 

    rm -rf delObjects
    mkdir delObjects
    cp out/pids.txt pids.txt

    perl FedoraBatchIngest.pl >> $logFile

    errorCount=`grep -i Error $logFile | wc -l`

    if [ "$errorCount" -gt 0 ]
      then
        grep -i Error $logFile > $errorFile
        mv $dataFile error/
        mv $fileStats error/
        mv $newPids error/

      else
        cd /apps/fedora/server/utilities/batch
        mv $fileStats done/
        mv $newPids done/
        mv $dataFile done/
        mv out/pids.txt $pidFile
        mv out/linkURLs.txt $linkURLs
        mv out/reindexPids.txt $reindexPids

        echo Objects >> $recordCounts
        wc -l $reindexPids >> $recordCounts

        cp $reindexPids UMDMpids.txt
        perl LuceneRX.pl

        cat prangehead.txt $linkURLs $recordCounts | mail -s $baseName $email -- -r phammer@umd.edu

    fi

fi
