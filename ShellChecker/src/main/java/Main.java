import org.apache.commons.cli.*;
import org.sec.Analysis;
import org.sec.Result;
import org.sec.util.DirUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        // java 7 环境运行的
        // 1、通过检测import，以及superclass，但是很难，因为有多层继承
        // 2、检测sink，反射
        // 3、检测关键词，比如exploit
        Option zipOpt = new Option("d","directory",true,"class directory");
        zipOpt.setRequired(true);
        Options options = new Options();
        options.addOption(zipOpt);
        CommandLine cli = null;
        CommandLineParser cliParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            cli = cliParser.parse(options, args);
            try {
                List<String> files = DirUtil.getFiles(cli.getOptionValue("d"));
                List<Result> results = Analysis.doAnalysis(files);
            } catch (Exception e) {
                logger.error(e.toString());
            }

        } catch (ParseException e) {
            // 解析失败是用 HelpFormatter 打印 帮助信息
            helpFormatter.printHelp("java -jar ShellChecker.jar -d ./test", options);
        }

    }
}
