package jdkExport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.Symbol;
import sun.jvm.hotspot.tools.jcore.ClassFilter;
import sun.jvm.hotspot.tools.jcore.ClassWriter;
import sun.jvm.hotspot.utilities.U1Array;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class NameFilter implements ClassFilter {
    private static final Logger logger = LoggerFactory.getLogger(NameFilter.class);
    private static final List<String> blackList = new ArrayList<String>();

    static {
        Constant.blackList.forEach(s -> blackList.add(s.split("#")[0]));
    }

    public NameFilter() {
    }


    public void dumpClass(InstanceKlass instanceKlass){
        try {
            String klassName = instanceKlass.getName().asString();
            OutputStream os = null;

            int index = klassName.lastIndexOf("/");
            File dir = null;
            if (index != -1) {
                String dirName = klassName.substring(0, index);
                dir = new File("classout", dirName);
            } else {
                dir = new File("classout");
            }

            dir.mkdirs();
            File f = new File(dir, klassName.substring(index + 1) + ".class");
            Object obj = f.createNewFile();

            os = new BufferedOutputStream(new FileOutputStream(f));
            ClassWriter cw = new ClassWriter(instanceKlass, (OutputStream)os);
            cw.write();
        } catch (Exception | InternalError e) {
            //捕获throw InternalError，不让异常退出
            e.printStackTrace();
        }
    }

    @Override
    public boolean canInclude(InstanceKlass instanceKlass) {

        boolean dumpFlag = false;
        String klassName = instanceKlass.getName().asString();
        String superClass = "";
        Klass superClassObj = instanceKlass.getSuper();
        if (superClassObj!=null) {
            superClass = superClassObj.getName().asString();
        }

        List interfaceList = instanceKlass.getDirectImplementedInterfaces();

        //import 列表
        U1Array tags = instanceKlass.getConstants().getTags();
        long len = (long)tags.length();
        int ci;
        for(ci = 1; (long)ci < len; ++ci) {
            int cpConstType = tags.at(ci);
            if (cpConstType == 1) {
                Symbol sym = instanceKlass.getConstants().getSymbolAt((long)ci);
                String classStr = sym.asString();

                for (String k : new String[]{
                        "javax/servlet/http/HttpServletRequest", //tomcat
                        "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping", //spring 请求的path
                }) {
                    if (classStr.contains(k)) {
                        dumpFlag = true;
                        break;
                    }
                }
            } else if (cpConstType == 5 || cpConstType == 6) {
                ++ci;
            }
        }

        // 父类
        for (String k : new String[]{
                "org.springframework.web.servlet.handler.HandlerInterceptorAdapter"
        }) {
            if (superClass.contains(k)) {
                dumpFlag = true;
                break;
            }
        }

        // 接口
        for (Object inerfaceKlass:interfaceList){
            for (String key:new String[]{
                    "javax/servlet/Filter",
                    "javax/servlet/ServletRequestListener",
                    "javax/servlet/Servlet"
            }){
                if(((InstanceKlass) inerfaceKlass).getName().asString().contains(key)){
                    dumpFlag = true;
                }
            }
        }

        //黑特征的类
        for (String k : new String[]{
                "shell",
                "memshell",
                "agentshell",
                "exploit",
                "payload",
                "rebeyond",
                "metasploit",
                "javax/servlet/http/HttpServlet",
                "org/apache/catalina/core/ApplicationFilterChain",
                "org/springframework/web/servlet/DispatcherServlet",
                "org/apache/tomcat/websocket/server/WsFilter",
                "weblogic.servlet.internal.ServletStubImpl",
        }) {
            if (klassName.contains(k)) {
                dumpFlag = true;
                break;
            }
        }

        if(dumpFlag){
            dumpClass(instanceKlass);
        }

        return false;
    }
}
