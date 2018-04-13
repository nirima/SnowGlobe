package com.nirima.snowglobe.core

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nirima.snowglobe.docker.DockerContainerState
import com.nirima.snowglobe.plan.PlanAction
import com.nirima.snowglobe.repository.IRepository
import com.nirima.snowglobe.repository.IRepositoryModule
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
import org.reflections.Reflections

import java.lang.reflect.ParameterizedType

@Slf4j
public class Core {

    Map<String, Class> classesMap;

    public static Core INSTANCE = new Core();


    private Core() {
                 
    }

    public void init() {

        log.debug "Initializing class map"
        classesMap = [:];

        Reflections reflections = new Reflections("com.nirima.snowglobe");

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(SGItem.class);

        annotated.each {

            it ->
                try {
                    String name = it.getAnnotation(SGItem.class).value();
                    log.debug "Seen ${name}"

                    classesMap.put(name, it)
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }


    }

    public void register(Class c, String name) {
        if (classesMap == null) {
            init()
        };
        classesMap[name] = c;
    }

    public Class getClassForName(String name) {
        if (classesMap == null) {
            init()
        };

        Class aClass = classesMap.get(name);
        if (aClass == null) {
            log.debug "I don't know what class ${name} is for";

        }

        return aClass;
    }

    public String getNameForClass(Class klass) {
        if (classesMap == null) {
            init()
        };

        String name = null;

        classesMap.each {
            if (it.value == klass) {
                name = it.key
            };
        }

        return name;
    }

    public void dump() {
        classesMap.each {
            println "Defined ${it.key} as ${it.value}"
        }
    }
}

public class DelegatedSnowGlobe {

    SnowGlobe snowGlobe;

    DelegatedSnowGlobe(SnowGlobe sg) {
        this.snowGlobe = sg;
    }

    public void moo() {
        println "moo";
    }

    def methodMissing(String name, def args) {
        snowGlobe.invokeMethod(name, args);
    }


}

@Slf4j
public class SnowGlobeSimpleReader {

    Map<String, SnowGlobeSimpleModule> modules = [:];

    public SnowGlobeSimpleReader(Closure closure) {
        closure.delegate = this;
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure();
    }

    public SnowGlobeSimpleModule module(String id, Closure clos) {
        log.debug "Define module ${id}";

        SnowGlobeSimpleModule newModule = new SnowGlobeSimpleModule(clos);
        modules[id] = newModule;
        return newModule;
    }

    int getResourceCount() {
        Collection<Integer> cx = modules.values().collect { it.resourceCount };
        Integer x = cx.sum(0);
        return x;
    }
}

public class SnowGlobeSimpleModule {

    Map<String, Object> resources = [:];


    public SnowGlobeSimpleModule(Closure closure) {
        closure.delegate = this;
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure();
    }

    int getResourceCount() {
        return resources.size();
    }

    /*
   * TODO: Not sure this should be here.
   */

    public JsonNode json(String sdata) {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.readTree(new ByteArrayInputStream(sdata.getBytes()));

        return rootNode;
    }

    def methodMissing(String name, args) {
        SnowGlobeSimpleResource resource = new SnowGlobeSimpleResource(args[1]);

        if (!resources.containsKey(name)) {
            resources[name] = new HashMap<String, Object>()
        };

        resources[name][args[0]] = resource;
    }
}

public class SnowGlobeSimpleResource {

    Map<String, Object> items = [:];


    public SnowGlobeSimpleResource(Closure closure) {
        if (closure == null) {
            return
        };

        closure.delegate = this;
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure();
    }

    public SnowGlobeSimpleResource(String name) {
        items.put("name", name);
    }

    def methodMissing(String name, args) {

        SnowGlobeSimpleResource resource;

        if (args.length == 1) {
            resource = new SnowGlobeSimpleResource(args[0]);
            items[name] = resource;

        } else {
            resource = new SnowGlobeSimpleResource(args[1]);
            items[name + "_" + args[0]] = resource;
        }


    }

    /*
    * TODO: Not sure this should be here.
    */

    public JsonNode json(String sdata) {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.readTree(new ByteArrayInputStream(sdata.getBytes()));

        return rootNode;
    }

    void setProperty(String property, Object value) {
        items[property] = value
    }
}

@Slf4j
public class SnowGlobe {

    private transient URL file;

    private transient Closure closure;

    public List<Module> modules = [];

