package jdkExport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.tools.jcore.ClassDump;
import sun.jvm.hotspot.tools.jcore.ClassFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JdkExportClasser {
    private static final Logger logger = LoggerFactory.getLogger(JdkExportClasser.class);

    public static void export(String pidStr)throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException{
        int pid = Integer.parseInt(pidStr);
        ClassFilter filter = new NameFilter();

        ClassDump classDump = new ClassDump();
        classDump.setClassFilter(filter);
        classDump.setOutputDirectory("classout");

        Class<?> toolClass = Class.forName("sun.jvm.hotspot.tools.Tool");
        Method method = toolClass.getDeclaredMethod("start", String[].class);
        method.setAccessible(true);
        String[] params = new String[]{String.valueOf(pid)};

        try {
            method.invoke(classDump, (Object) params);
        } catch (Exception e) {
            logger.error(e.toString());
            return;
        }
        logger.info("dump class finish");
        Field field = toolClass.getDeclaredField("agent");
        field.setAccessible(true);
        HotSpotAgent agent = (HotSpotAgent) field.get(classDump);
        agent.detach();
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        export("22116");
    }
}
