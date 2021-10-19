/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.n2fproxy;

import java.io.File;
import java.util.Arrays;

/**
 *
 * @author lendle
 */
public class Main {

    /**
     * @param args the command line arguments
     * parameters
     * 0: sub spaces
     * 1: mhz
     * 2: data directory
     * 3: theta
     * 4: phi
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        N2fExecutor executor=new N2fExecutorImpl(new File("."));
        executor.init(new File(args[2]));
        executor.addN2fExecutorListener(new N2fExecutorListener(){
            public void statusUpdated(N2fExecutorEvent e){
                System.out.println(e.getMessage());
            }
        });
        executor.execute(Long.valueOf(args[0]), Long.valueOf(args[1]), Double.valueOf(args[3]), Double.valueOf(args[4]));
        System.out.println(Arrays.toString(executor.getResults()));
        executor.close();
    }
    
}
