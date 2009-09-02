#!/usr/local/bin/perl -w
######################################################################
# Name: voxpopo1.cgi
######################################################################
# Description: This program takes a list of pids ( in pids.txt ) and
# reindexes them with an 8 second pause in between.
######################################################################
# References: 
######################################################################
# Parameters:
######################################################################
# Comments: Make sure that you source the proper fedora environment
# and set FEDORA_PASSWD.  Also, place a file called pids text in the
# folder with LuceneRX.pl before running it.
######################################################################
# Command: perl LuceneRX.pl
######################################################################
# History:
#
# Date: 06/03/2009     
# Programmer: Paul Hammer
# Comment: Created
#
######################################################################

# Use clauses
use Getopt::Std;
use File::Basename;
#use DBI;

#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
# Local variables
$debug = 0;

$TRUE = 1;
$FALSE = 0;

$iInCounter = 0;
$iOutCounter = 0;
$iCurrentCounter = 0;

@aRecord = ();
$iIndex = 0;

$iFirstElement = $iIndex++;

$iRFieldCount = $iIndex;

$sLine = "";
@aLineParts = ();
$iLFieldCount = 0;
$sThisFile = "pids.txt";

#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
#                         MAIN PROGRAM
#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
# 
if ( open( INFILE, $sThisFile ) || die "Sorry, can't open $sThisFile" ) {


    #$iRecordCounter = 1;

    while ( $sLine = <INFILE> ) {
	
	chomp $sLine;

	print "$sLine\n";

	# First lets break up the line
	#@aLineParts = split / /, $sLine;
	#$iLFieldCount = @aLineParts;

	if( ( $iOutCounter >= 0 ) && ( $sLine =~ /^umd:\d+$/ ) ) {

	    $sCmd = "fedora-index -a queue -p " . $sLine;

	    print "$sCmd\n";

	    $sResult = `$sCmd`;

	    print $sResult . "\n";

	    $iOutCounter++;

	    sleep(8);
	}

    }

    print "\nProcessed " . $iOutCounter . " pids.\n";

}

close( INFILE );

#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
#                         Subroutines
#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

#---------------------------------------------------------------------
# Subroutine Name: 
#---------------------------------------------------------------------
# Description: 
#---------------------------------------------------------------------
# Comments
#---------------------------------------------------------------------

#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
#                         End Program
#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


























