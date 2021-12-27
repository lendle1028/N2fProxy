/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.n2fproxy;

import java.io.File;

/**
 *
 * @author lendle
 */
public interface N2fExecutor {
    public void addN2fExecutorListener(N2fExecutorListener l);
    public void init(File dataDirectory);
    public void execute(long numSubspaces, long frequency, double theta, double phi, int delta);
    public void close();
    public double [] getResults();
    public double getRCSTotal();
}
