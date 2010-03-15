/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dssh.scripting;

import java.io.IOException;

/**
 *
 * @author jurajbednar
 */
public interface ScriptLoader {

    Object getScriptInstance(String scriptname) throws IOException;

}
