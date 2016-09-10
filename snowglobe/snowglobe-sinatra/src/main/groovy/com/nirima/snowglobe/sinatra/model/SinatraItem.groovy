package com.nirima.snowglobe.sinatra.model

import com.google.common.base.MoreObjects

/**
 * Created by magnayn on 27/04/2017.
 */
public class SinatraItem implements Serializable {
    public Sinatra sinatra;
    public SinatraItem parent;
    public String id;
    public String name;

    public SinatraItem(Sinatra s, SinatraItem parent, String id, Closure clos) {
        this.parent = parent;
        this.sinatra = s;
        this.id = id;
        this.sinatra.addItem(this);
        clos.delegate = this;
        clos.resolveStrategy = Closure.DELEGATE_FIRST
    }

    public SinatraItem(SinatraItem parent, String id, Closure clos) {
        this(parent.sinatra, parent, id, clos);
    }

    public SinatraItem(Sinatra s, String id, Closure clos) {
        this(s, null, id, clos);
    }

    public String getFullId() {
        String s = id;
        if( parent != null ) {
            s = parent.getFullId() + "." + s;
        }
        return s;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sinatra", sinatra)
                .add("id", id)
                .add("name", name)
                .toString();
    }
}

public class SinatraCollection extends SinatraItem {
    public List<SinatraItem> children = new ArrayList<>();

    public SinatraCollection(Sinatra s, SinatraItem parent, String id, Closure clos) {
        super(s, parent, id,clos);
    }

    SinatraCollection(SinatraItem parent, String id, Closure clos) {
        super(parent, id, clos)
    }

    SinatraCollection(Sinatra s, String id, Closure clos) {
        super(s, id, clos)
    }

    public List < SinatraItem > getChildren ( ) {
    return children ;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("children", children)
                .toString();
    }
}

public class Customer extends SinatraCollection {
    public Customer(Sinatra s, String id, Closure clos) {
        super(s,id,clos);

    }

    public Environment environment(String id, Closure clos) {
        println "Define environment ${id}";

        Environment newModule = new Environment(this, id, clos);
        children.add(newModule);
        clos();
        return newModule;
    }

    public List < Environment > getEnvironments ( ) {
    return this.children ;
    }


}

public class Environment extends SinatraCollection {

    public Service access_via;

    Environment(SinatraItem s, String id,
                Closure clos) {
        super(s, id, clos)
    }

    public Service getVia ( ) {
        return access_via;
    } 

    public Host host(String id, Closure closure) {
        Host h = new Host(this, id, closure);
        children.add(h);
        closure();
        return h;
    }



}

public class HostServices {
    Host h;

    public HostServices(Host hh) {
        this.h = hh;
    }

    public def methodMissing(String name, args) {                               
        // Intercept method that starts with find.
        Object[] items = (Object)args;

        Closure c = (Closure)items[0];

        if( name.equalsIgnoreCase("rdp") || name.equalsIgnoreCase("ssh") || name.equalsIgnoreCase("sql")|| name.equalsIgnoreCase("docker")|| name.equalsIgnoreCase("realtime")) {

            Service s = new Service(h, name, c);
            h.children.add(s);
            c.delegate = s;
            c();

            return s;
        }
        else
            throw new MissingMethodException(name, this.class, args);

    }
}


public class Host extends SinatraCollection {
    public String type;
    public String ip;
    public Service access_via;

    Host(SinatraItem s, String id,
         Closure clos) {
        super(s, id, clos)
    }

    public Service getVia ( ) {
        if( access_via == null )
            return ((Environment)parent).getVia();


        return access_via;
    }

    public Service service(String id, Closure closure) {
        Service s = new Service(this, id, closure);
        this.children.add(s);
        closure();
        return s;
    }

    public HostServices services(Closure closure) {
        HostServices hs = new HostServices(this);
        closure.delegate = hs;
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure();
        return hs;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("ip", ip)
                .toString();
    }
}

public class Service extends SinatraItem {
    public String type;
    public Credentials credentials;
    public Service access_via;
    public int port;
    
    Service(SinatraItem s, String id,
            Closure clos) {
        super(s, id, clos)
        if( id.equalsIgnoreCase("rdp"))
            port = 3389;
        if( id.equalsIgnoreCase("sql"))
            port = 1433;
        if( id.equalsIgnoreCase("docker"))
            port = 3456;
        if( id.equalsIgnoreCase("ssh"))
            port = 22;


    }

    public Credentials credentials(Closure clos) {
        Credentials c = new Credentials();
        clos.delegate = c;
        clos();
        return c;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .toString();
    }
    public Service getVia ( ) {
        if( access_via == null )
            return ((Host)parent).getVia();


        return access_via;
    } }

public class Credentials {
    public String username, password;


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .add("password", password)
                .toString();
    }
}