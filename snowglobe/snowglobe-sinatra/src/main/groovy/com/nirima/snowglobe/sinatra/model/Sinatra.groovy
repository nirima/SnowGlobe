package com.nirima.snowglobe.sinatra.model

import com.google.common.base.MoreObjects

import java.util.stream.Stream

/**
 * Created by magnayn on 27/04/2017.
 */
public class Sinatra {
   Map<String, SinatraItem> items = new HashMap<>();;

    Sinatra(URL url, Closure closure) {

     
    }

    public Customer customer(String id, Closure clos) {
        println "Define customer ${id}";

        Customer newModule = new Customer(this, id, clos);

        clos();
        return newModule;
    }






    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("items", items)
                .toString();
    }

    def addItem(SinatraItem sinatraItem) {
        items.put(sinatraItem.fullId, sinatraItem);
    }

    public <T> Stream<T> findAll(Class<T> ofClass) {
        return items.values().stream().filter( {
                it -> ofClass.isInstance(it)    }
        );
    }

    SinatraItem getItemByFullId(String sID) {
        return items.get(sID);
    }
}
