# Requirements #

All components (mainserver, webserver, clustercomputer) must use the "fire and forget" principle. This means that components must self-configure themselves, must find each other automatically, and must be controllable remotely.

Components can be started or stopped at any time, without severely affecting other components.

All persistent data must stored on the webserver.

# Details #

Setup involves running a RAWI-MainServer.jar on a computer, running RAWI-ClusterComputer.jar on zero or more other computers, and deploying RAWI-WebServer.war on a servlet container. A user then goes to http://.../rawi/ and receives an user interface.

## Working with Transformation Models ##

  * The user should be presented with a list of available transformation models.
    * The client interface must request this list and show it when the interface is first started
    * This list resides on the webserver... the webserver must have a servlet that serves a list of models in XML format
      * If the models are in a well-known location (one returned by context.getRealPath("/models")), then scan the folder for .xml files, and consider each file name (sans the .xml extension) as a model name
      * If the models are in a database (requires setting up a database), then just do a simple query
      * (more detail needed)
  * The user should be allowed to create a new model from scratch.
    * This means a "New Model" button. No requests to the webserver are sent yet.
    * The new model will have a single pack node (or even no nodes), and will have a generic name ("Untitled Model")
  * The user creates new nodes on the transformation model, links them to each other, and sets various properties
    * Not required, but the client could periodically ask the webserver to validate a model without saving it
      * The webserver needs a servlet for this
      * To validate it, the webserver needs to ask the mainserver. For this, it must know where the mainserver is...
      * The webserver queries the IP tracker to find a mainserver...
        * This assumes that the mainserver announced its presence to the IP tracker...
          * Every 2-3 minutes, the mainserver must send a request to http://testbot73.appspot.com/PutIPServlet?type=MainServer&name=IpAddress, probably for each IP address it has
          * The mainserver must figure out its IPs whenever it sends a request
        * If the IP tracker knows of no mainservers, it sends a "Cannot validate; no mainserver found" response to the client
        * If it receives one or more IPs, it checks them one by one (with RMI) until one responds
          * (exactly how to check is not yet clear... probably by attempting to call a ping() method that is exported by the mainserver through RMI)
      * If it finds a mainserver, it asks it to validate the model
        * This involves just a RMI call
        * The server will parse the model XML, attempt to construct a representation, and return whether it succeeded (the representation is then discarded)
      * The webserver will take the mainserver's response, and construct an XML from it
        * (no idea how... maybe by forwarding to a JSP page)
      * The web client receives this XML as a response, and in case of an error, it highlights the guilty node, and somehow shows the message (in a statusbar or tooltip)
    * The user clicks "Save model", and expects to see the model added to the listbox of available models
      * If the user did not change the name from "Untitled Model", then "Save model" will not work
      * Otherwise, it sends a Save request to the webserver
        * The webserver needs a servlet for this... probably the same one as before
        * The webserver validates the model (as above)
        * On success, it somehow saves/overwrites the XML in a well-known fixed location
          * Either as a file on a path returned by context.getRealPath("/models")
          * Or as text in a database (but this requires a database)
        * The webserver responds with an XML response (whether successful or not, as above)
      * The client receives the XML, and if successful, it adds the model to a listbox.
    * The user may want to see or modify a stored model
      * The web client must be able to retrieve a model XML from the webserver
        * If the model was stored in a well-known location, no servlet is needed for this
      * The web client downloads http://example.com/rawi/models/Model%20Name.xml, and displays it as a graph

## Working with Work Sessions (client's perspective) ##
  * The user decides to create a work session, by choosing a model from a list; the user may also choose a mainserver to process it, or let the webserver decide
    * This implies that the user can ask for a list of models, and a list of mainservers
      * (more info needed)
  * The user clicks 'CreateSession'
    * This sends a CreateSession request with the name of the XML to the webserver
    * A servlet is needed for this
      * It must make sure that the XML actually exists; otherwise it will respond with a 404 error
      * If no mainserver was explicitly specified, try each one until one responds
      * If the XML exists and mainserver is found, it will send a CreateSession call to a mainserver
        * If the mainserver replies with a success message, register the new session
      * On failure, send a message back to the client with the reason why
      * On success, send the needed IDs and URLs to the client
        * The client needs to have a way to refer to sessions; the session ID is needed
        * The client needs to be able to upload/download files, URLs are needed
        * (probably more)
  * The client must be able to ask for the status of a session, such as what files exist in pack nodes, the status of pack/transformation nodes, the status of the session itself (started/stopped), etc
    * This information is provided directly by the mainserver; the webserver must simply relay it
  * The user wants to upload a file to a pack in the session, by selecting a pack, a file, and clicking 'upload'.
    * The client will send a multi-part upload request to the webserver of the form http://example.com/rawi/upload/[SessionID]/path/to/file.txt
      * The same mechanism will also be used by clustercomputers; also, note that the pack is not specified here, but a session is... this means that all files belonging to a session must be deleted when the session is destroyed
      * Since more files of the same name can exist (although each with a potentially different content), the associated download path must contain, besides the logical file name (path/to/file.txt), a unique identifier, such as an auto-incremented integer
        * This means that the uploader must be notified of this ID; if the upload is successfully finished, the webserver will respond with a string of the form: "File ID: 2\n", which means that the uploaded file can be downloaded with the URL http://example.com/rawi/download/[SessionID]/2/path/to/file.txt
    * Once the upload is complete, the client will ask the server to associate the file with a pack, by sending a request to the URL http://example.com/rawi/put/[SessionID]/[FileID]/path/to/file.txt?inPack=[PackID]
      * Note that the same file may be associated with zero, one, or even more packs; however, the webserver does not have to be aware of these associations
      * The webserver will relay this command to a mainserver through a RMI call; the parameters of the call will be at least the session ID, a FileHandle object (part of RAWI-Common), and the pack ID.
        * The mainserver will either accept the command, or return a descriptive error; the error will most likely be in the form of a string
      * The webserver will respond to the client either with a confirmation message, or with the error and an http status different from 200
  * The user then wants to download the same file, by selecting a pack, and a file the pack's list of files
    * The client must form the download URL; it has the address of the webserver, the session ID, the pack ID, and the file ID and logical name
    * The download URL will then be: http://example.com/rawi/download/[SessionID]/[FileID]/[LogicalName] - note that the pack ID is not actually used
      * Once a request is sent to this address, the webserver simply must find and return the contents of this file
  * Since all files related to a session are deleted when the session is destroyed, the user will want to have a permanent space on the webserver, to avoid uploading the same file over and over again; this permanent space will be the FileBox™.
    * To upload to the FileBox, the client must send a multi-part upload request to a URL of the form http://example.com/rawi/upload/filebox/path/to/file.txt
      * If a file by that name already exists, it will be overwritten
      * Where and how the file is stored is irrelevant... however, the user will almost always want to copy the file to a session, and as such, the ideal way to store it (which is not necessarily required) would be one that does not involve any actual copying on the disk; for example, the file might be stored on disk with a UUID filename, and simply be associated with one or more usual files (which have either a FileBox logical name, or a session ID, file ID, and logical name)
    * To download a file from the FileBox, the client will use a URL of the form http://example.com/rawi/download/filebox/path/to/file.txt
    * There must be a way to copy a file from the FileBox to a session, and from a session to the FileBox
      * (not yet sure how the URLs will look like)
    * Optionally, the webserver could support extracting an archive in the FileBox, with the resulting files also being in the FileBox
  * (to be continued)

