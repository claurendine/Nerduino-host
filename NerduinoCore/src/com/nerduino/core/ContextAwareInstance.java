/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.core;

import org.openide.util.Lookup;

public interface ContextAwareInstance<T> {

    T createContextAwareInstance(Lookup context);
    
}
