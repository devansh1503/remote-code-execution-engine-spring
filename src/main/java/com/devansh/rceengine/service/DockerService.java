package com.devansh.rceengine.service;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DockerService {
    private static final Logger log = LoggerFactory.getLogger(DockerService.class);
    //    Had to download docker-java 3.2.1 for DockerClientBuilder
    private final DockerClient dockerClient = DockerClientBuilder.getInstance().build();
    private String language;

    public String runCode(String code, String stdin, String language){
        this.language = language;
        String containerId = null;
        try{
            CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(getBaseImage())
                    .withTty(false)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true);

            CreateContainerResponse container = createContainerCmd.exec();
            containerId = container.getId();
            log.info("Created Container: {}", containerId);

            dockerClient.startContainerCmd(containerId).exec();
            log.info("Started Container: {}", containerId);

            String[] command = {"/bin/sh", "-c", getShellCommand(code, stdin)};

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStdin(stdin!=null)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd(command)
                    .exec();

            String execId = execCreateCmdResponse.getId();

//            ExecStartResultCallback callback = new ExecStartResultCallback(stdout, stderr);

//            Because ExecStartResultCallback is deprecated using Result Callback Adapter
            dockerClient.execStartCmd(execId).exec(new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                    if (frame != null) {
                        try {
                            switch (frame.getStreamType()) {
                                case STDOUT:
                                case RAW:
                                    stdout.write(frame.getPayload());
                                    stdout.flush();
                                    break;
                                case STDERR:
                                    stderr.write(frame.getPayload());
                                    stderr.flush();
                                    break;
                                default:
                                    // Handle other stream types if necessary
                                    break;
                            }
                        } catch (IOException e) {
                            onError(e);
                        }
                    }
                }
            }).awaitCompletion(15, TimeUnit.SECONDS);

            String result = stdout.toString();
            String errorOutput = stderr.toString();

            if(!errorOutput.isEmpty()) {
                result = result = "STDERR:\n" + errorOutput + "\nSTDOUT:\n" + result;
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to execute code in container", e);
            return "Error: " + e.getMessage();
        } finally {
            if (containerId != null){
                try{
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                    log.info("Removed Container: {}", containerId);
                }
                catch (Exception e){
                    log.error("Could not remove container: {}", containerId, e);
                }
            }
        }
    }

    public String getBaseImage(){
        return switch (this.language.toLowerCase()) {
            case "python" -> "python:3.10-alpine";
            case "javascript" -> "node:18-alpine";
            case "java" -> "openjdk:17-alpine";
            case "cpp" -> "gcc";
            default -> "openjdk:17-alpine";
        };
    }

    public String getShellCommand(String code, String stdin){
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
            default -> "echo \"%INPUT%\" | node -e \"%CODE%\"";
        };

        String escapedCode = code.replace("\"", "\\\"");
        String escapedInput = stdin.replace("\"", "\\\"");

        return template
                .replace("%CODE%", escapedCode)
                .replace("%INPUT%", escapedInput);
    }

}
