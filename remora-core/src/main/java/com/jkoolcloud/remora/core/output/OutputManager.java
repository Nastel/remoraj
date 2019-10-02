package com.jkoolcloud.remora.core.output;

import com.jkoolcloud.remora.RemoraConfig;
import com.jkoolcloud.remora.core.EntryDefinition;

import java.util.ArrayList;
import java.util.List;

import static com.jkoolcloud.remora.core.utils.LoggerWrapper.pLog;

public enum OutputManager {
    INSTANCE;

    private static boolean shutdown = false;
    private static AgentOutput output;
    private static List<AgentOutput.OutputListener> outputListeners;

    OutputManager() {
        install();
    }

    public static void install() {
        pLog("Starting OutputManager");
        outputListeners = new ArrayList<>();
        String outputClass = System.getProperty("probe.output");
        if (outputClass != null) {
            try {
                Class<?> outClass = Class.forName(outputClass);
                output = (AgentOutput) outClass.newInstance();
            } catch (Exception e) {
                outputListeners.forEach(l -> l.onInitialized(e));
            }
            outputListeners.forEach(l -> l.onInitialized(null));
        } else {
            output = new ChronicleOutput();
        }
        RemoraConfig.configure(output);
        synchronized (output) {
            try {
                outputListeners.forEach(listener -> listener.onInitialize());
                output.init();
            } catch (AgentOutput.OutputException e) {
                outputListeners.forEach(l -> l.onInitialized(e));
            }
            outputListeners.forEach(l -> l.onInitialized(null));

        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (output) {
                shutdown = true;
                outputListeners.forEach(l -> l.onShutdown());
                output.shutdown();
            }
        }));

    }

    public static void send(EntryDefinition entryDefinition) {
        if (output != null) {
            outputListeners.forEach(l -> l.onSend());
            output.send(entryDefinition);
        } else {
            outputListeners.forEach(l -> l.onSent(new AgentOutput.OutputException("No output initialsed")));
        }
    }

    public static interface AgentOutput<T> {
        void init() throws OutputException;

        void send(T entry);

        void shutdown();

        class OutputException extends Exception {
            public OutputException(String message) {
                super(message);
            }
        }

        interface OutputListener {
            /**
             * Will be triggered on OutputManager before handling it to real AgentOutputs
             */
            void beforeSend();

            /**
             * Will be triggered on initialising the output
             */
            void onInitialize();

            /**
             * Wil be triggered after AgentOutputs initializes
             *
             * @param e Exception would be filled if one occurs
             */
            void onInitialized(Exception e);

            /**
             * Will be triggered at AgentOutputs send
             */

            void onSend();

            /**
             * Will be triggered on AgentsOutput shutdown
             */

            void onShutdown();

            /**
             * Will be trigered when sennding finished
             *
             * @param e Exception would be filled if one occurs on sending
             */
            void onSent(Exception e);

            class OutputLogger implements OutputListener {

                @Override
                public void beforeSend() {

                }

                @Override
                public void onInitialize() {

                }

                @Override
                public void onInitialized(Exception e) {

                }

                @Override
                public void onSend() {

                }

                @Override
                public void onShutdown() {
                    pLog("Shutting down: ");
                }

                @Override
                public void onSent(Exception e) {
                    if (e == null) {
                        pLog("Message sent");
                    } else {
                        pLog("Cannot send the message");
                    }
                }

            }


        }

    }
}
