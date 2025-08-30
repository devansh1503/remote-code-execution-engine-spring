package com.devansh.rceengine.service;

import com.devansh.rceengine.dto.CodeExecutionResult;
import com.devansh.rceengine.dto.CodeJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class CodeWorker {

    private final JobQueueService jobQueueService;
    private final DockerService dockerService;

    @Scheduled(fixedRate = 5000)
    public void processJobFomQueue(){
        String jobId = jobQueueService.getNextJobId();
        if(jobId == null) return;
        log.info("Processing job {}", jobId);

        CodeJob job = jobQueueService.getJob(jobId);
        if(job==null){
            log.error("Job {} not found", jobId);
            return;
        }

        job.setStatus(CodeJob.Status.RUNNING);
        jobQueueService.updateJob(job);

        CodeExecutionResult result = new CodeExecutionResult();

        try{
            String output = dockerService.runCode(
                    job.getRequest().getCode(),
                    job.getRequest().getStdin(),
                    job.getRequest().getLanguage()
            );
            result.setOutput(output);
            job.setResult(result);
            job.setStatus(CodeJob.Status.COMPLETED);
        } catch (Exception e) {
            log.error("Job {} failed with error :", jobId, e);
            result.setError("Internal Server error: "+e.getMessage());
            job.setResult(result);
            job.setStatus(CodeJob.Status.FAILED);
        } finally {
            jobQueueService.updateJob(job);
            log.info("Job {} finished with status: {}", jobId, job.getStatus());
        }

    }
}
