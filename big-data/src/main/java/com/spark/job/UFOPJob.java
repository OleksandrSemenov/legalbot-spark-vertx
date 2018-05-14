package com.spark.job;

import com.spark.service.SparkService;
import org.quartz.*;

/**
 * @author Taras Zubrei
 */
public class UFOPJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            SparkService sparkService = (SparkService) schedulerContext.get("SparkService");
            sparkService.parseLastUFOPData(false);
        } catch (SchedulerException ex) {
            throw new JobExecutionException("Failed to fetch scheduler context", ex);
        }
    }
}
