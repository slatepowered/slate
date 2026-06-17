package com.orbyfied.slate.loader;

import com.orbyfied.slate.util.reflect.ClassLoaders;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;

public class PriorityURLClassLoader extends URLClassLoader {

  public PriorityURLClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  public PriorityURLClassLoader(URL[] urls) {
    super(urls);
  }

  public PriorityURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
    super(urls, parent, factory);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> kl;

    // try to find this class loaded in any parent loader
    ClassLoader cl = this;
    while ((kl = ClassLoaders.findLoadedClass(cl, name)) == null && (cl = ClassLoaders.getParent(cl)) != null) ;

    if (kl == null) {
      try {
        synchronized (this) {
          // first try to load class from URLs
          kl = findClass(name);
        }
      } catch (ClassNotFoundException ex) {
        kl = getParent().loadClass(name);
      }
    }

    // resolve at the end
    if (resolve) {
      resolveClass(kl);
    }

    return kl;
  }

  public void addLoaded(Iterable<URL> urls) {
    for (URL u : urls) {
      addURL(u);
    }
  }

}