    SnowGlobe(URL url, Closure closure) {
        this.file = url;
        this.closure = closure;
    }

    /*SnowGlobe(Closure closure) {
        this.closure = closure
    }*/


    public Module module(String id, Closure clos) {
        log.debug "Define module ${id}";

        Module newModule = new Module(this, id, clos);
        modules << newModule;
        return newModule;
    }

    public void accept(Object context) {

        closure.delegate = context;
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()
    }

    public void evaluate(Closure closure1) {
        closure1.delegate = closure.delegate;
        closure1.resolveStrategy = Closure.DELEGATE_FIRST;
        closure1()
    }

    /*
    * TODO: Not sure this should be here.
    */

    public JsonNode json(String sdata) {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.readTree(new ByteArrayInputStream(sdata.getBytes()));

        return rootNode;
    }

    public Script loadModule(String uri) {
        IRepository repo;

        IRepositoryModule module = repo.getModule(uri);

        String scriptData = module.getConfig(null);


        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setScriptBaseClass(DelegatingScript.class.getName());

        DelegatingScript dslScript = (DelegatingScript) new GroovyShell(getClass().getClassLoader(),
                                                                        new Binding(), cc).
        parse(scriptData);

        return dslScript;

    }

    public Script load(String relativePath) {

        File f = new File(file.getFile());
        def dsl = new File(f.getParentFile(), relativePath);


        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setScriptBaseClass(DelegatingScript.class.getName());

        DelegatingScript dslScript = (DelegatingScript) new GroovyShell(getClass().getClassLoader(),
                                                                        new Binding(), cc).
        parse(dsl.text);

        return dslScript;
    }

    Module getModule(String id) {
        modules.find { it.id == id }
    }

    def addModule(Module sgModule) {
        modules << sgModule;
        sgModule.parent = this;
    }
}

public class ModuleImports {

    Module module;
    transient Closure closure;

    List references = [];

    ModuleImports(Module module, Closure closure) {
        this.module = module
        this.closure = closure
    }

    public Object using(Object c) {
        assert (c != null)
        references << c;
    }

    public void accept(Object context) {

        closure.delegate = context;
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()
    }

    public String toString() {
        return "ModuleImports for ${module}";
    }
}

@Slf4j
public class Module {

    protected SnowGlobe parent;
    public String id;
    private transient Closure closure;

    public List<Resource> resources = [];

    public Module(SnowGlobe globe, String id, Closure clos) {
        this.parent = globe;
        this.id = id;
        this.closure = clos;
    }

    public void accept(Object context) {
        log.debug "Accept in ${this}";
        closure.delegate = context;
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()
    }

    public ModuleImports imports(Closure c) {
        return new ModuleImports(this, c);
    }

    /**
     * This is usually called in the initial evaluation phase, such as
     * snowglobe {*     module('foo') {*         myResource('foo') {*
     *}*}*}*
     * The route is that ModuleContext is accepted by Module, which invokes the module closure
     * using ModuleContext as the delegate. it's invokeMethod then calls back to this object,
     * which below is trying to evaluate the method 'myResource' (which is missing) in order to
     * create the _Resource_ object.
     *
     * @param name
     * @param args
     * @return
     */
    def methodMissing(String name, def args) {
        Class klass = Core.INSTANCE.getClassForName(name);

        Object[] items = (Object) args;

        // Items is either closure or ID, Closure
        // We want ID, Closure
        Object[] items2 = new Object[3];
        if (items.length == 1) {
            items2[2] = items[0]
        } else if (items.length == 2) {
            items2[1] = items[0];
            items2[2] = items[1];
        }

        items2[0] = this;
        if (klass == null) {
            //   throw new ClassNotFoundException("Missing ${name}");
            return parent.invokeMethod(name, args);
        }
        return klass.newInstance(items2);
    }


    @Override
    public String toString() {
        return "module('${id}')";
    }

    def addResource(Resource sgResource) {
        sgResource.module = this;
        resources << sgResource;
    }

    Resource getResource(Class klass, String id) {
        resources.find { klass.isAssignableFrom(it.getClass()) && it.id == id }
    }
}

@Slf4j
public class State implements Comparable {

    @JsonIgnore
    public Closure closure;

    public State(Closure closure) {
        this.closure = closure;
    }


