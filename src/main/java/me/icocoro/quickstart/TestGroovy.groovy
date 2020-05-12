package me.icocoro.quickstart

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * http://docs.groovy-lang.org/latest/html/documentation/#integ-groovyshell
 */
class TestGroovy {

    static void main(String[] args) {
        InvokerHelper.runScript(TestScript, args)
        int x = 1
        int y = 2
        // Exception in thread "main" Assertion failed:
//        assert x + y == 5

        assert x + y == 3

        def shell = new GroovyShell()
        def result = shell.evaluate '3 == 5'

        println result

        def result2 = shell.evaluate(new StringReader('3*5'))
        assert result == result2
        def script = shell.parse '3*5'
        assert script instanceof groovy.lang.Script
        assert script.run() == 15

        def sharedData = new Binding()
        shell = new GroovyShell(sharedData)
        def now = new Date()
        sharedData.setProperty('text', 'I am shared data!')
        sharedData.setProperty('date', now)

        result = shell.evaluate('"At $date, $text"')

        assert result == "At $now, I am shared data!"
    }
}

class TestScript extends Script {
    int power(int n) { 2**n }

    def run() {
        println 'Hello'
        println "2^6==${power(6)}"
    }
}