package com.nirima.snowglobe.core

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.Target

import static java.lang.annotation.RetentionPolicy.RUNTIME

@Target(value = [ElementType.TYPE] )
@Retention(RUNTIME)
public @interface SGItem
{
    String value();
}

// Element with an ID
public interface IElement {
    String getId();
}

public interface ISnowglobe extends IElement {

}

public interface IModule extends IElement {

}

public interface IModuleElement extends IElement {

}

public interface IResource<T extends ResourceState> extends IModuleElement {
    T getState();
    T getSavedState();
}

public interface IProvider extends IModuleElement {

}

@Target(value = [ElementType.FIELD] )
@Retention(RUNTIME)
public @interface NoCompare
{

}