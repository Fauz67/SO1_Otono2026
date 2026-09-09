import java.util.Vector;
import java.util.Queue;
import java.util.LinkedList;
import java.io.*;

public class SchedulingAlgorithm {

  // Punto de entrada invocado por Scheduling.java
  public static Results Run(int runtime, Vector processVector, Results result) {
    // Para ejecutar Round Robin:
    //return runRoundRobin(runtime, processVector, result);

    // Para comparar con FCFS generado por IA, descomenta la siguiente linea y comenta la anterior:
     return runFCFS(runtime, processVector, result);
  }

  // Metodo auxiliar para calcular el quantum promedio en base a los intervalos de E/S
  private static int calculateIOQuantum(Vector processVector) {
    int totalIO = 0;
    int ioProcessCount = 0;

    for (int i = 0; i < processVector.size(); i++) {
      sProcess p = (sProcess) processVector.elementAt(i);
      if (p.ioblocking > 0) {
        totalIO += p.ioblocking;
        ioProcessCount++;
      }
    }

    if (ioProcessCount == 0) {
      return 50;
    }

    return totalIO / ioProcessCount;
  }

  // 1. Implementacion Round Robin (Apropiativo / Preemptive)
  public static Results runRoundRobin(int runtime, Vector processVector, Results result) {
    int comptime = 0;
    int size = processVector.size();
    int completed = 0;
    String resultsFile = "Summary-Processes-RR";

    //int quantum = 80;
    int quantum = calculateIOQuantum(processVector);
    int currentQuantum = 0;

    result.schedulingType = "Interactivo (Apropiativo)";
    result.schedulingName = "Round Robin (Quantum Dinamico de E/S: " + quantum + "ms)";

    Queue<Integer> readyQueue = new LinkedList<>();
    for (int i = 0; i < size; i++) {
      readyQueue.add(i);
    }

    try {
      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
      
      int currentProcess = readyQueue.poll();
      sProcess process = (sProcess) processVector.elementAt(currentProcess);
      out.println("Proceso: " + currentProcess + " registrado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");

      while (comptime < runtime) {
        // Evento 1: Terminacion
        if (process.cpudone == process.cputime) {
          completed++;
          out.println("Proceso: " + currentProcess + " completado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          
          if (completed == size) {
            result.compuTime = comptime;
            out.close();
            return result;
          }

          currentProcess = readyQueue.poll();
          process = (sProcess) processVector.elementAt(currentProcess);
          currentQuantum = 0;
          out.println("Proceso: " + currentProcess + " registrado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
        }

        // Evento 2: Bloqueo por E/S
        if (process.ioblocking > 0 && process.ioblocking == process.ionext) {
          out.println("Proceso: " + currentProcess + " I/O bloqueado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          process.numblocked++;
          process.ionext = 0;

          readyQueue.add(currentProcess);
          currentProcess = readyQueue.poll();
          process = (sProcess) processVector.elementAt(currentProcess);
          currentQuantum = 0;
          out.println("Proceso: " + currentProcess + " registrado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
        }

        // Evento 3: Expiracion del Quantum
        if (currentQuantum >= quantum) {
          if (!readyQueue.isEmpty()) {
            out.println("Proceso: " + currentProcess + " desalojado (quantum expirado)... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
            readyQueue.add(currentProcess);
            currentProcess = readyQueue.poll();
            process = (sProcess) processVector.elementAt(currentProcess);
            out.println("Proceso: " + currentProcess + " registrado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          }
          currentQuantum = 0;
        }

        // Avance de tiempo
        process.cpudone++;
        if (process.ioblocking > 0) {
          process.ionext++;
        }
        currentQuantum++;
        comptime++;
      }
      out.close();
    } catch (IOException e) { /* Ignorado por simplicidad */ }

    result.compuTime = comptime;
    return result;
  }

  // 2. Implementacion FCFS generado por IA (No apropiativo / Non-preemptive)
  public static Results runFCFS(int runtime, Vector processVector, Results result) {
    int comptime = 0;
    int currentProcess = 0;
    int size = processVector.size();
    int completed = 0;
    String resultsFile = "Summary-Processes-FCFS";
    boolean[] isDone = new boolean[size];

    result.schedulingType = "Por Lotes (No apropiativo)";
    result.schedulingName = "First-Come, First-Served (Generado por IA)";

    try {
      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
      sProcess process = (sProcess) processVector.elementAt(currentProcess);
      out.println("Proceso: " + currentProcess + " registrado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");

      while (comptime < runtime) {
        if (completed == size) {
          result.compuTime = comptime;
          out.close();
          return result;
        }

        process = (sProcess) processVector.elementAt(currentProcess);

        // Proceso terminado
        if (isDone[currentProcess] || process.cpudone >= process.cputime) {
          if (!isDone[currentProcess]) {
            isDone[currentProcess] = true;
            completed++;
            out.println("Proceso: " + currentProcess + " completado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          }
          currentProcess = (currentProcess + 1) % size;
          if (completed == size) break;
          continue;
        }

        // Bloqueo por E/S
        if (process.ioblocking > 0 && process.ionext == process.ioblocking) {
          out.println("Proceso: " + currentProcess + " I/O bloqueado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          process.numblocked++;
          process.ionext = 0;
          currentProcess = (currentProcess + 1) % size;
          process = (sProcess) processVector.elementAt(currentProcess);
          out.println("Proceso: " + currentProcess + " registrado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          continue;
        }

        // Ejecucion continua (sin quantum)
        process.cpudone++;
        if (process.ioblocking > 0) {
          process.ionext++;
        }
        comptime++;

        // Verificacion de finalizacion tras consumir el ciclo
        if (process.cpudone == process.cputime) {
          isDone[currentProcess] = true;
          completed++;
          out.println("Proceso: " + currentProcess + " completado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          currentProcess = (currentProcess + 1) % size;
          if (completed < size) {
            process = (sProcess) processVector.elementAt(currentProcess);
            out.println("Proceso: " + currentProcess + " registrado... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          }
        }
      }
      out.close();
    } catch (IOException e) { /* Ignorado por simplicidad */ }

    result.compuTime = comptime;
    return result;
  }
}