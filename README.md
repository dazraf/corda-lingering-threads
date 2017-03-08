# Corda Node issue with lingering threads
 
This project reproduces the issue.

## Tools used:

* JDK 1.8.0_112
* [Maven 3.3.9](https://archive.apache.org/dist/maven/maven-3/3.3.9/binaries/)

## How to reproduce

In a terminal:

```
cd <this-directory>
mvn clean compile exec:exec
```

`stdout` will look like this:

``` 
Node started. To see the stack, in a separate terminal run 
jstack -l 15513
Press ENTER to shutdown
```

Note the line: `jstack -l 15513`. You can execute this command in a terminal get a stack dump of the process' threads.

Hit `ENTER`. This should show:
```
Shutting down ...
Node stopped. Your process *should* now complete.
```

## Observations

The process doesn't shutdown.

In a terminal, running `jstack -l <pid>` gives the following (with elided stacks):

```
➤ jstack -l 15513                                                                                                               
Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.112-b16 mixed mode):

"Attach Listener" #83 daemon prio=9 os_prio=31 tid=0x00007fd61515c800 nid=0x8b07 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"DestroyJavaVM" #69 prio=5 os_prio=31 tid=0x00007fd615149800 nid=0x1c03 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"Thread-4 (ActiveMQ-client-global-scheduled-threads-3097073)" #54 daemon prio=5 os_prio=31 tid=0x00007fd613ca2800 nid=0xf07 waiting on condition [0x000070000a85d000]
   java.lang.Thread.State: WAITING (parking)

   Locked ownable synchronizers:
        - None

"WebServer" #48 prio=5 os_prio=31 tid=0x00007fd612bff800 nid=0x8503 waiting on condition [0x000070000a451000]
   java.lang.Thread.State: WAITING (parking)

   Locked ownable synchronizers:
        - None

"TestNode Messaging" #45 daemon prio=5 os_prio=31 tid=0x00007fd61080e800 nid=0x7f03 waiting on condition [0x000070000a148000]
   java.lang.Thread.State: WAITING (parking)

   Locked ownable synchronizers:
        - None

"Thread-3 (ActiveMQ-client-global-scheduled-threads-3097073)" #43 daemon prio=5 os_prio=31 tid=0x00007fd611b53800 nid=0x7b03 waiting on condition [0x0000700009f42000]
   java.lang.Thread.State: WAITING (parking)

   Locked ownable synchronizers:
        - None

"Thread-2 (ActiveMQ-client-global-scheduled-threads-3097073)" #42 daemon prio=5 os_prio=31 tid=0x00007fd6115ba800 nid=0x7903 waiting on condition [0x0000700009e3f000]
   java.lang.Thread.State: WAITING (parking)

   Locked ownable synchronizers:
        - None

"Thread-0 (ActiveMQ-client-global-scheduled-threads-3097073)" #40 daemon prio=5 os_prio=31 tid=0x00007fd610429000 nid=0x7703 waiting on condition [0x0000700009d3c000]
   java.lang.Thread.State: WAITING (parking)

   Locked ownable synchronizers:
        - None

"Thread-1 (ActiveMQ-client-global-scheduled-threads-3097073)" #41 daemon prio=5 os_prio=31 tid=0x00007fd613625000 nid=0x7503 waiting on condition [0x0000700009c39000]
   java.lang.Thread.State: WAITING (parking)

   Locked ownable synchronizers:
        - None

"threadDeathWatcher-2-1" #38 daemon prio=1 os_prio=31 tid=0x00007fd612b89000 nid=0x7103 waiting on condition [0x0000700009a33000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)

   Locked ownable synchronizers:
        - None

"FiberTimedScheduler-Same thread scheduler" #21 daemon prio=5 os_prio=31 tid=0x00007fd611e29000 nid=0x5503 waiting on condition [0x0000700008c09000]
   java.lang.Thread.State: TIMED_WAITING (parking)

   Locked ownable synchronizers:
        - None

"H2 TCP Server (tcp://172.20.10.2:56718)" #11 daemon prio=5 os_prio=31 tid=0x00007fd6104ee800 nid=0x4d03 runnable [0x00007000087fd000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"Service Thread" #8 daemon prio=9 os_prio=31 tid=0x00007fd611800000 nid=0x4903 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"C1 CompilerThread2" #7 daemon prio=9 os_prio=31 tid=0x00007fd61007c000 nid=0x4703 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"C2 CompilerThread1" #6 daemon prio=9 os_prio=31 tid=0x00007fd611801000 nid=0x4503 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"C2 CompilerThread0" #5 daemon prio=9 os_prio=31 tid=0x00007fd610837000 nid=0x4303 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"Signal Dispatcher" #4 daemon prio=9 os_prio=31 tid=0x00007fd610836000 nid=0x4103 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"Finalizer" #3 daemon prio=8 os_prio=31 tid=0x00007fd610803800 nid=0x3103 in Object.wait() [0x00007000080e8000]
   java.lang.Thread.State: WAITING (on object monitor)

   Locked ownable synchronizers:
        - None

"Reference Handler" #2 daemon prio=10 os_prio=31 tid=0x00007fd610065000 nid=0x2f03 in Object.wait() [0x0000700007fe5000]
   java.lang.Thread.State: WAITING (on object monitor)

   Locked ownable synchronizers:
        - None

"VM Thread" os_prio=31 tid=0x00007fd610803000 nid=0x2d03 runnable 

"GC task thread#0 (ParallelGC)" os_prio=31 tid=0x00007fd610025800 nid=0x2503 runnable 

"GC task thread#1 (ParallelGC)" os_prio=31 tid=0x00007fd610026000 nid=0x2703 runnable 

"GC task thread#2 (ParallelGC)" os_prio=31 tid=0x00007fd610801000 nid=0x2903 runnable 

"GC task thread#3 (ParallelGC)" os_prio=31 tid=0x00007fd610802000 nid=0x2b03 runnable 

"VM Periodic Task Thread" os_prio=31 tid=0x00007fd61007d000 nid=0x4b03 waiting on condition 

JNI global references: 342

➤   
```

A quick review shows the `WebServer` thread could be the non-daemon thread being a potential culprit.
 