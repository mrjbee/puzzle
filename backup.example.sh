#!/bin/bash
DEVICE=/dev/sdd
PARTITION_DEVICE=/dev/sdd1
LAST_RUN_FILE=/mnt/Storage/last_backup_date

TODAY=`date +%D`

if [ -f $LAST_RUN_FILE ];
then
   echo "Found last backup date file"
   LAST_BACK_UP_DATE=`cat $LAST_RUN_FILE`
   if [ $LAST_BACK_UP_DATE = $TODAY ];
   then
	echo "One backup was done today already. Today is $TODAY"
	udisks --unmount $PARTITION_DEVICE
        udisks --detach $DEVICE
	exit 1
   fi
fi

echo "Going to mount and perform backup ... "
udisks --mount $PARTITION_DEVICE
rsync --partial -r --progress --bwlimit=10000 /mnt/Storage/Catalog/ /media/MediaBackup1/Catalog
echo $TODAY > $LAST_RUN_FILE
echo "Backup done. Backup date $TODAY"
udisks --unmount $PARTITION_DEVICE
udisks --detach $DEVICE
echo "Exit from backup procedure"
