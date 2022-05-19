package org.sec;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.sec.asm.CheckClassVistor;
import org.sec.util.DirUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class Analysis {
    private static final Logger logger = LoggerFactory.getLogger("Analysis");

    public static List<Result> doAnalysis(List<String> fileList) throws Exception {
        logger.info("start analysis");
        List<Result> results = new ArrayList<>();


        int api = Opcodes.ASM9;
        int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;


        Map<String,List<String>> resultMap = new HashMap<String,List<String>>();

        for (String fileName : fileList) {
            byte[] bytes = Files.readAllBytes(Paths.get(fileName));
            if (bytes.length == 0) {
                continue;
            }
            ClassReader cr;
            ClassVisitor cv;
            try {
                cr = new ClassReader(bytes);
                cv = new CheckClassVistor(api,fileName,resultMap);
                cr.accept(cv, parsingOptions);


            } catch (Exception ignored) {
            }
        }

        List<String> importCharalList = new ArrayList<String>(){{
            add("javax/servlet/http/HttpServletRequest"); //tomcat
            add("org/springframework/web/servlet/mvc/method/annotation/RequestMappingHandlerMapping"); //spring 请求的path
            add("io/netty/handler/codec/http/HttpRequest"); //netty
        }};

        List<List<String>> sinkCharaList = new ArrayList<List<String>>(){{
            add(asList("java/lang/ProcessBuilder"));
            add(asList("java/lang/Runtime.exec"));
            add(asList("java/lang/Class.forName","java/lang/reflect/Method.invoke"));
            add(asList("java/lang/ClassLoader.defineClass"));
            add(asList("java/lang/Class.newInstance"));
            add(asList("sun/misc/BASE64Decoder"));
            add(asList("javax/crypto/Cipher"));
            add(asList("java/util/Base64"));
        }};

        List<String> strCharaList = new ArrayList<String>(){{
            add("shell");
            add("memshell");
            add("agentshell");
            add("exploit");
            add("payload");
            add("rebeyond");
            add("metasploit");
            add("filter_jsp");
            add("json");
        }};


        for (String classFilePathStr:resultMap.keySet()
        ) {
            boolean importFlag = false;
            for (String _importChara:importCharalList) {
                for (String _methodStr:resultMap.get(classFilePathStr)) {
                    if (_methodStr.contains(_importChara)) {
                        importFlag = true;
                        break;
                    }
                }
                if(importFlag) break;
            }

            // 如果没检测到import危险类，就continue
//            if (!importFlag) continue;


            boolean checkFlag = false;
            for (List<String> charaSubList:sinkCharaList) {
                for (String charaStr:charaSubList) {
                    if(resultMap.get(classFilePathStr).contains(charaStr)){
                        checkFlag = true;
                    }
                }


                if (checkFlag){
                    logger.info("====================================================================================");
                    logger.info(classFilePathStr);
                    for (String charaStr:resultMap.get(classFilePathStr)) {
                        logger.info(charaStr);
                    }
                    break;
                }
            }

            if(!checkFlag){
                String evilCharaStr = "";
                for (String strChara:strCharaList) {
                    byte[] bytes = Files.readAllBytes(Paths.get(classFilePathStr));
                    if (new String(bytes).contains(strChara)){
                        checkFlag = true;
                        evilCharaStr = strChara;
                    }
                }
                if (checkFlag){
                    logger.info("====================================================================================");
                    logger.info(classFilePathStr);
                    logger.info("存在关键词：" + evilCharaStr);
                }

            }

        }

        return results;

    }

    public static void main(String[] args) throws Exception {
//        List<String> files = DirUtil.getFiles("E:\\tmp\\FindShell\\classout\\listeners");
        List<String> files = DirUtil.getFiles("E:\\tmp\\memshell");
        List<Result> results = Analysis.doAnalysis(files);
    }
}
