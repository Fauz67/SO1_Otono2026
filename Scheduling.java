// This file contains the main() function for the Scheduling
// simulation.  Init() initializes most of the variables by
// reading from a provided file.  SchedulingAlgorithm.Run() is
// called from main() to run the simulation.  Summary-Results
// is where the summary results are written, and Summary-Processes
// is where the process scheduling summary is written.

// Created by Alexander Reeder, 2001 January 06

import java.io.*;
import java.util.*;
//import sProcess;
//import Common;
//import Results;
//import SchedulingAlgorithm;

public class Scheduling {

  private static int processnum = 5;
  private static int meanDev = 1000;
  private static int standardDev = 100;
  private static int runtime = 1000;
  private static Vector processVector = new Vector();
  private static Results result = new Results("null","null",0);
  private static String resultsFile = "Summary-Results";

  private static void Init(String file) {
    File f = new File(file);
    String line;
    String tmp;
    int cputime = 0;
    int ioblocking = 0;
    double X = 0.0;

    try {   
      //BufferedReader in = new BufferedReader(new FileReader(f));
      DataInputStream in = new DataInputStream(new FileInputStream(f));
      while ((line = in.readLine()) != null) {
        if (line.startsWith("numprocess")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          processnum = Common.s2i(st.nextToken());
        }
        if (line.startsWith("meandev")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          meanDev = Common.s2i(st.nextToken());
        }
        if (line.startsWith("standdev")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          standardDev = Common.s2i(st.nextToken());
        }
        if (line.startsWith("process")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          ioblocking = Common.s2i(st.nextToken());
          X = Common.R1();
          while (X == -1.0) {
            X = Common.R1();
          }
          X = X * standardDev;
          cputime = (int) X + meanDev;
          processVector.addElement(new sProcess(cputime, ioblocking, 0, 0, 0));          
        }
        if (line.startsWith("runtime")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          runtime = Common.s2i(st.nextToken());
        }
      }
      in.close();
    } catch (IOException e) { /* Handle exceptions */ }
  }

  private static void debug() {
    int i = 0;

    System.out.println("processnum " + processnum);
    System.out.println("meandevm " + meanDev);
    System.out.println("standdev " + standardDev);
    int size = processVector.size();
    for (i = 0; i < size; i++) {
      sProcess process = (sProcess) processVector.elementAt(i);
      System.out.println("process " + i + " " + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.numblocked);
    }
    System.out.println("runtime " + runtime);
  }

