package com.archinamon.api

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException

class AspectJWeaver {

    def private static final errorReminder = "Look into %s file for details";

    private Project project;

    String logFile;
    String encoding;

    boolean weaveInfo;
    boolean debugInfo;
    boolean addSerialVUID;
    boolean noInlineAround;
    boolean ignoreErrors;

    boolean breakOnError;
    boolean experimental;

    ArrayList<File> ajSources = new ArrayList<>();
    ArrayList<File> aspectPath = new ArrayList<>();
    ArrayList<File> inPath = new ArrayList<>();
    ArrayList<File> classPath = new ArrayList<>();
    String bootClasspath;
    String sourceCompatibility;
    String targetCompatibility;
    String destinationDir;

    AspectJWeaver(Project project) {
        this.project = project;
    }

    protected void doWeave() {
        final def log = project.logger;

        //http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html

        def args = [
                "-encoding", encoding,
                "-source", sourceCompatibility,
                "-target", targetCompatibility,
                "-d", destinationDir,
                "-bootclasspath", bootClasspath,
                "-classpath", classPath.join(File.pathSeparator),
                "-sourceroots", ajSources.join(File.pathSeparator),
                "-inpath", inPath.join(File.pathSeparator)
        ];

        if (!aspectPath.empty) {
            args << "-aspectpath" << aspectPath.join(File.pathSeparator);
        }

        if (!logFile?.isEmpty()) {
            args << "-log" << logFile;
        }

        if (debugInfo) {
            args << "-g";
        }

        if (weaveInfo) {
            args << "-showWeaveInfo";
        }

        if (addSerialVUID) {
            args << "-XaddSerialVersionUID";
        }

        if (noInlineAround) {
            args << "-XnoInline";
        }

        if (ignoreErrors) {
            args << "-proceedOnError" << "-noImportError";
        }

        if (experimental) {
            args << "-XhasMember" << "-Xjoinpoints:synchronization,arrayconstruction";
        }

        log.warn "ajc args: " + Arrays.toString(args as String[]);
        prepareLogger();

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args as String[], handler);
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ERROR:
                    log.error message?.message, message?.thrown;
                    if (breakOnError) throw new GradleException(String.format(errorReminder, logFile));
                    break;
                case IMessage.FAIL:
                case IMessage.ABORT:
                    log.error message?.message, message?.thrown;
                    throw new GradleException(message?.message);
                case IMessage.INFO:
                case IMessage.DEBUG:
                case IMessage.WARNING:
                    log.warn message?.message, message?.thrown;
                    if (!logFile?.empty) log.error(errorReminder, logFile);
                    break;
            }
        }

        detectErrors();
    }

    void setLogFile(String name) {
        if (name != null && name.length() > 0)
            this.logFile = project.buildDir.absolutePath + File.separator + name;
    }

    void setAjSources(File... ajSources) {
        for (File input : ajSources) {
            if (!this.ajSources.contains(input)) {
                this.ajSources.add(input);
            }
        }
    }

    def private prepareLogger() {
        File lf = new File(logFile);
        if (lf.exists()) {
            lf.delete();
        }
    }

    def private detectErrors() {
        File lf = new File(logFile);
        if (lf.exists()) {
            lf.readLines().reverseEach { String line ->
                if (line.contains("[error]") && breakOnError) {
                    throw new GradleException(String.format(errorReminder, logFile));
                }
            }
        }
    }
}