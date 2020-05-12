package me.icocoro.quickstart;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GroovyParseExpression {

    public static void main(String[] args) {
        CompilerConfiguration cfg = new CompilerConfiguration();
        cfg.setScriptBaseClass(BasicScript.class.getName());

        GroovyShell shell = new GroovyShell(cfg);
        Script script = shell.parse("5 > 9");
        System.out.println(script.run());
        script = shell.parse("1 + 1");
        System.out.println(script.run());
        script = shell.parse("(6 > 3 && 9 > 5)");
        System.out.println(script.run());
        Binding biding = new Binding();
        // suc_rate < x || total_num < y
        Double suc_rate = 0.9;
        Long total_num = 200L;
//        biding.setProperty("suc_rate", suc_rate);
//        biding.setProperty("total_num", total_num);
        Map<String, Object> map = new HashMap<>();
        map.put("suc_rate", suc_rate);
        map.put("total_num", total_num);
        biding = new Binding(map);
        script = shell.parse("(suc_rate < 0.95 || total_num < 180)");
        script.setBinding(biding);
        System.out.println(script.run());
        System.out.println(shell.parse("2**6==power(6)").run());
    }

    /**
     * 自定义表达式方法
     */
    public static class BasicScript extends Script {

        @Override
        public Object run() {
            Method[] methods = BasicScript.class.getDeclaredMethods();
            StringBuilder sb = new StringBuilder();
            for (Method method : methods) {
                sb.append(method);
            }
            return sb.substring(0, sb.length() - 1);
        }

        int power(int n) {
            return 2 ^ n;
        }
    }
}
