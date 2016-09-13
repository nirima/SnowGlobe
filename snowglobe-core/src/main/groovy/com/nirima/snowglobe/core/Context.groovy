package com.nirima.snowglobe.core
import groovy.util.logging.Slf4j
/**
 * Created by magnayn on 04/09/2016.
 */
abstract class Context<T> {
    private T proxy;
    Context(T t) {
        proxy = t;
    }

    public T getProxy() { return proxy; }


    public String toString() {
        return "Context<${getProxy()}>";
    }
}

public class Dependency {
    Context from;
    Context to;

    public Dependency(Context from, Context to) {
        assert(from != null);
        assert(to != null);
        assert(from != to);

        this.from = from
        this.to = to
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        Dependency that = (Dependency) o

        if (from != that.from) {
            return false
        }
        if (to != that.to) {
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = (from != null ? from.hashCode() : 0)
        result = 31 * result + (to != null ? to.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
Dependency{
    from=$from,
    to=$to
}"""
    }
}

@Slf4j
public class SnowGlobeContext extends Context<SnowGlobe> {

    List<ModuleContext> modules = [];
    Set<Dependency> dependencies = [];

    Set<ModuleContext> processedModules = [];
    Set<ModuleContext> inProcessModules = [];


    SnowGlobeContext(SnowGlobe proxy) {
        super(proxy);
    }

    def invokeMethod(String name, Object args) {
        Object result = getProxy().invokeMethod(name, args);
        if (result instanceof Module) {
            ModuleContext child = new ModuleContext(result, this);
            modules << child;
            result.accept(child)
        }
        return result;
    }

    public void build() {
        // Initial visit goes down as far as the modules
        initModules();

        buildModules();
    }

    public void buildModules() {
        log.trace 'Build Modules';
        modules.each {
            processModule(it);
        }
    }

    public void initModules() {
        modules = [];
        dependencies = [];
        processedModules = [];
        inProcessModules = [];
        log.trace "Visit Modules"
        getProxy().accept(this);
    }

    private void processModule(ModuleContext context) {
        log.debug "Build Module ${context}"

        if( processedModules.contains(context))
            return;

        if( inProcessModules.contains(context) )
            throw new IllegalStateException("Circular dependency on ${context}");

        inProcessModules.add(context);
        context.build();
        inProcessModules.remove(context);

        processedModules.add(context);
        log.debug "Built Module ${context}"
    }

    public Context<?> getElement(ModuleContext from, Class klass, String id) {
        ModuleContext result = modules.
                find {  klass.isAssignableFrom( it.getProxy().getClass() ) && it.getProxy().id == id }
        if (result == null)
            throw IllegalStateException("Cannot find resource ${id} type ${klass}");

        dependencies << new Dependency(from, result);

        //
        if( !processedModules.contains(result) )
            processModule(result);

        return result;
    }

    Context<?> getContextFor(Object o) {
       Context<?> result = modules.collect {
           it.getContextFor(o)
       }.find {
               it != null }
       return result;
    }
}


@Slf4j
public class ProviderContext extends Context<Provider> {
    ModuleContext parent;

    ProviderContext(Provider proxy, ModuleContext parent) {
        super(proxy);
        this.parent = parent
    }

    def getProperty(String name) {
        try {
            Provider resource = getProxy();
            return resource.getProperty(name);
        } catch(Exception ex) {
            log.error "State for resource " + getProxy() + " does not have property " + name;
            throw ex;
        }
    }

    void setProperty(String name, Object value) {

        log.debug "     set ${name}=${value} on ${this}"

        try {
            getProxy().setProperty(name,value);
        }
        catch( Exception ex ) {
            log.error ("Error setting state for resource ${getProxy()} property ${name} with value ${value}");
            println ex;
            throw ex;
        }



    }

}


public class ModuleReferenceContext
        //extends Context<SGModule>
{
    ModuleContext referenceFrom, referenceTo;

    ModuleReferenceContext(ModuleContext from, ModuleContext to) {
        //super(from.getProxy());

        assert(from != null);
        assert(to!=null);

        this.referenceFrom = from;
        this.referenceTo = to;
    }
    def invokeMethod(String name, Object args) {
        Object[] theArgs = (Object[]) args;

        Class c = Core.INSTANCE.getClassForName(name);

        // We can use provider("name") or provider() or provider(null)
        String id = null;
        if( theArgs.length > 0 )
            id = theArgs[0];

        referenceTo.getElement(referenceFrom,c,(String)id)
    }
}

public class ModuleImportContext extends Context<ModuleImports> {
    ModuleContext parent;

    ModuleImportContext( ModuleImports moduleImports,ModuleContext parent) {
        super(moduleImports)
        this.parent = parent;
    }

    // Within a resource this is used to refer to another resource
    def invokeMethod(String name, Object args) {
        Object[] theArgs = (Object[])args;

        if( name == "module") {
            // Refers to another module
            return parent.getElement(this, Module.class, (String)theArgs[0]);
        }

        Class c = Core.INSTANCE.getClassForName(name);

        if( c == null ) {
            return getProxy().invokeMethod(name, args);
        }

        String id = null;
        if( ((Object[])args).length > 0 )
            id =  ((Object[])args)[0];

        return  parent.getElement(this, c, id);
    }
}

@Slf4j
public class ModuleContext extends Context<Module>{
    SnowGlobeContext parent;

    List<ModuleImportContext> imports = [];
    List<ResourceContext> resources = null;
    List<ProviderContext> providers = [];
    List<DataSourceContext> dataSources = [];

    Set<ResourceContext> processedResources = [];
    Set<ResourceContext> inProcessResources = [];

    Set<Dependency> dependencies = [];

    ModuleContext(Module proxy, SnowGlobeContext parent) {
        super(proxy);
        this.parent = parent;
    }

    Context<?> getContextFor(Object o) {
        if( o == proxy )
            return this;

        Context<?> result = resources.find { it.getContextFor(o) != null }


        return result;
    }

    Object invokeMethod(String name, Object args) {
        Object result = proxy.invokeMethod(name,args);
        if( result instanceof Resource ) {
            getProxy().addResource(result);

//            ResourceContext child = new ResourceContext(result, this);
//            resources << child;


            // result.accept( child ) DO NOT ACCEPT THIS YET
        }
        else if( result instanceof DataSource ) {
            DataSourceContext child = new DataSourceContext(result, this);
            dataSources << child;
        }
        else if( result instanceof Provider ) {
            ProviderContext child = new ProviderContext(result, this);
            providers << child;
        } else if( result instanceof ModuleImports ) {
            ModuleImportContext child = new ModuleImportContext(result, this);
            imports << child;
        }
        return result;
    }

    public void build() {
        imports.each() {
            it.getProxy().accept(it);
        }

        providers.each() {
            it.getProxy().accept(it)
        }

        dataSources.each() {
            it.build()
        }

        getResources().each() {
            processResource(it);
        }
    }

    public List<ResourceContext> getResources() {
        if( resources == null ) {
            resources = [];

            getProxy().resources.each() {
                ResourceContext child = new ResourceContext(it, this);
                resources << child;
            }
        }
        return resources;
    }

    private void processResource(ResourceContext context) {
        if( processedResources.contains(context))
            return;

        if( inProcessResources.contains(context) )
            throw new IllegalStateException("Circular dependency on ${context}");

        inProcessResources.add(context);


        context.build();




        inProcessResources.remove(context);

        processedResources.add(context);
    }

    public Context findImport(Class klass, String id) {
        for(ModuleImportContext mic : imports) {
            Object ctx = mic.getProxy().references.find {
                klass.isAssignableFrom( it.getProxy().getClass() ) && it.getProxy().id == id
            }
            if( ctx !=null )
                return (Context)ctx;
        }
        return null;
    }

    public Object getElement(Context<?> from, Class klass, String id) {

        log.debug("ModuleContext.getElement ${from} wants {$klass} of id ${id}");

        if(klass == Module.class ) {
            // Need to ask the parent for a module.
            Object context = parent.getElement(this, klass, id);

            return new ModuleReferenceContext(this, context);
        }

        ProviderContext provider = providers.find { klass.isAssignableFrom( it.getProxy().getClass() ) && it.getProxy().id == id }
        if( provider != null ) {
            dependencies << new Dependency(from, provider);
            return provider;
        }

        DataSourceContext dataSource = dataSources.find {  klass.isAssignableFrom( it.getProxy().getClass() ) && it.getProxy().id == id }
        if( dataSource != null ) {
            dependencies << new Dependency(from, dataSource);
            return dataSource;
        }

        ResourceContext result = resources.find {  klass.isAssignableFrom( it.getProxy().getClass() ) && it.getProxy().id == id }
        if( result == null ) {
            // Perhaps it was imported in the imports {} section

            Context ctx = findImport(klass, id);
            if( ctx != null )
                return ctx;
        }

        if( result == null )
            throw new IllegalStateException("Cannot find resource ${id} type ${klass}");

        dependencies << new Dependency(from, result);
        //
        if( !processedResources.contains(result) )
            processResource(result);

        return result;
    }

}

@Slf4j
public class StateContext {

    public Context parent;
    public ModuleContext moduleContext;

    // Actual object
    private State proxy;

    StateContext(
            State state, Context context, ModuleContext moduleContext) {
        assert context     != null
        this.parent        = context;
        this.moduleContext = moduleContext;
        this.proxy         = state;
    }

    def invokeMethod(String name, Object args) {
        Object[] theArgs = (Object[]) args;

        if (name == "module") {
            // Refers to another module
            return moduleContext.getElement(parent, Module.class, (String) theArgs[0]);
        }

        Class c = Core.INSTANCE.getClassForName(name);

        if (c == null) {
            return getProxy().invokeMethod(name, args);
        }

        String id = null;
        if (((Object[]) args).length > 0)
            id = ((Object[]) args)[0];

        ModuleContext mc =  moduleContext;
        mc.getElement(parent, c, id);
    }


    State getProxy() {
        return proxy
    }
// To get a value
    def getProperty(String name) {

        log.trace "     get ${name}? on ${this}"

        try {
            State resource = getProxy();
            return resource.getProperty(name);
        } catch (Exception ex) {
            log.error "State for resource " + getProxy() + " does not have property " + name;
            throw ex;
        }
    }

    void setProperty(String name, Object value) {

        println "     set ${name}=${value} on ${this}"

        try {
            getProxy().setProperty(name, value);
        }
        catch (Exception ex) {
            log.error(
                    "Error setting state for resource ${getProxy()} property ${name} with value ${value}");
            println ex;
            throw ex;
        }

        println "     ±set ${name}=${value} on ${this}"

    }

    void build() {
        if( getProxy() != null )
            getProxy().accept(this)
    }


    @Override
    public String toString() {
        return """\
StateContext{
    parent=$parent
}"""
    }
}

@Slf4j
public class DataSourceContext extends Context<DataSource> {

    public ModuleContext moduleContext;
    public StateContext  stateContext;

    DataSourceContext(DataSource proxy, ModuleContext moduleContext) {
        super(proxy)
        this.moduleContext = moduleContext

        stateContext = new StateContext(proxy.state,this, moduleContext);
    }

    def build() {
        //   getProxy().accept(this)
        if (stateContext != null)
            stateContext.build()
    }

    def propertyMissing(String name) {
        try {
            DataSource resource = getProxy();

            try {
                return resource.getState().getProperty(name);
            } catch(MissingPropertyException e) {
                return resource.getProperty(name);
            }
        } catch(Exception ex) {
            log.error "State for resource " + getProxy() + " does not have property " + name;
            throw ex;
        }


    }

}

@Slf4j
public class ResourceContext extends Context<Resource> {

    public ModuleContext moduleContext;

    public StateContext  stateContext;
    public StateContext  savedStateContext;


    ResourceContext(Resource proxy, ModuleContext moduleContext) {
        super(proxy);
        this.moduleContext = moduleContext

        stateContext = new StateContext(proxy.state,this, moduleContext);
        if( proxy.savedState != null )
            savedStateContext = new StateContext(proxy.savedState,this, moduleContext);

    }

    Context<?> getContextFor(Object o) {
        if( o == proxy )
            return this;

        return null;
    }

    def propertyMissing(String name) {
        try {
            Resource resource = getProxy();
            return resource.getState().getProperty(name);
        } catch(Exception ex) {
            log.error "State for resource " + getProxy() + " does not have property " + name;
            throw ex;
        }
    }


    def build() {
     //   getProxy().accept(this)
        if( stateContext != null )
            stateContext.build()
        if( savedStateContext != null )
            savedStateContext.build()
    }
}