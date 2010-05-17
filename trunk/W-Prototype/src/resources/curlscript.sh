HOST=http://5.146.43.252:8084/I-Prototype

echo "Step 1: Validate XML"
curl -s $HOST/ValidateXMLServlet --data-urlencode name=RaytracerModel.xml --data-urlencode savexml=true --data-urlencode xml@RaytracerModel.xml > /dev/null

echo "Step 2: Create Session"
Response=`curl -s $HOST/CreateSession?modelName=RaytracerModel.xml&mainServerIp=127.1`
SessionId=`echo $Response | sed 's/\r/\n/g' | grep sessionId | sed 's/ \+<sessionId>\(.*\)<.*/\1/g' | sed 's/ //g'`
echo "Session ID: $SessionId"

if [ ! -n "$SessionId" ]; then
   echo "Couldn't create a session. :("
   exit
fi

function uploadToPack()
{
   echo "Step 3: Upload $1"
   Response=`curl -s $HOST/TheUploadServlet/$SessionId/$1$3 -F file=@$1`
   FileId=`echo $Response | sed 's/File ID: \([0-9]\+\).*/\1/'`
   
   echo "Step 4: Put $1 in pack $2"
   Response=`curl -s "$HOST/PutFileInPackServlet?sessionId=$SessionId&fileId=$FileId&fileName=$1&packId=$2"`
}


uploadToPack Splitter.exe 8
uploadToPack TinyRaytracer.zip 9 ?zipFile=true
uploadToPack Joiner.exe 10
uploadToPack cod.cad 1

echo "Step 5: Start session"
Response=`curl -s "$HOST/StartStopSession?sessionId=$SessionId&action=start"`

cmd /c start "$HOST/svgSessionStatus.jsp?sessionId=$SessionId"
