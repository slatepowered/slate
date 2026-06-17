package com.orbyfied.slate.util.lifetime;

/**
 * A resource which is attached to an object, which may be automatically released
 * once the owner lifetime concludes.
 */
public interface Releasable<O> {

    void release(O owner);

}
