import data.Host;
import data.Resource;
import data.Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.UnknownHostException;
import java.util.Calendar;

public class DCMDataConverterDarwin // Being called by InventoryServer and PollServer to convert host & resources txt data to a server object(host,resources[]) and uses CommandsLibrary to get the right polling command for each resource
{
    //Server   server;
    Host     host;
    Resource resource;
    DCMCommandLibraryDarwin osCommands;
    int resCnt = 0;
    int pollServerInstance;
    int retentionTime;
//    String      headerKey;
    
    public DCMDataConverterDarwin(Host hostParam, int retentionTimeParam) throws UnknownHostException
    {
        //server = new Server();
        host = hostParam;
        osCommands = new DCMCommandLibraryDarwin(host);
	retentionTime = retentionTimeParam;
    }
    
    public Server convertInventoryDataToServer(String dataParam) // This method (used by the InventoryServer Object) takes a host and txt data and converts it into a full Server object and returns it
    {
//        host = hostParam;
//        headerKey = "DCMInventory:" + host.getHostname() + ": ";

        Server outputServer = new Server();
        String dataHeaders = "";
        BufferedReader reader;
        String line = "";
        String output = "";

        long        id =            0;
        long        hostId =        0;
        String      category =      "";
        String      resourceType =  "";
        String      valueType =     "";
        String      counterType =   "";
        String      resourceName =  "";
        boolean     enabled =       true;
        String      pollCommand =   "";
        Double      lastValue =     0D;
        Double      warningLimit =  0D;
        Double      criticalLimit = 0D;
        int         alertPolls =    30;
        Calendar    created =       Calendar.getInstance(); created.setTimeInMillis(0);
        String      rrdFile =       "";
                
// ====================================== Building Server.Host Sysinfo Resources from data ========================================        
        
        outputServer.setHost(host);        
        host.setCommand(osCommands.getPSCPUHostCommand()); // Add a host command
        host.addCommand(osCommands.getPSMEMHostCommand()); // Add a host command
        host.addCommand(osCommands.getCPUHostCommand()); // Set a host command
        host.addCommand(osCommands.getDiskIOHostCommand()); // Add a host command

// ====================================== Building Server MinLoad Resource from data ========================================

//      This section adds minuteload)        
        id =            0;
        hostId =        0;
        category =      "WORKLOAD";
        resourceType =  "MINUTELOAD";
        valueType =     "Factor";
        counterType =   "GAUGE";
        resourceName =  "MINLOAD";
        enabled =       true;
        pollCommand =   "";
        lastValue =     0D;
        warningLimit =  0D;
        criticalLimit = 0D;
        alertPolls =    30;
        created =       Calendar.getInstance(); created.setTimeInMillis(0);
        rrdFile =       "";

        id = resCnt; resCnt++; pollCommand = osCommands.getWorkload(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

// ====================================== Building Server CPU Resources from data ========================================
        
//        dataHeaders = headerKey + "CPU: ";
        dataHeaders = "CPU: ";
        reader = new BufferedReader(new StringReader(DCMTools.startsWith(dataParam, dataHeaders)));
        line = "";
        output = "";

        try
        {
            while ((line = reader.readLine()) != null)
            {
                id =            0;
                hostId =        0;
                category =      "CPU";
                resourceType =  "";
                valueType =     "Percentage";
                counterType =   "GAUGE";
                resourceName =  line.replace(dataHeaders, "");
                enabled =       true;
                pollCommand =   "";
                lastValue =     0D;
                warningLimit =  0D;
                criticalLimit = 0D;
                alertPolls =    30;
                created =       Calendar.getInstance(); created.setTimeInMillis(0);
                rrdFile =       "";

                // These id's will not be inserted in the database due to autonumbering, so the pollscript will contain autonumbered resourceid's
                id = resCnt; resCnt++; resourceType = "USR"; pollCommand = osCommands.getCPUUSERCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "SYS"; pollCommand = osCommands.getCPUSYSCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "IDL"; pollCommand = osCommands.getCPUIDLECommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);
            }

        } catch(IOException e) { e.printStackTrace(); }

        
// ====================================== Building Server DiskIO Resources from data ========================================
        
//        dataHeaders = headerKey + "DiskIO: ";
        dataHeaders = "DiskIO: ";
        reader = new BufferedReader(new StringReader(DCMTools.startsWith(dataParam, dataHeaders)));
        line = "";
        output = "";

        try
        {
            while ((line = reader.readLine()) != null)
            {
                id =            0;
                hostId =        0;
                category =      "DiskIO";
                resourceType =  "";
                valueType =     "";
                counterType =   "GAUGE";
                resourceName =  line.replace(dataHeaders, "");
                enabled =       true;
                pollCommand =   "";
                lastValue =     0D;
                warningLimit =  0D;
                criticalLimit = 0D;
                alertPolls =    30;
                created =       Calendar.getInstance(); created.setTimeInMillis(0);
                rrdFile =       "";

                id = resCnt; resCnt++; resourceType = "KBPerTransfer"; valueType  = "KB"; pollCommand = osCommands.getDiskIOKBPerTransferCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "TransfersPerSec"; valueType  = "Transfers"; pollCommand = osCommands.getDiskIOTransfersPerSecondCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "MBPerSecond"; valueType  = "MB"; pollCommand = osCommands.getDiskIOMBPerSecondCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);
            }

        } catch(IOException e) { e.printStackTrace(); }

        
// ====================================== Building Server Memory Resources from data ========================================
        
