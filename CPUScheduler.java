import java.util.LinkedList;
import java.util.Random;

public class CPUScheduler {
	// DO NOT MODIFY THE CLASS Job
	public static class Job {
		int jobId;
		int arrivalTime;
		int burst;
		int remainingBurst;
		int startTime;
		int endTime;
		
		public Job(int jobId, int arrivalTime, int burst) {
			this.jobId = jobId;
			this.arrivalTime = arrivalTime;
			this.burst = burst;
			this.remainingBurst = burst;
			this.startTime = -1;
			this.endTime = -1;
		}
		
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("[");
			result.append(jobId);
			result.append("] ");
			result.append("Burst - ");
			result.append(burst);
			result.append(" Arrival - ");
			result.append(arrivalTime);
			result.append(" Start - ");
			result.append(startTime);
			result.append(" Remaining Burst - ");
			result.append(remainingBurst);
			return result.toString();
		}
		
		public boolean equals(Object o) {
			return ((Job)o).jobId == this.jobId;
		}
	}
	
	// DO NOT MODIFY THE CLASS ReadyQueue
	public static class ReadyQueue {		
		private LinkedList<Job> readyQueue;
		
		public ReadyQueue() {			
			readyQueue = new LinkedList<Job>();
		}
		
		public void enqueue(Job j) {
			readyQueue.add(j);
		}
		
		public Job dequeue() {
			if (readyQueue.isEmpty()) {
				return null;
			} else {
				return readyQueue.poll();
			}
		}
		
		public LinkedList<Job> queue() {
			return readyQueue;
		}
	}
	
	// DO NOT MODIFY THE CLASS CPU
	public static class CPU {
		private ReadyQueue readyQueue;
		private Job running;
		
		private int lastJobCreated;
		private Random rand;
		
		public CPU(ReadyQueue readyQueue) {
			this.readyQueue = readyQueue;
			running = null;
			lastJobCreated = 0;
			rand = new Random();
		}
		
		public void newJob(int sec) {
			// Ensure 20% chance a new job is created.
			if (rand.nextInt(100) < 20) {
				lastJobCreated++;
				Job j = new Job(lastJobCreated, sec, rand.nextInt(10) + 1);
				System.out.println(sec + " === " + j);
				readyQueue.enqueue(j);
			} 
		}
		
		public Job getRunning() { return running; }
		
		public void setRunning(int sec, Job j) {
			System.out.println(sec + " <<< " + running);
			running = j;
			if (j == null) {
				System.out.println(sec + " >>> No Job Scheduled");
			} else {
				System.out.println(sec + " >>> " + j);
			}
		}
		
	}
	
	// DO NOT MODIFY THE INTERFACE Schedule
	public static interface Scheduler {
		public void schedule(int currSec);
	}
	
	// WRITE CODE HERE; UPDATE THE CLASS FIFOScheduler
	public static class FIFOScheduler implements Scheduler {
		CPU c;
		ReadyQueue r;
		
		public FIFOScheduler(CPU c, ReadyQueue r) {
			this.c = c;
			this.r = r;
		}
		
		// WRITE CODE HERE
		public void schedule(int currSec) {
			// Check if no jobs are currently running
	        if (c.getRunning() == null) {
	            Job next = r.dequeue(); // Get the next job from ready queue
	            
	            // This is when a job is ready to execute
	            if (next != null) {
	                next.startTime = currSec;
	                int waitingTime = currSec - next.arrivalTime;
	                int end = currSec + next.burst;
	                next.endTime = end;
	                c.setRunning(currSec, next); // Setting CPU's running job to the next job
	                System.out.println("--------------------------");
	                System.out.println("Job " + next.jobId + " Waiting Time: " + waitingTime);
	                System.out.println("--------------------------");
	                
	            }
	        } 
	        // When a job is currently running
	        else {
	            Job runningJob = c.getRunning();
	            runningJob.remainingBurst -= 1;
	            // The job finished executing 
	            if (runningJob.remainingBurst == 0) {
	            	c.setRunning(currSec, null); // Null to indicate the CPU is idle
	            	int turnaroundTime = runningJob.endTime - runningJob.arrivalTime;
	            	System.out.println("--------------------------");
	            	System.out.println("Job " + runningJob.jobId + " Turnaround Time: " + turnaroundTime);
	            	System.out.println("--------------------------");
	                
	            } 
	        }
	    }
	}
	
	// WRITE CODE HERE; UPDATE THE CLASS SRTF Scheduler 
	public static class SRTFScheduler implements Scheduler {
		CPU c;
		ReadyQueue r;
		
		public SRTFScheduler(CPU c, ReadyQueue r) {
			this.c = c;
			this.r = r;
		}
		
		// WRITE CODE HERE
		public void schedule(int currSec) {
			int waitingTime = 0;
		    Job runningJob = c.getRunning();
		    Job shortestRemainingJob = null;
		    int shortestTime = Integer.MAX_VALUE;

		    // if a job is currently running
		    if (runningJob != null) {
		        runningJob.remainingBurst -= 1;

		        // Check if the running job is completed
		        if (runningJob.remainingBurst == 0) {
		        	c.setRunning(currSec, null);
		        	int turnaroundTime = runningJob.endTime - runningJob.arrivalTime;
		        	System.out.println("--------------------------");
		        	System.out.println("Job " + runningJob.jobId + " Turnaround Time: " + turnaroundTime);
		        	System.out.println("--------------------------");
		            
		        }
		    }

		    // checking the jobs in the ready queue to ensure that the shortest remaining burst job is scheduled first
		    for (Job job : r.queue()) {
		        if (job.arrivalTime <= currSec && job.remainingBurst > 0 && job.remainingBurst < shortestTime) {
		            shortestRemainingJob = job;
		            shortestTime = job.remainingBurst;
		        }
		    }
		    
		    // when shortest remaining job is identified and running
		    if (shortestRemainingJob != null) {
		        if (runningJob == null || shortestRemainingJob.remainingBurst < runningJob.remainingBurst) {
		        	// if there is a job currently running and it is not the shortest job identified, the running job needs to stop and reenter the ready queue
		            if (runningJob != null && runningJob != shortestRemainingJob) {
		                runningJob.endTime = currSec;
		                r.enqueue(runningJob);
		                System.out.println("++++++++++++++++++++++++++");
		                System.out.println("      SWITCHING JOBS      "); // testing purposes to see when jobs are switching
		                System.out.println("++++++++++++++++++++++++++");
		            } 
		            // update shortest remaining job's variables
		            if (shortestRemainingJob.startTime == -1) { // makes sure the starting time of the reentered job doesn't get updated
		            shortestRemainingJob.startTime = currSec;
		            }
		            shortestRemainingJob.endTime = currSec + shortestRemainingJob.remainingBurst;
		            c.setRunning(currSec, shortestRemainingJob);
			        waitingTime = shortestRemainingJob.startTime - shortestRemainingJob.arrivalTime;
			        System.out.println("--------------------------");
			        System.out.println("Job " + shortestRemainingJob.jobId + " Waiting Time: " + waitingTime);
			        System.out.println("--------------------------");
		      }
		   }
		}
	}
	
	// DO NOT MODIFY THE METHOD simulate
	public static void simulate(int length, CPU c, Scheduler s) {
		for (int sec = 0; sec < length; sec++) {
			c.newJob(sec);
			s.schedule(sec);
		}
	}
	
	// TRY OTHER TEST CASES BY UPDATING THE METHOD main
	public static void main(String[] args) {
		int MAX_LENGTH = 100;
		
		System.out.println("==== FIFO ====");
		ReadyQueue r = new ReadyQueue();		
		CPU c = new CPU(r);
		Scheduler f = new FIFOScheduler(c, r);
		simulate(MAX_LENGTH, c, f);
			
		System.out.println("==== SRTF ====");
		r = new ReadyQueue();
		c = new CPU(r);
		Scheduler s = new SRTFScheduler(c, r);
		simulate(MAX_LENGTH, c, s);
	}
}
