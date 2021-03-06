package com.nirima.snowglobe.core

import groovy.util.logging.Slf4j

@Slf4j
class ComparatorUtils {

    static int compare(Object a, Object b)
    {
        // DO *NOT* call this from within the implementation of compareTo!!!
        if( a == null && b == null )
            return 0;
        if (a == null && b != null)
            return -1;
        if (a != null && b == null)
            return 1;

        if (a.is(b)) {
            return 0
        }
        if( a.getClass().isPrimitive() ) {
            if( a.equals(b) )
                return 0;
            else {
                log.info("${a} to ${b}");
                return -1;
            }
        }
        if (Comparable.class.isAssignableFrom(a.getClass())) {
            return a.compareTo(b);
        }

        if (a.getClass() != b.getClass()) {
            return -1
        }

        return fieldwiseCompare(a,b);
    }

    static int fieldwiseCompare(Object a, Object b) {

        if( a == null && b == null )
            return 0;
        if (a == null && b != null)
            return -1;
        if (a != null && b == null)
            return 1;

        if (a.is(b)) {
            return 0
        }

        if( a.getClass().isPrimitive() ) {
            if( a.equals(b) )
                return 0;
            else {
                log.info("${a} to ${b}");
                return -1;
            }
        }


        if (a.getClass() != b.getClass()) {
            return -1
        }


        def list1 = a.class.declaredFields.findAll { !it.synthetic };


        def list = list1.collect {
            it ->

                if (it.isAnnotationPresent(NoCompare.class)) {
                    return 0
                };

                def l = a[it.name];
                def r = b[it.name];

                if( l == null && r == null)
                    return 0;
                if( l == null && r != null ) {
                    log.info("[${it.name}] ${l} to ${r}");
                    return 1;
                }
                if( r == null && l != null ) {
                    log.info("[${it.name}] ${l} to ${r}");
                    return -1;
                }


                if (Comparable.class.isAssignableFrom(it.type)) {
                    return l.compareTo(r);
                }

                if( List.class.isAssignableFrom(it.type)) {
                    return compareLists(l,r);
                }

                if( Map.class.isAssignableFrom(it.type)) {
                    return compareMaps(l,r);
                }

                if( it.type.isPrimitive() ) {
                    if( l.equals(r) )
                        return 0;
                    else {
                        log.info("[${it.name}] ${l} to ${r}");
                        return -1;
                    }
                }

                log.error("don't know how to compare field {} [examining {} and {}]" ,it.name, a,b);

                return 0;

        };


        log.debug "Compare " + list1;
        log.debug "------- " + list;


        def x = list.find {
            it -> it != 0
        };


        if (x == null) {
            return 0
        };
        return x;


    }

    static int compareLists(List a, List b) {
        if( a.size() != b.size() )
            return a.size() - b.size();

        for(int i=0;i<a.size();i++) {
            int v = compare(a.get(i), b.get(i));
            if( v != 0 )
                return v;
        }
        return 0;
    }

    static int compareMaps(Map a, Map b) {
        if( a.size() != b.size() )
            return a.size() - b.size();


        for(def e : a.entrySet()) {
            if(! b.containsKey(e.key))
                return -1;
            int v = fieldwiseCompare(e.value, b.get(e.key));
            if( v != 0 )
                return v;
        }

        return 0;
    }


}
