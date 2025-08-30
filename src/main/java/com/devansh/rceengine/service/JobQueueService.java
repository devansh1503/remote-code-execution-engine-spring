package com.devansh.rceengine.service;

import com.devansh.rceengine.dto.CodeExecutionRequest;
import com.devansh.rceengine.dto.CodeJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobQueueService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String JOB_QUEUE_KEY = "jobs:queue";
    private static final String JOB_STORE_KEY = "jobs:metadata";

    public CodeJob submitJob(CodeExecutionRequest request){
        String jobId = UUID.randomUUID().toString();
        CodeJob job = new CodeJob();
        job.setId(jobId);
        job.setStatus(CodeJob.Status.SUBMITTED);
        job.setRequest(request);

        redisTemplate.opsForHash().put(JOB_STORE_KEY, jobId, job);
        log.info("Saved job {} to store", jobId);

        redisTemplate.opsForList().rightPush(JOB_QUEUE_KEY, jobId);
        log.info("Job {} added to queue", jobId);

        return job;
    }

    public String getNextJobId(){
        return (String) redisTemplate.opsForList().leftPop(JOB_QUEUE_KEY);
    }

    public void updateJob(CodeJob job){
        redisTemplate.opsForHash().put(JOB_STORE_KEY, job.getId(), job);
    }

    public CodeJob getJob(String jobId){
        return (CodeJob) redisTemplate.opsForHash().get(JOB_STORE_KEY, jobId);
    }
}
