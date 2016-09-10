package com.nirima.snowglobe

import com.nirima.snowglobe.core.Context
import com.nirima.snowglobe.core.Core
import com.nirima.snowglobe.core.Module
import com.nirima.snowglobe.core.Provider
import com.nirima.snowglobe.core.Resource
import com.nirima.snowglobe.core.SnowGlobe
import com.nirima.snowglobe.core.SnowGlobeContext
import com.nirima.snowglobe.core.SnowGlobeSystem
import com.nirima.snowglobe.core.State
import com.nirima.snowglobe.graph.Graph
import com.nirima.snowglobe.graph.GraphBuilder
import com.nirima.snowglobe.plan.Plan
import com.nirima.snowglobe.plan.PlanBuilder
import com.nirima.snowglobe.plan.PlanType
import com.nirima.snowglobe.state.*
import org.codehaus.groovy.runtime.InvokerHelper

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.security.AccessController
import java.security.PrivilegedAction

/**
 * Created by magnayn on 06/09/2016.
 */
class SGExec {

    InputStream planFile, stateFile;

    SnowGlobeSystem dsl = new SnowGlobeSystem();
    public SnowGlobe snowGlobe;
    public SnowGlobe stateGlobe;
    private Graph g,stateGraph;

    private SnowGlobeContext sgContext;

    public SGExec( File plan) {

        this.planFile = new FileInputStream(plan)

        init();
    }

    SGExec(
           File file, File file1) {

        this.planFile = new FileInputStream(file)
        if( file1 != null && file1.exists())
            this.stateFile = new FileInputStream(file1);
        init();
    }

    SGExec(
            InputStream file, InputStream state) {

        planFile = file;
        stateFile = state;


        init();
    }

    String save() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        SGWriter sw = new SGWriter(byteArrayOutputStream);
        sw.write(snowGlobe);
        byteArrayOutputStream.close();
        return new String(byteArrayOutputStream.toByteArray());
    }

    void init() {
        dsl.parseScript( planFile );
        snowGlobe = dsl.runScript();

        sgContext = new SnowGlobeContext(snowGlobe);
        sgContext.initModules();


        if( stateFile != null ) {
             dsl.parseScript(stateFile);
             stateGlobe = dsl.runScript();

            //SnowGlobeContext sctxt = dsl.getState(stateGlobe);
            SnowGlobeContext stContext = new SnowGlobeContext(stateGlobe);

            stContext.initModules();

            // Don't init it.

            mergeState(snowGlobe, stateGlobe);

        }



        sgContext.buildModules();

        g = new GraphBuilder().build(sgContext);
    }

    void mergeState(SnowGlobe globe, SnowGlobe state) {
        assert globe != null
        assert state != null

        state.modules.each { stateModule ->

            Module globeModule = globe.getModule(stateModule.id);
            if( globeModule == null ) {
                globe.addModule( stateModule )
            } else {
                // it existed

                stateModule.resources.each { stateResource ->
                    Resource globeResource = globeModule.getResource(stateResource.getClass(), stateResource.id );

                    if( globeResource == null ) {
                        globeModule.addResource( stateResource );
                        stateResource.savedState = stateResource.state;
                        stateResource.state = null;
                    } else {
                        // It existed
                        globeResource.savedState = stateResource.state;
                        globeResource.savedState.resource = globeResource;
                    }

                }

            }

        }


    }

    public void graph(OutputStream os) {
        String out = new GraphBuilder().graphViz(g);
        os.write(out.getBytes());
    }

    public void apply() {
        PlanBuilder pb = new PlanBuilder();

        Plan plan = pb.buildPlan(sgContext, PlanType.Apply);

        plan.execute();


    }

    public void destroy() {
        PlanBuilder pb = new PlanBuilder();

        Plan plan = pb.buildPlan(sgContext, PlanType.Destroy);

        plan.execute();


    }


}

public class SGWriter {

    OutputStream os;

    public SGWriter(OutputStream os) {
        this.os = os;
    }

    public void write(SnowGlobe sg) {
        os << "snowglobe {\n"

        sg.modules.each {
            module ->
                os << "   module(\"${module.id}\") { \n";


                module.resources.each { resource ->


                    write(resource);

                }

                os << "   }\n";
        }

        os << "}\n"
    }

    public void write(Resource resource) {

        State self = resource.savedState;
        if( self != null ) {
            String name = Core.INSTANCE.getNameForClass(resource.getClass());
            os << "     ${name}(\"${resource.id}\") {\n"
            os << writeObject(self);
            os << "     }\n";
        }
    }
        public String writeObject(Object self) {

            String os = "";

        Class klass = self.getClass();
        boolean groovyObject = self instanceof GroovyObject;

        while (klass != null && klass != State.class) {
            for (final Field field : klass.getDeclaredFields()) {
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    if (groovyObject && field.getName().equals("metaClass")) {
                        continue;
                    }
                    AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            field.setAccessible(true);
                            return null;
                        }
                    });

                    try {
                        Object value = field.get(self);
                        if( value instanceof Context )
                            value = ((Context)value).getProxy();

                        if( value instanceof List ) {

                            if( value.size() == 0 )
                                continue;
                            // Could introstpect the type maybe
                            Object item = value.get(0);
                            if( !item.getClass().isPrimitive() && !(item.getClass() == String.class || item instanceof GString) ) {

                                value.each {
                                    os += "          ${field.getName()} { \n";

                                    os += writeObject(it);

                                    os += "          }\n"
                                }


                                continue;
                            }

                        }


                        os += "          ${field.getName()} = ";


                        if( value instanceof Provider ) {
                            String name = Core.INSTANCE.getNameForClass( value.getClass() );
                            String id = value.id;
                            if( id == null )
                                os += " ${name}(null)";
                            else
                                os += " ${name}(\"${value.id}\")";
                        } else {
                            os += stringObject(value);


                        }
                        os += "\n";
                    } catch (Exception e) {

                    }

                }
            }

            klass = klass.getSuperclass();

        }

        return os;

    }

    public String stringObject(Object value) {
        if( value == null )
            return "null";

        if( value instanceof String || value instanceof GString ) {
            return getValue(value);
        }
        if( value instanceof Map ) {
            String obj = null;
            value.each {
                if( obj == null ) obj = "[" else obj += ", ";

                obj = obj + "${stringObject(it.key)} : ${stringObject(it.value)}"
            }
            return obj + "]";
        }

        if( value instanceof List ) {
            String obj = null;
            value.each {
                if( obj == null ) obj = "[" else obj += ", ";

                obj = obj + "${stringObject(it)}"
            }
            return obj + "]";
        }

        return InvokerHelper.toString(value)
    }

    public String getProperty(property) {
        "${property.key} = ${getValue(property.value)}"
    }

    public String getValue(Object o) {
        if( o instanceof String || o instanceof GString) {
            // TODO : quoted string

            String qs = o.replace("\\", "\\\\")
                    .replace("\$", "\\\$")
                    .replace("\"", "\\\"");

            return "\"${qs}\"";
        }

        return "${o}";
    }
}