## Working with Work Sessions (cluster computer's perspective) ##

Notes:
  * The mainserver has a list of tasks which will be distributed to cluster computers. This list is dynamic; some of its contents will depend on the results of other tasks, and as such, the list is incomplete at the beginning. This means tasks will be sent one by one, not all at once in the beginning. However...
  * The entire process needs to be fast. In the most naive case, a cluster computer will download files, execute a task, upload files, and repeat... this means that when downloading/uploading, CPU is not used, and when executing a task, network bandwidth is not used. To use both resources continuously, the mainserver may send another task before the previous one is completed; the cluster computer must queue it, and make sure it uses both CPU and network at the same time, whenever possible.
  * One of the most important aspects of a distributed system is failover (fault tolerance), meaning "What happens if one of the cluster computers crashes, or a main server crashes, or the network temporarily goes down?" The ideal answer is, of course, "The system quickly recovers and finds another way to finish the process as fast as possible."

Analysis:
  * Due to the zero-configuration requirement, as soon as the clustercomputer process is started, it must retrieve a list of mainservers and notify each one of its presence
    * Each RMI call should be done in a new thread, because a RMI call to an unresponsive IP blocks the current thread for a time; checking 50 unresponsive IPs in the same thread is a recipe for failure
    * When notifying its presence to the mainserver, some state information must also be sent, such as its IP and its number of CPUs or CPU cores; this information will be sent in a structure
  * A mainserver may be started (or restarted) after a clustercomputer; the discovery must be immediate, and as such, the clustercomputer must be able to receive notifications itself; a clustercomputer may receive a RMI call that asks if it is alive, to which it must respond with its state information (same as above)
  * The clustercomputer should allow pings from a mainserver, and should be able to ping a mainserver (when this is used is detailed further down)
    * A ping will be done through a RMI function, which simply receives an integer value and returns the same value.
  * With the above implemented, the mainserver will always know when a clustercomputer is alive; the clustercomputer must now await requests from a mainserver
  * When a mainserver wants a clustercomputer to execute a task, it will send a RMI call with details about the task
    * These details will include at least a task ID, an IP to send the result to, a list of files to be downloaded, an URL to use for downloads, an URL to use for uploads, and a command to execute.
      * (in the future it may also contain information about the executable, environment variables, etc)
      * It might happen that a mainserver is restarted while a task is being executed, which means that a clustercomputer may sometimes send a task result to the wrong mainserver; to prevent a session from being irrevocably disrupted (due to the mainserver receiving wrong files), the task ID (or a separate session ID) must be unique... specifically, it must be an UUID string
    * The RMI call must return immediately (before it begins processing the task)
    * The clustercomputer must, in another thread, queue the task to be executed
  * Executing tasks will be done in two (or three) permanent threads: one (or two) that handles downloading and uploading, and one that handles executables; the threads are started as soon as the clustercomputer itself is started
    * Each thread will wait for semi-tasks, and start working on them as soon as one arrives. This will be done with locks, concurrent queues, wait/notify, or any other mechanism.
    * A finished download semi-task will trigger an execute semi-task, and a finished execute semi-task will trigger an upload-semi task; basically, a pipeline
  * Once a task is finished, the clustercomputer will try to send the results (in a structure) to the mainserver; since all RMI calls might block the thread for a long time, this will be done in a new and temporary thread
    * If the RMI call times out, the cluster computer must keep trying for at least three minutes, with at least five seconds between each try
    * The task result structure will contain at least the task ID, and a list of new files
  * (to be continued)