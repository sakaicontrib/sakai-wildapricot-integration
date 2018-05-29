package com.longsight.wa.jobs.utils;

import java.util.List;

import org.quartz.JobExecutionContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobUtils {
    public static boolean isJobRunning(JobExecutionContext jobExecutionContext) {
        try {
            List<JobExecutionContext> jobs = jobExecutionContext.getScheduler().getCurrentlyExecutingJobs();
            for (JobExecutionContext job : jobs) {
                if (job.getJobDetail().getKey().getName().equals(jobExecutionContext.getJobDetail().getKey().getName()) && !job.getJobInstance().equals(jobExecutionContext.getJobInstance())) {
                    log.error("Aborting execution: There's another running instance of the job {}.", job.getJobDetail());
                    return true;
                }
            }
        }catch(Exception ex){log.warn("WARNING : "+ex.getMessage());}
        return false;
    }
}
