cd /apx/ExportFile2RN

java -cp ./lib/ojdbc8.jar:. exportFile2RN/ExportFile2RN

cp file2RN.json /apx/net_mounts/ppg


cat exportFile2RN.conf | mail -s "file2RN.json er kopieret til web"  -r noreply clag@dr.dk

cat exportFile2RN.conf | mail -s "file2RN.json er kopieret til web"  -r noreply frj@dr.dk
