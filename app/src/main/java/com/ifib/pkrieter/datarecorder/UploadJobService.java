package com.ifib.pkrieter.datarecorder;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class UploadJobService extends JobService {
    private static final String TAG = "UploadJobService";
    private static final int JOBID = 999;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob called" );

        Intent work = new Intent(this, EventDetectionService.class);
        EventDetectionService.enqueueWork(this, EventDetectionService.class, JOBID, work);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob called" );
        return false;
    }

    public static void scheduleJob(Context context) {
        if(!isJobServiceOn( context )){
            ComponentName serviceComponent = new ComponentName(context, UploadJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOBID, serviceComponent);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setRequiresDeviceIdle(false); // device should be idle, for debugging: deactivate
            builder.setRequiresCharging(true);
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            jobScheduler.schedule(builder.build());
        }
    }

    public static boolean isJobServiceOn( Context context ) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE ) ;
        boolean hasBeenScheduled = false ;
        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() == JOBID ) {
                hasBeenScheduled = true ;
                break ;
            }
        }
        return hasBeenScheduled ;
    }

}