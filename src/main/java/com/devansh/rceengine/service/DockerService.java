package com.devansh.rceengine.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DockerService {
    private DockerClient dockerClient;
    private String language;

    public DockerService() {
        String dockerHost = "tcp://localhost:2375";
        log.info("Connecting to Docker at: {}", dockerHost);

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);

        try {
            dockerClient.pingCmd().exec();
            log.info("Successfully connected to Docker!");
        } catch (Exception e) {
            log.error("Failed to connect to Docker. Please ensure Docker is running.", e);
            throw new RuntimeException("Docker is not available. Please start Docker Desktop.", e);
        }
    }

    public String[] runCode(String code, String stdin, String language) {
        this.language = language;
        String containerId = null;
        try {
            log.info("Executing {} code: {}", language, code);

            CreateContainerResponse container = dockerClient.createContainerCmd(getBaseImage())
                    .withTty(false)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("sh", "-c", "tail -f /dev/null")
                    .exec();

            containerId = container.getId();
            log.info("Created Container: {}", containerId);

            dockerClient.startContainerCmd(containerId).exec();
            log.info("Started Container: {}", containerId);

            // Add small delay to ensure container is ready
            Thread.sleep(1000);

            String shellCommand = getShellCommand(code, stdin);
            log.info("Shell command: {}", shellCommand);

            String[] command = {"/bin/sh", "-c", shellCommand};

            final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStdin(stdin != null)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd(command)
                    .exec();

            String execId = execCreateCmdResponse.getId();
            log.info("Created exec instance: {}", execId);

            // Use the modern ResultCallback.Adapter approach
            ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                    if (frame != null && frame.getPayload() != null) {
                        try {
                            byte[] payload = frame.getPayload();
                            switch (frame.getStreamType()) {
                                case STDOUT:
                                case RAW:
                                    stdout.write(payload);
                                    break;
                                case STDERR:
                                    stderr.write(payload);
                                    break;
                            }
                        } catch (IOException e) {
                            log.warn("Error writing frame payload", e);
                        }
                    }
                }

                @Override
                public void onComplete() {
                    super.onComplete();
                    log.info("Execution completed successfully");
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("Execution error", throwable);
                    super.onError(throwable);
                }
            };

            long startTime = System.nanoTime();
            // Execute and wait for completion
            dockerClient.execStartCmd(execId).exec(callback).awaitCompletion(30, TimeUnit.SECONDS);

            long endTime = System.nanoTime();

            double executionTimeSeconds = (endTime - startTime) / 1_000_000_000.0;
            log.info("Execution time: {} seconds", executionTimeSeconds);

            String result = stdout.toString("UTF-8");
            String errorOutput = stderr.toString("UTF-8");

            log.info("STDOUT: '{}'", result);
            log.info("STDERR: '{}'", errorOutput);

            String[]outputAndTime = new String[2];
            outputAndTime[1] = String.format("%.3f Seconds",executionTimeSeconds);

            if (!errorOutput.isEmpty()) {
                outputAndTime[0] = "STDERR:\n" + errorOutput + "\nSTDOUT:\n" + result;
            }
            else {
                outputAndTime[0] = result.isEmpty() ? "(No output)" : result;
            }
            return outputAndTime;

        } catch (Exception e) {
            log.error("Failed to execute code in container", e);
            return new String[]{"Error: " + e.getMessage(), "0 Seconds"};
        } finally {
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                    log.info("Removed Container: {}", containerId);
                } catch (Exception e) {
                    log.error("Could not remove container: {}", containerId, e);
                }
            }
        }
    }

    public String getBaseImage() {
        return switch (this.language.toLowerCase()) {
            case "python" -> "python:3.10-alpine";
            case "javascript" -> "node:18-alpine";
            case "java" -> "openjdk:17-alpine";
            case "cpp" -> "gcc:latest";
            default -> "python:3.10-alpine";
        };
    }

    public String getShellCommand(String code, String stdin) {
        String template = switch (language.toLowerCase()) {
            case "python" -> "echo \"%INPUT%\" | python3 -c \"%CODE%\"";
            case "javascript" -> "echo \"%INPUT%\" | node -e \"%CODE%\"";
            case "java" -> """
                echo "%CODE%" > Main.java &&
                javac Main.java &&
                echo "%INPUT%" | java Main
                """;
            case "cpp" -> """
                echo "%CODE%" > Main.cpp &&
                g++ Main.cpp -o Main &&
                echo "%INPUT%" | ./Main
                """;
            default -> "echo \"%INPUT%\" | python3 -c \"%CODE%\"";
        };

        String escapedCode = code.replace("\"", "\\\"").replace("$", "\\$");
        String escapedInput = stdin != null ? stdin.replace("\"", "\\\"").replace("$", "\\$") : "";

        return template
                .replace("%CODE%", escapedCode)
                .replace("%INPUT%", escapedInput);
    }
}