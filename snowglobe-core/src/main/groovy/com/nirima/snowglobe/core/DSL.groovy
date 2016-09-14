package com.nirima.snowglobe.core

import com.nirima.snowglobe.plan.PlanAction
import groovy.util.logging.Slf4j
import org.reflections.Reflections

import java.lang.reflect.ParameterizedType

@Slf4j
public class Core {
    Map<String, Class> classesMap;

    public static Core INSTANCE  = new Core();


    private Core() {

    }
    public void init() {

        log.info "Initializing class map"
        classesMap = [:];

        Reflections reflections = new Reflections("com.nirima.snowglobe");

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(SGItem.class);

        annotated.each {

            it -> try {
                String name = it.getAnnotation(SGItem.class).value();
                log.info "Seen ${name}"

                classesMap.put(name, it)
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    public void register(Class c, String name) {
        if( classesMap == null )
            init();
        classesMap[name] = c;
    }

    public Class getClassForName(String name) {
        if( classesMap == null )
            init();

        Class aClass =  classesMap.get(name);
        if( aClass == null ) {
            log.debug "I don't know what class ${name} is for";

        }

        return aClass;
    }

    public String getNameForClass(Class klass) {
        if( classesMap == null )
            init();

        String name = null;

        classesMap.each {
            if( it.value == klass )
                name = it.key;
        }

        return name;
    }

    public void dump() {
        classesMap.each {
            println "Defined ${it.key} as ${it.value}"
        }
    }
}

public class SnowGlobe {

    private transient Closure closure;

    public List<Module> modules = [];

    SnowGlobe(Closure closure) {
        this.closure = closure
    }

    public Module module(String id, Closure clos) {
        Module newModule = new Module(this, id, clos);
        modules << newModule;
        return newModule;
    }

    public void accept(Object context) {

        closure.delegate = context;
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()
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
        assert(c != null)
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
    public  String id;
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

    def methodMissing(String name, def args) {
        Class klass = Core.INSTANCE.getClassForName(name);

        Object[] items = (Object)args;

        // Items is either closure or ID, Closure
        // We want ID, Closure
        Object[] items2 = new Object[3];
        if(items.length == 1)
            items2[2] = items[0];
        else if(items.length == 2) {
            items2[1] = items[0];
            items2[2] = items[1];
        }

        items2[0] = this;
        if( klass == null ) {
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
        resources.find {  klass.isAssignableFrom( it.getClass() ) && it.id == id }
    }
}

@Slf4j
public class State {
    public Closure closure;

    public State(Closure closure) {
        this.closure = closure;
    }

    public void accept(Object context) {
        log.debug "State Accept in ${this} CTX= ${context} ";

        assert(context != null)

        closure.delegate = context;
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()

        Closure defaults = getDefaults();
        defaults.delegate = context;
        defaults.resolveStrategy = Closure.DELEGATE_FIRST
        defaults();

    }

    public Closure getDefaults() {
        return {}
    }
}

public class ResourceState extends State {

    Resource resource;
    public Object provider;

    ResourceState(Resource parent, Closure closure) {
        super(closure);
    }

    @Override
    void accept(Object context) {
        super.accept(context)
        if( provider == null )
            throw new IllegalStateException("Provider missing for ${this}")
    }

    public Provider getProvider() {
        if( provider == null )
            return null;
        if( provider instanceof Context ) {
            return provider.getProxy();
        }
        return provider;
    }
}

@Slf4j
abstract public class Resource<T extends ResourceState>
{
    Module module;
    public String id;


    T state;
    T savedState;

    Resource(Module module, String id, Closure closure) {
        assert(module != null)
        assert(id != null)

        this.module = module;
        this.id = id

        //TODO: Not here?
        state = newState(closure);

    }

    T newState(Closure closure) {
        return ((Class) ((ParameterizedType) this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0]).newInstance(this,closure);
    }
    // TODO: I'd like the SGResource to not be aware of Context
    public Provider getProvider() {
        if( state.provider == null )
            return null;
        if( state.provider instanceof Context ) {
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

        assert(context != null)

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
    private Module module;
    String id;
    public transient Closure closure;

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

public class DataSource<T extends DataSourceState> extends Provider
{

    T state;

    DataSource(Module module, String id, Closure closure) {
        super(module, id, closure)

        // TODO: Not here?
        state = newState(closure);
    }

    T newState(Closure closure) {
        return ((Class) ((ParameterizedType) this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0]).newInstance(this,closure);
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
        if( provider == null )
            throw new IllegalStateException("Provider missing for ${this}")
    }

    public Provider getProvider() {
        if( provider == null )
            return null;
        if( provider instanceof Context ) {
            return provider.getProxy();
        }
        return provider;
    }
}