package com.nirima.snowglobe.core

import org.codehaus.groovy.runtime.MethodClosure

/**
 * Created by magnayn on 04/09/2016.
 */
public class SnowGlobeSystem {

    Script dslScript;

    SnowGlobe globe;

    IoCapture c = new IoCapture();

    public SnowGlobeSystem() {

    }

    /*
    void parseScript(File dsl) {
        dslScript = new GroovyShell().parse(dsl.text);


        dslScript.metaClass = createEMC(dslScript.class,
                                        {
                                            ExpandoMetaClass emc ->

                                                emc.snowglobe = {
                                                    Closure cl ->
                                                        globe = new SnowGlobe(cl);


                                                }

                                                emc.snowglobeData = {
                                                    Closure cl ->
                                                        globe = new SnowGlobe(cl);
                                                }

                                        })


    }
    */

    /**
     * Function JUST for parsing a state file so we can output it
     * in different formats (e.g JSON)
     * @param baseUrl
     * @param dsl
     */
    SnowGlobeSimpleReader parseStateOnly(InputStream dsl ) {
        Binding binding = new Binding();

        binding.setProperty("out", c.io.out);
        binding.setProperty("print", new MethodClosure(c, "print") );
        binding.setProperty("println", new MethodClosure(c, "println") );


        GroovyShell shell = new GroovyShell(binding);

        SnowGlobeSimpleReader sgl;

        dslScript = shell.parse(dsl.text);


        dslScript.metaClass = createEMC(dslScript.class,
                                        {
                                            ExpandoMetaClass emc ->

                                                emc.snowglobe = {
                                                    Closure cl ->
                                                        sgl = new SnowGlobeSimpleReader( cl);


                                                }

                                        })

        dslScript.run();
        return sgl;
    }




    void parseScript(URL baseUrl, InputStream dsl) {
        Binding binding = new Binding();

        binding.setProperty("out", c.io.out);
        binding.setProperty("print", new MethodClosure(c, "print") );
        binding.setProperty("println", new MethodClosure(c, "println") );


        GroovyShell shell = new GroovyShell(binding);



        dslScript = shell.parse(dsl.text);


        dslScript.metaClass = createEMC(dslScript.class,
                                        {
                                            ExpandoMetaClass emc ->

                                                emc.snowglobe = {
                                                    Closure cl ->
                                                        globe = new SnowGlobe(baseUrl, cl);


                                                }

                                                emc.snowglobeData = {
                                                    Closure cl ->
                                                        globe = new SnowGlobe(baseUrl, cl);
                                                }

                                        })


    }

    SnowGlobe runScript() {

        dslScript.run();
        return globe;
    }

    static ExpandoMetaClass createEMC(Class scriptClass, Closure cl) {
        ExpandoMetaClass emc = new ExpandoMetaClass(scriptClass, false);
        cl(emc)
        emc.initialize()
        return emc
    }

    static SnowGlobeContext getState(SnowGlobe x) {
        SnowGlobeContext sgc = new SnowGlobeContext(x);

        sgc.build();

        return sgc;
    }
}
