package code.landgrey.copagent;

import utils.ClassUtils;
import utils.LogUtils;
import utils.PathUtils;
import utils.SearchUtils;
import com.sun.org.apache.bcel.internal.Repository;
import com.sun.org.apache.bcel.internal.util.ClassPath;
import com.sun.org.apache.bcel.internal.util.SyntheticRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Agent {
    public static File agent_work_directory = null;

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        if(agentArgs==null){
            agentArgs = "[unknown]";
        }
        instrumentation.addTransformer(new DefineTransformer(), true);
        catchThief(agentArgs, instrumentation);

    }

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if(agentArgs==null){
            agentArgs = "[unknown]";
        }
        instrumentation.addTransformer(new DefineTransformer(), true);
        catchThief(agentArgs, instrumentation);

    }



    static class DefineTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            return classfileBuffer;
        }
    }


    private static synchronized void catchThief(String name, Instrumentation ins){
        LogUtils.logit("Agent.jar is success attached");
        LogUtils.logit("Current Agent.jar Directory : " + PathUtils.getCurrentDirectory());
        LogUtils.logit("Prepared Dump class name    : " + name);

        List<Class<?>> resultClasses = new ArrayList<Class<?>>();

        // 获得所有已加载的类及类名
        Class<?>[] loadedClasses = ins.getAllLoadedClasses();
        LogUtils.logit("Found All Loaded Classes    : " + loadedClasses.length);
        List<String> loadedClassesNames = new ArrayList<String>();
        for(Class<?> cls: loadedClasses){
            loadedClassesNames.add(cls.getName());
        }

        // 单独保存已加载的类名及 hashcode、classloader、classloader hashcode 信息
        agent_work_directory = new File(PathUtils.getCurrentDirectory());
        File allLoadedClassFile = new File(new File(agent_work_directory, "logs"), "allLoadedClasses.txt");
        LogUtils.logit("Prepared Store All Loaded Classes Name ...");
        PathUtils.appendTextToFile(allLoadedClassFile, "[*] Format: [classname | class-hashcode | classloader | classloader-hashcode]\n");
        ClassUtils.storeAllLoadedClassesName(allLoadedClassFile, loadedClasses);
        LogUtils.logit("All Loaded Classes Name Store in : " + allLoadedClassFile.getAbsolutePath());


        // 默认没有指定具体类名的流程
        if(name.equals("[unknown]")){
            List<String> interfaces = null;

            for(Class<?> clazz: loadedClasses){
                try {
                    if(loadedClassesNames.contains(clazz.getName()) && !clazz.getName().contains("$")){
                        LogUtils.logit("dump class: " + clazz.getName());
                        ins.retransformClasses(clazz);
//                        ClassUtils.dumpClass(ins, clazz.getName(), false, null);
                    }else{
                        LogUtils.logit("cannot find " + clazz.getName() + " classes in instrumentation");
                    }
                }catch (Exception e){
                    LogUtils.logit(e.getMessage());
                }catch (Throwable e){
                    LogUtils.logit(e.getMessage());
                }

            }
        }else{
            if(loadedClassesNames.contains(name)){
                Class<?> clazz = ClassUtils.dumpClass(ins, name, false, null);
                resultClasses.add(clazz);
            }else if(name.contains("*")){
                Set<Class<?>> findClasses = SearchUtils.searchClass(ins, name, true, null);
                while(findClasses.iterator().hasNext()){
                    Class<?> clazz = findClasses.iterator().next();
                    resultClasses.add(clazz);
                    ClassUtils.dumpClass(ins, clazz.getName(), false, null);
                }
            }else{
                LogUtils.logit("class name [" + name + "] not found in loaded classes");
            }
        }

    }
}
