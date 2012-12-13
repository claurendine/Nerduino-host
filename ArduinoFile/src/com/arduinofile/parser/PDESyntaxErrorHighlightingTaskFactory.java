/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arduinofile.parser;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.TaskFactory;

public class PDESyntaxErrorHighlightingTaskFactory extends TaskFactory {

    @Override
    public Collection create (Snapshot snapshot) {
        return Collections.singleton (new PDESyntaxErrorHighlightingTask());
    }

}