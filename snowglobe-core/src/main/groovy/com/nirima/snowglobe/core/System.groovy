package com.nirima.snowglobe.core
/**
 * Created by magnayn on 04/09/2016.
 */
public class SnowGlobeSystem {

    Script dslScript;

    SnowGlobe globe;

    public SnowGlobeSystem() {

    }

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

    void parseScript(InputStream dsl) {
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
