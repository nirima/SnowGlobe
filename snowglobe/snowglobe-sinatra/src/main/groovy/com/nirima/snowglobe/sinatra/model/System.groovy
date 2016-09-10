package com.nirima.snowglobe.sinatra.model

public class SinatraSystem {

    Script dslScript;

    Sinatra sinatra;

    public SinatraSystem() {

    }


    void parseScript(URL baseUrl, InputStream dsl) {
        dslScript = new GroovyShell().parse(dsl.text);

        dslScript.metaClass = createEMC(dslScript.class,
                                        {
                                            ExpandoMetaClass emc ->

                                                emc.sinatra = {
                                                    Closure cl ->
                                                        sinatra = new Sinatra(baseUrl, cl);
                                                        cl.delegate = sinatra;
                                                        cl.resolveStrategy = Closure.DELEGATE_FIRST

                                                        cl()
                                                }

                                        })


    }

    Sinatra runScript() {

        dslScript.run();
        return sinatra;
    }

    static ExpandoMetaClass createEMC(Class scriptClass, Closure cl) {
        ExpandoMetaClass emc = new ExpandoMetaClass(scriptClass, false);
        cl(emc)
        emc.initialize()
        return emc
    }

   
}
