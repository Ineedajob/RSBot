#!/bin/sh

cc=javac
cflags=
scripts=Scripts/Sources
scriptspre=Scripts/Precompiled
jarpathfile=Settings/path.txt

cd ~/RSBot

if [ ! -e "$jarpathfile" ]; then
echo "Path file does not exist. Please run RSBot and try again."
exit
fi

for file in $scripts/*.java
do
if [ ! -e "${file}" ]; then
echo "No .java script source files found."
     exit
fi
done

echo "Compiling scripts"
for file in $scripts/*.class
do
if [ -e "${file}" ]; then
rm -R $scripts/*.class
     break
fi
done

"$cc" $cflags -cp "/$(cat $jarpathfile)" $scripts/*.java