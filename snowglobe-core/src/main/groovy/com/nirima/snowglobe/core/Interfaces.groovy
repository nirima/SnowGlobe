package com.nirima.snowglobe.core

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