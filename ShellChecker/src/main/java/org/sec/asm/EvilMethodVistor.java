package org.sec.asm;

import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvilMethodVistor extends MethodVisitor {
    public String fileName;
    public Map<String,List<String>> resultMap = new HashMap<String,List<String>>();
    public EvilMethodVistor(int api) {
        super(api);
    }
    public EvilMethodVistor(int api,String fileName,Map<String,List<String>> resultMap) {
        super(api);
        this.resultMap = resultMap;
        this.fileName = fileName;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        boolean evilFlag = false;
        String classMethod = owner+"."+name;
        String charaStr = "";


        for (String key:new String[]{
                "java/lang/ClassLoader.defineClass",
                "java/lang/Class.forName",
                "java/lang/reflect/Method.invoke",
                "java/lang/Class.newInstance",
                "java/lang/ProcessBuilder", //ProcessBuilder.start
                "java/lang/Runtime.exec",
                "sun/misc/BASE64Decoder",
                "javax/crypto/Cipher",
                "java/util/Base64",
                "java/io/OutputStream.write",

                "javax/servlet/http/HttpServletRequest",
                "org/springframework/web/servlet/mvc/method/annotation/RequestMappingHandlerMapping",
                "io/netty/handler/codec/http/HttpRequest"
        }){
            if (classMethod.contains(key)){
                evilFlag = true;
                charaStr = key;
            }
        }
        if (evilFlag){
//            this.resultLis.add("检测到恶意方法："+classMethod);
            List<String> evilMethodList =  this.resultMap.get(this.fileName);
            if(evilMethodList==null){
                evilMethodList = new ArrayList<String>();
                evilMethodList.add(charaStr);
                this.resultMap.put(this.fileName,evilMethodList);
            }
            else if (!evilMethodList.contains(charaStr)){
                evilMethodList.add(charaStr);
            }
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }




}
