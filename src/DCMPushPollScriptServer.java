import data.Configuration;
import data.Resource;
import data.Server;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DCMPushPollScriptServer implements DCMFileTranferCaller
{
    private Configuration           configuration;
//    private String                  pollerHeaderKey;
    private InetAddress             inetAddress;
    private String                  dcmServerIP;
    private Server                  server;
    private StringBuffer            command;
    private String                  dcmPollerScriptFile;
    private String                  dcmPollerDataFile;
    private DCMPushPollScriptCaller pushPollScriptCaller;
    private DCMFileTranferCaller    dcmFileTranferCaller;
    
    
    public DCMPushPollScriptServer(DCMPushPollScriptCaller pushPollScriptCallerParam, Server serverParam) throws UnknownHostException
    {
        // Generate the Poll Script and push it to the server
        dcmFileTranferCaller = this;
        pushPollScriptCaller =  pushPollScriptCallerParam;
        configuration =         new Configuration();
        inetAddress =           InetAddress.getLocalHost();
        dcmServerIP =           inetAddress.getHostAddress();
        server =                serverParam;
//        pollerHeaderKey =       "DCMPoller:" + server.getHost().getHostname() + ":";
        dcmPollerScriptFile =   ".dcmpollscript_" + dcmServerIP + "_" + server.getHost().getHostname() + ".sh";
        dcmPollerDataFile =     ".dcmpolldata_" + dcmServerIP + "_" + server.getHost().getHostname() + ".dat";
        command =               new StringBuffer();
        
        // Generate pollscript
        command.setLength(0);
        command.append("# This script is generated by DatacenterManager " + DCMLicense.getVersion() + " hosted on: " + dcmServerIP + " \n");
        command.append("# Please do not change this script (unless you know what you are doing) as it's maintained by DCM\n");
        command.append("# Check the latest DCM version: https://sites.google.com/site/ronuitzaandam/home\n");
        command.append("# If for whatever reason this script malfunctions, then just delete it and it will be pushed again\n");
        command.append("#\n");
        command.append("set +e \n"); // continue execution on errors
//        command.append("trap \"\" 1\n"); // HUP Signal ignored
        if (server.getHost().getSysinfo().contains("HP-UX")) { command.append("UNIX95=\"\"; export UNIX95 # Just to make ps -o work on HPUX\n"); } // ps -o format only working on HPUX when env var: UNIX95 is set
        command.append("#\n");
//        command.append("LANG=en_EN.UTF-8; export LANG\n");
        for (Resource resource:server.getResourceList())
        {
            if (resource.getEnabled())
            {
//                command.append("id").append(resource.getId()).append("=`").append(resource.getPollCommand().replaceAll("`", "\\\\`")).append("` && echo \"").append(pollerHeaderKey).append(" ").append(resource.getId()).append(" ${id").append(resource.getId()).append("}\" >> ").append(dcmPollerDataFile).append(" & \n");
//                command.append("id").append(resource.getId()).append("=`").append(resource.getPollCommand().replaceAll("`", "\\\\`")).append("` && echo \"").append(" ").append(resource.getId()).append(" ${id").append(resource.getId()).append("}\" >> ").append(dcmPollerDataFile).append(" & \n");
                command.append("id").append(resource.getId()).append("=`").append(resource.getPollCommand().replaceAll("`", "\\\\`")).append("` && echo \"").append(" ").append(resource.getId()).append(" ${id").append(resource.getId()).append("}\" >> ").append(dcmPollerDataFile).append(" \n");
            }
        }
        if (server.getHost().getCommand().length() > 0) // Add the hostcommand to the script if the host has one
        {
            command.append("#\n");
            command.append("# Host Command\n");
            command.append("#\n");
            command.append(server.getHost().getCommand()); // Don't append the & \n (happens in cmdlibraries)
            command.append("#\n");            
        }
        
        Thread PushPollScriptThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // Write the pollerscript string to file
                new DCMFileWrite(dcmPollerScriptFile,command.toString());

                // Send the file to server
                pushPollScriptCaller.log("DCMPollServer transfering pollscript to server: " + server.getHost().getHostname()+ " ...", true, true, true);                
                try { DCMFileTransfer fileTransfer = new DCMFileTransfer(dcmFileTranferCaller,server.getHost(),dcmPollerScriptFile); } catch (CloneNotSupportedException ex) { }
                pushPollScriptCaller.log("DCMPollServer transfering pollscript to server: " + server.getHost().getHostname()+ " completed", true, true, true);                
            }
        });
        PushPollScriptThread.setName("PushPollScriptThread");
        PushPollScriptThread.setDaemon(false);
        PushPollScriptThread.setPriority(Thread.NORM_PRIORITY);
        PushPollScriptThread.start();
    }

    public void log(String messageParam, boolean logToStatus, boolean logApplication, boolean logToFile)
    {
        pushPollScriptCaller.log(messageParam, logToStatus, logApplication, logToFile);
    }
}
