package com.nirima.snowglobe

import com.fasterxml.jackson.databind.JsonNode
import com.moandjiezana.toml.Toml
import com.nirima.snowglobe.core.*

import com.nirima.snowglobe.graph.Graph
import com.nirima.snowglobe.graph.GraphBuilder
import com.nirima.snowglobe.plan.Plan
import com.nirima.snowglobe.plan.PlanBuilder
import com.nirima.snowglobe.plan.PlanType
import com.nirima.snowglobe.repository.IRepositoryItem
import org.codehaus.groovy.runtime.InvokerHelper

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.security.AccessController
import java.security.PrivilegedAction

/**
 * Parameters : some key-value pairs that can get used in the execution
 */
public class SGParameters {

    Toml data;
    private Map _itemMap = [:];

    public SGTags tags;

    SGParameters() {}

    SGParameters(Properties properties) {
        this._itemMap = properties;
    }

    def propertyMissing(String name) {
        if( _itemMap.containsKey(name))
            return _itemMap.get(name);

        return System.getProperty(name);
    }

    def propertyMissing(String name, value) { _itemMap[name] = value }

    def set(String name, value) {

        _set(_itemMap, name, value);
    }

    private static _set(Map itemMap, String name, value) {
        if(!name.contains(".") ) {
            itemMap[name] = value;

        } else {
            def idx = name.indexOf('.');
            String thisName = name.substring(0,idx);
            String nextName = name.substring(idx+1);
            if(!itemMap.containsKey(thisName)) {
                itemMap[thisName] = new HashMap();
            }
            _set((Map)itemMap[thisName],nextName,value);
        }


    }

    def load(InputStream inputStream) {

        data = new Toml().read(inputStream)
        this._itemMap = data.toMap();
    }


    @Override
    public String toString() {
        String r = "[SGParameters]\n";
        r = r + _toString(_itemMap,"");
        return r;
    }

    private String _toString(Map m, String indent) {
        String r = "";
        for(String k : m.keySet()) {
            r = r + indent + k + "\t";
            if( m[k] instanceof Map ) {
                r = r + "\n"
                r = r + _toString((Map)m[k], indent + "\t");
            } else {
                r = r + m[k] + "\n";
            }
        }
        return r;
    }
}

/**
 * Access to the tags
 */
public class SGTags {
    private final IRepositoryItem globeProcessor;
    Set<String> tags;

    SGTags(IRepositoryItem globeProcessor) {
        this.globeProcessor = globeProcessor;
        this.tags = globeProcessor.getTags();
    }

    public boolean contains(String name) {
        if( tags == null )
            return false;

        return tags.contains(name);
    }

    public void add(String tag) {
        this.tags.add(tag);
        globeProcessor.setTags(tags);
    }

    public void remove(String tag) {
        this.tags.remove(tag);
        globeProcessor.setTags(tags);
    }
}


/**
 * Created by magnayn on 06/09/2016.
 * Snowglobe execution environment.
 */
class SGExec {
    URL baseUrl;

    InputStream planFile, stateFile;

    SnowGlobeSystem dsl = new SnowGlobeSystem();
    public SnowGlobe snowGlobe;
    public SnowGlobe stateGlobe;
    private Graph g,stateGraph;

    public SGParameters parameters;

    public SnowGlobeContext sgContext;

    public SGExec(File plan ) {
        this(plan, new SGParameters());
    }
    SGExec(
            File file, File file1 ) {
        this(file,file1,new SGParameters());
    }

    SGExec(
            InputStream file, InputStream state ) {
        this(file,state,new SGParameters());
    }
    
    public SGExec( File plan, SGParameters parameters) {
        baseUrl = plan.toURL();
        this.planFile = new FileInputStream(plan)
        this.parameters = parameters;
        init();
    }

    SGExec(
           File file, File file1, SGParameters parameters) {
        baseUrl = file.toURL();
        this.planFile = new FileInputStream(file)
        if( file1 != null && file1.exists())
            this.stateFile = new FileInputStream(file1);
        this.parameters = parameters;
        init();
    }

