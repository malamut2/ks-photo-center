package de.wolfgangkronberg.kspc.edit;

/**
 * Represents the common interface of all on-screen user edit actions, like rotate, crop, paint, etc.
 */
public interface EditAction {

    void confirm();
    void cancel();

}
