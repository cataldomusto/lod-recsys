package di.uniba.it.lodrecsys.utils;

import java.io.*;

/**
 * Created by asuglia on 4/22/14.
 */
public class CmdExecutor {
    /**
     * Executes a specific command to the BASH and save the results printed on the
     * stdout into a file whose name is the one specified in input.
     *
     * @param command        the command that will be executed
     * @param resultFilename the file that will contain the output of the command
     */
    public static void executeCommandAndPrint(String command, String resultFilename) {

        StringBuilder builder = new StringBuilder("");
        Process p;
        try {
            p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(resultFilename));
            writer.write(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert writer != null;
            writer.close();
        }
    }

    /**
     * Executes a specific command to the BASH.
     *
     * @param command the command that will be executed
     */
    public static void executeCommand(String command, boolean printOnShell) {
        Process p;
        try {
            p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            p.waitFor();

            if (printOnShell) {
                StringBuilder builder = new StringBuilder("");

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                System.out.println(builder.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