    SGExec(
            InputStream file, InputStream state, SGParameters parameters) {

        planFile = file;
        stateFile = state;
        this.parameters = parameters;


        init();
    }

    String save() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        SGWriter sw = new SGWriter(byteArrayOutputStream);
        sw.write(snowGlobe);
        byteArrayOutputStream.close();
        return new String(byteArrayOutputStream.toByteArray());
    }

    /**
     * Initialise the system.
     * The plan file creates a SnowGlobe configuration, with closures that may be
     * re-evaluated in dependency order.
     *
     * The state file is in the same format, and contains a persisted state of the system
     * as-of the last apply.
     */
    void init() {
        dsl.parseScript(baseUrl, planFile );
        snowGlobe = dsl.runScript();

        sgContext = new SnowGlobeContext(snowGlobe, parameters);
        sgContext.initModules();

        // Do we have a persisted state?
        if( stateFile != null ) {
             SnowGlobeSystem dslState = new SnowGlobeSystem();

             dslState.parseScript(null, stateFile);
             stateGlobe = dslState.runScript();

            if( stateGlobe != null ) {
                SnowGlobeContext stContext = new SnowGlobeContext(stateGlobe, parameters);

                stContext.initModules();

                // Don't init it.

                mergeState(snowGlobe, stateGlobe);
            }
        }

        sgContext.buildModules();

        g = new GraphBuilder().build(sgContext);
    }

    /**
     * If we have state defined on disk, we merge it into the Snowglobe tree
     * here.
     *
     * @param globe
     * @param state
     */
    void mergeState(SnowGlobe globe, SnowGlobe state) {
        assert globe != null
        assert state != null

        // Look at all the modules in the saved state
        state.modules.each { stateModule ->

            // Find the equivalent module
            Module globeModule = globe.getModule(stateModule.id);
            if( globeModule == null ) {
                // If it didn't exist, then just add a module based on the saved one
                globe.addModule( stateModule )
            } else {
                // it existed

                // Merge all the resources
                stateModule.resources.each { stateResource ->
                    Resource globeResource = globeModule.getResource(stateResource.getClass(), stateResource.id );


                    if( globeResource == null ) {
                        // There was no equivalent resource in our configuration (this will likely entail
                        // a delete).
                        globeModule.addResource( stateResource );
                        // Flip state to savedState
                        stateResource.savedState = stateResource.state;
                        stateResource.state = null;
                    } else {
                        // It existed - store the saved state within the resource.
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

    public String apply() {
        PlanBuilder pb = new PlanBuilder();

        Plan plan = pb.buildPlan(sgContext, PlanType.Apply);

        plan.execute();

        dsl.c.close();

        return dsl.c.toString();
    }

    public String destroy() {
        PlanBuilder pb = new PlanBuilder();

        Plan plan = pb.buildPlan(sgContext, PlanType.Destroy);

        plan.execute();

        dsl.c.close();

        return dsl.c.toString();

    }


    void setLogger(Object entry) {

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
            if( value.size() == 0 )
                return "[:]";

            String obj = "[";
            value.each {
                if( obj != "[" )  obj += ", ";

                obj = obj + "${stringObject(it.key)} : ${stringObject(it.value)}"
            }
            return obj + "]";
        }

        if( value instanceof List ) {
            String obj = "[";
            value.each {
                if( obj != "[" )  obj += ", ";

                obj = obj + "${stringObject(it)}"
            }

            return obj + "]";
        }

        if( value instanceof JsonNode ) {
            return "json(\"\"\"" + InvokerHelper.toString(value) + "\"\"\")"
        }

        return InvokerHelper.toString(value)
    }

    public String getProperty(property) {
        "${property.key} = ${getValue(property.value)}"
    }

    public String getValue(Object o) {
        if( o instanceof String || o instanceof GString) {
            // TODO : quoted string

            String theString = o.toString();
            // Multi-line, then use delims.
            if( theString.contains("\n") )
                return "\"\"\"" + theString + "\"\"\"";


            String qs = o.replace("\\", "\\\\")
                    .replace("\$", "\\\$")
                    .replace("\"", "\\\"");

            return "\"${qs}\"";
        }

        return "${o}";
    }
}