        id =            0;
        hostId =        0;
        category =      "Memory";
        resourceType =  "";
        valueType =     "MB";
        counterType =   "GAUGE";
        resourceName =  line;
        enabled =       true;
        pollCommand =   "";
        lastValue =     0D;
        warningLimit =  0D;
        criticalLimit = 0D;
        alertPolls =    30;
        created =       Calendar.getInstance(); created.setTimeInMillis(0);
        rrdFile =       "";

        id = resCnt; resCnt++; resourceType = "RAMTOT"; resourceName =  "RAMTOT"; pollCommand = osCommands.getRAMTOTCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "RAMUSED"; resourceName =  "RAMUSED"; pollCommand = osCommands.getRAMUSEDCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "RAMFREE"; resourceName =  "RAMFREE"; pollCommand = osCommands.getRAMFREECommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "SWAPTOT"; resourceName =  "SWAPTOT"; pollCommand = osCommands.getSWAPTOTCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "SWAPUSED"; resourceName =  "SWAPUSED"; pollCommand = osCommands.getSWAPUSEDCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "SWAPFREE"; resourceName =  "SWAPFREE"; pollCommand = osCommands.getSWAPFREECommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "TOTMEM"; resourceName =  "TOTMEM"; pollCommand = osCommands.getTOTMEMCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "TOTUSED"; resourceName =  "TOTUSED"; pollCommand = osCommands.getTOTUSEDCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "TOTFREE"; resourceName =  "TOTFREE"; pollCommand = osCommands.getTOTFREECommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

// ====================================== Building Server Storage Resources from data ========================================
        
//        dataHeaders = headerKey + "Storage: ";
        dataHeaders = "Storage: ";
        reader = new BufferedReader(new StringReader(DCMTools.startsWith(dataParam, dataHeaders))); // This only gets the "Storage: " filesystems lines from the data and creates 4 resources for every FS
        line = "";
        output = "";

        try
        {
            while ((line = reader.readLine()) != null)
            {
                id =            0;
                hostId =        0;
                category =      "Storage";
                resourceType =  "";
                valueType =     "MB";
                counterType =   "";
                resourceName =  line.replace(dataHeaders, "");
                enabled =       true;
                pollCommand =   "";
                lastValue =     0D;
                warningLimit =  0D;
                criticalLimit = 0D;
                alertPolls =    30;
                created =       Calendar.getInstance(); created.setTimeInMillis(0);
                rrdFile =       "";

                id = resCnt; resCnt++; resourceType = "FSTot"; valueType  = "MB"; counterType = "GAUGE"; pollCommand = osCommands.getFSTOTCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName.replace("/", "-") + ".rrd";
                if (resourceName.length() <= 20) {resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);}
                else {resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName.substring(resourceName.length()-20, resourceName.length()),true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);}
                outputServer.addResource(resource);
                