    public void accept(Object context) {
        log.debug "State Accept in ${this} CTX= ${context} ";

        assert (context != null)
        try {
            closure.delegate = context;
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()

            Closure defaults = getDefaults();
            defaults.delegate = context;
            defaults.resolveStrategy = Closure.DELEGATE_FIRST
            defaults();
        }
        catch (MissingPropertyException ex) {
            log.error(
                    "Error evaluating in ${this} CTX= ${context}. Check that the property in the exception was not re-defined");
            throw new RuntimeException(
                    "Error evaluating in ${this} CTX= ${context}. Check that the property in the exception was not re-defined.",
                    ex);
        }
        catch (Exception ex) {
            log.error("Error evaluating in ${this} CTX= ${context}", ex);
            throw new RuntimeException("Error evaluating in ${this} CTX= ${context}", ex);
        }
    }



    int compareTo(Object o) {
        return ComparatorUtils.fieldwiseCompare(this, o);
    }

    /*
     * After the state is evaluated, getDefaults is
     * called to fill out any missing entries with default
     * or computed values. It should always take care to
     * not blat user-implemented values.
     */

    public Closure getDefaults() {
        return {}
    }
}

public class ResourceState extends State {

    @JsonIgnore
    Resource resource;
    @JsonIgnore
    public Object provider;

    ResourceState(Resource parent, Closure closure) {
        super(closure);
    }

    @Override
    void accept(Object context) {
        super.accept(context)
        if (provider == null) {
            throw new IllegalStateException("Provider missing for ${this}")
        }
    }

    @JsonIgnore
    public Provider getProvider() {
        if (provider == null) {
            return null
        };
        if (provider instanceof Context) {
            return provider.getProxy();
        }
        return provider;
    }
}

@Slf4j
abstract public class Resource<T extends ResourceState> {

    @JsonIgnore
    Module module;
    public String id;


    T state;
    T savedState;

    Resource(Module module, String id, Closure closure) {
        assert (module != null)
        assert (id != null)

        this.module = module;
        this.id = id

        //TODO: Not here?
        state = newState(closure);

    }

    T newState(Closure closure) {
        return ((Class) ((ParameterizedType) this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0]).newInstance(this, closure);
    }
    // TODO: I'd like the SGResource to not be aware of Context
    public Provider getProvider() {
        if (state.provider == null) {
            return null
        };
        if (state.provider instanceof Context) {
            return state.provider.getProxy();
        }
        return state.provider;
    }

    public PlanAction assess() {
        // Maybe to another class in config
        return null;
    }

    public void accept(Object context) {

        log.debug "Accept in ${this}";

        assert (context != null)

        // Maybe now there is nothing to do here?

        //state.accept(context);

        /*
        closure.delegate = context;
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()

        Closure defaults = getDefaults();
        defaults.delegate = context;
        defaults.resolveStrategy = Closure.DELEGATE_FIRST
        defaults();

        if( provider == null )
            throw new IllegalStateException("Provider missing for ${this}")
        */
    }

    //  abstract Closure getDefaults();

    @Override
    public String toString() {
        return "${getClass()}:${id}"
    }
}

@Slf4j
public class Provider {

    @JsonIgnore
    private Module module;
    String id;
    @JsonIgnore
    public transient Closure closure;

    @JsonIgnore
    public Object provider;

    Provider(Module module, String id, Closure closure) {
        this.module = module;
        this.id = id
        this.closure = closure
    }

    public void accept(Object context) {
        log.debug "Accept in ${this}";
        closure.delegate = context;
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()
    }


    @Override
    public String toString() {
        return "provider(${getClass()}):${id}"
    }
}

public abstract class DataSource<T extends DataSourceState> extends Provider {

    T state;

    DataSource(Module module, String id, Closure closure) {
        super(module, id, closure)

        // TODO: Not here?
        state = newState(closure);
    }

    T newState(Closure closure) {
        return ((Class) ((ParameterizedType) this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0]).newInstance(this, closure);
    }
}

public class DataSourceState extends State {

    public Object provider;
    DataSource parent;
    // Maybe this is a kind of resourceState? hmm.

    DataSourceState(DataSource parent, Closure closure) {
        super(closure)
        this.parent = parent;
    }


    @Override
    void accept(Object context) {
        super.accept(context)

        // Maybe this is allowable??
        if (provider == null) {
            throw new IllegalStateException("Provider missing for ${this}")
        }
    }

    public Provider getProvider() {
        if (provider == null) {
            return null
        };
        if (provider instanceof Context) {
            return provider.getProxy();
        }
        return provider;
    }
}