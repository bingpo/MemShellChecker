package org.sec.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckClassVistor extends ClassVisitor {
    public List<String> resultLis;
    public String curClassName;
    String fileName;
    public Map<String,List<String>> resultMap = new HashMap<String,List<String>>();

    public CheckClassVistor(int api) {
        super(api);
    }

    public CheckClassVistor(int api,String fileName, Map<String,List<String>> resultMap) {
        super(api);
        this.resultMap = resultMap;
        this.fileName = fileName;
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//        System.out.println(name);
//        System.out.println(superName);
//        System.out.println(interfaces);
        this.curClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//        return super.visitMethod(access, name, descriptor, signature, exceptions);
        return new EvilMethodVistor(this.api,this.fileName,this.resultMap);
    }



    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }
}
