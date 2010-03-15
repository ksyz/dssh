package dssh.scripting;


import bsh.EvalError;
import bsh.Interpreter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jurajbednar
 */
public class BshScriptLoader implements ScriptLoader {
/*
 * DSSH Beanshell Script Loader
 */

    public BshScriptLoader() {
    }

    @Override
    public Object getScriptInstance(String scriptname) throws IOException {
        File scriptFileName = new File(scriptname);
        
        
        Interpreter i = new Interpreter();
        try {
            return ((Class) i.source(scriptname)).newInstance();


        } catch (FileNotFoundException ex) {
            Logger.getLogger(BshScriptLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EvalError ex) {
            Logger.getLogger(BshScriptLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(BshScriptLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(BshScriptLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;

    }

}
