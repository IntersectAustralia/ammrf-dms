#!/bin/bash
# Script to trim and resize all screenshots
# Resized images are written to resized directory
# Requires ImageMagik to be installed

CURRENT_DIR=$(cd "$(dirname "$0")"; pwd)
TARGET_DIR=$CURRENT_DIR/resized
echo "Saving rezised images to $TARGET_DIR"
rm -f $TARGET_DIR/*.*

FILES=screenshots/*.png

count=0
for f in $FILES
do
  echo "Processing $f file..."
  # take action on each file. $f store current file name
  fileName=$(basename $f)
  convert $f -trim -resize 1200 $TARGET_DIR/$fileName
  count=`expr $count + 1`
done
echo "Total number of precessed images is: $count"