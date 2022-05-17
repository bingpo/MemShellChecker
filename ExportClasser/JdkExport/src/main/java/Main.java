import jdkExport.NameFilter;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ProcessUtils;


import static jdkExport.JdkExportClasser.export;

public class Main {
    public static Logger logger = LoggerFactory.getLogger(NameFilter.class);


    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException, ParseException {
        long pid = ProcessUtils.select(false, -1, null);
        if(pid==-1){
            Options options = new Options();
            options.addOption("h", "help", false, "print options information");
            options.addOption("p", "pid", true, "attach jvm process pid");
            CommandLineParser cliParser = new DefaultParser();
            HelpFormatter helpFormatter = new HelpFormatter();


            CommandLineParser parser = new DefaultParser();
            CommandLine cmdLine = parser.parse(options, args);

            try {
                export(cmdLine.getOptionValue("pid"));
            }catch (Exception err){
                helpFormatter.printHelp("java -jar JdkExport.jar -p test.jar", options);
            }

        }else {
            export(String.valueOf(pid));
        }

    }
}