                id = resCnt; resCnt++; resourceType = "FSUsed"; valueType = "MB"; counterType = "GAUGE"; pollCommand = osCommands.getFSUSEDCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName.replace("/", "-") + ".rrd";
                if (resourceName.length() <= 20) {resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);}
                else {resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName.substring(resourceName.length()-20, resourceName.length()),true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);}
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "FSFree"; valueType = "MB"; counterType = "GAUGE"; pollCommand = osCommands.getFSFREECommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName.replace("/", "-") + ".rrd";
                if (resourceName.length() <= 20) {resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);}
                else {resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName.substring(resourceName.length()-20, resourceName.length()),true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);}
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "FSUsedPerc"; valueType = "Percentage"; counterType = "GAUGE"; pollCommand = osCommands.getFSUSEDPercCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName.replace("/", "-") + ".rrd";
                if (resourceName.length() <= 20) {resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);}
                else {resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName.substring(resourceName.length()-20, resourceName.length()),true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);}
                outputServer.addResource(resource);
            }

        } catch(IOException e) { e.printStackTrace(); }
        
// ====================================== Building Server Network Resources from data ========================================
        
//        dataHeaders = headerKey + "Network: ";        
        dataHeaders = "Network: ";        
        reader = new BufferedReader(new StringReader(DCMTools.startsWith(dataParam, dataHeaders)));
        line = "";
        output = "";

        try
        {
            while ((line = reader.readLine()) != null)
            {
                id =            0;
                hostId =        0;
                category =      "Network";
                resourceType =  "";
                valueType =     "";
                counterType =   "COUNTER";
                resourceName =  line.replace(dataHeaders, "");
                enabled =       true;
                pollCommand =   "";
                lastValue =     0D;
                warningLimit =  0D;
                criticalLimit = 0D;
                alertPolls =    30;
                created =       Calendar.getInstance(); created.setTimeInMillis(0);
                rrdFile =       "";

                id = resCnt; resCnt++; resourceType = "TXPackets"; valueType  = "Packets"; pollCommand = osCommands.getIFTXPacketsCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName.replace("/", "-") + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "RXPackets"; valueType = "Packets"; pollCommand = osCommands.getIFRXPacketsCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName.replace("/", "-") + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "TXErr"; valueType = "Errors"; pollCommand = osCommands.getIFTXErrCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName.replace("/", "-") + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "RXErr"; valueType = "Errors"; pollCommand = osCommands.getIFRXErrCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName.replace("/", "-") + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);

                id = resCnt; resCnt++; resourceType = "Collisions"; valueType = "Collisions"; pollCommand = osCommands.getIFCollisionCommand(resourceName); rrdFile = category + "_" + resourceType + "_" + resourceName.replace("/", "-") + ".rrd";
                resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
                outputServer.addResource(resource);
            }

        } catch(IOException e) { e.printStackTrace(); }
        
