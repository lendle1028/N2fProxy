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
        args=new String[]{"8", "1000", "/home/lendle/Desktop/test/angle0", "0", "0", "360"};
        N2fExecutor executor=new N2fExecutorImpl(new File("/home/lendle/dev/projects/1101/jzy3d_test/n2ftools"));
        try{
            executor.init(new File(args[2]));
        }catch(Throwable e){}
        executor.addN2fExecutorListener(new N2fExecutorListener(){
            public void statusUpdated(N2fExecutorEvent e){
                System.out.println(e.getMessage());
            }
        });
        executor.execute(Long.valueOf(args[0]), Long.valueOf(args[1]), Double.valueOf(args[3]), Double.valueOf(args[4]), Integer.valueOf(args[5]));
        System.out.println(Arrays.toString(executor.getResults()));
//        executor.close();
    }
    
}