  public static void main(String[] args) {
    int i = 0;

    if (args.length != 1) {
      System.out.println("Usage: 'java Scheduling <INIT FILE>'");
      System.exit(-1);
    }
    File f = new File(args[0]);
    if (!(f.exists())) {
      System.out.println("Scheduling: error, file '" + f.getName() + "' does not exist.");
      System.exit(-1);
    }  
    if (!(f.canRead())) {
      System.out.println("Scheduling: error, read of " + f.getName() + " failed.");
      System.exit(-1);
    }
    System.out.println("Working...");
    Init(args[0]);
    if (processVector.size() < processnum) {
      i = 0;
      while (processVector.size() < processnum) {       
          double X = Common.R1();
          while (X == -1.0) {
            X = Common.R1();
          }
          X = X * standardDev;
        int cputime = (int) X + meanDev;
        processVector.addElement(new sProcess(cputime,i*100,0,0,0));          
        i++;
      }
    }
    //result = SchedulingAlgorithm.Run(runtime, processVector, result);
    //result = SchedulingAlgorithm.Run(runtime, processVector, result);        
    try {
      // 1. Clonacion profunda del vector de procesos para FCFS
      Vector processVectorFCFS = new Vector();
      for (i = 0; i < processVector.size(); i++) {
        sProcess p = (sProcess) processVector.elementAt(i);
        processVectorFCFS.addElement(new sProcess(p.cputime, p.ioblocking, 0, 0, 0));
      }

      // -------------------------------------------------------------
      // 2. EJECUCION Y REPORTE: ROUND ROBIN
      // -------------------------------------------------------------
      Results resultRR = new Results("null", "null", 0);
      resultRR = SchedulingAlgorithm.runRoundRobin(runtime, processVector, resultRR);

      PrintStream outRR = new PrintStream(new FileOutputStream("Summary-Results-RR"));
      outRR.println("Tipo de Planificacion: " + resultRR.schedulingType);
      outRR.println("Nombre del Algoritmo: " + resultRR.schedulingName);
      outRR.println("Tiempo de Ejecucion de Simulacion: " + resultRR.compuTime);
      outRR.println("Media: " + meanDev);
      outRR.println("Desviacion Estandar: " + standardDev);
      outRR.println("Proceso #\tTiempo CPU\tBloqueo E/S\tCPU Completado\tVeces Bloqueado");

      for (i = 0; i < processVector.size(); i++) {
        sProcess process = (sProcess) processVector.elementAt(i);
        outRR.print(Integer.toString(i));
        if (i < 100) { outRR.print("\t\t"); } else { outRR.print("\t"); }
        outRR.print(Integer.toString(process.cputime));
        if (process.cputime < 100) { outRR.print(" (ms)\t\t"); } else { outRR.print(" (ms)\t"); }
        outRR.print(Integer.toString(process.ioblocking));
        if (process.ioblocking < 100) { outRR.print(" (ms)\t\t"); } else { outRR.print(" (ms)\t"); }
        outRR.print(Integer.toString(process.cpudone));
        if (process.cpudone < 100) { outRR.print(" (ms)\t\t"); } else { outRR.print(" (ms)\t"); }
        outRR.println(process.numblocked + " veces");
      }
      outRR.close();

      // -------------------------------------------------------------
      // 3. EJECUCION Y REPORTE: FCFS (IA)
      // -------------------------------------------------------------
      Results resultFCFS = new Results("null", "null", 0);
      resultFCFS = SchedulingAlgorithm.runFCFS(runtime, processVectorFCFS, resultFCFS);

      PrintStream outFCFS = new PrintStream(new FileOutputStream("Summary-Results-FCFS"));
      outFCFS.println("Tipo de Planificacion: " + resultFCFS.schedulingType);
      outFCFS.println("Nombre del Algoritmo: " + resultFCFS.schedulingName);
      outFCFS.println("Tiempo de Ejecucion de Simulacion: " + resultFCFS.compuTime);
      outFCFS.println("Media: " + meanDev);
      outFCFS.println("Desviacion Estandar: " + standardDev);
      outFCFS.println("Proceso #\tTiempo CPU\tBloqueo E/S\tCPU Completado\tVeces Bloqueado");

      for (i = 0; i < processVectorFCFS.size(); i++) {
        sProcess process = (sProcess) processVectorFCFS.elementAt(i);
        outFCFS.print(Integer.toString(i));
        if (i < 100) { outFCFS.print("\t\t"); } else { outFCFS.print("\t"); }
        outFCFS.print(Integer.toString(process.cputime));
        if (process.cputime < 100) { outFCFS.print(" (ms)\t\t"); } else { outFCFS.print(" (ms)\t"); }
        outFCFS.print(Integer.toString(process.ioblocking));
        if (process.ioblocking < 100) { outFCFS.print(" (ms)\t\t"); } else { outFCFS.print(" (ms)\t"); }
        outFCFS.print(Integer.toString(process.cpudone));
        if (process.cpudone < 100) { outFCFS.print(" (ms)\t\t"); } else { outFCFS.print(" (ms)\t"); }
        outFCFS.println(process.numblocked + " veces");
      }
      outFCFS.close();

    } catch (IOException e) {
      System.out.println("Error al procesar archivos de salida: " + e.getMessage());
    }
  System.out.println("Completed.");
  }
}

