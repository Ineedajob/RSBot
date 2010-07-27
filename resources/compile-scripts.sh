#!/bin/sh

cc=javac
cflags=
scripts=Scripts
scriptspre=$scripts/Precompiled
jarpathfile=Settings/path.txt

if [ ! -e "$jarpathfile" ]; then
	echo "Path file does not exist. Please run RSBot and try again."
	exit
fi

if [ ! -e $scripts/*.java ]; then
	echo "No .java script source files found."
	exit
fi


echo "Compiling scripts"
if [ -e $scripts/*.class ]; then rm -R $scripts/*.class; fi
"$cc" $cflags -cp "/$(cat $jarpathfile)" $scripts/*.java