// ====================================== Building Server TCPSTATS Resources from data ========================================
        
        id =            0;
        hostId =        0;
        category =      "TCPSTATS";
        resourceType =  "";
        valueType =     "NumOf";
        counterType =   "GAUGE";
        resourceName =  line;
        enabled =       true;
        pollCommand =   "";
        lastValue =     0D;
        warningLimit =  0D;
        criticalLimit = 0D;
        alertPolls =    30;
        created =       Calendar.getInstance(); created.setTimeInMillis(0);
        rrdFile =       "";

        id = resCnt; resCnt++; resourceType = "ESTABLISHED"; resourceName =  "ESTABLISHED"; pollCommand = osCommands.getTCPSTATESTABLISHEDCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "SYN_SENT"; resourceName =  "SYN_SENT"; pollCommand = osCommands.getTCPSTATSYN_SENTCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "SYN_RECV"; resourceName =  "SYN_RECV"; pollCommand = osCommands.getTCPSTATSYN_RECVCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "FIN_WAIT1"; resourceName =  "FIN_WAIT1"; pollCommand = osCommands.getTCPSTATFIN_WAIT1Command(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "FIN_WAIT2"; resourceName =  "FIN_WAIT2"; pollCommand = osCommands.getTCPSTATFIN_WAIT2Command(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "TIME_WAIT"; resourceName =  "TIME_WAIT"; pollCommand = osCommands.getTCPSTATTIME_WAITCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "CLOSED"; resourceName =  "CLOSED"; pollCommand = osCommands.getTCPSTATCLOSEDCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "CLOSE_WAIT"; resourceName =  "CLOSE_WAIT"; pollCommand = osCommands.getTCPSTATCLOSE_WAITCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "LAST_ACK"; resourceName =  "LAST_ACK"; pollCommand = osCommands.getTCPSTATLAST_ACKCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "LISTEN"; resourceName =  "LISTEN"; pollCommand = osCommands.getTCPSTATLISTENCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "CLOSING"; resourceName =  "CLOSING"; pollCommand = osCommands.getTCPSTATCLOSINGCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "UNKNOWN"; resourceName =  "UNKNOWN"; pollCommand = osCommands.getTCPSTATUNKNOWNCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

// ====================================== Building Server Generic System Resources from data ========================================
        
        id =            0;
        hostId =        0;
        category =      "GENERIC";
        resourceType =  "";
        valueType =     "";
        counterType =   "GAUGE";
        resourceName =  line;
        enabled =       true;
        pollCommand =   "";
        lastValue =     0D;
        warningLimit =  0D;
        criticalLimit = 0D;
        alertPolls =    30;
        created =       Calendar.getInstance(); created.setTimeInMillis(0);
        rrdFile =       "";

        id = resCnt; resCnt++; resourceType = "NUMOFUSERS"; resourceName =  "NUMOFUSERS"; valueType = "Users"; pollCommand = osCommands.getNUMOFUSERSCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "NUMOFPROCS"; resourceName =  "NUMOFPROCS"; valueType = "Processes"; pollCommand = osCommands.getNUMOFPROCSCommand(host); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        // ====================================== Building Server Process Resources from data ========================================
        
        id =            0;
        hostId =        0;
        category =      "PROCESS";
        resourceType =  "";
        valueType =     "";
        counterType =   "GAUGE";
        resourceName =  line;
        enabled =       true;
        pollCommand =   "";
        lastValue =     0D;
        warningLimit =  0D;
        criticalLimit = 0D;
        alertPolls =    30;
        created =       Calendar.getInstance(); created.setTimeInMillis(0);
        rrdFile =       "";

        id = resCnt; resCnt++; resourceType = "PSCPUPID"; resourceName =  "PS1CPUPID"; valueType = "PID"; pollCommand = osCommands.getPS1CPUPIDCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSCPU"; resourceName =  "PS1CPU"; valueType = "Percentage"; pollCommand = osCommands.getPS1CPUCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSCPUPID"; resourceName =  "PS2CPUPID"; valueType = "PID"; pollCommand = osCommands.getPS2CPUPIDCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSCPU"; resourceName =  "PS2CPU"; valueType = "Percentage"; pollCommand = osCommands.getPS2CPUCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSCPUPID"; resourceName =  "PS3CPUPID"; valueType = "PID"; pollCommand = osCommands.getPS3CPUPIDCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSCPU"; resourceName =  "PS3CPU"; valueType = "Percentage"; pollCommand = osCommands.getPS3CPUCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);
        
        id = resCnt; resCnt++; resourceType = "PSMEMPID"; resourceName =  "PS1MEMPID"; valueType = "PID"; pollCommand = osCommands.getPS1MEMPIDCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSMEM"; resourceName =  "PS1MEM"; valueType = "Percentage"; pollCommand = osCommands.getPS1MEMCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSMEMPID"; resourceName =  "PS2MEMPID"; valueType = "PID"; pollCommand = osCommands.getPS2MEMPIDCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSMEM"; resourceName =  "PS2MEM"; valueType = "Percentage"; pollCommand = osCommands.getPS2MEMCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSMEMPID"; resourceName =  "PS3MEMPID"; valueType = "PID"; pollCommand = osCommands.getPS3MEMPIDCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        id = resCnt; resCnt++; resourceType = "PSMEM"; resourceName =  "PS3MEM"; valueType = "Percentage"; pollCommand = osCommands.getPS3MEMCommand(); rrdFile = category + "_" + resourceType + "_" + resourceName + ".rrd";
        resource = new Resource(id,hostId,category,resourceType,valueType,counterType,resourceName,true,pollCommand,lastValue,warningLimit,criticalLimit,alertPolls,created,rrdFile,retentionTime);
        outputServer.addResource(resource);

        
        return outputServer;
    }
}
